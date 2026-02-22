package me.entropire.simple_factions.Gui;

import me.entropire.simple_factions.Simple_Factions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Chat-based name input GUI.
 * Click the name tag to enter a name via chat; Accept confirms, Cancel aborts.
 */
public class AnvilInputGui implements Listener {

    private final Player player;
    private final String prompt;
    private final Consumer<String> onConfirm;

    private Inventory gui;
    private String currentInput = "";
    private boolean waitingForChat = false;

    public AnvilInputGui(Player player, String prompt, String initialText, Consumer<String> onConfirm) {
        this.player = player;
        this.prompt = prompt;
        this.onConfirm = onConfirm;
        if (initialText != null && !initialText.isEmpty()) {
            this.currentInput = initialText;
        }
    }

    public void open() {
        Bukkit.getPluginManager().registerEvents(this, Simple_Factions.plugin);
        openGui();
    }

    private void openGui() {
        waitingForChat = false;

        gui = Bukkit.createInventory(null, 27,
                Component.text(prompt, NamedTextColor.DARK_GRAY));

        refreshGui();
        player.openInventory(gui);
    }

    /** Rebuilds the GUI items to reflect the current input. */
    private void refreshGui() {
        // Slot 11 – Accept (green)
        gui.setItem(11, makeItem(Material.LIME_CONCRETE,
                Component.text("✔ Accept", NamedTextColor.GREEN),
                Component.text("Click to confirm the name.", NamedTextColor.GRAY)));

        // Slot 13 – Current name / click to edit
        Component nameDisplay = currentInput.isEmpty()
                ? Component.text("(click to set a name)", NamedTextColor.DARK_GRAY)
                : Component.text(currentInput, NamedTextColor.WHITE);
        gui.setItem(13, makeItem(Material.NAME_TAG,
                nameDisplay,
                Component.text("Click to type a new name in chat.", NamedTextColor.GRAY)));

        // Slot 15 – Cancel (red)
        gui.setItem(15, makeItem(Material.RED_CONCRETE,
                Component.text("✘ Cancel", NamedTextColor.RED),
                Component.text("Close without saving.", NamedTextColor.GRAY)));
    }

    private ItemStack makeItem(Material mat, Component name, Component... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        if (!clicker.equals(player)) return;
        if (!event.getView().title().equals(Component.text(prompt, NamedTextColor.DARK_GRAY))) return;

        event.setCancelled(true);

        int slot = event.getSlot();

        if (slot == 13) {
            // Name tag clicked — close GUI and wait for chat input
            waitingForChat = true;
            player.closeInventory();
            player.sendMessage(Component.text("─────────────────────────────────", NamedTextColor.DARK_GRAY));
            player.sendMessage(Component.text("Type your faction name in chat:", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("─────────────────────────────────", NamedTextColor.DARK_GRAY));

        } else if (slot == 11) {
            // Accept
            if (currentInput.isEmpty()) {
                player.sendMessage(Component.text("Click the name tag first to set a name!", NamedTextColor.RED));
                return;
            }
            String confirmed = currentInput;
            cleanup();
            player.closeInventory();
            onConfirm.accept(confirmed);

        } else if (slot == 15) {
            // Cancel
            cleanup();
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getPlayer().equals(player)) return;
        if (!event.getView().title().equals(Component.text(prompt, NamedTextColor.DARK_GRAY))) return;
        // Only clean up if we are NOT about to reopen for chat input
        if (!waitingForChat) {
            cleanup();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onChat(AsyncChatEvent event) {
        if (!event.getPlayer().equals(player)) return;
        if (!waitingForChat) return;

        // Intercept the message — don't send it to anyone
        event.setCancelled(true);
        currentInput = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        // Reopen the GUI on the main thread with the updated name
        Bukkit.getScheduler().runTask(Simple_Factions.plugin, this::openGui);
    }

    private void cleanup() {
        HandlerList.unregisterAll(this);
    }
}
