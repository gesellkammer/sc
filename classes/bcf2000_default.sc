BCF {
    classvar <p_faders_cc;
    classvar <p_faders_ch;
    classvar <p_knobs_cc;
    classvar <p_knobs_ch;
    classvar <p_buttons_cc;
    classvar <p_buttons_ch;
    classvar <p_cc_flat;
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
        }
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
        ^[p_knobs_ch[preset-1][group-1][num-1], p_knobs_cc[preset-1][group-1][num-1]];
    }
    *button {|num, group=1, preset=1, row=\lower|
        if( row==\upper ) { num = num + 8; };
        num = num.clip(1, 16);
        ^[p_buttons_ch[preset-1][group-1][num-1], p_buttons_cc[preset-1][group-1][num-1]];
    }
    *button_cc {|num, group=1, preset=1, row=\lower|
        ^BCF.button(num, group, preset, row)[1];
    }
    *button_chan {|num, group=1, preset=1, row=\lower|
        ^BCF.button(num, group, preset, row)[0];
    }
    *first_cc {
	    ^this.fd(1);
    }
}
