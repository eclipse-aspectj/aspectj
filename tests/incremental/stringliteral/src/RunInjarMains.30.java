
public aspect RunInjarMains {
    before() : execution(static void packageOne.main(String[])) {
        String[] args = new String[0];
        InjarOneMain.main(args);
        InjarTwoMain.main(args);
    }
}