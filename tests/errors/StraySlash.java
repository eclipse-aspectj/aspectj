import org.aspectj.testing.*;

public class StraySlash {
    public static void main(String[] args) {
        new StraySlash().realMain(args);
    }
    
    public void realMain(String[] args) {
        \
        /
        &
        *
        (
         )
         @
        #
        $
            %
            ^
        Tester.check(false, "Shouldn't have compiled");
    }
}

