
import org.aspectj.testing.Tester;
import java.util.*;
import java.io.*;

/** @testcase unmatched type name in a declare parents should result in a warning in -Xlint mode */
public class MissingTypeInDeclareParents {

    public static void main(String[] args) throws Exception {
        FileReader fr = new FileReader("foo");
        //fr.run();
        Tester.check(true, "Kilroy was here");
    }
}

class C {
}
aspect A {
    /** Xlint warning expected where FileReader is outside code controlled by implementation */
    declare parents : FileReader extends Runnable; // CW 20 Xlint warning 
}
