BCF {
    /*

    Knob zones (rottary encoders)

           A or 1 | B or 2
         ---------+---------
           C or 3 | D or 4

    */

    classvar <p_faders_cc;
    classvar <p_faders_ch;
    classvar <p_knobs_cc;
    classvar <p_knobs_ch;
    classvar <p_buttons_cc;
    classvar <p_buttons_ch;
    classvar <p_cc_flat;
    classvar <fader1, <fader2, <fader3, <fader4, <fader5, <fader6, <fader7, <fader8;
    classvar <knobA1, <knobA2, <knobA3, <knobA4, <knobA5, <knobA6, <knobA7, <knobA8;
    *new {
        ^super.new.init();
    }
    *initClass {
        p_faders_cc = [
            (81..88)
        ];
        p_faders_ch = [
            8.collect {1}
        ];
        p_knobs_cc = [
            [(1..8), (9..16), (17..24), (25..32)]
        ];
        p_knobs_ch = [
            [8.collect {1}, 8.collect{1}, 8.collect{1}, 8.collect{1}]
        ];
        p_buttons_cc = [
            [(65..72), (73..80)]
        ];
        p_buttons_ch = [
            [8.collect{1}, 8.collect{1}]
        ];
        p_cc_flat = [];
        1.do {|preset|
            p_cc_flat = p_cc_flat.addAll(p_faders_cc[preset]);
            p_cc_flat = p_cc_flat.addAll(p_knobs_cc[preset].flat);
        };
        fader1 = this.fader_cc(1);
        fader2 = this.fader_cc(2);
        fader3 = this.fader_cc(3);
        fader4 = this.fader_cc(4);
        fader5 = this.fader_cc(5);
        fader6 = this.fader_cc(6);
        fader7 = this.fader_cc(7);
        fader8 = this.fader_cc(8);
        knobA1 = this.knob_cc(1, $A);
        knobA2 = this.knob_cc(2, $A);
        knobA3 = this.knob_cc(3, $A);
        knobA4 = this.knob_cc(4, $A);
        knobA5 = this.knob_cc(5, $A);
        knobA6 = this.knob_cc(6, $A);
        knobA7 = this.knob_cc(7, $A);
        knobA8 = this.knob_cc(8, $A);
    }
    *at {|num|
        ^this.p_cc_flat[num];
    }
    *fader {|num, preset=1|
        ^[p_faders_ch[preset - 1][num - 1], p_faders_cc[preset-1][num-1]];
    }
    *fader_cc {|num, preset=1|
        ^BCF.fader(num, preset)[1];
    }
    *fader_chan {|num, preset=1|
        ^BCF.fader(num, preset)[0];
    }

    *knob {|num, group=1, preset=1|
        case
        { group.isKindOf(String) } { group = group[0].asInt - 64; }
        { group.isKindOf(Symbol) } { group = group.asString[0].asInt - 64; }
        { group.isKindOf(Char) }   { group = group.asInt - 64; };
        ^[p_knobs_ch[preset-1][group-1][num-1], p_knobs_cc[preset-1][group-1][num-1]];
    }
    *knob_cc {|num, group=1, preset=1|
        ^BCF.knob(num, group, preset)[1];
    }
    *knob_chan {|num, group=1, preset=1|
        ^BCF.knob(num, group, preset)[0];
    }
    *button {|num, row=1, preset=1|
        ^[p_buttons_ch[preset-1][row-1][num-1], p_buttons_cc[preset-1][row-1][num-1]];
    }
    *button_cc {|num, row=1, preset=1|
        ^BCF.button(num, row, preset)[1];
    }
    *button_chan {|num, row=1, preset=1|
        ^BCF.button(num, row, preset)[0];
    }
    *first_cc {
	    ^this.fd(1);
    }
}
