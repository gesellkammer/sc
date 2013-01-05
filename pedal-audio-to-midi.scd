(
    MIDIClient.init;
    m = MIDIOut(0);
    m.latency = 0;
    ~pedalvalue = 0;
)

(
a = { |minamp= -94, maxamp= -16, channel=3, trigrate=20, bw=0.5, freq=10000, rms_numsamps=4400|
	var pedin = SoundIn.ar(channel) !> BBandPass.ar(_, freq, bw);
	// var amp = Amplitude.kr(pedin, ampatt, amprel).ampdb; // .ampdb.poll(10, "amp");
	var amp = RunningSum.rms(pedin, rms_numsamps).ampdb;
	var ampdebug = amp.poll(10, "amp");
	// var relamp = amp.linlin(minamp, maxamp, 0, 1); // .poll(10, "relamp");
	var relamp = ((amp - minamp) / (maxamp - minamp)).clip(0, 1);
	var relamp_debug = relamp.poll(10, "relamp");
	// var relampdebug = relamp.poll(10);
	var transform = InterplPairs( 
	    [[0, 0], 
	     [0.7, 64],
	     [1, 127]
	    ], \linear )  
	    !> IEnvGen.kr(_,  relamp) 
	    !> (_.floor); 
	    
	var transform_debug = transform.poll(10, "transform");
	var trig = Impulse.kr(trigrate);
	// var trig = Changed.kr(transform, 0.01);
	// var trig = Impulse.kr(20);
	// trig.poll(10, "trig");
	// var trigdebug = Poll.kr(trig, relamp, "sending!");
    SendReply.kr(trig, "/pedal", transform);
	
	SinOsc.ar(freq) !> Out.ar(channel, _);
	
	Out.kr(0, 0);
	
}.play;

b = OSCFunc(path:"/pedal", func: {|msg|
    var value = msg[3];
    if( (value - ~pedalvalue).abs >= 1 ) {
        m.control(0, 101, value);
        ~pedalvalue = value;
    };
})
)

a.set(\t_getvalues, 1);
-28.linlin(-98, -25, 0, 1)
a.autogui

10.4.asInteger

GUI.cocoa