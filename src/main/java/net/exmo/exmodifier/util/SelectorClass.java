package net.exmo.exmodifier.util;

import net.exmo.exmodifier.content.selected.BaseItemSelected;
import net.exmo.exmodifier.selector.BaseItemSelector;


public  interface SelectorClass<T extends BaseItemSelector<?>> {
    T getModifierItemSelector();
}
