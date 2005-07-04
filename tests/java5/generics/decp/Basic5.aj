// fails, Number is not a subclass of double!
interface I<T super Number> { }

public class Basic5 {
}

aspect X {
    declare parents: Basic5 implements I<Double>;
}
