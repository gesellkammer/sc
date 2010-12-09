MidiPixelation {
    
    classvar <midinotes;
    var server, midiout, <>midi_gain, <>dur, <responder, <synth;
    var <running_status, osc_name;
    var <nfft, <trig_rate;
    var <in_bus, <out_bus;
    var <id;
    var <post_funcs;
    var <bus_env = -1;
    var <numkeys = 88;
    var <>group;
    var <>bus_powers;
    var <>analyzer_hop = 0.25;
    var <>synth_analyzer, <>synth_sender, <>synth_env, <>synth_env_analyze;
    classvar <>pr_maxid = 0;
    classvar <synthdef_name = 'MidiPixelation';
    classvar <synthdef_analysis = 'MidiPix_ANAL';
    classvar <synthdef_send = 'MidiPix_SEND';
    classvar <synthdef_env  = 'MidiPix_ENV';
    classvar <>synthdefs_loaded = false;
    classvar <default_server;
    classvar <piano_freqs = #[
          27.625     ,    29.26766798,    31.00801408,    32.85184655,
          34.805319  ,    36.87495097,    39.06764966,    41.390733  ,
          43.85195406,    46.45952694,    49.22215418,    52.14905578,
          55.25      ,    58.53533596,    62.01602817,    65.7036931 ,
          69.61063801,    73.74990194,    78.13529932,    82.781466  ,
          87.70390812,    92.91905389,    98.44430835,   104.29811155,
         110.5       ,   117.07067193,   124.03205634,   131.40738621,
         139.22127601,   147.49980389,   156.27059864,   165.56293199,
         175.40781624,   185.83810777,   196.88861671,   208.5962231 ,
         221.0       ,   234.14134385,   248.06411268,   262.81477242,
         278.44255203,   294.99960777,   312.54119728,   331.12586399,
         350.81563248,   371.67621554,   393.77723342,   417.19244621,
         442.0       ,   468.28268771,   496.12822535,   525.62954483,
         556.88510405,   589.99921554,   625.08239457,   662.25172798,
         701.63126497,   743.35243108,   787.55446684,   834.38489241,
         884.0       ,   936.56537541,   992.25645071,  1051.25908966,
        1113.77020811,  1179.99843109,  1250.16478914,  1324.50345596,
        1403.26252994,  1486.70486217,  1575.10893367,  1668.76978482,
        1768.0       ,  1873.13075083,  1984.51290141,  2102.51817932,
        2227.54041621,  2359.99686217,  2500.32957828,  2649.00691192,
        2806.52505988,  2973.40972434,  3150.21786734,  3337.53956964,
        3536.0       ,  3746.26150165,  3969.02580282,  4205.03635865, 4400];
    classvar <piano_amps = #[
        0.995633, 0.994918, 0.994105, 0.993186, 0.992148, 0.990981, 
        0.989672, 0.988208, 0.986576, 0.984762, 0.982749, 0.980523, 
        0.978067, 0.975365, 0.9724, 0.969155, 0.965613, 0.961758, 
        0.957573, 0.953043, 0.948152, 0.942887, 0.937234, 0.93118,
        0.924714, 0.917825, 0.910504, 0.902742, 0.89453, 0.885862, 
        0.87673, 0.867128, 0.85705, 0.846493, 0.835452, 0.823925, 
        0.81191, 0.799409, 0.786425, 0.772963, 0.759032, 0.744647, 
        0.729824, 0.714586, 0.698961, 0.682984, 0.666694, 0.650138, 
        0.633369, 0.616447, 0.599436, 0.582405, 0.56543, 0.548586, 
        0.531951, 0.515604, 0.499621, 0.484076, 0.469038, 0.45457, 
        0.440727, 0.427558, 0.415104, 0.403396, 0.392457, 0.382304, 
        0.372943, 0.364378, 0.356604, 0.349615, 0.343399, 0.337944, 
        0.333235, 0.32926, 0.326005, 0.32346, 0.321615, 0.320465, 
        0.320009, 0.320247, 0.321186, 0.322835, 0.325209, 0.328326, 
        0.332207, 0.336878, 0.342366, 0.348703
    ];
    classvar <piano_amps2;

    *new {|fftsize=8192, update_rate=1, note_dur=0.1, gain=2|
        ^super.new.init(fftsize, update_rate, note_dur, gain);
    }
    *initClass {
        midinotes = piano_freqs.collect {|freq| freq.cpsmidi};
        piano_amps2 = piano_amps * piano_amps;
        default_server = Server.local;
        StartUp.add {
            if( default_server.serverRunning ) {
                this.load_synthdefs.();
                synthdefs_loaded = true;
            };
        }
    } 
    prGetId {
        var newid = this.class.pr_maxid;
        this.class.pr_maxid = this.class.pr_maxid + 1;
        if( this.class.pr_maxid > 1000 ) {
            this.class.pr_maxid = 0;
        };
        ^newid;
        
    }
    init {|fftsize=8192, update_rate=1, note_dur=0.1, gain=2|
        server     = default_server;
        midi_gain  = gain;
        dur        = note_dur;
        nfft       = fftsize;
        trig_rate  = update_rate;
        id         = this.prGetId;
        osc_name   = '/edu/midipix';
        group      = Group.tail(server);
        bus_powers = Bus.control(server, numkeys);

        if( MIDIClient.initialized.not ) {
            MIDIClient.init;
        };
        midiout = MIDIOut(0);
        if( synthdefs_loaded.not ) {
            server.doWhenBooted({
                this.load_synthdefs.();
            });
        };
        running_status = 'STOPPED';
        post_funcs     = List();
        CmdPeriod.add({this.free});

        // -----------------------------------------------------------
        responder = OSCresponderNode(nil, osc_name, 
            {|time, responder, message|
                var amp, midinote, amps, noteoffs, new_midinotes;           
                if( message[2] == id ) {
                    noteoffs = Array(88);
                    amps = message[4..91];
                    new_midinotes = midinotes.copy;
                    post_funcs.do {|func|
                        #new_midinotes, amps = func.(new_midinotes, amps);
                    };
                    amps = (amps * (127.0 * midi_gain)).clip(0, 127); // this could also be a post_func 
                    (amps.size - 1).do {|i|
                        amp = amps[i].floor;
                        if( amp > 0 ) {
                            midinote = new_midinotes[i];
                            midiout.noteOn(0, midinote, amp);
                            noteoffs.add(midinote);
                        };
                    };
                    if( noteoffs.size > 0 ) {
                        fork {
                            dur.wait;
                            noteoffs.do {|note|
                                    midiout.noteOff(0, note, 0)
                            };
                        };    
                    };     
                };
            }
        );
        CmdPeriod.doOnce { this.stop };
    } 
    // ----------------------------------------------------------------
    load_synthdefs {
        SynthDef(synthdef_analysis) {|in_bus_audio=0, out_bus_powers=0, post_gain=1, i_nfft=8192, hop=0.25, freq0=30, freq1=4800|
            var freqs = #[25,
                          27.625     ,    29.26766798,    31.00801408,    32.85184655,
                          34.805319  ,    36.87495097,    39.06764966,    41.390733  ,
                          43.85195406,    46.45952694,    49.22215418,    52.14905578,
                          55.25      ,    58.53533596,    62.01602817,    65.7036931 ,
                          69.61063801,    73.74990194,    78.13529932,    82.781466  ,
                          87.70390812,    92.91905389,    98.44430835,   104.29811155,
                         110.5       ,   117.07067193,   124.03205634,   131.40738621,
                         139.22127601,   147.49980389,   156.27059864,   165.56293199,
                         175.40781624,   185.83810777,   196.88861671,   208.5962231 ,
                         221.0       ,   234.14134385,   248.06411268,   262.81477242,
                         278.44255203,   294.99960777,   312.54119728,   331.12586399,
                         350.81563248,   371.67621554,   393.77723342,   417.19244621,
                         442.0       ,   468.28268771,   496.12822535,   525.62954483,
                         556.88510405,   589.99921554,   625.08239457,   662.25172798,
                         701.63126497,   743.35243108,   787.55446684,   834.38489241,
                         884.0       ,   936.56537541,   992.25645071,  1051.25908966,
                        1113.77020811,  1179.99843109,  1250.16478914,  1324.50345596,
                        1403.26252994,  1486.70486217,  1575.10893367,  1668.76978482,
                        1768.0       ,  1873.13075083,  1984.51290141,  2102.51817932,
                        2227.54041621,  2359.99686217,  2500.32957828,  2649.00691192,
                        2806.52505988,  2973.40972434,  3150.21786734,  3337.53956964,
                        3536.0       ,  3746.26150165,  3969.02580282,  4205.03635865, 4400];
            var sr = SampleRate.ir;
            var nyfreq = sr * 0.5;
        	var fft_buf = LocalBuf(i_nfft);
            var in = In.ar(in_bus_audio); 
            var thresh0 = freq0 / nyfreq;
            var thresh1 = -1 * (1 - (freq1 / nyfreq));
            //var trigger = Impulse.kr(sr/i_nfft * trig_rate);
            var fft_data = FFT(fft_buf, in, hop, wintype:1) // wintype = hann
                           | PV_BrickWall(_, thresh0)
                           | PV_BrickWall(_, thresh1)
                           ;
            var powers = FFTSubbandPower.kr(fft_data, freqs, 0);
            //powers = powers * post_gain;
            powers = powers * piano_amps;
            ReplaceOut.kr(out_bus_powers, powers);
            //SendReply.kr(Impulse.kr(1), osc_name, powers, id);
        }.send(server);

        SynthDef(synthdef_send) {|in_bus_powers, trig_rate=1, block_low=0, block_high=1, clip_min=0, clip_max=1, map_min=0, map_max=1|
            var powers = In.kr(in_bus_powers, numkeys);
            var rate = SampleRate.ir/nfft * analyzer_hop.reciprocal * trig_rate;
            var trigger = Impulse.kr(rate);
            powers = powers * (powers > block_low);
            powers = powers * (powers < block_high);
            powers = powers.clip(clip_min, clip_max);
            powers = powers.linlin(clip_min, clip_max, map_min, map_max);
            SendReply.kr(trigger, osc_name, powers, id);
        }.send(server);
    
        SynthDef(synthdef_env) {|bus_powers, bus_env, env_floor=(-50.dbamp)|
            var powers = In.kr(bus_powers, numkeys);
            var env    = In.kr(bus_env, numkeys);
            var out    = powers * (env + env_floor);
            ReplaceOut.kr(bus_powers, out);
        }.send(server);
    }
    
    play {|in_bus_audio|
        switch( running_status,
            'STOPPED', {
                running_status = 'PLAYING';
                responder.add;
                forkIfNeeded {
                    synth_analyzer = Synth.tail(group, this.class.synthdef_analysis, 
                                                args: ['in_bus_audio', in_bus_audio, 'out_bus_powers', bus_powers.index, 'hop', analyzer_hop]);
                    //server.sync;                    
                    synth_sender = Synth.tail(group, this.class.synthdef_send,
                                                args: ['in_bus_powers', bus_powers.index, 'trig_rate', trig_rate]);
                    
                    server.sync;
                    group.run(1);
                };
            },
            'PAUSED', {
                running_status = 'PLAYING';
                group.run(1);
            },
            'PLAYING', {
                'already playing!'.postln;
            }
        );
        
    }
    stop {
        group.run(0);
        group.free;
        responder.remove;
        running_status = 'STOPPED';
    }
    pause {
        running_status = 'PAUSED';
        group.run(0);
    }
    free {
        this.stop;
    }
    test_midi {
        midiout.noteOn(0, 60, 90);
        fork {
            2.wait;
            midiout.noteOff(0, 60, 0);
        }
    }
    register_post_processing {|func|
        /*
        func should have the prototype func {|midinotes, amps| ... } where:
        amps      = the amplitude (between 0 and 1) of each note
        midinotes = the midi note-number corresponding to each amp
        
        it should return an array [amps, midinotes] with the desired midifications
        */ 
        post_funcs.add(func)    
    }
    analyze_env {|in_audio, floor=(-50.dbamp)|
        //this.pr_spectral_envelope_run(true);
        assert { synth_env.isNil and: synth_analyzer.notNil };
        bus_env = Bus.control(server, numkeys);

        synth_env_analyze = Synth.head(group, this.class.synthdef_analysis, 
                                       args: ['in_bus_audio', in_audio, 'out_bus_powers', bus_env]);
        synth_env = Synth.after(synth_analyzer, this.class.synthdef_env, 
                                       args: ['bus_powers', bus_powers, 'bus_env', bus_env, 'env_floor', floor]); 
    }
    analyze_env_stop {
        if( synth_env_analyze.notNil ) {
            synth_env_analyze.free;
            synth_env.free;
            bus_env.free;
        };
    }
}
