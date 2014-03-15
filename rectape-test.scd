(
a = { |ampthreshU=0.01,lagU=0.05, lagD=0.02, recthresh=0.94, att=0.001, decay=0.01, sust=1, release=0.02, ampatt=0.01, amprel=0.005, ampdel=0.005, ampthreshD=0.01|
	var a0 = SoundIn.ar(0);
	var clock = Impulse.kr(30);
	// var amp = RunningSum.rms(a0, rmssamples);
	var amp = Amplitude.ar(a0, ampatt, amprel);
	var bowovermicU = (amp > ampthreshU)
	                 => LagUD.ar(_, lagU, lagD)
	                 ;
	var bowovermicD = (amp < ampthreshD) * (bowovermicU < 0)
	                 => LagUD.ar(_, lagU, lagD)
	                 ;
	var bowgate = bowovermicU * (1 - bowovermicD);
	var gateenv = EnvGen.ar(Env.adsr(att, decay, sust, release), bowgate);
	
	var isrecording = gateenv;
	amp => SendReply.kr(clock, '/amp', _);
	isrecording => SendReply.kr(clock, '/isrecording', _);
	
	bowgate.poll(10);
	
	Out.ar(0, 0);
	
}.play
)

(

~win = FlowView(bounds:Rect(300, 300, 300, 400));
~paths = ["/amp", "/isrecording"];
~widgets = ~paths.collect {|label|
    var text = EZText(~win, label: label, initVal:0);
    OSCFunc( {|msg| 
        defer { text.value = msg[3] };
    }, label);
};
)

a.autogui 
EnvGen
