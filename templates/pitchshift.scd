(
a = { |interval=5|
	var buf = LocalBuf(SampleRate.ir*4);
	var pitchratio = interval.midiratio;
	var pitch = MouseX.kr(0.5, 2);
	var a0 = SoundIn.ar(0);
	(
		// a0 !> WarpIn.ar(_, ~buf.bufnum, 1, 0.5)
		a0 !> PitchShift.ar(_, pitchRatio:pitchratio)
		!> Out.ar(0, _);
	);

}.play
)

a.autogui;