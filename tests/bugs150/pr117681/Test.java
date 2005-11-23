public class Test {
    public static void main(String[] args) {
        Audit a = (Audit)new Test();
        a.setLastUpdatedBy("username");
        System.out.println("Username ="+a.getLastUpdatedBy());
    }
}
