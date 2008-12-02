package aa;

public aspect AdvisesC {
    int C.nothing() {
        return nothing();
    }
    
    before() : call(int C.nothing()) {
        
    }
}
