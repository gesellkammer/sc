ReEquestri2 {
	*piano_pixelator {
        ^MidiPixelation();
	}
	*midi_scale_filter_static {|scale|
        var freqs = FreqQuantization.scale(scale);
        var env   = FreqQuantization.freqs2env(freqs, bw_in_semitones:1, downslope_in_semitones:1, curve:2);
        var amps  = MidiPixelation.midinotes.collect {|m| env.at(m)};
        var func  = {|midinotes, midiamps|
            midiamps = midiamps * amps;
            [midinotes, midiamps];
        };
	    ^func;
	}
	*midi_env_filter_dyn {|env|
	    ^MidiPixelationEnvFilter(env);
	}
	*midi_scale_attractor {|scale, distance_func|
	    // distance func should return a gain to apply to the nearest attractor
	    // in function of the distance. 
        var midis = FreqQuantization.scale(scale).collect( _.tomidi ).sort;
        var nearest, note, amp, func, n;
        distance_func = distance_func ? {|dist| 1};
        func = {|midinotes, midiamps|
            n = min(midiamps.size, midinotes.size);
            n.do {|i|
                note    = midinotes[i];
                amp     = midiamps[i];
                nearest = midis.nearest(note);
                amp = amp * distance_func.value( (nearest - note).abs );
                midinotes[i] = nearest;
                midiamps[i]  = amp;
            };
            [midinotes, midiamps];
        };
        ^func;
	}
	*midi_attractor {|attractor_notes, distance_func|
	    var nearest, note, amp, func, n;
        distance_func = distance_func ? {|dist| 1};
        func = {|notes, amps|
            n = min(notes.size, amps.size);
            n.do {|i|
                note    = notes[i];
                amp     = amps[i];
                nearest = attractor_notes.nearest(note);
                amp = amp * distance_func.value( (nearest - note).abs );
                notes[i] = nearest;
                amps[i]  = amp;
            };
            [notes, amps];
        };
        ^func;
	}
	*enhancer {
	    ^Proto({
            ~memsize = 8;
            ~mem = Array.fill(~memsize, 0);
            ~memsize.do {|i|
                ~mem[i] = [(21..(21+87)), Array.fill(88, 0)];
            };
            ~counter = 0;
            ~thresh = 0.1;
            ~next = {|notes, amps|
                var now = ~counter;
                var prev = (now - 1) % ~memsize;
                var diff = amps - ~mem[prev][1];
                ~mem[now] = [notes, amps];
                amps = amps.collect{|amp, i|
                    if( diff[i] < 0.001 ) {
                        //amp *  ((1 - diff[i]) ** 2);
                        amp = 0;

                    } {
                        amp = amp * (1 + diff[i]);
                    }
                };
                ~counter = (~counter + 1) % ~memsize;
                [notes, amps];
            }
        });
	}
	synthdefs {|server|
	    server = s ? Server.default;
	    SynthDef('reeq-frozen') { |out=0, in=0, smooth_ratio=0.5, freeze=0, i_nfft=8192, i_hop=0.25|
	        var audioin = In.ar(in);
	        var chain = FFT(LocalBuf(i_nfft), audioin, i_hop, 1);
	        var frozen = chain
	                     | PV_Freeze( _, doit ) 
                         | PV_MagSmooth( _, smooth_ratio )
                         | IFFT( _ )
                         | (_ ! 2 * 0.5)   // TODO: STEREO o MONO?
                         ;
             Select.ar(freeze, [audioin, frozen]) | Out.ar(out, _);
        }.send(server);
    }
}

MidiPixelationEnvFilter {
    var <env, <>amps;
    *new {|env|
        ^super.new.init(env);
    }
    init {|env|
        this.env_(env);
        // amps = MidiPixelation.midinotes.collect { |m| newenv.at(m) };
    }
    env_ {|newenv|
        env = newenv;
        amps = MidiPixelation.midinotes.collect { |m| newenv.at(m) };
    }
    value {|midinotes, amps|
        amps = amps * this.amps;
        ^[midinotes, amps];
    }
}

