<CsoundSynthesizer>
<CsOptions>
</CsOptions>
<CsInstruments>

sr = 44100
ksmps = 128
nchnls = 2
0dbfs = 1

;; ---- bauauf. high freqs


instr 1
kfreq line 100, p3, 1000
aout oscil 0.2, kfreq, 1
outvalue "freqsweep", kfreq
outs aout, aout
endin

instr 2
kamp  invalue  "am"
kamp portk kamp, 0.1
kfreq  invalue  "freq"
aout oscil kamp, kfreq, 1
outs aout, aout
endin

</CsInstruments>
<CsScore>
f 1 0 65536 10 1
i 2 0 20
e
</CsScore>
</CsoundSynthesizer>
