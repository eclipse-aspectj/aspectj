import java.io.Serializable;

aspect MyList_Serializable {
  declare parents : MyList implements Serializable;

  private static final long MyList.serialVersionUID = 1L; // causes compiler failure
}