
class Type { }

class bat {
    public static final Type SOURCE = new Type();
}
public class DefiniteStatic {
    protected static final Type SINK = bat.SOURCE; 
    public Type sink = SINK;    // incorrect CE: field SINK might not have a value
    public static void main(String[] args) { }
}
