class CFlowThreads implements Runnable {
    private static final int MAX_VALUE = 100;

    public static void main(String[] argv) {
        // Create a lot of threads
        int x = 0;
        while (x++ < 100) {
            new Thread(new CFlowThreads()).start();
            try {
                Thread.sleep((long)(Math.random()*100));
            } catch (Exception e) {}
        }
    }

    public boolean gotOne(int x) {
//        System.out.println("Succeeded, x = "+x);
        return true;
    }

    public boolean methodA(int x) {
        if (x % 2 == 0) return methodB(x+3);
        else return methodC(x/2);
    }

    public boolean methodB(int x) {
        if (x % 3 == 0) return methodC(x+4);
        else return methodA(x/3);
    }

    public boolean methodC(int x) {
        if (x > 2) return methodB(x-3);
        else return gotOne(x);
    }

    public boolean startIt(int x) {
        return methodA(x);
    }

    public void run() {
        startIt((int)(Math.random()*MAX_VALUE));
    }

    static aspect OriginalValue of eachcflow(OriginalValue.entrypoints(int)) {
        int recursioncount = 0;
        int original;

        pointcut entrypoints(int x):
            instanceof(CFlowThreads) && receptions(boolean startIt(x));

        before(int x): entrypoints(x) {
            original = x;
        }

        pointcut successes(int x):
            instanceof(CFlowThreads) && receptions(boolean gotOne(x));

        pointcut recursiveCalls(int x):
            instanceof(CFlowThreads) &&
            receptions(boolean *(x)) && !receptions(boolean gotOne(x));

        after(int x) returning (boolean s): successes(x) {
//            System.out.println("Started at "+original+", used "+recursioncount+" hops");
        }

        boolean fail(int x) {
//            System.out.println("Never gonna make it from "+original);
            return false;
        }

        around(int x) returns boolean: recursiveCalls(x) {
            if (recursioncount++ > 50) {
                return fail(x);
            } else {
                return(proceed(x));
            }
        }
    }
    static aspect CheckValues {
        static int[] hops = new int[MAX_VALUE];
        static after(int x, OriginalValue a) returning (boolean s):
            OriginalValue.successes(x) && hasaspect(a) {
            synchronized (hops) {
                if (hops[a.original] != 0) {
                    if (hops[a.original] != a.recursioncount) {
                        System.out.println("Error: "+a.original+" takes "+
                            hops[a.original]+", not "+
                            a.recursioncount+" hops.");
                    }
                } else {
                    hops[a.original] = a.recursioncount;
                }
            }
        }
        static after(int x, OriginalValue a) returning (boolean s):
            instanceof(a) && receptions(boolean fail(x)) {
            synchronized (hops) {
                if (hops[a.original] != 0 &&&& hops[a.original] != -1) {
                    System.out.println("Error: "+a.original+" takes "+
                        hops[a.original]+" hops, doesn't fail");
                } else {
                    hops[a.original] = -1;
                }
            }
        }
    }
}
