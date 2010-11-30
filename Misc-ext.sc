+ Function {
	benchN { |times=1000|
		^{times.do { this }}.bench;
	}
	assert {
		if(this.value) {
			^true;
		}{
			Error("Assertion error").throw;
		};
	}
}

+ SequenceableCollection {
	pairwise { 
		^this.slide(2).clump(2);
	}
}