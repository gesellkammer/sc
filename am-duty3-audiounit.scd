( var name, func, specs, componentType, componentSubtype, builder;

name = "amdutystereo";
func = {|freq=20, freqdev=0, freqrate=0, duty=0.5, base=0.1, lagratio=0.1|
	var a0 = AudioIn.ar(1);
	var a1 = AudioIn.ar(2);
	var f = freq + (freqdev * LFNoise1.ar(freqrate));
	var env = (
		LFPulse.ar(f, 0, duty) + base
	).lag(1/freq * lagratio);
	Out.ar(0, [a0*env, a1*env]);
};

specs = #[
	[0, 1000, \Logarithmic, 1, \Hertz],
	[0, 20, \Linear, 0.01, \Hertz],
	[0, 10, \Linear, 0.01, \Hertz],
	[0, 1, \Linear, 0.01, \Generic],
	[0, 1, \Linear, 0.01, \Generic],
	[0, 1, \Linear, 0.01, \Generic],
];


componentType = \aufx;
componentSubtype = \XAMD;

builder = AudioUnitBuilder.new(name, componentSubtype, func, specs, componentType);
builder.makeInstall;
)