//@Configurable
public class FancyDomainLogic<E, D extends DomainObject<E>> extends DomainLogic<E, D> { }

aspect X {
declare parents: FancyDomainLogic implements java.io.Serializable;
}
