(
fork {
    a = {|ff=10, fb=10, gfb=0.0, g0=0.9, gff=0.5|
        var a0 = SoundIn.ar(0) + (LocalIn.ar(1) * gfb);
        var a1 = DelayC.ar(a0, 1, fb/1000);
        var a2 = a0 * g0 + a1;
        var a3 = DelayC.ar(a0, 1, ff/1000);
        var a4 = a0 + (a3 * gff);
        var a5 = DelayC.ar(a4, 1, ff/1000);
        var a6 = a4 + (a5 * gff);
        var out = a6;

        LocalOut.ar(a2);
        Out.ar([0, 1], out);
    }.play;

    defer { a.autogui };
}
)


