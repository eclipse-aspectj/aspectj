public interface GenericInterfaceChain<T> extends Foo<String,T> {
}

interface Foo<A,B> extends Bar<B>,java.io.Serializable {
}

interface Bar<B> extends java.io.Serializable {
}
