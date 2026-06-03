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
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;

public class AccountSwitcherScreen
extends Screen {
    private TextFieldWidget usernameBox;
    private String statusMessage = "";
    private int statusColor = 0xFFFFFF;
    private final List<String> accounts = new ArrayList<String>();

    public AccountSwitcherScreen() {
        super((Text)Text.literal((String)"Account Switcher"));
    }

    protected void init() {
        super.init();
        int cx = this.width / 2;
        int sy = this.height / 4 + 20;
        this.usernameBox = new TextFieldWidget(this.textRenderer, cx - 100, sy, 200, 20, (Text)Text.literal((String)"Username"));
        this.usernameBox.setMaxLength(16);
        this.usernameBox.setPlaceholder((Text)Text.literal((String)"Enter username...").styled(s -> s.withColor(0x888888)));
        this.addDrawableChild(this.usernameBox);
        this.addDrawableChild(ButtonWidget.builder((Text)Text.literal((String)"Login"), btn -> this.loginOffline()).dimensions(cx - 100, sy + 30, 200, 20).build());
        this.addDrawableChild(ButtonWidget.builder((Text)Text.literal((String)"Back"), btn -> this.close()).dimensions(cx - 100, this.height - 40, 200, 20).build());
        this.loadAccounts();
        int listY = sy + 70;
        for (int i = 0; i < this.accounts.size() && i < 8; ++i) {
            String accName = this.accounts.get(i);
            int y = listY + i * 24;
            this.addDrawableChild(ButtonWidget.builder((Text)Text.literal((String)accName), btn -> {
                this.usernameBox.setText(accName);
                this.loginOffline();
            }).dimensions(cx - 80, y, 160, 20).build());
        }
    }

    private void loginOffline() {
        String username = this.usernameBox.getText().trim();
        if (username.isEmpty()) {
            this.statusMessage = "Username cannot be empty!";
            this.statusColor = 0xFF5555;
            return;
        }
        try {
            MinecraftClient client = MinecraftClient.getInstance();
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
        for (Field f : MinecraftClient.class.getDeclaredFields()) {
            String typeName = f.getType().getSimpleName();
            if (!typeName.equals("Session") && !typeName.contains("Session")) continue;
            return f;
        }
        for (String name : new String[]{"session", "user", "session"}) {
            try {
                return MinecraftClient.class.getDeclaredField(name);
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
        File file = new File(MinecraftClient.getInstance().runDirectory, "modid_accounts.txt");
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
        File file = new File(MinecraftClient.getInstance().runDirectory, "modid_accounts.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true));){
            writer.println(username);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072693232, -804257776);
        guiGraphics.drawCenteredTextWithShadow(this.textRenderer, "Account Switcher", this.width / 2, 20, 0xFFFFFF);
        if (!this.statusMessage.isEmpty()) {
            guiGraphics.drawCenteredTextWithShadow(this.textRenderer, this.statusMessage, this.width / 2, this.height / 4 + 10, this.statusColor);
        }
        if (!this.accounts.isEmpty()) {
            guiGraphics.drawCenteredTextWithShadow(this.textRenderer, "Saved Accounts:", this.width / 2, this.height / 4 + 58, 0xAAAAAA);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public boolean shouldPause() {
        return false;
    }
}

