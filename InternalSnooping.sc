InternalSnooping {
	classvar <helpMenuItem;
	*initClass {
		StartUp.add {
			UI.registerForShutdown {
				CocoaMenuItem.clearCustomItems;
			};
		};
	}
	*postInterface  {|class, filter, prefix=""|
		var postMethod = { |meth, pre| 
			var str = (pre ++ meth.name + meth.argNames.as(Array).drop(1));
			if(filter.isNil or: str.find(filter).notNil) {
				str.postln; }
			};
			
    		if(class !== Object) {
        		postf("------------- % --------------\n", class);
        		class.class.methods.do { |meth| postMethod.(meth, prefix ++ "*") };
        		class.methods.do { |meth| postMethod.(meth, prefix) };
        		this.postInterface(class.superclass, filter, prefix ++ "\t");
        	}
    }
	*menuInterface {|class, filter, prefix=""|
		var fun_meth, fun_class, parent; // , parent;
		if(helpMenuItem.notNil) {helpMenuItem.prRemoveMenuItem;};
		helpMenuItem = CocoaMenuItem(nil, 10, class.asString ++ " Methods", true);
		
		// CocoaMenuItem(helpMenuItem, helpMenuItem.children.size, "remove", false, { CocoaMenuItem.clearCustomItems });
		fun_meth = {|parent, meth, pre=""|
			CocoaMenuItem(parent, 
						parent.children.size, 
						pre ++ meth.name + meth.argNames.as(Array).drop(1), 
						false, {class.openHelpFile}); 
		};
		fun_class = {|class, pre=""|
			if(class != Object) {
				parent = CocoaMenuItem(helpMenuItem, helpMenuItem.children.size, class.asString, true);
				class.class.methods.do {|meth| fun_meth.(parent, meth, "*")};
				class.methods.do {|meth| fun_meth.(parent, meth) };
				fun_class.(class.superclass);
			}
		};
		fun_class.(class, prefix);
	}		
}

