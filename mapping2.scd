(

a = { |a0=1, a2=0.1|
	SoundIn.ar(0) + SoundIn.ar(1) | (_ * a0) | Out.ar(0, _);
	SoundIn.ar(2) | Out.ar(1, _);
	SoundIn.ar(2) | (_ * a2) | LPF.ar(_, 5000) | Out.ar(0, _);
}.play


)


LPF
a[\a2] = 0.05
