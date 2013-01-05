a = { |lagin=0.1, lagout=0.2, ampthresh=0.125, aten=0.2, lagrat2=1, shiftrange=0.005|
	SoundIn.ar(1) => EM_FeedbackKill.ar(_, lagin:lagin, lagout:lagout, ampthresh:ampthresh, lagrat2:lagrat2, aten:aten, shiftrange:shiftrange) => Out.ar(0, _) 
}.play

f.do( _.free ); f = [
	MIDIFunc.ccbind(BCF.fd(1), a, \lagin, 2),
	MIDIFunc.ccbind(BCF.fd(2), a, \lagout, 2),
	MIDIFunc.ccbind(BCF.fd(3), a, \lagrat2, 10),
	MIDIFunc.ccbind(BCF.fd(4), a, \shiftrange, 2),
]

Server.killAll