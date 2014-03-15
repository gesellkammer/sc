README
======

This repository contains Classes, Extensions and Sketches for Supercollider3.

    emlib/
        * Pseudo UGens
            - ringmodulation
            - trumpet physical modelling
            - translation of LOSER plugins from Reaper
            - PV bandpass filter
            - compressor
            - noisegate
            - feedback control
        * syntax extensions
            - Plug: { SinOsc.ar(440) !> Out.ar(0, _) }
            - XFade: { |freq=440, xfade=0.5| 
                       SinOsc.ar(freq) >< Saw.ar(freq) @ xfade 
                     }
            - Dry/Wet:
                 { |wet=0.5|
                     PinkNoise.ar !>< HPF.ar(_, 500) @ wet
                 }
    
    Poppsch-UGens/
        * Pseudo UGens by Constantin Popp
        * Saturation, Oversampling
    
    templates/
        * snippets for very common tasks
