import org.aspectj.testing.Tester;

public class SwitchStmtLocals {
    public static void main(String[] args) {
        new SwitchStmtLocals().realMain(args);
    }

    int i = 40;
    public void realMain(String[] args) {

        int val;

        val = 1;
        i = 40;
        switch (val) {
        case 0:  int   i = 10; break;
        case 1:  val = i = 20; break;
        default: val = i = 30; break;
        }
        Tester.checkEqual(val, 20);
        Tester.checkEqual(i, 40);

        val = 1;
        i = 40;
        switch (val) {
        case 0:  int   i = 10; break;
        case 1:
            switch (val-1) {
            case 0:  val = i = 20; break;
            default: val = i = 30; break;
            }
            break;
        default:       i = 30; break;
        }
        Tester.checkEqual(val, 20);
        Tester.checkEqual(i, 40);

        val = 1;
        i = 40;
        switch (val) {
        case 0:  int   i = 10; break;
        case 1:
            switch (val-1) {
            case 0:
                switch (val-1) {
                case 0:  val = i = 20; break;
                default: val = i = 30; break;
                }
                break;
            default: val = i = 30; break;
            }
            break;
        default:       i = 30; break;
        }
        Tester.checkEqual(val, 20);
        Tester.checkEqual(i, 40);
    }
}
