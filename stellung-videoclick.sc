StellungVideoClick {
    var <curr_dur;
    var <win;
    var <beats;
    var <winx, <winy;
    var <xmargin, <colmargin, <ymargin;
    var <beat_height;
    var <colmargin; 
    var <currbeat;
    var <numbeats;
    var <winbounds;
    var <xratio;
    var <lasttime;
    var <beatdur;
    var <timeslider, direction;
    var <tick;
    var maxticks;
    var started;
    
    *new { |x=1280, y=800|
        ^super.new.init(x, y)
    }
    init { |x=1280, y=800|
        var maxbeats = 7;
        var maxx, ratio;
        
        winx = x;
        winy = y;
        curr_dur = 4;
        beats = [];
        colmargin = 60;
        beat_height = 600;
        xratio = 40;
        numbeats = 0;
        currbeat = 0;
        xmargin = colmargin;
        ymargin = (beat_height * 0.2).round;
        lasttime = Main.elapsedTime;
        direction = 1;
        this.makeWindow(x, y);
        this.connect();
        started = false;
      
        maxx = xmargin * 2 + (colmargin * (maxbeats - 1)) +
               (maxbeats * 4 * xratio);
        if( maxx > winbounds.width ) {
            ratio = winbounds.width / maxx;
            colmargin = colmargin * ratio;
            xratio = xratio * ratio
        };
    }
    checkDependencies {
        var quarks_used = [
            "AudioUnitBuilder", "wslib"
        ];
        quarks_used.do { |quark|
            Quarks.install(quark);
        };
    }
    connect {
        MIDIWindow();
        MIDIIn.noteOn = { |src, chan, num, vel|
            started = true;
            chan.switch( 
                0, { curr_dur = 4 - (num - 48);
                     maxticks = curr_dur * 6 + 1
                    },
                1, { this.click(num) }
            );
        };
        MIDIIn.sysrt = {
            |src, chan, val|
            var x;
            if( started ) {
            
            
                tick = tick + 1;
                if( direction == 0 ) {
                    x = tick / maxticks
                } /*else*/ { 
                    x = 1 - (tick / maxticks);
                };
                { timeslider.value_(x); }.defer
            };
            //this.prUpdateTimeslider;
            //var now = Main.elapsedTime;
            //var dt = now - lasttime;
            //lasttime = now;
            //beatdur = dt * 24;
            //ticksPerBeat = 
        }
    }
    //prUpdateTimeslider {
    //    var val = (tick / maxticks);
    //   
    //}
    initRow { |numbeats, duration|
        var shapex = (duration * xratio);
        direction = 1;
        {
            if( beats.size > 0 ) {
                beats.do{ |beat| beat.remove };
                win.refresh;
            };
            beats = numbeats.collect {|numbeat|
                
                RoundButton(win, Rect(xmargin + (numbeat * (shapex + colmargin)), ymargin, shapex, beat_height))
                    .states_([
                        ["", Color.black, Color.gray(0.7)],
                        ["", Color.red, Color.red]
                        ])
                    .extrude_(false);
        
            };
        }.defer;
    }
    click { |pitch|
        
        case
            { pitch <= 70 } {
                { this.initRow( pitch - 59, curr_dur );
                  currbeat = 0;
                  this.clickNext();
                }.defer;
            }
            { pitch >= 71 } { 
                { this.clickNext(); }.defer;
            }
    }
    clickNext {
        var x;
        tick = 0;
        direction = 1 - direction;
        if( beats.size == currbeat ) {
            Error("too many beats").throw;
        };
        beats[currbeat].value_(1);
        //this.prUpdateTimeslider;
        currbeat = currbeat + 1;
        if( direction == 0 ) {
            x = tick / maxticks
        } /*else*/ { 
            x = 1 - (tick / maxticks);
        };
        { timeslider.value_(x); }.defer
    }
    
    makeWindow { |width, height|
        var win_xmargin, win_ymargin;
        winbounds = SCWindow.screenBounds;
        win_xmargin = winbounds.width - width / 2.0;
        win_ymargin = winbounds.height - height / 2.0;
        win = SCWindow(
            "S T E L L U N G",
            Rect(win_xmargin, win_ymargin, width, height)
            )
            .front //.decorate(30@((height - beat_height) / 2.0), 40@40);
        ;
        timeslider = RoundSlider(win, Rect(xmargin, 15, 200, 50))
                        .value_(0)
                        .extrude_(false)
                        .background_(Color.gray(0.5))
                        .knobColor_( Color.blue)
                        .knobSize_(1)
        ;
        
    }
}