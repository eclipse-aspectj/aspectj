class AbstractSuperClass<A,B> {}
interface InterfaceOne {}
interface InterfaceTwo<A> {}
class ID {}
abstract class AbstractTestClass<T> extends AbstractSuperClass<T,ID> implements InterfaceOne, InterfaceTwo<T> {
}
class TestType {}
class ConcreteClass extends AbstractTestClass<TestType> {
}
