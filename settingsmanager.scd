(
~settings_manager = {|path, persist_period=10|
    var data;
    var persist_task;
    var load = {|path|
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
        data;
    };
    var save = {|data, path|
        var file = File(path, "w");
        var str = data.asCompileString;
        file.write(str);
        file.close;
    };

    var persist_start = {|data, path, period|
        persist_task = Routine {
            loop {
                save.(data, path);
                period.wait;
            }
        }.play(AppClock);
        persist_task;
    };

    var persist_stop = {
        save.(data, path);
        persist_task.stop;
        persist_task = nil;
    };

    path = path.absolutePath;
    data = load.(path);

    CmdPeriod.doOnce({
        {
            save.(data, path);
        }.defer;
    });

    persist_start.(data, path, persist_period);

    (data:data, persist_task:persist_task, stop:persist_stop, save:save, load:load);
};
)

