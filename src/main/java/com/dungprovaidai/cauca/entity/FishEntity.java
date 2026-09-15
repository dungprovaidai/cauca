package com.dungprovaidai.cauca.entity;

import com.dungprovaidai.cauca.component.FishCatchData;
import com.dungprovaidai.cauca.fishing.FishQuality;
import com.dungprovaidai.cauca.fishing.FishSize;
import com.dungprovaidai.cauca.fishing.FishSpecies;
import com.dungprovaidai.cauca.fishing.FishingPhase;
import com.dungprovaidai.cauca.fishing.FishingService;
import com.dungprovaidai.cauca.fishing.FishingSpot;
import com.dungprovaidai.cauca.fishing.FishingSpotResolver;
import com.dungprovaidai.cauca.fishing.FishState;
import com.dungprovaidai.cauca.fishing.MovementPattern;
import com.dungprovaidai.cauca.fishing.SizeTier;
import com.dungprovaidai.cauca.fishing.SpeciesManager;
import com.dungprovaidai.cauca.item.FishNetItem;
import com.dungprovaidai.cauca.registry.ModComponents;
import com.dungprovaidai.cauca.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.NetworkHooks;

import java.util.Optional;
import java.util.UUID;

/**
 * A real, tracked fish entity. It has no health and cannot be damaged like a mob;
 * the only fight variables are stamina, movement and line tension.
 */
