import java.util.*;
//ObserverProtocol
public abstract aspect ObserverProtocol{
	 abstract pointcut stateChange(Subject subject);
	 
	 after(Subject subject):stateChange(subject){
	   Iterator it=subject.getObservers().iterator();
	   while(it.hasNext()){
	        Observer observer=(Observer)it.next();
	        observer.update();
	   }
	 }
 
	 private Collection Subject.observers=new ArrayList();
	 
	 public void Subject.addObserver(Observer observer){
	        observers.add(observer);
	 }
	 
	 public void Subject.removeObserver(Observer observer){
	       observers.remove(observer);
	 }
	 
	 public Collection Subject.getObservers()
	 {
	  return observers;
	 }
	 
	 public void Subject.setObservers(Collection c) {
		 observers = c;
	 }
	 
	 private Collection myCollection = new ArrayList();
	 
	 public Collection returnsCollectionMethod() { return myCollection; }
	 
	 public Collection C1.getCollection() {
		 return aCollection;
	 }
	 
	 public void C1.setCollection(Collection c) {
		 aCollection = c;
	 }
}

class C1 {
  public Collection aCollection;	
}