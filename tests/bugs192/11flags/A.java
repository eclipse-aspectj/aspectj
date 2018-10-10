import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface B {}

public class A {
	@B
	public void foo() {}
}

aspect X {
	before(): execution(@B * foo(..)) {}
}
