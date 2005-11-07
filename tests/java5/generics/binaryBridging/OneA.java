import java.util.*;

public class OneA<T extends Number> {

  public T firstMethod() { return null;}

  public void secondMethod(T parm) { }
 
  public void thirdMethod(T parm,T parm2) { }

  public void fourthMethod(List<T> parm) {}

  public T fifthMethod(T parm,List<T> parm2) { return null; }

}
