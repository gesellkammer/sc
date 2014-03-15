(
a = {
	var distance_out = SinOsc.ar(10000);
	var pressure_out = SinOsc.ar(10000);
	var distance_in = In.ar(0);
	var pressure_in = In.ar(1);
	var distance_rms = (RunningSum.ar(distance_in.squared, 1000) / 1000).sqrt;
	var pressure_rms = (RunningSum.ar(pressure_in.squared, 60) / 60).sqrt;
	
	distance_rms.poll(10, "distance");
	pressure_rms.poll(10, "pressure");
	
	distance_out | Out.ar(0, _);
	pressure_out | Out.ar(1, _);
}.play
)
	 
					 