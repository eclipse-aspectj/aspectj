import java.util.ArrayList;
import java.util.List;

class PlayList {

    private static PlayList instance;
    
    private List<Song> list;
    
    private PlayList() {
        list = new ArrayList<Song>();         
    }

    public static PlayList instance() {
        if(instance==null ) {
            instance = new PlayList();
        }
        return instance;
    }

    public void enqueue(Song song) {
        list.add(song);
        if(Player.instance().isIdle()) {
            new Thread() {
                public void run() {
                    System.out.println("Playing playlist...");
                    for (Song s : list) {
                        Player.instance().play(s.getName());
                    }
                }  
            }.start();            
        }
    }

}

class Song {

    private String name; 

    public Song(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}

class Player {

    private static Player instance;
    
    private Player() {}

    public static Player instance() {
        if(instance==null ) {
            instance = new Player();
        }
        return instance;
    }

    public void play(String name) {
        System.out.println("Playing Song "+name+"...");
    }

    public boolean isIdle() {
        return true;
    }
}

class Jukebox {
    
	   public void play(Song song) {
	        Player.instance().play(song.getName());
	   }

	}

class Main {

    public static void main(String[] args) {
        Song song = new Song("Merry XMas");
        Jukebox jukebox = new Jukebox();
        
        jukebox.play(song);
    }
    
}

aspect PlaylistAspect {
    
    void around(Song song) :
        call(public void Jukebox.play(Song))
        && args(song) {
        PlayList.instance().enqueue(song);
    }

}

aspect CreditsAspect {

    void around() : call(public void Jukebox.play(Song)) {
        if(Credits.instance().enoughCredits()) {
            System.out.println("Withdrawing credit.");
            Credits.instance().withDraw();
            proceed();
        } else {
            throw new InsufficientCreditsException();
        }
    }
    
}

class Credits {

    private static final int INITIAL_CREDITS = 10;

    private static Credits instance;
    
    private int credits;
    
    private Credits() {
        credits = INITIAL_CREDITS;        
    }
    
    public static Credits instance() {
        if(instance==null ) {
            instance = new Credits();
        }
        return instance;
    }

    public boolean enoughCredits() {
        return credits > 0;
    }
    
    public void withDraw() {
        credits--;
    }
}

@SuppressWarnings("serial")
class InsufficientCreditsException extends RuntimeException {

    public InsufficientCreditsException() {
        super();
    }

    public InsufficientCreditsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientCreditsException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public InsufficientCreditsException(Throwable cause) {
        super(cause);
    }

}
