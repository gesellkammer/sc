(
b = Buffer.alloc(s, 4 * 48000);
)


(
y = { |bufnum, rate=1|
	var a0 = SoundIn.ar(0);

	BufWr.ar(in, 0, Phasor.ar(0, BufRateScale.kr(bufnum) * rate, 0, BufFrames.kr(bufnum)));
	0.0;
}.play(args:[bufnum: b]);
)