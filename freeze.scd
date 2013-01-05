MIDIClient.init
MIDIIn.connectAll

(
fork {
	b = {|freeze=0|
		var a0 = SoundIn.ar(0);
		var a1 = SoundIn.ar(1);
		var wet0 = FFT(LocalBuf(4096), a0) !> PV_Freeze(_, freeze) !> IFFT(_);
		var wet1 = FFT(LocalBuf(4096), a1) !> PV_Freeze(_, freeze) !> IFFT(_);
		var mix0 = SelectX.ar(freeze, [a0, wet0]);
		var mix1 = SelectX.ar(freeze, [a1, wet1]);
		Out.ar(0, [mix0, mix1]);
	}.play;
	
	~rms = {|rmstime=0.04, minfreq=1, maxfreq=80, lagU=1, lagD=1|
		var a0 = SoundIn.ar(0);
		var rms = RunningSum.rms(a0, rmstime * SampleRate.ir) !> LeakDC.ar(_);
		var pitch, hasp;
		
		#pitch, hasp = Pitch.kr(rms, minFreq:minfreq, maxFreq:maxfreq);
		pitch = (pitch * hasp) !> LagUD.kr(_, lagU, lagD);
		SendReply.kr(Impulse.kr(20), "/rmsfreq", pitch);
		Out.kr(0, 0);
	}.play;
	
	s.sync;
	
	~pedal = MIDIFunc.cc(
		{ |val, a1, a2, a3| 
			b.set(\freeze, val/127.0);
			val.postln;
		},
		ccNum: 100, 
		srcID: MIDIClient.getSource("BCF").uid
	);
	
	~rms_resp = OSCFunc({|msg|
		msg[3].postln;
	}, "/rmsfreq"
	);
};
)

// -------------------------------------------------------------
(
~dumpcc = MIDIFunc.cc({|... args| args.postln; })
)



// -------------------------------------------------------------

{ SoundIn.ar(0) !> Out.ar(0, _) }.play

LagUD.kr(