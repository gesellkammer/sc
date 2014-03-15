(
a = {|freq=321, fback=0, nl=0, res=0, freq0=440, pregain=2, sinamp=1, blipamp=0, mix=1, xwet=1|
	var a0 = SoundIn.ar(0);
	var aorig = a0;
	var mod0 = (SinOsc.ar(freq) * sinamp) + (Blip.ar(freq) * blipamp);
	var mod = mod0; // mod0 !> OSFold8.ar(_, -1, 1); //  - 0.20260;
	var ringmod = RingModDiodeN.ar(a0, freq, fback, nl);
	var out = SelectX.ar(mix, [a0 * mod, ringmod]);
	var wetout = SelectX.ar(xwet, [aorig, out]);
	Out.ar([0, 1], wetout);
	// a0 !> MoogLadder.ar(_, freq, res) !> Out.ar([0, 1], _)
}.play
)

a.autogui
Bl
MoogVCF

(
b =  {|in = 0, out = 0, drv = 0, amp = 1,jitf = 0.1, jit = 0.1,env = 0,
lag = 1, lpp = 120, res = 0.1, gate = 1, att = 0.001, rel = 0.1, freq=440, wet=1, freq0=440|
var sig, x3, jitter, input, envf;
var input0 = SinOsc.ar(freq0); // SoundIn.ar(0);
input = SoundIn.ar(0);

// input = SinOsc.ar(freq);
drv = Lag.kr(drv, lag).dbamp;
sig = drv * Clipper8.ar(input, -1.875,1.875);
//jitter = LFNoise1.ar(jitf, jit, 1);
envf = AmplitudeMod.ar(input,att, rel);
jitter = Ê(envf * jit)+1;
x3 = sig.pow(3);
sig = ((x3 *(-0.18963)) + sig) + (x3*sig.pow(2) * 0.0161817*jitter);
sig = SoftClipper8.ar(sig);
sig = XFade2.ar(sig, sig*envf, (env * 2)-1);
sig = LeakDC.ar(sig);
sig = BMoog.ar(sig, Lag.kr(lpp, lag).midicps, res, 0);
sig = sig * EnvGen.kr(Env.asr(lag, 1, lag), gate, doneAction:2);
sig = SelectX.ar(wet, [input, sig]);
sig = input0 * input;
Out.ar([0, 1], sig * Lag.kr(amp, 1));
}.play 
)

 
 
b.autogui