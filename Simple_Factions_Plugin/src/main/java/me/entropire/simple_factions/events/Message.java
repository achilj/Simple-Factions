package me.entropire.simple_factions.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.entropire.simple_factions.Simple_Factions;
import me.entropire.simple_factions.objects.Faction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;

public class Message implements Listener
{
    @EventHandler
    public void MessageSend(AsyncChatEvent e)
    {
        Player player = e.getPlayer();
        // Render the message to plain text for re-use
        String plainMessage = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(e.message());

        if (Simple_Factions.playerDatabase.hasFaction(player))
        {
            int factionId = Simple_Factions.playerDatabase.getFactionId(player);
            Faction faction = Simple_Factions.factionDatabase.getFactionDataById(factionId);
            // Convert stored ChatColor to NamedTextColor via legacy char
            net.kyori.adventure.text.format.TextColor factionColor = LegacyComponentSerializer.legacySection()
                    .deserialize(faction.getColor().toString()).color();
            if (factionColor == null) factionColor = NamedTextColor.WHITE;

            switch (Simple_Factions.playerDatabase.getChat(player.getUniqueId()).toLowerCase()) {
                case "public":
                    e.setCancelled(true);
                    Component prefix = Component.text("[" + faction.getName() + "] ", factionColor);
                    Component publicMsg = prefix
                            .append(Component.text(player.getName() + ": ", NamedTextColor.WHITE))
                            .append(Component.text(plainMessage, NamedTextColor.GRAY));
                    Bukkit.getServer().broadcast(publicMsg);
                    break;
                case "faction":
                    e.setCancelled(true);
                    handleFactionChat(faction, player, plainMessage);
                    break;
                default:
                    player.sendMessage(Component.text("Could not retrieve chat type!", NamedTextColor.RED));
            }
        }
        else
        {
            e.setCancelled(true);
            Component globalMsg = Component.text(player.getName() + ": ", NamedTextColor.WHITE)
                    .append(Component.text(plainMessage, NamedTextColor.GRAY));
            Bukkit.getServer().broadcast(globalMsg);
        }
    }

    private void handleFactionChat(Faction faction, Player player, String message)
    {
        net.kyori.adventure.text.format.TextColor factionColor = LegacyComponentSerializer.legacySection()
                .deserialize(faction.getColor().toString()).color();
        if (factionColor == null) factionColor = NamedTextColor.WHITE;

        Component factionMsg = Component.text("FACTION: ", NamedTextColor.GREEN)
                .append(Component.text("[" + faction.getName() + "] ", factionColor))
                .append(Component.text(player.getName() + ": ", NamedTextColor.WHITE))
                .append(Component.text(message, NamedTextColor.GRAY));

        ArrayList<String> members = faction.getMembers();
        for (String s : members)
        {
            Player member = Bukkit.getPlayer(s);
            if (member != null)
            {
                member.sendMessage(factionMsg);
            }
        }
    }
}
