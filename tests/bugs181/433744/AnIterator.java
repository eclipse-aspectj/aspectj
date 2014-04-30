import java.util.Iterator;

public class AnIterator implements Iterator<Object> {
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Object next() {
        throw new IllegalStateException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
