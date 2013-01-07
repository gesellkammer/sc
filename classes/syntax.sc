ETests {
    *syntax {
        assert { [10, 20] !< (_ + _) == 30 };
        assert { 10 !> (_ + 2) == 12 };
        assert { 10 !> [_, 20] == [10, 20] };
        assert { 10 !< [_, _] == [10, 10] };
        assert { [1, 2] !< [_, _] == [1, 2] };
        assert { 1 !> [_, _] <! 2 == [1, 2] };
        "all tests passed".postln;
    }
}

+ Object {

    => {|other, mod=\s|
        ^switch( mod,
    			\s,      { other.apply(this) },  // serial, the default
    			\serial, { other.apply(this) },
    			\p,   { [this, this => other] }, // parallel, carry the original
    			\par, { [this, this => other] },
        )
	}
    !> {|other, mod=\s|
	    ^switch( mod,
    			\s,      { other.apply(this) },  // serial, the default
    			\serial, { other.apply(this) },
    			\p,   { [this, this => other] }, // parallel, carry the original
    			\par, { [this, this => other] },
    	)
	}
	!< {|other|
	    ^other.applyOneToMany(this);
	}

    <! {|other|
        ^( other !> this );
    }

    >! {|other|
        ^( other !< this );
    }

    mix {
	    ^(Mix(this));
    }

	arity {
		^0
	}
	apply {|x|
		^this.value(x)
	}

	par {|other|
		^[this, other]
	}

}


+ SequenceableCollection {
	=> { |other|
		^other.apply(this)
	}
	!> { |other, which|
		var out;
		if( which.isNil ) {
			^other.apply(this)
		} {
			out = other.apply(this[which]);
			^(this.copy.slotPut(which, out))
		}
	}
	!< { |other| ^other.apply_distribute(*this) }

	apply_distribute{ |xs|
	    "error".postln
	}

	applyOneToMany{ |obj|
	    if( obj.isKindOf(SequenceableCollection) ){
	       ^this.apply_distribute(obj);
	    }{  // else
	       ^this.collect{ |x| x.apply(obj) };
	    };
	}
}

+BinaryOpFunction {
	arity {
		^(this.slotAt(this.slotIndex(\a)).arity +
		  this.slotAt(this.slotIndex(\b)).arity
		)
	}
}

+UnaryOpFunction {
	arity {
		^(this.slotAt(this.slotIndex(\a)).arity)
	}
}

+ Function {
	arity {
		^this.def.argNames.size
	}
	doesChannelExpansion {
		if( this.def.selectors.isNil, {^false});
		^this.def.selectors.any
			{|x| x.asClass.superclasses.includes(UGen)}
	}
}

+ AbstractFunction {
    apply {|obj|
        switch( this.arity,
            1, { ^(this.value(obj)) },
            2, { ^{|a| this.value(obj, a)} },
            3, { ^{|a, b| this.value(obj, a, b)} },
            4, { ^{|a, b, c| this.value(obj, a, b, c)} },
            5, { ^{|a, b, c, d| this.value(obj, a, b, c, d)} },
        );
        ^{ |...args| this.values(*([obj] ++ args)) };
    }

	apply_distribute {|...seq|
		var tmp, arity;
		arity = this.arity;
		if( seq.size < arity ) {
			^{|...args| this.value(*(seq ++ args))}
		};
		if( seq.size == arity ) {
			^this.value(*seq);
		} /* else: seq.size > arity */ {
			// ^this.value(seq);
			^( [this.value(*seq[..arity])] ++ seq[arity..] );
		};
	}

	applyOneToMany { |obj|
	    ^this.value(*Array.fill(this.arity, obj));
	}
}

+ Mix {
    *apply {|array|
        ^Mix(array);

    }
}

+ Out {
    *new {|chan|
        if( chan.isKindOf(Bus) ) {
            if( chan.rate == 'audio' ) {
                ^Out.ar(chan.index, _);
            } /* else */ {
                ^Out.kr(chan.index, _);
            }
        } /* else */ {
            ^Out.ar(chan, _);
        }
    }
}

+ Symbol {
	ndef {|func|
		^Ndef(this, func)
	}
}

+ ListPattern {
	* apply {|seq|
        ^this.new(seq);
    }
}

// TODO: fix channel expansion in UGens