(
a = { 
	SoundIn.ar(0) | (_ ! 2) | Out.ar(2, _);
	SoundIn.ar(1) | (_ ! 2) | Out.ar(0, _);
}.play
)
	
	
