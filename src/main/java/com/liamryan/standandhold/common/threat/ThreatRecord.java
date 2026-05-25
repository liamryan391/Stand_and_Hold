package com.liamryan.standandhold.common.threat;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public final class ThreatRecord {
    private static final String TAG_DIMENSION = "Dimension";
    private static final String TAG_REGION_X = "RegionX";
    private static final String TAG_REGION_Z = "RegionZ";
    private static final String TAG_THREAT_SCORE = "ThreatScore";
    private static final String TAG_PARASITE_KILLS = "ParasiteKills";
    private static final String TAG_HUMAN_LOSSES = "HumanLosses";
    private static final String TAG_OUTPOST_ATTACKS = "OutpostAttacks";
    private static final String TAG_REINFORCEMENTS_SENT = "ReinforcementsSent";
    private static final String TAG_LAST_EVENT_WORLD_TIME = "LastEventWorldTime";
    private static final String TAG_LAST_REINFORCEMENT_WORLD_TIME = "LastReinforcementWorldTime";
    private static final String TAG_LAST_OUTPOST_ATTACK_WORLD_TIME = "LastOutpostAttackWorldTime";
    private static final String TAG_LAST_DECAY_WORLD_TIME = "LastDecayWorldTime";

    private final int dimension;
    private final int regionX;
    private final int regionZ;
    private int threatScore;
    private int parasiteKills;
    private int humanLosses;
    private int outpostAttacks;
    private int reinforcementsSent;
    private long lastEventWorldTime = -1L;
    private long lastReinforcementWorldTime = -1L;
    private long lastOutpostAttackWorldTime = -1L;
    private long lastDecayWorldTime = -1L;

    public ThreatRecord(int dimension, int regionX, int regionZ) {
        this.dimension = dimension;
        this.regionX = regionX;
        this.regionZ = regionZ;
    }

    public static ThreatRecord readFromNBT(NBTTagCompound compound) {
        ThreatRecord record = new ThreatRecord(
                compound.getInteger(TAG_DIMENSION),
                compound.getInteger(TAG_REGION_X),
                compound.getInteger(TAG_REGION_Z)
        );
        record.threatScore = Math.max(0, compound.getInteger(TAG_THREAT_SCORE));
        record.parasiteKills = Math.max(0, compound.getInteger(TAG_PARASITE_KILLS));
        record.humanLosses = Math.max(0, compound.getInteger(TAG_HUMAN_LOSSES));
        record.outpostAttacks = Math.max(0, compound.getInteger(TAG_OUTPOST_ATTACKS));
        record.reinforcementsSent = Math.max(0, compound.getInteger(TAG_REINFORCEMENTS_SENT));
        record.lastEventWorldTime = compound.hasKey(TAG_LAST_EVENT_WORLD_TIME) ? compound.getLong(TAG_LAST_EVENT_WORLD_TIME) : -1L;
        record.lastReinforcementWorldTime = compound.hasKey(TAG_LAST_REINFORCEMENT_WORLD_TIME) ? compound.getLong(TAG_LAST_REINFORCEMENT_WORLD_TIME) : -1L;
        record.lastOutpostAttackWorldTime = compound.hasKey(TAG_LAST_OUTPOST_ATTACK_WORLD_TIME) ? compound.getLong(TAG_LAST_OUTPOST_ATTACK_WORLD_TIME) : -1L;
        record.lastDecayWorldTime = compound.hasKey(TAG_LAST_DECAY_WORLD_TIME) ? compound.getLong(TAG_LAST_DECAY_WORLD_TIME) : record.lastEventWorldTime;
        return record;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(TAG_DIMENSION, dimension);
        compound.setInteger(TAG_REGION_X, regionX);
        compound.setInteger(TAG_REGION_Z, regionZ);
        compound.setInteger(TAG_THREAT_SCORE, threatScore);
        compound.setInteger(TAG_PARASITE_KILLS, parasiteKills);
        compound.setInteger(TAG_HUMAN_LOSSES, humanLosses);
        compound.setInteger(TAG_OUTPOST_ATTACKS, outpostAttacks);
        compound.setInteger(TAG_REINFORCEMENTS_SENT, reinforcementsSent);
        compound.setLong(TAG_LAST_EVENT_WORLD_TIME, lastEventWorldTime);
        compound.setLong(TAG_LAST_REINFORCEMENT_WORLD_TIME, lastReinforcementWorldTime);
        compound.setLong(TAG_LAST_OUTPOST_ATTACK_WORLD_TIME, lastOutpostAttackWorldTime);
        compound.setLong(TAG_LAST_DECAY_WORLD_TIME, lastDecayWorldTime);
        return compound;
    }

    public void recordParasiteKill(int threatIncrease, long worldTime, int maxThreatScore) {
        parasiteKills++;
        addThreat(threatIncrease, worldTime, maxThreatScore);
    }

    public void recordHumanLoss(int threatIncrease, long worldTime, int maxThreatScore) {
        humanLosses++;
        addThreat(threatIncrease, worldTime, maxThreatScore);
    }

    public void recordOutpostAttack(int threatIncrease, long worldTime, int maxThreatScore) {
        outpostAttacks++;
        lastOutpostAttackWorldTime = worldTime;
        addThreat(threatIncrease, worldTime, maxThreatScore);
    }

    public void recordReinforcements(int count, long worldTime) {
        if (count > 0) {
            reinforcementsSent += count;
            lastReinforcementWorldTime = worldTime;
        }
    }

    public boolean decayThreat(int decayAmount, long worldTime) {
        lastDecayWorldTime = worldTime;
        int safeDecayAmount = Math.max(0, decayAmount);
        if (safeDecayAmount <= 0 || threatScore <= 0) {
            return false;
        }

        int newThreatScore = Math.max(0, threatScore - safeDecayAmount);
        if (newThreatScore == threatScore) {
            return false;
        }

        threatScore = newThreatScore;
        return true;
    }

    public boolean resetThreat(long worldTime) {
        lastDecayWorldTime = worldTime;
        if (threatScore == 0) {
            return false;
        }

        threatScore = 0;
        return true;
    }

    private void addThreat(int threatIncrease, long worldTime, int maxThreatScore) {
        if (threatIncrease > 0) {
            threatScore = Math.min(Math.max(1, maxThreatScore), threatScore + threatIncrease);
        }
        lastEventWorldTime = worldTime;
        if (lastDecayWorldTime < 0L) {
            lastDecayWorldTime = worldTime;
        }
    }

    public String getKey() {
        return getKey(dimension, regionX, regionZ);
    }

    public static String getKey(int dimension, int regionX, int regionZ) {
        return dimension + ":" + regionX + ":" + regionZ;
    }

    public int getDimension() {
        return dimension;
    }

    public int getRegionX() {
        return regionX;
    }

    public int getRegionZ() {
        return regionZ;
    }

    public int getThreatScore() {
        return threatScore;
    }

    public ThreatLevel getThreatLevel() {
        return ThreatLevel.fromScore(threatScore);
    }

    public int getParasiteKills() {
        return parasiteKills;
    }

    public int getHumanLosses() {
        return humanLosses;
    }

    public int getOutpostAttacks() {
        return outpostAttacks;
    }

    public int getReinforcementsSent() {
        return reinforcementsSent;
    }

    public long getLastEventWorldTime() {
        return lastEventWorldTime;
    }

    public long getLastReinforcementWorldTime() {
        return lastReinforcementWorldTime;
    }

    public long getLastOutpostAttackWorldTime() {
        return lastOutpostAttackWorldTime;
    }

    public long getLastDecayWorldTime() {
        return lastDecayWorldTime;
    }

    public BlockPos getApproximateCenterBlock(int regionChunkSize) {
        int safeRegionChunkSize = Math.max(1, regionChunkSize);
        int centerChunkX = regionX * safeRegionChunkSize + safeRegionChunkSize / 2;
        int centerChunkZ = regionZ * safeRegionChunkSize + safeRegionChunkSize / 2;
        return new BlockPos((centerChunkX << 4) + 8, 64, (centerChunkZ << 4) + 8);
    }
}
