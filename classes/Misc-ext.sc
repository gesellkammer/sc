+ Function {
	benchN { |times=1000|
		^{times.do { this }}.bench;
	}
	assert { |message="", func|
		if(this.value) {
			^true;
		}{
			if( func.notNil ) { func.() };
			Error("Assertion error: % in function \n %".format(message, this.def.sourceCode)).throw;
		};
	}
}

+ SequenceableCollection {
	pairwise {
		^this.slide(2).clump(2);
	}
}

+ Condition {
	moveon {
		this.test = true; this.signal;
	}
}

+ Object {
	postpp {|labelstr|
		// like postln but prepend the line with labelstr
		"%: %".format(labelstr, this).postln;
		^this
	}
}

+ SimpleNumber {
	asSpec { ^[0, this].asSpec }
}

+ MIDIFunc {
	*ccbind {|cc, synth, param, spec=nil, post=true|
		/* create a MIDIFunc.cc that sets the synth's param according to the given spec
		param: the parameter to set: synth.set(param, value)
		spec: a Spec, an object with a 'asSpec' method, or a function converting a midi value to the value passed to set

        Example
        =======

        ~synth = {|freq| SinOsc.ar(freq)}.play;
        MIDIFunc.ccbind(81, ~synth, \freq, {|val| val.linexp(0, 127, 20, 800)});

        */
		var func;
		assert { synth.isKindOf(Node) };
		if(MIDIClient.initialized.not) {
		    MIDIIn.connectAll;
		};
		if(spec.isKindOf(Function)) {
		    func = spec;
		} // else
		{
		    spec = spec.asSpec;
		    func = { |x| spec.map(x/127) };
	    };
		if( post ) {
    		^MIDIFunc.cc(
    			{|value| synth.set(param, func.(value).postpp("CC% -> %".format(cc, param))) },
    			cc
    		)
    	} // else
    	{
	    	^MIDIFunc.cc(
    			{|value| synth.set(param, func.(value))},
    			cc
    		)
    	};
	}
}

+ Node {
	ccbind {|cc, param, spec=nil, post=true|
		^MIDIFunc.ccbind(cc, this, param, spec, post);
	}
}

+ Synth {
    ccmap { |cc, param, range_or_func, post=true|
        if( range_or_func.isNil ) {
		   ^MIDIFunc.cc(ccNum: cc, func: {|val|
		       this.set(param, val);
		       if( post ) { "%: %".format(param, val).postln; }
		       })
	    };
        if( range_or_func.isKindOf(SequenceableCollection) ) {
            ^MIDIFunc.cc(ccNum: cc, func: {|val|
                var val2 = val.linlin(0, 127, range_or_func[0], range_or_func[1]);
                this.set(param, val2);
                if( post ) { "%: %".format(param, val2).postln; }
            });
        }{  // else : A Function
            ^MIDIFunc.cc(ccNum: cc, func: {|val|
                var outval = range_or_func.(val);
                this.set(param, outval);
                if( post ) { "%: %".format(param, outval).postln; }
            });
        }
    }

    ccbind {|cc, params=nil, post=true|
        /*
        Example
        =======

        // synth.ccbind(device, [\freq->[40, 2000], \amp, \amp2->2]
        ~synth = {|freq=440, amp=1| SinOsc.ar(freq) * amp }.play
        ~synth.ccbind(81, freq->{|val| val.linexp(0, 127, 20, 800)});

        or

        TODO

        */
        var spec;
        if( cc.isKindOf( SimpleNumber ).not ) {
            cc = cc.at(0);  // assume that cc is actually a class defining a MIDI device
        };
        if( params.isKindOf(SequenceableCollection).not) {
            params = [params];
        };
        ^params.collect { |param, i|
            case
                { param.isKindOf(Association) } {
                        spec = param.value;
                        param = param.key;
                    }
                { param.isKindOf(SequenceableCollection) } {
                        spec = param[0];
                        param = param[1];
                    }
                { true }    { spec = 1; }  // default
                ;
            /*
            if( param.isKindOf(Association) ) {
                spec = param.value;
                param = param.key;
            } // else
            {
                spec = 1;
            };
            */
            "binding % -> %".format(cc+i, param).postln;
            MIDIFunc.ccbind(cc+i, this, param, spec, post);
        }
    }
}

+ AudioUnitBuilder {
    getPlugins {|func|
        var pipe, line, synthDef, ugens, cmd, paths;
	    synthDef = SynthDef(\autest, func);
	    ugens = synthDef.children.collect({|i| i.class.name}).asSet;
	    paths = List();
	    cmd = "grep  -e _"++ugens.asSequenceableCollection.join("_ -e _") + "_ /Applications/SuperCollider/SuperCollider.app/Contents/Resources/plugins/*.scx";
	    cmd.postln;
	    pipe = Pipe.new(cmd,"r");
        line = pipe.getLine;
        while({line.notNil}, {
	        line = line.findRegexp("[^ ]*.scx")[0][1];
	        line.postln;
	        paths.add(line);
             line = pipe.getLine;
        });
        pipe.close;
        cmd = "grep  -R -e _"++ugens.asSequenceableCollection.join("_ -e _") + "_ \"%/appsupport/SuperCollider/Extensions\"".format("HOME".getenv);
	    cmd.postln;
	    pipe = Pipe.new(cmd,"r");
        line = pipe.getLine;
        while({line.notNil}, {
	        line = line.findRegexp("[^ ]*.scx")[0][1];
	        line.postln;
	        paths.add(line);
            line = pipe.getLine;
        });
        pipe.close;
        ^paths;
	}
}

+ PartConv {
    *getBuffer {|server, ir, fftsize=4096, action|
        /*
        get the ir buffer and prepare it.

        ir: the path to the impulse response
        action: this action will be called with the allocated buffer as argument
        (see Example)

        Example
        =======

        (
        ~fftsize = 4096;
        PartConv.getbuffer(s, "/path/to/ir.wav", ~fftsize, action:{|buf| ~irbuf = buf});  // this is performed async.
        )

        (
            a = { SoundIn.ar(0) !> PartConv.ar(_, ~fftsize, ~irbuf.index) }.play
        )

        ~irbuf.free;    // clean up
        */
        var irbuffer, bufsize, irspectrum;
        forkIfNeeded {
            irbuffer = Buffer.read(server, ir);
            server.sync;
            assert {irbuffer.notNil};
            bufsize = PartConv.calcBufSize(fftsize, irbuffer);
            irspectrum = Buffer.alloc(server, bufsize, 1);
            server.sync;
            assert {irspectrum.notNil};
            irspectrum.preparePartConv(irbuffer, fftsize);
            server.sync;
            irbuffer.free;
            action.(irspectrum);  // call the action with buffer
        }
        ^irspectrum;
    }
}
