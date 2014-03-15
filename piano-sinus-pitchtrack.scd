~trigbus = Bus.control();

(
a = { |det_cent=0, det_hz=4, det_cent2=0, det_hz2 = -4, pitchthresh=0.93, sigamp=2, sigamp2=2, ampup=1, ampdown=3, follup=2, folld=7, mix=0.7, lagpitch=0.2, pan=0, fback=0|
	var pitch, hasp, amp, signal1, pitch1, pitch2, sourceamp, signal2, amp2, out;
	var a1 = LocalIn.ar(1);
	var a0 = SoundIn.ar(19) + (a1 * fback);
	#pitch, hasp = Tartini.kr(a0, threshold:pitchthresh, overlap:512);
	sourceamp = Amplitude.kr(a0);
	amp = sigamp * sourceamp * (hasp => LagUD.kr(_, ampup, ampdown)) => LagUD.kr(_, follup, folld);
	amp2 = sigamp2 * sourceamp * (hasp => LagUD.kr(_, ampup * 1.2, ampdown * 0.9)) => LagUD.kr(_, follup * 0.9, folld * 1.2);
	pitch1 = (pitch.cpsmidi + det_cent).midicps + det_hz => Lag.kr(_, lagpitch);
	pitch2 = (pitch.cpsmidi + det_cent2).midicps + det_hz2 => Lag.kr(_, lagpitch);
	signal1 = SinOsc.ar(pitch1) * amp;
	signal2 = SinOsc.ar(pitch2, pi*0.25) * amp2;
	
	out = a0 * (1-mix)
		+ (signal1 * mix)
		+ (signal2 * mix);
	out => LocalOut.ar(_);
	out => Pan2.ar(_, pan) => Out.ar(0, _);
	

}.play
)

SinOsc
a.autogui

