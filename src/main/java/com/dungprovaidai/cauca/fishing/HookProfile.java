package com.dungprovaidai.cauca.fishing;

public record HookProfile(String id, float strength, int minSize, int maxSize, float biteMultiplier) {
    public static final HookProfile SMALL = new HookProfile("small_hook", 3.0F, 0, 2, 1.15F);
    public static final HookProfile MEDIUM = new HookProfile("medium_hook", 5.0F, 1, 3, 1.0F);
    public static final HookProfile LARGE = new HookProfile("large_hook", 7.0F, 2, 4, 0.78F);
    public static final HookProfile BARBED = new HookProfile("barbed_hook", 5.5F, 1, 4, 1.08F);
    public static final HookProfile DEEP = new HookProfile("deep_hook", 8.0F, 2, 4, 0.72F);

    public boolean accepts(SizeTier tier) {
        int value = tier.ordinal();
        return value >= minSize && value <= maxSize;
    }
}
