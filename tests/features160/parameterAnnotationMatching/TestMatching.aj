// Each pointcut should match as specified the methods that immediately follow it
aspect TestMatching {
  before(): execution(* m001*(@Anno1 *)) {}
  public void m001a(AnnotatedWithAnno1 p) {} // yes

  before(): execution(* m002*(@Anno1 (*))) {}
  public void m002a(@Anno1 String p) {} // yes
  public void m002b(Integer p) {} // no

  before(): execution(* m003*(@Anno2 (*))) {}
  public void m003a(@Anno2 String p) {} // yes
  public void m003b(@Anno1 String p) {} // no
  public void m003c(Integer p) {} // no

  before(): execution(* m004*(@Anno1 (@Anno2 *))) {}
  public void m004a(@Anno1 AnnotatedWithAnno2 p) {} // yes
  public void m004b(@Anno2 String p) {} // no
  public void m004c(@Anno1 String p) {} // no
  public void m004d(Integer p) {} // no

  before(): execution(* m005*(@Anno1 *,@Anno2 *)) {}
  public void m005a(AnnotatedWithAnno1 p,AnnotatedWithAnno2 q) {} // yes
  public void m005b(AnnotatedWithAnno1 p,@Anno2 String q) {} // no
  public void m005c(String p,AnnotatedWithAnno2 q) {} // no
  
  before(): execution(* m006*(@Anno1 (*),@Anno2 (*))) {}
  public void m006a(@Anno1 String p,@Anno2 String q) {} // yes
  public void m006b(AnnotatedWithAnno1 p,@Anno2 String q) {} // no
  public void m006c(String p,AnnotatedWithAnno2 q) {} // no
  public void m006d(AnnotatedWithAnno1 p,AnnotatedWithAnno2 q) {} // no
  public void m006e(@Anno1 @Anno2 String p,@Anno1 @Anno2 String q) {} // yes

  before(): execution(* m007*(@Anno1 (@Anno2 *))) {}
  public void m007a(@Anno1 AnnotatedWithAnno2 p) {} // yes
  public void m007b(@Anno1 String p) {} // no
  public void m007c(AnnotatedWithAnno2 p) {} // no
  public void m007d(@Anno1 AnnotatedWithAnno1 p) {} // no

  before(): execution(* m008*(@Anno1 (*),..,@Anno2 (*))) {}
  public void m008a(@Anno1 String p,Integer q,@Anno2 String r) {} // yes
  public void m008b(@Anno1 String p,@Anno2 String r) {} // yes
  public void m008c(@Anno1 String p,Integer q,String r,@Anno2 String s) {} // yes
  public void m008d(@Anno1 String p,Integer q,String r,@Anno2 String s,String t) {} // no
  public void m008e(String p,Integer q,String r,@Anno2 String s) {} // no

  before(): execution(* m009*(@Anno1 (*),..,@Anno2 *)) {}
  public void m009a(@Anno1 String p, Integer q,AnnotatedWithAnno2 r) {} // yes
  public void m009b(@Anno1 String p, Integer q,@Anno2 AnnotatedWithAnno2 r) {} // yes
  public void m009c(@Anno1 String p, Integer q,@Anno2 Integer r) {} // no
  public void m009d(String p, Integer q,@Anno2 Integer r) {} // no

  before(): execution(* m010*(..,@Anno1 (*))) {}
  public void m010a(@Anno1 String p,@Anno1 String q) {} // yes
  public void m010b(String p,@Anno1 String q) {} // yes
  public void m010c(@Anno1 String p,String q) {} // no

  before(): execution(* m011*(@Anno1 *...)) {}
  public void m011a(AnnotatedWithAnno1... p) {} // no (the array is not annotated)
  public void m011b(@Anno1 String... p) {} // no

  before(): execution(* m012*(@Anno1 (*...))) {}
  public void m012a(@Anno1 String... p) {} // yes
  public void m012b(AnnotatedWithAnno1... p) {} // no
}
