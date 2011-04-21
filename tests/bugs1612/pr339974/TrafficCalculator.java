public aspect TrafficCalculator {

    public static class City.TrafficCalculator {
        Function<City, Time> EXTREME = createExtremeTraffic(); 
        Function<City, Time> BASIC = createBasicTraffic();
    }


    private static Function<City, Time> createExtremeTraffic() {
        return null;
    } 
    private static Function<City, Time> createBasicTraffic() {
        return null;
    } 

    public static class Time { } 


}
class Function<A,B> {}
