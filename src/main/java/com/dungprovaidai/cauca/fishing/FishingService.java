package com.dungprovaidai.cauca.fishing;

import com.dungprovaidai.cauca.component.JournalData;
import com.dungprovaidai.cauca.entity.FishEntity;
import com.dungprovaidai.cauca.entity.FishingFloatEntity;
import com.dungprovaidai.cauca.item.BaitItem;
import com.dungprovaidai.cauca.item.CaucaFishingRodItem;
import com.dungprovaidai.cauca.registry.ModAttachments;
import com.dungprovaidai.cauca.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/** All state transitions enter here; entities never trust a client-side input. */
public final class FishingService {
    private FishingService() {}

    public static void handleRodAction(ServerPlayer player, InteractionHand hand, CaucaFishingRodItem rod, ItemStack rodStack) {
        FishingFloatEntity floatEntity = activeFloat(player).orElse(null);
        if (floatEntity == null) {
            startCast(player, hand, rod, rodStack);
            return;
        }

        switch (floatEntity.phase()) {
            case WAITING, CAST -> {
                player.sendSystemMessage(Component.translatable("message.cauca_fishing.cancelled"));
                releaseFish(null, floatEntity);
            }
            case BITE -> {
                floatEntity.setPhase(FishingPhase.HOOKED);
                EntityAccess fish = fish(floatEntity);
                if (fish.fish() != null) {
                    fish.fish().requestReel();
                    floatEntity.setPhase(FishingPhase.FIGHT);
                    player.sendSystemMessage(Component.translatable("message.cauca_fishing.hooked", fish.fish().species().displayName()));
                    syncState(player, fish.fish());
                } else {
                    ItemStack treasure = floatEntity.takeSpecialCatch();
                    if (!treasure.isEmpty()) {
                        if (!player.getInventory().add(treasure)) player.drop(treasure, false);
                        player.sendSystemMessage(Component.translatable("message.cauca_fishing.special_catch"));
                    }
                    releaseFish(null, floatEntity);
                }
            }
            case HOOKED, FIGHT, REEL -> {
                FishEntity fish = fish(floatEntity).fish();
                if (fish != null) {
                    fish.requestReel();
                    floatEntity.setPhase(FishingPhase.REEL);
                    syncState(player, fish);
                    rodStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
            }
            case LANDED -> player.sendSystemMessage(Component.translatable("message.cauca_fishing.landed_tool"));
            case LINE_SNAPPED -> releaseFish(null, floatEntity);
            default -> {}
        }
    }

    public static void onReelInput(ServerPlayer player, boolean reeling) {
        if (!reeling) return;
        activeFloat(player).ifPresent(floatEntity -> {
            if (floatEntity.phase() == FishingPhase.FIGHT || floatEntity.phase() == FishingPhase.REEL || floatEntity.phase() == FishingPhase.HOOKED) {
                FishEntity fish = fish(floatEntity).fish();
                if (fish != null) {
                    fish.requestReel();
                    floatEntity.setPhase(FishingPhase.REEL);
                }
            }
        });
    }

    private static void startCast(ServerPlayer player, InteractionHand hand, CaucaFishingRodItem rod, ItemStack rodStack) {
        if (!(player.level() instanceof ServerLevel level)) return;
        Vec3 target = waterTarget(player, rod.stats().castingDistance());
        if (target == null) {
            player.sendSystemMessage(Component.translatable("message.cauca_fishing.need_water"));
            return;
        }
        BaitSelection bait = findBait(player.getInventory());
        if (bait == null) {
            player.sendSystemMessage(Component.translatable("message.cauca_fishing.need_bait"));
            return;
        }
        if (findFirst(player.getInventory(), ModItems.BASIC_LINE.get(), ModItems.STRONG_LINE.get(), ModItems.FINE_LINE.get(), ModItems.REINFORCED_LINE.get()) == null
                || findFirst(player.getInventory(), ModItems.SMALL_HOOK.get(), ModItems.MEDIUM_HOOK.get(), ModItems.LARGE_HOOK.get(), ModItems.BARBED_HOOK.get(), ModItems.DEEP_HOOK.get()) == null) {
            player.sendSystemMessage(Component.translatable("message.cauca_fishing.need_tackle"));
            return;
        }
        FishingSpot spot = FishingSpotResolver.resolve(level, BlockPos.containing(target));
        FishingFloatEntity entity = new FishingFloatEntity(com.dungprovaidai.cauca.registry.ModEntities.FISHING_FLOAT.get(), level);
        int wait = 65 + level.getRandom().nextInt(100) - Math.round(rod.stats().sensitivity() * 25.0F);
        entity.cast(player, target, bait.profile().id(), spot, Math.max(30, wait));
        level.addFreshEntity(entity);
        bait.stack().shrink(1);
        updateState(player, new FishingState(FishingPhase.CAST, 0.0F, 0.0F, "", false));
        player.sendSystemMessage(Component.translatable("message.cauca_fishing.cast", spot.type().name()));
        rodStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    private static Vec3 waterTarget(ServerPlayer player, float maxDistance) {
        HitResult hit = player.pick(maxDistance, 1.0F, false);
        Vec3 origin = player.getEyePosition();
        Vec3 direction = player.getLookAngle();
        if (hit.getType() == HitResult.Type.BLOCK && ((BlockHitResult) hit).getBlockPos() != null) {
            Vec3 at = hit.getLocation();
            BlockPos block = ((BlockHitResult) hit).getBlockPos();
            if (player.level().getFluidState(block).is(FluidTags.WATER)) return at;
            if (player.level().getFluidState(block.above()).is(FluidTags.WATER)) return Vec3.atCenterOf(block.above());
        }
        for (double distance = 1.0D; distance <= maxDistance; distance += 0.5D) {
            Vec3 point = origin.add(direction.scale(distance));
            BlockPos pos = BlockPos.containing(point);
            if (player.level().getFluidState(pos).is(FluidTags.WATER)) return new Vec3(point.x, pos.getY() + 0.55D, point.z);
        }
        return null;
    }

    public static Optional<FishingFloatEntity> activeFloat(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(FishingFloatEntity.class, player.getBoundingBox().inflate(96.0D),
                entity -> player.getUUID().equals(entity.anglerUUID())).stream().findFirst();
    }

    private static EntityAccess fish(FishingFloatEntity entity) {
        if (entity.fishId() < 0) return new EntityAccess(null);
        return new EntityAccess(entity.level().getEntity(entity.fishId()) instanceof FishEntity fish ? fish : null);
    }

    public static void releaseFish(FishEntity fish) {
        if (fish != null) {
            FishingFloatEntity floatEntity = fish.floatId() < 0 ? null : (fish.level().getEntity(fish.floatId()) instanceof FishingFloatEntity value ? value : null);
            if (floatEntity != null) floatEntity.discard();
            fish.discard();
            ServerPlayer player = fish.getAngler();
            if (player != null) updateState(player, FishingState.IDLE);
        }
    }

    public static void releaseFish(FishEntity fish, FishingFloatEntity floatEntity) {
        if (fish == null && floatEntity != null) fish = fish(floatEntity).fish();
        if (fish != null) fish.discard();
        ServerPlayer player = floatEntity == null ? null : floatEntity.getAngler();
        if (floatEntity != null) floatEntity.discard();
        if (player != null) updateState(player, FishingState.IDLE);
    }

    public static void lineSnap(FishEntity fish, ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.cauca_fishing.line_snap"));
        player.level().broadcastEntityEvent(fish, (byte) 3);
        removeOneMatching(player.getInventory(), ModItems.MEDIUM_HOOK.get(), ModItems.SMALL_HOOK.get(), ModItems.LARGE_HOOK.get(), ModItems.BARBED_HOOK.get(), ModItems.DEEP_HOOK.get());
        FishingFloatEntity floatEntity = fish.floatId() < 0 ? null : (fish.level().getEntity(fish.floatId()) instanceof FishingFloatEntity value ? value : null);
        if (floatEntity != null) {
            floatEntity.setPhase(FishingPhase.LINE_SNAPPED);
            floatEntity.setFishId(-1);
        }
        fish.discard();
        updateState(player, FishingState.IDLE);
    }

    public static void syncState(ServerPlayer player, FishEntity fish) {
        FishingPhase phase = fish.isHooked() ? (fish.isReeling() ? FishingPhase.REEL : FishingPhase.FIGHT) : (fish.isCapturable() ? FishingPhase.LANDED : FishingPhase.FIGHT);
        updateState(player, new FishingState(phase, fish.tension(), fish.stamina(), fish.speciesId(), fish.isReeling()));
    }

    public static void updateState(ServerPlayer player, FishingState state) {
        player.setData(ModAttachments.FISHING_STATE, state);
    }

    public static void recordCatch(ServerPlayer player, FishEntity fish) {
        JournalData current = player.getData(ModAttachments.JOURNAL);
        String spot = fish.spotId().isBlank() ? "Unknown" : fish.spotId();
        JournalData next = current.recordCatch(fish.speciesId(), fish.weightKg(), fish.lengthCm(), spot,
                fish.baitId().isBlank() ? "—" : fish.baitId(), fish.timeId());
        player.setData(ModAttachments.JOURNAL, next);
    }

    public static float rodStrength(ServerPlayer player) {
        return rod(player).map(value -> value.stats().strength()).orElse(4.0F);
    }

    public static float rodPower(ServerPlayer player) {
        return rod(player).map(value -> value.stats().landingPower()).orElse(1.0F);
    }

    public static float sensitivity(ServerPlayer player) {
        return rod(player).map(value -> value.stats().sensitivity()).orElse(0.5F);
    }

    public static float lineSlack(ServerPlayer player) {
        return rod(player).map(value -> value.stats().lineCapacity() * 0.12F).orElse(2.0F);
    }

    public static float lineStrength(ServerPlayer player) {
        Item line = findFirst(player.getInventory(), ModItems.REINFORCED_LINE.get(), ModItems.STRONG_LINE.get(), ModItems.FINE_LINE.get(), ModItems.BASIC_LINE.get());
        if (line == ModItems.REINFORCED_LINE.get()) return 10.0F;
        if (line == ModItems.STRONG_LINE.get()) return 7.0F;
        if (line == ModItems.FINE_LINE.get()) return 3.0F;
        return 4.0F;
    }

    public static float hookStrength(ServerPlayer player) {
        Item hook = findFirst(player.getInventory(), ModItems.DEEP_HOOK.get(), ModItems.LARGE_HOOK.get(), ModItems.BARBED_HOOK.get(), ModItems.MEDIUM_HOOK.get(), ModItems.SMALL_HOOK.get());
        if (hook == ModItems.DEEP_HOOK.get()) return 8.0F;
        if (hook == ModItems.LARGE_HOOK.get()) return 7.0F;
        if (hook == ModItems.BARBED_HOOK.get()) return 5.5F;
        if (hook == ModItems.MEDIUM_HOOK.get()) return 5.0F;
        return 3.0F;
    }

    private static Optional<CaucaFishingRodItem> rod(ServerPlayer player) {
        if (player.getMainHandItem().getItem() instanceof CaucaFishingRodItem item) return Optional.of(item);
        if (player.getOffhandItem().getItem() instanceof CaucaFishingRodItem item) return Optional.of(item);
        return Optional.empty();
    }

    private static BaitSelection findBait(Inventory inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof BaitItem bait) return new BaitSelection(stack, bait.profile());
        }
        return null;
    }

    private static Item findFirst(Inventory inventory, Item... items) {
        for (Item wanted : items) for (int i = 0; i < inventory.getContainerSize(); i++) if (inventory.getItem(i).is(wanted)) return wanted;
        return null;
    }

    private static void removeOneMatching(Inventory inventory, Item... items) {
        for (Item wanted : items) for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(wanted)) { stack.shrink(1); return; }
        }
    }

    private record BaitSelection(ItemStack stack, BaitProfile profile) {}
    private record EntityAccess(FishEntity fish) {}
}
