PV_BandPass : PV_ChainUGen {
	*new { |buf, min_freq=0, max_freq=24000|
	    var nyfreq = SampleRate.ir / 2;
	    var thresh0 = min_freq / nyfreq;
	    var thresh1 = -1 * (1 - (max_freq / nyfreq));
	    ^(PV_BrickWall(PV_BrickWall(buf, thresh0), thresh1));
	}
}