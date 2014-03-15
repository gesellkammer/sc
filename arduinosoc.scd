
~slider = EZSlider();
~ampbus = Bus.control(s);
~buflen = 256;
~bufs = [Buffer.alloc(s, ~buflen/2), Buffer.alloc(s, ~buflen/2)];
~bufs.do {|buf| buf.fill(0, ~buflen/2, 0)};
~currbuf = 0;

(
fork {

    b = { |buf, bufdur=0.025, t_buf=0, curve=1.5, lagt_ms=20|
        var rate = 1/(bufdur);
        var ramp = Phasor.ar(t_buf, rate/SampleRate.ir, 0, ~buflen/2);
        var amp = BufRd.ar(1, buf, ramp, 0, BufFrames.kr(buf), interpolation:2, loop:0)**curve
            !> Lag2.ar(_, lagt_ms/1000.0);
        Out.kr(~ampbus, A2K.kr(amp));


        Out.ar(10, amp);
        SinOsc.ar(440) * amp !> Out.ar(0, _);
    }.play;

    s.sync;

    ~osc = OSCFunc(path:"/snr/light", func:{|msg, time, addr, recvPort|
        var millis = msg[1];
        var dt = millis/1000;
        var currbuf = ~currbuf;
        var oldbuf, newbuf;
        oldbuf = ~bufs[currbuf];
        currbuf = currbuf+1 % 2;
        newbuf = ~bufs[currbuf];


        b.set(\buf, newbuf.bufnum);
        b.set(\t_buf, 1);
        b.set(\bufdur, millis/1000.0);
        oldbuf.setn(0, msg[2..] / 1023.0);

        ~currbuf = currbuf;


        //amp.postln;


    });

    ~gui = fork {
        loop {
            defer { ~slider.value = ~ampbus.getSynchronous; };
            0.1.wait;
        }
    }

    defer{ b.autogui};

}
)

(
c = {
    Phasor.kr(0, 1/ControlRate.ir, 0, 1000).poll(30);
    Out.kr(0, 0);
}.play
)

BufRateScale
Phasor



~ampbus.getSynchronous