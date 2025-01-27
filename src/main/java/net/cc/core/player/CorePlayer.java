package net.cc.core.player;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CorePlayer {

    private final UUID mojangId;
    private String username;
    private String displayName;
    private String nickname;
    private boolean vanished;
    private List<UUID> friends;

    public CorePlayer(
            final Player player
    ) {
        mojangId = player.getUniqueId();
        username = player.getName();
        displayName = "<gray>" + player.getName() + "</gray>";
        nickname = "";
        vanished = false;
        friends = new ArrayList<>();
    }

    public CorePlayer(
            final UUID mojangId,
            final String username,
            final String displayName,
            final String nickname,
            final boolean vanished,
            final List<UUID> friends
    ) {
        this.mojangId = mojangId;
        this.username = username;
        this.displayName = displayName;
        this.nickname = nickname;
        this.vanished = vanished;
        this.friends = friends;
    }

    public UUID getMojangId() {
        return mojangId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }

    public boolean isVanished() {
        return vanished;
    }

    public void setVanished(final boolean vanished) {
        this.vanished = vanished;
    }

    public List<UUID> getFriends() {
        return friends;
    }

    public void setFriends(final List<UUID> friends) {
        this.friends = friends;
    }
}
