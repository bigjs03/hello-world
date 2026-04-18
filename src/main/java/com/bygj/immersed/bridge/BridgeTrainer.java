package com.bygj.immersed.bridge;

import com.bygj.immersed.config.ImmersedConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class BridgeTrainer {
    private static boolean enabled = false;
    private static long lastPlaceMs = 0L;

    public static void init() {
        KeyBinding toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.immersed.bridge_trainer",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.immersed"
        ));

        KeyBinding syncKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.immersed.sync_cloud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.immersed"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                handleToggle(client);
            }
            while (syncKey.wasPressed()) {
                boolean ok = ImmersedConfig.syncFromCloud();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal(ok
                            ? "\u00a7aConfig synced from cloud."
                            : "\u00a7cCloud sync failed (check URL)."), false);
                }
            }
            if (enabled && isLocalWorld(client)) {
                tickBridge(client);
            }
        });
    }

    private static void handleToggle(MinecraftClient client) {
        if (!isLocalWorld(client)) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal(
                        "\u00a7cBridge trainer is singleplayer/LAN-only."), false);
            }
            enabled = false;
            return;
        }
        enabled = !enabled;
        if (client.player != null) {
            client.player.sendMessage(Text.literal(enabled
                    ? "\u00a7aBridge trainer: ON (sneak + walk backwards holding a block)"
                    : "\u00a77Bridge trainer: OFF"), false);
        }
    }

    private static boolean isLocalWorld(MinecraftClient client) {
        return client.getServer() != null;
    }

    private static void tickBridge(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (p == null || client.world == null || client.interactionManager == null) return;
        if (!p.isSneaking()) return;

        ItemStack stack = p.getMainHandStack();
        if (!(stack.getItem() instanceof BlockItem)) return;

        long now = System.currentTimeMillis();
        long interval = Math.max(50L, ImmersedConfig.get().bridgeIntervalMs);
        if (now - lastPlaceMs < interval) return;

        Vec3d look = p.getRotationVec(1.0f);
        int dx = Math.abs(look.x) > Math.abs(look.z) ? (look.x > 0 ? 1 : -1) : 0;
        int dz = dx == 0 ? (look.z > 0 ? 1 : -1) : 0;

        BlockPos feet = p.getBlockPos();
        BlockPos placeAt = feet.add(-dx, -1, -dz);

        if (!client.world.getBlockState(placeAt).isAir()) return;

        BlockPos against = placeAt.add(dx, 0, dz);
        if (client.world.getBlockState(against).isAir()) {
            against = placeAt.down();
            if (client.world.getBlockState(against).isAir()) return;
        }

        Direction face = against.equals(placeAt.down()) ? Direction.UP
                : Direction.fromVector(-dx, 0, -dz);
        if (face == null) face = Direction.UP;

        Vec3d hitVec = Vec3d.ofCenter(against).add(
                face.getOffsetX() * 0.5,
                face.getOffsetY() * 0.5,
                face.getOffsetZ() * 0.5
        );
        BlockHitResult hit = new BlockHitResult(hitVec, face, against, false);

        client.interactionManager.interactBlock(p, Hand.MAIN_HAND, hit);
        p.swingHand(Hand.MAIN_HAND);
        lastPlaceMs = now;
    }
}
