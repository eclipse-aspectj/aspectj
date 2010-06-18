import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Annot {}

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {}

class Person {
	@Foo
	public void foo() {}
	@Foo
	public boolean bar() {return false;}
	@Foo
	public String getString() { return null; }
	@Foo
	public boolean isSet() { return false; }
	@Foo
	public void isNotReturningBoolean() { }

	public void getin() {}
}

aspect DAMethod2 {

    declare @method: !(* *.get*()) && !(* aspectOf(..)) && !(* hasAspect(..)): @Annot;
    
    declare @method: !@Foo * *(..) && !(* aspectOf(..)) && !(* hasAspect(..)): @Annot;
}
