(
	var name, func, specs, componentType, componentSubtype, builder;
	name = "splitrandA";
	func = {
		|thresh, numslots|
		var in = AudioIn.ar(1);
		var onsets = FFT(LocalBuf(1024), in)
					  | Onsets.kr(_, thresh);
					  
		var outchan = TIRand.kr(0, numslots - 1, onsets); // Stepper.kr(_, 0, 0, numslots-1, 1);
		//Out.ar(0, [in, DC.ar(outchan)]);
		Out.ar(0, WhiteNoise.ar(EnvGen.kr(Env.perc(0.001, 0.1, 0.2), onsets)));	};
	
	specs = #[
		[0, 1, \Linear, 0.22, \Generic],
		[0, 16, \Linear, 4, \Indexed]
	];
	
	componentType = \aufx;
	componentSubtype = \E1DA;
	builder = AudioUnitBuilder.new(name, componentSubtype,func, specs, componentType);
	//builder.makePlugin;
	builder.makeInstall;
)

(
	var name, func, specs, componentType, componentSubtype, builder;
	name = "splitrandB";
	func = {
		|slot, lagt|
		var in = AudioIn.ar(1);
		var info = AudioIn.ar(2);
		var lagtime = lagt / 1000;
		var outchan = A2K.kr(info);
		var amp = (outchan > (slot - 1)) * (outchan < (slot + 1)) | Lag.kr(_, lagtime);
		var out = in * amp;
		Out.ar(0, [out, out])
	};

	specs = #[
			[0, 16, \Linear, 0, \Indexed],
			[0, 2000, \Linear, 150, \Milliseconds]
	];
	componentType = \aufx;
	componentSubtype = \E1DB;
	builder = AudioUnitBuilder.new(name, componentSubtype,func, specs, componentType);
	builder.makeInstall;
)

RT