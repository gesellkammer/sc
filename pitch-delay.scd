(
a = { |delay=0.175, gain=1, grainsize=0.05, ratio=0.1|
	SoundIn.ar(0)
	| PitchShift.ar(_, grainsize, ratio)
	| DelayC.ar(_, 1, delay)
	| (_ * gain)
	| Out.ar([0, 1], _)
}.play
)

a.autogui
a.free
PitchShift