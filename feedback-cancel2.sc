(
/*
a = { |f0=50, f1=4, f2=4, a0=1, a2=0.3,  aten=0.1, outbus=1|
	var mic = SoundIn.ar(0) * aten;
	//var mic_amp = Amplitude.kr(mic);
	//var mic_pitch = Pitch.kr(mic);
	//var feed_trig = (mic_amp > -12.dbamp) * (mic_pitch > 3000) ;
	
	
	Impulse.ar(f0 + (SinOsc.ar(f1) * f1))
	* a0
	* (SinOsc.ar(f2) * a2 + 1) / 0.5
	+ mic
	| Out.ar(outbus, _)
}.play;
*/

a = {
    SoundIn.ar(1)
    => Out.ar(0, _)
}.play;

c = {|ampthresh= 0.1, pcile_fract=0.5|
    var a1 = In.ar(0);
    var amp = Amplitude.kr(a1, ampatt, amprel);
    var chain0 = FFT(LocalBuf(512), a1);
    var crest = chain0
                => FFTCrest.kr(_, 300, 10000)
                => _.poll(5, 'crest');
    chain0
    => SpecPcile(0, pcile_fract).poll(5, 'pcile');
    
    Out.kr(0, 0);
    /*  
                * (amp > ampthresh).lag(dt * 0.5);
    var is_feedback = (chain > mincrest).lag(dt).poll(5, "feedb");
    var pitch, haspitch, atenuated;
    #pitch, haspitch = Tartini.kr(a1);
    atenuated = BBandStop.ar(a1, pitch, bw) * pad;
    
    Select.ar(is_feedback, [a1, atenuated])
    => ReplaceOut.ar(0, _)
    */
}.play.after(a)

c.autogui

b = { | inbus=0, amp_thresh= -20, amp_lag=0.1, clipamp=1,
        freq_thresh=0, freq_lag=0.1, bw=0.1, mincrest=800.0  |
    var a0 = In.ar(inbus);
    var amp = Amplitude.kr(a0);
    var chain = FFT(LocalBuf(2048, 1), a0);
    var pitch, has_pitch, atenuated, is_howling, howling_rate;
    # pitch, has_pitch = Pitch.kr(a0);
    is_howling = (((amp > amp_thresh.dbamp).lag(amp_lag) * 
                     (pitch > freq_thresh).lag(freq_lag)) > 0.5);
    howling_rate = (amp / amp_thresh.dbamp) * (FFTCrest.kr(chain).poll(5, 'crest') < mincrest)
                   => _.linlin(0, 10, 0, clipamp)
                   => _.lag(0.3)
                   => _.clip(0, clipamp)
                   ;
    howling_rate.poll(5);
    atenuated = BBandStop.ar(a0, (pitch * has_pitch).clip(20, 20000), bw);
    // is_howling.poll(5);
    a0 = a0 * (1 - howling_rate);
    atenuated = atenuated * howling_rate ;
    // atenuated = 0;
    
    a0 + atenuated
    => ReplaceOut.ar(inbus, _)
    ;
    
    /*
    (a0 * (1 - howling_rate)) + (atenuated * howling_rate)
    | ReplaceOut.ar(inbus, _)
    ; 
    */
    
}.play.after(a);

)

a.autogui
b.autogui
a.free
b.free
after

10.linlin(0, 2, 0, 1)

// ----------------------
(
	a = { |thresh= -18, rq=0.05, lagt=0.5, damped=0.1, minfreq=350| 
		var a1 = SoundIn.ar(1);
		var pitch, haspitch;
		
		#pitch, haspitch = Tartini.kr(a1);
		a1 = SelectX.ar((haspitch * (pitch > minfreq) * (Amplitude.kr(a1) > thresh.dbamp)).lag(lagt), 
			// [a1, Notch.ar(a1, pitch, rq) => Notch.ar(_, pitch, rq) => (_ * damped)]);
			[a1, BBandStop.ar(a1, pitch, rq) => BBandStop.ar(_, pitch, rq) => (_ * damped)]);
		#pitch, haspitch = Tartini.kr(a1);
		a1 = SelectX.ar((haspitch * (pitch > minfreq) * (Amplitude.kr(a1) > thresh.dbamp)).lag(lagt), 
			// [a1, Notch.ar(a1, pitch, rq) => Notch.ar(_, pitch, rq) => (_ * damped)]);
			[a1, BBandStop.ar(a1, pitch, rq) => BBandStop.ar(_, pitch, rq) => (_ * damped)]);
    	#pitch, haspitch = Tartini.kr(a1);
    	a1 = SelectX.ar((haspitch * (pitch > minfreq) * (Amplitude.kr(a1) > thresh.dbamp)).lag(lagt), 
    		// [a1, Notch.ar(a1, pitch, rq) => Notch.ar(_, pitch, rq) => (_ * damped)]);
    		[a1, BBandStop.ar(a1, pitch, rq) => BBandStop.ar(_, pitch, rq) => (_ * damped)]);
		a1 
		=> Limiter.ar(_, 1, 0.01)
		=> Out.ar(0, _)
	}.play
)

Limiter
a.autogui
