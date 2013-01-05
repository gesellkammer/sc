( var name, func, specs, componentType, componentSubtype, builder;

name = "PVFreezeST2048";
func = {|freeze=0|
	var a0 = SoundIn.ar(0);
	var a1 = SoundIn.ar(1);
	var buf0 = LocalBuf(2048);
	var buf1 = LocalBuf(2048);
	var chain0 = FFT(buf0, a0);
	var a0_frozen = IFFT( PV_Freeze(chain0, freeze) );
	var chain1 = FFT(buf1, a1);
	var a1_frozen = IFFT( PV_Freeze(chain1, freeze) );
	Out.ar(0, [a0_frozen, a1_frozen]);
};

specs = #[
	[0, 1, \Linear, 0, \Generic]
];

componentType = \aufx;
componentSubtype = \XFS8;

builder = AudioUnitBuilder.new(name, componentSubtype, func, specs, componentType);
builder.makeInstall;
)
