package com.dungprovaidai.cauca.fishing;

import java.util.Set;

public record BaitProfile(String id, Set<String> preferredSpecies, float encounterMultiplier, boolean nightGlow) {
    public boolean prefers(String speciesId) {
        return preferredSpecies.isEmpty() || preferredSpecies.contains(speciesId);
    }
}
