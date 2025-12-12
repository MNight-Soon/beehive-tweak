package org.mnight.beehivetweak

import com.mnightsoon.beehivetweak.ModConfig
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.fml.config.ModConfig as NeoModConfig

@Mod(BeehiveTweak.MOD_ID)
class BeehiveTweak (modEventBus: IEventBus, modContainer: ModContainer){
    companion object{
        const val MOD_ID = "beehivetweak"
    }

    init {
        modContainer.registerConfig(NeoModConfig.Type.COMMON, ModConfig.SPEC)

        ModRegistry.ATTACHMENT_TYPES.register(modEventBus)

        NeoForge.EVENT_BUS.register(BeeLuckHandler)

        NeoForge.EVENT_BUS.register(ModCommands)
    }
}