Whisper : PV_ChainUGen {
	/* 
	    Takes a PV chain and synthesizes it through Band-Enhanced oscilators
	    implementing different distribution scales
	    
	    at the moment only Bark scale is implemented
	    
	    recommended FFT for 44100-48000 : 2048, hop 0.25

		Example
		=======

		a = { |bw=1|
		    var in = SoundIn.ar(0);
		    var chain = FFT(LocalBuf(2048), in, 0.25);
		    var whispered = Whisper(chain, bw);
		    var out = whispered * bw + (in * (1 - bw));
		    Out.ar(0, out)
		}.play
	*/
	classvar <bark_edges = #[0, 100, 200, 300, 400, 510, 630, 770, 920, 
		1080, 1270, 1480, 1720, 2000, 2320, 2700, 
		3150, 3700, 4400, 5300, 6400, 7700, 9500, 
		12000, 15500];
    classvar <bark_centers = #[50, 150, 250, 350, 450, 570, 700, 840, 
		1000, 1170, 1370, 1600, 1850, 2150, 2500, 
		2900, 3400, 4000, 4800, 5800, 7000, 8500, 
		10500, 13500];
    classvar <freqs;
    *initClass {
        freqs = (bark: [bark_edges, bark_centers]);
    }
	*new { |chain, bw=1, distribution='bark'|
	    var centers, powers, edges;
	    #edges, centers = freqs[distribution];
	    powers = FFTSubbandPower.kr(chain, edges, 0);
	    ^Mix.ar(BEOsc.ar(centers, bw:bw) * powers);
	}
}