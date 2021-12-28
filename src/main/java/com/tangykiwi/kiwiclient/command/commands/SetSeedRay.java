package com.tangykiwi.kiwiclient.command.commands;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.tangykiwi.kiwiclient.KiwiClient;
import com.tangykiwi.kiwiclient.command.Command;
import com.tangykiwi.kiwiclient.modules.render.seedray.SeedRay;
import com.tangykiwi.kiwiclient.util.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.text.LiteralText;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SetSeedRay extends Command {
    public SetSeedRay() {
        super("seedray", "Sets the seed for seedray");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("seed", StringArgumentType.string()).executes(context -> {
            long seed;
            try {
                seed = Long.parseLong(context.getArgument("seed", String.class));
            } catch (Exception e) {
                seed = context.getArgument("seed", String.class).hashCode();
            }
            Utils.mc.inGameHud.getChatHud().addMessage(new LiteralText("Set SeedRay seed to " + seed));
            ((SeedRay) KiwiClient.moduleManager.getModule(SeedRay.class)).worldSeed = seed;

            return SINGLE_SUCCESS;
        }));
    }
}
