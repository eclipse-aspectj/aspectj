package lib;

public abstract aspect AbstractA {
    public interface Marker {}
    
    public String Marker.value = "public";
    private String Marker.pValue = "private";
    
    public static String getPrivateValue(Marker m) { return m.pValue; }

	protected abstract pointcut scope();

    declare error: scope() && within(Marker+) && call(* java.io.PrintStream.println(*)):
        "use a proper logger";
    
    before(Marker m): this(m) && execution(new(..)) {
        System.err.println("making a Marker: " + m + " with " + m.pValue);
    }
    
    declare parents: *..*MarkMe implements Marker;
}