import java.util.List;

@interface AspectAnnotation {}
@interface InterfaceAnnotation {}
@interface ITDFieldAnnotation {}
@interface ITDMethodAnnotation {}
@interface MethodAnnotation {}
@interface AdviceAnnotation {}

@AspectAnnotation
public abstract aspect AnnotatingAspects {
	
	@InterfaceAnnotation
	public interface Observer {}
	
	@InterfaceAnnotation
	interface Subject {}
	
	@ITDFieldAnnotation
	private List<Observer> Subject.observers;
	
	@ITDMethodAnnotation
	public void Subject.addObserver(Observer o) {
		((List<Observer>)observers).add(o);		// this cast will not be needed when generics are fixed
	}
	
	@ITDMethodAnnotation
	public void Subject.removeObserver(Observer o) {
		observers.remove(o);
	}
	
	@MethodAnnotation
	private void notifyObservers(Subject subject) {
		for(Observer o : (List<Observer>)subject.observers)  // this cast will not be needed when generics are fixed 
			notifyObserver(o,subject);
	}
	
	@MethodAnnotation
	protected abstract void notifyObserver(Observer o, Subject s);
	
	protected abstract pointcut observedEvent(Subject subject);
	
	@AdviceAnnotation
	after(Subject subject) returning : observedEvent(subject) {
		notifyObservers(subject);
	}
	
	
}