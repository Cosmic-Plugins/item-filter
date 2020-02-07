package me.randomhashtags.itemfilter.addon.util;

import me.randomhashtags.itemfilter.universal.UInventory;

public interface Inventoryable extends Identifiable {
    String getTitle();
    UInventory getInventory();
}
