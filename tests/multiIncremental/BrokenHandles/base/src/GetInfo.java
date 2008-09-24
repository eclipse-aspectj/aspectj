package p;

aspect GetInfo {
   declare warning : set(int Demo.x) : "field set";
   declare warning : set(int Demo.x) : "field set";
   declare parents : Demo implements Serializable;
}
