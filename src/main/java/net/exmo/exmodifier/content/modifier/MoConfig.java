package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.util.exconfig;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public class MoConfig extends exconfig {
    public ModifierEntry.Type type = ModifierEntry.Type.UNKNOWN;
    public MoConfig(Path configFile) throws FileNotFoundException {
        super(configFile);
    }
}
