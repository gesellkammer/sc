(
f = {|f0=1000, f1=100, amp0=1, amp1=0.3|
    var sig = Saw.ar(f0);
    var mod = SinOsc.ar(f1);
    var res = sig*mod;
    var pitch_detected, pitch_freq;
    #pitch_freq, pitch_detected =
    (
        res
        !> BLowPass4.ar(_, 800, 0.1)
        !> Amplitude.ar(_, attackTime:0.002, releaseTime:0.002)
        !> LeakDC.ar(_)
        !> Tartini.kr(_)
    );
    (pitch_freq * 0.5).poll;

}.play;
)

f.autogui


v = [1.2, 3.4, 5.6, 7.8, 9.0, 10.1, 11.2, 13.3, 14.0, 15.0, 16.0]
t = FloatArray.newFrom(v)

bench { 1000.do{t+t} }
bench { 1000.do{v+v} }