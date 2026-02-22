package me.entropire.simple_factions.events;

import me.entropire.simple_factions.Simple_Factions;
import me.entropire.simple_factions.objects.Faction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnJoin implements Listener
{
    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        Player player = e.getPlayer();
        if (!Simple_Factions.playerDatabase.playerExists(player.getName()))
        {
            Simple_Factions.playerDatabase.addPlayer(e.getPlayer());
        }

        int factionId = Simple_Factions.playerDatabase.getFactionId(player);
        if (factionId > 0)
        {
            Faction faction = Simple_Factions.factionDatabase.getFactionDataById(factionId);
            net.kyori.adventure.text.format.TextColor factionColor = LegacyComponentSerializer.legacySection()
                    .deserialize(faction.getColor().toString()).color();
            if (factionColor == null) factionColor = NamedTextColor.WHITE;

            Component prefix = Component.text("[" + faction.getName() + "] ", factionColor);
            Component nameComponent = prefix.append(Component.text(player.getName(), NamedTextColor.WHITE));

            player.displayName(nameComponent);
            player.playerListName(nameComponent);

            Component joinMsg = nameComponent.append(Component.text(" joined the game.", NamedTextColor.YELLOW));
            e.joinMessage(joinMsg);
        }
    }
}
