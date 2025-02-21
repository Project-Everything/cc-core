package net.cc.core.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CorePlayer {

    private final UUID mojangId;
    private String username;
    private String displayName;
    private String nickname;
    private boolean vanished;
    private List<String> friends;

    // New CorePlayer
    public CorePlayer(
            Player player
    ) {
        mojangId = player.getUniqueId();
        username = player.getName();
        displayName = "<gray>" + player.getName() + "</gray>";
        nickname = "";
        vanished = false;
        friends = new ArrayList<>();
    }

    // Existing CorePlayer
    public CorePlayer(
            UUID mojangId,
            String username,
            String displayName,
            String nickname,
            boolean vanished,
            List<String> friends
    ) {
        this.mojangId = mojangId;
        this.username = username;
        this.displayName = displayName;
        this.nickname = nickname;
        this.vanished = vanished;
        this.friends = friends;
    }

    public void addFriend(final UUID mojangId) {
        this.friends.add(mojangId.toString());
    }

    public void removeFriend(final UUID mojangId) {
        this.friends.remove(mojangId.toString());
    }

    public boolean isFriend(final UUID mojangId) {
        return this.friends.contains(mojangId.toString());
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof CorePlayer corePlayer)) return false;
        return mojangId.equals(corePlayer.mojangId);
    }
}
