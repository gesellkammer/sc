
(
a = { |in=18, generation=4|
	var a1;
	a1 = Out.ar(0, SoundIn.ar(in));

	// var vel = MPVelocity.ar(in, generation:gen);
	// var pos = MPPosition.ar(in, generation:gen);
	// [vel, pos].poll(2);
	
}.draw
)


a = { In.ar(0, 2) }.play

b

(
m = { |in=18, gen=4|
	var a1, vel, pos, signal;
	a1 = SoundIn.ar([in, in+1]);
	signal = MPSignalPower.ar(a1) > 0.11;
	vel = A2K.kr(MPVelocity.ar(a1, generation:gen) * signal);
	pos = A2K.kr(MPPosition.ar(a1, generation:gen) * signal);
	[vel, pos].poll(10);
	// a1 | Out.ar(2, _);
	Silence.ar;
	// WhiteNoise.ar | Out.ar(b, _)
}.play
) 

(
SynthDef(\test, { |in=18, thresh=0.1, gen=4,sharp=0, abs=1|
	var a1, a2, a3, vel, pos, signal, err;
	a2 = SoundIn.ar([in, in+1]);
	
	pos = MPPosition.ar(a2, absolute:abs, threshold:thresh, generation:gen, sharpness:sharp) |ÊA2K.kr(_);
	err = MPErrorMetric.ar(a2, absolute:abs, threshold:thresh, sharpness:sharp, generation:gen);
	[pos, err].poll(5);
	//Silence.ar;
	Out.ar(2, a2);
	// WhiteNoise.ar | Out.ar(b, _)
}).add
)

n = Synth(\test)
n.free
n.autogui