package de.codex.bluegamba;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

final class GambaScreenHandler extends GenericContainerScreenHandler {
    private final SimpleInventory inventory;
    private final ServerPlayerEntity owner;
    private OreSymbol[] reels = {OreSymbol.COAL, OreSymbol.IRON, OreSymbol.GOLD};

    GambaScreenHandler(int syncId, PlayerInventory playerInventory, ServerPlayerEntity owner) {
        this(syncId, playerInventory, owner, new SimpleInventory(27));
    }

    private GambaScreenHandler(int syncId, PlayerInventory playerInventory,
                               ServerPlayerEntity owner, SimpleInventory inventory) {
        super(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, inventory, 3);
        this.inventory = inventory;
        this.owner = owner;
        refresh();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (button != 0 || actionType != SlotActionType.PICKUP || slotIndex < 0 || slotIndex >= 27) {
            return;
        }

        switch (slotIndex) {
            case 9 -> GambaService.adjustBet(owner, -10);
            case 10 -> GambaService.adjustBet(owner, -1);
            case 11, 12, 13 -> {
                // Reels are display-only.
            }
            case 14 -> GambaService.adjustBet(owner, 1);
            case 15 -> GambaService.adjustBet(owner, 10);
            case 22 -> GambaService.startSpin(owner, this);
            default -> {
                // Decorative panes and status items do nothing.
            }
        }
    }

    void refresh() {
        for (int slot = 0; slot < 27; slot++) {
            Item pane = (slot + slot / 9) % 2 == 0
                ? Items.BLUE_STAINED_GLASS_PANE
                : Items.CYAN_STAINED_GLASS_PANE;
            inventory.setStack(slot, named(pane, " "));
        }

        int balance = GambaService.getBalanceTenths(owner);
        inventory.setStack(4, named(Items.LAPIS_LAZULI,
            "Guthaben: " + GambaService.formatCoins(balance) + " Coins"));

        inventory.setStack(9, named(Items.BLUE_DYE, "Einsatz -10"));
        inventory.setStack(10, named(Items.LIGHT_BLUE_DYE, "Einsatz -1"));
        inventory.setStack(14, named(Items.LIGHT_BLUE_DYE, "Einsatz +1"));
        inventory.setStack(15, named(Items.BLUE_DYE, "Einsatz +10"));

        int bet = GambaService.getBet(owner);
        boolean invalidBet = GambaService.isBetInvalid(owner);
        String betLabel = invalidBet
            ? "Einsatz ungültig — setze einen Betrag > 0"
            : "Einsatz: " + bet + " Coins";
        inventory.setStack(16, named(invalidBet ? Items.BARRIER : Items.GOLD_INGOT, betLabel));

        inventory.setStack(11, oreStack(reels[0]));
        inventory.setStack(12, oreStack(reels[1]));
        inventory.setStack(13, oreStack(reels[2]));

        boolean spinning = GambaService.isSpinning(owner);
        boolean canSpin = GambaService.canSpin(owner);
        Item spinIcon = spinning ? Items.CLOCK : (canSpin ? Items.EMERALD : Items.BARRIER);
        String spinLabel = spinning ? "Dreht…" : (canSpin ? "DREHEN" : "Drehung gesperrt");
        inventory.setStack(22, named(spinIcon, spinLabel));

        sendContentUpdates();
    }

    void setReels(OreSymbol first, OreSymbol second, OreSymbol third) {
        reels = new OreSymbol[] {first, second, third};
        inventory.setStack(11, oreStack(first));
        inventory.setStack(12, oreStack(second));
        inventory.setStack(13, oreStack(third));
        sendContentUpdates();
    }

    private static ItemStack oreStack(OreSymbol symbol) {
        return named(symbol.icon(), symbol.label() + " · " + symbol.multiplierLabel());
    }

    private static ItemStack named(Item item, String label) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(label).formatted(Formatting.AQUA));
        return stack;
    }
}
