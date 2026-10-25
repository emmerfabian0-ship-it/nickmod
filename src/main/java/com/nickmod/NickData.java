package com.nickmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

/** Alle Daten eines Nicknames. Wird gespeichert, ueber das Netzwerk geschickt und gerendert. */
public record NickData(boolean enabled, String name, int color, int caseMode, int effect) {

    public static final int EFFECT_COUNT = 6; // NONE, RAINBOW, SHINY, WAVE, PULSE, GLITCH
    public static final int RAINBOW = 1, SHINY = 2, WAVE = 3, PULSE = 4, GLITCH = 5;

    public static final NickData NONE = new NickData(false, "", 0xFFFFFF, 0, 0);

    public static final StreamCodec<ByteBuf, NickData> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, NickData::enabled,
            ByteBufCodecs.stringUtf8(64), NickData::name,
            ByteBufCodecs.VAR_INT, NickData::color,
            ByteBufCodecs.VAR_INT, NickData::caseMode,
            ByteBufCodecs.VAR_INT, NickData::effect,
            NickData::new);

    /** Macht die Daten sicher (der Server darf Clients nie vertrauen). */
    public NickData sanitized() {
        String n = name == null ? "" : name.replaceAll("\\p{Cntrl}", "").strip();
        if (n.length() > 24) n = n.substring(0, 24);
        return new NickData(enabled && !n.isEmpty(), n, color & 0xFFFFFF,
                Math.floorMod(caseMode, 3), Mth.clamp(effect, 0, EFFECT_COUNT - 1));
    }

    private String applyCase(String s) {
        return switch (caseMode) {
            case 1 -> s.toUpperCase();
            case 2 -> s.toLowerCase();
            default -> s;
        };
    }

    /** Baut den animierten Text. Muss jeden Frame neu aufgerufen werden. */
    public MutableComponent render(long ms) {
        int[] cps = applyCase(name).codePoints().toArray();
        int n = cps.length;
        MutableComponent out = Component.empty();
        for (int i = 0; i < n; i++) {
            Style st = Style.EMPTY.withColor(colorAt(i, n, ms));
            if (effect == GLITCH && glitch(i, ms)) st = st.withObfuscated(true);
            out.append(Component.literal(new String(Character.toChars(cps[i]))).setStyle(st));
        }
        return out;
    }

    private int colorAt(int i, int n, long ms) {
        switch (effect) {
            case RAINBOW: {
                float h = ((ms % 3000L) / 3000f + i * 0.07f) % 1f;
                return Mth.hsvToRgb(h, 0.85f, 1f) & 0xFFFFFF;
            }
            case SHINY: {
                double p = (ms / 70.0) % (n + 8) - 4;
                double t = Mth.clamp(1.0 - Math.abs(i - p) / 2.5, 0.0, 1.0);
                return mix(color, 0xFFFFFF, (float) t);
            }
            case WAVE: {
                float f = (float) (0.5 + 0.5 * Math.sin(ms / 250.0 - i * 0.6));
                return mix(mix(color, 0x000000, 0.5f), mix(color, 0xFFFFFF, 0.3f), f);
            }
            case PULSE: {
                float f = (float) (0.5 + 0.5 * Math.sin(ms / 300.0));
                return mix(mix(color, 0x000000, 0.5f), mix(color, 0xFFFFFF, 0.3f), f);
            }
            default:
                return color;
        }
    }

    /** Zufaellig flackernde Zeichen (ca. 8 %), wechselt alle 80 ms. */
    private static boolean glitch(int i, long ms) {
        long seed = (ms / 80L) * 31L + i * 7919L;
        return ((seed * 0x9E3779B97F4A7C15L) >>> 58) < 5;
    }

    private static int mix(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
