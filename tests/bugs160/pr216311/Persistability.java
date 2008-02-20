

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;


@Aspect
public class Persistability {

    static class Persistable implements IPersistable {

        private static final long serialVersionUID = 7120491865883787353L;

        private int id;

        public Persistable() {
            super();
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

    }

    @DeclareParents(value = "PersistabilityTest", defaultImpl = Persistable.class)
    private IPersistable observable;

    @Pointcut("initialization(IPersistable.new(..)) && this(bean) && !this(Persistable)")
    void init(IPersistable bean) {
    }

    @Before("init(bean)")
    public void beforeInit(IPersistable bean) {
        bean.setId(System.identityHashCode(bean));
    }

}
