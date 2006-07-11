class CommonEntity {

  public void add(CommonEntity ce) {}
  public void remove(CommonEntity ce) {}

}

class ManageEntity {

  ManageEntity(CommonEntity ce) {
  }
}


abstract aspect Y {
  abstract pointcut entityAccessor(CommonEntity entity);
  before(CommonEntity entity): entityAccessor(entity) {}
}


aspect X extends Y {

    public pointcut entityAccessor1(CommonEntity entity)
        : (execution(* CommonEntity+.add*(CommonEntity+))
           || (execution(* CommonEntity+.remove*(CommonEntity+))))
          && within(CommonEntity+)
          && args(entity) && if(entity != null);

    public pointcut entityAccessor2(CommonEntity entity)
        : execution(ManageEntity.new(CommonEntity+, ..)) 
          && within(ManageEntity)
          && args(entity, ..) 
          && if(entity != null);

    public pointcut entityAccessor(CommonEntity entity)
        : entityAccessor1(entity) || entityAccessor2(entity);


}


