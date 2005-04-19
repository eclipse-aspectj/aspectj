// "@DeclareWarning with a static final Object (that is a String)"

import org.aspectj.lang.annotation.*;

aspect A{
	  @DeclareWarning("within(org..*)")
	  static final Object msg = new String("woo");
}
