b = Buffer.read(s,  "sounds/a11wlk01.wav");

(
a = {|freq=20, freqdev=0, freqrate=0, duty=0.5, base=0.1, bufnum, lagratio=0.1|
	var a0 = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), loop:1);
	var f = freq + (freqdev * LFNoise1.ar(freqrate));
	var env = LFPulse.ar(f, 0, duty) + base | _.lag(1/freq * lagratio);
	var res = a0 * env;
	Out.ar(0, a0 * 	env ! 2);
	
}.play(args:[\bufnum, b]);
)

a.autogui

LFNoise2