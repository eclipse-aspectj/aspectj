
package spacewar;


class Timer extends Thread {

    private final static int TICK_PERIOD = 40;  // time between ticks in millis

    private Game game;

    Game getGame() { return game; }

    Timer (Game theGame) {
        super("Timer");
        game = theGame;
    }

    public void run() {
        long t1, tdiff;
        while (true) {
            t1 = System.currentTimeMillis();
            getGame().clockTick();
            tdiff = System.currentTimeMillis() - t1;
            if (tdiff < TICK_PERIOD) {
                try {
                    sleep (Math.max(0 , TICK_PERIOD - tdiff));
                }
                catch (InterruptedException e) { }
            }
        }
    }
}
