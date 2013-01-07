Valve {
    /*
    A Pseudo Ugen to simulate the action of a valve in a trumpet

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
        var a0 = DelayC.ar(in, del0, del0);
        var a1 = DelayC.ar(in, del1, del1);
        var out = SelectX.ar(state, [a0, a1]);
        ^out;
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
    *ar {|which, a0, a1|
        ^SelectX.ar(which, [a0, a1]);
    }
    *kr {|which , a0, a1|
        ^SelectX.kr(which, [a0, a1]);
    }
}

DelayConst {
    /*
    This is just a shortcut for DelayN where we dont want to change the delay time
    Useful for waveguide simulation
    */
    *ar {|in, time|
        ^DelayN.ar(in, time, time);
    }
    *kr {|in, time|
        ^DelayN.kr(in, time, time);
    }
}
