interface StatisticsType {
}

public class StatisticsTypeImpl implements StatisticsType {
    static class UIStatisticsType extends UofwStatisticsType {
    };

    public static void main(String argz[]) {
        System.out.println(new UIStatisticsType().toString());
    }
}

class UofwStatisticsType extends StatisticsTypeImpl {
}