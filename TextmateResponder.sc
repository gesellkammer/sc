TextmateResponder {
	classvar <>filename;
	*initClass {
		StartUp.add {
			filename = "TEXTMATE_SC3_BRIDGE_FILE".getenv.standardizePath;
		}
	}
	*writeArray {|array|
		var file = File(filename, "w");
		array.do { |elem| file.putString(elem ++ "\n"); };
		file.close;
		^array
	}
}