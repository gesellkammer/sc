(
a = { |pcile=0.9, aten=0.5, att=0.1, rel=0.2, ampthresh= -16, lagup=0.4, lagdown=0.04, bw=100, freqshift=0, compthresh= -12, slopeBelow=1, slopeAbove=3, clampTime=0.01, relaxTime=0.4, wet=1|
	var a0 = SoundIn.ar(0);
	var amp = Amplitude.kr(a0, att, rel);
	// var amp = RunningSum.rms_kr(a0, 512).poll(10, "amp");
	var ampdb = amp.ampdb.poll(10, "ampdb");
	var fft = FFT(LocalBuf(2048), a0);
	var pcile_freq = fft !> SpecPcile.kr(_, pcile) !> Poll.kr(10, _, "pcile");
	var centroid = SpecCentroid.kr(fft).poll(10, "centroid");
	var isloud = (ampdb > ampthresh);
	var isfeedback = isloud * (pcile_freq > 3000) * (centroid > 2200);
	var which = LagUD.kr(isfeedback, lagup, lagdown);
	var pitch1 = Tartini.kr(a0, n:2048, overlap:512);
	var bwinv = 1 / bw;
	// 0: normal, 1: atenuated
	// var atenshape = InterplPairs( [[x0, y0], [x1, y1], [x2, y2]], \linear ) !> IEnvGen.kr(_,  x);

	var atenuated = BBandStop.ar(a0, pitch1[0], bwinv)
				  !> BBandStop.ar(_, pitch1[0], bwinv)
				  !> FreqShift.ar(_, freqshift)
				  * aten;
				  /*
				  !2
				  !< Compander.ar(_, _, thresh:compthresh.dbamp, slopeBelow:slopeBelow, 
				                        slopeAbove:slopeAbove, clampTime:clampTime, relaxTime:relaxTime);
				                        */
	var pitch2 = Tartini.kr(atenuated, n:2048, overlap:512);
	var atenuated2 = BBandStop.ar(atenuated, pitch2[0], bwinv)
				  !> BBandStop.ar(_, pitch2[0], bwinv)
				  !> FreqShift.ar(_, freqshift)
				  * aten;
				  /*
				  !2
				  !< Compander.ar(_, _, thresh:compthresh.dbamp, slopeBelow:slopeBelow, 
				                        slopeAbove:slopeAbove, clampTime:clampTime, relaxTime:relaxTime);
				  */
	
	var out = LinSelectX.ar(which, [a0, atenuated2]);
	var out2 = a0 * (1-wet) + (out * wet);
	
	//isloud.poll(10, "isloud");	
	isfeedback.poll(10, "isfeedback");
	which.poll(10, "which");
	
	
	
	Out.ar(2, out2);
}.play
)

a.autogui