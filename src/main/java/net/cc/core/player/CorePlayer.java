package net.cc.core.player;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CorePlayer {

    private final UUID mojangId;
    private String username;
    private String displayName;
    private String nickname;
    private boolean vanished;
    private String friends;

    public CorePlayer(
            final Player player
    ) {
        mojangId = player.getUniqueId();
        username = player.getName();
        displayName = "<gray>" + player.getName() + "</gray>";
        nickname = "";
        vanished = false;
        friends = "";
    }

    public CorePlayer(
            final UUID mojangId,
            final String username,
            final String displayName,
            final String nickname,
            final boolean vanished,
            final String friends
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

    public String getFriends() {
        return friends;
    }

    public void setFriends(final String friends) {
        this.friends = friends;
    }

    public void addFriend(final UUID mojangId) {
        this.friends = StringUtils.appendIfMissing(this.friends, "," + mojangId.toString());
    }

    public void removeFriend(final UUID mojangId) {
        this.friends = StringUtils.remove(this.friends, "," + mojangId.toString());
    }

    public boolean isFriend(final UUID mojangId) {
        List<String> friends = Arrays.stream(this.friends.split(",")).toList();
        return friends.contains(mojangId.toString());
    }
}
