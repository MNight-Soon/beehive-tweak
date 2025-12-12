package org.mnight.beehivetweak

import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.NeoForge

class BeehiveTweak (modEventBus: IEventBus){
    companion object{
        const val MOD_ID = "beehive_tweak"
    }

    init {
        ModRegistry.ATTACHMENT_TYPES.register(modEventBus)
        NeoForge.EVENT_BUS.register(BeeLuckHandler)
    }
}