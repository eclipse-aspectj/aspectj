package theapp;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

import moody.Moody;

@Aspect
class AnnotationMoodIndicator {
   @DeclareParents(value="theapp.AnnotationMoodImplementor",defaultImpl=MoodyImpl.class)
   private Moody implementedInterface;
}

