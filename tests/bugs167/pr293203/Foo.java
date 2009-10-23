public class Foo {
  public void m(int i,@Anno String s,int j) {}

  public static void main(String []argv) {
    new Foo().m(1,"A",2);
  }
}

@interface Anno {}

aspect X {
  before(): execution(* *(..,String,..)) {System.out.println("advice");}
  before(): execution(* *(..,@Anno (String),..)) {System.out.println("advice");}
  before(): execution(* *(*,@Anno (String),*)) {System.out.println("advice");}
}

