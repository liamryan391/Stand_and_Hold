package com.liamryan.standandhold.common.entity;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public final class ModEntities {
    private static int nextEntityId = 1;

    private ModEntities() {
    }

    public static void registerEntities() {
        registerSoldier();
    }

    private static void registerSoldier() {
        EntityRegistry.registerModEntity(
                new ResourceLocation(StandAndHoldConstants.MOD_ID, "soldier"),
                EntitySoldier.class,
                "soldier",
                nextEntityId++,
                StandAndHold.instance,
                64,
                3,
                true,
                0x2F4F3A,
                0xC8C0A8
        );
    }
}
