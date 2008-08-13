import java.util.List;

public interface PartitionedFinder<P extends Partitioned<?>> extends Finder {
//	public <T extends Localized> List<T> bestLanguageMatch(List<T> list, List<String> languageOrder);
	public <T extends Partitioned<?>> List<T> bestPartitionMatch(List<T> list, List<String> partitionOrder);
}

aspect PartitionedFinderAspect {

    public Class<? extends P> PartitionedFinder<P>.getPartitionedType(){
            return ClassUtils.guessGenericType(getClass());
    }

    public List<String> PartitionedFinder<P>.getPartitionOrder(){
            return H2Deployment.instance().getPartitionOrder(getPartitionedType());
    }

    public <T extends Partitioned<?>> List<T> PartitionedFinder<P>.bestPartitionMatch(List<T> list, List<String> partitionOrder){
            return new OrderComparator<T, String>(partitionOrder){

                    @Override
                    public String getOrdering(T partitioned){
                            return partitioned.getPartitionId();
                    }

            }.bestMatch(list);
    }
}

class OrderComparator<A,B> {
	OrderComparator(List<String> ls) {}
	public String getOrdering(A a) {return "";}
	List bestMatch(List l) {return null;}
}
