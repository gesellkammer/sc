BEPartialPlayerX {
    var <rootpath;
    var <active_voices;
    var <basename;
    var duration;
    var <curr_synth;
    var <synthdef_name;
    var <stretch;
    var <pitch;
    var <amp;
    var <prRoutine;
    var chan0;
    var <timenow;
    var <osc_responder;
    var server;
    *new {|path, chan0=0|
        ^super.new.init(path, chan0)
    }
    init {|path, chan0|
        var get_sdifs =  {|basepath|
            var last_dir, glob, filenames;
           	last_dir = basepath.split($/).last;
        	glob = basepath ++ $/ ++ last_dir ++ "*.sdif";
        	filenames = glob.pathMatch;
        	assert{ filenames.isEmpty.not };
        	filenames;
        };
        var voice_index_and_name = {|sdiffile|
            var base_index_name = sdiffile.split($/).last.removeExtension.split($-);
            basename = base_index_name[0];
            if( base_index_name.size > 2 ) {
                [base_index_name[1], base_index_name[2]];
            }{ // else
                [base_index_name[1], ""];
            };
        };
        var filenames = get_sdifs.(path);
        var durations;
        server = Server.local.boot;
        pitch = 1;
        amp = 1;
        stretch = 1;
        rootpath = path;
        active_voices = filenames.collect{ |filename|
            var index, name, voice;
            #index, name = voice_index_and_name.(filename);
            voice = ();
            voice.partials = BEPartials(RBE_SDIF_File(filename));
            voice.sdiffile = filename;
            voice.bus = chan0 + index.asInteger;
            voice.index = index.asInteger;
            voice.name = name;
            voice;
            };
        durations = active_voices.collect{ |voice|
            voice.partials.dur;            
        };
        duration = durations.reduce(\max);
        this.sendSynthDef.();     
    }

    sendSynthDef{ 
        synthdef_name = "BEP"; //  ++ active_voices.size.asString ++ "-" ++ basename;
        
        SynthDef(synthdef_name) {|stretch=1, pitch=1, amp=1, gate=1|
            var attack  = 0, 
                release = 0.1,
                out = 0,
                epsilon = 0.1,
                max_duration = (duration + epsilon) * stretch;
            active_voices.do { |voice, index|
        	    var envs = voice.partials.ar(stretch, pitch);
                Line.kr(0, max_duration, max_duration) | SendTrig.kr(Impulse.kr(10), 0, _);
        	    BEOsc.ar(*envs) 
        	    * 
                (   Env([0, 1, 1, 0], [attack, max_duration, release], \sine)
                    |
                    EnvGen.ar(_, gate, amp, doneAction: DoneAction.free)
                    *
                    gate
                )
                |
        	    Mix(_) * amp
        	    |
        	    Out.ar(voice.bus, _);
        	};
        }.load(server);
        
    }
    
    play { |loop=true, notification_function, stop_function, amp, target, addAction=\addToHead|
        if( loop ) {
            prRoutine = Routine {
                loop {
                    this.synth(amp, notification_function, target, addAction);
                    this.duration.wait;
                }
            }.play;
        }{ /* else */  
            this.synth(amp, notification_function, target, addAction);
            if( stop_function.notNil ) {
                stop_function.()
            };
        };
    }
    
    synth { |amp, notification_function, target, addAction=\addToHead|
            curr_synth = Synth(this.synthdef_name, [\stretch, stretch, \pitch, pitch, \amp, amp], target:target, addAction:addAction);
            osc_responder = OSCresponderNode(server.addr,'/tr',{ arg time,responder,msg;
                timenow = msg.last;
                if(notification_function.notNil) {
                    notification_function.(timenow);
                }
            }).add;
            ^curr_synth;
    }
    stop {
        curr_synth.set(\gate, 0);
        fork {
            0.5.wait;
            curr_synth.free;
            curr_synth = nil;
        };
        prRoutine.stop;
        osc_responder.remove;
    }
    duration {
        ^(duration * stretch);
    }
    stretch_ {|val|
        stretch = val;
        if( curr_synth.notNil ) {
            curr_synth.set(\stretch, val);
        };
        
    }
    pitch_ { |val|
        pitch = val;
        if( curr_synth.notNil ) {
            curr_synth.set(\pitch, val);
        };
        
    }
    amp_ { |val|
        amp = val;            
        if( curr_synth.notNil ) {
            curr_synth.set(\amp, val);
        };
        
    }
    
}



