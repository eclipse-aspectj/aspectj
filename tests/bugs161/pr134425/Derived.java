package pkg;

import pkg.Base.BaseBean;

public aspect Derived {
    public interface DerivedBean extends BaseBean {}

    public String DerivedBean.describe() {
        return "Derived state plus "+super.describe();
    }
    public static void main(String args[]) {
        new DerivedBean() {}.describe();
    }
}
