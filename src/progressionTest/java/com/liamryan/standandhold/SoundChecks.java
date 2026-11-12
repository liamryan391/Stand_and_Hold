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
    private static final Map<String, double[]> EVENTS = Map.of("npc.gear", new double[] {0.1, 0.5},
            "launcher.fire", new double[] {0.3, 1.2}, "post.radio", new double[] {0.5, 2.0});
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
                    while (stream.readChunk(value -> { samples[0]++; peak[0] = Math.max(peak[0], Math.abs(value)); })) { }
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
    private static void check(boolean condition, String message) { assertions++; if (!condition) { throw new AssertionError(message); } }
}
