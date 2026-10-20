package net.exmo.exmodifier.content.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.exmo.exmodifier.Exmodifier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Reads one token through whitespace, including {@code :} and {@code /}.
 * {@code StringArgumentType.word()} stops at the colon.
 */
public final class TokenArgument implements ArgumentType<String> {
    private static DeferredRegister<ArgumentTypeInfo<?, ?>> types;
    private static DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<TokenArgument>> tokenType;

    private TokenArgument() {}

    public static TokenArgument token() {
        return new TokenArgument();
    }

    public static void register(IEventBus bus) {
        if (types == null) {
            types = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Exmodifier.MODID);
            tokenType = types.register("token", () -> ArgumentTypeInfos.registerByClass(
                    TokenArgument.class, SingletonArgumentInfo.contextFree(TokenArgument::token)));
        }
        types.register(bus);
    }

    public static String get(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead() || Character.isWhitespace(reader.peek())) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedStartOfQuote().createWithContext(reader);
        }
        if (StringReader.isQuotedStringStart(reader.peek())) {
            return reader.readQuotedString();
        }
        int start = reader.getCursor();
        while (reader.canRead() && !Character.isWhitespace(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }
}
