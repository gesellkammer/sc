// Andrea Valle, jan 2010

// Extensions fo SynthDef and Synth, see SynthDefAutogui help 
+ SynthDef {
	

	autogui2 { arg aSynth, rate = \audio, target, args, addAction=\addToTail, 
					closeOnCmdPeriod = true, freeOnClose = true, 
					window, step = 50, hOff = 0, vOff = 0, scopeOn=false, specs, onInit = true, fscale=1 ;
			
		SynthDefAutogui(
			name, aSynth, rate, target,args,addAction, 
			closeOnCmdPeriod, freeOnClose , 
			window, step, hOff, vOff, scopeOn, specs, onInit, fscale)
	}

}


+ Synth {
	
	autogui2 { arg rate = \audio, closeOnCmdPeriod = true, freeOnClose = false, 
					window, step = 50, hOff = 0, vOff = 0, scopeOn = false, specs, onInit = true, fscale=1 ;
		SynthDefAutogui
			(defName, this, rate, 
				closeOnCmdPeriod: closeOnCmdPeriod, freeOnClose: freeOnClose, 
				window: window, hOff: hOff, vOff: vOff, step:step, scopeOn:scopeOn, specs:specs, onInit:onInit, fscale:fscale)
	}
}
