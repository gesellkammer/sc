(

a = { |mic1=0, mic2=1, mic3=2, amp1=1, amp2=1, amp3=1, 
       delay1=0.5, delay2=1, delay3=1.5, out=0|
       
    var a1 = SoundIn.ar(mic1) * amp1
             | DelayC.ar(_, 4, delay1);
             
    var a2 = SoundIn.ar(mic2) * amp2
             | DelayC.ar(_, 4, delay2);
             
    var a3 = SoundIn.ar(mic3) * amp3
             | DelayC.ar(_, 6, delay3);
             
    a1 + a2 + a3
    | Out.ar(out, _)
    ;

}    

)