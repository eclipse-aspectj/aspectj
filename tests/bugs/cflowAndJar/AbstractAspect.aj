public abstract aspect AbstractAspect{
  public abstract pointcut directCall();
  before(): directCall(){
    System.out.println("direct");
  }
}
