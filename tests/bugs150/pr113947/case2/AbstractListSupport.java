
public abstract aspect AbstractListSupport<I,M extends I> {

  //declare parents : @LinkedListItem * implements M;


  // Interface
  interface ListInterface<Item> {
    Item getNext();
    void setNext(Item item);
  }
    
  private K ListInterface<K>.next;

  public K ListInterface<K>.getNext () {
    return next;
  }

  public void ListInterface<K>.setNext (K item) {
    next = item;
  }

}
