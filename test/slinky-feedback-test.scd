(
a = { |freq = 60, amp=0, lp=600, ampin=1, feed=0, wet=0.5|
	var a0 = 	LFPulse.ar(freq) * amp
			+ (SoundIn.ar(0) * ampin)
			=> BLowPass4.ar(_, lp)
			+ (SoundIn.ar(1) * feed);
	var fkill = EM_FeedbackKill.ar(a0);
	a0 * (1 + wet) + (fkill * wet)
	=> Out.ar(0, _)
}.play
)

a.autogui

