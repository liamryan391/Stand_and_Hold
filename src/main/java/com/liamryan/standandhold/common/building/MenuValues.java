package com.liamryan.standandhold.common.building;

/** Lossless long encoding for vanilla signed-short container data packets. */
public final class MenuValues {
    private MenuValues() { }
    public static int limb(long value, int limb) { return (int) (value >>> (limb * 16)) & 0xffff; }
    public static long accept(long old, int limb, int value) {
        int shift = limb * 16;
        return old & ~(0xffffL << shift) | ((long) value & 0xffffL) << shift;
    }
}
