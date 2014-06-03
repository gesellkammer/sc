( var name, func, specs, componentType, componentSubtype, builder;

name = "amduty2";
func = {|freq=20, freqfine=0, duty=0.5, base=0.0, gain=1, lagratio=0.1|
	var a0 = AudioIn.ar(1);
	var a1 = AudioIn.ar(2);
	var f = max(freq + freqfine, 0);
	var env =(
		LFPulse.ar(f, 0, duty) * gain
			+ base
	).lag(1/f * lagratio);
	Out.ar(0, [a0*env, a1*env]);
};

specs = #[
	[0, 300, \Linear, 20, \Hertz],
	[-5, 15, \Linear, 0, \Hertz],
	[0, 1, \Linear, 0.5, \Generic],
	[0, 1, \Linear, 0.0, \Generic],
	[0, 1, \Linear, 1.0, \Generic],
	[0, 1, \Linear, 0.1, \Generic]
];


componentType = \aufx;
componentSubtype = \XAM2;

builder = AudioUnitBuilder.new(name, componentSubtype, func, specs, componentType);
builder.makeInstall;
)