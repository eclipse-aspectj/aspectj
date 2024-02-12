package a.b.c;

public aspect Underscorer {
  public Vote.__ Vote.__()  { return new Vote.__(null); }

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
