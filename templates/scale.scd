(
fork {
	~scale = { |amp=0.5|
		var warp = Env.bpflinear(0, 0, 0.08, 0, 0.15, 0.5, 0.78, 0.5, 0.97, 1, 1, 1)
		!> IEnvGen.kr(_, amp);

		SendReply.kr(Impulse.kr(10), '/warp', warp);
	}.play;

	s.sync; // --------------------------------------

	~slider = EZSmoothSlider();
	~slider.action = {|slider
}
)

)