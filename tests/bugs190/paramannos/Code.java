import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}

public class Code {
	public Code(@Anno String boo) {}
	public Code(Object boo) {}
}

aspect X {
	before(): execution(new(!@Anno (*))) { }
}
