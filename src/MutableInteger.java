/**
 * Created by IntelliJ IDEA.
 * User: karlicos
 * Date: 07.10.11
 * Time: 4:48
 * To change this template use File | Settings | File Templates.
 */
public class MutableInteger
{
    private int value;
    public MutableInteger()
    {
        value = 0;
    }
    public MutableInteger(int v)
    {
        value = v;
    }
    public int getValue()
    {
        return value;
    }
    public void setValue(int v)
    {
        value = v;
    }
    public void increment()
    {
        value++;
    }
    public String toString()
    {
        return Integer.toString(value);
    }
}
