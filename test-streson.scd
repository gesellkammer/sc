(
a = { |res=0.989, df=12, strgain=0.1, td=0.01, gain2=0.2, gainoct=1, harm=1|
	var freqs = 6.collectÊ{|i| (54 + i).midicps };
	var a1 = SoundIn.ar(1);
	freqs.collect {|freq, n|
		var res0 = Streson.ar(a1, 1/freq, res);
		var res1 = PitchShift.ar(a1, pitchRatio:(freq - df)/freq, timeDispersion:td)
		           => Streson.ar(_, 1/(freq-df), res);
		var res2 = PitchShift.ar(a1, pitchRatio:(freq + df)/freq, timeDispersion:td)
				  => Streson.ar(_, 1/(freq+df), res);
		var oct0 = Streson.ar(a1, 1/(freq * 2 * harm), res);
		res0
		+ 
		((res1 + res2) * gain2)
		+ (oct0 * gainoct)
		* strgain
		=> Limiter.ar(_, 0.5, 0.005)
		=> Pan2.ar(_, n.linlin(0, 5, -1, 1))
	} => Mix(_) => Out.ar(0, _);
}.play	
	
		
)

PitchShift
a.ccbind(BCF.fd(1), \res, [0.6, 1.1])
a.ccbind(BCF.fd(2), \cents, 100)
a.ccbind(BCF.fd(3), \strgain, 1)
a.ccbind(BCF.fd(4), \td, 0.4)
a.ccbind(BCF.fd(5), \gain2, 1)
a.ccbind(BCF.fd(6), \gainoct, 1)
a.ccbind(BCF.fd(7), \harm, {|x| x.linlin(0, 127, 0.5, 1.5)})