public class AfterReturningResult {
    public static void main (String[] args) {
        new CFCommandProcessor().run();
    }
}

class CFCommand {
    void handleResponse() {}
    void updateCache() { System.err.println("updating cache");} }

class CFCommandProcessor {
    public void run() {
        new CFCommand().handleResponse();
    }
}

aspect A {
    pointcut response(CFCommand cmd) : within(CFCommandProcessor) &&
                                       target(cmd) &&
                                       call(void CFCommand.handleResponse (..));

    after(CFCommand cmd) returning: response(cmd) {
        cmd.updateCache();
    }
}


aspect B {
    Object around(): execution(void run()) {
        return proceed();
    }
}
