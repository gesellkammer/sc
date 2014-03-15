(
a = { |out=0, mf=2|
	SinOsc.ar(400)
	| Pan2.ar(_, LFCub.ar(mf))
	| Out.ar(out, _)
}.play;

b = {|out=0, in=0, t=0.01|
	var a0 = In.ar(in);
	var a1 = In.ar(in + 1);
	var highest = (a0.abs > a1.abs).lag(t);
	var mix = (a0 * highest) + (a1 * (1 - highest));
	ReplaceOut.ar(out, mix ! 2);
}.play.after(a)

c = {|out=0, in=0, atime=0.001, rtime=0.002, lagt=0|
	var a0 = In.ar(in);
	var a1 = In.ar(in + 1);
	var amp0 = AmplitudeMod.kr(a0, atime, rtime);
	var amp1 = AmplitudeMod.kr(a1, atime, rtime);
	var highest = (amp0 > amp1).lag(lagt);
	var mix = (a0 * highest) + (a1 * (1 - highest));
	ReplaceOut.ar(out, mix ! 2);
}.play.after(a)

d = {|out=0, in=0, t=0|
	var a0 = In.ar(in);
	var a1 = In.ar(in+1);
	var b0 = Balance.ar(a0, a1);
	var b1 = Balance.ar(a1, a0);
	var highest = (b0.abs > b1.abs).lag(t);
	var mix = (a0 * highest) + (a1 * (1 - highest));
	ReplaceOut.ar(out, mix ! 2);
}.play.after(a)
)

b.free
c.free
a.free
c.autogui
b.autogui
a.autogui
d.autogui
Pan2