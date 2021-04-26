public class TopManager extends Manager {
  public static void main(String[] args) {
    TopManager topManager = new TopManager();
    topManager.sayHello("John");

    // Call ITD methods
    topManager.setJobTitle("CEO");
    System.out.println(topManager.getJobTitle());
  }
}
