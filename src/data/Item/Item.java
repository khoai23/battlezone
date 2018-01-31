package data.Item;

import data.Unit.Trait;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Created by Quan on 1/11/2017.
 */
public interface Item extends Serializable {
    int getId();
    String getType();
    String getName();
    String getDescription();
    int getStock();

    List<Trait> getItemTraits();
}
