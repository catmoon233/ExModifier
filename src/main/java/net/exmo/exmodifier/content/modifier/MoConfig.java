package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.content.type.ExType;
import net.exmo.exmodifier.content.type.ItemType;
import net.exmo.exmodifier.util.ExConfig;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

public class MoConfig extends ExConfig {
    public ItemType type = ExType.UNKNOWN.get();
    public String CuriosType = "";
    public MoConfig(Path configFile) throws FileNotFoundException {
        super(configFile);
    }
    public MoConfig(Path configFile, InputStream stream) throws FileNotFoundException {
        super(configFile, stream);
    }
}
