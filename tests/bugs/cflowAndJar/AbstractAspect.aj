public abstract aspect AbstractAspect{
  public abstract pointcut directCall();
  
  before(): directCall(){
    noteDirectCall();
  }
  
  abstract void noteDirectCall();
  
  public abstract pointcut badCall();
  
  declare warning: badCall(): "bad";
}
