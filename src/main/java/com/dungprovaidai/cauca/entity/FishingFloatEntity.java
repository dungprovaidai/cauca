package com.dungprovaidai.cauca.entity;

import com.dungprovaidai.cauca.fishing.FishSize;
import com.dungprovaidai.cauca.fishing.FishSpecies;
import com.dungprovaidai.cauca.fishing.FishState;
import com.dungprovaidai.cauca.fishing.FishingPhase;
import com.dungprovaidai.cauca.fishing.FishingService;
import com.dungprovaidai.cauca.fishing.FishingSpot;
import com.dungprovaidai.cauca.fishing.FishingSpotResolver;
import com.dungprovaidai.cauca.fishing.MovementPattern;
import com.dungprovaidai.cauca.fishing.SpeciesManager;
import com.dungprovaidai.cauca.fishing.SpecialCatchTable;
import com.dungprovaidai.cauca.item.CaucaFishingRodItem;
import com.dungprovaidai.cauca.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;


import java.util.Optional;
import java.util.UUID;

/** Lightweight tracked float and bite marker. The fish is a separate entity linked by id. */
public class FishingFloatEntity extends Entity {
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(FishingFloatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FISH_ID = SynchedEntityData.defineId(FishingFloatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> ANGLER = SynchedEntityData.defineId(FishingFloatEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> TENSION = SynchedEntityData.defineId(FishingFloatEntity.class, EntityDataSerializers.FLOAT);
    private int waitTicks;
    private int biteTicks;
    private String baitId = "";
    private String spotId = "";
    private FishingSpot spot;
    private boolean encounterCreated;
    private net.minecraft.world.item.ItemStack specialCatch = net.minecraft.world.item.ItemStack.EMPTY;

    public FishingFloatEntity(EntityType<? extends FishingFloatEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PHASE, FishingPhase.CAST.ordinal());
        builder.define(FISH_ID, -1);
        builder.define(ANGLER, Optional.empty());
        builder.define(TENSION, 0.0F);
    }

    public void cast(ServerPlayer player, Vec3 target, String bait, FishingSpot spot, int wait) {
        setPos(target);
        entityData.set(ANGLER, Optional.of(player.getUUID()));
        entityData.set(PHASE, FishingPhase.WAITING.ordinal());
        baitId = bait == null ? "" : bait;
        this.spot = spot;
        spotId = spot.type().name().toLowerCase(java.util.Locale.ROOT);
        waitTicks = wait;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        ServerPlayer player = getAngler();
        if (player == null || !player.isAlive() || player.distanceToSqr(this) > 80.0D * 80.0D) {
            discard();
            return;
        }
        FishingPhase phase = phase();
        if (phase == FishingPhase.WAITING) {
            bob();
            if (--waitTicks <= 0) createEncounter(player);
        } else if (phase == FishingPhase.BITE) {
            bob();
            if (--biteTicks <= 0) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.cauca_fishing.missed_bite"));
                FishingService.releaseFish(null, this);
            }
        } else if (phase == FishingPhase.FIGHT || phase == FishingPhase.REEL || phase == FishingPhase.HOOKED) {
            Entity entity = level().getEntity(entityData.get(FISH_ID));
            if (!(entity instanceof FishEntity fish) || fish.isRemoved()) {
                discard();
            } else {
                entityData.set(TENSION, fish.tension());
                if (fish.fishState() == FishState.STRUGGLING && !fish.isInWater()) setPhase(FishingPhase.LANDED);
            }
        } else if (phase == FishingPhase.LANDED || phase == FishingPhase.LINE_SNAPPED) {
            // Keep the float around long enough for the line and landing state to be visible.
            if (tickCount > 80) discard();
        }
    }

    private void createEncounter(ServerPlayer player) {
        if (encounterCreated) return;
        encounterCreated = true;
        if (spot == null) spot = FishingSpotResolver.resolve(level(), blockPosition());
        String weather = level().isRaining() ? "rain" : "clear";
        long day = level().getDayTime() % 24000L;
        String time = day < 12000L ? (day < 3000L ? "dawn" : day > 9000L ? "dusk" : "day") : "night";
        java.util.Optional<net.minecraft.world.item.ItemStack> special = SpecialCatchTable.roll(level().getRandom(), spot, baitId);
        if (special.isPresent()) {
            specialCatch = special.get();
            setPhase(FishingPhase.BITE);
            biteTicks = 28 + (int) (FishingService.sensitivity(player) * 12.0F);
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.cauca_fishing.special_bite"));
            level().broadcastEntityEvent(this, (byte) 2);
            return;
        }
        FishSpecies species = SpeciesManager.pick(level().getRandom(), spot.habitat(), spot.type(), spot.depth(), baitId, weather, time, level().getMoonPhase());
        FishEntity fish = new FishEntity(ModEntities.FISH.get(), level());
        FishSize size = FishSize.roll(species, level().getRandom());
        fish.setSpeciesData(species, size);
        Vec3 fishPos = position().add((level().getRandom().nextDouble() - 0.5D) * 1.5D, -0.45D - Math.min(spot.depth(), 6) * 0.18D, (level().getRandom().nextDouble() - 0.5D) * 1.5D);
        fish.setPos(fishPos);
        fish.beginHooked(player.getUUID(), getId(), baitId, spot, time);
        level().addFreshEntity(fish);
        entityData.set(FISH_ID, fish.getId());
        setPhase(FishingPhase.BITE);
        biteTicks = 28 + (int) (FishingService.sensitivity(player) * 12.0F);
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.cauca_fishing.bite", species.displayName()));
        level().broadcastEntityEvent(this, (byte) 2);
    }

    private void bob() {
        setPos(getX(), getY() + Math.sin((tickCount + getId()) * 0.18D) * 0.006D, getZ());
    }

    public ServerPlayer getAngler() {
        if (!(level() instanceof ServerLevel server)) return null;
        return entityData.get(ANGLER).map(uuid -> server.getServer().getPlayerList().getPlayer(uuid)).orElse(null);
    }

    public UUID anglerUUID() { return entityData.get(ANGLER).orElse(null); }
    public int fishId() { return entityData.get(FISH_ID); }
    public FishingPhase phase() {
        int id = Math.max(0, Math.min(FishingPhase.values().length - 1, entityData.get(PHASE)));
        return FishingPhase.values()[id];
    }
    public float tension() { return entityData.get(TENSION); }
    public String baitId() { return baitId; }
    public String spotId() { return spotId; }
    public FishingSpot spot() { return spot; }
    public net.minecraft.world.item.ItemStack takeSpecialCatch() {
        net.minecraft.world.item.ItemStack result = specialCatch;
        specialCatch = net.minecraft.world.item.ItemStack.EMPTY;
        return result;
    }
    public void setPhase(FishingPhase phase) { entityData.set(PHASE, phase.ordinal()); }
    public void setFishId(int id) { entityData.set(FISH_ID, id); }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(PHASE, tag.getInt("Phase"));
        entityData.set(FISH_ID, tag.getInt("Fish"));
        baitId = tag.getString("Bait");
        spotId = tag.getString("Spot");
        waitTicks = tag.getInt("Wait");
        if (tag.hasUUID("Angler")) entityData.set(ANGLER, Optional.of(tag.getUUID("Angler")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Phase", phase().ordinal());
        tag.putInt("Fish", fishId());
        tag.putString("Bait", baitId);
        tag.putString("Spot", spotId);
        tag.putInt("Wait", waitTicks);
        if (anglerUUID() != null) tag.putUUID("Angler", anglerUUID());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(net.minecraft.server.level.ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 2 && level().isClientSide()) {
            for (int i = 0; i < 4; i++) level().addParticle(net.minecraft.core.particles.ParticleTypes.SPLASH,
                    getX() + (getRandom().nextDouble() - 0.5D) * 0.4D, getY(),
                    getZ() + (getRandom().nextDouble() - 0.5D) * 0.4D, 0.0D, 0.06D, 0.0D);
        }
        super.handleEntityEvent(id);
    }
}
