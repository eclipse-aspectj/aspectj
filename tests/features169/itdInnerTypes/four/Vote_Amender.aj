package a.b.c;

public aspect Vote_Amender {
  public static class Vote.__ {
    private String string;
    public static class choice {}
    public __(String string) { this.string = string; }
    public String getString() { return this.string; }
  }
}
