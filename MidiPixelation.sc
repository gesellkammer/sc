MidiPixelation {
    
    classvar <midinotes;
    var server, midiout, <>midi_gain, <>dur, <responder, <synth;
    var <running_status, osc_name;
    var <nfft, <trig_rate;
    var <id;
    var <midi_processing_functions;
    classvar <synthdef_name = 'MidiPixelation';
    classvar <pr_maxid;
    classvar <freqs = #[
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
    
    *new {|fftsize=8192, update_rate=1.5, note_dur=0.1, gain=2|
        ^super.new.init(fftsize, update_rate, note_dur, gain);
    }
    *initClass {
        midinotes = freqs.collect {|freq| freq.cpsmidi};
        pr_maxid = 0;
    } 
    prGetId {
        var newid = this.class.pr_maxid;
        this.class.pr_maxid = this.class.pr_maxid + 1;
        if( this.class.pr_maxid > 1000 ) {
            this.class.pr_maxid = 0;
        };
        ^newid;
        
    }
    init {|fftsize=8192, update_rate=1.5, note_dur=0.1, gain=2|
        server = Server.local;
        midi_gain = gain;
        dur = note_dur;
        
        nfft = fftsize;
        trig_rate = update_rate;
        id = this.prGetId;
        osc_name = '/edu/midipix'
        //osc_name = '/midipix';
        if( MIDIClient.initialized.not ) {
            MIDIClient.init;
        };
        midiout = MIDIOut(0);
        server.doWhenBooted({
            this.load_synthdef.();
        });
        running_status = 'STOPPED';
        midi_processing_functions = [];
        responder = OSCresponderNode(nil, osc_name, 
            {|time, responder, message|
                var amp, noteoffs, midinote;           
                if( message[2] == id ) {
                    noteoffs = Array.new(88);
                    fork {
                        88.do {|i| 
                            midinote = midinotes[i];
                            amp = (message[i+4] * midi_gain * 127.0).clip(0, 127);
                            if( amp > 0 ) {
                                midiout.noteOn(0, midinote, amp);
                                noteoffs.add(midinote) 
                            };
                        };
                    };

                    fork {
                        dur.wait;
                        if( noteoffs.size > 0 ) {
                            noteoffs.do {|elem, i|
                                midiout.noteOff(0, elem, 0)
                            };
                        };
                    };
                };
                

            }
        );
    }
    load_synthdef {
        SynthDef(this.class.synthdef_name) {|in_bus=0, trig_rate=1.5|
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
        	var fft_buf = LocalBuf(nfft);
            var trig = Impulse.kr(SampleRate.ir/nfft * trig_rate);
        	var in = In.ar(in_bus); 
            var fft_data = FFT(fft_buf, in);
            var powers = FFTSubbandPower.kr(fft_data, freqs, 0);
            SendReply.kr(trig, osc_name, powers, id);

        }.send(server);
    }
    play {|in_bus=0, target, addAction=\addToTail|
        // NumOutputBuses.ir + 0 is the same as SoundIn.ar(0)
        switch( running_status,
            'PAUSED', {
                this.synth.run(1);
                running_status = 'PLAYING';
            },  
            'STOPPED', {
                responder.add;
                synth = Synth(this.class.synthdef_name, args:['in_bus', in_bus, 'trig_rate', trig_rate], target:target, addAction:addAction);
                running_status = 'PLAYING';
            },
            'PLAYING', {
                'already playing'.postln;
            }
        )
        ^synth
    }
    stop {
        synth.free;
        responder.remove;
        running_status = 'STOPPED';
    }
    pause {
        running_status = 'PAUSED';
        synth.run(0);
        ^synth;
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
    register_midi_processing {|func|
        // func should accept an array ... @todo
        
    }
}