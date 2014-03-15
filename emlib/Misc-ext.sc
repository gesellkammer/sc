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
    /*
    *cccall {|cc, func, post, store, chan, src|
        /*
        Example

        MIDIFunc.cccall(81, {|x| dosomethingwith.(x)}, store:~mydict, src:"BCF");
        */
        var func2;
        src = MIDIClient.getSource(src);
        if( store.notNil ) {
            if(store.isKindOf(Function)) {
                storefunc = store;
            }{
                storefunc = {|value, param|
                    store[param] = value;
                };
            };
            func2 = {|x|
                var out = func.(x);
                storefunc.(out);
                out;
            };
        } {
            func2 = func;
        };
        ^MIDIFunc.cc(func2, ccNum:cc, chan:chan, srcID:src);
    };
    */

    *ccbind {|cc, synth, param, spec=nil, post=true, store=nil, postpath="/print"|
		/* create a MIDIFunc.cc that sets the synth's param according to the given spec
        cc    : see the examples
        synth : a synth
		param : the parameter to set: synth.set(param, value)
		spec  : a Spec, an object with a 'asSpec' method, or a function converting a midi value to the value passed to set
                if not given, a default conversion of 0-127 to 0-1 is used
        post  : if true  --> the value sent to the synth will be posted
                a number --> it will be sent via OSC to that port number
                a Dictionary --> (path:"/my/osc/path", label:"a_label_to_use_as_first_arg", port:osc_port)

        store : if a Dictionary, the value is stored in store[param]. If a function, the function is called like func.(value, param, ccvalue, cc)

        Example
        =======

        ~synth = {|freq| SinOsc.ar(freq)}.play;

        MIDIFunc.ccbind(81, ~synth, \freq, {|val| val.linexp(0, 127, 20, 800)});
        MIDIFunc.ccbind((cc:81, src:'bcf', chan:1), ~synth, \freq, {|val| val.linexp(0, 127, 20, 800)});
        MIDIFunc.ccbind([81, 4], ~synth, \freq, {|val| val.linexp(0, 127, 20, 800)}); // cc81. ch4 (remember, in SC we start in CH 0)
        MIDIFunc.ccbind("BCF:81", ~synth, \freq, _/127*800);
        MIDIFunc.ccbind("bcf(8):81", ...)  // <---- src:bcf, channel:8, cc:81

        */
		var func, func2, storefunc=nil, chan, src, netaddr;
        if(MIDIClient.initialized.not) {
            MIDIClient.init;
		    MIDIIn.connectAll;
		};

        case
        { cc.isKindOf(Dictionary) } {
            chan = cc[\chan];
            src  = cc[\src];
            cc   = cc[\cc];
        }{ cc.isKindOf(String) } {
            if( cc.contains(":") ) {
                #src, cc = cc.split($:);
                cc = cc.asInt;
                if( src.contains("(") ) {
                    #src, chan = src[0..src.size-2].split("(");
                    chan = chan.asInt;
                };
            } { // else
                cc = cc.asInt;
            };
        }{ cc.isKindOf(SequenceableCollection)} {
            src = cc[2];
            chan = cc[1];
            cc = cc[0];
        };

        if( src.notNil ) {
            src = case
            { src.isKindOf(MIDIEndPoint) } { src.uid }
            { src.isKindOf(Symbol) } {MIDIClient.getSource(src)}
            { src.isKindOf(String) } {MIDIClient.getSource(src)}
            { src }  // default
            ;
        };
		assert { synth.isKindOf(Node) };
        if( spec.isNil ) {
            spec = _/127;
        };
        if( spec.isKindOf(Function) or: (spec.isKindOf(BinaryOpFunction))) {
		    func = spec;
		} // else
		{
		    spec = spec.asSpec;
		    func = { |x| spec.map(x/127) };
	    };
        case
        { post.isNil } { func2 = func }
        { post.isKindOf( Boolean ) } {  // true|false : post message
            if( post ) {
                func2 = {|x|
                    var value = func.(x);
                    var s = "CC% -> %: %".format(cc, param, value).postln;
                    value;
                };
            }{
                func2 = func;
            }
        }
        { post.isKindOf( SimpleNumber ) } {  // OSC port, end message /print param value
            netaddr = NetAddr("localhost", post);
            func2 = {|x|
                var value = func.(x);
                netaddr.sendMsg(postpath, param, value);
                value;
            }
        }
        { post.isKindOf( NetAddr ) } {  // specify a net addr, param is the osc path
            var oscpath = '/' ++ param;
            func2 = {|x|
                var value = func.(x);
                post.sendMsg(param, value);
                value;
            }
        }
        { post.isKindOf( Dictionary ) } {
            var oscpath = post[\path] ? "/print";
            var port = post[\port] ? 31415;
            var label = post[\label] ? param;
            netaddr = NetAddr("localhost", port);
            func2 = {|x|
                var value = func.(x);
                netaddr.sendMsg(oscpath, label, value);
                value;
            }
        };

        if( store.notNil ) {
            if(store.isKindOf(Function)) {
                storefunc = store;
            }{
                storefunc = {|value, param|
                    store[param] = value;
                };
            };
        };

        /*
        if( osc.notNil ) {
            var oschost, oscport, oscpath, netaddr;
            "----------------> OSC sent defined".postln;
            if( osc.isKindOf(SimpleNumber) ) {
                osc = [osc];
            };
            case
            {oscport.size == 3 } {
                oschost = osc[0];
                oscport = osc[1];
                oscpath = osc[2];
            }
            {oscport.size == 2 } {
                oschost = "127.0.0.1";
                oscport = osc[0];
                oscpath = osc[1];
            }
            { oscport.size == 1 } {
                oscpath = "/";
                oscport = osc[0];
                oschost = "127.0.0.1";
            };
            netaddr = NetAddr(oschost, oscport);
            func = {|x|
                var value = func.(x);
                netaddr.sendMsg(oscpath, param, value);
                value;
            };
        };
        */
        "cc: %, ch: %, src: %  --> %".format(cc, chan, src, param).postln;
        ^MIDIFunc.cc(
            {|value|
                var valuepost;
                valuepost = func2.(value);
                synth.set(param, valuepost);
                if( storefunc.notNil ) {
                    storefunc.(valuepost, param, value, cc);
                };
            },
            cc, chan:chan, srcID:src
        );
    }
}

