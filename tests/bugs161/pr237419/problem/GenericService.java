package problem;

public abstract class GenericService<T extends Generic<?>> {
  protected abstract T update(T current);
}
