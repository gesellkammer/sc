RingModDiodeSC {
    // Ring Modulation with a basic simulation of a diode for the modulating signal
    /*
    in : the signal to modulate
    mod_in : the modulating signal. if nil, a sinus tone is used
    mod_dist : apply distortion to the mod-signal
    diode_mul and diode_add : the diode is implemented as (mod_in.abs * diode_mul) - diode_add
    mix_diode : cross-fade between the mod-signal with and without the diode distortion
    os_mul : cross-fade between ringmodulated signal without (0) and with (1) oversampling (4-times)
    mix_wavesh : cross-fade between a pure ring-modulation (0) and the ring-modulation with diode+distortion+versampling
    drywet: 0: dry, 1: wet.
    */
    *ar {|in, mod_in=nil, freq=20, mix_diode=0.54, mix_analog=0.34, mod_dist=0.08, diode_mul=2, diode_add= -0.2026|
        var mod_diode, mod, ring_mod, hilbert_mod, in_multi, s_out_multi, s_out, s_out_simple, out;
        // waveshaping of modulation signal
        mod_in = mod_in ?? SinOsc.ar(freq);
        mod_diode = UpSample.ar(mod_in)
                    !> LOSER_WaveShapeDist.ar(_, mod_dist)
                    !> Diode(_, diode_mul, diode_add)
                    !> DownSample.ar( _ );
        mod = SelectX.ar(mix_diode, [mod_in, mod_diode]);
        // ring-modulation: analog-model and theoretic-model via hilbert transform
        ring_mod = UpSample.ar(in) * mod !> DownSample.ar( _ );
        hilbert_mod = RingModHilbert.ar( in, freq );
        out = SelectX.ar(mix_analog, [hilbert_mod, ring_mod]);
        ^out;
    }
}

RingModHilbert {
    // implementation of ring-modulation through hilbert transform. Does not need oversampling, but source must be filtered
    // to avoid frequencies over the nyquist freq and under 0
    *ar { |in, freq, mul_up=1, mul_down=1|
        var in_up = LPF.ar(in, SampleRate.ir * 0.5 - freq);  // TODO: use shelf filters of the BEQSuite
        var in_down = in; // HPF.ar(in, freq);
        var out = FreqShift.ar(in_up, freq)*mul_up + (FreqShift.ar(in_down, freq.neg)*mul_down);
        ^out;
    }
}

RingModOS {
    /*
    Oversampling version of a mathematical ring-modulation.
    */
    *ar { |in, mod_in, drive=1|
        var in_os = UpSample.ar(in);
        var mod_in_os = UpSample.ar(mod_in);
        var out_os = in_os * (mod_in_os * drive);
        var out = DownSample.ar(out_os);
        ^out;
    }
}

Diode {
    // waveshaping simulation of a diode, without oversampling
    *ar { |in, diode_mul=2, diode_add= -0.2026|
        var out = (in.abs * diode_mul) + diode_add;
        ^out
    }
}

DiodeOS {
    // same as Diode, with 4-times oversampling
    *ar { |in, diode_mul, diode_add|
        var in_up = UpSample.ar(in);
        in_up = Diode.ar(in_up, diode_mul, diode_add);
        ^( DownSample.ar(in_up) );
    }
}