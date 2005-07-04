// Works, Double meets the spec for the type parameter
interface I<T extends Number> { }

public class Basic4 {
}

aspect X {
    declare parents: Basic4 implements I<Double>;
}
