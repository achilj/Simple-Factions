package me.entropire.simple_factions.Gui;

import me.entropire.simple_factions.FactionEditor;
import me.entropire.simple_factions.objects.Colors;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ChangeFactionColorGui extends BaseGui
{
    @Override
    public void open(Player player)
    {
        Gui gui = new Gui("Change faction color" , GuiSize.Small);

        int i = 0;
        List<Integer> slots = Arrays.asList(1,2,3,4,5,6,7,10,11,12,13,14,15,16,21,23);
        for (String colorName : Colors.getColorNames())
        {
            gui.addButton(slots.get(i), Colors.getChatColorWithColorName(colorName).toString() + colorName, Colors.getMaterialWithColorName(colorName), "", (btn, event) -> {
                // Strip color codes from the display name to get the plain color name
                String rawName = btn.getItemMeta().displayName() != null
                        ? PlainTextComponentSerializer.plainText().serialize(btn.getItemMeta().displayName())
                        : "";
                FactionEditor.modifyColor((Player) event.getView().getPlayer(), rawName);
                player.closeInventory();
            });
            i++;
        }

        player.openInventory(gui.create());
    }
}
