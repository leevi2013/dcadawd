package de.codex.bluegamba;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.Random;

enum OreSymbol {
    COAL(Items.COAL_ORE, "Kohleerz", 5, "0,5x"),
    IRON(Items.IRON_ORE, "Eisenerz", 12, "1,2x"),
    GOLD(Items.GOLD_ORE, "Golderz", 20, "2,0x"),
    DIAMOND(Items.DIAMOND_ORE, "Diamanterz", 50, "5,0x"),
    EMERALD(Items.EMERALD_ORE, "Smaragderz", 100, "10,0x"),
    ANCIENT_DEBRIS(Items.ANCIENT_DEBRIS, "Netherit-Schrott", 250, "25,0x");

    private final Item icon;
    private final String label;
    private final int multiplierTenths;
    private final String multiplierLabel;

    OreSymbol(Item icon, String label, int multiplierTenths, String multiplierLabel) {
        this.icon = icon;
        this.label = label;
        this.multiplierTenths = multiplierTenths;
        this.multiplierLabel = multiplierLabel;
    }

    Item icon() {
        return icon;
    }

    String label() {
        return label;
    }

    int multiplierTenths() {
        return multiplierTenths;
    }

    String multiplierLabel() {
        return multiplierLabel;
    }

    static OreSymbol random(Random random) {
        OreSymbol[] symbols = values();
        return symbols[random.nextInt(symbols.length)];
    }
}
