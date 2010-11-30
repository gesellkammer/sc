Completer {
	classvar <allClasses;
	classvar <classNames;
	classvar <methodCache;
	classvar <argsCache;
	classvar <annotatedMethods;
	*initClass {
		StartUp.add {
			allClasses = Class.allClasses.select {|x, i| 
				x.isMetaClass.not};
			classNames = allClasses.collect {|x, i|
				x.asString };
			methodCache = IdentityDictionary.new;
			argsCache = IdentityDictionary.new;
			annotatedMethods = IdentityDictionary[
				({|x| x.beginsWith("num")} -> Integer),
				({|x| #["size",
						"count",
						"indexOf",
						"maxSize",
						"maxIndex",
						"minIndex"
						].indexOfEqual(x).notNil} -> Integer),
				({|x| x.beginsWith("is")} -> Boolean),
				({|x| #["reverse", 
						"stutter",
						"flop",
						"rotate",
						"choose",
						"permute",
						"multiChannelExpand"
						].indexOfEqual(x).notNil} -> Array),
				({|x| #["getenv",
						"readAllString",
						"getcwd"
					   ].indexOfEqual(x).notNil} -> String),
				({|x| #[	"contains",
					   	"includes",
					   	"includesAll",
						"includesAny",
						"any",
						"even",
						"odd"
					  ].indexOfEqual(x).notNil} -> Boolean),
				({|x| #[	"ampdb",
						"dbamp",
						"abs",
						"cpsmidi",
						"midicps"
					  ].indexOfEqual(x).notNil} -> Float),
				("ar" -> UGen),
				("kr" -> UGen)
			];
		};
	}
	*minimumCompletion {|possibilities, startIndex|
		
		var res, first, poss_size, count, i, letter;
		poss_size = possibilities.size;
		count = poss_size;
		first = possibilities[0];
		i = startIndex ? 0;
		{ count == poss_size }.while {
			count = 0;
			letter = first[i];
			possibilities.do {|x| 
				if( x.size >= i and: x[i] == letter,
					{count = count + 1});
			};
			i = i + 1;
		}
		// i gets incremented for the last try which is false, and we
		// substract 1 more becuase of how sc does slicing (the last
		// index gets included.
		^first[0..i-2];
	}
	*prTest_minimumCompletion {
		assert{ Completer.minimumCompletion("eduardo", "eduardito", "eduardazo") == "eduard" };
	}
	*classesBeginningWith {|str|
		if( str == "" or: str.isNil ) {
			^classNames;
		}{ //else 
			^classNames.select( _.beginsWith(str));	
		}; 
	} 
	*classesContaining {|str|
		^classNames.select( _.contains(str));
	}
	*writeAllClassesToFile { |filename|
		var file;
		filename = filename ? "~/Library/Application Support/TextMate/Bundles/SuperCollider.tmbundle/Support/Completions.txt";
		filename = filename.standardizePath;
		file = File(filename, "w");
		classNames.do {|class_name| file.putString(class_name ++ "\n")};
		file.close;
	}
	*methodsBeginningWith {|class, str|
		//if( str.isNil or: str == "" ) {
		if( str == "" ) {
			// we are facing a situation "Class."
			// so we match all methods
			^this.getMethods(class);
		};
		^this.getMethods(class).select {|elem, i| elem.beginsWith(str)};
	}
	*methodsContaining {|class, str|
		^this.getMethods(class).select {|elem, i| elem.contains(str)};
	}

	*getMethods {|class|
		var methods = methodCache[class];
		var args = argsCache[class];
		var collect_methods;
		var meth_name;
		if(methods.isNil) {
			// collect all method names, save them in methodCache and
			// return result
			methods = List[];
			args = IdentityDictionary.new;
			collect_methods = {|aClass|
				if( aClass !== Object ) {
					aClass.class.methods.do {|meth|
						meth_name = meth.name.asString;
						//assert{meth_name != "allClasses"};
						if( meth_name.size >= 2 and: {meth_name[0].isPunct.not} ) {
							methods.add(meth_name);
							args.put(meth_name, meth.argNames.as(Array).drop(1));	
						};
					};
					aClass.methods.do{ |meth| 
						meth_name = meth.name.asString;
						if( meth_name.size > 2 and: {meth_name[0].isPunct.not} ) {
							methods.add(meth_name);
							args.put(meth_name, meth.argNames.as(Array).drop(1));	
						};
					};
					collect_methods.(aClass.superclass);
				};
			};
			collect_methods.(class);
			//methods = methods.sort;
			methodCache.put(class, methods);
			argsCache.put(class, args)
		};
		^methods;
	}
	
	
	//*getArgs {|class, method_name|
	//	var methods = argsCache[class];
	//	if( methods.isNil ) {
	//		this.getMethods(class);
	//		methods = argsCache[class];
	//	}
	//	^methods[method_name];
	//}
	
	*getArgs {|class, method|
		// assert { class.isKindOf(Class) };
		class = class.asSymbol.asClass;
		if( class.isNil ) {
			^[];
		};
		method = class.findRespondingMethodFor(method.asSymbol) ? 
			class.metaclass.findRespondingMethodFor(method.asSymbol);
		^method.argNames.as(Array).drop(1);
	}
	//*getArgsAt{|str, index|
	//	var class, method;
	//	#class, method = this.getClassAndMethod(str, index);
	//	^[class, method, this.getArgs(class, method)];
	//}
	*prProcessChoices {|choices, doc, curr_word, kind_of_completion|
		var curr_word_size = curr_word.size;
		var text_to_insert;
		//if( curr_word_size == 0 ) {
		//	^nil;
		//};
		case
			{ choices.size == 1 }	{ ^doc.insertText(choices[0][curr_word_size..]) }
			{ choices.size > 1 }	{
				 
				var old_color = Document.postColor;
				text_to_insert = this.minimumCompletion(choices, curr_word_size);
					if(text_to_insert.size > curr_word_size) {
						doc.insertText(text_to_insert[curr_word_size..]);
					};
					
				
				Routine {
					Document.postColor = Color.yellow;
						
					"\n".postln;
					"------------ completions \n".postln;
					choices.do { |choice|
						("--> " ++ choice).postln;
					};
					"".postln;
					"-------------------------\n".postln;
					0.1.yield;
					Document.postColor = old_color;
				}.play;
				//Document.postColor = old_color;
			}	
		;
	}
	/*
	*getClassAndMethod{|str, index|
		var curr_word, curr_word_start, curr_word_end, class, method;
		if( index.isNil or: {index >= str.size} ) {
			index = str.size - 1
		};
		#curr_word_start, curr_word_end = str.getWordBoundsAt(index);
		curr_word = str[curr_word_start..curr_word_end];
		if( curr_word.notNil and: {curr_word.at(0).isUpper} ){
			class = curr_word.asSymbol.asClass;
			if( class.isNil ) {
				class = curr_word;
			};
			method = nil;
		} /* else */ {
			/* it is a method */
			if( str[curr_word_start - 1] == $. ) {
				class = this.infereClass(str[0..curr_word_start-2]);
				if( class.notNil ) {
					method = curr_word;
				};
			};
		};
		^[class, method];
	}
	*/
	*getClassAndMethod{|str, index|
		/* returns [completed_class, completed_method, completing_class, completing_method] 
		where each of them can be a Class/symbol or nil
		*/
		var words, curr_word;
		var	class, completing_class, completed_class;
		var method, completing_method, completed_method;
		var prev_char, is_arg, found_method;
		index = index ? str.size - 1;
		prev_char = str.getPreviousChar(index + 1);
		is_arg = prev_char == $(;
		
		str = str.trimRight[..index].splitByFunction({|char|
			(char.isValidEntityChar.not) and: {char != $.} })
			.select( _ != "" )
			.last;
		words = str.split($.);
		curr_word = str.getWordAt(str.size - 1);
		case
			{ words.size == 0 }	{ class = method = nil; }
			{ words.size == 1 }	{ class = words[0]; method = nil }
			{ words.size >= 2 }	{ #class, method = words[words.size - 2..] }
			;
		
		completed_class = this.infereClass(class);
		if( completed_class.isNil ) {
			completing_class = class;
			completed_method = completing_method = nil;
		} /* else */ {
			found_method = this.getMethods(completed_class).indexOfEqual(method);
			
			if( found_method.notNil ) {
				completed_method = method;
				completing_method = nil;
			} /* else */ {
				completing_method = method;
				completed_method = nil;
			};
		}
		^[completed_class, completed_method, completing_class, completing_method, is_arg];
	}
	
	
	*possibleCompletions {|str, cursor_pos|
		/* returns [possible_completions, current_word, kind_of_completion, class, method] */
		var completed_class, completed_method, completing_class, completing_method, is_arg;
		var kind_of_completion, current_word, choices;
		cursor_pos = cursor_pos ? str.size;
		#completed_class, completed_method, completing_class, completing_method, is_arg = this.getClassAndMethod(str, cursor_pos);
		case 
			{ is_arg } {
				current_word = "";
				kind_of_completion = \args;
				if( completed_class.notNil ) {
					if( completed_method.notNil ) {
						choices = this.getArgs(completed_class, completed_method);
					} {
						choices = this.getArgs(completed_class, "new");
					};
				};
			} 
			{ completed_class.notNil and: completed_method.notNil } { 
				// nothind to do
			}
			{ completed_class.notNil and: completed_method.isNil } {
				// we are completing a method
				kind_of_completion = \method;
				choices = this.methodsBeginningWith(completed_class, completing_method.asString);
				current_word = completing_method;
			}
			{ completed_class.isNil and: completed_method.notNil } {
				// nothing to do
			}
			{ completed_class.isNil and: completed_method.isNil } {
				kind_of_completion = \class;
				choices = this.classesBeginningWith(completing_class);
				current_word = completing_class;
			}
			;
		^[choices.asArray, current_word.asString, kind_of_completion.asSymbol, completed_class, completed_method];
	}
	*prShowArgs{|class, method, args|
		"".postln;
		args = args.join(", ");
		("---> " ++ class.asString ++ "." ++ method ++ "(" ++ args ++ ")").postln;
		"".postln;		
	}
	//* {|doc, cursor_pos|
	//	var line;
	//	doc = doc ? Document.current;
	//	cursor_pos = cursor_pos ? doc.cursorPos;
	//	#line, cursor_pos = doc.currentLineAndCursorPos(cursor_pos);
	//	^this.possibleCompletions(line, cursor_pos)
	//}
	*completeAtCursor{|cursor_pos, doc|
		/* cursor_pos and doc are optional */
		var choices, curr_word, line, kind_of_completion, class, method;
		doc = doc ? Document.current;
		cursor_pos = cursor_pos ? doc.cursorPos;
		#line, cursor_pos = doc.currentLineAndCursorPos(cursor_pos);
		// #choices, curr_word = this.completionsAt(doc, doc.cursorPos);
		#choices, curr_word, kind_of_completion, class, method = this.possibleCompletions(line, cursor_pos);
		//this.getClassAndMethod(line, cursor_pos);
		switch( kind_of_completion,
			\class,	 {this.prProcessChoices(choices.sort, doc, curr_word, kind_of_completion)},
			\method, {this.prProcessChoices(choices.sort, doc, curr_word, kind_of_completion)},
			\args,	 {this.prShowArgs(class, method, choices)},
			nil, 	 {nil}
		);	
	}	
	*infereClass {|str|
		/* assume cursor is at extreme right */
		var prev_char, prev_word, class, first_char;
		prev_char = str.getPreviousChar(str.size);
		prev_word = str.getWordAt(str.size - 1);
		class = prev_word.asSymbol.asClass;
		first_char = prev_word.at(0);
		
		if( prev_char.notNil ) {
			if( prev_char == $] ) {
				^Array;
			};
			if( prev_char == $} ) {
				^Function;
			};
		};
		if( prev_word.couldBeNumber ) {
			if( prev_word.indexOf($.).notNil ) {
				^Float;
			} {
				^Integer;
			}
		};
		if( first_char == $" ){ // '" 
			^String; };
		if( first_char == $') {   //'
			^Symbol; };
		if( first_char == $\\) {
			^Symbol; };
		if( first_char.isUpper ) {
			^class; };
		if( prev_word.beginsWith("as") ) {
			class = prev_word[2..].asSymbol.asClass;
			if( class.notNil ) {
				^class; 
			};
		};
		if( prev_word.beginsWith("is") or: prev_word.beginsWith("not") ) {
			^Boolean;
		};
		if( #[	"do", 
				"collect",
				"detect",
				"detectAll",
				"detectIndex", 
				"select",
				].indexOfEqual(prev_word).notNil) {
			^Collection;
		};
		if( prev_word == "s" ) {
			^Server;
		}
		
		^annotatedMethods.matchAt(prev_word);
	}
}