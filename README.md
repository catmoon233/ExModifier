# ExModifier 1.21.1

Rewritten NeoForge 1.21.1 modifier runtime (`modid=exmodifier`). Public API: `net.exmo.exmodifier.api.ExModifierApi`.

Definitions load from datapack codecs. Item state is data components. Entity elements are NeoForge attachments.

JEI is optional (compile against API, client runtime jar). Curios is optional (reflection).

In-game pack editor: `/exmodifier pack` or the pack editor item. Suggestions include registered entries and entries already written in the current pack. `/exmodpack` is the same editor for the singleplayer owner. Apply writes a world datapack and reloads it.
