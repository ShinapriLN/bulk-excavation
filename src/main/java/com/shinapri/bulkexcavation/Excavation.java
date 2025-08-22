package com.shinapri.bulkexcavation;

import com.shinapri.bulkexcavation.config.ExcavationConfigIO;
import com.shinapri.bulkexcavation.network.SetRegionPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.tag.BlockTags;

import java.io.Console;
import java.util.List;


public class Excavation implements ModInitializer {
            public static final String MOD_ID = "bulk-excavation";

    @Override
            public void onInitialize() {
                PayloadTypeRegistry.playC2S().register(SetRegionPayload.ID, SetRegionPayload.CODEC);

                ServerPlayNetworking.registerGlobalReceiver(SetRegionPayload.ID,
                        (payload, context) -> {
                            context.server().execute(() -> {
                                var player = context.player();
                                ServerWorld world = player.getWorld();

                                var cfg       = com.shinapri.bulkexcavation.config.ExcavationConfigIO.get();
                                int limit     = cfg.maxVolume;
                                boolean mustUseTool = cfg.requireTool;
                                boolean dropLoot    = cfg.dropLoot;
                                boolean consoleLog = cfg.consoleLog;
                                List<String> skipBlocks = cfg.skipBlocks;

                                var a = payload.pos1();
                                var b = payload.pos2();

                                int minX = Math.min(a.getX(), b.getX());
                                int minY = Math.min(a.getY(), b.getY());
                                int minZ = Math.min(a.getZ(), b.getZ());
                                int maxX = Math.max(a.getX(), b.getX());
                                int maxY = Math.max(a.getY(), b.getY());
                                int maxZ = Math.max(a.getZ(), b.getZ());

                                int volume = (maxX-minX+1)*(maxY-minY+1)*(maxZ-minZ+1);
                                if (volume > limit) {
                                    player.sendMessage(
                                            net.minecraft.text.Text.literal("§c[Excavation] Volume exceeds limit ("+limit+")."),
                                            false
                                    );
                                    return;
                                }

                                BlockPos.Mutable m = new BlockPos.Mutable();
                                int broken = 0, skippedNoTool = 0;

                                for (int x=minX; x<=maxX; x++) {
                                    for (int y=minY; y<=maxY; y++) {
                                        for (int z=minZ; z<=maxZ; z++) {
                                            m.set(x,y,z);
                                            BlockState state = world.getBlockState(m);
                                            if (state.isAir()) continue;

                                            Identifier id = Registries.BLOCK.getId(state.getBlock());
                                            if (skipBlocks.contains(id.toString())) continue;

                                            int slot = -1;
                                            ItemStack tool = ItemStack.EMPTY;

                                            // Specify use exist tools in config
                                            if (mustUseTool) {
                                                slot = findSuitableToolSlot(player, state);
                                                if (slot < 0) { skippedNoTool++; continue; }    // Skip the block if no tools required exist
                                                tool = player.getInventory().getStack(slot);    // Otherwise
                                                damageToolOne(tool, player);
                                            } else {
                                                slot = findSuitableToolSlot(player, state);
                                            }

                                            int old = player.getInventory().getSelectedSlot();
                                            if (slot >= 0) player.getInventory().setSelectedSlot(slot);

                                            boolean ok = world.breakBlock(m, dropLoot, player);

                                            player.getInventory().setSelectedSlot(old);
                                            player.currentScreenHandler.sendContentUpdates();
                                            if (ok) broken++;

                                        }
                                    }
                                }

                                if (broken > 0) {
                                    if(consoleLog){
                                        player.sendMessage(net.minecraft.text.Text.literal(
                                                "§a[Excavation] Mined " + broken + " blocks; " +
                                                        (skippedNoTool > 0 ? ("§cskipped " + skippedNoTool + " (no suitable tool)") : "§7no skips")
                                        ), false);
                                    }

                                } else {
                                    player.sendMessage(net.minecraft.text.Text.literal(
                                            skippedNoTool > 0
                                                    ? "§c[Excavation] No suitable tool for this region."
                                                    : "§7[Excavation] Nothing to mine."
                                    ), false);
                                }
                            });


                        }

                );
    }

    private static int findSuitableToolSlot(ServerPlayerEntity player, BlockState state) {
        var inv = player.getInventory();
        int size = inv.size();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getStack(i);
            // filtered out not tools and empty stack
            if (stack.isEmpty()) continue;
            if (!isToolLike(stack)) continue;

            // Check if the block require tools
            if (state.isToolRequired()) {
                if (stack.isSuitableFor(state)) return i;
                else continue;
            }

            // The current block needs no tools
            // Check if the current stack suits for the current block
            if (stack.isSuitableFor(state)) return i;

            // Finally if the current block uses shear
            if (stack.getItem() instanceof ShearsItem &&
                    (state.isIn(BlockTags.LEAVES) || state.isIn(BlockTags.WOOL) || state.isOf(Blocks.COBWEB))){
                return i;
            }
        }
        return -1;
    }

    private static boolean isToolLike(ItemStack stack) {
        var item = stack.getItem();
        return stack.isDamageable() || item instanceof ShearsItem;
    }

    private static void damageToolOne(ItemStack tool, ServerPlayerEntity player) {
        if (!tool.isDamageable()) return;
        tool.setDamage(tool.getDamage() + 1);
        if (tool.getDamage() >= tool.getMaxDamage()) {
            player.playSound(SoundEvents.ENTITY_ITEM_BREAK.value(), 1.0f, 1.0f);
            tool.decrement(1);
        }
    }

}

