(
a = {|freq1=440, freq2=450, delay_ms=2, rmstime=0.04, gain=5, dc=0, phase1=0.1, minfreq=1, maxfreq=80|
	var sig = SinOsc.ar(freq1, phase1) * 0.2 + (SinOsc.ar(freq2, 0) * 0.2);
	var sig2 = sig + DelayN.ar(sig, 1, delay_ms / 1000);
	var rms = RunningSum.rms(sig2, rmstime * SampleRate.ir);
	var rms2 = LeakDC.ar(rms) * gain + dc;
	var pitch, hasp;
	// ZeroCrossing.ar(rms2).poll(10);
	#pitch, hasp = Pitch.kr(rms2, minFreq:minfreq, maxFreq:maxfreq);
	Out.ar(0, rms2);
	/*
	
	
	Out.ar(0, [sig2, SinOsc.ar(1000 + (rms2 * 1000))])
	*/
}.play
)

a.autogui


1/2*pi