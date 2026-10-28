package com.liamryan.standandhold.common.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** One synchronous bounded attempt. Its adapter is also used by isolated fault-injection GameTests. */
public final class ExpansionApply {
    public interface Access {
        boolean valid();
        CompoundTag read(BlockPos pos);
        boolean write(ExpansionOperation.Change change);
        String protectedDigest();
        void store(ExpansionOperation operation);
    }
    public record Result(boolean success, String message) { }
    private ExpansionApply() { }
    public static Result apply(ExpansionOperation operation, Access access) {
        if (operation.phase() != ExpansionOperation.Phase.PREPARED || !access.valid()) { throw new IllegalArgumentException("Expansion requires a fresh validated prepared intent."); }
        access.store(operation); // Intent is saved before any block write; setChanged is deliberately not called fsync.
        int index = 0;
        try {
            for (; index < operation.changes().size(); index++) {
                var cell = operation.changes().get(index);
                if (!access.valid() || !access.read(cell.pos()).equals(cell.before())) { throw new IllegalStateException("Expected-before conflict at " + cell.pos().toShortString()); }
                if (!access.write(cell) || !access.read(cell.pos()).equals(cell.after())) { throw new IllegalStateException("Refused/changed write at " + cell.pos().toShortString()); }
            }
            if (!access.valid() || operation.observe(access::read).state() != ExpansionOperation.Observed.ALL_AFTER
                    || !operation.protectedDigest().equals(access.protectedDigest())) { throw new IllegalStateException("Final delta/protected-footprint mismatch"); }
            access.store(operation.withPhase(ExpansionOperation.Phase.APPLIED, 137, "All 137 cells verified; no extra cost/reward."));
            return new Result(true, "Apron applied once: 137 cells; buildings/storage/caps unchanged. Operation " + operation.id());
        } catch (RuntimeException failure) {
            String detail = "Cell " + index + ": " + failure.getMessage();
            try { access.store(operation.withPhase(ExpansionOperation.Phase.REVIEW, index, detail)); }
            catch (RuntimeException receiptFailure) { com.mojang.logging.LogUtils.getLogger().error("Expansion {} lost its writable anchor; no reconstruction/rollback attempted", operation.id(), receiptFailure); }
            com.mojang.logging.LogUtils.getLogger().warn("Expansion {} stopped at {}: {}. No rollback/replay.", operation.id(), index, failure.toString());
            return new Result(false, "Expansion stopped; operation " + operation.id() + ", " + detail.substring(0, Math.min(160, detail.length())) + ". Inspect outpost status/backup; no automatic repair.");
        }
    }
}
