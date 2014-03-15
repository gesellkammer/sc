(
a = {
	SoundIn.ar(0) => Out.ar(0, _)
}.play
)

(
b = {Ê|freq1=17, df=0.5, amp=1|
	var a0 = SoundIn.ar(0);
	var a1 = 	LFPar.ar([freq1, freq1+df]) => Mix(_) * amp;
	a0 => EM_FeedbackKill.ar(_)
	+ a1
	=> Out.ar(0, _);
}.play
)

b.autogui

LF