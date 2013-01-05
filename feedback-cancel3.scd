(
a = { |ampthresh=0.125, pcile=0.9, pcile_thresh= 3500, amprelease=0.02, lagin=0.2, lagout=0.3, amplim= -4, shiftrange=0.005, aten=0.5, bw=0.0065, atenfact=0.5, feedthresh=0.5| 
	var a1 = SoundIn.ar(1);
	var amp = Amplitude.kr(a1, 0.01, amprelease);
	var ch = FFT(LocalBuf(1024), a1) => SpecPcile.kr(_, pcile);
	var pitch, haspitch, atenuated, isfeed;
	var pitch2, haspitch2, pitch3, haspitch3, aten2, aten3, a2, a3;
	#pitch, haspitch = Tartini.kr(a1);
	isfeed =	
	(
	 ((amp > amplim.dbamp) * haspitch)//.poll(10, 'LIM')
	 + 
	 ((ch > pcile_thresh) * (amp > ampthresh) * haspitch)//.poll(10, '----------- PCILE')
	 >= feedthresh
	) => LagUD.kr(_, lagin, lagout);
	
	atenuated = BBandStop.ar(a1, pitch, bw) 
			  => BBandStop.ar(_, pitch, bw) 
			  => PitchShift.ar(_, 0.1, TRand.kr(1-shiftrange, 1+shiftrange, Impulse.kr(10)).lag(0.1).poll(1, 'shift'))
			  * aten;
	#pitch2, haspitch2 = Tartini.kr(atenuated);
	aten2 = aten * atenfact;
	a2 = LinSelectX.ar(LagUD.kr(haspitch2, lagin, lagout), 
		[atenuated, 
		// ---------------------
		BBandStop.ar(atenuated, pitch, bw) 
		=> BBandStop.ar(_, pitch, bw) 
         => PitchShift.ar(_, 0.1, TRand.kr(1-shiftrange, 1+shiftrange, Impulse.kr(10)).lag(0.1))
		* aten2
		]
	);
	#pitch3, haspitch3 = Tartini.kr(a2);
	aten3 = aten2 * atenfact;
	a3 = Select.ar(LagUD.kr(haspitch3, lagin * 0.4, lagout* 0.4), 
		[a2, 
		// ---------------------
		BBandStop.ar(atenuated, pitch, bw) => BBandStop.ar(_, pitch, bw) 
		=> PitchShift.ar(_, 0.1, TRand.kr(1-shiftrange, 1+shiftrange, Impulse.kr(10)).lag(0.1))
		* aten3
		]
	);

	#pitch3, haspitch3 = Tartini.kr(a2);
	aten3 = aten2 * atenfact;
	a3 = Select.ar(LagUD.kr(haspitch3, lagin * 0.4, lagout* 0.4), 
		[a2, 
		// ---------------------
		Notch.ar(atenuated, pitch, bw) => Notch.ar(_, pitch, bw) 
		=> PitchShift.ar(_, 0.1, TRand.kr(1-shiftrange, 1+shiftrange, Impulse.kr(10)).lag(0.1))
		* aten3
		]
	);
	
	LinSelectX.ar(isfeed, [a1, a3])
	=> Limiter.ar(_, 1, 0.01)
	=> Out.ar(0, _);
}.play
)
Lag
a.autogui
Spe