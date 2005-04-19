// "@DeclareWarning with a non-final String"

import org.aspectj.lang.annotation.*;

aspect A{
	  @DeclareWarning("within(org..*)")
	  static String msg = "Let this be a warning to you";
}
