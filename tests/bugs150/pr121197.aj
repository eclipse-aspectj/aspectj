import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect( "perthis( readOperations() || writeOperations() )" )
public abstract class pr121197 {
	@Pointcut( "" )
    protected abstract void readOperations();
    
    @Pointcut( "" )
    protected abstract void writeOperations();
    
    private ReadWriteLock _lock = new ReentrantReadWriteLock();
    
    @Before( "readOperations()" )
    public void beforeReading() {
        _lock.readLock().lock();
    }

    @After( "readOperations()" )
    public void afterReading() {
        _lock.readLock().unlock();
    }
    
    @Before( "writeOperations()" )
    public void beforeWriting() {
        _lock.writeLock().lock();
    }

    @After( "writeOperations()" )
    public void afterWriting() {
        _lock.writeLock().unlock();
    }

}

@Aspect
class ModelThreadSafety extends pr121197 {
    @Pointcut( "execution( * C.read*(..) )" )
    @Override
    protected void readOperations() {}

    @Pointcut( "execution( * C.write*(..) )" )
    @Override
    protected void writeOperations() { }
}

class C {
	
	public void readSomething() {}
	public void writeSomething() {}
	
}
