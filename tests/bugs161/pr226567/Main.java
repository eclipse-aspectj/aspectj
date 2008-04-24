package c;

import b.Bar;
import b.Foo;

public class Main {

   public static void main(String [] args) {
      Foo foo = new Foo();
      System.out.println(foo instanceof Bar);      
   }
}