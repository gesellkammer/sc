


~dev = SerialPort("/dev/tty.usb*".pathMatch[0], baudrate: 115200, crtscts: true);
~slider = EZSlider();
~bufplot = ~buf.plot;

(
~numchannels = 2;
~chunksize = 64;
~channels = ~numchannels.collect { FloatArray.newClear(~chunksize) };
~buffer_chunks = 4;
~buflen = ~chunksize * ~buffer_chunks;
~buf.free; ~buf = Buffer.alloc(s, ~buflen);
~buf.fill(0, ~buflen, 0);
)

(
a = {|bufdur = 0.04, t_buf=1, buf, lag_ms=7, minamp=0, maxamp=1, curve=1|
    var rate = 1/(bufdur);
    var ramp = Phasor.ar(t_buf, rate, 0, BufFrames.kr(buf));
    var value = BufRd.ar(1, buf, ramp, loop:0, interpolation:1) !> Lag.ar(_, lag_ms/1000.0);
    var tr_startover = 0.00001 - ramp;
    var freq = 1000;
    var amp = value.linlin(minamp, maxamp, 0, 1);
    // BufWr.ar(1, buf, ramp - 1);
    Out.ar(20, value);


    // var amp = EnvGen.kr(Env.perc, tr_startover);
    //var amp = Trig1.ar(tr_startover, 0.005);


    SinOsc.ar(freq) * amp !> Out.ar(0, _);
}.play(args:[buf:~buf.bufnum])
)

(

r = Routine({
    var msg, numchannels, numbytes;
    var dev = ~dev;
    var t0, t1, t2;
    var bufchunk = 0;
    var buf = ~buf;
    var chunksize = ~chunksize;
    var buffer_chunks = ~buffer_chunks;
    var bufchunk_distance = buffer_chunks - 1;
    var bufchunk_to_update = bufchunk + bufchunk_distance;
    var channels = ~channels;
    var sync_counter = 0;
    var sync_threshold = 4;

    loop {
        msg = dev.read;
        if( (msg & 2r10000000) > 0) {
            numchannels = dev.read;
            chunksize = dev.read*128 + dev.read;
            numbytes = chunksize*2*numchannels;

            chunksize.do {|i|
                numchannels.do {|chan|
                    channels[chan][i] = dev.read * 128 + dev.read / 1023.0;
                };
            };
            buf.setn(bufchunk_to_update * chunksize, channels[0]);
            if( (bufchunk_to_update == (buffer_chunks - 1)) ) {
                sync_counter = sync_counter + 1 % sync_threshold;
                if (sync_counter == 0) {
                    a.set(\t_buf, 1);
                };
            };

            bufchunk = bufchunk + 1 % buffer_chunks;
            bufchunk_to_update = bufchunk_to_update + 1 % buffer_chunks;
        }

    }
}).play;

~gui = fork {
    loop {
        defer{
            ~slider.value = ~channels[0][0];
            ~bufplot.refresh;
        };
        ~buf.loadToFloatArray(action: {|arr|
            defer { ~bufplot.value_(arr) ; ~bufplot.specs = [[0, 1]]; }
        });
        0.1.wait;

    };
};

)

(
r.stop;
~gui.stop;

)

a.autogui
a
