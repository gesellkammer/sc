(
// open port
m = MIDIOut.newMatchingRegex("Pure Data");
)

(
// send CC
m.control(0, 10, 120);
)
