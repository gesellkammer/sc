+ Document {
	getWordAt {|cursor_pos|
		// returns word at cursor position
		// if cursor position is not given it defaults to the current position
		cursor_pos = cursor_pos ? this.cursorPos;
		^this.string.copyRange(*this.getWordBoundsAt(cursor_pos));
	}
	getWordAtCursor {
		^this.getWordAt(this.cursorPos);
	}
	getWordBoundsAt {|cursor_pos|
		// returns [start, end], which represent the positions in the document
		// where the current word starts/ends
		cursor_pos = cursor_pos ? this.cursorPos;
		^this.string.getWordBoundsAt(cursor_pos);
	}
	currentLineAndCursorPos {|cursor_pos|
		/* returns the current line at cursor_pos as string and
		   the cursor position relative to this line
		 */
		var line_start, line_end, str;
		cursor_pos = cursor_pos ? this.cursorPos;
		str = this.string;
		#line_start, line_end = str.getLineBoundsAt(cursor_pos);
		^[str[line_start..line_end], cursor_pos - line_start];
	}
	
		
	cursorMove {|pos| this.selectRange(pos, 0)}
	cursorMoveRelative {|spaces| this.selectRange(this.cursorPos + spaces, 0); }
	cursorPos { ^this.selectedRangeLocation; }
	
	saveExcursion { |function|
		var savedPos = this.cursorPos;
		function.(this);
		this.cursorMove(savedPos);
	}
	selectWord {|cursor|
		var start, end;
		cursor = cursor ? this.selectionStart;
		#start, end = this.getWordBoundsAt(cursor);
		this.selectRange(start, end - start + 1); 
	}
	insertText {|text, pos, save_excursion=false|
		var saved_pos = this.cursorPos;
		pos = pos ? saved_pos;
		this.selectRange(pos, 0).selectedString_(text);
		if(save_excursion.not)
			{^this};
		if(pos < saved_pos) {
			this.cursorMove(saved_pos + text.size);
			} {
			this.cursorMove(saved_pos) };
			
	}
	charAtCursor{|offset=0|
		^this.string[this.cursorPos + offset];
		}
	*currentWord {
		^current.getWordAtCursor();
	}
	
	*flashPostWindow {
		var curr = current;
		this.listener.front;
		curr.front;
	}
}
