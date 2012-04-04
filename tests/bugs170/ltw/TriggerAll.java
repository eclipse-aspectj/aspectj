package ch.aspects;

import ch.annotation.Anno;

public aspect TriggerAll {
        declare @field : * *.myInt : @Anno;

        before(Anno anno) : @annotation(anno) && set(* *.myInt) {
               System.out.println("Triggered");
             }
}
