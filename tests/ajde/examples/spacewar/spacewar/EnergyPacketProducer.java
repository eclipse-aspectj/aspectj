
package spacewar;


class EnergyPacketProducer extends Thread {
    private final static int MIN = -20;
    private final static int MAX = 80;
    private final static int EXPECTEDINTERVAL = 15;

    private Game game;

    Game getGame() { return game; }

    EnergyPacketProducer(Game theGame) {
        super("EnergyPacketProducer");
        game = theGame;
    }

    public void run() {
        while(true) {
            produceAPacket();
            waitForABit();
        }
    }

    void waitForABit() {
        try { Thread.sleep((int)(Math.random() * EXPECTEDINTERVAL * 2000)); }
        catch (InterruptedException e) {}
    }

    void produceAPacket() {
        EnergyPacket pkt =
            new EnergyPacket(game,
                             Math.random() * getGame().getWidth(),
                             Math.random() * getGame().getHeight(),
                             Math.random() * 2 - 1,
                             Math.random() * 2 - 1,
                             Math.random() * (MAX - MIN) + MIN);
    }
}
