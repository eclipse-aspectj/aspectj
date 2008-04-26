import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public aspect Test {
    // OK (matches f1 and f2):
    declare warning : execution(* *(!(Object+), ..)) : "mOne: value parameter";
   
    // Wrong (matches f1 and f2, should match only f1):  a Not type pattern containing @A and negatedPattern Object+ is built
    declare warning : execution(* *(@A (!(Object+)), ..)) : "mTwo: @A annotated value parameter";
   
    // OK (matches f1):
    declare warning : execution(* *(@A (*), ..)) && execution(* *(!(Object+), ..)): "mThree: @A annotated value parameter.";

    // OK (matches f3 and f4):
    declare warning : execution(* *(Object+, ..)) : "mFour: Reference parameter.";

    // Wrong (no matches, should match f3):
    declare warning : execution(* *(@A (Object+), ..)) : "mFive: @A annotated reference parameter!";
   
    // OK (matches f3):
    declare warning : execution(* *(@A (*), ..)) && execution(* *(Object+, ..)): "mSix: @A annotated reference parameter.";
   
    // Wrong (matches f1 and f2, should match only f2):
     declare warning : execution(* *(!@A (!(Object+)), ..)) : "mSeven: Non-@A annotated value parameter!";
   
    // Wrong (matches f1 and f2, should match only f2):
    declare warning : execution(* *(!@A (*), ..)) && execution(* *(!(Object+), ..)): "mEight: Non-@A annotated value parameter.";

    // OK (matches f2):
    declare warning : !execution(* *(@A (*), ..)) && execution(* *(!(Object+), ..)): "mNine: Non-@A annotated value parameter.";

    // Wrong (matches f3 and f4, should match only f4):
    declare warning : execution(* *(!@A (Object+), ..)) : "mTen: Non-@A annotated reference parameter!";
   
    // Wrong (matches f3 and f4, should match only f4):
    declare warning : execution(* *(!@A (*), ..)) && execution(* *(Object+, ..)): "mEleven: Non-@A annotated reference parameter.";

    // OK (matches f4):
    declare warning : !execution(* *(@A (*), ..)) && execution(* *(Object+, ..)): "mTwelve: Non-@A annotated reference parameter.";

    void f1(@A int i) {}

    void f2(int i) {}

    void f3(@A Integer i) {}

    void f4(Integer i) {}
   
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    private static @interface A {

    }
}