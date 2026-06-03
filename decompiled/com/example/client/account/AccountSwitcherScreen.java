/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 *  net.minecraft.class_364
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 */
package com.example.client.account;

import com.mojang.authlib.GameProfile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;

@Environment(value=EnvType.CLIENT)
public class AccountSwitcherScreen
extends class_437 {
    private class_342 usernameBox;
    private String statusMessage = "";
    private int statusColor = 0xFFFFFF;
    private final List<String> accounts = new ArrayList<String>();

    public AccountSwitcherScreen() {
        super((class_2561)class_2561.method_43470((String)"Account Switcher"));
    }

    protected void method_25426() {
        super.method_25426();
        int cx = this.field_22789 / 2;
        int sy = this.field_22790 / 4 + 20;
        this.usernameBox = new class_342(this.field_22793, cx - 100, sy, 200, 20, (class_2561)class_2561.method_43470((String)"Username"));
        this.usernameBox.method_1880(16);
        this.usernameBox.method_47404((class_2561)class_2561.method_43470((String)"Enter username...").method_27694(s -> s.method_36139(0x888888)));
        this.method_37063((class_364)this.usernameBox);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Login"), btn -> this.loginOffline()).method_46434(cx - 100, sy + 30, 200, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Back"), btn -> this.method_25419()).method_46434(cx - 100, this.field_22790 - 40, 200, 20).method_46431());
        this.loadAccounts();
        int listY = sy + 70;
        for (int i = 0; i < this.accounts.size() && i < 8; ++i) {
            String accName = this.accounts.get(i);
            int y = listY + i * 24;
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)accName), btn -> {
                this.usernameBox.method_1852(accName);
                this.loginOffline();
            }).method_46434(cx - 80, y, 160, 20).method_46431());
        }
    }

    private void loginOffline() {
        String username = this.usernameBox.method_1882().trim();
        if (username.isEmpty()) {
            this.statusMessage = "Username cannot be empty!";
            this.statusColor = 0xFF5555;
            return;
        }
        try {
            class_310 client = class_310.method_1551();
            GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes()), username);
            Field sessionField = this.findSessionField();
            if (sessionField == null) {
                this.statusMessage = "Cannot find session field";
                this.statusColor = 0xFF5555;
                return;
            }
            sessionField.setAccessible(true);
            Object session = this.createOfflineSession(profile, sessionField.getType());
            if (session == null) {
                this.statusMessage = "Cannot create session object";
                this.statusColor = 0xFF5555;
                return;
            }
            sessionField.set(client, session);
            this.statusMessage = "Logged in as: " + username;
            this.statusColor = 0x55FF55;
            this.saveAccount(username);
        }
        catch (Exception e) {
            this.statusMessage = "Error: " + e.getMessage();
            this.statusColor = 0xFF5555;
        }
    }

    private Field findSessionField() {
        for (Field f : class_310.class.getDeclaredFields()) {
            String typeName = f.getType().getSimpleName();
            if (!typeName.equals("Session") && !typeName.contains("Session")) continue;
            return f;
        }
        for (String name : new String[]{"session", "user", "field_1726"}) {
            try {
                return class_310.class.getDeclaredField(name);
            }
            catch (NoSuchFieldException noSuchFieldException) {
            }
        }
        return null;
    }

    private Object createOfflineSession(GameProfile profile, Class<?> sessionClass) {
        Class<?> typeClass = null;
        for (Class<?> c : sessionClass.getDeclaredClasses()) {
            if (!c.isEnum()) continue;
            typeClass = c;
            break;
        }
        Object offlineType = null;
        if (typeClass != null) {
            for (Object e : typeClass.getEnumConstants()) {
                String name = e.toString().toUpperCase();
                if (!name.contains("LEGACY") && !name.contains("OFFLINE")) continue;
                offlineType = e;
                break;
            }
            if (offlineType == null) {
                offlineType = typeClass.getEnumConstants()[0];
            }
        }
        String uuid = profile.id().toString();
        String name = profile.name();
        for (Constructor<?> ctor : sessionClass.getDeclaredConstructors()) {
            ctor.setAccessible(true);
            Class<?>[] params = ctor.getParameterTypes();
            try {
                Object[] args = new Object[params.length];
                int stringIndex = 0;
                boolean hasGameProfile = false;
                for (int j = 0; j < params.length; ++j) {
                    if (params[j] == GameProfile.class) {
                        args[j] = profile;
                        hasGameProfile = true;
                        continue;
                    }
                    if (params[j] == UUID.class) {
                        args[j] = profile.id();
                        continue;
                    }
                    if (params[j] == String.class) {
                        switch (stringIndex) {
                            case 0: {
                                args[j] = name;
                                break;
                            }
                            case 1: {
                                args[j] = uuid;
                                break;
                            }
                            default: {
                                args[j] = "";
                            }
                        }
                        ++stringIndex;
                        continue;
                    }
                    args[j] = params[j].isEnum() ? offlineType : null;
                }
                Object result = ctor.newInstance(args);
                if (result == null) continue;
                return result;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return null;
    }

    private void loadAccounts() {
        this.accounts.clear();
        File file = new File(class_310.method_1551().field_1697, "modid_accounts.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file));){
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    this.accounts.add(line.trim());
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private void saveAccount(String username) {
        for (String acc : this.accounts) {
            if (!acc.equals(username)) continue;
            return;
        }
        this.accounts.add(username);
        File file = new File(class_310.method_1551().field_1697, "modid_accounts.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true));){
            writer.println(username);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void method_25394(class_332 guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.method_25296(0, 0, this.field_22789, this.field_22790, -1072693232, -804257776);
        guiGraphics.method_25300(this.field_22793, "Account Switcher", this.field_22789 / 2, 20, 0xFFFFFF);
        if (!this.statusMessage.isEmpty()) {
            guiGraphics.method_25300(this.field_22793, this.statusMessage, this.field_22789 / 2, this.field_22790 / 4 + 10, this.statusColor);
        }
        if (!this.accounts.isEmpty()) {
            guiGraphics.method_25300(this.field_22793, "Saved Accounts:", this.field_22789 / 2, this.field_22790 / 4 + 58, 0xAAAAAA);
        }
        super.method_25394(guiGraphics, mouseX, mouseY, partialTick);
    }

    public boolean method_25421() {
        return false;
    }
}

