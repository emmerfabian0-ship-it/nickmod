package com.nickmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

public class NickScreen extends Screen {
    private static final int[] PRESETS = {
            0xFFFFFF, 0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x5555FF, 0xFF55FF
    };

    private String name;
    private int r, g, b, caseMode, effect;

    public NickScreen() {
        super(Component.translatable("screen.nickmod.title"));
        NickData o = ClientNicks.own;
        name = o.name();
        r = (o.color() >> 16) & 255;
        g = (o.color() >> 8) & 255;
        b = o.color() & 255;
        caseMode = o.caseMode();
        effect = o.effect();
    }

    private int color() {
        return (r << 16) | (g << 8) | b;
    }

    private NickData current() {
        return new NickData(!name.isBlank(), name.strip(), color(), caseMode, effect).sanitized();
    }

    private Component caseText() {
        return Component.translatable("nickmod.case", Component.translatable("nickmod.case." + caseMode));
    }

    private Component effectText() {
        return Component.translatable("nickmod.effect", Component.translatable("nickmod.effect." + effect));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int top = Math.max(10, height / 2 - 105);

        EditBox field = new EditBox(font, cx - 100, top + 30, 200, 20, Component.translatable("nickmod.name"));
        field.setMaxLength(24);
        field.setValue(name);
        field.setResponder(s -> name = s);
        addRenderableWidget(field);
        setInitialFocus(field);

        addRenderableWidget(new ChannelSlider(cx - 100, top + 56, 200, "R", r, v -> r = v));
        addRenderableWidget(new ChannelSlider(cx - 100, top + 78, 200, "G", g, v -> g = v));
        addRenderableWidget(new ChannelSlider(cx - 100, top + 100, 200, "B", b, v -> b = v));

        for (int i = 0; i < PRESETS.length; i++) {
            final int c = PRESETS[i];
            addRenderableWidget(Button.builder(
                    Component.literal("■").withColor(c),
                    btn -> {
                        r = (c >> 16) & 255;
                        g = (c >> 8) & 255;
                        b = c & 255;
                        rebuildWidgets();
                    }).bounds(cx - 100 + i * 25, top + 126, 22, 20).build());
        }

        addRenderableWidget(Button.builder(caseText(), btn -> {
            caseMode = (caseMode + 1) % 3;
            btn.setMessage(caseText());
        }).bounds(cx - 100, top + 152, 98, 20).build());

        addRenderableWidget(Button.builder(effectText(), btn -> {
            effect = (effect + 1) % NickData.EFFECT_COUNT;
            btn.setMessage(effectText());
        }).bounds(cx + 2, top + 152, 98, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("nickmod.save"), btn -> {
            ClientNicks.setOwn(current());
            onClose();
        }).bounds(cx - 100, top + 178, 98, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("nickmod.reset"), btn -> {
            ClientNicks.setOwn(NickData.NONE);
            onClose();
        }).bounds(cx + 2, top + 178, 98, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int cx = width / 2;
        int top = Math.max(10, height / 2 - 105);
        graphics.text(font, title, cx - font.width(title) / 2, top, 0xFFFFFFFF, true);
        NickData d = current();
        Component preview = d.enabled() ? d.render(System.currentTimeMillis()) : Component.literal("...");
        graphics.text(font, preview, cx - font.width(preview) / 2, top + 14, 0xFFFFFFFF, true);
    }

    private static class ChannelSlider extends AbstractSliderButton {
        private final String label;
        private final IntConsumer setter;

        ChannelSlider(int x, int y, int w, String label, int value, IntConsumer setter) {
            super(x, y, w, 20, Component.empty(), value / 255.0);
            this.label = label;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + (int) Math.round(value * 255)));
        }

        @Override
        protected void applyValue() {
            setter.accept((int) Math.round(value * 255));
        }
    }
}
