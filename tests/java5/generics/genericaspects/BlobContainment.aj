public aspect BlobContainment extends ParentChildRelationship<Blob,Blob> {

  public static void main(String []argv) {
    Blob a = new Blob();
    Blob b = new Blob();
    Blob c = new Blob();
    Blob d = new Blob();
    Blob e = new Blob();

    // arrange as follows: A contains B,C,D and B contains E

    a.addChild(b);
    a.addChild(c);
    a.addChild(d);
    b.addChild(e);

    // now query the layout

/*
    if (!e.getParent().equals(b)) 
      throw new RuntimeException("why is E not parent of B? "+e.getParent());
    if (!d.getParent().equals(a)) 
      throw new RuntimeException("why is A not parent of D? "+d.getParent());
    if (a.getChildren().size()!=3)
      throw new RuntimeException("A should have 3 children, not:"+a.getChildren().size());
*/
  }

}
