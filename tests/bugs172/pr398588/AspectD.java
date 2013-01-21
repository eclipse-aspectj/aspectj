import a.b.c.Anno2;

public aspect AspectD {
  before(): execution(@Anno2 * *(..)) {System.out.println("D");}
}
