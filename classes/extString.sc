+ String {
	getWordBoundsAt { |index|
		/* returns [start, end], which represent the positions in the string
		where the current word starts/ends */
		var end, str, max;
		var start = index;
		str = this;
		max = str.size;
		start = index ? max;
		// check if cursor is past last char
		if( start > max ) {
			start = max;
			str = str ++ " ";
		};
		if( str[start].isWordDelimiter ) {
			if( (start + 1 >= max or: str[start + 1].isWordDelimiter.not) and: {str[start - 1].isWordDelimiter.not}) {
				start = start - 1;
			};
			//^[start, start];
			
		};
		// find next word to the left if we are not on a word now
		while { (start > 0) and: str[start].isSpace } {
			start = start - 1;
		};
		end = start;
		// find left bound of current word
		while {(start >= 0) and: {str[start].isWordDelimiter.not}} {
			start = start - 1;
		};
		// right bound
		while { (end < max) and: {str[end].isWordDelimiter.not }} {
			end = end + 1;
		};
		start = start + 1;
		end = end - 1;
		if( start > end ) {
			end = start;
		};
		if( str[start..end].any(_.isWordDelimiter) ) {
			^[index, index-1];
		}
		^[start, end]
	}
	getWordAt{ |index|
		/* returns word at index */
		var start, end;
		#start, end = this.getWordBoundsAt(index);
		if( start > end ) {
			end = start
		};
		^this.copyRange(start, end);
	}
	getLineBoundsAt { |index|
		/* returns [start, end], which represent the positions in the string
		where the current line starts/ends */
		// a = "edu\nardo\nmogui";
		var end, str, max;
		var start = index;
		str = this;
		max = str.size;
		start = index ? max;
		// check if cursor is past last char
		if( start >= max ) {
			start = max - 1;
		};
		end = start;
		// find left bound of current line
		while { (start >= 0) and: {str[start] != $\n}  } {
			start = start - 1;
		};
		// right bound
		while { (end < max) and: {str[end] != $\n} } {
			end = end + 1;
		};
		start = start + 1;
		end = end - 1;
		if( start > end ) {
			end = start;
		};
		^[start, end];
	}
	getLineAt { |index|
		^this.copyRange(*this.getLineBoundsAt(index));
	}
	*test_getLineAt {
		assert{ 
			var str = "line1\nline2\nline3\nline4";
			str.getLineAt(6) == "line2";
		}
	}
	getPreviousChar{|pos|
		^this[this.indexOfPreviousChar(pos)];
	}
	indexOfPreviousChar{|pos|
		/* skip white space to the left and return the first char which
		is not whitespace
		*/
		var max = this.size;
		var index = pos ? max - 1;
		if( index >= max ) {
			index = max - 1;
		};
		while {(index > 0) and: {this[index].isSpace}} {
			index = index - 1;
		};
		^index;
	}	
	getPreviousPunct{|pos|
		/* returns the previous punctuation char or nil */
		^this[this.indexOfPreviousPunct(pos)];
	}
	indexOfPreviousPunct {|pos|
		/* returns the index of the previous punctuation
		char or -1 if no punctuation char is found
		 */
		var max, index;
		max = this.size;
		index = pos ? max - 1;
		if( index >= max ) {
			index = max - 1;
		};
		while {(index == 0) and: {this[index].isPunct.not}} {
			index = index - 1;
		};
		^index;
	}
	isIn {|collection|
		^collection.indexOfEqual(this).notNil;
	}
	splitByFunction {|function_isSeparator|
		/* function_isSeparator takes a Char and returns true if
		this char is a separator
		 */
		var word = "";
		var array = [];
		this.do { |let, i|
			if( function_isSeparator.(let) ) {
				array = array.add(word);
				word = "";	
			} /* else */ {  
				word = word ++ let;
			};
		};
		^array.add(word);	
	}	
}

+ Nil {
	isWordDelimiter {
		^true
	}
	isUpper {
		^false;
	}
	isSpace {
		^false;
	}
}

+ Char {
	isWordDelimiter {
		^(this.isSpace or: (this.isPunct and: (#[$~, $_, $$, $\\, $', $"].includes(this).not)))
	}
	isValidEntityChar {
		^(this.isAlphaNum or: {#[ $", $', $\\, $~, $_ ].includes(this)});
	}
}
