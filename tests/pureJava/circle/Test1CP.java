package circle;

public class Test1CP {
    public static void main(String[] args) {
        new Base();
    }
}


class Base implements Type.Reflexive {
}

class Type {
    public interface Reflexive {
    }

    public class Concrete extends Base {
    }
}
