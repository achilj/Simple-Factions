package me.entropire.simple_factions.Gui;

import me.entropire.simple_factions.FactionEditor;
import me.entropire.simple_factions.Simple_Factions;
import me.entropire.simple_factions.objects.Faction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class ChangeFactionNameGui extends BaseGui
{
    @Override
    public void open(Player player)
    {
        int factionId = Simple_Factions.playerDatabase.getFactionId(player);
        Faction faction = Simple_Factions.factionDatabase.getFactionDataById(factionId);

        if(faction == null)
        {
            player.sendMessage(Component.text("You must be the owner of the faction to perform this action!", NamedTextColor.RED));
            return;
        }

        new AnvilInputGui(player, "Change faction name", faction.getName(),
                text -> FactionEditor.modifyName(player, text)).open();
    }
}
