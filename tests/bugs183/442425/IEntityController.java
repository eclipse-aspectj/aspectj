public interface IEntityController<T> {
  void setEntity(T entity);

  T getEntity();
}
