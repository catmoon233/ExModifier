package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.util.ExConfig;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public class MoConfig extends ExConfig {
    public ItemType type = ExType.UNKNOWN.get();
    public String CuriosType = "";
    public MoConfig(Path configFile) throws FileNotFoundException {
        super(configFile);
    }
}
