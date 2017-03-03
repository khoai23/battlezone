package data.Item;

import java.io.Serializable;
import java.lang.reflect.Constructor;

/**
 * Created by Quan on 1/11/2017.
 */
public interface Item extends Serializable {
    public int getId();
    public String getType();
    public String getName();
    public String getDescription();
    public int getStock();
}
