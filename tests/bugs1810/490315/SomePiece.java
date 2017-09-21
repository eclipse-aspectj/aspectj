package test;

public class SomePiece<T> {

    private T data;
    private boolean last;
    private Long totalCount;

    public SomePiece(T data, boolean last) {
        this.data = data;
        this.last = last;
    }

    public T getData() {
        return data;
    }

    public boolean isLast() {
        return last;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

}
