package fr.phoenix.aibuilds.listener.temp;

import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.communication.ConstructionHandler;
import fr.phoenix.aibuilds.utils.ActionBarRunnable;
import fr.phoenix.aibuilds.utils.message.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.util.Vector;

public class ModifyConstructionListener extends TemporaryListener {
    private final ActionBarRunnable actionBarRunnable;
    private final ConstructionHandler constructionHandler;

    private final boolean includeDensity;


    public ModifyConstructionListener(ConstructionHandler constructionHandler, boolean includeDensity) {
        super(PlayerItemHeldEvent.getHandlerList());
        this.constructionHandler = constructionHandler;
        Message actionBarMessage= includeDensity ? Message.CONSTRUCTION_MODIFICATION_WITH_DENSITY : Message.CONSTRUCTION_MODIFICATION;
        actionBarRunnable = new ActionBarRunnable(constructionHandler.getPlayer(), formatString(actionBarMessage.format().getAsString()));
        actionBarRunnable.runTaskTimer(AIBuilds.plugin, 0L, 10L);
        this.includeDensity = includeDensity;
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
        //The effects are reversed when sneaking.
        direction = player.isSneaking() ? direction.multiply(-1) : direction;

        switch (slot) {
            case 0:
                constructionHandler.pushFrom(direction);
                break;
            case 1:
                constructionHandler.rotateClockWiseAround(direction);
                break;
            case 2:
                if (player.isSneaking())
                    constructionHandler.downSize();
                else
                    constructionHandler.upSize();
                break;
            //Increasing the density corresponds to decreasing the threshold.
            case 3:
                if (player.isSneaking())
                    constructionHandler.increaseThreshold();
                else
                    constructionHandler.decreaseThreshold();
                break;

            case 8:
                constructionHandler.acceptConstruction();
                break;

            case 9:
                constructionHandler.removeConstruction();
                break;

        }
    }

    @Override
    public void whenClosed() {
        actionBarRunnable.cancel();
    }
}
