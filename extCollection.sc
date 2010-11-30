+ Collection {
	asBuffer {|server|
	    server = server ? Server.default;
		^Buffer.loadCollection(server, this);
	}
}
