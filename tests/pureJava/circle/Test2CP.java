package circle;

public class Test2CP {
    public static void main(String[] args) {
        new Base();
    }
}


class Base implements Type.Reflexive {
    public interface I {}
}

class Type {
    public interface Reflexive {
    }

    public class Concrete implements Base.I {
    }
}
