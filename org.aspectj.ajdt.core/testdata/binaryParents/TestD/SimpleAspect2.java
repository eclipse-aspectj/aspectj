public aspect SimpleAspect2 {
  declare parents: SimpleClass2 implements Runnable;
  public void SimpleClass2.run()  {}

}
