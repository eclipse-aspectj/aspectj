import java.util.*;

interface Item {}
interface Container {}
interface CompositeItem<T> {}
interface CompositeContainer<T extends CompositeItem> {}

public abstract aspect Broke<Item, Container> {

  private C CompositeItem<C>.container;

  public List<I> CompositeContainer<I>.itemList = new ArrayList<I>();

  public CompositeContainer<I> CompositeContainer<I>.addItem(I i) {
    itemList.add(i);

    i.container = this;

    return this;
  }

}