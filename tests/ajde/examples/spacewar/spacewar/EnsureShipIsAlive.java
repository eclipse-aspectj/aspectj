
package spacewar;

/**
 * This aspect makes sure that the ship is alive before performing any console
 * commands.
 *
 */
aspect EnsureShipIsAlive {
    void around (Ship ship): Ship.helmCommandsCut(ship) {
        if ( ship.isAlive() ) {
            proceed(ship);
        }
    }
}
