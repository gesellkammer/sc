Elib {
    *moses { |func, seq|
        var trues = List();
        var falses = List();
        seq.do {|x|
            if( func.(x) ) {
                trues.add(x);
            } /*else*/ {
                falses.add(x);
            }
        };
        ^[trues.array, falses.array]
    }
    *split_string { |s|
        ^s.asString.split($ );
    }
    *seq_order_lower { |a, b|
        /*
        returns which of both sequences should be ordered first
        */
        var n;
        var max_n = min(a.size, b.size);
        max_n.do {|n|
            if( a[n] < b[n] ) {
                ^true;
            }{
                if( a[n] > b[n] ) {
                    ^false;
                };
            }
        };
        ^true
    }
    *seq_order_greater { |a, b|
        /*
        returns which of both sequences should be ordered first
        */
        var n;
        var max_n = min(a.size, b.size);
        max_n.do {|n|
            if( a[n] > b[n] ) {
                ^true;
            }{
                if( a[n] < b[n] ) {
                    ^false;
                };
            }
        };
        ^true
    }
    *startup_open {
   	    var extensions= #[".scd", ".rtf"];
   		extensions.do{ |x|
   		    var path= PathName(Platform.userExtensionDir).pathOnly++"startup"++x;
   			if(File.exists(path)) {
   			    Document.open(path);
   			};
   		};
   	}


}

MovingAverage {
    var <wsize, <>avg, partialratio, oneratio;
    *new {|winsize=5, startvalue=0|
        ^super.new.init(winsize, startvalue)
    }
    init {|winsize=5, startvalue=0|
        avg = startvalue;
        wsize = winsize;
        partialratio = (winsize - 1) / winsize;
        oneratio = 1 / winsize;
    }
    value  {|x| ^this.update(x); }
    update {|x|
        avg = (avg * partialratio) + (x * oneratio);
        ^avg;
    }
}


+ Env {
    *fromxy { |xs, ys, curve='lin'|
        var deltas = (xs.size - 1).collect {|i|
            xs[i+1] - xs[i]
        };
        ^Env.new(ys, deltas, curve);
    }
    *frommatrix { |matrix, curve='lin'|
        var xs, ys;
        # xs, ys = matrix.clump(2).flop;
        ^Env.fromxy(xs, ys, curve);
    }
    *frompairs { |... pairs|
        var xs, ys;
        # xs, ys = pairs.flop;
        ^Env.fromxy(xs, ys);
    }
    *bpf{ |...args|
	    // the first arg is the curve, the rest is a seq
	    var xs, ys;
	    # xs, ys = args[1..].clump(2).flop;
	    ^Env.fromxy(xs, ys, curve:args[0]);
    }
    *bpfexpon { |...seq|
	    var xs, ys;
	    # xs, ys = seq.clump(2).flop;
	    ^Env.fromxy(xs, ys, curve:\exp);
    }
    *bpfstep { |...seq|
	    var xs, ys;
	    # xs, ys = seq.clump(2).flop;
	    ^Env.fromxy(xs, ys, curve:\step);
    }
    *bpfwelch { |...seq|
	    var xs, ys;
	    # xs, ys = seq.clump(2).flop;
	    ^Env.fromxy(xs, ys, curve:\welch);
    }
    *bpflinear { |...seq|
	    var xs, ys;
	    # xs, ys = seq.clump(2).flop;
	    ^Env.fromxy(xs, ys);
    }
    *bpfsine { |...seq|
	    var xs, ys;
	    # xs, ys = seq.clump(2).flop;
	    ^Env.fromxy(xs, ys, curve:\sine);
    }
    *bpfsquared { |...seq|
	    var xs, ys;
	    # xs, ys = seq.clump(2).flop;
	    ^Env.fromxy(xs, ys, curve:\squared);
    }
    asBuffer {|numpoints=8192, action|
	    /*
	    Env.bpfsine(0, 0, 1, 1).asBuffer(action:{|buf| ~mybuf = buf })
	    */
	    ^Buffer.sendCollection(Server.default, this.discretize(numpoints), action:action)
    }
}

