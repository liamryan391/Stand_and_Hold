package com.liamryan.standandhold.common.progression;

import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import net.minecraft.server.MinecraftServer;

public final class HumanPointManager {
    private HumanPointManager() {
    }

    public static Progression status(MinecraftServer server) {
        HumanWorldData data = HumanWorldData.get(server);
        data.requireSupportedVersion();
        HumanStage stage = ProgressionRules.stageFor(data.points(), HumanProgressionConfig.thresholds());
        data.setProgression(data.points(), stage);
        return new Progression(data.points(), stage);
    }

    public static Progression addPoints(MinecraftServer server, long amount) {
        HumanWorldData data = HumanWorldData.get(server);
        data.requireSupportedVersion();
        long points = ProgressionRules.addExact(data.points(), amount);
        return update(data, points);
    }

    public static Progression setStage(MinecraftServer server, HumanStage stage) {
        HumanWorldData data = HumanWorldData.get(server);
        data.requireSupportedVersion();
        return update(data, HumanProgressionConfig.thresholds()[stage.level()]);
    }

    private static Progression update(HumanWorldData data, long points) {
        HumanStage stage = ProgressionRules.stageFor(points, HumanProgressionConfig.thresholds());
        data.setProgression(points, stage);
        return new Progression(points, stage);
    }

    public record Progression(long points, HumanStage stage) {
    }
}
