package test;

import java.util.Collection;

/**
 */
public class SomeServiceImpl implements SomeService {

    @Override
    @SomeAnno
    public SomePiece<Collection<SomeDTO>> someMethod(SomeCriteria criteria) {
    	System.out.println("stuff");
    	
    	return null;
    }
}