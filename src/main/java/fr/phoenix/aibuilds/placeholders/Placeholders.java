package fr.phoenix.aibuilds.placeholders;

import fr.phoenix.aibuilds.AIBuilds;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Placeholders {
    private final Map<String, String> placeholders = new HashMap<>();

    public void register(String path, Object obj) {
        placeholders.put(path, obj.toString());
    }

    public String apply(Player player, String str) {

        // Remove conditions first
        str = removeCondition(str);

        /*
         * For MMOCore not to loop on unparsable placeholders, it keeps
         * track of the "last placeholder" parsed. The 'explored' string
         * has NO parsed placeholder.
         */
        String explored = str;

        // Internal placeholders
        while (explored.contains("{") && explored.substring(explored.indexOf("{")).contains("}")) {
            final int begin = explored.indexOf("{"), end = explored.indexOf("}");
            final String holder = explored.substring(begin + 1, end);
            @Nullable String found = placeholders.get(holder);

            /*
             * Do NOT replace the placeholder unless a corresponding value has
             * been found. This simple workaround fixes an issue with PAPI
             * math expansions which interferes with MMOCore placeholders since
             * it uses {....} as well.
             */
            if (found != null)
                str = str.replace("{" + holder + "}", found);

            // Increase counter
            explored = explored.substring(end + 1);
        }

        // External placeholders
        return AIBuilds.plugin.placeholderParser.parse(player, str);
    }

    private String removeCondition(String str) {
        return str.startsWith("{") && str.contains("}") ? str.substring(str.indexOf("}") + 1) : str;
    }
}
