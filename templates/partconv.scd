(
fork {
	var fftsize = 4096;
	~buf = PartConv.getBuffer(
		"/media/data/Audio/IR/piano/upright_piano_nodamp_close.wav",
		fftsize:fftsize
	);

	s.sync;

	~syn = {
		SoundIn.ar(0)
		!> PartConv.ar(_, fftsize, ~buf.value.bufnum)
		!> Out.ar([0, 1], _)
	}.play;
}
)

~buf.free



