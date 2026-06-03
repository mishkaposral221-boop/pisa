/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.example.client.menu.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class Easing {
    public static float easeOutCubic(float t) {
        return 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t);
    }
}

