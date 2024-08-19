package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.util.exconfig;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.nio.file.Path;

public class moconfig extends exconfig {
    public ModifierEntry.Type type = ModifierEntry.Type.UNKNOWN;
    public moconfig(Path configFile) throws FileNotFoundException {
        super(configFile);
    }
}
