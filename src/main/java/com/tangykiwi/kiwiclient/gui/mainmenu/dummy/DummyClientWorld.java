package com.tangykiwi.kiwiclient.gui.mainmenu.dummy;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.dimension.DimensionType;

import java.util.OptionalLong;

public class DummyClientWorld extends ClientWorld {

    private static DummyClientWorld instance;

    public static DummyClientWorld getInstance() {
        if (instance == null) instance = new DummyClientWorld();
        return instance;
    }

    private DummyClientWorld() {
        super(DummyClientPlayNetworkHandler.getInstance(), new Properties(Difficulty.EASY, false, true), null, DimensionType.create(OptionalLong.empty(), true, false, false, true, 1.0D, false, false, true, false, true, 0, 256, 256, BlockTags.INFINIBURN_OVERWORLD.getId(), DimensionType.OVERWORLD_ID, 0.0F), 0, 0, instance.getProfilerSupplier(), MinecraftClient.getInstance().worldRenderer, false, 0);
    }
}
