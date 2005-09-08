import java.util.*;
//Subject.java
interface Subject {
 public void addObserver(Observer observer);
 public void removeObserver(Observer observer);
 public Collection getObservers();
}