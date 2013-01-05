(
var name, func, specs, componentType, componentSubtype, builder;

name = "thaumatrope-with-gaps";

func = {|freq=18, duty=0.5, dutygap=0, base0=0, base1=0, pan0=0, pan1=0, gain0=1, gain1=1, normalize=0, lagratio=0.1|
	// var a0 = PlayBuf.ar(1, b.bufnum, BufRateScale.kr(b.bufnum), loop:1);
	// var a1 = PlayBuf.ar(1, c.bufnum, BufRateScale.kr(c.bufnum), loop:1);
	var a0 = AudioIn.ar(1);
	var a1 = AudioIn.ar(2);
	var dutysnd = 1 - dutygap;
	var envA = LFPulse.ar(freq, 0, dutysnd) 
				| Lag.ar(_, 1/freq * lagratio);
	var envB = LFPulse.ar(freq, 0, duty * dutysnd) 
				| Lag.ar(_, 1/freq * lagratio);
	var envC = (1 - envB) * envA;
	var g0 = base0 + (envB * (gain0 - (base0 * normalize)));
	var g1 = base1 + (envC * (gain1 - (base1 * normalize)));
	var out0 = a0 * g0 | Pan2.ar(_, pan0);
	var out1 = a1 * g1 |ÊPan2.ar(_, pan1);
	Out.ar(0, out0 + out1);	
};

specs = #[
	[0, 300, \Logarithmic, 	18, 	\Hertz],  
	[0, 1, 	 \Linear, 	0.5,	\Generic],   
	[0, 1, 	 \Linear, 	0,		\Generic], 
	[0, 1, 	 \Linear, 	0,	\Generic],      
	[0, 1, 	 \Linear, 	0,	\Generic],      
	[-1, 1, 	 \Linear, 	0,	\Generic],      
	[-1, 1, 	 \Linear, 	0,	\Generic],      
	[0, 1, 	 \Linear, 	1,	\Generic],
	[0, 1, 	 \Linear, 	1,	\Generic],      
	[0, 1, 	 \Linear, 	0,	\Generic],      
	[0, 1, 	 \Linear, 	0.1,	\Generic],      
];

componentType = \aufx;
componentSubtype = \XTHG;
builder = AudioUnitBuilder.new(name, componentSubtype, func, specs, componentType);
builder.makeInstall;

)
