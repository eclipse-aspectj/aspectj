class LionCage2 extends Cage2<Lion2> {}
class Lion2 extends Animal2<LionCage2> {}

class Animal2<T> { }

class Bar2 {}

public class Cage2<T extends Animal2<? extends Cage2<T>>> extends Bar2 { }

aspect X {
	declare parents: Cage2 implements java.io.Serializable;
}

