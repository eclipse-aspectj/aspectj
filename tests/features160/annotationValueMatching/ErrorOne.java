import java.lang.annotation.*;

public class ErrorOne {
  public static void main(String[] args) { }
}

@interface Anno {
  float fval();
}

enum Color { RED,GREEN,BLUE };

// Non existent value of the annotation!
aspect X {	
    before(): execution(@Anno(ival=Color.GREEN) * *(..)) {}
}
