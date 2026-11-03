package com.possessedgrin;

import com.possessedgrin.power.PossessedPowers;
import net.fabricmc.api.ModInitializer;

/** Common (client + server) start-up: sets up the possession powers. */
public final class PossessedGrinMod implements ModInitializer {
    @Override
    public void onInitialize() {
        PossessedPowers.init();
    }
}
