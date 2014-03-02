Acoustics {
    *duct_resonfreq_unflanged {|length, radius, vibrmode=1, speed=343|
        /*
        calculate the resonance frequency of an unflanged duct (unflanged=open)
        length: the length of the tube
        radius: the radius of the tube
        vibrmode: 1, 2, 3, ... --> vibration mode
        speedOfPropagation: the speed of propagation --> defaults to air at 20°
        */
        // var frequency = vibrmode * speedOfPropagation / (2*(length + (0.61*radius)));
        var freq = vibrmode*speed / (2*((0.61*radius) + length));
        ^freq;
    }
    *duct_maxfreq {|radius, speed=343|
        /* only frequencies with a lambda higher then 4 times the dimeter of the duct are transmitted */
        ^( speed / (8*radius) );
    }
}