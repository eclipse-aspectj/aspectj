package test;

import junit.framework.TestCase;

/**
 * Test case to illustrate problem with SPR-4587.
 * 
 * @author Ramnivas Laddad
 *
 */
public class GenericConfigurableBugTest {
    public static void main(String[] argv) {
        RegularClass regular = new RegularClass();
        GenericParameterClass generic = new GenericParameterClass();
        
        if (!(regular instanceof ConfigurableObject)) 
            throw new RuntimeException("regular not instanceof ConfigurableObject");

        if (!(generic instanceof ConfigurableObject)) 
            throw new RuntimeException("generic not instanceof ConfigurableObject");
        
        if (TestAspect.aspectOf().count!=4) 
            throw new RuntimeException("Count should be 4 but is "+TestAspect.aspectOf().count);
    }
}

aspect TestAspect {
    int count = 0;
    
    after() : initialization(ConfigurableObject+.new(..)) {
        System.out.println(thisJoinPoint);
        count++;
    }
    
    declare parents: @Configurable * implements ConfigurableObject;
}

interface ConfigurableObject {
}

@interface Configurable {
}

@Configurable
class RegularClass {
}

@Configurable
class GenericParameterClass<T> {
}

