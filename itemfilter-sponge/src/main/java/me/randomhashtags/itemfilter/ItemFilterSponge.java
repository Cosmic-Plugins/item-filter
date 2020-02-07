package me.randomhashtags.itemfilter;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;

@Plugin(
        id = "itemfilter",
        name = "ItemFilter",
        description = "Allows players to limit what items they wantt and don't want to pick up off the ground.",
        authors = {
                "RandomHashTags"
        }
)
public class ItemFilterSponge {

    @Inject
    private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
    }
}
