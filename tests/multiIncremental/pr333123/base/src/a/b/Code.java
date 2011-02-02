package a.b;
import java.util.*;

public class Code {
  public void method(String s) {}

  public int getInt() { return 1; }

  public Code transform(Code code,String s, long l) { return code; }

  public List<Code> transform2(List<String> listOfString) { return null; }

  int fieldInt;

  String fieldString;

  Code fieldCode;
  
  List<a.b.Code> fieldList;
}
