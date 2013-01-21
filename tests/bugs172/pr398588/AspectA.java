import a.b.c.Anno;

public aspect AspectA {
  before(): execution(@Anno * *(..)) {System.out.println("A");}
}
