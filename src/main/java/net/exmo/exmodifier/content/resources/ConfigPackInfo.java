package net.exmo.exmodifier.content.resources;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

public record ConfigPackInfo(Path filePath, String name, String description, String author, String version,
                             ResourceLocation icon) {

}
