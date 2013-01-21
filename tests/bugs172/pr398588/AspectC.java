import a.b.c.Blah;

public aspect AspectC {
  before(): execution(@Blah * *(..)) {System.out.println("C");}
}
