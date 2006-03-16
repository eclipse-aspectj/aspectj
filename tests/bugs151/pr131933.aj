import java.util.List;

aspect Slide71 {
        before(): GenericType<String>.foo() {}
        before(): GenericType<MyList>.foo() {}
        //before(): GenericType.foo() {}
}

class GenericType<T> {
        public pointcut foo(): execution(* T.*(..));
}
