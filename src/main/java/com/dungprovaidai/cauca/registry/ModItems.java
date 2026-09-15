package com.dungprovaidai.cauca.registry;

import com.dungprovaidai.cauca.CaucaFishing;
import com.dungprovaidai.cauca.fishing.BaitProfile;
import com.dungprovaidai.cauca.fishing.HookProfile;
import com.dungprovaidai.cauca.fishing.LineProfile;
import com.dungprovaidai.cauca.fishing.RodStats;
import com.dungprovaidai.cauca.item.BaitItem;
import com.dungprovaidai.cauca.item.CaucaFishingRodItem;
import com.dungprovaidai.cauca.item.FishBucketItem;
import com.dungprovaidai.cauca.item.FishMeatItem;
import com.dungprovaidai.cauca.item.FishNetItem;
import com.dungprovaidai.cauca.item.FisherJournalItem;
import com.dungprovaidai.cauca.item.FishingKnifeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ModItems {
    private ModItems() {}

    public static final DeferredItem<CaucaFishingRodItem> BASIC_ROD = rod("basic_fishing_rod", new RodStats("basic", 4.0F, 0.65F, 0.9F, 14.0F, 24.0F, 128, "basic"));
    public static final DeferredItem<CaucaFishingRodItem> REINFORCED_ROD = rod("reinforced_rod", new RodStats("reinforced", 6.0F, 0.62F, 0.85F, 16.0F, 32.0F, 240, "reinforced"));
    public static final DeferredItem<CaucaFishingRodItem> SWIFT_ROD = rod("swift_rod", new RodStats("swift", 4.5F, 0.88F, 1.35F, 15.0F, 24.0F, 180, "swift"));
    public static final DeferredItem<CaucaFishingRodItem> HEAVY_ROD = rod("heavy_rod", new RodStats("heavy", 8.5F, 0.5F, 0.72F, 12.0F, 40.0F, 260, "heavy"));
    public static final DeferredItem<CaucaFishingRodItem> DEEPWATER_ROD = rod("deepwater_rod", new RodStats("deepwater", 7.5F, 0.72F, 0.9F, 20.0F, 44.0F, 300, "deepwater"));
    public static final DeferredItem<CaucaFishingRodItem> LUCKY_ROD = rod("lucky_rod", new RodStats("lucky", 5.0F, 0.8F, 1.0F, 17.0F, 28.0F, 200, "lucky"));

    public static final DeferredItem<BaitItem> WORM = bait("worm", new BaitProfile("worm", java.util.Set.of("cauca_fishing:carp", "cauca_fishing:trout", "cauca_fishing:catfish"), 1.1F, false));
    public static final DeferredItem<BaitItem> GRUB = bait("grub", new BaitProfile("grub", java.util.Set.of("cauca_fishing:pufferfish", "cauca_fishing:tropical_fish", "cauca_fishing:eel"), 1.0F, false));
    public static final DeferredItem<BaitItem> MINNOW = bait("minnow", new BaitProfile("minnow", java.util.Set.of("cauca_fishing:pike", "cauca_fishing:trout", "cauca_fishing:tuna"), 1.2F, false));
    public static final DeferredItem<BaitItem> SHRIMP = bait("shrimp", new BaitProfile("shrimp", java.util.Set.of("cauca_fishing:tuna", "cauca_fishing:great_tuna", "cauca_fishing:pufferfish"), 1.15F, false));
    public static final DeferredItem<BaitItem> GLOW_WORM = bait("glow_worm", new BaitProfile("glow_worm", java.util.Set.of("cauca_fishing:eel", "cauca_fishing:abyssal_fish", "cauca_fishing:golden_fish"), 1.15F, true));
    public static final DeferredItem<BaitItem> FISH_MEAT_BAIT = bait("fish_meat_bait", new BaitProfile("fish_meat", java.util.Set.of("cauca_fishing:catfish", "cauca_fishing:pike", "cauca_fishing:giant_catfish"), 0.95F, false));
    public static final DeferredItem<BaitItem> SPECIAL_BAIT = bait("special_bait", new BaitProfile("special_bait", java.util.Set.of("cauca_fishing:golden_fish", "cauca_fishing:giant_catfish", "cauca_fishing:ancient_carp", "cauca_fishing:great_tuna", "cauca_fishing:abyssal_fish"), 1.3F, true));

    public static final DeferredItem<Item> BASIC_LINE = line("basic_line");
    public static final DeferredItem<Item> STRONG_LINE = line("strong_line");
    public static final DeferredItem<Item> FINE_LINE = line("fine_line");
    public static final DeferredItem<Item> REINFORCED_LINE = line("reinforced_line");
    public static final DeferredItem<Item> SMALL_HOOK = hook("small_hook");
    public static final DeferredItem<Item> MEDIUM_HOOK = hook("medium_hook");
    public static final DeferredItem<Item> LARGE_HOOK = hook("large_hook");
    public static final DeferredItem<Item> BARBED_HOOK = hook("barbed_hook");
    public static final DeferredItem<Item> DEEP_HOOK = hook("deep_hook");

    public static final DeferredItem<FishNetItem> FISH_NET = CaucaFishing.ITEMS.register("fish_net", () -> new FishNetItem(new Item.Properties()));
    public static final DeferredItem<FishingKnifeItem> FISHING_KNIFE = CaucaFishing.ITEMS.register("fishing_knife", () -> new FishingKnifeItem(new Item.Properties()));
    public static final DeferredItem<FishBucketItem> FISH_BUCKET = CaucaFishing.ITEMS.register("fish_bucket", () -> new FishBucketItem(new Item.Properties()));
    public static final DeferredItem<FisherJournalItem> FISHERMANS_JOURNAL = CaucaFishing.ITEMS.register("fishermans_journal", () -> new FisherJournalItem(new Item.Properties()));

    public static final DeferredItem<FishMeatItem> FISH_MEAT = CaucaFishing.ITEMS.register("fish_meat", () -> new FishMeatItem(new Item.Properties()));
    public static final DeferredItem<FishMeatItem> SALTED_FISH = CaucaFishing.ITEMS.register("salted_fish", () -> new FishMeatItem(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(3).saturationModifier(0.45F).build())));
    public static final DeferredItem<FishMeatItem> SMOKED_FISH = CaucaFishing.ITEMS.register("smoked_fish", () -> new FishMeatItem(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(5).saturationModifier(0.7F).build())));
    public static final DeferredItem<FishMeatItem> DRIED_FISH = CaucaFishing.ITEMS.register("dried_fish", () -> new FishMeatItem(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(4).saturationModifier(0.55F).build())));
    public static final DeferredItem<FishMeatItem> FROZEN_FISH = CaucaFishing.ITEMS.register("frozen_fish", () -> new FishMeatItem(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(4).saturationModifier(0.5F).build())));
    public static final DeferredItem<Item> FISH_HEAD = simple("fish_head");
    public static final DeferredItem<Item> FISH_BONE = simple("fish_bone");
    public static final DeferredItem<Item> FISH_SCALE = simple("fish_scale");
    public static final DeferredItem<Item> FISH_SKIN = simple("fish_skin");
    public static final DeferredItem<Item> FISH_FIN = simple("fish_fin");
    public static final DeferredItem<Item> FISH_TEETH = simple("fish_teeth");
    public static final DeferredItem<Item> POISON_SAC = simple("poison_sac");
    public static final DeferredItem<Item> ANCIENT_COIN = simple("ancient_coin");
    public static final DeferredItem<Item> MAP_FRAGMENT = simple("map_fragment");
    public static final DeferredItem<BlockItem> FISH_TROPHY_ITEM = CaucaFishing.ITEMS.registerSimpleBlockItem("fish_trophy", ModBlocks.FISH_TROPHY);

    public static final DeferredItem<BlockItem> FISH_BASKET_ITEM = CaucaFishing.ITEMS.registerSimpleBlockItem("fish_basket", ModBlocks.FISH_BASKET);
    public static final DeferredItem<BlockItem> AQUARIUM_ITEM = CaucaFishing.ITEMS.registerSimpleBlockItem("aquarium", ModBlocks.AQUARIUM);
    public static final DeferredItem<BlockItem> FISH_PROCESSING_TABLE_ITEM = CaucaFishing.ITEMS.registerSimpleBlockItem("fish_processing_table", ModBlocks.FISH_PROCESSING_TABLE);
    public static final DeferredItem<BlockItem> FISHING_MARKET_ITEM = CaucaFishing.ITEMS.registerSimpleBlockItem("fishing_market", ModBlocks.FISHING_MARKET);

    private static DeferredItem<CaucaFishingRodItem> rod(String id, RodStats stats) {
        return CaucaFishing.ITEMS.register(id, () -> new CaucaFishingRodItem(stats, new Item.Properties()));
    }

    private static DeferredItem<BaitItem> bait(String id, BaitProfile profile) {
        return CaucaFishing.ITEMS.register(id, () -> new BaitItem(profile, new Item.Properties()));
    }

    private static DeferredItem<Item> line(String id) {
        return CaucaFishing.ITEMS.register(id, () -> new Item(new Item.Properties().stacksTo(16)));
    }

    private static DeferredItem<Item> hook(String id) {
        return CaucaFishing.ITEMS.register(id, () -> new Item(new Item.Properties().stacksTo(16)));
    }

    private static DeferredItem<Item> simple(String id) {
        return CaucaFishing.ITEMS.register(id, () -> new Item(new Item.Properties()));
    }
}
