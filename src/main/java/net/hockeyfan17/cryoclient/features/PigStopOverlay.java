package net.hockeyfan17.cryoclient.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.hockeyfan17.cryoclient.CryoConfig;
import net.hockeyfan17.cryoclient.mixin.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.screen.slot.Slot;

public class PigStopOverlay {
    public static MinecraftClient client = MinecraftClient.getInstance();
    private static boolean showOverlay = false;
    private static int originalGuiScale = -1;
    private static ChatVisibility previousChatVisibility;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Screen screen = client.currentScreen;
            if (screen != null && screen.getTitle() != null) {
                boolean isPigStops = screen.getTitle().getString().contains("PigStops");
                showOverlay = isPigStops && CryoConfig.INSTANCE.pigStopDisplayToggle;

                if (isPigStops && CryoConfig.INSTANCE.pigStopDisplayToggle) {
                    GameOptions options = client.options;
                    if (originalGuiScale == -1) {
                        originalGuiScale = options.getGuiScale().getValue();
                        options.getGuiScale().setValue(CryoConfig.INSTANCE.pigStopDisplaySize);
                        if (CryoConfig.INSTANCE.pigStopDisplayHideChatToggle) {
                            previousChatVisibility = MinecraftClient.getInstance().options.getChatVisibility().getValue();
                            MinecraftClient.getInstance().options.getChatVisibility().setValue(ChatVisibility.HIDDEN);
                        }
                        client.onResolutionChanged();
                    }
                } else {
                    restoreGuiScale();
                }
            } else {
                showOverlay = false;
                restoreGuiScale();
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen.getTitle() != null && screen.getTitle().getString().contains("PigStops")) {
                ScreenEvents.afterRender(screen).register((scr, context, mouseX, mouseY, delta) -> {
                    if (showOverlay && scr instanceof HandledScreen<?> handled) {
                        renderOverlay(context, handled);
                    }
                });
            }
        });

//        for (Method m : HandledScreen.class.getDeclaredMethods()) {
//            System.out.println("HandledScreen method: " + m.getName() + " " + Arrays.toString(m.getParameterTypes()));
//        }
    }

    private static void restoreGuiScale() {
        if (originalGuiScale != -1) {
            client.options.getGuiScale().setValue(originalGuiScale);
            client.onResolutionChanged();
            originalGuiScale = -1;
            if (CryoConfig.INSTANCE.pigStopDisplayHideChatToggle) {
                MinecraftClient.getInstance().options.getChatVisibility().setValue(previousChatVisibility);
                previousChatVisibility = null;
            }
        }
    }

    private static void renderOverlay(DrawContext context, Screen screen) {
        if (!(screen instanceof HandledScreen<?> handled)) return;

        HandledScreenAccessor accessor = (HandledScreenAccessor) handled;

        int overlayTopOffset = 10;

        int guiX = accessor.getX();
        int guiY = accessor.getY();
        int guiWidth = accessor.getBackgroundWidth();
        int guiHeight = accessor.getPlayerInventoryTitleY() + 4;

        context.fill(guiX, guiY + overlayTopOffset, guiX + guiWidth, guiY + guiHeight, 0xAA000000);

        for (Slot slot : handled.getScreenHandler().slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty() || stack.isOf(Items.GRAY_STAINED_GLASS_PANE)) continue;

            int slotX = slot.x + guiX;
            int slotY = slot.y + guiY;
            if (slotY >= guiY + guiHeight) continue;

            int slotSize = 16;
            context.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, CryoConfig.INSTANCE.pigStopDisplayColor);
        }
    }
}
