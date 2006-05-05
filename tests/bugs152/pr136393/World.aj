package hello;

public aspect World {
        pointcut greeting():
                execution(* Hello.sayHello(..));
        after() returning: greeting() {
                System.out.println(" World!");
        }

        String.new(Hello c) {
                this(h.sayHell());
        }

        private static void main(String[] args) {
                String s = new String(new Hello());
                Stystems.substring(0);
        }

}

class Hello {
  public void sayHell() {}
}