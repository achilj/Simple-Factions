package me.entropire.simple_factions.commands;

import me.entropire.simple_factions.Simple_Factions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ChatCommands implements CommandExecutor, TabCompleter
{
    ArrayList<String> chats = new ArrayList<>(Arrays.asList("public", "faction"));

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if (!(sender instanceof Player player))
        {
            sender.sendMessage(Component.text("Only players can perform this command!", NamedTextColor.RED));
            return false;
        }

        if(!Simple_Factions.playerDatabase.hasFaction(player))
        {
            player.sendMessage(Component.text("You must be in a faction to perform this action!", NamedTextColor.RED));
            return false;
        }

        if(command.getName().equalsIgnoreCase("cf"))
        {
            Simple_Factions.playerDatabase.setChat(player.getUniqueId(), "faction");
            player.sendMessage(Component.text("Changed chat to faction", NamedTextColor.AQUA));
            return true;
        }

        if(command.getName().equalsIgnoreCase("cp"))
        {
            Simple_Factions.playerDatabase.setChat(player.getUniqueId(), "public");
            player.sendMessage(Component.text("Changed chat to public", NamedTextColor.AQUA));
            return true;
        }

        if(args.length < 1)
        {
            player.sendMessage(Component.text("Command usage: /chat [public or faction]"));
            return false;
        }

        if(!chats.contains(args[0]))
        {
            player.sendMessage(Component.text(args[0] + " is not a valid chat type!", NamedTextColor.RED));
        }

        Simple_Factions.playerDatabase.setChat(player.getUniqueId(), args[0]);
        player.sendMessage(Component.text("Changed chat to " + args[0], NamedTextColor.AQUA));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args)
    {
        List<String> suggestions = new ArrayList<>();

        if(sender instanceof Player && !command.getName().equalsIgnoreCase("cp") && !command.getName().equalsIgnoreCase("cf"))
        {
            if(args.length == 1)
            {
                suggestions.add("public");
                suggestions.add("faction");
            }

            return  suggestions;
        }

        return List.of();
    }
}

