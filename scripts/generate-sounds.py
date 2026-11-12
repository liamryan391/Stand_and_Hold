"""Procedurally synthesises Stand and Hold's original sound set (M33).

Every sample is built from sine partials, filtered noise and simple formant pulses, seeded for repeatability, so no
recording, sample library or other mod's audio is involved. Output is mono 44.1 kHz Ogg Vorbis, the format Minecraft
needs for positional sounds. Requires numpy, scipy and ffmpeg (libvorbis):

    python scripts/generate-sounds.py

Re-run after changing a recipe; the .ogg files are committed and checked by the logic tests.
"""
import pathlib
import subprocess

import numpy as np
from scipy.signal import butter, sosfilt

RATE = 44100
OUT = pathlib.Path(__file__).resolve().parent.parent / "src/main/resources/assets/standandhold/sounds"


def t(seconds):
    return np.arange(int(RATE * seconds)) / RATE


def band(signal, low, high, order=4):
    return sosfilt(butter(order, [low, high], btype="band", fs=RATE, output="sos"), signal)


def low(signal, cutoff, order=4):
    return sosfilt(butter(order, cutoff, btype="low", fs=RATE, output="sos"), signal)


def place(target, sound, at):
    start = int(at * RATE)
    end = min(len(target), start + len(sound))
    target[start:end] += sound[: end - start]


def clink(rng, base):
    """A small metal-on-metal tick: inharmonic partials with fast exponential decay."""
    time = t(0.09)
    tone = sum(np.sin(2 * np.pi * base * ratio * time + rng.uniform(0, 6.28)) * np.exp(-time / rng.uniform(0.012, 0.03))
               for ratio in (1.0, 2.76, 5.4))
    return tone * 0.4 + band(rng.normal(0, 1, len(time)), 2000, 9000) * np.exp(-time / 0.004) * 0.6


def gear(seed):
    """Webbing, buckles and a canteen shifting with a step."""
    rng = np.random.default_rng(seed)
    out = np.zeros(int(RATE * 0.32))
    rustle = band(rng.normal(0, 1, len(out)), 350, 2400) * np.exp(-((t(0.32) - 0.08) ** 2) / 0.003)
    out += rustle * 0.35
    for _ in range(rng.integers(3, 6)):
        place(out, clink(rng, rng.uniform(1800, 3600)) * rng.uniform(0.25, 0.6), rng.uniform(0.0, 0.16))
    return out


def launcher(seed):
    """Pneumatic launch thump and air release, then the mechanism cycling the next bolt."""
    rng = np.random.default_rng(seed)
    out = np.zeros(int(RATE * 0.95))
    time = t(0.25)
    sweep = np.sin(2 * np.pi * np.cumsum(np.linspace(130, 42, len(time))) / RATE) * np.exp(-time / 0.07)
    place(out, sweep * 0.9, 0.0)
    hiss = band(rng.normal(0, 1, int(RATE * 0.3)), 700, 5200) * np.exp(-t(0.3) / 0.06)
    place(out, hiss * 0.55, 0.004)
    place(out, clink(rng, rng.uniform(2400, 2900)) * 0.5, 0.0)
    cycle = 0.42 + rng.uniform(-0.03, 0.03)
    place(out, clink(rng, rng.uniform(1500, 1900)) * 0.55, cycle)
    slide = band(rng.normal(0, 1, int(RATE * 0.07)), 900, 4000) * np.hanning(int(RATE * 0.07))
    place(out, slide * 0.3, cycle + 0.03)
    place(out, clink(rng, rng.uniform(2100, 2600)) * 0.6, cycle + 0.1)
    return out


VOWELS = [(730, 1090), (530, 1840), (270, 2290), (570, 840), (300, 870), (660, 1720), (490, 1350)]


def syllable(rng, duration):
    """An unintelligible voiced syllable: glottal pulse train through two formant resonators."""
    time = t(duration)
    f0 = rng.uniform(105, 165) * (1 + 0.06 * np.sin(2 * np.pi * rng.uniform(2, 5) * time))
    phase = np.cumsum(f0) / RATE
    pulses = (np.mod(phase, 1.0) < 0.08).astype(float) - 0.08
    f1, f2 = VOWELS[rng.integers(len(VOWELS))]
    voiced = band(pulses, f1 * 0.8, f1 * 1.2, 2) + 0.6 * band(pulses, f2 * 0.85, f2 * 1.15, 2)
    envelope = np.sin(np.pi * np.linspace(0, 1, len(time))) ** 0.6
    return voiced * envelope


def radio(seed):
    """Short garbled field-radio exchange: squelch, a few syllables through a narrow radio band, roger beep."""
    rng = np.random.default_rng(seed)
    out = np.zeros(int(RATE * 1.7))
    place(out, band(rng.normal(0, 1, int(RATE * 0.07)), 300, 3400) * 0.5, 0.0)
    cursor = 0.1
    for _ in range(rng.integers(4, 8)):
        length = rng.uniform(0.07, 0.15)
        place(out, syllable(rng, length) * rng.uniform(0.6, 1.0), cursor)
        cursor += length + rng.uniform(0.0, 0.06)
        if cursor > 1.35:
            break
    beep_time = t(0.07)
    place(out, np.sin(2 * np.pi * 1250 * beep_time) * np.hanning(len(beep_time)) * 0.35, cursor + 0.05)
    place(out, band(rng.normal(0, 1, int(RATE * 0.09)), 300, 3400) * np.linspace(0.5, 0, int(RATE * 0.09)), cursor + 0.13)
    out = np.tanh(band(out, 320, 3200) * 3.0) / 3.0
    out += band(rng.normal(0, 1, len(out)), 400, 3000) * 0.015
    return out


def write(name, signal, peak_db):
    signal = signal - np.mean(signal)
    signal = signal / (np.max(np.abs(signal)) + 1e-9) * (10 ** (peak_db / 20))
    audible = np.nonzero(np.abs(signal) > 0.08 * np.max(np.abs(signal)))[0]
    signal = signal[: min(len(signal), audible[-1] + int(RATE * 0.06))]  # trim dead air after the last audible sample
    fade = int(RATE * 0.01)
    signal[-fade:] *= np.linspace(1, 0, fade)
    path = OUT / f"{name}.ogg"
    path.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(["ffmpeg", "-loglevel", "error", "-y", "-f", "f32le", "-ar", str(RATE), "-ac", "1", "-i", "-",
                    "-c:a", "libvorbis", "-q:a", "4", "-map_metadata", "-1", "-fflags", "+bitexact", str(path)],
                   input=signal.astype("<f4").tobytes(), check=True)


if __name__ == "__main__":
    for i in range(3):
        write(f"npc/gear{i + 1}", gear(100 + i), -6)
    for i in range(2):
        write(f"launcher/fire{i + 1}", launcher(200 + i), -2)
    for i in range(4):
        write(f"post/radio{i + 1}", radio(300 + i), -7)
    print("Wrote 9 original sounds to", OUT)
