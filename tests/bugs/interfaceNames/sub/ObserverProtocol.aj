package sub;

public abstract aspect ObserverProtocol {  
    protected interface Observer { }
    public void add(Observer o) {}
}
