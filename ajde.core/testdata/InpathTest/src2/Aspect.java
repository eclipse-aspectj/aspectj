public aspect Aspect {
  pointcut sayCalls(): call(* say(..));

  before(): sayCalls() {
    System.err.println("Before say()");
  }
}
