
(
fork {
    ~envsource = ();
    ~envsource.bus = Bus.audio;
    s.sync;
    
    ~envsource.synt = { |smooth_ratio=0.5|
        var doit = MouseY.kr > 0.5;
        var in = SoundIn.ar(0);
        var chain = FFT( LocalBuf(8192), in, 0.25, 1);
        var frozen = chain
                     | PV_Freeze( _, doit ) 
                     | PV_MagSmooth( _, smooth_ratio )
                     | IFFT( _ )
                     | (_ ! 2 * 0.5) 
                     ;
        Select.ar(doit, [in!2, frozen]) | Out(~envsource.bus);
    }.play;
    
    m = MidiPixelation();
    ~soundin = ();
    ~soundin.bus = Bus.audio(s);
    s.sync;
    
    ~soundin.synt = {
        SoundIn.ar(0) | Out.ar(~soundin.bus, _)
    }.play;
    
    ~soundin.buf = Buffer.read(s, "/Users/edu/Desktop/voiceover2.aif");
    s.sync;
    ~soundin.synt = {
        PlayBuf.ar(1, ~soundin.buf, BufRateScale.kr(~soundin.buf), loop:1) | Out.ar(~soundin.bus, _)
    }.play
    m.play(~soundin.bus);
}
)
~soundin.synt.free
m.free
m.analyze_env(~envsource.bus)
~soundin.bus.index
Synth.browse
m.midi_gain = 1
m.analyze_env_stop
Synth.tail
b = Bus.audio
a = {SinOsc.ar(440) | Out.ar(b, _)}.play.tail
c = { In.ar(b) | Out.ar(0, _)}.play.tail
s.queryAllNodes
c.free
m.synth_sender['trig_rate'] = 0.75
m.dur = 0.2
PV_Freeze
1.dup
1!2
m.synth_sender
~soundin.synt.free

~filter = ReEquestri2.midi_scale_attractor(scale:"A D# E Bb G#")
s.plotTree
~filter.value([1,2,3,4], [2,3,4,5])
f = "1+1".asString.compile

Synth.browse
Server.killAll
Env.fromxy([0, 1, 2, 3], [1, 1, 0.5, 0], [0, 2, -2]).plot

m.post_funcs.pop; m.register_post_processing(~filter)
m.synth_sender['trig_rate'] = 1
m.synth_sender['floor_amp'] = 0.01
m.synth_sender['ceil_amp'] = 0.5
m.synth_sender['clip_min'] = 0.001
m.synth_sender['map_min'] = 0.0
m.synth_sender['clip_max']  = 1
m.synth_sender['map_max'] = 1
m.synth_sender.dump
m.class.piano_freqs
m.post_funcs.pop; m.register_post_processing(~f2)
m.post_funcs.pop; m.register_post_processing(~f3)
m.post_funcs.pop; m.register_post_processing(~f4)
m.post_funcs.pop; m.register_post_processing(~f5)
m.post_funcs.pop; m.register_post_processing(~f6)
m.post_funcs.pop; m.register_post_processing(~f7)

m.post_funcs.pop

[1,2,3,4].copy

FreqQuantization.scale("A B C D E").collect {|f| f.f2n}
min(4, 5)
~f2 = ReEquestri2.midi_scale_attractor("A B C D E")
~f3 = ReEquestri2.midi_scale_attractor("A D E F# G# A#")
~f4 = ReEquestri2.midi_scale_attractor("A Bb E D# G# B")
~f5 = ReEquestri2.midi_scale_attractor("A Bb B G# C#")
~f6 = ReEquestri2.midi_scale_attractor("A G# Bb")
~f7 = ReEquestri2.midi_scale_attractor("C Eb G Ab")
~f6 = {|ms, amps|
    [ms, amps.clip(a[0], a[1]).linlin(a[1], a[2], a[3], a[4])];
}
~f7 = ReEquestri2.midi_contrast(0.2, 1, 0, 1);
~f8 = Proto {
    ~gate = 0.1
    ~min0 = 0.1;
    ~min1 = 0.02;
    ~max0 = 1;
    ~max1 = 0.5;
    ~next = {|notes, amps|
        amps.size.do {|i|
            amps[i] = amps[i] * (amp )
            var amp = amps[i];
            if( amp < ~gate ) {
                amp = 0
            } /*else*/ { 
                
            };
            
            
        }
        
    }
}

~f7.min0
~f7.max0
~f7.min1
~f7.max1
~f7.min0 = 0.3
~f7.max0 = 1
~f7.max1 = 0.8
~f7.min1 = 0.0
m.sort
m
a = [0.0, 0.6, 0.2, 0.6, 0.05, 1]
a[0] = 0.1
a[1] = 0.2
a[3] = 0.5

(0,0.1..10).clip(2, 8).linlin(2, 8, 0, 10).postcs

~f2.(['E4'].collect(_.tomidi), [1])


'4C'.tomidi
'A4'.tofre



r = Proto({
    ~y = 3;
    ~method = {|x| x * ~y};
    ~value = { |x| x * ~y};
    ~next = {|x| x * ~y};
    
})

a = fork {
    loop {
        1.wait;
        r.(4).postln;
        
    }
}
a.stop
r.value(2)
Array.clip
Proto.value
~freqs = ("A0".tomidi.."C8".tomidi).collect(_.tofreq)
a = {
    var amps = AmpCompA.kr(~freqs);
    amps.poll(0.5);
}.play
~freqs.size

a = {
    var freqs = ()
}