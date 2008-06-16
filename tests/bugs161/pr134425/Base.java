package pkg;

public aspect Base {
    public interface BaseBean {}
    public String BaseBean.describe() {
        return "Base holds "+super.toString();
    }
}
