package symbols;

public class C {
    /**
     * multiline
     * comment
    */
    void MethV() { }
    public void MethVI(int i) { }
    static public synchronized void MethVLF(long l, float f) { }
    int  MethISO(String s, Object o) { return this.toString().length(); }

    public static volatile int i;
    public float f;

    static {
        i = 0;
    }

    {
        f = 1.f;
    }

}
