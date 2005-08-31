interface ISequence<V> {
	
}

interface ICounter<T>  {

}

abstract class ASequence <V, T extends ICounter<V> > 
          implements ISequence<V> {

}