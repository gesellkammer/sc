Valve {
    /*
    A Pseudo Ugen to simulate the action of a valve in a trumpet or horn
    */
    *ar {|in, state, dist1=0, dist0=0.01, i_c=343|
        /*
        in: audio to pass through the valve
        state: state of the valve itself (0=up, 1=down)
        dist1: distance (in m) when the valve is down
        dist0: distance (in m) when the valve is up
        i_c: speed of propagation
        */
        var del0 = dist0/i_c;
        var del1 = dist1/i_c;
        var a0 = DelayN.ar(in, del0, del0);
        var a1 = DelayN.ar(in, del1, del1);
        var out = SelectX.ar(state, [a0, a1]);
        ^out;
    }
    *new {|in, state, dist1=0, dist0=0.01, c=343, attenuate=1|
        ^Valve.ar(in, state, dist1, dist0, c, attenuate);
    }
}

DuctAtten {
    /*
    A Pseudo Ugen to simulate the attenuation on a duct with the shape of the valve
    A realistic simulation should also implement the resonance of the tube
    --> see Acoustics.duct_resonfreq_unflanged
    */
    *ar {|in, length, radius=0.01, i_c=343|
        /*
        in: audio to pass through the valve
        length: the length of the Duct. Used for resonance --> not implemented;
        radius
        */
        var f_cutoff = Acoustics.duct_maxfreq(radius);
        ^LPF.ar(in, f_cutoff);
    }
}

SelectX2 {
    /*
    This is just a shortcut for Select.ar(which, [if0, if1]) to avoid encapsulating
    the signals in an array so that it works with the _ notation.

    Example
    =======

    signal !> SelectX2.ar(which, _, Silence.ar)
    */
    *ar {|which, a, b|
        ^SelectX.ar(which, [a, b]);
    }
    *kr {|which , a, b|
        ^SelectX.kr(which, [a, b]);
    }
    *new {|which, a, b|
		var rate = a.asArray.collect(_.rate).unbubble;
        if( rate == 'audio' ) {
            ^SelectX2.ar(which, a, b);
        } {
            ^SelectX2.kr(which, a, b);
        };
    }
}

DelayConst {
    /*
    This is just a shortcut for DelayN where we dont want to change the delay time
    Useful for waveguide simulation. It supports delays shorter than a control period.
    */
    *ar {|in, time|
        ^DelayN.ar(in, time, time);
    }
    *kr {|in, time|
        ^DelayN.kr(in, time, time);
    }
    *new {|in, time|
		var rate = in.asArray.collect(_.rate).unbubble;
        if( rate == 'audio' ) {
            ^DelayConst.ar(in, time);
        } {
            ^DelayConst.ar(in, time);
        };
    }
}
