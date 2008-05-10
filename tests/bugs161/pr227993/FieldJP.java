
import java.lang.annotation.*;

enum Store {YES,NO;}

@Retention(RetentionPolicy.RUNTIME)
@interface SearchableProperty { Store store(); }

public class FieldJP {
    @SearchableProperty(store=Store.YES)
    public static int fieldOne;
    
    @SearchableProperty(store=Store.NO)
    public static int fieldTwo;
    
    public static int fieldThree;
    
    public static void main(String[] args) {
        System.err.println("fone="+fieldOne);
        System.err.println("ftwo="+fieldTwo);
        System.err.println("fthr="+fieldThree);
        fieldOne = 5;
        fieldTwo = 6;
        fieldThree = 7;
    }
}

aspect X {
    before(): get(@SearchableProperty(store=Store.YES) * *) {
        System.err.println("get of YES field");
    }
    before(): get(@SearchableProperty(store=Store.NO) * *) {
        System.err.println("get of NO field");
    }
    before(): set(@SearchableProperty(store=Store.YES) * *) {
        System.err.println("set of YES field");
    }
    before(): set(@SearchableProperty(store=Store.NO) * *) {
        System.err.println("set of NO field");
    }
}