package pkg;

public class Obj {
    public int m() {
        return 2;
    }
}

aspect Asp {
     int around(): target(Obj) && execution(int m()) {
        return 3;
    }
}
