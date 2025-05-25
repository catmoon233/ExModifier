package net.exmo.exmodifier.content.element;

import net.exmo.exmodifier.util.AbstractReloadListener;

import static net.exmo.exmodifier.util.ExUtil.classToString;


public class ElementPreparableReloadListener extends AbstractReloadListener<ExElement> {

    public ElementPreparableReloadListener() {
        super("elements",
            "loading exElement data...",
            ExElement.EX_SERIALIZE::fromJson,
            (key, element) -> ExElementHandle.registryExElement(element)
        );
    }

    @Override
    public String getName() {
        return classToString(this.getClass());
    }
}