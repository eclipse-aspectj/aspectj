package p;

aspect Checker {
  declare warning: execution(* A.m(..)): "joinpoint location is {joinpoint.sourcelocation.sourcefile}:{joinpoint.sourcelocation.line}";
  declare warning: execution(* A.m(..)): "joinpoint is {joinpoint}";
  declare warning: execution(* A.m(..)): "joinpoint kind is {joinpoint.kind}";
  declare warning: execution(* A.m(..)): "joinpoint kind is '{joinpoint.kind}'";
  declare warning: execution(* A.m(..)): "joinpoint line is '{joinpoint.sourcelocation.line}'";
  declare warning: execution(* A.m(..)): "joinpoint signature is {joinpoint.signature}";
  declare warning: get(int *) && within(A): "joinpoint signature is {joinpoint.signature}";
  declare warning: execution(* A.m(..)): "joinpoint declaring type is {joinpoint.signature.declaringType}";
  declare warning: execution(* A.m(..)): "advice sourcelocation is {advice.sourcelocation.sourcefile}:{advice.sourcelocation.line}";
  declare warning: get(int *): "aspect is {advice.aspecttype}";
  declare warning: get(int *): "signature name for field is {joinpoint.signature.name}";
  declare warning: execution(* A.m(..)): "signature name for method is {joinpoint.signature.name}";
  declare warning: execution(* A.m(..)): "\\{}wibble";
  declare warning: execution(* A.m(..)): "{}foobar"; 
  declare warning: execution(* A.m(..)): "test {advice.sourcelocation.line}\\{}{joinpoint.sourcelocation.line}";
}

class A {
  int i;
  public void m() {
    System.out.println(i);
  } 
}
