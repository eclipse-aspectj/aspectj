import org.aspectj.testing.Tester;
import java.io.*;

// PR#96

interface HttpConstants {
  static final String s = "s";
}

public aspect FieldFromImplementsNotFound implements HttpConstants { 
    public static void main(String[] args) { test(); } 

    pointcut sendHeader(): 
        call(void LocalFile.sendHeader());
	
    static String aspectField = "t";
    /*static*/ before(): sendHeader() {
	        aspectField += s;
    }

    public static void test() {
        new LocalFile().sendHeader();
        Tester.checkEqual(aspectField, "ts", "field + constant");
    }
}

class LocalFile {
	void sendHeader() {
	}	
}
