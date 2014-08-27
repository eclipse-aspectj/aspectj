package de.scrum_master.app;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.*;

@Aspect
public class EntityControllerAspect {
    @DeclareParents(value = "@EntityController *", defaultImpl = EntityMongoController.class)
    private IEntityController iEntityController;
/*
    @DeclareMixin("@EntityController *")
    private IEntityController createEntityControllerInstance() {
      return new EntityMongoController();
    }
*/
}
