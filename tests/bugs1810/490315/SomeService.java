package test;

import java.util.Collection;

public interface SomeService {

    SomePiece<Collection<SomeDTO>> someMethod(SomeCriteria criteria);
}