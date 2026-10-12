package de.codex.bluegamba;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class BlueGambaMod implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(CommandManager.literal("gamba")
                .executes(context -> {
                    GambaService.open(context.getSource().getPlayerOrThrow());
                    return 1;
                })
                .then(CommandManager.literal("bet")
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            int amount = IntegerArgumentType.getInteger(context, "amount");
                            GambaService.setBetFromCommand(player, amount);
                            return 1;
                        })))
                .then(CommandManager.literal("balance")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        player.sendMessage(Text.literal("Guthaben: "
                            + GambaService.formatCoins(GambaService.getBalanceTenths(player))
                            + " Coins"), false);
                        return 1;
                    }))
            )
        );

        ServerTickEvents.END_SERVER_TICK.register(GambaService::tick);
    }
}
