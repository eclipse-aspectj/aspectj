public class GenericServiceImpl<T extends SomeInterface> implements
GenericService<T> {
       public void doStuff(T t) {}
}

