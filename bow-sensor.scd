(
a = { |o_dist=2, o_press=3, i_dist=2, i_press=3, o_speak=0|
	var distance_out = SinOsc.ar(10000);
	var pressure_out = SinOsc.ar(10000);
	var distance_in = SoundIn.ar(i_dist);
	var pressure_in = SoundIn.ar(i_press);
	var distance_rms = (RunningSum.ar(distance_in.squared, 1000) / 1000).sqrt;
	var pressure_rms = (RunningSum.ar(pressure_in.squared, 60) / 60).sqrt;
	var clock = Impulse.kr(30);
	distance_rms.poll(10, "distance");
	// pressure_rms.poll(10, ">>>>> pressure");
	
	distance_rms => SendReply.kr(clock, 'dist', _);
	pressure_rms => SendReply.kr(clock, 'press', _);
		
	distance_out => Out.ar(0, _);
	pressure_out => Out.ar(1, _);
}.play
)
	 
(
~dist_slider  = EZSmoothSlider(label:" dist ");
~press_slider = EZSmoothSlider(label:" press ");

~dist.remove; ~dist = OSCresponderNode(nil, 'dist', { |t, r, msg|
	var val = msg[3];
	~dist_slider.value = val.linlin(0.0039, 0.536, 0, 1);
}).add;

~press.remove; ~press = OSCresponderNode(nil, 'press', { |t, r, msg|
	var val = msg[3];
	~press_slider.value = val.linlin(0.43, 0.68, 0, 1);
}).add;
)
