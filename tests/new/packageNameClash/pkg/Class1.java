package pkg;

public class Class1 { 
    public String doIt() {
        // I guess it's gone now ???
        //Aspect1 pkg = Aspect1.aspectOf(this);
        Aspect1 pkg = null;
        try {
            java.lang.reflect.Constructor c =
                Aspect1.class.getDeclaredConstructors()[0];
            c.setAccessible(true);
            pkg = (Aspect1)c.newInstance(new Object[]{});
        } catch (Exception e) { throw new Error(e+""); }
        return pkg.getClass().getName();
    }
}
