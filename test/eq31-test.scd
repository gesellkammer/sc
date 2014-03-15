(
~crossover = { arg signal, cutoffs, n_filters=2, filterType=LPF;
    var bands = Array.new(cutoffs.size+1);
    cutoffs.do {|cutoff, i|
        var band = signal;
        n_filters.do {
            band = filterType.ar(band, cutoff);
        };
        bands.add(band);
        signal = signal - band;
    };
    bands.add(signal);
    bands;
}
)
(
~eq = { arg levels = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
    WhiteNoise.ar(0.1)
    !> ~crossover.(_, #[20, 25, 31.5, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12000, 16000, 20000])
    !> (_ * levels.dbamp)
    !> Mix
    !> Out(0);

}.play
)
/*
~eq31 = {|in, oscpath="/eq31/bands", gains=[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]|
    var freqs = #[20, 25, 31.5, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12000, 16000, 20000];
    var bandin = {|in|
        BLowPass.ar(
*/

MultiEQ


{ SinOsc.ar([400, 440]) * [0.5, 0.7] }.draw

a = { |a1, a2 |
    [a1, a2].poll(2);
    }.play

Multi
a.set(2, 0.4)

~eq.set

e = MultiEQ.new(2, \eq, 600, 0.7, 1.8, \loshelf, 200, 0.1, \eq, 11000, 1.5, 1.5);
m = MultiEQGUI()
Array.fill(31, 0).plot

(
n=31;
w = Window.new.front;
m = MultiSliderView(w,Rect(10,10,n*13+4,100)); //default thumbWidth is 13
m.value=Array.fill(n, {|v| 1}); // size is set automatically when you set the value
m.action = { arg q;
    ~eq.set(\levels, q.value.ampdb);
    q.value.postln;
};
)

/440