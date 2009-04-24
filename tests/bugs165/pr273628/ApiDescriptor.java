package p;

public enum ApiDescriptor {
    TARGET_CLASS_TARGET_METHOD(999);

    ApiDescriptor(int number) {
    	this.number = number;
    }

    public final int number;
}
