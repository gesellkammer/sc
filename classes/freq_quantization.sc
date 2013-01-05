FreqQuantization {
    // pitchtools
      *logfreqs {|deltamidi=0.5, start=0, end=139|
          ^(start..end).collect { |i| i.tofreq };
      }
      *semitone_freqs {
          ^logfreqs(1);
      }
      *quartertones_freqs {
          ^logfreqs(0.5);
      }
      *piano_freqs {
          var keys = ('A0'.tomidi .. 'C8'.tomidi);
          ^keys.collect {|key| key.tofreq};       
      }
      *scale { |prototype='c d eb f g a h', minfreq=0, maxfreq=24000|
          var new_note, fq;
          var pitches = prototype.asString.split($ );
          var octaves = 10;
          var freqs = List();
          octaves.do {|octave|
              pitches.do {|pitch|
                  new_note = octave.asString ++ pitch;
                  fq = new_note.tofreq;
                  if( minfreq < fq and: fq < maxfreq ) {
                      freqs.add(fq)
                  };
              };
          };
          ^freqs.array;
      }
      *freqs2env {|freqs, bw_in_semitones=1, downslope_in_semitones=1, curve=2|
          /*
                       -bw-
                       ....------|  <-- downslope
                      .    .
                      .    .
                     .      .
                    .        .
                  ..          ..
                ..              ..
          ......                  ...........


          */
          var center, crest0, crest1, valley0, valley1, deltas;
          var xs = List();
          var ys = List();
          var curves = List();
          xs.add(0);
          ys.add(0);
          freqs.do {|freq|
              center = freq.tomidi;
              crest0 = center - (bw_in_semitones / 2);
              crest1 = center + (bw_in_semitones / 2);
              valley0 = crest0 - downslope_in_semitones;
              valley1 = crest1 + downslope_in_semitones;
              xs.addAll([valley0, crest0, crest1, valley1]);
              ys.addAll([0, 1, 1, 0]);
              curves.addAll([0, curve, 0, curve.neg]);
          };
          deltas = (xs.size - 1).collect {|i|
              xs[i+1] - xs[i]
          };
          ^Env(ys, deltas, curves);
      }
}
