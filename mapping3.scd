(

a = { |a0=1, a1=0.2, a2=0.01|
	var h2 = SoundIn.ar(2) + SoundIn.ar(3); 
	var contact_mic = SoundIn.ar(0) ;
	var nxt = 0;
	var speaker = 1;
	h2 | (_ * a0) | Out.ar(nxt, _);
	contact_mic | Out.ar(speaker, _);
	contact_mic * a1 
		| GVerb.ar(_, roomsize:5, revtime:0.6, damping:0.62, inputbw:0.48,
				  drylevel:-6, earlyreflevel:-11, taillevel:-13) 
		| Out.ar(speaker, _);
	// contact_mic | (_ * a2) | LPF.ar(_, 5000) | Out.ar(0, _);
	
}.play


)

a = Synth(\pru)

a[\a2] = 0.0
a[\a0] = 2
1+1


E
SynthDefAutogui
Def

def
Server.killAll