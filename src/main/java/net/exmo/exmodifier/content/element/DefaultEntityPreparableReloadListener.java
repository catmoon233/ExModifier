package net.exmo.exmodifier.content.element;

import static net.exmo.exmodifier.util.ExUtil.classToString;

public class DefaultEntityPreparableReloadListener extends AbstractReloadListener<DefaultEntityElement> {

    public DefaultEntityPreparableReloadListener() {
        super("default_entity_elements", 
            "loading default entity elements data...",
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