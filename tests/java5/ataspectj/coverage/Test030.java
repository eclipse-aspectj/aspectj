// "@DeclareWarning with a static final Integer"

import org.aspectj.lang.annotation.*;

aspect A{
	  @DeclareWarning("within(org..*)")
	  static final Integer msg = new Integer(22378008);
}
