import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface Secured {
	String value();
}

interface DemoService {
    @Secured("READ")
    void secureMethod();
}

class DemoServiceImpl implements DemoService {
    public void secureMethod() {    }
}

aspect X {
	// None of these match, the subject at execution(secureMethod()) does not have the annotation
	// see https://github.com/eclipse-aspectj/aspectj/blob/master/docs/adk15notebook/joinpointsignatures.adoc#join-point-modifiers
	before(): execution(@Secured! * *Service+.*(..))  {	}
}

public class AspNew {
	public static void main(String[] args) {
		new DemoServiceImpl().secureMethod();
	}
}
