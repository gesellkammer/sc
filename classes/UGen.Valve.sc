Valve {
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
    *ar {|which, a0, a1|
        ^SelectX.ar(which, [a0, a1]);
    }
    *kr {|which , a0, a1|
        ^SelectX.kr(which, [a0, a1]);
}

DelayConst {
    *ar {|in, time|
        ^DelayN.ar(in, time, time);
    }
    *kr {|in, time|
        ^DelayN.kr(in, time, time);
}
