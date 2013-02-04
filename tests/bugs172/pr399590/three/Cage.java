//class LionCage extends Cage<Lion> {}
//class Lion extends Animal<LionCage> {}

class Animal<T> { }

class Bar {}

interface XXX<T> {}

public class Cage<T extends Animal<? super XXX<T>>> extends Bar { }
