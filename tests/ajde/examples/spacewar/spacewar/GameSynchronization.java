
package spacewar;

import coordination.Coordinator;

/**
 * This aspect ensures synchronized access to methods of the Game in the
 * presence of several threads.
 *
 * It uses the Coordinator class, from the AspectJ coordination library.
 * (This case is right on the borderline of being too simple to use the
 * coordination library, but we use it anyways to keep the similarity
 * with the RegistrySynchronizer.)
 *
 * It uses a per-Game coordination scheme, so there is one instance of
 * this class for each instance of the Game class.  When this class is
 * constructed, it registers appropriate mutexes and selfexes using
 * the behavior inherited from Coordinator.
 *
 * The coordination constraints for the Game are simple.  We just need to
 * make sure that newShip and handleCollisions are mutually exclusive.  That
 * ensures that they we can't destroy a ship that has just been replaced.
 */
aspect GameSynchronization extends Coordinator perthis(this(Game)) {

    protected pointcut synchronizationPoint():
	call(void Game.handleCollisions(..)) || call(Ship Game.newShip(..));

    public GameSynchronization() {
        addMutex(new String[] {"handleCollisions", "newShip"});
    }

}
