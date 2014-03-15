(
var name, func, specs, componentType, componentSubtype, builder, plugins;

name = "bassboost";

func = { |gain=5, srcgain=2, lagt=0.15, ampthresh= -36, pitchthresh=55, knee=4, ampknee=3, noisegate= -40, limamp=0, limt=0.01|
	var a0 = AudioIn.ar(1);
	var a1 = AudioIn.ar(2);
	var amono = (a0 + a1) * (0.5 * srcgain);
	var pitch, haspitch, needsgain, pitchgain, ampgain, gain2, gate, outL, outR;
	var amp = Amplitude.kr(amono);
	var freqthresh = pitchthresh.midicps;
	#pitch, haspitch = Tartini.kr(amono);
	haspitch = haspitch.lincurve(0, 1, 0, 1, 3);
	pitchgain = ((pitchthresh - pitch.cpsmidi * haspitch) / knee).linlin(0, 1, 1, gain);
	ampgain = ((amp - ampthresh).ampdb / ampknee).linlin(0, 1, 0, 1);
	gate = (amp > noisegate.dbamp).lag(lagt);
	gain2 = pitchgain * ampgain * AmpCompA.kr(pitch.lag(lagt));
	outL = LinSelectX.ar(gate, [a0, a0*gain2]) => Limiter.ar(_, limamp.dbamp, limt);
	outR = LinSelectX.ar(gate, [a1, a1*gain2]) => Limiter.ar(_, limamp.dbamp, limt);
	Out.ar(0, [outL, outR]);

};
 
specs = #[
    [1, 20, \Linear, 5, \Generic], // gain
    [1, 20, \Linear, 2, \Generic], // srcgain
    [0, 1, \Linear, 0.15, \Generic], //lagt
    [-60, 0, \Linear, -36, \Generic], //ampthresh
    [20, 80, \Linear, 55, \Generic], //pitchthresh
    [0, 24, \Linear, 4, \Generic], //knee
    [0, 24, \Linear, 3, \Generic], //ampknee
    [-60, 0, \Linear, -40, \Generic], //noisegate
    [-24, 6, \Linear, 0, \Generic], //limamp
    [0, 1, \Linear, 0.01, \Generic] //limt
];

componentType = \aufx;
componentSubtype = \XBBT;
// func.asSynthDef(\test).children
builder = AudioUnitBuilder.new(name, componentSubtype, func, specs, componentType);
~plugins = builder.getPlugins(func).postln;
builder.makeInstall;
~plugins.do {|p| "cp \"%\" ~/Library/Audio/Plug-Ins/Components/%.component/Contents/Resources/plugins".format(p, name).postln.unixCmd }

)
