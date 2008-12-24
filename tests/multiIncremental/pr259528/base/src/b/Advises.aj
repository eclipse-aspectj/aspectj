package b;

public aspect Advises {
    before() : execution(public void IsAdvised.doNothing()) {
        
    }

    int IsAdvised.x;
}
