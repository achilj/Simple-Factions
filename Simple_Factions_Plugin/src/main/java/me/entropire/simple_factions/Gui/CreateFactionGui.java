package me.entropire.simple_factions.Gui;

import me.entropire.simple_factions.FactionEditor;
import org.bukkit.entity.Player;

public class CreateFactionGui extends BaseGui
{
    @Override
    public void open(Player player)
    {
        new AnvilInputGui(player, "Set faction name", null,
                text -> FactionEditor.create(player, text)).open();
    }
}
