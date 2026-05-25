package com.liamryan.standandhold.common.supply;

import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.world.World;

public final class SupplyTransferManager {
    private SupplyTransferManager() {
    }

    public static TransferResult transferToLocal(World world, ISupplyStorage storage) {
        if (!StandAndHoldConfig.supply.enableGuiSupplyTransfers) {
            return TransferResult.disabled(storage);
        }

        if (world == null || world.isRemote || storage == null) {
            return TransferResult.noTransfer(TransferStatus.NO_STORAGE, storage);
        }

        int globalSupplies = SupplyManager.getSupplyPoints(world);
        if (globalSupplies <= 0) {
            return TransferResult.noTransfer(TransferStatus.GLOBAL_EMPTY, storage);
        }

        int localCapacity = storage.getMaxStoredSupplies() - storage.getStoredSupplies();
        if (localCapacity <= 0) {
            return TransferResult.noTransfer(TransferStatus.LOCAL_FULL, storage);
        }

        int requestedAmount = getTransferAmount();
        int moved = Math.min(requestedAmount, Math.min(globalSupplies, localCapacity));
        if (!SupplyManager.spendSupplies(world, moved, "building supply import: " + storage.getPos())) {
            return TransferResult.noTransfer(TransferStatus.GLOBAL_EMPTY, storage);
        }

        int accepted = storage.addStoredSupplies(moved);
        if (accepted < moved) {
            SupplyManager.addSupplies(world, moved - accepted, "building supply import rollback: " + storage.getPos());
        }
        return TransferResult.transferred(TransferStatus.TRANSFERRED_TO_LOCAL, accepted, storage, SupplyManager.getSupplyPoints(world));
    }

    public static TransferResult transferToGlobal(World world, ISupplyStorage storage) {
        if (!StandAndHoldConfig.supply.enableGuiSupplyTransfers) {
            return TransferResult.disabled(storage);
        }

        if (world == null || world.isRemote || storage == null) {
            return TransferResult.noTransfer(TransferStatus.NO_STORAGE, storage);
        }

        int localSupplies = storage.getStoredSupplies();
        if (localSupplies <= 0) {
            return TransferResult.noTransfer(TransferStatus.LOCAL_EMPTY, storage);
        }

        int moved = storage.removeStoredSupplies(Math.min(getTransferAmount(), localSupplies));
        if (moved <= 0) {
            return TransferResult.noTransfer(TransferStatus.LOCAL_EMPTY, storage);
        }

        int globalSupplies = SupplyManager.addSupplies(world, moved, "building supply export: " + storage.getPos());
        return TransferResult.transferred(TransferStatus.TRANSFERRED_TO_GLOBAL, moved, storage, globalSupplies);
    }

    private static int getTransferAmount() {
        return Math.max(1, StandAndHoldConfig.supply.guiSupplyTransferAmount);
    }

    public enum TransferStatus {
        TRANSFERRED_TO_LOCAL,
        TRANSFERRED_TO_GLOBAL,
        DISABLED,
        GLOBAL_EMPTY,
        LOCAL_EMPTY,
        LOCAL_FULL,
        NO_STORAGE
    }

    public static final class TransferResult {
        private final TransferStatus status;
        private final int movedSupplies;
        private final int storedSupplies;
        private final int maxStoredSupplies;
        private final int globalSupplies;

        private TransferResult(TransferStatus status, int movedSupplies, int storedSupplies, int maxStoredSupplies, int globalSupplies) {
            this.status = status;
            this.movedSupplies = movedSupplies;
            this.storedSupplies = storedSupplies;
            this.maxStoredSupplies = maxStoredSupplies;
            this.globalSupplies = globalSupplies;
        }

        private static TransferResult transferred(TransferStatus status, int movedSupplies, ISupplyStorage storage, int globalSupplies) {
            return new TransferResult(status, Math.max(0, movedSupplies), getStoredSupplies(storage), getMaxStoredSupplies(storage), Math.max(0, globalSupplies));
        }

        private static TransferResult disabled(ISupplyStorage storage) {
            return noTransfer(TransferStatus.DISABLED, storage);
        }

        private static TransferResult noTransfer(TransferStatus status, ISupplyStorage storage) {
            return new TransferResult(status, 0, getStoredSupplies(storage), getMaxStoredSupplies(storage), 0);
        }

        private static int getStoredSupplies(ISupplyStorage storage) {
            return storage == null ? 0 : storage.getStoredSupplies();
        }

        private static int getMaxStoredSupplies(ISupplyStorage storage) {
            return storage == null ? 0 : storage.getMaxStoredSupplies();
        }

        public TransferStatus getStatus() {
            return status;
        }

        public int getMovedSupplies() {
            return movedSupplies;
        }

        public int getStoredSupplies() {
            return storedSupplies;
        }

        public int getMaxStoredSupplies() {
            return maxStoredSupplies;
        }

        public int getGlobalSupplies() {
            return globalSupplies;
        }
    }
}
