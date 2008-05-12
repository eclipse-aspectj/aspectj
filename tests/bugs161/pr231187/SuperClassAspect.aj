package concrete;

import java.util.Vector;


public aspect SuperClassAspect {
   declare parents : WetCement implements Cement;

   after(SuperClass sc, Vector cm) returning: execution(void SuperClass.addSomeTs(Vector)) && target(sc) && args(cm) {
    //  System.out.println(cm);   
   }
}
