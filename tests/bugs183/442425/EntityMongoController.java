package de.scrum_master.app;

public class EntityMongoController<T> implements IEntityController<T> {
    private T entity;

    public void setEntity(T entity) { this.entity = entity; }
    public T getEntity() { return entity; }
}
