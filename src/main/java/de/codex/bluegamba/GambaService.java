package de.codex.bluegamba;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

final class GambaService {
    private static final String OBJECTIVE_ID = "bluegamba_coins";
    private static final int START_BALANCE_TENTHS = 10_000;
    private static final int MAX_BALANCE_TENTHS = 2_000_000_000;
    private static final int MAX_BET = 100_000;

    private static final Map<UUID, Integer> BETS = new HashMap<>();
    private static final Set<UUID> INVALID_BETS = new HashSet<>();
    private static final Map<UUID, Spin> SPINS = new HashMap<>();
    private static final Random RANDOM = new Random();

    private GambaService() {
    }

    static void open(ServerPlayerEntity player) {
        ensureAccount(player);
        player.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
            (syncId, playerInventory, user) -> new GambaScreenHandler(syncId, playerInventory, player),
            Text.literal("Blue Gamba · Ore Slots")
        ));
    }

    static int getBet(ServerPlayerEntity player) {
        return BETS.getOrDefault(player.getUuid(), 10);
    }

    static boolean isSpinning(ServerPlayerEntity player) {
        return SPINS.containsKey(player.getUuid());
    }

    static boolean isBetInvalid(ServerPlayerEntity player) {
        return INVALID_BETS.contains(player.getUuid());
    }

    static boolean canSpin(ServerPlayerEntity player) {
        int bet = getBet(player);
        return !isBetInvalid(player)
            && bet > 0
            && bet <= MAX_BET
            && !isSpinning(player)
            && getBalanceTenths(player) >= bet * 10L;
    }

    static int getBalanceTenths(ServerPlayerEntity player) {
        ScoreboardObjective objective = ensureAccount(player);
        return player.getServer().getScoreboard()
            .getOrCreateScore(player, objective)
            .getScore();
    }

    static String formatCoins(int tenths) {
        return String.format(Locale.GERMANY, "%.1f", tenths / 10.0);
    }

    static void setBetFromCommand(ServerPlayerEntity player, int amount) {
        if (amount <= 0) {
            INVALID_BETS.add(player.getUuid());
            player.sendMessage(Text.literal("Ungültiger Einsatz: Der Betrag muss größer als 0 sein. Die Drehung ist gesperrt."), false);
            refreshOpenMenu(player);
            return;
        }
        if (amount > MAX_BET) {
            player.sendMessage(Text.literal("Der Einsatz darf höchstens " + MAX_BET + " Coins betragen."), false);
            return;
        }

        BETS.put(player.getUuid(), amount);
        INVALID_BETS.remove(player.getUuid());
        player.sendMessage(Text.literal("Einsatz auf " + amount + " Coins gesetzt."), false);
        refreshOpenMenu(player);
    }

    static void adjustBet(ServerPlayerEntity player, int delta) {
        long current = Math.max(0, getBet(player));
        long next = Math.max(1L, Math.min((long) MAX_BET, current + delta));
        BETS.put(player.getUuid(), (int) next);
        INVALID_BETS.remove(player.getUuid());
        refreshOpenMenu(player);
    }

    static void startSpin(ServerPlayerEntity player, GambaScreenHandler menu) {
        if (isSpinning(player)) {
            player.sendMessage(Text.literal("Die Maschine dreht bereits."), false);
            return;
        }

        int bet = getBet(player);
        if (isBetInvalid(player) || bet <= 0) {
            player.sendMessage(Text.literal("Ungültiger Einsatz. Setze zuerst einen Einsatz größer als 0."), false);
            menu.refresh();
            return;
        }
        if (bet > MAX_BET) {
            player.sendMessage(Text.literal("Der Einsatz ist zu hoch."), false);
            menu.refresh();
            return;
        }

        int balance = getBalanceTenths(player);
        int stakeTenths = bet * 10;
        if (balance < stakeTenths) {
            player.sendMessage(Text.literal("Nicht genug Guthaben für diesen Einsatz."), false);
            menu.refresh();
            return;
        }

        setBalance(player.getServer(), player.getGameProfile().getName(), balance - stakeTenths);
        int duration = 40 + RANDOM.nextInt(41);
        SPINS.put(player.getUuid(), new Spin(player.getUuid(),
            player.getGameProfile().getName(), bet, duration, menu));

        player.sendMessage(Text.literal("Drehung gestartet. Einsatz: " + bet + " Coins."), false);
        menu.refresh();
    }

    static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Spin>> iterator = SPINS.entrySet().iterator();
        while (iterator.hasNext()) {
            Spin spin = iterator.next().getValue();
            spin.ticksLeft--;

            ServerPlayerEntity player = server.getPlayerManager().getPlayer(spin.playerId);
            if (spin.ticksLeft > 0 && spin.ticksLeft % 2 == 0
                && player != null && player.currentScreenHandler == spin.menu) {
                spin.menu.setReels(
                    OreSymbol.random(RANDOM),
                    OreSymbol.random(RANDOM),
                    OreSymbol.random(RANDOM)
                );
                spin.menu.refresh();
            }

            if (spin.ticksLeft <= 0) {
                OreSymbol first = OreSymbol.random(RANDOM);
                OreSymbol second = OreSymbol.random(RANDOM);
                OreSymbol third = OreSymbol.random(RANDOM);
                boolean won = first == second && second == third;
                int payoutTenths = won ? spin.bet * first.multiplierTenths() : 0;

                ScoreboardObjective objective = ensureObjective(server.getScoreboard());
                ScoreAccess score = server.getScoreboard()
                    .getOrCreateScore(ScoreHolder.fromName(spin.playerName), objective);
                long newBalance = Math.min(MAX_BALANCE_TENTHS,
                    (long) score.getScore() + payoutTenths);
                score.setScore((int) newBalance);

                if (player != null) {
                    if (player.currentScreenHandler == spin.menu) {
                        spin.menu.setReels(first, second, third);
                        spin.menu.refresh();
                    }
                    String message = won
                        ? "Drei gleiche Symbole: " + first.label() + " — Auszahlung "
                            + formatCoins(payoutTenths) + " Coins (" + first.multiplierLabel() + "). "
                            + "Guthaben: " + formatCoins((int) newBalance) + " Coins."
                        : "Kein Dreier (" + first.label() + ", " + second.label() + ", "
                            + third.label() + ") — Einsatz verloren. Guthaben: "
                            + formatCoins((int) newBalance) + " Coins.";
                    player.sendMessage(Text.literal(message), false);
                }
                iterator.remove();
            }
        }
    }

    private static void refreshOpenMenu(ServerPlayerEntity player) {
        if (player.currentScreenHandler instanceof GambaScreenHandler menu) {
            menu.refresh();
        }
    }

    private static ScoreboardObjective ensureAccount(ServerPlayerEntity player) {
        Scoreboard scoreboard = player.getServer().getScoreboard();
        ScoreboardObjective objective = ensureObjective(scoreboard);
        if (scoreboard.getScore(player, objective) == null) {
            scoreboard.getOrCreateScore(player, objective).setScore(START_BALANCE_TENTHS);
        }
        return objective;
    }

    private static ScoreboardObjective ensureObjective(Scoreboard scoreboard) {
        ScoreboardObjective objective = scoreboard.getNullableObjective(OBJECTIVE_ID);
        if (objective == null) {
            objective = scoreboard.addObjective(OBJECTIVE_ID, ScoreboardCriterion.DUMMY,
                Text.literal("Blue Gamba Coins"), ScoreboardCriterion.RenderType.INTEGER, false, null);
        }
        return objective;
    }

    private static void setBalance(MinecraftServer server, String playerName, int tenths) {
        ScoreboardObjective objective = ensureObjective(server.getScoreboard());
        server.getScoreboard().getOrCreateScore(ScoreHolder.fromName(playerName), objective).setScore(tenths);
    }

    private static final class Spin {
        private final UUID playerId;
        private final String playerName;
        private final int bet;
        private int ticksLeft;
        private final GambaScreenHandler menu;

        private Spin(UUID playerId, String playerName, int bet, int ticksLeft, GambaScreenHandler menu) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.bet = bet;
            this.ticksLeft = ticksLeft;
            this.menu = menu;
        }
    }
}
