
package spacewar;

import coordination.Coordinator;


/**
 * This aspect ensures synchronized access to methods of the Registry in
 * the presence of several threads.
 *
 * It uses the Coordinator class, from the AspectJ coordination library.
 *
 * It uses a per-Registry coordination scheme, so there is one instance of
 * this class for each instance of the Registry class.  When this class is
 * constructed, it registers appropriate mutexes and selfexes using the
 * behavior inherited from Coordinator.
 *
 * The mutating methods (register and unregister) should be self-exclusive.
 * Each reader method should be mutually exclusive with the mutating
 * methods.  But the readers can run concurrently.  */
aspect RegistrySynchronization extends Coordinator perthis(this(Registry)) {

    protected pointcut synchronizationPoint():
        call(void Registry.register(..))   ||
        call(void Registry.unregister(..)) ||
        call(SpaceObject[] Registry.getObjects(..)) ||
        call(Ship[] Registry.getShips(..));
    
    public RegistrySynchronization() {
        addSelfex("register");
        addSelfex("unregister");

        addMutex(new String[] {"register", "unregister", "getObjects"});
        addMutex(new String[] {"register", "unregister", "getShips"});
    }

}
