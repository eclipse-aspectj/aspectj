public class BadSynchronized {
    public void m() {
        synchronized(2) {}
        synchronized(false) {}
        synchronized(null) {}
        synchronized("");
    }
}
