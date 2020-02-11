package me.randomhashtags.itemfilter;

import com.sun.istack.internal.NotNull;
import me.randomhashtags.itemfilter.universal.UVersionable;
import me.randomhashtags.itemfilter.addon.FilterCategory;
import me.randomhashtags.itemfilter.addon.FileFilterCategory;
import me.randomhashtags.itemfilter.universal.UInventory;
import me.randomhashtags.itemfilter.universal.UMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public enum ItemFilterAPI implements CommandExecutor, Listener, UVersionable {
    INSTANCE;

    private boolean isEnabled;

    private File otherdataF;
    private YamlConfiguration otherdata;

    private ItemStack item;
    private ItemMeta itemMeta;
    private List<String> lore;

    private UInventory gui;
    private String enablePrefix, disabledPrefix;
    private List<String> enable, disable, addedLore;
    private HashMap<Integer, String> categorySlots;
    private HashMap<String, FileFilterCategory> categories, categoryTitles;

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player) sender;
        final int l = args.length;
        if(l == 0) {
            viewHelp(player);
        } else {
            switch (args[0]) {
                case "toggle":
                    toggleFilter(player);
                    break;
                case "edit":
                    viewCategories(player);
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    public void load() {
        if(isEnabled) {
            return;
        }
        isEnabled = true;
        final long started = System.currentTimeMillis();

        PLUGIN_MANAGER.registerEvents(this, ITEM_FILTER);

        item = new ItemStack(Material.APPLE);
        itemMeta = item.getItemMeta();
        lore = new ArrayList<>();

        categorySlots = new HashMap<>();
        categories = new HashMap<>();
        categoryTitles = new HashMap<>();

        addedLore = colorizeListString(ITEM_FILTER_CONFIG.getStringList("settings.categories added lore"));
        enablePrefix = colorize(ITEM_FILTER_CONFIG.getString("settings.enabled prefix"));
        enable = colorizeListString(ITEM_FILTER_CONFIG.getStringList("settings.enabled lore"));
        disabledPrefix = colorize(ITEM_FILTER_CONFIG.getString("settings.disabled prefix"));
        disable = colorizeListString(ITEM_FILTER_CONFIG.getStringList("settings.disabled lore"));

        gui = new UInventory(null, ITEM_FILTER_CONFIG.getInt("categories.size"), colorize(ITEM_FILTER_CONFIG.getString("categories.title")));
        final Inventory gi = gui.getInventory();
        for(String s : getConfigurationSectionKeys(ITEM_FILTER_CONFIG, "categories", false, "title", "size")) {
            final String path = "categories." + s + ".", opens = ITEM_FILTER_CONFIG.getString(path + "opens");
            final int slot = ITEM_FILTER_CONFIG.getInt(path + "slot");
            item = createItemStack(ITEM_FILTER_CONFIG, "categories." + s); itemMeta = item.getItemMeta();
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS);
            itemMeta.setLore(addedLore);
            item.setItemMeta(itemMeta);
            gi.setItem(slot, item);
            categorySlots.put(slot, opens);
        }

        save(null, "_data.yml");
        otherdataF = new File(DATA_FOLDER, "_data.yml");
        otherdata = YamlConfiguration.loadConfiguration(otherdataF);
        if(!otherdata.getBoolean("saved default filter categories")) {
            final String[] defaultCategories = new String[] {
                    "EQUIPMENT",
                    "FOOD",
                    "ORES",
                    "OTHER",
                    "POTION_SUPPLIES",
                    "RAIDING",
                    "SPECIALTY",
            };
            for(String s : defaultCategories) {
                save("filter categories", s + ".yml");
            }
            otherdata.set("saved default filter categories", true);
            saveOtherData();
        }
        for(File f : getFilesInFolder(DATA_FOLDER + SEPARATOR + "filter categories")) {
            if(!f.getAbsoluteFile().getName().equals("_settings.yml")) {
                final FileFilterCategory fc = new FileFilterCategory(f);
                categories.put(f.getName(), fc);
                categoryTitles.put(fc.getTitle(), fc);
            }
        }
        sendConsoleDidLoadFeature("ItemFilterAPI", started);
    }
    public void unload() {
        if(isEnabled) {
            isEnabled = false;
            HandlerList.unregisterAll(this);
            FILTER_CATEGORIES.clear();
        }
    }

    private void saveOtherData() {
        try {
            otherdata.save(otherdataF);
            otherdata = YamlConfiguration.loadConfiguration(otherdataF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void viewHelp(@NotNull CommandSender sender) {
        if(sender.hasPermission("ItemFilter.command")) {
            sendStringListMessage(sender, getStringList(ITEM_FILTER_CONFIG, "messages.help"), null);
        }
    }
    public void viewCategories(@NotNull Player player) {
        if(player.hasPermission("ItemFilter.edit")) {
            player.closeInventory();
            player.openInventory(Bukkit.createInventory(player, gui.getSize(), gui.getTitle()));
            player.getOpenInventory().getTopInventory().setContents(gui.getInventory().getContents());
            player.updateInventory();
        }
    }
    private ItemStack getStatus(List<UMaterial> filtered, ItemStack is) {
        itemMeta = is.getItemMeta(); lore.clear();
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS);
        final UMaterial u = UMaterial.match(is);
        final boolean isFiltered = filtered.contains(u);
        itemMeta.setDisplayName((isFiltered ? enablePrefix : disabledPrefix) + ChatColor.stripColor(itemMeta.getDisplayName()));
        itemMeta.setLore(isFiltered ? enable : disable);
        is.setItemMeta(itemMeta);
        if(isFiltered) {
            is.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
        } else {
            is.removeEnchantment(Enchantment.ARROW_DAMAGE);
        }
        return is;
    }
    public void toggleFilter(@NotNull Player player) {
        if(player.hasPermission("ItemFilter.toggle")) {
            final IFPlayer pdata = IFPlayer.get(player.getUniqueId());
            final boolean status = !pdata.hasActiveFilter();
            pdata.setActiveFilter(status);
            sendStringListMessage(player, getStringList(ITEM_FILTER_CONFIG, "messages." + (status ? "en" : "dis") + "able"), null);
        }
    }
    public void viewCategory(@NotNull Player player, @NotNull FilterCategory category) {
        if(player.hasPermission("ItemFilter.edit")) {
            player.closeInventory();
            final List<UMaterial> filtered = IFPlayer.get(player.getUniqueId()).getFilteredItems();
            final UInventory target = category.getInventory();
            final int size = target.getSize();
            player.openInventory(Bukkit.createInventory(player, size, target.getTitle()));
            final Inventory top = player.getOpenInventory().getTopInventory();
            top.setContents(target.getInventory().getContents());
            for(int i = 0; i < size; i++) {
                item = top.getItem(i);
                if(item != null) {
                    top.setItem(i, getStatus(filtered, item.clone()));
                }
            }
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void inventoryClickEvent(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory top = player.getOpenInventory().getTopInventory();
        final String t = event.getView().getTitle();
        final FileFilterCategory category = categoryTitles.getOrDefault(t, null);
        if(t.equals(gui.getTitle()) || category != null) {
            event.setCancelled(true);
            player.updateInventory();
            final ItemStack current = event.getCurrentItem();
            final int slot = event.getRawSlot();
            if(slot < 0 || slot >= top.getSize() || current == null || current.getType().equals(Material.AIR)) {
                return;
            }

            if(category != null) {
                final List<UMaterial> filtered = IFPlayer.get(player.getUniqueId()).getFilteredItems();
                final UMaterial target = UMaterial.match(current);
                if(filtered.contains(target)) {
                    filtered.remove(target);
                } else {
                    filtered.add(target);
                }
                top.setItem(slot, getStatus(filtered, current));
                player.updateInventory();
            } else if(categorySlots.containsKey(slot)) {
                final FilterCategory fc = getFilterCategory(categorySlots.get(slot));
                if(fc != null) {
                    player.closeInventory();
                    viewCategory(player, fc);
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void playerPickupItemEvent(PlayerPickupItemEvent event) {
        final IFPlayer pdata = IFPlayer.get(event.getPlayer().getUniqueId());
        if(pdata.hasActiveFilter() && !pdata.getFilteredItems().contains(UMaterial.match(event.getItem().getItemStack()))) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    private void inventoryCloseEvent(InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        final FileFilterCategory c = categoryTitles.getOrDefault(event.getView().getTitle(), null);
        if(c != null) {
            SCHEDULER.scheduleSyncDelayedTask(ITEM_FILTER, () -> viewCategories(player), 0);
        }
    }
    @EventHandler
    private void playerJoinEvent(PlayerJoinEvent event) {
        IFPlayer.get(event.getPlayer().getUniqueId());
    }
    @EventHandler
    private void playerQuitEvent(PlayerQuitEvent event) {
        IFPlayer.get(event.getPlayer().getUniqueId()).unload();
    }
}
