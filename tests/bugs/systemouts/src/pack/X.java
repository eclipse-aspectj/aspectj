
package pack;

public final class X {

    public static void ut(boolean b, String m) {
        if (!b) {
            try {
                if (b)
                    return;
            } catch (RuntimeException e) {
                System.out.println("");
                e.printStackTrace(System.out);
            }
        }
    }
    
    
    public static void printArgs(String programName, String[] args) {
        System.out.print(programName);
        for (int i=0; i<args.length; i++){
            System.out.print(" ");
            System.out.print(args[i]);
        }
    }
    static void t() {
        System.out.print("s");
        System.out.flush();
        System.out.print('.');
        System.out.flush();
        System.out.print('.');
        System.out.flush();
        System.out.print('.');
        System.out.flush();
        System.out.print('.');
        System.out.flush();
        System.out.print('.');
        System.out.flush();
        System.out.print('.');
        System.out.flush();
        System.out.println("");        
    }
}
