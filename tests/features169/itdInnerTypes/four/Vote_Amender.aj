package a.b.c;

public aspect Vote_Amender {
  public static class Vote._ {
    private String string;
    public static class choice {}
    public _(String string) { this.string = string; }
    public String getString() { return this.string; }
  }
}
