( var name, func, specs, componentType, componentSubtype, builder;

name = "tapebowMono";
func = {|lagtime=0.01|
	var a0 = AudioIn.ar(1);
	var a1 = AudioIn.ar(2);
	var highest = (a0.abs > a1.abs).lag(lagtime);
	var mix = (a0 * highest) + (a1 * (1 - highest));
	Out.ar(0, mix ! 2);	
};

specs = #[
	[0, 2, \Linear, 0.001, \Seconds]
];


componentType = \aufx;
componentSubtype = \XTBM;

builder = AudioUnitBuilder.new(name, componentSubtype, func, specs, componentType);
builder.makeInstall;
)