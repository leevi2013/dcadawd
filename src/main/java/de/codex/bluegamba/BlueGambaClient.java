package de.codex.bluegamba;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class BlueGambaClient implements ClientModInitializer {
    private KeyBinding openGambaKey;

    @Override
    public void onInitializeClient() {
        openGambaKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.blue_gamba.open",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGambaKey.wasPressed()) {
                if (client.player != null
                    && client.getNetworkHandler() != null
                    && client.currentScreen == null) {
                    client.getNetworkHandler().sendChatCommand("gamba");
                }
            }
        });
    }
}
