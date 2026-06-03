package com.example.client;

import com.example.client.ModMenuInitializer;
import net.fabricmc.api.ClientModInitializer;
public class ExampleModClient
implements ClientModInitializer {
    public void onInitializeClient() {
        ModMenuInitializer.MENU.registerKeybind();
    }
}

