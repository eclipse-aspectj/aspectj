// Each pointcut should match as specified the methods that immediately follow it
aspect TestMatching {
  before(): execution(C001*.new(@Anno1 *)) {}
  static class C001a {  C001a(AnnotatedWithAnno1 p) {} } // yes

  before(): execution(C002*.new(@Anno1 (*))) {}
  static class C002a { C002a(@Anno1 String p) {} } // yes
  static class C002b { C002b(Integer p) {} } // no

  before(): execution(C003*.new(@Anno2 (*))) {}
  static class C003a { C003a(@Anno2 String p) {} } // yes
  static class C003b { C003b(@Anno1 String p) {} } // no
  static class C003c { C003c(Integer p) {} } // no

  before(): execution(C004*.new(@Anno1 (@Anno2 *))) {}
  static class C004a { C004a(@Anno1 AnnotatedWithAnno2 p) {} } // yes
  static class C004b { C004b(@Anno2 String p) {} } // no
  static class C004c { C004c(@Anno1 String p) {} } // no
  static class C004d { C004d(Integer p) {} } // no

  before(): execution(C005*.new(@Anno1 *,@Anno2 *)) {}
  static class C005a { C005a(AnnotatedWithAnno1 p,AnnotatedWithAnno2 q) {} } // yes
  static class C005b { C005b(AnnotatedWithAnno1 p,@Anno2 String q) {} } // no
  static class C005c { C005c(String p,AnnotatedWithAnno2 q) {} } // no
  
  before(): execution(C006*.new(@Anno1 (*),@Anno2 (*))) {}
  static class C006a { C006a(@Anno1 String p,@Anno2 String q) {} } // yes
  static class C006b { C006b(AnnotatedWithAnno1 p,@Anno2 String q) {} } // no
  static class C006c { C006c(String p,AnnotatedWithAnno2 q) {} } // no
  static class C006d { C006d(AnnotatedWithAnno1 p,AnnotatedWithAnno2 q) {} } // no
  static class C006e { C006e(@Anno1 @Anno2 String p,@Anno1 @Anno2 String q) {} } // yes

  before(): execution(C007*.new(@Anno1 (@Anno2 *))) {}
  static class C007a { C007a(@Anno1 AnnotatedWithAnno2 p) {} } // yes
  static class C007b { C007b(@Anno1 String p) {} } // no
  static class C007c { C007c(AnnotatedWithAnno2 p) {} } // no
  static class C007d { C007d(@Anno1 AnnotatedWithAnno1 p) {} } // no

  before(): execution(C008*.new(@Anno1 (*),..,@Anno2 (*))) {}
  static class C008a { C008a(@Anno1 String p,Integer q,@Anno2 String r) {} } // yes
  static class C008b { C008b(@Anno1 String p,@Anno2 String r) {} } // yes
  static class C008c { C008c(@Anno1 String p,Integer q,String r,@Anno2 String s) {} } // yes
  static class C008d { C008d(@Anno1 String p,Integer q,String r,@Anno2 String s,String t) {} } // no
  static class C008e { C008e(String p,Integer q,String r,@Anno2 String s) {} } // no

  before(): execution(C009*.new(@Anno1 (*),..,@Anno2 *)) {}
  static class C009a { C009a(@Anno1 String p, Integer q,AnnotatedWithAnno2 r) {} } // yes
  static class C009b { C009b(@Anno1 String p, Integer q,@Anno2 AnnotatedWithAnno2 r) {} } // yes
  static class C009c { C009c(@Anno1 String p, Integer q,@Anno2 Integer r) {} } // no
  static class C009d { C009d(String p, Integer q,@Anno2 Integer r) {} } // no

  before(): execution(C010*.new(..,@Anno1 (*))) {}
  static class C010a { C010a(@Anno1 String p,@Anno1 String q) {} } // yes
  static class C010b { C010b(String p,@Anno1 String q) {} } // yes
  static class C010c { C010c(@Anno1 String p,String q) {} } // no

  before(): execution(C011*.new(@Anno1 *...)) {}
  static class C011a { C011a(AnnotatedWithAnno1... p) {} } // no (the array is not annotated)
  static class C011b { C011b(@Anno1 String... p) {} } // no

  before(): execution(C012*.new(@Anno1 (*...))) {}
  static class C012a { C012a(@Anno1 String... p) {} } // yes
  static class C012b { C012b(AnnotatedWithAnno1... p) {} } // no
}
