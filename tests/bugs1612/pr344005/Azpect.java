interface Marker {
  
void doit(String s);
}

aspect Azpect {
  declare parents: (@Anno *) implements Marker;

  public void Marker.doit(String t) {}

  before(Marker m): execution(void m()) && this(m) {}
}
