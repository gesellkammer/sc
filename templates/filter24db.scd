(
z = { |freq=1000, q=4|
	PinkNoise.ar
	!> BMoog.ar(_, freq, q, 0)
	!> Out.ar(0, _);
}.play
)

(
z.autogui;
s.freqscope;
)