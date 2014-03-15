(
a = { |in=0|
	var a0 = SoundIn.ar(in);
	var delaytimes = ~delaytimes; // (2..20) ** 1.5 * 4 / 1000;
	var distances = delaytimes * 340;
	var attenuations = 1 / distances;
	var n = delaytimes.size;
	var pan_positions = n.collect {|i| (i / n).linlin(0, 1, -1, 1+(1/n)) };
	var delays = DelayN.ar(a0, 1, delaytimes)
				 | Pan2.ar(_, pan_positions);
	
	
	delays
	* attenuations
	| Mix(_)
	| Out.ar(0, _)
}.play
	
)

~delaytimes = (2, 4..22) ** 2 * 2 / 1000;

Delay
((1..10) ** 1.5 * 4 / 1000 * 340).sum / 11

DelayC

1 / ((2..22) ** 2 * 0.8 / 1000 * 340);

2.sqrt

(2, 4..20)

(~delaytimes * 340).sum / 11
~n = ~delaytimes.size

~n.collect {|i| (i / ~n).linlin(0, 1, -1, 1+(1/~n))}