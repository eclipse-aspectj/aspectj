/*
 * Created on Jan 12, 2005
 */

package foo;

/**
 * @author Mik Kersten
 */
public abstract aspect Pointcuts {

    private pointcut privatePointcut ();
    protected pointcut protectedPointcut ();
    public pointcut publicPointcut ();
    
    private void privateMethod () {
        
    }
    
    public void protectedMethod () {
        
    }
    
    public void publicMethod () {
        
    }
}
