package concrete;

import java.util.*;

public class Main {
   public static void main(String[] args) {
     ConcreteClass cc = new ConcreteClass();
     WetCement wc = new WetCement();
     Vector<WetCement> v = new Vector<WetCement>();
     v.add(wc);
     cc.addSomeTs(v);
     System.out.println("ran!");
   }
}