/*

Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.

This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.

|<---            this code is formatted to fit into 80 columns             --->|
|<---            this code is formatted to fit into 80 columns             --->|
|<---            this code is formatted to fit into 80 columns             --->|


EnergyPacket.java
Part of the Spacewar system.

*/

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
