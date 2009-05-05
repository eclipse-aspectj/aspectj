import org.aspectj.lang.annotation.*;
aspect X {
  @SuppressAjWarnings
  public A.new() {System.out.println("itd ctor");}

}
