import org.aspectj.testing.Tester;

public class ParentsAndPointcuts {
    public static void main(String[] args) {
        ContainerDescriptor d = new ContainerDescriptor();
        Tester.check(d instanceof AbstractCaching.Key, "instanceof");
    }
}

aspect AspectBug extends AbstractCaching
perthis(execution(ContainerLoader+.new(..)))
{
    declare parents: ContainerDescriptor implements AbstractCaching.Key;

    protected pointcut loadExecutions( Key key ):
        ContainerLoader.containerLoads( *, key );
}

abstract aspect AbstractCaching  {
    interface Key {}
    protected abstract pointcut loadExecutions(Key key);
}

class Key {
}

class ContainerDescriptor {
}

class ActiveContainer {
}

class ContainerLoader {
    public ActiveContainer createContainer(ContainerDescriptor c) {
        return null;
    }

    public pointcut containerLoads(ContainerLoader loader,
                                   
ContainerDescriptor containerDesc ):
        this(loader) && args(containerDesc)
        && execution(ActiveContainer ContainerLoader.createContainer
(ContainerDescriptor));
}