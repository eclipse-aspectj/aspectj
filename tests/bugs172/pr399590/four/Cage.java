//class LionCage extends Cage<Lion> {}
//class Lion extends Animal<LionCage> {}

class Animal<T,R> { }

class Bar {}

interface XXX<T> {}
interface YYY<T> {}

public class Cage<T extends Animal<? super XXX<T>,YYY>> extends Bar { }
