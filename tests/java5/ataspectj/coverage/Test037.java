//"@Before advice with empty string"

import org.aspectj.lang.annotation.*;

aspect A{
	  @Before("") // this should compile I think, it's just Before advice for no pointcut
	  public void someCall(){
	  }
}
