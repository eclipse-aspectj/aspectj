interface I<T extends Number> { }

public class Basic5 {
}

aspect X {
    declare parents: Basic5 implements I<String>;
}
