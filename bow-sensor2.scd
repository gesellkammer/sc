(
    ~bowlength = 50;
    ~buf = Buffer.read(s, "/Users/edu/Desktop/voiceover20-192.aif");
    ~speedbpf_numpoints = 100000;
    
    ~speedbpf = Env.bpflinear(
        // speed, lagtime
        0, 0.5, 
        0.001, 0.5, 
        3, 0.4, 
        6, 0.15, 
        12, 0.09,
        20, 0.06
    );
    ~speedbpf.asBuffer(~speedbpf_numpoints, {|buf| ~speedbuf=buf});
    
    ~bowlinearbpf = Env.bpflinear(
        // x_rms_norm, cm
        0, 0,
        0.5, 25,
        1, 50,
    );
    ~bowlinearbpf.asBuffer(~speedbpf_numpoints, {|buf| ~bowlinearbuf = buf});
    
    ~pressbpf = Env.bpflinear(
        // press, press
        0, 0,
        0.01, 0.01,
        0.5, 0.9,
        1, 1
    );
    ~pressbpf.asBuffer(~speedbpf_numpoints, {|buf| ~pressbuf = buf});
)

(
// SAFFIRE 40:
// Knob 3: 2.5  | Knob 4: 7.5
a = { | o_x=2, o_y=3, i_x=2, i_y=3, o_speak=0, 
        x_scale_min=0.002, x_scale_max=0.2037, 
        y_floor=0.8056, 
        x_avg_samples=1100, y_avg_samples=900, 
        x_discard=0.7, speed_delta_t=0.010, minspeed=0.17, maxspeed=200, 
        lagvalidU=0.07, 
        lagvalidD=0.07,
        minspU=0.03, minspD=0.03,
        y_min = 0.018, ygateU=0.35, ygateD=0.006,
        sigfreq=12000, sigbw=10|
       
	var x_signal_out = SinOsc.ar(sigfreq) => Out.ar(o_x, _);
	var y_signal_out = SinOsc.ar(sigfreq) => Out.ar(o_y, _);
	var x_signal_in = SoundIn.ar(i_x) => BBandPass.ar(_, sigfreq, sigbw);
	var y_signal_in = SoundIn.ar(i_y);
	var sendclock = Impulse.kr(10);
    
	var x_rms = RunningSum.rms(x_signal_in, x_avg_samples);
	var y_rms = RunningSum.rms(y_signal_in, y_avg_samples);
	
	var x_rms_norm = x_rms.linlin(x_scale_min, x_scale_max, 0, 1);
	var x_rms_linear = x_rms_norm * BufFrames.kr(~bowlinearbuf)
	                   => BufRd.ar(1, ~bowlinearbuf, _, loop:0, interpolation:2)
	                   => (_ / ~bowlength);
	var y_rms_norm = (1 - y_rms.linlin(0, y_floor, 0, 1));
	var y_rms_linear = y_rms_norm * BufFrames.kr(~bowlinearbuf)
	                   => BufRd.ar(1, ~pressbuf, _, loop:0, interpolation:2);

	var x_cm_for_speed = x_rms_linear => Lag2.ar(_, 0.07) * ~bowlength;
	var x_speed = (x_cm_for_speed - DelayN.ar(x_cm_for_speed, maxdelaytime:0.2, delaytime:speed_delta_t)).abs / speed_delta_t;
	var lagtime = (x_speed / ~speedbpf.duration) * BufFrames.kr(~speedbuf)
	              => BufRd.ar(1, ~speedbuf, _, loop:0, interpolation:4);
	var x_pos_norm = x_rms_linear
	                 => Lag2.ar(_, lagtime);
	var x_cm = x_pos_norm * ~bowlength;
	
	var valid_x =   (x_rms > x_scale_min) 
	              * (x_rms < x_scale_max) 
    	          * ((x_speed > minspeed) => LagUD.ar(_, minspU, minspD))
    	          => Lag2UD.ar(_, lagvalidU, lagvalidD);
    	          
    var y_gate = (y_rms_linear > y_min) 
                 => Lag2UD.ar(_, ygateU, ygateD);
    	          
	var tape = BufRd.ar(1, ~buf, x_pos_norm * BufFrames.kr(~buf), loop:0, interpolation:4) 
	           * valid_x
	           * y_gate;
	       
	(   'xpos'     : x_rms,
	    'xposnorm' : x_rms_norm,
	    'y_linear'   : y_rms_linear,
	    'ygate'    : y_gate,
	    "y_rms"    : y_rms
	).keysValuesDo 
	    {|path, value| SendReply.kr(sendclock, "/" ++ path, value) };
	/*
	x_rms      => SendReply.kr(sendclock, '/xpos', _);
	x_rms_norm => SendReply.kr(sendclock, '/xposnorm', _);
	x_cm       => SendReply.kr(sendclock, '/xposcm', _);
	x_speed    => SendReply.kr(sendclock, '/speed', _);
	lagtime    => SendReply.kr(sendclock, '/lagtime', _);
	*/
	
	// y_rms_norm.poll(10, 'Y');
	
	// out
	Out.ar([o_speak, o_speak+1], tape);
}.play
)

(
~win = FlowView(bounds:Rect(300, 300, 300, 400));
~paths = ["y_linear", "ygate", "y_rms"];
~widgets = ~paths.collect {|label|
    var text = EZText(~win, label: label, initVal:0);
    OSCFunc( {|msg|
        defer { text.value = msg[3] };
    }, "/" ++ label);
};
)

a.autogui;

// ------------------------------------------------------------

(
    ~buf.free;
    ~speedbuf.free;
    ~bowlinearbuf.free;
    ~pressbuf.free;
  
)

// -------------------------

b = { |speed|
	BufRd.kr(1, ~speedbuf, (speed / ~speedbuf.duration) * BufFrames.kr(~speedbuf), loop:0, interpolation:2).poll(5)
}.play
1+1
b.set(\speed, )

~speedbpf.plot