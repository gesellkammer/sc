+ Object {
	| {|other|
		^other.apply(this);
	}
	|< {|other|
	    ^other.applyOneToMany(this);
	}
	
	arity {
		^0
	}
	apply {|other|
		^this.value(other)
	}
	
	!! {|other|
	    ^[this, other]
	}

	
}   

                                       

+ SequenceableCollection {
	| { |other|
		^other.apply(*this)
	}
	applyOneToMany{ |obj| 
	    ^this.collect{ |x| x.apply(obj) }
	}
	!! {|other|
	    ^this.add(other)
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
	apply {|...seq|
		var tmp, arity;
		arity = this.arity;
		if( seq.size < arity ) {
			^{|...args| this.value(*(seq ++ args))}
		};
		if( seq.size == arity ) {
			^this.value(*seq);
		} /* else: seq.size > arity */ {
			^this.value(seq);
		};
	}
	
	applyOneToMany { |obj|
	    ^this.value(*Array.fill(this.arity, obj));
	}
	
	<< {|other|
	    ^(other | this);
	}
}


+ Synth {
    put {|other, val|
        this.set(other, val)
    }
}


// TODO: fix channel expansion in UGens