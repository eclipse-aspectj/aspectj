public class pr113445
{
    public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass)
    {
        return (T)new Object(); 
    }
}