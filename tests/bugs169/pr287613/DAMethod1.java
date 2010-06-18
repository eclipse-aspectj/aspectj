import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Annot {}

class Person {
	public void foo() {}
	public boolean bar() {return false;}
	public String getString() { return null; }
	public boolean isSet() { return false; }
	public void isNotReturningBoolean() { }
}

aspect DAMethod1 {
    declare @method: (* *.get*()) || (boolean *.is*()): @Annot;
}
