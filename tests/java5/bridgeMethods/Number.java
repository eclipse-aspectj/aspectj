public class Number implements Comparable<Number> {
  private int i;
  public Number(int i) { this.i = i; }
  public int getValue() { return i;}
  public int compareTo(Number that) { return this.getValue() - that.getValue(); }
}
