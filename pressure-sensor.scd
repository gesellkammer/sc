( // <---------- EVAL THIS 

~sensor = {|chan=7, rmstime=0.01, sendrate=20, out=0, smooth=0.2, smoothratio=2.5|
	var sin = SinOsc.ar(11000) !> Out.ar(chan, _);
	var amp = SoundIn.ar(chan) !> RunningSum.rms(_, rmstime * SampleRate.ir);
	var calibhigh = \calibhigh.tr(0);
	var caliblow = \caliblow.tr(0);
	var amphigh = Latch.ar(amp, calibhigh);
	var inittrig = Line.kr(-1, 1, 0.5) !> Trig.ar(_);
	var amplow = caliblow | inittrig !> Latch.ar(amp, _);
	var value = amp.linlin(amplow, amphigh, 0.0000001, 1) !> LagUD.ar(_, smooth, smooth*smoothratio);
	
	// the normalized value 0-1 is sent to this osc adress
	SendReply.kr(Impulse.kr(sendrate), "/press", [value]);
	
	// the value is sent also as control to this bus
	Out.kr(out, value);
}.play;

)

// calibrate the low value 
~sensor.set(\caliblow, 1);
// press as hard as you would like to be the maximum
~sensor.set(\calibhigh, 1);


// a GUI

~slider = EZSmoothSlider();
~pressfunc = OSCFunc( {|msg| ~slider.value = msg[3]; }, "/press" );

/*

NB: when changing the rmstime the sensor must be recalibrated

*/

// --------------------
~sensor.set(\sendrate, 60)
~sensor.set(\smooth, 0.4)
~sensor.set(\rmstime, 0.01)
~sensor.set(\smoothratio, 2.5)