Keycode {
	classvar <codes;
	
	*initClass {
		codes = Dictionary[
			'a' -> 0,
			'b' -> 11,
			'c' -> 8,
			'd' -> 2,
			'e' -> 14,
			'f' -> 3,
			'g' -> 5,
			'h' -> 4,
			'i' -> 34,
			'j' -> 38,
			'k' -> 40,
			'l' -> 37,
			'm' -> 46,
			'n' -> 45,
			'o' -> 31,
			'p' -> 35,
			'q' -> 12,
			'r' -> 15,
			's' -> 1,
			't' -> 17,
			'u' -> 32,
			'v' -> 9,
			'w' -> 13,
			'x' -> 7,
			'y' -> 6,
			'z' -> 16,
			'space' -> 49,
			'1' -> 18,
			'2' -> 19,
			'3' -> 20,
			'4' -> 21,
			'5' -> 23,
			'6' -> 22,
			'7' -> 26,
			'8' -> 28,
			'9' -> 25,
			'0' -> 29,
			'left' -> 123,
			'right' -> 124,
			'up' -> 126,
			'down' -> 125,
			'enter' -> 36,
			'del' -> 51,
			'delete' -> 51,
			'backspace' -> 117,
			'+' -> 30,
			'plus' -> 30,
			'-' -> 44,
			'minus' -> 44,
			',' -> 43,
			'.' -> 47,
			'esc' -> 53,
			'f1' -> 122,
			'f2' -> 120,
			'f3' -> 99,
			'f4' -> 118,
			'f5' -> 96,
			'f6' -> 97,
			'f7' -> 98,
			'f8' -> 100,
			'f9' -> 101,
			'f10' -> 109,
			'f11' -> 103,
			'f12' -> 111,
			'#' -> 42,
			'tab' -> 48
				
			];
	}
	*at {|key|
		^codes.matchAt(key);
	}
}

+ Symbol {
	keycode {
		^Keycode.at(this.toLower);
	}
	
}

+ String {
	keycode {
		^this.asSymbol.keycode;
	}
	browse {
		var as_class = this.asSymbol.asClass;
		if( as_class.notNil,
			{ as_class.browse },
			{ this.interpret.class.browse });
	}
}

+ Char {
	keycode {
		^this.asSymbol.keycode;
	}
}
