package net.exmo.exmodifier.content.suit;

import net.exmo.exmodifier.content.element.DefaultEntityElement;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.exmo.exmodifier.util.AbstractReloadListener;

import static net.exmo.exmodifier.util.ExUtil.classToString;

public class ExSuitPreparableReloadListener extends AbstractReloadListener<DefaultEntityElement> {

    public ExSuitPreparableReloadListener() {
        super("suit",
            "loading ex suit data...",
            DefaultEntityElement.SERIALIZER::fromJson,
            (key, element) -> {
                if (!element.getEntityTypeString().isEmpty()) {
                    ExElementHandle.elementDefaultMap2.put(element.getEntityType(), element);
                }
            });
    }

    @Override
    public String getName() {
        return classToString(this.getClass());
    }
}