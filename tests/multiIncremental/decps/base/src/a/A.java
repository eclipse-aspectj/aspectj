package a;
import java.io.*;

public aspect A {
  declare parents: p.C implements Serializable,Goo;
}

interface Goo {}
