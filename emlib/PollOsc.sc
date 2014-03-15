SendOSCServer {
    classvar <path="/mog/po";   // internal address
    classvar <>oscfunc;
    classvar <>addrs;
    * defaulthandler {
        var out = OSCFunc( {|msg|
            var port, value, outpathn, outpath, labeln, label=nil, netaddr;
            port = msg[3];
            value = msg[4];
            outpathn = msg[5].asInt;
            labeln = msg[6].asInt;
            outpath = msg[7..(6 + outpathn)].asAscii;
            if( labeln > 0) {
                label = msg[7+outpathn..].asAscii;
            };
            netaddr = addrs[port];
            if( netaddr.isNil ) {
                netaddr = NetAddr("127.0.0.1", port);
                addrs[port] = netaddr;
            };
            if (label.notNil) {
                netaddr.sendMsg(outpath, label, value);
            }{
                netaddr.sendMsg(outpath, value);
            };
        }, path);
        ^out;
    }
    *debug {
        var out = OSCFunc( {|msg|
            var port, value, outpathn, outpath, labeln, label=nil, netaddr;

            port = msg[3];
            value = msg[4];
            outpathn = msg[5].asInt;
            labeln = msg[6].asInt;
            outpath = msg[7..(6 + outpathn)].asAscii;
            if( labeln > 0) {
                label = msg[7+outpathn..].asAscii;
            };
            [port, value, outpath, label].postln;
            netaddr = addrs[port];
            if( netaddr.isNil ) {
                netaddr = NetAddr("127.0.0.1", port);
                addrs[port] = netaddr;
            };
            if (label.notNil) {
                netaddr.sendMsg(outpath, label, value);
            }{
                netaddr.sendMsg(outpath, value);
            };
        }, path);
        ^out;
    }

    *initClass {
        StartUp.add {
            oscfunc = SendOSCServer.defaulthandler.fix;
        };
        addrs = IdentityDictionary();
    }
}

SendOSC {
    * kr {|in, port, path, label, trig=nil|
        /*  like Poll, but sends OSC. If given, label will be the first argument

        Amplitude.ar(WhiteNoise.ar(0.5)) !> SendOsc.kr(_, path:"/noise/amp", port:9999) --> oscsend localhost 9999 /noise/amp "f" <amp>
        Amplitude.ar(WhiteNoise.ar(0.5)) !> SendOsc.kr(_, path:"/monitor", label:"amp", port:9999) --> oscsend localhost 9999 /monitor "sf" "amp" <amp>
        */

        var outarray;
        if( in.rate == \audio ) {
            trig = trig ? Impulse.kr(8);
        } {
            trig = trig ? Changed.kr(in);
        };

        if( label.notNil) {
            outarray = [port, in, path.asString.size, label.asString.size] ++ path.ascii ++ label.ascii;
        }{
            outarray = [port, in, path.asString.size, 0] ++ path.ascii;
        };
        SendReply.kr(trig, SendOSCServer.path, outarray);
        ^in;
    }
}

+ UGen {
    oscsend {|port, path, label=nil, trig=nil|
        /*
        like .poll, but sends OSC
        Example:

        // send the current value with the prepended string 'sin' to port 31415, to the path '/print'
        SinOsc.kr(10).sendosc(port:31415, path:'/print', label:'sin')

        */
        label = label ? this.generate_label();
        SendOSC.kr(this, port, path, label, trig);
        ^this;
    }
    oscprint {|label, trig=nil, port=31415, fader=false, convfunc|
        var oscpath;
        var obj = if( convfunc.isNil ) {this} {convfunc.(this)};
        label = label ? this.generate_label();

        if( obj.rate == 'audio' ) {
            trig = trig ? Impulse.kr(8);
            Amplitude.kr(obj, attackTime:0.05, releaseTime:0.05).sendosc(port, "/print/vu", label, trig:trig);
            ^this;
        }{
            oscpath = if(fader, '/print/vu', '/print');
            obj.oscsend(port:port, path:oscpath, label:label, trig:trig);
            ^this;
        };
    }
    oscpeak {|label, decay=0.999, trig=nil, port=31415, suffix="[peak]"|
        label = label ? this.generate_label() ++ suffix;
        ^SendOSC.kr(PeakFollower.kr(this, decay:decay), port:port, path:"/print/vu", label:label);
    }
    generate_label {
        var inputstr = this.inputs.collect(_.asString).join(", ");
        ^"%(%)".format(this.class, inputstr);
    }
}
