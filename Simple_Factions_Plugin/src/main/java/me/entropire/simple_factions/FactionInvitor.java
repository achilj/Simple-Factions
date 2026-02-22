package me.entropire.simple_factions;

import me.entropire.simple_factions.objects.Faction;
import me.entropire.simple_factions.objects.Invite;
import me.entropire.simple_factions.objects.Join;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FactionInvitor
{
    public static void invite(Player player, String invitedPlayerName)
    {
        if(!Simple_Factions.playerDatabase.hasFaction(player))
        {
            player.sendMessage(Component.text("You must be in a faction to perform this action!", NamedTextColor.RED));
            return;
        }

        int factionId = Simple_Factions.playerDatabase.getFactionId(player);
        Faction faction = Simple_Factions.factionDatabase.getFactionDataById(factionId);

        if(!faction.getOwner().equals(player.getUniqueId()))
        {
            player.sendMessage(Component.text("Only the owner can invite players to the faction.", NamedTextColor.RED));
            return;
        }

        Player invitedPlayer = Bukkit.getServer().getPlayer(invitedPlayerName);
        if (invitedPlayer == null || !invitedPlayer.isOnline())
        {
            player.sendMessage(Component.text(invitedPlayer == null
                    ? invitedPlayerName + " is not a player."
                    : invitedPlayerName + " is not online.", NamedTextColor.RED));
            return;
        }

        if (Simple_Factions.playerDatabase.hasFaction(invitedPlayer))
        {
            player.sendMessage(Component.text(invitedPlayerName + " is already in a faction.", NamedTextColor.RED));
            return;
        }

        if(Simple_Factions.invites.containsKey(invitedPlayer.getUniqueId()))
        {
            player.sendMessage(Component.text(invitedPlayerName + " is already invited to a faction.", NamedTextColor.RED));
            return;
        }

        Invite invite = new Invite(invitedPlayer.getUniqueId(), factionId, System.currentTimeMillis() + 30000);
        Simple_Factions.invites.put(invitedPlayer.getUniqueId(), invite);

        invitedPlayer.sendMessage(Component.text("You have been invited to the faction " + faction.getName(), NamedTextColor.YELLOW));
        invitedPlayer.sendMessage(Component.text("To accept type: /faction accept", NamedTextColor.YELLOW));
        invitedPlayer.sendMessage(Component.text("To decline type: /faction decline", NamedTextColor.YELLOW));

        player.sendMessage(Component.text("Invited " + invitedPlayerName + " to your faction.", NamedTextColor.YELLOW));
    }

    public static void join(Player player, String factionName)
    {
        if(Simple_Factions.playerDatabase.hasFaction(player))
        {
            player.sendMessage(Component.text("You are already in a faction.", NamedTextColor.RED));
            return;
        }
        if(!Simple_Factions.factionDatabase.factionExistsByName(factionName))
        {
            player.sendMessage(Component.text("Faction " + factionName + " does not exist.", NamedTextColor.RED));
            return;
        }

        Faction faction = Simple_Factions.factionDatabase.getFactionDataByName(factionName);
        Player receiver = Bukkit.getPlayer(faction.getOwner());
        if (receiver == null)
        {
            player.sendMessage(Component.text("Something went wrong while making a join request.", NamedTextColor.RED));
            return;
        }
        Join join = new Join(faction.getOwner(), player.getUniqueId(), faction.getId(), System.currentTimeMillis() + 30000);
        Simple_Factions.joins.put(faction.getOwner(), join);

        player.sendMessage(Component.text("You have sent a join request to " + faction.getName(), NamedTextColor.YELLOW));

        receiver.sendMessage(Component.text(player.getName() + " wants to join your faction.", NamedTextColor.YELLOW));
        receiver.sendMessage(Component.text("To accept type: /faction accept", NamedTextColor.YELLOW));
        receiver.sendMessage(Component.text("To decline type: /faction decline", NamedTextColor.YELLOW));
    }

    public static void accept(Player player)
    {
        if (Simple_Factions.invites.containsKey(player.getUniqueId()))
        {
            Invite invite = Simple_Factions.invites.get(player.getUniqueId());
            Faction faction = Simple_Factions.factionDatabase.getFactionDataById(invite.factionId());

            Simple_Factions.factionDatabase.updateFactionMembers(invite.factionId(), player.getName(), true);
            Simple_Factions.playerDatabase.updateFactionWithPlayerName(player.getName(), invite.factionId());

            Player sender = Bukkit.getPlayer(faction.getOwner());
            if(sender != null)
            {
                sender.sendMessage(Component.text(player.getName() + " is now part of your faction.", NamedTextColor.YELLOW));
            }

            changePlayerDisplayName(player, buildNameComponent(faction, player.getName()));
            player.sendMessage(Component.text("You have joined the faction " + faction.getName(), NamedTextColor.YELLOW));

            Simple_Factions.invites.remove(player.getUniqueId());
            return;
        }

        if(Simple_Factions.joins.containsKey(player.getUniqueId()))
        {
            Join join = Simple_Factions.joins.get(player.getUniqueId());
            String senderName = Simple_Factions.playerDatabase.getPlayerName(join.sender().toString());
            Faction faction = Simple_Factions.factionDatabase.getFactionDataById(join.factionId());

            Simple_Factions.factionDatabase.updateFactionMembers(join.factionId(), senderName, true);
            Simple_Factions.playerDatabase.updateFactionWithPlayerUUID(join.sender(), join.factionId());

            Player sender = Bukkit.getPlayer(join.sender());
            if(sender != null)
            {
                changePlayerDisplayName(sender, buildNameComponent(faction, sender.getName()));
                sender.sendMessage(Component.text("You have joined the faction " + faction.getName(), NamedTextColor.YELLOW));
            }
            player.sendMessage(Component.text(senderName + " is now part of your faction.", NamedTextColor.YELLOW));

            Simple_Factions.joins.remove(player.getUniqueId());
            return;
        }

        player.sendMessage(Component.text("You don't have any pending invites or requests.", NamedTextColor.RED));
    }

    public static void decline(Player player)
    {
        if (Simple_Factions.invites.containsKey(player.getUniqueId()))
        {
            Invite invite = Simple_Factions.invites.get(player.getUniqueId());
            Faction faction = Simple_Factions.factionDatabase.getFactionDataById(invite.factionId());

            player.sendMessage(Component.text("You have declined the faction invitation from " + faction.getName(), NamedTextColor.YELLOW));

            Simple_Factions.invites.remove(player.getUniqueId());
            return;
        }

        if(Simple_Factions.joins.containsKey(player.getUniqueId()))
        {
            Join join = Simple_Factions.joins.get(player.getUniqueId());
            String senderName = Simple_Factions.playerDatabase.getPlayerName(join.sender().toString());
            player.sendMessage(Component.text("You have declined the join request from " + senderName, NamedTextColor.RED));

            Player sender = Bukkit.getPlayer(join.sender());
            if(sender != null)
            {
                sender.sendMessage(Component.text(player.getName() + " declined your join request.", NamedTextColor.YELLOW));
            }

            Simple_Factions.joins.remove(player.getUniqueId());
            return;
        }

        player.sendMessage(Component.text("You don't have any pending invites or requests.", NamedTextColor.RED));
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
