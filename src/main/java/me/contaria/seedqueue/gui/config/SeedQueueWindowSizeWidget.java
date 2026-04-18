package me.contaria.seedqueue.gui.config;

import me.contaria.seedqueue.SeedQueueConfig;
import me.contaria.seedqueue.mixin.accessor.TextFieldWidgetAccessor;
import me.contaria.speedrunapi.config.api.gui.SpeedrunWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class SeedQueueWindowSizeWidget implements SpeedrunWidget {
    private final SeedQueueConfig.WindowSize windowSize;
    private final TextFieldWidget widthWidget;
    private final TextFieldWidget heightWidget;

    private final int width;
    private final int height;
    private int x;
    private int y;

    public SeedQueueWindowSizeWidget(SeedQueueConfig.WindowSize windowSize) {
        this.windowSize = windowSize;
        this.widthWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 65, 20) {
            @Override
            public boolean keyPressed(char character, int code) {
                boolean bl = super.keyPressed(character, code);
                SeedQueueConfig.WindowSize windowSize = SeedQueueWindowSizeWidget.this.windowSize;
                String text = this.getText();
                if (text.isEmpty()) {
                    windowSize.setWidth(0);
                } else {
                    try {
                        windowSize.setWidth(Integer.parseUnsignedInt(text));
                    } catch (NumberFormatException ignored) {
                    }
                }
                this.setText(String.valueOf(windowSize.width()));
                return bl;
            }
        };
        this.widthWidget.setText(String.valueOf(this.windowSize.width()));
        this.heightWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 65, 20) {
            @Override
            public boolean keyPressed(char character, int code) {
                boolean bl = super.keyPressed(character, code);
                SeedQueueConfig.WindowSize windowSize = SeedQueueWindowSizeWidget.this.windowSize;
                String text = this.getText();
                if (text.isEmpty()) {
                    windowSize.setHeight(0);
                } else {
                    try {
                        windowSize.setHeight(Integer.parseUnsignedInt(text));
                    } catch (NumberFormatException ignored) {
                    }
                }
                this.setText(String.valueOf(windowSize.height()));
                return bl;
            }
        };
        this.heightWidget.setText(String.valueOf(this.windowSize.height()));
        this.width = 150;
        this.height = 20;
        this.x = 0;
        this.y = 0;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        ((TextFieldWidgetAccessor) this.widthWidget).seedQueue$setX(this.x);
        ((TextFieldWidgetAccessor) this.widthWidget).seedQueue$setY(this.y);
        this.widthWidget.render();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        textRenderer.draw("X", this.x + (this.width - textRenderer.getStringWidth("X")) / 2, this.y, 0xFFFFFF);
        ((TextFieldWidgetAccessor) this.heightWidget).seedQueue$setX(this.x + 85);
        ((TextFieldWidgetAccessor) this.heightWidget).seedQueue$setY(this.y);
        this.heightWidget.render();
    }

    @Override
    public boolean keyPressed(char id, int code) {
        this.widthWidget.keyPressed(id, code);
        this.heightWidget.keyPressed(id, code);
        return true;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        this.widthWidget.mouseClicked(mouseX, mouseY, button);
        this.heightWidget.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    public void tick() {
        this.widthWidget.tick();
        this.heightWidget.tick();
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
}
