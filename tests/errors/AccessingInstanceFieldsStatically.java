public class AccessingInstanceFieldsStatically {
    public static void main(String[] args) {
        new AccessingInstanceFieldsStatically().realMain(args);
    }
    public void realMain(String[] args) {
    }
}

class T {
    public void printIt() {}

    public int getJ() { return -1; }

    public static void m() {
        Object o = this; //ERROR static reference to this
        this.clay++;     //ERROR static reference to instance field
        clay++;          //ERROR static reference to instance field
        printIt();       //ERROR static reference to instance method
    }

    public T(int i, int j) {
        clay = i;
    }

    public T() {
        this(clay,     //ERROR static reference to instance field
             getJ());  //ERROR static reference to instance method
        clay++;
        getJ();
        1+1; //ERROR not a legal statement
    }
}

aspect TAspect {
    int T.clay = 0;
    void around (T tt):
        target(tt) && call(void printIt()) {
        T.clay = 1; // ERROR static reference to instance field
        T.getJ(); //ERROR static reference to instance method
    }
}

