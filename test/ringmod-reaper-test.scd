Hilbert
(
Ndef(\ringmod, {|
    freq=20,
    sourcefreq=440,
    fb=0,       // feedback 0-1
    nlrel=0.3,  // non-linearities, relative, 0-1
    nlfeed=0.1  // max nonlinearity for feedback
    sim_r = 0.85,    // sim resistance
    mod_dist=0.0,
    mix=1,      // 0=dry, 1=wet
    gaindb=0,   // gain in dB
    diodemul=2,
    diodeadd= -0.20260
    mixdiode=0,
    mixmodos = 1,
    mixmulos=1,
    mixwave = 1,
    drywet = 1,
    modus = 1

    |
    var lagt = 0.02;
    // var nl_fb = ((2 * nlrel).rand - nlrel) * nlfeed;
    var nl_fb = ((2 * nlrel) * WhiteNoise.kr.abs - nlrel) * nlfeed;
    var sin0, fp0, fp1, sin_multi, s_out, sin, s_out_multi, in_multi, sin_multi_diode, sin_multi_dist, mod, s_out_simple, mod_os, mod_norm, out, compare;
    // var in = \in1.ar([0]);
    var in = SinOsc.ar(sourcefreq);
    // var in = SoundIn.ar(0);
    freq = freq.lag(lagt);
    mix = mix.lag(lagt);
    sin0 = SinOsc.ar(freq);
    sin_multi = UpSample.ar(sin0);
    sin_multi_diode = (sin_multi.abs * diodemul) + diodeadd;
    sin_multi_dist = LOSER_WaveShapeDist.ar(sin_multi, mod_dist);
    sin_multi = sin_multi_diode * mixdiode + (sin_multi_dist * ( 1- mixdiode));
    mod_os = DownSample.ar(sin_multi);
    mod_norm = ((sin0.abs * diodemul) + diodeadd) * mixdiode + (LOSER_WaveShapeDist.ar(sin0) * (1 - mixdiode));
    mod = SelectX.ar(mixmodos, [mod_norm, mod_os]);
    // m_out = DownSample.ar(sin);
    // fp0 = LocalIn.ar(1);
    // fp1 = (in + ((fb - nl_fb) * fp0)) * sin0 * sim_r;
    // LocalOut.ar(fp1);
    in_multi = UpSample.ar(in);
    // in_multi = in;
    s_out_multi = SelectX.ar(modus, [ FreqShift.ar(in_multi, freq) + FreqShift.ar(in_multi, freq.neg), (in_multi * mod)]); // + fp1;
    s_out = DownSample.ar(s_out_multi);
    s_out_simple = SelectX.ar(modus, [ FreqShift.ar(in, freq) + FreqShift.ar(in, freq.neg), (in * mod)]); // + fp1;in * mod;
    out = SelectX.ar(mixmulos, [s_out_simple, s_out]);
    out = SelectX.ar(mixwave, [in * sin0, out]);
    out = SelectX.ar(drywet, [in, out]);
    out ! 2;

}).play;

Delay1
// Ndef(\sin, { |freq=440| SinOsc.ar(freq) });

// Ndef(\ringmod) <<>.in1 Ndef(\sin);
/*
Ndef(\ringmod0, {|
    freq=20,
    sourcefreq=440|
    SinOsc.ar(sourcefreq) * SinOsc.ar(freq);
}).play
*/

)

s.makeGui
Ndef(\ringmod).set(\mixwave, 0)
Ndef.clear()
NdefMixer(s)

(
Ndef(\tst, {|freq=100| SinOsc.ar(freq) }).play;
Ndef(\d2, {|freq=110| SinOsc.ar(freq, 0.5)}).play;


w = FlowView();
[\tst, \d2].do {|label| Ndef(label).edit(parent:w)};

)

Server.killAll

Ndef(\ringtest, {|freqsource=440, freq=40, mix=1|
    var source = SinOsc.ar(freqsource);
    var hilb = RingModHilbert.ar(source, freq: freq);
    var ring = source * SinOsc.ar(freq);
    var mod = SelectX.ar(mix, [ring, hilb]);
    HPF.ar(mod, 10) !> LPF.ar(_, 20000);
}).play

s.makeGui