
package spacewar;

class Bullet extends SpaceObject {

    static private final int SIZE = 3;        //Can't be changed for now!!!
    static private int LIFETIME = 50;

    private int lifeLeft;

    Bullet (Game theGame, double xP, double yP, double xV, double yV) {
        super(theGame, xP, yP, xV, yV);
        lifeLeft = LIFETIME;
    }

    int getSize() { return SIZE; }

    void handleCollision(SpaceObject obj) {
        die();
    }

    void clockTick() {
        if (--lifeLeft == 0)
            die();
        super.clockTick();
    }
}
