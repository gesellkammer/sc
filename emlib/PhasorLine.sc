PhasorLine {
    *ar { arg x0=0, x1=1, dur=1, trig=0, mul=1, add=0;
		var impulse = Impulse.ar(1/dur);
		var trig2 = if( impulse > trig, impulse, trig );
        var sr = SampleRate.ir;
        var speed = (x1 - x0) / dur;
        ^Phasor.ar(trig2, speed/sr, x0*sr, x1*sr, x0*sr).madd(mul, add);
    }
}

