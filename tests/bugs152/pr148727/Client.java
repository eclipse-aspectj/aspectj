public class Client {
    public static void main(String argz[]) {
        if (!Asp.hasAspect()) 
          throw new RuntimeException("ou est le aspect?");
        System.out.println("Can call aspectOf? "+Asp.aspectOf());
    }
}
