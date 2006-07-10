import java.util.*;

interface I extends Collection { }

class B implements I {

  public Object[] toArray(Object[] os) { return os; }

  public boolean add(Object o) { return false; }
  public boolean addAll(Collection c) { return false; }
  public void clear() { } 
  public boolean contains(Object o) { return false; }
  public boolean containsAll(Collection c) { return false; }
  public boolean isEmpty() { return false; }
  public Iterator iterator() { return null; }
  public boolean remove(Object o) { return false; }
  public boolean removeAll(Collection c) { return false; }
  public boolean retainAll(Collection c) { return false; }
  public int size() { return 0; }
  public Object[] toArray() { return null; }
}

class C { }

aspect X {
  declare parents: C extends B;
}

