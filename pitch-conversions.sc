// Pitch conversions

+ Symbol {
    tomidi {
        ^this.asString.tomidi;
    }
    tofreq {
        ^this.asString.tofreq;
    }
}

+ String {
    tofreq {
        ^this.tomidi.tofreq;
    }
    tomidi { 
        ^Priv_PitchConvertion.note_to_midi(this);
    }
}

+ SimpleNumber {
    tomidi {
    	^this.cpsmidi;
    }
    tofreq {
        ^this.midicps;
    }
    freqtonote {
        ^this.tomidi.miditonote;
    }
    f2n {
    	// freq to note, shorthand -> should probably go somewhere else
    	^this.tomidi.miditonote;
    }
    miditonote {
        ^Priv_PitchConvertion.midi_to_note(this);
    }
    tonote {
    	// a number defaults to midi, so tonote == miditonote
        ^Priv_PitchConvertion.tonote(this);
    }
}