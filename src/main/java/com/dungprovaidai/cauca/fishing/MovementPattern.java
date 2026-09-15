package com.dungprovaidai.cauca.fishing;

/** Small strategy enum; the fight controller never needs a species-specific if-chain. */
public enum MovementPattern {
    CRUISE(0.75F, 0.70F, 1.00F),
    DART(1.25F, 0.45F, 1.35F),
    ERRATIC(1.05F, 0.80F, 1.20F),
    DEEP_DIVER(0.90F, 1.10F, 1.15F),
    NIGHT_EEL(1.15F, 0.65F, 1.30F),
    LEGENDARY(1.45F, 1.30F, 1.55F);

    private final float burstMultiplier;
    private final float recoveryMultiplier;
    private final float tensionMultiplier;

    MovementPattern(float burstMultiplier, float recoveryMultiplier, float tensionMultiplier) {
        this.burstMultiplier = burstMultiplier;
        this.recoveryMultiplier = recoveryMultiplier;
        this.tensionMultiplier = tensionMultiplier;
    }

    public float burstMultiplier() {
        return burstMultiplier;
    }

    public float recoveryMultiplier() {
        return recoveryMultiplier;
    }

    public float tensionMultiplier() {
        return tensionMultiplier;
    }

    public static MovementPattern parse(String value) {
        try {
            return value == null ? CRUISE : valueOf(value.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return CRUISE;
        }
    }
}
