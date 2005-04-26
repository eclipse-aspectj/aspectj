// "@Pointcut with garbage string"
package ataspectj.misuse;

import org.aspectj.lang.annotation.*;

@Aspect
public class Test014{
  @Pointcut("call%dddd\n\n\n\n\n\n\n\n\n\n\n%dwdwudwdwbuill817pe;][{\ngrgrgnjk78877&&<:{{{+=``\"")
  void somecall(){
  }
}
