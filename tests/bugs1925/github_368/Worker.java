public class Worker {

  public void doWork() throws Exception {
    try {
      riskyCall();
    }
    catch (Exception | LinkageError e) {
      System.out.println("handled: " + e);
    }
  }

  private void riskyCall() throws Exception {
    throw new IllegalStateException("boom");
  }
}
