package com.shinapri.bulkexcavation.client;

import com.shinapri.bulkexcavation.network.SetRegionPayload;

import com.mojang.datafixers.util.Pair;
import com.shinapri.bulkexcavation.ClientSel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import org.lwjgl.glfw.GLFW;

public class BulkexcavationClient implements ClientModInitializer {
    private static KeyBinding KEY_POS;

    private boolean prevLmb = false;
    private static final long CLICK_COOLDOWN_MS = 120;
    private long lastClickMs = 0;


    @Override public void onInitializeClient() {

        KEY_POS = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.excavation.pos",
                GLFW.GLFW_KEY_LEFT_ALT,
                "key.categories.excavation"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.world == null || client.isPaused() || client.currentScreen != null) return;

            long window = client.getWindow().getHandle();

            boolean altHeld = KEY_POS.isPressed() || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_ALT);

            boolean lmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            boolean risingEdge = lmb && !prevLmb;
            prevLmb = lmb;

            if (!(altHeld && risingEdge)) return;
            long now = net.minecraft.util.Util.getMeasuringTimeMs();
            if (now - lastClickMs < CLICK_COOLDOWN_MS) return;
            lastClickMs = now;

            BlockPos hit = getLookedBlock(client);
            if (hit == null) return;

            boolean complete = ClientSel.push(hit);
            if (complete) {
                Pair<BlockPos, BlockPos> s = ClientSel.consume();
                ClientPlayNetworking.send(new SetRegionPayload(s.getFirst(), s.getSecond()));
            } else {
                client.inGameHud.setOverlayMessage(
                        Text.literal("anchor point " + hit.toShortString()).formatted(Formatting.GREEN),
                        false
                );
            }
        });

    }

    private static BlockPos getLookedBlock(MinecraftClient client) {
        HitResult target = client.crosshairTarget;
        if (!(target instanceof BlockHitResult bhr)) return null;
        return bhr.getBlockPos();
    }

}
