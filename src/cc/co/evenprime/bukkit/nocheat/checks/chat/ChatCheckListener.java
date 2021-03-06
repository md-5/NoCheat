package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import cc.co.evenprime.bukkit.nocheat.EventManager;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;

/**
 * Central location to listen to events that are 
 * relevant for the chat checks
 * 
 */
public class ChatCheckListener implements Listener, EventManager {

    private final SpamCheck  spamCheck;
    private final ColorCheck colorCheck;

    private final NoCheat    plugin;

    public ChatCheckListener(NoCheat plugin) {

        this.plugin = plugin;

        spamCheck = new SpamCheck(plugin);
        colorCheck = new ColorCheck(plugin);
    }

    /**
     * We listen to PlayerCommandPreprocess events because commands can be
     * used for spamming too.
     * 
     * @param event The PlayerCommandPreprocess Event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void commandPreprocess(final PlayerCommandPreprocessEvent event) {
        String message = handle(event.getPlayer(), event.getMessage());
        if (message == null) {
            event.setCancelled(true);
        } else {
            event.setMessage(message);
        }
    }

    /**
     * We listen to PlayerChat events for obvious reasons
     * @param event The PlayerChat event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
    public void chat(final AsyncPlayerChatEvent event) {
        String message = handle(event.getPlayer(), event.getMessage());
        if (message == null) {
            event.setCancelled(true);
        } else {
            event.setMessage(message);
        }
    }

	private String handle(Player eplayer, String message) {
        boolean cancelled = false;

        final NoCheatPlayer player = plugin.getPlayer(eplayer);
        final ChatConfig cc = ChatCheck.getConfig(player);
        final ChatData data = ChatCheck.getData(player);

        // Remember the original message
        data.message = message;

        // Now do the actual checks

        // First the spam check
        if(cc.spamCheck && !player.hasPermission(Permissions.CHAT_SPAM)) {
            cancelled = spamCheck.check(player, data, cc);
        }

        // Second the color check
        if(!cancelled && cc.colorCheck && !player.hasPermission(Permissions.CHAT_COLOR)) {
            cancelled = colorCheck.check(player, data, cc);
        }

        // If one of the checks requested the event to be cancelled, do it
        if(cancelled) {
            return null;
        } else {
            // In case one of the events modified the message, make sure that
            // the new message gets used
            return data.message;
        }
    }

    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        LinkedList<String> s = new LinkedList<String>();

        ChatConfig c = ChatCheck.getConfig(cc);
        if(c.spamCheck)
            s.add("chat.spam");
        if(c.colorCheck)
            s.add("chat.color");
        return s;
    }
}
