package com.liamryan.standandhold.common.equipment;

public enum EquipmentKind {
    BLADE("anti_threat_blade", 1, "standandhold:parasite_samples"),
    ARMY("army", 1, "standandhold:field_communications"),
    ELITE("elite", 3, "standandhold:field_communications"),
    PROTOTYPE("prototype_launcher", 1, "standandhold:field_communications");

    public final String id, research;
    public final int stage;
    EquipmentKind(String id, int stage, String research) { this.id = id; this.stage = stage; this.research = research; }
}
