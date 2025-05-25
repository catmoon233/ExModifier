package net.exmo.exmodifier.content.suit;

import net.exmo.exmodifier.content.element.DefaultEntityElement;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.exmo.exmodifier.util.AbstractReloadListener;

import java.io.FileNotFoundException;

import static net.exmo.exmodifier.util.ExUtil.classToString;

public class ExSuitPreparableReloadListener extends AbstractReloadListener<ExSuit> {

    public ExSuitPreparableReloadListener() {
        super("suit",
            "loading ex suit data...",
            e-> {
                try {
                    return ExSuitHandle.processMoConfigEntries(e);
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            },
            (key, suit) -> {
                if (suit !=null) {
                    ExSuitHandle.registerExSuit(suit);
                }
            });
    }

    @Override
    public String getName() {
        return classToString(this.getClass());
    }
}