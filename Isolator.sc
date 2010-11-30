Isolator {
	*new {
		ProxySpace.push;
		~noise = {PinkNoise.ar * 0.5};
		~noise.play;
		~tap = {SoundIn.ar(0) * 4 | FreeVerb.ar(_, 1, \room_size.kr(0.5), 0.5)};
		~tap.play;
		ProxyMixer.new;
		
		
	}
}
