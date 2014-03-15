EM_SettingsManager {
    var <>mpath;
    var <>mpersistperiod;
    var <>data;
    var <>persist_task;

    *new { |path, persist_period=10, autostart=true|
        ^super.new.init(path, persist_period, autostart);
	}

    init {|path, persist_period, autostart=true|
        mpath = path.absolutePath;
        mpersistperiod = persist_period;
        this.load;
        CmdPeriod.doOnce({
            {
                this.save;
            }.defer;
        });
        if( autostart ) {
            this.start;
        };
    }

    save {
        var file = File(mpath, "w");
        var str = data.asCompileString;
        file.write(str);
        file.close;
    }

    load {
        var path = mpath;
        "reading settings from %".format(path).postln;
        data = if(File.exists(path)) {
            var file, str, dict;
            file = File(path, "r");
            str = file.readAllString;
            "settings: %".format(str).postln;
            dict = str.interpret;
            if ( dict.isKindOf(Dictionary).not ) {
                "settings: problem reading the saved settings".postln;
                ();
            } {
                dict;
            };
        }{
            ();  // <---- defaults to an empty Event
        };
        "finished reading settings".postln;
    }

    start {
        persist_task = Routine {
            loop {
                this.save;
                mpersistperiod.wait;
            }
        }.play(AppClock);
    }

    stop {
        this.save;
		if( persist_task.notNil ) {
			persist_task.stop;
			persist_task = nil;
		};
    }

	set {|key, value|
		data[key] = value;
	}

	get {|key, default=nil|
		var out = this.data[key];
		out ? default;
	}

}