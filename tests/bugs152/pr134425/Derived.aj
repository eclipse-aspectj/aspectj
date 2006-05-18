public aspect Derived {
    public interface DerivedBean extends Base.BaseBean {}

    public String DerivedBean.describe() {
        return "Derived state plus "+super.describe();
    }
    public static void main(String args[]) {
        new DerivedBean() {}.describe();
    }
}

aspect Base {
    public interface BaseBean {}
    public String BaseBean.describe() {
        return "Base holds "+super.toString();
    }
}