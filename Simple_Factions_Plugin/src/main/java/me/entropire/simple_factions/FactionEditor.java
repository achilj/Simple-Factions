package me.entropire.simple_factions;

import me.entropire.simple_factions.objects.Colors;
import me.entropire.simple_factions.objects.Faction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class FactionEditor
{
    public static void create(Player player, String factionName)
    {
        if(Simple_Factions.playerDatabase.hasFaction(player))
        {
            player.sendMessage(Component.text("You are already in a faction.", NamedTextColor.RED));
            return;
        }

        factionName = factionName.toLowerCase();

        if(factionName.matches(".*[^a-zA-Z].*"))
        {
            player.sendMessage(Component.text("Special characters are not allowed in faction names!", NamedTextColor.RED));
            return;
        }

        if(Simple_Factions.plugin.getSettingsConfig().getBoolean("enable-bannedNames", true) && Simple_Factions.plugin.getBannedWordsConfig().getStringList("BannedNames").contains(factionName))
        {
            player.sendMessage(Component.text("This name has been banned and cannot be used!", NamedTextColor.RED));
            return;
        }

        if(Simple_Factions.factionDatabase.factionExistsByName(factionName))
        {
            player.sendMessage(Component.text("The name " + factionName + " is already in use by another faction!", NamedTextColor.RED));
            return;
        }

        ArrayList<String> members = new ArrayList<>();
        members.add(player.getName());
        Faction faction = new Faction(0, factionName, ChatColor.WHITE, player.getUniqueId(), members);

        Simple_Factions.factionDatabase.addFaction(faction);
        Simple_Factions.playerDatabase.updateFactionWithPlayerName(player.getName(), Simple_Factions.factionDatabase.getFactionDataByName(factionName).getId());

        changePlayerDisplayName(player, buildNameComponent(faction, player.getName()));

        player.sendMessage(Component.text("New faction " + factionName + " created", NamedTextColor.AQUA));
    }

    public static void kick(Player player, String playerName)
    {
        if(!Simple_Factions.playerDatabase.hasFaction(player))
        {
            player.sendMessage(Component.text("You must be in a faction to perform this action!", NamedTextColor.RED));
            return;
        }

        int factionId = Simple_Factions.playerDatabase.getFactionId(player);
        Faction factionData = Simple_Factions.factionDatabase.getFactionDataById(factionId);
        if(!factionData.getOwner().equals(player.getUniqueId()))
        {
            player.sendMessage(Component.text("You must be the owner of the faction to perform this action!", NamedTextColor.RED));
            return;
        }
        if(!factionData.getMembers().contains(playerName))
        {
            player.sendMessage(Component.text("This player is not a part of your faction!", NamedTextColor.RED));
            return;
        }
        UUID playerToKickId = Simple_Factions.playerDatabase.getPlayerUUID(playerName);
        if(factionData.getOwner().equals(playerToKickId))
        {
            player.sendMessage(Component.text("You cannot kick the owner from the faction!", NamedTextColor.RED));
            return;
        }
        Player playerToKick = player.getServer().getPlayer(playerName);

        Simple_Factions.factionDatabase.updateFactionMembers(factionId, playerName, false);
        Simple_Factions.playerDatabase.updateFactionWithPlayerName(playerName, 0);

        player.sendMessage(Component.text("You have kicked " + playerName + " out of your faction", NamedTextColor.AQUA));
        if(playerToKick != null){
            changePlayerDisplayName(playerToKick, Component.text(playerToKick.getName()));
            if(playerToKick.isOnline()) playerToKick.sendMessage(Component.text("You have been kicked from your faction", NamedTextColor.AQUA));
        }
    }

    public static void leave(Player player)
    {
        if(!Simple_Factions.playerDatabase.hasFaction(player))
        {
            player.sendMessage(Component.text("You must be in a faction to perform this action!", NamedTextColor.RED));
            return;
        }

        int factionId = Simple_Factions.playerDatabase.getFactionId(player);
        Faction factionData = Simple_Factions.factionDatabase.getFactionDataById(factionId);
        if(factionData.getOwner().equals(player.getUniqueId()))
        {
            player.sendMessage(Component.text("You cannot perform this action as owner of the faction!", NamedTextColor.RED));
            return;
        }

        Simple_Factions.factionDatabase.updateFactionMembers(factionId, player.getName(), false);
        Simple_Factions.playerDatabase.updateFactionWithPlayerName(player.getName(), 0);

        changePlayerDisplayName(player, Component.text(player.getName()));
        player.sendMessage(Component.text("You have left your faction", NamedTextColor.AQUA));
    }

    public static void delete(Player player)
    {
        if(!Simple_Factions.playerDatabase.hasFaction(player))
        {
            player.sendMessage(Component.text("You must be in a faction to perform this action!", NamedTextColor.RED));
            return;
        }

        int factionId = Simple_Factions.playerDatabase.getFactionId(player);
        Faction faction = Simple_Factions.factionDatabase.getFactionDataById(factionId);

        if (faction == null)
        {
            player.sendMessage(Component.text("Your faction data has not been found!", NamedTextColor.RED));
            return;
        }

        if(!faction.getOwner().equals(player.getUniqueId()))
        {
            player.sendMessage(Component.text("You must be the owner of the faction to perform this action!", NamedTextColor.RED));
            return;
        }

        ArrayList<String> members = faction.getMembers();
        for (String memberName : members) {
            UUID memberUUID = Simple_Factions.playerDatabase.getPlayerUUID(memberName);
            Player member = Bukkit.getPlayer(memberUUID);

            Simple_Factions.playerDatabase.updateFactionWithPlayerUUID(memberUUID, 0);

            if(member != null)
            {
                changePlayerDisplayName(member, Component.text(member.getName()));
                if(member.isOnline() && !faction.getOwner().equals(memberUUID))
                    member.sendMessage(Component.text("You have been kicked from your faction because the faction has been deleted", NamedTextColor.AQUA));
            }
        }
        Simple_Factions.factionDatabase.deleteFaction(factionId);

        player.sendMessage(Component.text("You have deleted your faction", NamedTextColor.AQUA));
    }

    public static void modifyName(Player player, String newFactionName)
    {
        if(!Simple_Factions.playerDatabase.hasFaction(player))
        {
            player.sendMessage(Component.text("You must be in a faction to perform this action!", NamedTextColor.RED));
            return;
        }

        int factionId = Simple_Factions.playerDatabase.getFactionId(player);
        Faction factionData = Simple_Factions.factionDatabase.getFactionDataById(factionId);
        if(!factionData.getOwner().equals(player.getUniqueId()))
        {
            player.sendMessage(Component.text("You must be the owner of the faction to perform this action!", NamedTextColor.RED));
            return;
        }

        newFactionName = newFactionName.toLowerCase();

        if(newFactionName.matches(".*[^a-zA-Z].*"))
        {
            player.sendMessage(Component.text("Special characters are not allowed in faction names!", NamedTextColor.RED));
            return;
        }

        if(Simple_Factions.plugin.getSettingsConfig().getBoolean("enable-bannedNames", true) && Simple_Factions.plugin.getBannedWordsConfig().getStringList("BannedNames").contains(newFactionName))
        {
            player.sendMessage(Component.text("This name has been banned and cannot be used!", NamedTextColor.RED));
            return;
        }

        if(Simple_Factions.factionDatabase.factionExistsByName(newFactionName)){
            player.sendMessage(Component.text("The name '" + newFactionName + "' is already in use by another faction!", NamedTextColor.RED));
            return;
        }

        Simple_Factions.factionDatabase.updateFactionName(factionId, newFactionName);
        player.sendMessage(Component.text("You have changed your faction name to " + newFactionName, NamedTextColor.AQUA));
        factionData = Simple_Factions.factionDatabase.getFactionDataById(factionId);

        for(int i = 0; i < factionData.getMembers().size(); i++)
        {
            Player member = player.getServer().getPlayer(factionData.getMembers().get(i));
            if(member != null)
            {
                changePlayerDisplayName(member, buildNameComponent(factionData, member.getName()));
            }
        }
    }

    public static void modifyColor(Player player, String newColor)
    {
        if (!Simple_Factions.playerDatabase.hasFaction(player))
        {
            player.sendMessage(Component.text("You must be in a faction to perform this action!", NamedTextColor.RED));
            return;
        }

        if(!Colors.colorNameExists(newColor)){
            player.sendMessage(Component.text(newColor + " is not a valid color!", NamedTextColor.RED));
            return;
        }

        int factionId = Simple_Factions.playerDatabase.getFactionId(player);
        Faction faction = Simple_Factions.factionDatabase.getFactionDataById(factionId);

        if(!faction.getOwner().equals(player.getUniqueId()))
        {
            player.sendMessage(Component.text("You must be the owner of the faction to perform this action!", NamedTextColor.RED));
            return;
        }

        Simple_Factions.factionDatabase.updateFactionColor(factionId, newColor);

        net.kyori.adventure.text.format.TextColor tc = LegacyComponentSerializer.legacySection()
                .deserialize(Colors.getChatColorWithColorName(newColor).toString()).color();
        if (tc == null) tc = NamedTextColor.WHITE;
        player.sendMessage(Component.text("You have changed your faction color to " + newColor, tc));

        faction = Simple_Factions.factionDatabase.getFactionDataById(factionId);

        for(int i = 0; i < faction.getMembers().size(); i++)
        {
            Player member = player.getServer().getPlayer(faction.getMembers().get(i));
            if(member != null)
            {
                changePlayerDisplayName(member, buildNameComponent(faction, member.getName()));
            }
        }
    }

    public static void modifyOwner(Player player, String newOwnerName)
    {
        if (!Simple_Factions.playerDatabase.hasFaction(player))
        {
            player.sendMessage(Component.text("You must be in a faction to perform this action!", NamedTextColor.RED));
            return;
        }

        int factionId = Simple_Factions.playerDatabase.getFactionId(player);
        Faction faction = Simple_Factions.factionDatabase.getFactionDataById(factionId);

        if(!faction.getOwner().equals(player.getUniqueId()))
        {
            player.sendMessage(Component.text("You must be the owner of the faction to perform this action!", NamedTextColor.RED));
            return;
        }
        if(!faction.getMembers().contains(newOwnerName)){
            player.sendMessage(Component.text("This player is not a part of your faction!", NamedTextColor.RED));
            return;
        }

        String newOwnerUUID = Simple_Factions.playerDatabase.getPlayerUUID(newOwnerName).toString();
        Simple_Factions.factionDatabase.updateFactionOwner(factionId, newOwnerUUID);
        player.sendMessage(Component.text("You have promoted " + newOwnerName + " to owner", NamedTextColor.AQUA));
    }

    private static Component buildNameComponent(Faction faction, String playerName)
    {
        net.kyori.adventure.text.format.TextColor color = LegacyComponentSerializer.legacySection()
                .deserialize(faction.getColor().toString()).color();
        if (color == null) color = NamedTextColor.WHITE;
        return Component.text("[" + faction.getName() + "] ", color)
                .append(Component.text(playerName, NamedTextColor.WHITE));
    }

    private static void changePlayerDisplayName(Player player, Component nameComponent)
    {
        player.displayName(nameComponent);
        player.playerListName(nameComponent);
        player.customName(nameComponent);
    }
}
