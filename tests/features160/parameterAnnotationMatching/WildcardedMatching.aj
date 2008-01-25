aspect WildcardedMatching {
  before(): execution(* m001*(@Ann*1 *)) {}
  public void m001a(AnnotatedWithAnno1 p) {} // yes
  public void m001b(@Anno1 String p) {} // no

  before(): execution(* m002*(@*1 (*))) {}
  public void m002a(@Anno1 String p) {} // yes
  public void m002b(AnnotatedWithAnno1 p) {} // no

  before(): execution(* m003*(@Anno* (*))) {}
  public void m003a(@Anno1 String p) {} // yes
  public void m003b(@Anno2 String p) {} // yes
}
