/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2960
 */
package com.example.client.menu.data;

import com.example.client.menu.data.ModModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;

@Environment(value=EnvType.CLIENT)
public class Category {
    public final String name;
    public final class_2960 icon;
    public final ModModule[] modules;

    public Category(String name, class_2960 icon, ModModule[] modules) {
        this.name = name;
        this.icon = icon;
        this.modules = modules;
    }
}

