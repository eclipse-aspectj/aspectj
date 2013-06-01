aspect Aspect003 {
  before(): staticinitialization(*) && !within(Aspect003) {
    System.out.println("advice running "+thisJoinPoint);
  }
}