+ Node {
	ccbind {|cc, param, spec=nil, post=true, store=nil|
		^MIDIFunc.ccbind(cc, this, param, spec, post, store);
	}
}

+ Synth {
    ccbind {|cc, param, func_or_spec=nil, post=true, store=nil, postpath="/print"|
        /*
        see MIDIFunc.ccbind

        Example
        =======

        ~synth = {|freq=440, amp=1| SinOsc.ar(freq) * amp }.play;
        ~synth.ccbind(81, \freq, _.linexp(0, 127, 20, 800)));
        ~synth.ccbind((cc:80, chan:9), \freq, {...})
        */
        ^MIDIFunc.ccbind(cc, this, param, func_or_spec, post, store, postpath);
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
    *getBuffer {|ir, fftsize=4096, action, server='default'|
        /*
        get the ir buffer and prepare it. Returns a Ref to the Buffer.

        ir: the path to the impulse response
        action: this action will be called with the allocated buffer as argument

        (see Example)

        Example
        =======

        (
        ~fftsize = 4096;
        PartConv.getbuffer("/path/to/ir.wav", ~fftsize, action:{|buf| ~irbuf = buf});  // this is performed async.
        )

        (
            a = { SoundIn.ar(0) !> PartConv.ar(_, ~fftsize, ~irbuf.index) }.play
        )

        ~irbuf.free;    // clean up

        getBuffer returns a Ref, so you can do:

        ~buf = PartConv.getBuffer(path, fftsize);

        // later on, maybe some ms later, ~buf.value will have the buffer
        {
            PartConv.ar(SoundIn.ar(0), fftsize, ~buf.value.index);
        }.play

        */
        var irbuffer, bufsize, irspectrum, out;

        if( server == 'default' ) {
            server = Server.default;
        };

        out = Ref();

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
            out.value = irspectrum
        }
        ^out;
    }

}

+ BPeakEQ {
    * chain {|in, freqs, rq=1, db=0|
        /*
        A chain of BPeakEQs

        SoundIn.ar(0) !> BPeakEQ.chain(_, [100, 200, 300], 0.1, 6)   // if you need to specify rq and db for each band, you might as well use the ar method
        */
        var col = in;
        freqs.value.do {|freq|  // do this to avoid expansion
            col = BPeakEQ.ar(col, freq, rq, db);
        };
        ^col;
    }
}

+ AbstractFunction {
	plotBetween { arg from=0.0, to=1.0, n=512, name, bounds, discrete=false,
				numChannels, minval, maxval, parent, labels=true, xwarp='lin', xunit;
        /* the same as plotGraph, but set the x spec and return the plotter */
		var array = Array.interpolation(n, from, to);
		var res = array.collect { |x| this.value(x) };
		var plotter = res.plot(name, bounds, discrete, numChannels, minval, maxval, parent, labels);
        plotter.domainSpecs = [ ControlSpec(from, to, warp:xwarp, units:xunit) ];
        plotter.refresh;
        ^plotter;
	}
}
