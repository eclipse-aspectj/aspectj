package circle;

public class Test1CF {}


class Base {
    public interface I extends IBottom {}  //CE cyclic inheritance
}

class Type {
    public interface Reflexive {
    }
}

interface DerivedI extends Base.I, Type.Reflexive {}

interface IBottom extends DerivedI {}
