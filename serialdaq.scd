(
/* ---------------------------------------------
                 BEFORE BOOT
   ---------------------------------------------
*/
s.options.blockSize=16;
)

(
/* ----------------------------------------------
                   SETUP
   ----------------------------------------------
*/
if( ~dev.notNil ) {
    ~dev.close
};
~dev = SerialPort("/dev/tty.usb*".pathMatch[0], baudrate: 230400, crtscts: true);
~numchannels = 2;
~lightbus = Bus.control(s, ~numchannels);
)

(
/* ----------------------------------------------
                    RUN
   ----------------------------------------------
*/
var lightbus = ~lightbus;
var numchannels = ~numchannels;
fork {
    /* .......................................
                   SYNTH
       .......................................
    */
    a = {|freq=440, lag_ms=1, min0=0, max0=1, min1=0, max1=1, curve=1|
        var val0 = In.kr(lightbus.index).linlin(min0, max0, 0, 1) ** curve;
        var val1 = In.kr(lightbus.index + 1).linlin(min0, max0, 0, 1) ** curve;

        var source = SinOsc.ar(freq);

        var a0 = source * val1.lag(lag_ms/1000);
        var a1 = source * val1.lag(lag_ms/1000);

        [a0, a1] !> Out.ar(0, _);

        // SendReply.kr(Impulse.kr(20), '/light', [val0, val1]);
        val0.oscprint(\val0, fader:true);
        val1.oscprint(\val1, fader:true);
    }.play;

    s.sync; // --------------------------------------------

    r = fork {
        var dev = ~dev;
        var msg;
        var values = [0.0, 0.0];

        loop {
            msg = dev.read;
            if( (msg & 2r10000000) > 0) {
                numchannels.do {|ch|
                    values[ch] = (dev.read*128 + dev.read) / 1023.0;
                };
                lightbus.setnSynchronous(values);
            }
        }
    }.autostop;

    a.ccbind(BCF.fader1, \freq, _.linlin(0, 127, 40, 2000));

    /*
    OSCFunc({|value|
        value = value[3];

        defer{ ~slider.value = value };
    }, '/light/amp');
    */

}
)

// - - - - - - - - - - - - - - - -- - - - - - - - - - -
// - - - - - - - - - - - - - - - -- - - - - - - - - - -
// - - - - - - - - - - - - - - - -- - - - - - - - - - -

a.autogui

a.set(\lag_ms, 4)

~lightbus.value = 0.5

v = FloatArray.newClear(2)
v[0]

(
r.stop
)

thisProcess.st