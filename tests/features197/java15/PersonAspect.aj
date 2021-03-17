public aspect PersonAspect {
  // Weave into sealed class
  void around(String name): execution(void sayHello(*)) && args(name) {
    proceed("Sir " + name);
  }

  // ITD into non-sealed subclass of sealed class
  private String Manager.jobTitle;

  public void TopManager.setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public String TopManager.getJobTitle() {
    return jobTitle;
  }
}