+ String {
    path_split {
        var a = this.split($/);
        var basename = a.last;
        var path = a[.. a.size-2].join($/);
        ^[path, basename];
    }
    oscfunc {�|func|
	    ^OSCFunc(func:func, path:this);
    }
}

+ Buffer {
    *cue {|server, path|
        var channels = SoundFile(path).numChannels;
        ^this.cueSoundFile(server, path, 0, channels);
    }
}

+ Synth {
    tail {|target|
        fork {
            this.server.sync;
            this.moveToTail(target)
        }
    }
    after {|target|
        fork {
            this.server.sync;
            this.moveAfter(target);
        }
    }
    before {|target|
        fork {
            this.server.sync;
            this.moveBefore(target);
        }
    }
    head {|target|
        fork {
            this.server.sync;
            this.moveToHead(target);
        }
    }
}

+ Array {
    nearest {|x|
        var x0, x1;
        var i = this.indexOfGreaterThan(x);
        if( i.isNil ) {
            ^this.last
        };
        if( i == 0 ) {
            ^this.first
        };
        x1 = this.at(i);
        x0 = this.at(i - 1);
        if( (x1 - x).abs < (x0 - x).abs ) {
            ^x1
        } /* else */ {
            ^x0
        };
    }
    zip {|other|
        ^[this, other].flop;
    }
}


+ True {
    * {|other|
        ^other;
    }
}

+ False {
    * {|other|
        ^0;
    }
}


+ MIDIOut {
	mmcPlay {
		this.sysex(Int8Array(6).addAll([16rF0, 16r7F, 0, 6, 2, 16rF7]));
	}
	mmcStop {
		this.sysex(Int8Array(6).addAll([16rF0, 16r7F, 0, 6, 1, 16rF7]));
	}
	mmcRec {
		this.sysex(Int8Array(6).addAll([16rF0, 16r7F, 0, 6, 6, 16rF7]));
	}
	mmcRecStop {
		this.sysex(Int8Array(6).addAll([16rF0, 16r7F, 0, 7, 2, 16rF7]));
	}
	mmcPause {
		this.sysex(Int8Array(6).addAll([16rF0, 16r7F, 0, 6, 9, 16rF7]));
	}
	mmcFastForward {
		this.sysex(Int8Array(6).addAll([16rF0, 16r7F, 0, 6, 4, 16rF7]));
	}
	mmcRewind {
		this.sysex(Int8Array(6).addAll([16rF0, 16r7F, 0, 6, 5, 16rF7]));
	}
}

+ MIDIClient {
    *getSource { |first_letters_of_name|
        /* get the midi source whose name begins with the given string.
           if not found, raise an error (NEVER fail silently in supercollider),
           if found, return always the first source found, even when there are more.
        */
        var found = MIDIClient.sources.select {�|source| source.name includesStr: first_letters_of_name };
        if( found.size > 0 ) {
            ^found[0];
        }{  // else
            throw("MIDI source not found: " ++ first_letters_of_name);
        };
    }
}

+ FFT {
    *wintype_hann { ^1 }
    *wintype_sine { ^0 }
    *wintype_rect { ^-1}
}

+ Synth {
    synthDef {
        ^SynthDefStorage.synthDefDict[defName][0]
    }
    getArgIndex { |argname|
        var synthdef = this.synthDef();
        var argname_assymbol = argname.asSymbol;
        synthdef.allControlNames.do {|control, control_index|
            if( control.name == argname_assymbol) {
                ^control_index;
            };
        }
        ^nil;
    }
}

+ SynthDef {
    getSpecs {
        ^SynthDescLib.global[name.asSymbol].tryPerform(\metadata).tryPerform(\at, \specs);
    }
}

+ Integer {
	linspace {|start, end|
		var dx = (end - start) / (this - 1);
		^(start, start+dx .. end);
	}
}
/*
+ AudioUnitBuilder {
	makeInstallForAll {
		var cmd = "cp -r scaudk/%.component /Library/Audio/Plug-Ins/Components".format(name);
		this.makePlugin;
		cmd.systemCmd;
	}
}

*/
