Priv_PitchConvertion {
    classvar <pitchclasses, <notes, <enharmonics, <>default_magnitude;
    *initClass {
        pitchclasses = IdentityDictionary [
            $C -> 0,
            $c -> 0,
            $D -> 2,
            $d -> 2,
            $E -> 4,
            $e -> 4,
            $F -> 5,
            $f -> 5,
            $G -> 7,
            $g -> 7,
            $A -> 9,
            $a -> 9,
            $B -> 11,
            $b -> 11,
            $H -> 11,
            $b -> 11
        ];
        notes = "C C# D D# E F F# G G# A A# B C".split($ );
        enharmonics = "C Db D Eb E F Gb G Ab A Bb B C".split($ );
        default_magnitude = \midi;
    }
    *midi_to_note {|midi|
        var int;
        var micro = midi % 1;
        var oct = ((midi / 12.0) - 1).asInteger;
        var ps = (midi % 12).asInteger;
        var cents = (micro * 100 + 0.5).asInteger;
        var out = case
            { cents == 0  }    { (oct.asString ++ notes[ps]) }
            { cents == 50 }    { 
                if( includes(#[1, 3, 6, 8, 10], ps) ) {
                    oct.asString ++ notes[ps+1] ++ '-';
                } {
                    oct.asString ++ notes[ps] ++ '+';
                };
            }
            { cents > 50 }  { 
                cents = cents - 100;
                ps = ps + 1;
                if( ps > 11 ) {
                    oct = oct + 1;
                };
                oct.asString ++ enharmonics[ps] ++ cents.asString;
            }
            { cents < 50 } { 
                oct.asString ++ notes[ps] ++ '+' ++ cents.asString;
            }
        ;
        ^out;
    }
    
    *note_to_midi {|note|
        var octave, micro, pc, alteration;
        var out = note.split($+);
        if( out.size == 1 ) {   // no + sign
            out = note.split($-);
            if( out.size == 1 ) {
                // no - sign also. no microtone
                micro = 0.0;
            } /* else */ {
                // negative cents
                if( out[1] == "" ) { 
                    micro = -0.5;
                } {
                    micro = out[1].asInteger / 100.0 * -1;
                };
            };     
        } /*else*/ { 
            // either + or +n 
            assert{ out.size == 2};
            if( out[1] == "" ) {
                micro = 0.5;
            } /* else */ {
                micro = out[1].asInteger / 100.0;
            };
        };
        out = out[0];
        if( out[0].isDecDigit ) { // octave number is at beginning
            octave = out[0].ascii - $0.ascii; // octave = out[0].asInteger

            pc = pitchclasses[out[1]];
            alteration = out.last.ascii;
        } /*else*/ {   // octave number is at the end
            octave = out.last.ascii - $0.ascii; // asInteger;
            pc = pitchclasses[out[0]];
            alteration = out[1].ascii;
        };
        case
            { alteration == 35 } 
                { pc = pc + 1 }
            { (alteration == 98) or: (alteration == 115) } 
                { pc = pc - 1 }
        ;
        case
            { pc > 11 }    
                { pc = 0;
                  octave = octave + 1;  
                 }
            { pc < 0 } {
                pc = 0;
                octave = octave - 1;
            }
        ;
    ^(( octave + 1) * 12 + pc + micro);         
    }
    *tonote {|val|
        if( default_magnitude == \midi ) {
            ^this.midi_to_note(val);
        } /*else*/ { 
            ^this.midi_to_note(val.tomidi);
        };
    }
}

MidiNote {
    var <value;
    *new {|val|
        if( val.isKindOf(String) ) {
            val = val.tomidi;
        };
        ^super.new.init(val);
    }
    init {|val|
        value = val;
    }
    asFloat { ^this.value; }
    asString { ^(this.value.asString + "->" + this.value.miditonote); }
    asSymbol { ^this.asString.asSymbol; }
    asInteger { ^this.asFloat.asInteger; }
    tomidi { ^this; }
    tofreq { ^this.value.tofreq; }
    tonote { ^this.value.tonote; }
    + { |other|
        ^this.class.new( this.value + other.asFloat; )
    }
    - { |other|
        ^this.class.new( this.value - other.asFloat; )            
    }
    * { |other|
        ^this.class.new( this.value.tofreq * other.asFloat | _.tomidi; )            
    }
    / { |other|
        ^this.class.new( this.value.tofreq / other.asFloat | _.tomidi; )            
    }
}