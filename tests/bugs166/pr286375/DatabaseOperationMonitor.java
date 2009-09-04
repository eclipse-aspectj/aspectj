privileged public abstract aspect DatabaseOperationMonitor<T extends AggregatedDatabaseStats<T>> extends BaseOperationMonitor<T> {}
class BaseOperationMonitor<P> {}
class AggregatedDatabaseStats<Q> {}
class Wibble extends AggregatedDatabaseStats<Wibble> {}
aspect Foo extends DatabaseOperationMonitor<Wibble> {}
