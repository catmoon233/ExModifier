package net.exmo.exmodifier.events;

import net.exmo.exmodifier.content.element.ExElement;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.minecraftforge.eventbus.api.Event;

public class OnElementRegisterEvent extends Event {
    public OnElementRegisterEvent() {
    }

    public OnElementRegisterEvent registerElement(ExElement element){
        ExElementHandle.registryExElement(element);
        return  this;
    }
}
