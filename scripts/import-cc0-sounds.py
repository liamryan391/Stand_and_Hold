"""Imports the CC0 recordings Stand and Hold uses (decision D10) from Kenney's public-domain packs.

Liam downloaded the packs (Kenney "RPG Audio" and "Impact Sounds", both CC0 1.0). Each file below is converted to
mono 44.1 kHz Vorbis, softened (low-pass) so it sits under Minecraft's own footsteps, trimmed and levelled. The
mapping is listed in THIRD_PARTY_NOTICES.md. Never import audio from other mods.

    python scripts/import-cc0-sounds.py <folder with the extracted RPG Audio pack>
"""
import pathlib
import subprocess
import sys

import numpy as np
from scipy.signal import butter, sosfilt

RATE = 44100
OUT = pathlib.Path(__file__).resolve().parent.parent / "src/main/resources/assets/standandhold/sounds"
# target -> (source file in the RPG Audio pack, low-pass Hz, peak dB)
IMPORTS = {
    "npc/cloth1": ("Audio/cloth1.ogg", 3200, -14),
    "npc/cloth2": ("Audio/cloth2.ogg", 3200, -14),
    "npc/cloth3": ("Audio/cloth3.ogg", 3200, -14),
}


def decode(path):
    raw = subprocess.run(["ffmpeg", "-v", "error", "-i", str(path), "-f", "f32le", "-ac", "1", "-ar", str(RATE), "-"],
                         capture_output=True, check=True).stdout
    return np.frombuffer(raw, dtype="<f4").astype(np.float64)


def main(pack):
    for target, (source, cutoff, peak_db) in IMPORTS.items():
        signal = decode(pack / source)
        signal = sosfilt(butter(4, [90, cutoff], btype="band", fs=RATE, output="sos"), signal)
        audible = np.nonzero(np.abs(signal) > 0.05 * np.max(np.abs(signal)))[0]
        signal = signal[max(0, audible[0] - int(RATE * 0.01)): audible[-1] + int(RATE * 0.05)]
        signal = signal / (np.max(np.abs(signal)) + 1e-9) * (10 ** (peak_db / 20))
        fade = int(RATE * 0.01)
        signal[:fade] *= np.linspace(0, 1, fade)
        signal[-fade:] *= np.linspace(1, 0, fade)
        path = OUT / f"{target}.ogg"
        path.parent.mkdir(parents=True, exist_ok=True)
        subprocess.run(["ffmpeg", "-loglevel", "error", "-y", "-f", "f32le", "-ar", str(RATE), "-ac", "1", "-i", "-",
                        "-c:a", "libvorbis", "-q:a", "4", "-map_metadata", "-1", "-fflags", "+bitexact", str(path)],
                       input=signal.astype("<f4").tobytes(), check=True)
        print(f"{source} -> {target}.ogg")


if __name__ == "__main__":
    main(pathlib.Path(sys.argv[1]))
