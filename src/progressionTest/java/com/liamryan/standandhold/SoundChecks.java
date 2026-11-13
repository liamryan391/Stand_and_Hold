package com.liamryan.standandhold;

import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.sounds.JOrbisAudioStream;

/** M33: every sound event resolves to original mono Vorbis files the game's own decoder can read, with subtitles. */
final class SoundChecks {
    private static int assertions;
    /** Event -> {min, max} decoded seconds. */
    private static final Map<String, double[]> EVENTS = Map.of("npc.gear", new double[] {0.1, 0.6},
            "launcher.fire", new double[] {0.3, 1.2}, "post.radio", new double[] {1.0, 3.6}, "post.radio_urgent", new double[] {0.6, 2.2},
            "rifle.fire", new double[] {0.3, 1.0});
    /** M35 (Liam's playtest): the kit layer must stay low; M33's clinks measured about 4.2 kHz. Radio stays in the voice band; the rifle report keeps its low boom (about 2.6 kHz). */
    private static final Map<String, double[]> CENTROID_HZ = Map.of("npc.gear", new double[] {150, 1600},
            "post.radio", new double[] {600, 1800}, "post.radio_urgent", new double[] {800, 2600},
            "rifle.fire", new double[] {1800, 3400});
    static void run() throws Exception {
        Path assets = Path.of("src/main/resources/assets/standandhold");
        var json = JsonParser.parseString(Files.readString(assets.resolve("sounds.json"))).getAsJsonObject();
        var lang = JsonParser.parseString(Files.readString(assets.resolve("lang/en_us.json"))).getAsJsonObject();
        String source = Files.readString(Path.of("src/main/java/com/liamryan/standandhold/common/sound/StandAndHoldSounds.java"));
        check(json.keySet().equals(EVENTS.keySet()), "sounds.json declares exactly the registered events");
        Set<Path> used = new HashSet<>();
        for (var event : EVENTS.entrySet()) {
            var entry = json.getAsJsonObject(event.getKey());
            check(source.contains("sound(\"" + event.getKey() + "\")"), "event registered in code: " + event.getKey());
            check(lang.has(entry.get("subtitle").getAsString()), "subtitle translated: " + event.getKey());
            for (var raw : entry.getAsJsonArray("sounds")) {
                String id = raw.getAsString();
                check(id.startsWith("standandhold:") && !id.contains(".."), "namespaced sound path");
                Path file = assets.resolve("sounds/" + id.substring("standandhold:".length()) + ".ogg");
                check(Files.isRegularFile(file), "sound file exists: " + file);
                used.add(file.normalize());
                try (InputStream in = Files.newInputStream(file); var stream = new JOrbisAudioStream(in)) {
                    var format = stream.getFormat();
                    check(format.getChannels() == 1, "mono, so Minecraft can position it: " + id);
                    check(format.getSampleRate() == 44100F, "44.1 kHz: " + id);
                    long[] samples = {0}; float[] peak = {0};
                    var decoded = new java.util.ArrayList<Float>();
                    while (stream.readChunk(value -> { samples[0]++; peak[0] = Math.max(peak[0], Math.abs(value)); decoded.add(value); })) { }
                    var band = CENTROID_HZ.get(event.getKey());
                    if (band != null) {
                        double centroid = centroid(decoded);
                        check(centroid >= band[0] && centroid <= band[1], "spectral centroid of " + id + " is " + Math.round(centroid) + " Hz");
                    }
                    double seconds = samples[0] / 44100.0;
                    check(seconds >= event.getValue()[0] && seconds <= event.getValue()[1], "duration in range for " + id + ": " + seconds);
                    check(peak[0] > 0.05F && peak[0] <= 1.0F, "audible and not clipped: " + id);
                }
            }
        }
        try (var files = Files.walk(assets.resolve("sounds"))) {
            check(files.filter(Files::isRegularFile).allMatch(path -> used.contains(path.normalize())), "no unreferenced sound files ship");
        }
        System.out.println("Sound checks passed: " + assertions + " assertions.");
    }
    /** Magnitude-weighted mean frequency of up to 8192 samples, by direct DFT up to 11 kHz (pure JDK, a few million operations). */
    private static double centroid(java.util.List<Float> samples) {
        int n = Math.min(8192, samples.size()), offset = (samples.size() - n) / 2, bins = (int) (11000.0 * n / 44100);
        double weighted = 0, total = 0;
        for (int k = 1; k < bins; k++) {
            double re = 0, im = 0, w = 2 * Math.PI * k / n;
            for (int i = 0; i < n; i++) { double v = samples.get(offset + i); re += v * Math.cos(w * i); im -= v * Math.sin(w * i); }
            double magnitude = Math.hypot(re, im), frequency = k * 44100.0 / n;
            weighted += magnitude * frequency; total += magnitude;
        }
        return total == 0 ? 0 : weighted / total;
    }
    private static void check(boolean condition, String message) { assertions++; if (!condition) { throw new AssertionError(message); } }
}
