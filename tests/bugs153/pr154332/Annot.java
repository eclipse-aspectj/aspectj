import java.lang.annotation.*;

@Deprecated @Marker

public aspect Annot {

 

    pointcut test() : within(@Marker *);// *);

   

    declare warning: staticinitialization(@Deprecated *): "deprecated";   

    declare warning: staticinitialization(@Marker *): "marker";   

   

    public static void main(String argz[]) {

        new Baz().foo();

    }

}

 

@Deprecated @Marker

class Baz {

    public void foo() {}

}

 

@Retention(RetentionPolicy.RUNTIME)

 @interface Marker {

 

}