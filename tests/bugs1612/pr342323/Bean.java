package ppp;
public class Bean {
  public void m() {
    Runnable r = new Runnable() { public void run() { System.out.println("class");}};
    r.run();
  }
  public void n() {
    Runnable r = new Runnable() { public void run() { System.out.println("class");}};
    r.run();
  }
}
