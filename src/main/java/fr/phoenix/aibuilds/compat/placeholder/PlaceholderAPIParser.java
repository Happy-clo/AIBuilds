package fr.phoenix.aibuilds.compat.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import static fr.phoenix.aibuilds.compat.placeholder.DefaultPlaceholderParser.decode;


public class PlaceholderAPIParser implements PlaceholderParser {

    @Override
    public String parse(Player player, String input) {

        //Parse Unicode Characters
        input=decode(input);
        return PlaceholderAPI.setPlaceholders(player, input);
    }
}
