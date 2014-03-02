(
g = EZSlider();
a = NetAddr("10.0.0.2", 11111);
//a = NetAddr("localhost", 11111);
g.action = {|sl|
	a.sendMsg("/set", sl.value.pow(2)*1000);
};
)
