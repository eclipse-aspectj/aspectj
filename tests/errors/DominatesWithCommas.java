public class DominatesWithCommas {
    public static void main(String[] args) {
        new DominatesWithCommas().realMain(args);
    }
    public void realMain(String[] args) {
    }
}

aspect A dominates B,C{}
aspect B {}
aspect C {}
