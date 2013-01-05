(
var name, func, specs, componentType, componentSubtype, builder;

name = "thtrope2";

func = {|freq=20, duty=0.5, gap0=0.1, gap1=0.1, 
	     gain1=1, gain2=1, base1=0, base2=0, 
	     pan1=0, pan2=0, lagratio=0.1|
	// var a1 = PlayBuf.ar(1, b.bufnum, BufRateScale.kr(b.bufnum), loop:1);
	// var a2 = PlayBuf.ar(1, c.bufnum, BufRateScale.kr(c.bufnum), loop:1);
	var a1 = AudioIn.ar(1);
	var a2 = AudioIn.ar(2);
	var d0 = 1 - (gap0 + gap1);
	var d1 = d0 * duty;
	var d2 = d1 + gap0;
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
};

specs = #[
	[0, 200, \Logarithmic, 	18, 	\Hertz],  
	[0, 1, 	 \Linear, 	0.5,		\Generic], 
	[0, 1, 	 \Linear, 	0.1,	\Generic],   
	[0, 1, 	 \Linear, 	0.1,	\Generic],      
	
	[0, 1, 	 \Linear, 	1,	\Generic],
	[0, 1, 	 \Linear, 	1,	\Generic],      
	[0, 1, 	 \Linear, 	0,	\Generic],
	[0, 1, 	 \Linear, 	0,	\Generic],      
	
	[-1, 1, 	 \Linear, 	0,	\Generic],      
	[-1, 1, 	 \Linear, 	0,	\Generic],      
	[0, 1, 	 \Linear, 	0.1,	\Generic],      
];

componentType = \aufx;
componentSubtype = \XTH2;
builder = AudioUnitBuilder.new(name, componentSubtype, func, specs, componentType);
builder.makeInstall;

)
