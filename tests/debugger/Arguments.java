//package debugger;

public class Arguments {
    String[] args;

    public static void main(String[] args) {
        new Arguments(args).go();
    }

    Arguments(String[] args) {
        this.args = args;
    }

    void go () {
        int i = -1;
        String s = "";
        while ((++i) < args.length) {
            s = args[i];
            System.out.println("[" + i + "] " + s);
        }
    }
}
