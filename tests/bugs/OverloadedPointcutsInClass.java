public class OverloadedPointcutsInClass {

  pointcut pc1(): call(* *(..));
  pointcut pc1(): execution(* *(..));
  
  pointcut pc2(String s): call(* *(..)) && target(s);
  pointcut pc2(StringBuffer sb): call(* *(..)) && target(sb);

}
