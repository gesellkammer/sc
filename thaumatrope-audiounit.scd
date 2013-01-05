(
var name, func, specs, componentType, componentSubtype, builder;

name = "thaumatrope";
func = {|freq=18, duty=0.5, lagratio=0.1, base0=0, base1=1, mono=1|
	var a0 = AudioIn.ar(1);
	var a1 = AudioIn.ar(2);
	var env = LFPulse.ar(freq, 0, duty) | Lag.ar(_, 1/freq * lagratio);
	var out0 = (a0 * (env - base0)) + (a0 * base0);
	var out1 = (a1 * (1 - (env - base1))) + (a1 * base1);
	var stereosignal = [out0, out1];
	var monosignal = out0 + out1;
	
	Out.ar(0, 
		([monosignal, monosignal] * mono) + (stereosignal * (1 - mono))
	);  
	// NB: when outputting mono, we do scale the signal by 0.5 since they 
	// are complementary.
};

specs = #[
	[0, 300, \Logarithmic, 	0.2, 		\Hertz],
	[0, 1, 	 \Linear, 		0.01,	\Generic],
	[0, 1,   \Linear, 		0.01, 	\Generic],
	[0, 1,   \Linear, 		0.01, 	\Generic],
	[0, 1, 	 \Linear, 		0.01, 	\Generic],
	[0, 1, 	 \Linear, 		0.01, 	\Generic]
];

componentType = \aufx;
componentSubtype = \XTHM;

builder = AudioUnitBuilder.new(name, componentSubtype, func, specs, componentType);
builder.makeInstall;

)
