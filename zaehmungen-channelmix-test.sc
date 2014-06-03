(
/*
~hist = {|in, thresh_att=0.03, thresh_rel=0.01, attack=0.01, hold=0.1, release=0.2, min_rest=0.01|
	var t0 = in > thresh_att !> Trig.kr(_, hold);
	var t1 = in < thresh_rel * (1-t0) !> Trig.kr(_, min_rest);
	var trig_envelope = SetResetFF.kr(t0, t1);
	var out = EnvGen.kr(Env.adsr(attack, 0, 1, release), gate:trig_envelope);
	out;
};
*/

a = {|gain=1, lagu=0.5, lagd=0.1, lag2=0.01|
	var in0 = SoundIn.ar(0);
	var in1 = SoundIn.ar(1);
	var amp0 = Amplitude.ar(in0, attackTime:0.05, releaseTime:0.05);
	var amp1 = Amplitude.ar(in1, attackTime:0.05, releaseTime:0.05);
	var which0 = (amp0 < amp1).lagud(lagu, lagd);
	var which = which0 > 0.5 !> _.lag(lag2);
	SelectX.ar(which, [in0, in1])
	* gain
	!> HPF.ar(_, 80)
	!> EM_Compress.ar(_)
	!> Out.ar([0, 1], _);

	which0.oscsend(31415, "/print/fader", "which0", Impulse.kr(20));
	which.oscsend(31415, "/print/fader", "which", Impulse.kr(20));
	amp0.oscsend(31415, "/print/vu", "amp0");
	amp1.oscsend(31415, "/print/vu", "amp1");

}.play
)

a.set(lag1, 1)


a.autogui

EM_Hysteresis