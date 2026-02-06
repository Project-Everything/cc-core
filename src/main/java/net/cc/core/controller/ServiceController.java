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
import net.cc.core.api.model.CoreGroup;
import net.cc.core.api.model.CoreServer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

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
        this.luckPerms = LuckPermsProvider.get();
    }

    // Gets a player's group from LuckPerms
    public CoreGroup getGroup(final UUID uuid) {
        final User user = this.luckPerms.getUserManager().loadUser(uuid).join();

        if (user != null) {
            final String groupName = user.getPrimaryGroup();
            try {
                return CoreGroup.valueOf(groupName.toUpperCase());
            } catch (final IllegalArgumentException e) {
                this.plugin.getComponentLogger().error("Unable to load group '{}' for player '{}'", groupName, user.getUsername());
                return CoreGroup.DEFAULT;
            }
        }
        return CoreGroup.DEFAULT;
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

    // Checks if a player has a Town
    public boolean hasTown(final UUID uuid) {
        final Resident resident = this.getResident(uuid);

        if (resident != null) {
            final Town town = resident.getTownOrNull();
            return town != null;
        }
        return false;
    }

    // Checks if a player has a Nation
    public boolean hasNation(final UUID uuid) {
        final Resident resident = this.getResident(uuid);

        if (resident != null) {
            final Nation nation = resident.getNationOrNull();
            return nation != null;
        }
        return false;
    }

    // Checks if a player has a title
    public boolean hasTitle(final UUID uuid) {
        final Resident resident = this.getResident(uuid);

        if (resident != null) {
            return !(resident.getTitle().isEmpty()) && !(resident.getTitle().isBlank());
        }
        return false;
    }

    // Gets the Town name for a user ID
    public String getTownName(final UUID uuid) {
        if (this.plugin.getCoreServer() == CoreServer.EARTH) {
            final Resident resident = this.getResident(uuid);

            if (resident != null) {
                final Town town = resident.getTownOrNull();
                if (town != null) {
                    return town.getName();
                }
            }
        }
        return "";
    }

    // Gets the Nation name for a user ID
    public String getNationName(final UUID uuid) {
        if (this.plugin.getCoreServer() == CoreServer.EARTH) {
            final Resident resident = this.getResident(uuid);

            if (resident != null) {
                final Nation nation = resident.getNationOrNull();
                if (nation != null) {
                    return nation.getName();
                }
            }
        }
        return "";
    }

    // Gets a Resident's title from the Towny API
    public String getTownyTitle(final UUID uuid) {
        if (this.plugin.getCoreServer() == CoreServer.EARTH) {
            final Resident resident = this.getResident(uuid);

            if (resident.hasTitle()) {
                return this.getResident(uuid).getTitle();
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
