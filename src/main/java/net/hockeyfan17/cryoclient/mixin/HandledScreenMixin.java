package net.hockeyfan17.cryoclient.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.hockeyfan17.cryoclient.CryoConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {

    private ChatVisibility previousChatVisibility;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void hideBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (((HandledScreen<?>)(Object)this).getTitle().getString().contains("PigStops") && CryoConfig.INSTANCE.pigStopDisplayToggle) {
            ci.cancel();
        }
    }

    @Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    private void hideForeground(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (((HandledScreen<?>)(Object)this).getTitle().getString().contains("PigStops") && CryoConfig.INSTANCE.pigStopDisplayToggle) {
            ci.cancel();
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void hideSlots(DrawContext context, Slot slot, CallbackInfo ci) {
        if (((HandledScreen<?>)(Object)this).getTitle().getString().contains("PigStops") && CryoConfig.INSTANCE.pigStopDisplayToggle) {
            ci.cancel();
        }
    }

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    private void hideTooltips(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (((HandledScreen<?>) (Object) this).getTitle().getString().contains("PigStops") && CryoConfig.INSTANCE.pigStopDisplayToggle) {
            ci.cancel(); // prevents the item tooltip from being drawn
        }
    }

    @Inject(method = "drawSlotHighlightBack(Lnet/minecraft/client/gui/DrawContext;)V",
            at = @At("HEAD"), cancellable = true)
    private void hideSlotHighlightBack(DrawContext context, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>)(Object)this;
        if (screen.getTitle() != null && screen.getTitle().getString().contains("PigStops")
                && CryoConfig.INSTANCE.pigStopDisplayToggle
                && !CryoConfig.INSTANCE.pigStopDisplayHighlightSquareToggle) {
            ci.cancel();
        }
    }

    @Inject(method = "drawSlotHighlightFront(Lnet/minecraft/client/gui/DrawContext;)V",
            at = @At("HEAD"), cancellable = true)
    private void hideSlotHighlightFront(DrawContext context, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>)(Object)this;
        if (screen.getTitle() != null && screen.getTitle().getString().contains("PigStops")
                && CryoConfig.INSTANCE.pigStopDisplayToggle
                && !CryoConfig.INSTANCE.pigStopDisplayHighlightSquareToggle) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
            at = @At("HEAD"), cancellable = true)
    private void onClickOverride(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>)(Object)this;

        if (screen.getTitle() != null && screen.getTitle().getString().contains("PigStops") && CryoConfig.INSTANCE.pigStopDisplayToggle) {
            if (slot != null && !slot.getStack().isEmpty()) {
                int syncId = screen.getScreenHandler().syncId;
                int revision = screen.getScreenHandler().getRevision();
                ItemStack stack = slot.getStack().copy();

                // Convert DefaultedList to Int2ObjectMap
                Int2ObjectMap<ItemStack> modifiedStacks = new Int2ObjectOpenHashMap<>();
                DefaultedList<ItemStack> stacks = screen.getScreenHandler().getStacks();
                for (int i = 0; i < stacks.size(); i++) {
                    modifiedStacks.put(i, stacks.get(i));
                }

                assert client != null;
                Objects.requireNonNull(client.getNetworkHandler()).sendPacket(new ClickSlotC2SPacket(
                        syncId,
                        revision,
                        slot.id,
                        0,
                        SlotActionType.THROW,
                        stack,
                        modifiedStacks
                ));

                screen.getScreenHandler().setCursorStack(ItemStack.EMPTY);
            }

            ci.cancel();
        }
    }
}