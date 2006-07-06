public aspect AspectItd {
    //private interface Holder {}
    private int Holder.x;
    static aspect Inner {
        int doIt(Holder h) {
            return h.x++;
        }
    }
}

interface Holder{}
