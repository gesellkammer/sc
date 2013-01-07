PhasorLine { 
    *ar { arg x0=0, x1=1, dur=1, mul=1, add=0;
        var sr = SampleRate.ir;
        var speed = (x1 - x0) / dur;
        ^Phasor.ar(Impulse.ar(1/dur), speed/sr, x0*sr, x1*sr, x0*sr).madd(mul, add);
    }
}

