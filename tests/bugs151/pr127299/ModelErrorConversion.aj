aspect ModelErrorConversion {


  // convert exception types
  after(Entity entity) throwing (HibernateException e): modelExec(entity) {
      convertException(e, entity, thisJoinPoint);
  }
  after(Entity entity) throwing (ServiceException e): modelExec(entity) {
      convertException(e, entity, thisJoinPoint);
  }
  after(Entity entity) throwing (SOAPException e): modelExec(entity) {
      convertException(e, entity, thisJoinPoint);
  }
  after(Entity entity) throwing (SOAPFaultException e): modelExec(entity) {
      convertException(e, entity, thisJoinPoint);
  }

  /** execution of any method in the model */
  pointcut modelExecStatic() : 
      execution(* model..*(..));

  pointcut modelExec(Entity entity) : 
      modelExecStatic() && this(entity);
  // soften the checked exceptions
  declare soft: ServiceException: modelExecStatic(); 
  declare soft: SOAPException: modelExecStatic(); 


  /** Converts exceptions to model exceptions, storing context */
  private void convertException(Exception e, Entity entity, 
          JoinPoint jp) {
      ModelException me = new ModelException(e);
      me.setEntity(entity);
      me.setArgs(jp.getArgs());
      // ModelException extends RuntimeException, so this is unchecked
      throw me;
  }
}

class HibernateException extends RuntimeException {}
class ServiceException extends Exception {}
class SOAPException extends Exception {}
class SOAPFaultException extends RuntimeException {}

class Entity {}

class ModelException extends RuntimeException {
    public ModelException(Throwable t) { super(t); }
    public void setEntity(Entity entity) {}
    public void setArgs(Object[] argz) {}
}
