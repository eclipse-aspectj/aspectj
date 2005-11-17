public aspect IdentifiableAspect {
    declare parents: Bean implements LongIdentifiable;

    private T Identifiable<T>.m_id;

    public T Identifiable<T>.getId() {
        return m_id;
    }

    public void Identifiable<T>.setId(T id) {
        m_id= id;
    }

  public static void main(String []argv) {
    Bean b = new Bean();
    b.setId(37L);
    long l = b.getId();
    if (l!=37L) throw new RuntimeException("id should be 37");
  }
}
