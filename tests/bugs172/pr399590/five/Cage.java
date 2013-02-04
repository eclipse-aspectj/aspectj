//class LionCage extends Cage<Lion> {}
//class Lion extends Animal<LionCage> {}

class Animal<T> { }

class Bar {}


public class Cage<T extends Animal<?>> extends Bar { }
