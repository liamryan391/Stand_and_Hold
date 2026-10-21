package com.liamryan.standandhold.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/** Pixel-bounded text without splitting Unicode code points; full text remains available to tooltips. */
public final class GuiTextLayout {
    private GuiTextLayout() { }
    public static String fit(String text, int width, ToIntFunction<String> measure) {
        if (width <= 0) { return ""; }
        if (measure.applyAsInt(text) <= width) { return text; }
        String suffix = "...";
        while (!suffix.isEmpty() && measure.applyAsInt(suffix) > width) { suffix = suffix.substring(1); }
        return prefix(text, Math.max(0, width - measure.applyAsInt(suffix)), measure) + suffix;
    }
    public static List<String> lines(String text, int width, int maximum, ToIntFunction<String> measure) {
        if (width <= 0 || maximum <= 0) { return List.of(); }
        List<String> result = new ArrayList<>();
        String remaining = text.replace('\n', ' ').replace('\r', ' ').strip();
        while (!remaining.isEmpty() && result.size() < maximum) {
            if (result.size() == maximum - 1 || measure.applyAsInt(remaining) <= width) {
                result.add(fit(remaining, width, measure)); break;
            }
            String line = prefix(remaining, width, measure);
            if (line.isEmpty()) { result.add(fit(remaining, width, measure)); break; }
            int space = line.lastIndexOf(' ');
            if (space > 0) { line = line.substring(0, space); }
            result.add(line);
            remaining = remaining.substring(line.length()).stripLeading();
        }
        return List.copyOf(result);
    }
    private static String prefix(String text, int width, ToIntFunction<String> measure) {
        int low = 0, high = text.codePointCount(0, text.length());
        while (low < high) {
            int mid = (low + high + 1) >>> 1;
            String part = text.substring(0, text.offsetByCodePoints(0, mid));
            if (measure.applyAsInt(part) <= width) { low = mid; } else { high = mid - 1; }
        }
        return text.substring(0, text.offsetByCodePoints(0, low));
    }
}
