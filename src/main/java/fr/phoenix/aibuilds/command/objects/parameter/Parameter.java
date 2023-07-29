package fr.phoenix.aibuilds.command.objects.parameter;

import fr.phoenix.aibuilds.command.objects.CommandTreeExplorer;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.function.BiConsumer;

public class Parameter {
    private final String key;
    private final BiConsumer<CommandTreeExplorer, List<String>> autoComplete;

    public static final Parameter PLAYER = new Parameter("<player>",
            (explorer, list) -> Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName())));
    public static final Parameter PLAYER_OPTIONAL = new Parameter("(player)",
            (explorer, list) -> Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName())));
    public static final Parameter IMAGE = new Parameter("<image>",
            (explorer, list) -> {
            });
    public static final Parameter PROMPT = new Parameter("<prompt>",
            (explorer, list) -> {
            });

    public static final Parameter QUALITY = new Parameter("<quality>",
            (explorer, list) -> {
                list.add("LOW");
                list.add("MID");
                list.add("HIGH");
            });
    public static final Parameter AMOUNT = new Parameter("<amount>", (explorer, list) -> {
        for (int j = 0; j <= 10; ++j) {
            list.add("" + j);
        }

    });

    public Parameter(String key, BiConsumer<CommandTreeExplorer, List<String>> autoComplete) {
        this.key = key;
        this.autoComplete = autoComplete;
    }

    public String getKey() {
        return key;
    }

    public void autoComplete(CommandTreeExplorer explorer, List<String> list) {
        autoComplete.accept(explorer, list);
    }
}
