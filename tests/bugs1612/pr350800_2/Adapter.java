package test.aop;

public class Adapter extends AbstractAdapter<String> {

    @Override
    public String execute(String message) {
        return message;
    }
}
