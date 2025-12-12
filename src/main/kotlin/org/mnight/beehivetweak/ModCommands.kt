package org.mnight.beehivetweak

import com.mojang.brigadier.arguments.IntegerArgumentType
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent

object ModCommands {
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent){
        event.dispatcher.register(
            Commands.literal("beectrl")
                .requires { it.hasPermission(2) }

                .then(Commands.literal("check")
                    .executes { ctx ->
                        val player = ctx.source.playerOrException
                        val failures = player.getData(ModRegistry.FAILED_ATTEMPTS)
                        ctx.source.sendSuccess({ Component.literal("Your current failures: $failures")}, false)
                        1
                    }
                )
                .then(Commands.literal("set")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes { ctx ->
                            val player = ctx.source.playerOrException
                            val amount = IntegerArgumentType.getInteger(ctx, "amount")

                            player.setData(ModRegistry.FAILED_ATTEMPTS, amount)
                            ctx.source.sendSuccess({ Component.literal("Set failures to: $amount")}, false)
                            1
                        }
                    )
                )
        )
    }
}