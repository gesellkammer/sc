(
b = Buffer.read(s, "<<<<<<<<<<<<< PATH >>>>>>>>>>>>>>>");
)

(
~mono = {|bufnum, rate=1|

	PlayBuf.ar(1, bufnum, rate*BufRateScale.kr(bufnum), loop:1)
	!> Out.ar([0, 1], _);

}.play(args:[bufnum: b]);
)

~mono.autogui;

b.free;

