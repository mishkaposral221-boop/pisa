/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2960
 */
package com.example.client.menu.data;

import com.example.client.menu.data.Category;
import com.example.client.menu.data.ModModule;
import com.example.client.menu.data.Setting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;

@Environment(value=EnvType.CLIENT)
public class MenuData {
    public static final Category[] CATEGORIES = new Category[]{new Category("Visual", class_2960.method_60655((String)"modid", (String)"icons/visual"), new ModModule[]{new ModModule("NoRender", new Setting[]{new Setting("HurtCam", true), new Setting("Fire", true), new Setting("Nausea", true), new Setting("Darkness", true), new Setting("SpeedFX", true), new Setting("Nametag", true)}, false), new ModModule("Fullbright", new Setting[0], false), new ModModule("AspectRatio", new Setting[]{new Setting("Ratio", 50.0, 300.0, 133.0)}, false), new ModModule("GlowESP", new Setting[]{new Setting("Players", true), new Setting("Mobs", false), new Setting("Invisibles", true)}, false), new ModModule("Nametags", new Setting[]{new Setting("Armor", true)}, false)}), new Category("Combat", class_2960.method_60655((String)"modid", (String)"icons/combat"), new ModModule[]{new ModModule("AimAssist", new Setting[]{new Setting("FOV", 10.0, 180.0, 45.0), new Setting("Distance", 1.0, 6.0, 3.0), new Setting("Smoothness", 0.05, 1.0, 0.35), new Setting("Throw", 0.0, 1.0, 0.5), new Setting("OnlyWeapon", true)}, false), new ModModule("Triggerbot", new Setting[]{new Setting("SmartCrits", true)}, false), new ModModule("TargetHUD", new Setting[0], false)}), new Category("Movement", class_2960.method_60655((String)"modid", (String)"icons/movement"), new ModModule[]{new ModModule("AutoSprint", new Setting[0], false)}), new Category("Player", class_2960.method_60655((String)"modid", (String)"icons/player"), new ModModule[]{new ModModule("AutoSwap", new Setting[0], false)}), new Category("Other", class_2960.method_60655((String)"modid", (String)"icons/other"), new ModModule[]{new ModModule("Prediction", new Setting[]{new Setting("Pearl", true), new Setting("Arrow", true), new Setting("Snowball", true), new Setting("Potion", true), new Setting("Trident", true), new Setting("Egg", true)}, false)})};
}

