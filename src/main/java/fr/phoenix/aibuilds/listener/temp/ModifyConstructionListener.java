package fr.phoenix.aibuilds.listener.temp;

import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.communication.ConstructionHandler;
import fr.phoenix.aibuilds.placeholders.Placeholders;
import fr.phoenix.aibuilds.utils.ActionBarRunnable;
import fr.phoenix.aibuilds.utils.message.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.util.Vector;

public class ModifyConstructionListener extends TemporaryListener {
    private final ActionBarRunnable actionBarRunnable;
    private final ConstructionHandler constructionHandler;

    private final Placeholders holders = new Placeholders();

    public ModifyConstructionListener(ConstructionHandler constructionHandler) {
        super(PlayerItemHeldEvent.getHandlerList(), PlayerInteractEvent.getHandlerList());
        this.constructionHandler = constructionHandler;
        holders.register("current-batch", constructionHandler.getCurrentBatchIndex() + 1);
        holders.register("max-batch", constructionHandler.getNumberBatches());
        actionBarRunnable = new ActionBarRunnable(constructionHandler.getPlayer(),
                formatString(Message.CONSTRUCTION_MODIFICATION.format().getAsString()),holders);
        actionBarRunnable.runTaskTimer(AIBuilds.plugin, 0L, 10L);
    }

    public String formatString(String string) {
        String str = "slot";
        String[] split = string.split(str);
        String result = "";
        for (int i = 0; i < split.length - 1; i++) {
            result += split[i];
            result = result + (i + 1 + (constructionHandler.getPlayer().getInventory().getHeldItemSlot() <= i ? 1 : 0));
        }
        result += split[split.length - 1];
        return result;
    }

    @EventHandler
    public void onChangeBatch(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(constructionHandler.getPlayer()))
            return;
        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            constructionHandler.nextBatch();
            holders.register("current-batch", constructionHandler.getCurrentBatchIndex() + 1);
        }
    }

    @EventHandler
    public void onModify(PlayerItemHeldEvent event) {
        if (!event.getPlayer().equals(constructionHandler.getPlayer()))
            return;

        final Player player = event.getPlayer();


        /*
         * When the event is cancelled, another playerItemHeldEvent is
         * called and previous and next slots are equal. the event must not
         * listen to that non-player called event.
         */
        if (event.getPreviousSlot() == event.getNewSlot()) return;

        event.setCancelled(true);

        int slot = event.getNewSlot() + (event.getNewSlot() >= player.getInventory().getHeldItemSlot() ? -1 : 0);

        /*
         * The event is called again soon after the first since when
         * cancelling the first one, the player held item slot must go back
         * to the previous one.
         */
        Vector direction = player.getEyeLocation().getDirection();
        switch (slot) {
            case 0:
                constructionHandler.pushFrom(direction);
                break;
            case 1:
                constructionHandler.pullFrom(direction);
                break;

            case 2:
                constructionHandler.rotateClockWiseAround(direction);
                break;

            case 3:
                constructionHandler.rotateAntiClockWiseAround(direction);
                break;

            case 4:
                constructionHandler.upSize();
                break;

            case 5:
                constructionHandler.downSize();
                break;

            case 6:
                constructionHandler.acceptConstruction();
                break;

            case 7:
                constructionHandler.removeConstruction();
                break;

        }
    }

    @Override
    public void whenClosed() {
        actionBarRunnable.cancel();
    }
}
