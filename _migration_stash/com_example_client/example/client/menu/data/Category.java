package com.example.client.menu.data;

import com.example.client.menu.data.ModModule;
import net.minecraft.util.Identifier;

public class Category {
    public final String name;
    public final Identifier icon;
    public final ModModule[] modules;

    public Category(String name, Identifier icon, ModModule[] modules) {
        this.name = name;
        this.icon = icon;
        this.modules = modules;
    }
}

