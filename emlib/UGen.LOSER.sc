LOSER_Saturation {
    *ar {|sig, amount=0|
        // amount: from 0 to 1
        var amount0 = max(amount, 0.000001);
        var foo = amount0 / 2 * pi;
        var bar = sin(foo);
        ^min(max( sin(max(min(sig, 1), -1) * foo) / bar, -1), 1);
    }
}

LOSER_SaturationOS {
    *ar { |sig, amount=0|
        sig = UpSample.ar(sig);
        sig = LOSER_Saturation.ar(sig, amount);
        ^( DownSample.ar(sig) );
    }
}

/*
// (C) 2006-2007, Michael Gruhn.

desc:Saturation

slider1:0<0,100,1>Amount (%)

@init
gfx_clear = 0;
@slider
foo=slider1/200*$pi;
bar = sin(slider1/200*$pi);


@sample
slider1 ? (
spl0 = min(max( sin(max(min(spl0,1),-1)*foo)/bar ,-1) ,1);
spl1 = min(max( sin(max(min(spl1,1),-1)*foo)/bar ,-1) ,1);
);

*/

LOSER_WaveShapeDist {
    // wave-shapping distortion as implemented in Reaper
    *ar {|sig, amount=0|
        var amount0 = max(amount, 0.000001);
        var hdistr = min(amount0, 0.999);
    	var foo = 2 * hdistr / (1-hdistr);
    	sig = min(max(sig, -1), 1);
    	^(
    	    (1 + foo) * sig / (1 + (foo * abs(sig)))
        )
    }
}

LOSER_WaveShapeDistOS {
    *ar { |sig, amount=0|
        sig = UpSample.ar(sig);
        sig = LOSER_WaveShapeDist.ar(sig, amount);
        ^( DownSample.ar(sig) );
    }
}

LOSER_WaveShapeDist2 {
    *ar { |sig, amount=0|
        ^LOSER_WaveShapeDist.ar(LOSER_WaveShapeDist.ar(sig, amount), amount);
    }
}

LOSER_WaveShapeDist2OS {
    *ar { |sig, amount=0|
        sig = UpSample.ar(sig);
        sig = LOSER_WaveShapeDist2.ar(sig, amount);
        ^( DownSample.ar(sig) );
    }
}


/*
// (C) 2007, Michael Gruhn.

desc:Waveshapping Distortion

slider1:0<0,100,.1>Distortion

@slider
hdistr = min(slider1/100,.999);
foo = 2*hdistr/(1-hdistr);

@sample
spl0 = min(max(spl0,-1),1);
spl1 = min(max(spl1,-1),1);

spl0 = (1+foo)*spl0/(1+foo*abs(spl0));
spl1 = (1+foo)*spl1/(1+foo*abs(spl1));

*/

