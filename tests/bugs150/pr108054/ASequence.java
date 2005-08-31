public abstract class ASequence <V, T extends ICounter<V> > 
          implements ISequence<V> {

}
          
aspect EnsureTypesUnpackedInWeaver {
	
	before() : staticinitialization(*) && !within(EnsureTypesUnpackedInWeaver) {
		System.out.println("hi");
	}
	
}