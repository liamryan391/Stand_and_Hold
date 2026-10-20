package com.liamryan.standandhold.common.structure;

public enum StructureKind {
    CHECKPOINT(0x53414801L), MAIN_BASE(0x53414802L);
    private final long salt;
    StructureKind(long salt) { this.salt = salt; }
    public long salt() { return salt; }
}
