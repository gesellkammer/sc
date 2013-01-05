 SynthDef(\test, { |freq, rel=0.2, gate=1, width=0.5|
	 var att=0.005;
	 var env = Env.adsr(att, rel, sustainLevel:0, releaseTime:0.001)
	 		 => EnvGen.ar(_, gate, doneAction:2);
	 var signal = VarSaw.ar(freq, width:width) * env;
	 Out.ar(0, signal!2);
}).add
	 
~notes = ("C4".tomidi .. "C6".tomidi);

Pbind( 
	\instrument, \test,
	\midinote, Pseq(~notes),
	\dur, 0.1,
	\rel, ~notes.size.linspace(0.001, 0.4) => Pseq,
	\width, ~notes.size.linspace(0.01, 0.8) => Pseq
).play