public class FishEntity extends Entity {
    private static final EntityDataAccessor<String> SPECIES = SynchedEntityData.defineId(FishEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(FishEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WEIGHT = SynchedEntityData.defineId(FishEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STAMINA = SynchedEntityData.defineId(FishEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TENSION = SynchedEntityData.defineId(FishEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(FishEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FLOAT_ID = SynchedEntityData.defineId(FishEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> ANGLER = SynchedEntityData.defineId(FishEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> REELING = SynchedEntityData.defineId(FishEntity.class, EntityDataSerializers.BOOLEAN);

    private int movementTicks;
    private float movementYaw;
    private int reelingTicks;
    private int overTensionTicks;
    private int snagTicks;
    private int landedTicks;
    private boolean dropped;
    private boolean hooked;
    private String baitId = "";
    private String spotId = "river";
    private String timeId = "day";
    private SizeTier sizeTier = SizeTier.AVERAGE;
    private FishQuality quality = FishQuality.GOOD;

    public FishEntity(EntityType<? extends FishEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public static FishEntity fromCatch(EntityType<? extends FishEntity> type, Level level, FishCatchData data) {
        FishEntity fish = new FishEntity(type, level);
        fish.entityData.set(SPECIES, data.species());
        fish.entityData.set(LENGTH, data.lengthCm());
        fish.entityData.set(WEIGHT, data.weightKg());
        fish.entityData.set(STAMINA, Math.max(1.0F, data.weightKg() * 4.0F));
        fish.quality = FishQuality.values()[Math.max(0, Math.min(FishQuality.values().length - 1, data.quality()))];
        fish.sizeTier = FishSpeciesData.tier(data.weightKg(), SpeciesManager.get(data.species()));
        return fish;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPECIES, "cauca_fishing:cod");
        builder.define(LENGTH, 35.0F);
        builder.define(WEIGHT, 0.5F);
        builder.define(STAMINA, 25.0F);
        builder.define(TENSION, 0.0F);
        builder.define(STATE, FishState.HEALTHY.ordinal());
        builder.define(FLOAT_ID, -1);
        builder.define(ANGLER, Optional.empty());
        builder.define(REELING, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        if (isDeadFish()) {
            tickDead();
            return;
        }

        if (!isInWater()) {
            tickOnLand();
        } else if (hooked) {
            tickFight();
        } else {
            tickFreeSwimming();
        }

        if (tickCount % 5 == 0) syncFightState();
    }

    private void tickFreeSwimming() {
        FishSpecies species = species();
        if (movementTicks-- <= 0) chooseMovement(species);
        Vec3 swim = new Vec3(-Math.sin(Math.toRadians(movementYaw)), 0.0D, Math.cos(Math.toRadians(movementYaw)))
                .scale(0.012D * species.movementPattern().burstMultiplier());
        setDeltaMovement(getDeltaMovement().scale(0.82D).add(swim));
        move(MoverType.SELF, getDeltaMovement());
        setYRot(movementYaw);
    }

    private void tickFight() {
        ServerPlayer angler = getAngler();
        if (angler == null || !angler.isAlive()) {
            FishingService.releaseFish(this);
            return;
        }
        FishSpecies species = species();
        if (movementTicks-- <= 0) chooseMovement(species);

        Vec3 line = angler.getEyePosition().subtract(position());
        double distance = line.length();
        Vec3 direction = distance < 0.01D ? Vec3.ZERO : line.scale(1.0D / distance);
        float sizePressure = species.sizePressure(weightKg());
        float rodPower = FishingService.rodPower(angler);

        if (reelingTicks > 0) {
            reelingTicks--;
            entityData.set(REELING, true);
            double pull = 0.012D * rodPower * (1.0D - sizePressure * 0.45D);
            setDeltaMovement(getDeltaMovement().scale(0.78D).add(direction.scale(pull)));
            entityData.set(STAMINA, Math.max(0.0F, stamina() - (0.12F + sizePressure * 0.12F)));
        } else {
            entityData.set(REELING, false);
            entityData.set(STAMINA, Math.min(species.stamina(), stamina() + species.movementPattern().recoveryMultiplier() * 0.035F));
        }

        Vec3 burst = new Vec3(-Math.sin(Math.toRadians(movementYaw)),
                species.movementPattern() == MovementPattern.DEEP_DIVER ? -0.008D : (species.movementPattern() == MovementPattern.DART ? 0.004D : 0.0D),
                Math.cos(Math.toRadians(movementYaw))).scale(0.018D * species.movementPattern().burstMultiplier());
        setDeltaMovement(getDeltaMovement().add(burst));
        move(MoverType.SELF, getDeltaMovement());
        if (getDeltaMovement().horizontalDistanceSqr() > 0.00001D) setYRot((float) (Math.toDegrees(Math.atan2(-getDeltaMovement().x, getDeltaMovement().z))));

        float lineStrength = FishingService.lineStrength(angler);
        float hookStrength = FishingService.hookStrength(angler);
        float safeTension = Math.min(100.0F, Math.min(FishingService.rodStrength(angler), Math.min(lineStrength, hookStrength)) * 10.0F);
        float movementTension = (float) (getDeltaMovement().length() * 110.0D * species.movementPattern().tensionMultiplier());
        float distanceTension = (float) Math.max(0.0D, distance - FishingService.lineSlack(angler)) * 2.5F;
        float terrainTension = terrainResistance(angler);
        float nextTension = tension() + (movementTension + distanceTension + terrainTension + (isReeling() ? 0.8F : 0.0F)) * 0.055F;
        nextTension -= isReeling() ? 0.02F : 0.34F;
        nextTension = Math.max(0.0F, Math.min(100.0F, nextTension));
        entityData.set(TENSION, nextTension);

        if (terrainTension > 0.0F) snagTicks++; else snagTicks = Math.max(0, snagTicks - 2);
        if (nextTension > safeTension) overTensionTicks++; else overTensionTicks = Math.max(0, overTensionTicks - 2);
        if (overTensionTicks > 32 || snagTicks > 70) {
            FishingService.lineSnap(this, angler);
            return;
        }
        if (stamina() <= 0.0F) {
            setFishState(FishState.WEAK);
            // Exhausted fish stops generating large bursts and can be landed.
            setDeltaMovement(getDeltaMovement().scale(0.35D));
        } else if (stamina() < species.stamina() * 0.35F) {
            setFishState(FishState.STRUGGLING);
        } else {
            setFishState(FishState.HEALTHY);
        }

        if (tickCount % 45 == 0 && !isInWater()) setFishState(FishState.STRUGGLING);
    }

    private float terrainResistance(ServerPlayer angler) {
        Vec3 start = position();
        Vec3 end = angler.getEyePosition();
        int samples = Math.max(2, (int) (start.distanceTo(end) * 2.0D));
        float result = 0.0F;
        for (int i = 1; i < samples; i++) {
            Vec3 point = start.lerp(end, i / (double) samples);
            BlockState state = level().getBlockState(BlockPos.containing(point));
            if (state.is(net.minecraft.tags.BlockTags.LOGS) || state.is(Blocks.STONE) || state.is(Blocks.GRAVEL)
                    || state.is(Blocks.SAND) || state.is(Blocks.ICE) || state.is(Blocks.KELP)
                    || state.is(Blocks.KELP_PLANT) || state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS)) {
                result += 1.4F;
            }
        }
        return Math.min(8.0F, result);
    }

    private void tickOnLand() {
        landedTicks++;
        hooked = false;
        entityData.set(REELING, false);
        if (fishState() == FishState.HEALTHY) setFishState(FishState.STRUGGLING);
        if (landedTicks > 80 && fishState() == FishState.STRUGGLING) setFishState(FishState.WEAK);
        if (landedTicks > 260) {
            setFishState(FishState.DEAD);
            dropNaturalCatch();
            return;
        }

        BlockPos nearestWater = nearestWater();
        if (nearestWater != null) {
            Vec3 toWater = Vec3.atCenterOf(nearestWater).subtract(position());
            if (toWater.lengthSqr() < 2.5D) setDeltaMovement(getDeltaMovement().add(0.0D, 0.08D, 0.0D));
            else setDeltaMovement(getDeltaMovement().scale(0.72D).add(toWater.normalize().scale(0.018D)));
        }
        if (tickCount % 18 == 0) {
            setDeltaMovement(getDeltaMovement().add(0.0D, 0.13D, 0.0D));
            level().broadcastEntityEvent(this, (byte) 2);
        }
        setDeltaMovement(getDeltaMovement().add(0.0D, -0.035D, 0.0D));
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(0.88D));
        setYRot(getYRot() + 8.0F);
    }

    private void tickDead() {
        if (!dropped) dropNaturalCatch();
        if (tickCount % 20 == 0) discard();
    }

    private BlockPos nearestWater() {
        BlockPos origin = blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos candidate : BlockPos.betweenClosed(origin.offset(-6, -2, -6), origin.offset(6, 2, 6))) {
            if (level().getFluidState(candidate).is(net.minecraft.tags.FluidTags.WATER)) {
                double distance = candidate.distSqr(origin);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = candidate.immutable();
                }
            }
        }
        return best;
    }

    private void chooseMovement(FishSpecies species) {
        movementTicks = 14 + getRandom().nextInt(48);
        movementYaw = getRandom().nextFloat() * 360.0F;
        if (species.movementPattern() == MovementPattern.DEEP_DIVER) movementYaw += 180.0F;
        if (species.movementPattern() == MovementPattern.ERRATIC) movementTicks = 5 + getRandom().nextInt(18);
        if (getRandom().nextFloat() < 0.18F) level().broadcastEntityEvent(this, (byte) 2);
    }

    public void beginHooked(UUID owner, int floatId, String bait, FishingSpot spot, String time) {
        hooked = true;
        baitId = bait == null ? "" : bait;
        spotId = spot.type().name().toLowerCase(java.util.Locale.ROOT);
        timeId = time;
        entityData.set(ANGLER, Optional.of(owner));
        entityData.set(FLOAT_ID, floatId);
        setFishState(FishState.HEALTHY);
    }

    public void requestReel() {
        if (hooked && !isDeadFish()) reelingTicks = Math.max(reelingTicks, 9);
    }

    public void land() {
        hooked = false;
        entityData.set(FLOAT_ID, -1);
        entityData.set(TENSION, 0.0F);
        setFishState(FishState.STRUGGLING);
    }

    public boolean isCapturable() {
        return !isDeadFish() && !isInWater() && (fishState() == FishState.STRUGGLING || fishState() == FishState.WEAK);
    }

    public void capture(Player player, ItemStack tool, InteractionHand hand) {
        if (level().isClientSide() || !isCapturable()) return;
        FishCatchData data = catchData(true);
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.WATER_BUCKET) || held.is(ModItems.FISH_BUCKET.get())) {
            player.setItemInHand(hand, com.dungprovaidai.cauca.item.FishBucketItem.filled(data));
        } else if (tool.getItem() instanceof FishNetItem) {
            ItemStack liveBucket = com.dungprovaidai.cauca.item.FishBucketItem.filled(data);
            if (!player.getInventory().add(liveBucket)) player.drop(liveBucket, false);
            tool.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        } else {
            return;
        }
        recordCatch(player);
        FishingService.releaseFish(this);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        if (stack.getItem() instanceof FishNetItem || stack.is(Items.WATER_BUCKET) || stack.is(ModItems.FISH_BUCKET.get())) {
            capture(player, stack, hand);
            return InteractionResult.CONSUME;
        }
        if (stack.is(ModItems.FISHING_KNIFE.get()) && isCapturable()) {
            killAndProcess(player, hand);
            return InteractionResult.CONSUME;
        }
        if (isCapturable()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.cauca_fishing.fish_needs_tool"));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private void killAndProcess(Player player, InteractionHand hand) {
        recordCatch(player);
        dropNaturalCatch();
        ItemStack knife = player.getItemInHand(hand);
        knife.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        discard();
    }

    private void dropNaturalCatch() {
        if (dropped || level().isClientSide()) return;
        dropped = true;
        FishCatchData data = catchData(false);
        ItemStack meat = new ItemStack(ModItems.FISH_MEAT.get());
        meat.set(ModComponents.FISH_CATCH, data);
        spawnAtLocation(meat, 0.15F);
        if (species().id().getPath().equals("pufferfish")) spawnAtLocation(new ItemStack(ModItems.POISON_SAC.get()), 0.15F);
        else {
            spawnAtLocation(new ItemStack(ModItems.FISH_BONE.get()), 0.15F);
            if (quality.ordinal() >= FishQuality.GOOD.ordinal()) spawnAtLocation(new ItemStack(ModItems.FISH_SCALE.get()), 0.15F);
        }
    }

    private void recordCatch(Player player) {
        if (player instanceof ServerPlayer serverPlayer) FishingService.recordCatch(serverPlayer, this);
    }

    public FishCatchData catchData(boolean alive) {
        return new FishCatchData(speciesId(), weightKg(), lengthCm(), quality.ordinal(), alive ? 1200 : 600, alive);
    }

    private void syncFightState() {
        ServerPlayer player = getAngler();
        if (player != null) FishingService.syncState(player, this);
    }

    public ServerPlayer getAngler() {
        if (!(level() instanceof ServerLevel server)) return null;
        return entityData.get(ANGLER).map(uuid -> server.getServer().getPlayerList().getPlayer(uuid)).orElse(null);
    }

    public FishSpecies species() {
        return SpeciesManager.get(speciesId());
    }

    public String speciesId() { return entityData.get(SPECIES); }
    public float lengthCm() { return entityData.get(LENGTH); }
    public float weightKg() { return entityData.get(WEIGHT); }
    public float stamina() { return entityData.get(STAMINA); }
    public float tension() { return entityData.get(TENSION); }
    public FishState fishState() { return FishState.values()[Math.max(0, Math.min(FishState.values().length - 1, entityData.get(STATE)))]; }
    public boolean isDeadFish() { return fishState() == FishState.DEAD; }
    public boolean isReeling() { return entityData.get(REELING); }
    public boolean isHooked() { return hooked; }
    public String baitId() { return baitId; }
    public String spotId() { return spotId; }
    public String timeId() { return timeId; }
    public int floatId() { return entityData.get(FLOAT_ID); }
    public UUID anglerUUID() { return entityData.get(ANGLER).orElse(null); }
    public FishQuality quality() { return quality; }
    public SizeTier sizeTier() { return sizeTier; }
    public void setSpeciesData(FishSpecies species, FishSize size) {
        entityData.set(SPECIES, species.id().toString());
        entityData.set(LENGTH, size.lengthCm());
        entityData.set(WEIGHT, size.weightKg());
        entityData.set(STAMINA, species.stamina() * (0.8F + size.tier().ordinal() * 0.12F));
        sizeTier = size.tier();
        quality = size.quality();
    }
    public void setFishState(FishState state) { entityData.set(STATE, state.ordinal()); }
    public float renderScale() { return Math.max(0.45F, Math.min(2.5F, lengthCm() / 52.0F)); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Fish are intentionally not combat mobs: knife capture is an explicit interaction.
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(SPECIES, tag.getString("Species"));
        entityData.set(LENGTH, tag.getFloat("Length"));
        entityData.set(WEIGHT, tag.getFloat("Weight"));
        entityData.set(STAMINA, tag.getFloat("Stamina"));
        entityData.set(TENSION, tag.getFloat("Tension"));
        entityData.set(STATE, tag.getInt("State"));
        baitId = tag.getString("Bait");
        spotId = tag.getString("Spot");
        timeId = tag.getString("Time");
        hooked = tag.getBoolean("Hooked");
        if (tag.hasUUID("Angler")) entityData.set(ANGLER, Optional.of(tag.getUUID("Angler")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("Species", speciesId());
        tag.putFloat("Length", lengthCm());
        tag.putFloat("Weight", weightKg());
        tag.putFloat("Stamina", stamina());
        tag.putFloat("Tension", tension());
        tag.putInt("State", fishState().ordinal());
        tag.putString("Bait", baitId);
        tag.putString("Spot", spotId);
        tag.putString("Time", timeId);
        tag.putBoolean("Hooked", hooked);
        if (anglerUUID() != null) tag.putUUID("Angler", anglerUUID());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(net.minecraft.server.level.ServerEntity entity) {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 2 && level().isClientSide()) {
            for (int i = 0; i < 5; i++) level().addParticle(net.minecraft.core.particles.ParticleTypes.SPLASH,
                    getX() + (getRandom().nextDouble() - 0.5D) * 0.5D, getY() + 0.1D,
                    getZ() + (getRandom().nextDouble() - 0.5D) * 0.5D, 0.0D, 0.08D, 0.0D);
        }
        super.handleEntityEvent(id);
    }

    private static final class FishSpeciesData {
        private static SizeTier tier(float weight, FishSpecies species) {
            float pressure = species.sizePressure(weight);
            return SizeTier.fromPercentile(pressure, species.legendary());
        }
    }
}
