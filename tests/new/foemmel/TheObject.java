import org.aspectj.testing.Tester;

public class TheObject {
    public static void main(String[] args) { new TheObject().realMain(args); }
    public void realMain(String[] args) {
        new TheObject();
    }

    private     int private_int;
    protected   int protected_int;
    /*package*/ int package_int;
    public      int public_int;

    private     int private_call()   { return private_int;   }
    protected   int protected_call() { return protected_int; }
    /*package*/ int package_call()   { return package_int;   }
    public      int public_call()    { return public_int;    }

}

/* ----------------------
  privileged aspect TheAspect of eachobject(instanceof(TheObject)) {
     private TheObject theObject;
     private int myPrivate_int=-1, myPackage_int=-1, myProtected_int=-1, myPublic_int=-1;

     after() returning(TheObject obj): receptions(new()) {
          theObject = obj;
     }

     after() returning(): receptions(* realMain(..)) {
         test_eq();
         test_timeseq();
         test_diveq();
         test_modeq();
         test_pluseq();
         test_minuseq();
         test_leftShifteq();
         test_rightShifteq();
         test_xoreq();
         test_andeq();
         test_unsignedRightShifteq();
         test_postinc();
         test_postdec();
         test_preinc();
         test_predec();         
         test_oreq();
         test_gets();
         test_calls();
     }

     public void test_eq() {
         theObject.private_int   = 1;
         theObject.protected_int = 2;
         theObject.package_int   = 3;
         theObject.public_int    = 4;
         Tester.checkEqual(theObject.private_int,1,"set private_int");
         Tester.checkEqual(theObject.protected_int,2,"set protected_int");
         Tester.checkEqual(theObject.package_int,3,"set package_int");
         Tester.checkEqual(theObject.public_int,4,"set public_int");         
     }

     public void test_timeseq() {
         theObject.private_int   *= 4;
         theObject.protected_int *= 4;
         theObject.package_int   *= 4;
         theObject.public_int    *= 4;
         Tester.checkEqual(theObject.private_int,4,"times private_int");
         Tester.checkEqual(theObject.protected_int,8,"times protected_int");
         Tester.checkEqual(theObject.package_int,12,"times package_int");
         Tester.checkEqual(theObject.public_int,16,"times public_int");          
     }

     public void test_diveq() {
         theObject.private_int   /= 2;
         theObject.protected_int /= 2;
         theObject.package_int   /= 2;
         theObject.public_int    /= 2;
         Tester.checkEqual(theObject.private_int,2,"div private_int");
         Tester.checkEqual(theObject.protected_int,4,"div protected_int");
         Tester.checkEqual(theObject.package_int,6,"div package_int");
         Tester.checkEqual(theObject.public_int,8,"div public_int");          
     }

     public void test_modeq() {
         theObject.private_int   %= 2;
         theObject.protected_int %= 3;
         theObject.package_int   %= 4;
         theObject.public_int    %= 5;
         Tester.checkEqual(theObject.private_int,0,"mod private_int");
         Tester.checkEqual(theObject.protected_int,1,"mod protected_int");
         Tester.checkEqual(theObject.package_int,2,"mod package_int");
         Tester.checkEqual(theObject.public_int,3,"mod public_int");
     }

     public void test_pluseq() {
         theObject.private_int   += 2;
         theObject.protected_int += 2;
         theObject.package_int   += 2;
         theObject.public_int    += 2;
         Tester.checkEqual(theObject.private_int,2,"plus private_int");
         Tester.checkEqual(theObject.protected_int,3,"plus protected_int");
         Tester.checkEqual(theObject.package_int,4,"plus package_int");
         Tester.checkEqual(theObject.public_int,5,"plus public_int");
     }

     public void test_minuseq() {
         theObject.private_int   -= 1;
         theObject.protected_int -= 1;
         theObject.package_int   -= 1;
         theObject.public_int    -= 1;
         Tester.checkEqual(theObject.private_int,1,"minus private_int");
         Tester.checkEqual(theObject.protected_int,2,"minus protected_int");
         Tester.checkEqual(theObject.package_int,3,"minus package_int");
         Tester.checkEqual(theObject.public_int,4,"minus public_int");          
     }

     public void test_leftShifteq() {
         theObject.private_int   <<= 1;
         theObject.protected_int <<= 1;
         theObject.package_int   <<= 1;
         theObject.public_int    <<= 1;
         Tester.checkEqual(theObject.private_int,2,"left shift private_int");
         Tester.checkEqual(theObject.protected_int,4,"left shift protected_int");
         Tester.checkEqual(theObject.package_int,6,"left shift package_int");
         Tester.checkEqual(theObject.public_int,8,"left shift public_int");          
     }

     public void test_rightShifteq() {
         theObject.private_int   >>= 1;
         theObject.protected_int >>= 1;
         theObject.package_int   >>= 1;
         theObject.public_int    >>= 1;
         Tester.checkEqual(theObject.private_int,1,"right shift private_int");
         Tester.checkEqual(theObject.protected_int,2,"right shift protected_int");
         Tester.checkEqual(theObject.package_int,3,"right shift package_int");
         Tester.checkEqual(theObject.public_int,4,"right shift public_int");          
     }

     public void test_xoreq() {
         theObject.private_int   ^= 0;
         theObject.protected_int ^= 1;
         theObject.package_int   ^= 1;
         theObject.public_int    ^= 1;
         Tester.checkEqual(theObject.private_int,1,"xor private_int");
         Tester.checkEqual(theObject.protected_int,3,"xor protected_int");
         Tester.checkEqual(theObject.package_int,2,"xor package_int");
         Tester.checkEqual(theObject.public_int,5,"xor public_int");          
     }

     public void test_andeq() {
         theObject.private_int   &= 3;
         theObject.protected_int &= 6;
         theObject.package_int   &= 3;
         theObject.public_int    &= 4;
         Tester.checkEqual(theObject.private_int,1,"and private_int");
         Tester.checkEqual(theObject.protected_int,2,"and protected_int");
         Tester.checkEqual(theObject.package_int,2,"and package_int");
         Tester.checkEqual(theObject.public_int,4,"and public_int");          
     }

     public void test_unsignedRightShifteq() {
         theObject.private_int   >>>= 0;
         theObject.protected_int >>>= 1;
         theObject.package_int   >>>= 1;
         theObject.public_int    >>>= 2;
         Tester.checkEqual(theObject.private_int,1,"unsigned right shift private_int");
         Tester.checkEqual(theObject.protected_int,1,"unsigned right shift protected_int");
         Tester.checkEqual(theObject.package_int,1,"unsigned right shift package_int");
         Tester.checkEqual(theObject.public_int,1,"unsigned right shift public_int");          
     }

     public void test_postinc() {
         theObject.private_int   ++;
         theObject.protected_int ++;
         theObject.package_int   ++;
         theObject.public_int    ++;
         Tester.checkEqual(theObject.private_int,2,"post ++ private_int");
         Tester.checkEqual(theObject.protected_int,2,"post ++ protected_int");
         Tester.checkEqual(theObject.package_int,2,"post ++ package_int");
         Tester.checkEqual(theObject.public_int,2,"post ++ public_int");          
     }

     public void test_postdec() {
         theObject.private_int   --;
         theObject.protected_int --;
         theObject.package_int   --;
         theObject.public_int    --;
         Tester.checkEqual(theObject.private_int,1,"post -- private_int");
         Tester.checkEqual(theObject.protected_int,1,"post -- protected_int");
         Tester.checkEqual(theObject.package_int,1,"post -- package_int");
         Tester.checkEqual(theObject.public_int,1,"post -- public_int");          
     }

     public void test_preinc() {
         ++ theObject.private_int;
         ++ theObject.protected_int;
         ++ theObject.package_int;
         ++ theObject.public_int;
         Tester.checkEqual(theObject.private_int,2,"pre ++ private_int");
         Tester.checkEqual(theObject.protected_int,2,"pre ++ protected_int");
         Tester.checkEqual(theObject.package_int,2,"pre ++ package_int");
         Tester.checkEqual(theObject.public_int,2,"pre ++ public_int");          
     }

     public void test_predec() {
         -- theObject.private_int;
         -- theObject.protected_int;
         -- theObject.package_int;
         -- theObject.public_int;
         Tester.checkEqual(theObject.private_int,1,"pre -- private_int");
         Tester.checkEqual(theObject.protected_int,1,"pre -- protected_int");
         Tester.checkEqual(theObject.package_int,1,"pre -- package_int");
         Tester.checkEqual(theObject.public_int,1,"pre -- public_int");          
     }     

     public void test_oreq() {
         theObject.private_int   |= 8;
         theObject.protected_int |= 8;
         theObject.package_int   |= 8;
         theObject.public_int    |= 8;
         Tester.checkEqual(theObject.private_int,9,"or private_int");
         Tester.checkEqual(theObject.protected_int,9,"or protected_int");
         Tester.checkEqual(theObject.package_int,9,"or package_int");
         Tester.checkEqual(theObject.public_int,9,"or public_int");          
     }

     public void test_gets() {
         myPrivate_int   = theObject.private_int;
         myProtected_int = theObject.protected_int;
         myPackage_int   = theObject.package_int;
         myPublic_int    = theObject.public_int;
     }

     public void test_calls() {
         theObject.private_call();
         theObject.protected_call();
         theObject.package_call();
         theObject.public_call();         
     }
}
-------------------- */
