PV_BandPass : PV_ChainUGen {
	*new { |buf, min_freq=800, max_freq=1000|
	    var nyfreq = SampleRate.ir / 2;
	    var thresh0 = min_freq / nyfreq;
	    var thresh1 = -1 * (1 - (max_freq / nyfreq));
	    ^(PV_BrickWall(PV_BrickWall(buf, thresh0), thresh1));
	}
}

PV_HPF : PV_ChainUGen {
    *new { |buf, freq|
	    var nyfreq = SampleRate.ir / 2;
	    var thresh = freq / nyfreq;
        ^PV_BrickWall(buf, thresh);
	}
}

PV_LPF : PV_ChainUGen {
    *new { |buf, freq|
	    var nyfreq = SampleRate.ir / 2;
        var thresh = freq / nyfreq -1;
        ^PV_BrickWall(buf, thresh);
	}
}

PV_Notch : PV_ChainUGen {
    /*
    Example:

    ~synth = {
        SoundIn.ar(0)
        !> FFT(LocalBuf(2048), _)
        !> PV_Notch(_, LocalBuf(2048))
        !> IFFT(_)
        !> Out.ar([0, 1], _);
    }.play
    */
    *new { |buf, tmpbuf, min_freq=800, max_freq=1000|
        var buf2 = PV_Copy(buf, tmpbuf);
        var high = PV_HPF(buf, max_freq);
        var low  = PV_LPF(buf2, min_freq);
        ^PV_Add(low, high);
	}
}

/*
(
~synth = {
    var winsize=1024;
    // WhiteNoise.ar(\ngain.kr(0.2))
    SoundIn.ar(3)
    !> FFT(LocalBuf(winsize), _)
    !> PV_BinFilter(_, \freq0.kr(800) / (SampleRate.ir*0.5) * winsize, \freq1.kr(1000) / (SampleRate.ir*0.5) * winsize)
    !> IFFT(_)
    !> Out.ar([0, 1], _);
}.play
)
*/

