
package spacewar;


/**
 * Pilot is the abstract superclass of Player and Robot.
 *
 */

abstract class Pilot {
    private Game game;
    private int  number;
    protected Ship ship = null;

    Game getGame()   { return game; }
    int  getNumber() { return number; }
    Ship getShip()   { return ship; }

    void setShip(Ship s) { ship = s; }

    Pilot (Game g, int n) {
        super();
        game   = g;
        number = n;
    }
}
