b = Buffer.read(s, "/Volumes/TwistedFLAC/edu/Audio/Samples/BR-lotto-atem.wav");
c = Buffer.read(s, "/Users/edu/proj/zaehmungen/tapebow/BR-futbol.wav")

(
f = {|freq=20, gap0=0.1, duty=0.5, gap1=0.1, 
	     gain1=1, gain2=1, base1=0, base2=0, 
	     pan1=0, pan2=0, lagratio=0.1|
	var a1 = PlayBuf.ar(1, b.bufnum, BufRateScale.kr(b.bufnum), loop:1);
	var a2 = PlayBuf.ar(1, c.bufnum, BufRateScale.kr(c.bufnum), loop:1);
	// var a1 = AudioIn.ar(1);
	// var a2 = AudioIn.ar(2);
	var d0 = 1 - gap0;
	var absgap1 = d0 * gap1;
	var d1 = (d0 - absgap1) * duty;
	var d2 = d1 + absgap1;
	var envA = LFPulse.ar(freq, 0, d0);
	var envB = LFPulse.ar(freq, 0, d1);
	var envC = LFPulse.ar(freq, 0, d2); 
	var envD = (1 - envC) * envA;
	var lagtime = 1/freq * lagratio;
	var g1 = base1 + (envB * gain1) | Lag.ar(_, lagtime);
	var g2 = base2 + (envD * gain2) | Lag.ar(_, lagtime);
	
	var out1 = a1 * g1 | Pan2.ar(_, pan1);
	var out2 = a2 * g2 | Pan2.ar(_, pan2);
	Out.ar(0, out1 + out2);	
}.play;
)

f.autogui