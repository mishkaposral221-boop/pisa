package com.example.client.menu.util;

public class Easing {
    public static float easeOutCubic(float t) {
        return 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t);
    }
}

