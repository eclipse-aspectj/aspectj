package secondary;

aspect Gamma {

  pointcut calls(): call(* *(..));

}

