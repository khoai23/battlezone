package data.Item;

import java.lang.reflect.Constructor;

/**
 * Created by Quan on 1/11/2017.
 */
public interface Item {
    public int getId();
    public String getType();
    public String getName();
    public String getDescription();
    public int getStock();
}
