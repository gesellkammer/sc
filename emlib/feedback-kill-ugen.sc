EM_FeedbackKill {
	var <>rate = 'audio';

	*ar {|in, ampthresh=0.125, pcile=0.9, pcile_thresh= 3500, ampattack=0.01, amprelease=0.02, lagin=0.2, lagout=0.7, lagrat2=1.4,
	      amplim= -4, shiftrange=0.005, aten=0.5, bw=0.0065, atenfact=0.5, feedthresh=0.5, freqshift=2|
		var a1 = in;
    	// var amp = RunningSum.rms_kr(a1, SampleRate.ir * 0.01); // Amplitude.kr(a1, 0.01, amprelease);
    	var amp = Amplitude.kr(a1, ampattack, amprelease);
    	var ch = FFT(LocalBuf(1024), a1) !> SpecPcile.kr(_, pcile);
    	var pitch, haspitch, atenuated, isfeed;
    	var pitch2, haspitch2, pitch3, haspitch3, aten2, aten3, a2, a3;
    	var do_atenuation;
    	var lagin2 = lagin*lagrat2;
    	var lagout2 = lagout*lagrat2;
    	var lagin3 = lagin*(lagrat2*lagrat2);
    	var lagout3 = lagout*(lagrat2*lagrat2);

    	#pitch, haspitch = Tartini.kr(a1);
    	isfeed =
    	(
    	 ((amp > amplim.dbamp) * haspitch)//.poll(10, 'LIM')
    	 +
    	 ((ch > pcile_thresh) * (amp > ampthresh) * haspitch)//.poll(10, '----------- PCILE')
    	 > feedthresh
    	) !> LagUD.kr(_, lagin, lagout);

    	atenuated = BBandStop.ar(a1, pitch, bw)
    			  !> BBandStop.ar(_, pitch, bw)
    			  !> PitchShift.ar(_, 0.1, TRand.kr(1-shiftrange, 1+shiftrange, Impulse.kr(5)).lag(0.1))
    			  * aten;

    	#pitch2, haspitch2 = Tartini.kr(atenuated);
    	// amp = RunningSum.rms_kr(atenuated, SampleRate.ir * 0.01) ; // Amplitude.kr(a1, 0.01, amprelease);
    	amp = Amplitude.kr(a1, ampattack, amprelease);
    	do_atenuation = haspitch2 * (amp > ampthresh) !> LagUD.kr(_, lagin2, lagout2);
    	a2 = LinSelectX.ar(do_atenuation,
    		[atenuated,
    		// ---------------------
    		BBandStop.ar(atenuated, pitch2, bw)
    		!> BBandStop.ar(_, pitch2, bw)
    		!> FreqShift.ar(_, freqshift)
            // !> PitchShift.ar(_, 0.1, TRand.kr(1-shiftrange, 1+shiftrange, Impulse.kr(7)).lag(0.1))
    		* aten
    		]
    	);

    	#pitch3, haspitch3 = Tartini.kr(atenuated);
    	// amp = RunningSum.rms_kr(atenuated, SampleRate.ir * 0.01); // Amplitude.kr(a1, 0.01, amprelease);
    	amp = Amplitude.kr(a1, ampattack, amprelease);
    	do_atenuation = haspitch3 * (amp > ampthresh) !> LagUD.kr(_, lagin3, lagout3);
    	a3 = LinSelectX.ar(do_atenuation,
    		[a2,
    		// ---------------------
    		BBandStop.ar(a2, pitch3, bw)
    		!> BBandStop.ar(_, pitch3, bw)
    		!> FreqShift.ar(_, freqshift)
            // => PitchShift.ar(_, 0.1, TRand.kr(1-shiftrange, 1+shiftrange, Impulse.kr(11)).lag(0.1))
    		* aten
    		]
    	);

    	^ LinSelectX.ar(isfeed, [a1, a3])
    	//   => Limiter.ar(_, 1, limrelease);
	}
}

+ RunningSum {
    *rms_kr {|in, numsamp=40|
        ^(RunningSum.kr(in.squared, numsamp) * (numsamp.reciprocal)).sqrt;
    }
}