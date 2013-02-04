
class Animal<T,Q> { }

class Bar {}

class Intf {}

public class Cage<T extends Animal<? extends Cage<T,Intf>,Intf>,Q> extends Bar { }

