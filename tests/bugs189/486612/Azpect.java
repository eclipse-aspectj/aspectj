aspect Azpect {
 declare parents: B implements I;
 declare parents: D implements I;
  before(): staticinitialization(!Azpect){}
}

interface I {}
