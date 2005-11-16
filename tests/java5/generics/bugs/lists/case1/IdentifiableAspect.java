public aspect IdentifiableAspect {
    declare parents: Bean implements LongIdentifiable;

    private Long LongIdentifiable.m_id;

    public Long LongIdentifiable.getId() {
        return m_id;
    }

    public void LongIdentifiable.setId(Long id) {
        m_id= id;
    }

  public static void main(String []argv) { 
    new Bean();
  }
}
