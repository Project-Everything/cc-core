package net.cc.core.controller;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CoreServer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Controller class for external services.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class ServiceController {

    private final CorePlugin plugin;
    private LuckPerms luckPerms;

    // Initializes the controller
    public void initialize() {
        this.luckPerms = this.plugin.getServer().getServicesManager().load(LuckPerms.class);
    }

    // Gets a player's prefix from LuckPerms as a String
    public @NotNull String getPrefix(final UUID uuid) {
        final User user = this.luckPerms.getUserManager().loadUser(uuid).join();
        final String def = PlainTextComponentSerializer.plainText().serialize(
                this.plugin.getConfigController().getMessage("default-prefix")
        );

        if (user != null) {
            final String prefix = user.getCachedData().getMetaData().getPrefix();
            return prefix != null ? prefix : def;
        }
        return def;
    }

    // Gets a player's primary group from Luckperms as a String
    public @NotNull String getGroup(final UUID id) {
        final User user = this.luckPerms.getUserManager().loadUser(id).join();
        final String def = "default";

        if (user != null) {
            return user.getPrimaryGroup();
        }
        return def;
    }

    // Checks if a User has a permission in their cached data
    public boolean hasPermission(final UUID uuid, final String permission) {
        final User user = this.luckPerms.getUserManager().loadUser(uuid).join();
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    // Gets a Resident from the Towny API
    public Resident getResident(final UUID uuid) {
        final TownyAPI townyAPI = TownyAPI.getInstance();
        return townyAPI.getResident(uuid);
    }

    // Gets a Town from the Towny API
    public Town getTown(final Resident resident) {
        return resident.getTownOrNull();
    }

    // Gets a Nation from the Towny API
    public Nation getNation(final Resident resident) {
        return resident.getNationOrNull();
    }

    // Gets a Resident's title from the Towny API
    public String getTownyTitle(final UUID uuid) {
        if (this.plugin.getCoreServer() == CoreServer.EARTH) {
            final Resident resident = this.getResident(uuid);

            if (resident.hasTitle()) {
                return this.getResident(uuid).getTitle() + " ";
            }
        }
        return "";
    }

    // Gets a player's current plot from the Plot API
    public Plot getPlot(final UUID uuid) {
        final PlotAPI plotAPI = new PlotAPI();
        final PlotPlayer<?> plotPlayer = plotAPI.wrapPlayer(uuid);

        if (plotPlayer != null) {
            return plotPlayer.getCurrentPlot();
        }
        return null;
    }

}