BEPartialPlayerXGui {
    var <> mixer;
    var <window, <rootpath;
    var <sdif_folders;
    var sdif_folders_popup, path_textfield, duration_text, stretch_nbox, stretch_slider;
    var pitch_slider, pitch_nbox, time_text, time_slider, active_voices_toggle, active_voices_names;
    var play_button;
    var <partial_player;
    var <ready_to_play;
    var <playing;
    var stretch, pitch, amp, loop;
    var multichannel_sndfile, audio_buffer, play_as_audio;
    var osc_responder;
    var curr_synth;
    var server;
    var volume;
    var volume_ratio = 100;
    var player_type;
    *new {|path|
        ^super.new.init(path);
    }
    prUpdatePath {|path|
        sdif_folders = (path ++ "/*").pathMatch.select(_.isFolder).collect{ |path|
            path.trim($/).split($/).last;            
        };
        rootpath = path;
        defer { 
            sdif_folders_popup.items = sdif_folders;
            path_textfield.string = path
        };
    }
    prLoadSelection {|path|
        var server = Server.local;
        var fullpath = rootpath ++ $/ ++ path;
        var sndfile = (fullpath ++ "/out?.aiff").pathMatch;
        var last;
        if( sndfile.isEmpty ) {
            player_type = \partials;
        }{
            var last = sndfile[0].split($/).last;
            if( last == "out1.aiff" ) {
                player_type = \mono;
            } /* else */ {
                assert{ last == "out8.aiff" };
                player_type = \multi;
            };
            sndfile = sndfile[0];
        };
        switch( player_type,
            \multi, {
                defer {
                    play_as_audio.value = true;
                    active_voices_toggle.do {|v| v.value = true };
                    
                };
                Routine.run {
                    var cond = Condition.new;
                    server.bootSync;
                    audio_buffer.free;
                    audio_buffer = Buffer.read(server, sndfile, action:{
                        cond.test = true;
                        cond.signal;
                        }
                    );
                    server.sync;
                    cond.wait;
                    audio_buffer.updateInfo;
                    defer {
                        duration_text.string = (audio_buffer.numFrames / audio_buffer.sampleRate).asTimeString;
                    };
                    ready_to_play = true;
                    "------ ready to play -----".postln;
                };
            },
            \mono, {
                defer {
                    play_as_audio.value = true;
                    active_voices_toggle[0..1].do {|v| v.value = true };
                    active_voices_toggle[2..].do  {|v| v.value = false};
                    
                };
                Routine.run {
                    var cond = Condition.new;
                    server.bootSync;
                    audio_buffer.free;
                    audio_buffer = Buffer.read(server, sndfile, action:{
                        cond.test = true;
                        cond.signal;
                        }
                    );
                    server.sync;
                    cond.wait;
                    audio_buffer.updateInfo;
                    defer {
                        duration_text.string = (audio_buffer.numFrames / audio_buffer.sampleRate).asTimeString;
                    };
                    
                    ready_to_play = true;
                    "------ ready to play -----".postln;
                    
                };
            },
            \partials, {  // loris partials
                Routine.run {
                    server.bootSync;
                    partial_player = BEPartialPlayerX(fullpath);
                    server.sync;
                    defer {
                        play_as_audio.value = false;
                        duration_text.string = partial_player.duration.asTimeString;
                        active_voices_toggle.do { |toggle|
                            toggle.value = false;            
                        };
                        active_voices_names.do { |name|
                            name.string = "";            
                        };
                        partial_player.active_voices.do {|voice|
                            active_voices_toggle[voice.index].value = true;
                            active_voices_names[voice.index].string = voice.bus.asString ++ " - " ++ voice.name;
                        };
                    };
                    ready_to_play = true;
                    "------ ready to play -----".postln;
                };   
            }
        );            
    }
    prSliderToStretch { |x|
        ^(2 ** (x * 6 - 3));
    }
    prStretchToSlider { |x|
        ^((log(x) + (3 * log(2)) / (6 * log(2))));
    }
    prSliderToPitch { |x|
        ^this.prSliderToStretch(x);
    }
    prPitchToSlider{|x|
        ^this.prStretchToSlider(x);
    }
    stretch_{ |val|
        stretch = val;
        if( partial_player.notNil ) {
	       partial_player.stretch = val;
	    };
    } 
    pitch_{ |val|
        pitch = val;
        if( partial_player.notNil ) {
	       partial_player.pitch = val;
	    };
    }
    play {
        playing = true;
        switch( player_type,
            \multi, {
                audio_buffer.updateInfo;
                osc_responder = OSCresponderNode(server.addr,'/tr',{ arg time,responder,msg;
                    var frame = msg.last;
                    var pos = (frame / audio_buffer.numFrames);
                    if( pos == 1 and: loop.not ) {
                       this.stop;
                    } {
                        defer {
                            time_slider.value = pos;
                        }
                    }
                }).add;
                curr_synth = { 
                    var channels = PlayBufSendIndex.ar(8, audio_buffer.bufnum, BufRateScale.kr(audio_buffer), loop:loop.binaryValue);
                    var mix = Mix(channels);
                    Out.ar(8, mix);
                    Out.ar(0, channels * \amp.kr);
                }.play(server, [\amp, volume.value * volume_ratio]);
            },
            \mono, {
                 audio_buffer.updateInfo;
                 osc_responder = OSCresponderNode(server.addr,'/tr',{ arg time,responder,msg;
                    var frame = msg.last;
                    var pos = (frame / audio_buffer.numFrames);
                    if( pos == 1 and: loop.not ) {
                       this.stop;
                    } {
                        defer {
                            time_slider.value = pos;
                        }
                    }
                }).add;
                curr_synth = { 
                    var out = PlayBufSendIndex.ar(1, audio_buffer.bufnum, BufRateScale.kr(audio_buffer), loop:loop.binaryValue) * \amp.kr;
                    Out.ar(8, out);
                    Out.ar(0, out.dup);
                }.play(server, [\amp, volume.value * volume_ratio]);
            },
            \partials, {
                partial_player.play(loop, 
                    notification_function: {|timenow|
                        defer { 
                            time_text.string = timenow.asTimeString;
                            time_slider.value = timenow / partial_player.duration;
                        };
                    },
                    stop_function: {
                        defer { 
                            play_button.value = 0;
                            playing = false;
                        };
                    },
                    amp: volume.value * volume_ratio
                );
            }
        );
    }
    stop {
        if( play_as_audio.value ) {
            curr_synth.free;
            playing = false;
            osc_responder.remove;
            defer {
                play_button.value = 0
            };
        }{
            playing = false;
            partial_player.stop;
        };
    }
    
    init { |path|
        ready_to_play = false;
        playing = false;
        stretch = 1;
        pitch = 1;
        amp = 1;
        loop = true;
        server = Server.local;
        Routine({
            window = SCWindow.new("",Rect(318, 357, 714, 399)).front;
            path = path ? "~/".standardizePath;
            path_textfield = SCTextField.new(window,Rect(23, 38, 300, 21))
                .string_(rootpath)
            	.action_{|v| };
            sdif_folders_popup = SCPopUpMenu.new(window,Rect(441, 38, 255, 21))
                .action_{|v| 
                    this.prLoadSelection(v.items[v.value]);
                };
            this.prUpdatePath(path);
            RoundButton.new(window,Rect(331, 39, 100, 20))
            	.states_([ ["Browse", Color.white, Color(0.23, 0.37, 0.8)]])
            	.extrude_( false )
            	.action_{|v|
            	    CocoaDialog.selectDirectory(rootpath) | this.prUpdatePath(_);
            	};
            play_button = RoundButton.new(window,Rect(26, 72, 162, 154))
            	.states_([ [\play, Color.black, Color.green], [ \stop, Color.black, Color.red ] ])
            	.extrude_( false )
            	.canFocus_(false)
            	.action_{|v|
            	    if( v.value == 1 ) {
            	        if( ready_to_play ) {
            	           this.play;
            	        };
            	    }{ /* else */  
            	        if( playing ) {
            	           this.stop;
            	        };
            	    };
            	};
            time_slider = SCSlider.new(window,Rect(198, 209, 494, 18))
            	.canFocus_(false)
                .action_{|v| };
            time_text = SCStaticText.new(window,Rect(407, 231, 100, 20))
            	.string_("|")
            	.action_{|v| };
            duration_text = SCStaticText.new(window,Rect(594, 231, 100, 20))
            	.string_("00:00:00")
            	.action_{|v| };
            SCStaticText.new(window,Rect(594, 286, 100, 20))
            	.string_("stretch")
            	.action_{|v| };
            stretch_nbox = ScrollingNBox.new(window,Rect(495, 286, 59, 21))
                .value_(stretch)
            	.action_{|v| 
            	    var val = this.prStretchToSlider(v.value);
            	    this.stretch = v.value;
            	    defer{ stretch_slider.value = val };
            	};
            stretch_slider = SmoothSlider.new(window,Rect(22, 286, 461, 17)) // stretch
            	.canFocus_(false)
            	.value_(this.prStretchToSlider(stretch))
            	.action_{|v| 
            	    var val = this.prSliderToStretch(v.value);
            	    this.stretch = val;
            	    defer{ stretch_nbox.value = val };
            	};
            SCStaticText.new(window,Rect(594, 319, 100, 20))
            	.string_("pitch")
            	.action_{|v| };
            pitch_slider = SmoothSlider.new(window,Rect(22, 319, 461, 17)) // pitch
            	.canFocus_(false)
            	.value_(this.prPitchToSlider(pitch))
                .action_{|v| 
                    var val = this.prSliderToPitch(v.value);
                    this.pitch_(val);
                    defer{ pitch_nbox.value = val };
                };
        
            pitch_nbox = ScrollingNBox.new(window,Rect(495, 319, 59, 21))
                .value_(pitch)
            	.action_{|v| 
            	    var val = this.prPitchToSlider(v.value);
            	    this.pitch = v.value;
            	    defer{ pitch_slider.value = val };
            	};
            volume = SCKnob.new(window,Rect(549, 68, 141, 123))
                .value_(0.1)
            	.action_{|v|
            	    if( play_as_audio.value ) {
            	       if( playing ) {
            	           curr_synth.set(\amp, v.value * volume_ratio)
            	       };
            	    }{ /* else */
            	        if( playing ) {
            	           partial_player.curr_synth.set(\amp, v.value * volume_ratio)
            	        };
            	    };
            	};
            ToggleView.new(window,Rect(206, 102, 23, 20))
            	.colorOn_(Color.red(alpha:0.7))
            	.value_( loop )
            	.canFocus_(false)
            	.action_{|v| 
            	    loop = v.value;
            	    if( playing and: play_as_audio.value ) {
            	       curr_synth.set(\loop, loop.binaryValue);
            	    };
            	};
            SCStaticText.new(window,Rect(237, 101, 100, 20))
            	.string_("loop")
            	.action_{|v| };
            play_as_audio = ToggleView.new(window,Rect(206, 154, 23, 20))
                .colorOn_(Color.green(alpha:0.8))
        	    .value_( false )
        	    .canFocus_(false)
        	    .action_{|v| };
            SCStaticText.new(window,Rect(236, 154, 100, 20))
            	.string_("Play as audio")
            	.action_{|v| };

            
            
            active_voices_toggle = 8.collect{ |i|
                ToggleView.new(window, Rect(348, 84 + (i*12), 12, 12))
                    .value_(false)
                    .canFocus_(false)
                    .colorOn_(Color.red(alpha:0.7));
            };
            active_voices_names = 8.collect{ |i|
                SCStaticText.new(window, Rect(372, 84 + (i*12), 84 + (i+1)*12, 12))
                    .string_(i.asString)
                    .action_{|v| }
                    .font = Font("Monaco", 10);
            };
            server.volume.gui;
            //RedMatrixMixerGUI(RedMatrixMixer(), 0@700);
            CmdPeriod.doOnce({if(window.isClosed.not, {window.close})});
            
        }).play(AppClock);
    }
    close {
        window.close;
    }
}
