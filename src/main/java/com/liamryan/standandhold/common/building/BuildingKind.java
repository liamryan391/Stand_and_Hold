package com.liamryan.standandhold.common.building;

public enum BuildingKind {
    COMMAND_POST("field_command_post"), LAB("research_lab"), CRATE("supply_crate");

    private final String id;
    BuildingKind(String id) { this.id = id; }
    public String id() { return id; }
}
