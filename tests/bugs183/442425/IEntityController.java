package de.scrum_master.app;

public interface IEntityController<T> {
    void setEntity(T entity);
    T getEntity();
}
