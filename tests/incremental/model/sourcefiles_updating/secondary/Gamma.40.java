package secondary;

aspect Gamma {
  before(): call(* *(..)) {
  }

  after(): call(* *(..)) {
  }
}

