package a.b.c;

public aspect Underscorer {
  public int Vote.i = 5;
  public String Vote.zzz() { return "abc"; }
  public static class Vote.__ {
    private String string;
    public static class choice {}
    public Ip ip = new Ip();
    public __(String string) { this.string = string; }
    public String getString() { return this.string; }
    public class Ip {
      public String fieldName() { return "ip"; }
      public Class<Vote> type() { return Vote.class; }
    }
  }
}
