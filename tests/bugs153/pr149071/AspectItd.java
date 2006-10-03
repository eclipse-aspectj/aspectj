public aspect AspectItd {
    //private interface Holder {}
    private int Holder.x;
    static class Inner {
        int doIt(Holder h) {
            return h.x++;
        }
    }
    
    public static void main(String []argv) {
    	new Inner().doIt(new HolderImpl());
    }
}

interface Holder{}

class HolderImpl implements Holder {}
