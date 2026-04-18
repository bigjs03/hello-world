package com.bygj.immersed;

import com.bygj.immersed.bridge.BridgeTrainer;
import com.bygj.immersed.config.ImmersedConfig;
import net.fabricmc.api.ClientModInitializer;

public class ImmersedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ImmersedConfig.load();
        if (ImmersedConfig.get().autoSyncOnStart) {
            ImmersedConfig.syncFromCloud();
        }
        BridgeTrainer.init();
    }
}
