package com.dungprovaidai.cauca.client;

import com.dungprovaidai.cauca.CaucaFishing;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Physical-client entry point kept separate so common code never loads client-only classes on a server. */
@Mod(value = CaucaFishing.MOD_ID, dist = Dist.CLIENT)
public final class CaucaFishingClient {
    public CaucaFishingClient(IEventBus modEventBus) {
        ClientSetup.init(modEventBus);
    }
}
