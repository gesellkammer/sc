
(
fork {
 	
 	b = OSCresponderNode(nil, '/reply', {|time, resp, msg| 
			("REPLY: " ++ msg[1]).postln}).add;
	d = OSCresponderNode(nil, '/debug', {|time, resp, msg| 
			('DEBUG: ' ++ msg[1]).postln}).add;
	a = NetAddr("127.0.0.1", 57130);
	MIDIClient.init;
	~midiout = MIDIOut(0);
 	
 	~s1 = ();
	~s1.bus = Bus.audio;
	
	// here specify the audio file to analyze. it should be a mono file
	~s1.buf = Buffer.read(s,"/Users/edu/proj/flauta/timeline3.wav" );
	s.sync;
	
	// this responder stops the recording after the analysis stopped
	~responder.remove; 
	~responder = OSCresponderNode(nil, '/tr', {|t, r, m|
		var id = m[1];
		fork {
			0.2.wait;
			~midiout.mmcStop;
		}
	}).add;

	// this synthdef just plays the buffer and sends a trigger when finished
	// actually, it stops 0.1 secs before, you can change this in the line
	// marker with // <== 
	SynthDef(\playbufandstop, {|buf, bus, bufamp=1|
   		var a1 = PlayBuf.ar(1, buf, BufRateScale.kr(buf), doneAction:0, loop:0) * bufamp;
   		var turnoff_synth = Line.kr(0, 1, BufDur.ir(buf) + 0.1, doneAction:2); 
   		var sendtrig = 
   			Line.ar(-1, 1, (BufDur.ir(buf) * 2)) // <== it will cross the 0 as the playback is finished
   			| Trig.kr(_) 
   			| SendTrig.kr(_, 1);
   		
   		
    	a1 | Out.ar(bus, _);   
    }).add; 
    s.sync;
	
	// midichan determines the default midichannel to send the
	// outcoming midi. If amp_to_channel is 1, the midichannel value
	// is not honoured and the midi notes are sent to a different
	// channel according to their dynamic
	// >> the softest ones are sent to channel 0, and the channel is higher
	// as the notes get louder. The number of channels and the shape
	// in which the channels are assigned to the dynamics are defined in the
	// midiserver in _amp_to_channel_exp and _amp_to_channel_max_channels
	// these can be changed in by calling MidiPixelation.
	~m1 = MidiPixelation4(midichan:1, amp_to_channel:0);  
	s.sync;
	

}
)

(	// Set up convertion parameters
// ~m1.conn_send('newpost', 1, 'speedlim', "speedlim=0.08") 
// ~m1.conn_send('newpost', 2, 'loudest', 10);
// ~m1.conn_send('

// ~m1.conn_send('amp2chan/status', 1)       // << turn on amp to chan
// ~m1.conn_send('amp2chan/exp', 2)          // << set the exponent of the shape (2 is the default)
// ~m1.conn_send('amp2chan/maxchannels', 8)  // << set the exponent of the shape (2 is the default)
)

(		// evaluate this to start recording midi and playback. when the~midiout.mmcRec;
    	~s1.syn = Synth(\playbufandstop, args:[\buf, ~s1.buf, \bufamp, 2, \loop, 0]); 
    	~m1.play(~s1.bus);
 
)
   
~s1.syn.free   
Server.killAll

~m1.pause

// "/Users/edu/Library/Application Support/SuperCollider/midipixelation-control.rtf"
j = { PlayBuf.ar(1, ~s1.buf, BufRateScale.kr(~s1.buf), doneAction:2) | Out.ar(0, _)}.play
j.free


