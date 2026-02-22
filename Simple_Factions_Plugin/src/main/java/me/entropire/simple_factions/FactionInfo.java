package me.entropire.simple_factions;

import me.entropire.simple_factions.objects.Faction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class FactionInfo
{
    public static void list(Player player, int pageNumber)
    {
        ArrayList<String> factions = Simple_Factions.factionDatabase.getFactions();

        int maxPageNumber = (int)Math.ceil(factions.size() / 9) + 1;

        if(pageNumber > maxPageNumber || pageNumber < 0)
        {
            player.sendMessage(Component.text("Invalid page number!", NamedTextColor.YELLOW));
            return;
        }

        player.sendMessage(Component.text("Factions - " + pageNumber + "/" + maxPageNumber, NamedTextColor.AQUA));

        if(!factions.isEmpty())
        {
            factions.remove(player.getName());

            int endIndex = Math.min(9 * pageNumber, factions.size());
            int startIndex = 9 * (pageNumber - 1);
            for(int i = startIndex; i < endIndex; i++)
            {
                player.sendMessage(Component.text("- " + factions.get(i), NamedTextColor.AQUA));
            }
        }
    }

    public static void members(Player player, String factionName, int pageNumber)
    {
        Faction faction = Simple_Factions.factionDatabase.getFactionDataByName(factionName);

        if(faction == null)
        {
            player.sendMessage(Component.text("There is no faction with the name " + factionName, NamedTextColor.RED));
            return;
        }

        ArrayList<String> members = faction.getMembers();

        int maxPageNumber = (int)Math.ceil(members.size() / 9) + 1;

        if(pageNumber > maxPageNumber || pageNumber < 1)
        {
            player.sendMessage(Component.text("Invalid page number!", NamedTextColor.YELLOW));
            return;
        }

        player.sendMessage(Component.text("Members of " + faction.getName() + " - " + pageNumber + "/" + maxPageNumber, NamedTextColor.AQUA));

        if(!members.isEmpty())
        {
            members.remove(player.getName());

            int endIndex = Math.min(9 * pageNumber, members.size());
            int startIndex = 9 * (pageNumber - 1);
            for(int i = startIndex; i < endIndex; i++)
            {
                player.sendMessage(Component.text("- " + members.get(i), NamedTextColor.AQUA));
            }
        }
    }

    public static void owner(Player player, String factionName)
    {
        if(!Simple_Factions.playerDatabase.hasFaction(player) && factionName == null)
        {
            player.sendMessage(Component.text("Command usage /faction owner [Faction name]", NamedTextColor.RED));
            return;
        }

        Faction faction;

        if(factionName == null || factionName.isEmpty())
        {
            if(Simple_Factions.playerDatabase.hasFaction(player))
            {
                int factionId = Simple_Factions.playerDatabase.getFactionId(player);
                faction = Simple_Factions.factionDatabase.getFactionDataById(factionId);
            }
            else
            {
                player.sendMessage(Component.text("There is no faction with the name " + factionName, NamedTextColor.RED));
                return;
            }
        }
        else
        {
            faction = Simple_Factions.factionDatabase.getFactionDataByName(factionName);
        }

        String ownerUUID = faction.getOwner().toString();
        String ownerName = Simple_Factions.playerDatabase.getPlayerName(ownerUUID);
        player.sendMessage(Component.text("The owner of " + faction.getName() + " is " + ownerName, NamedTextColor.AQUA));
    }
}
