package com.liamryan.standandhold.common.foundation;

public final class FoundationAmounts {
    private FoundationAmounts() { }

    public static long credit(long balance, long amount) {
        if (balance < 0 || amount < 0 || amount > Long.MAX_VALUE - balance) {
            throw new IllegalArgumentException("Reward would exceed the supported balance; nothing was changed.");
        }
        return balance + amount;
    }

    public static long debit(long balance, long amount) {
        if (balance < 0 || amount < 0 || amount > balance) {
            throw new IllegalArgumentException("Insufficient supplies: need " + amount + ", stored " + balance + ".");
        }
        return balance - amount;
    }
}
