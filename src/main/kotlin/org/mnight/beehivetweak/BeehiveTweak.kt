package org.mnight.beehivetweak

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge

@Mod(BeehiveTweak.MOD_ID)
class BeehiveTweak (modEventBus: IEventBus){
    companion object{
        const val MOD_ID = "beehivetweak"
    }

    init {
        ModRegistry.ATTACHMENT_TYPES.register(modEventBus)

        NeoForge.EVENT_BUS.register(BeeLuckHandler)

        NeoForge.EVENT_BUS.register(ModCommands)
    }
}