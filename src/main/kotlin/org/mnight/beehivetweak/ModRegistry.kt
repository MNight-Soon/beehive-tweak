package org.mnight.beehivetweak

import com.mojang.serialization.Codec
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Supplier

object ModRegistry {
    val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, BeehiveTweak.MOD_ID)

    val FAILED_ATTEMPTS: Supplier<AttachmentType<Int>> = ATTACHMENT_TYPES.register("failed_attempts") { ->
        AttachmentType.builder { -> 0 }
            .serialize(Codec.INT)
            .copyOnDeath()
            .build()
    }
}