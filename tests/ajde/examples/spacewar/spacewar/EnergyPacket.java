
package spacewar;


class EnergyPacket extends SpaceObject {

  static private final int SIZE = 5;             //Can't be changed for now!!!
  int getSize() { return SIZE; }

  private double energy;

  double getEnergy() { return energy; }

  EnergyPacket(Game theGame,
               double xP, double yP, double xV, double yV, double e) {
    super(theGame, xP, yP, xV, yV);
    energy = e;
  }

  void handleCollision(SpaceObject obj) {
    die();
  }
}
