
class Animal2<T,Q> { }

class Bar2 {}

class Intf2 {}

public class Cage2<T extends Animal2<? extends Cage2<T,Intf2>,Intf2>,Q> extends Bar2 { }


aspect X {
	declare parents: Cage2 implements java.io.Serializable;
}
