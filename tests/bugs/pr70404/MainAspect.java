import org.aspectj.lang.reflect.MethodSignature;
 
public aspect MainAspect {
 
    pointcut testcall(): execution(* test*(..));
 
    before(): testcall() {
      MethodSignature sig = 
        (MethodSignature) thisJoinPointStaticPart.getSignature();
      System.out.println(sig);
      Class[] params = sig.getParameterTypes();
      for(int i=0;i<params.length;i++) {
        Class cls = params[i];
        String name = cls.getName();
        System.out.println(" - " + name);
        if (name.indexOf("ClassNotFoundException")!=-1) throw new RuntimeException("");
      }
    }
}
