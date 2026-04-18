package me.contaria.seedqueue.mixin.accessor;

import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextFieldWidget.class)
public interface TextFieldWidgetAccessor {
    @Mutable
    @Accessor("x")
    void seedQueue$setX(int x);

    @Mutable
    @Accessor("y")
    void seedQueue$setY(int y);
}
