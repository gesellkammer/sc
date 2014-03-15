PD_hip {
    // One-pole high pass filter. (signal - OnePole.ar(signal, exp(-2pi * (freq * SampleDur.ir))))
    *ar {|in, freq|
        var out = in - OnePole.ar(in, exp(-2pi * (freq * SampleDur.ir)));
        ^out;
    }
}

PD_lop {
    // One-pole low pass filter
    *ar {|in, freq|
        ^OnePole.ar(in, exp(-2pi * (freq * SampleDur.ir)))
    }
}

PD_phasor {
    // Sawtooth oscillator. Ramps from 0 to 1; can be considered a Sawtooth waveform between 0 and 1.
    *ar { |freq|
        ^LFSaw.ar(freq, 1, 0.5, 0.5);
    }
}


