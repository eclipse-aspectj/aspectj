package com.j4fe.aspects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Entity;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Autowired;

public aspect SpringConfigurableMixin {

    public static interface EntityManagerAware {
      EntityManager getEntityManager();
    }


    // not working 
    // declare @type : (@Entity *) : @Configurable(autowire = Autowire.BY_TYPE, preConstruction = true);

    // also not working
    // declare @type : (@Entity *) : @Configurable

    declare parents : (@Entity *) implements EntityManagerAware;

    @PersistenceContext
    transient private EntityManager EntityManagerAware.entityManager;

    public EntityManager EntityManagerAware.getEntityManager() {
       return entityManager;
    }    
}
