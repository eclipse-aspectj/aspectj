package secondary;

aspect Gamma {

  pointcut calls(): call(* *(..));

  before(): calls() {
  }

  after(): calls() {
  }
}

