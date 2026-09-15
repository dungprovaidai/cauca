package com.dungprovaidai.cauca.fishing;

public record LineProfile(String id, float strength, float visibility, float capacity) {
    public static final LineProfile BASIC = new LineProfile("basic_line", 4.0F, 0.75F, 24.0F);
}
