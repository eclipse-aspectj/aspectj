public class ReachableEmpty {
    public static void main(String[] args) { }

    public void m() {
        return;;
    }

    public int m1() {
        return 2;;
    }

    public void m2() {
        throw new Error();;
    }

    public void m3() {
        return;;;;;;
    }
}
