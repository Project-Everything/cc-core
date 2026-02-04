package net.cc.core.model.config;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for Meow settings.
 *
 * @since 1.0.0
 */
@Getter
@ConfigSerializable
public final class MeowConfig {

    private final long cooldown;
    private final int defaultReward;
    private final int specialReward;
    private final double specialChance;
    private final List<String> blacklist;

    // Constructor
    public MeowConfig() {
        this.cooldown = 86400;
        this.defaultReward = 5;
        this.specialReward = 50;
        this.specialChance = 0.001;
        this.blacklist = new ArrayList<>();
    }
}
