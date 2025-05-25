package net.exmo.exmodifier.content.element;

import net.exmo.exmodifier.util.AbstractReloadListener;

import static net.exmo.exmodifier.util.ExUtil.classToString;

public class DefaultItemPreparableReloadListener extends AbstractReloadListener<DefaultItemElement> {

    public DefaultItemPreparableReloadListener() {
        super("default_elements", 
            "loading default item elements data...",
            DefaultItemElement.EX_SERIALIZE::fromJson,
            (key, element) -> {
                if (element.getItemSelector() != null) {
                    ExElementHandle.elementDefaultMap.put(element.itemSelector(), element);
                }
            });
    }

    @Override
    public String getName() {
        return classToString(this.getClass());
    }
}