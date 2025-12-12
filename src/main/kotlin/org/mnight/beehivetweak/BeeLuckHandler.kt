package org.mnight.beehivetweak

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.TickTask
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BeehiveBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SaplingBlock
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.level.block.CropGrowEvent
import kotlin.random.Random


object BeeLuckHandler {
    private const val BASE_CHANCE = 0.05
    private const val DEBUG_MODE = true

    @SubscribeEvent
    fun onCropGrow(event: CropGrowEvent.Pre){
        val level = event.level as? ServerLevel ?: return
        val pos = event.pos
        val state = event.state

        if (state.block !is SaplingBlock) return
        if (!hasFlowerNearby(level, pos)) return

        val player = level.getNearestPlayer(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 10.0, false) ?: return

        level.server.tell(TickTask(level.server.tickCount + 1) { ->
            checkAndApplyPity(level, pos, player)
        })
    }

    private fun checkAndApplyPity(level: ServerLevel, saplingPos: BlockPos, player: Player){
        val baseLogPos = saplingPos

        if (!level.getBlockState(baseLogPos).`is`(BlockTags.LOGS)){
            return
        }

        val vanillaNestPos = findNearbyBeeNest(level, baseLogPos)

        if (vanillaNestPos != null) {
            level.setBlock(vanillaNestPos, Blocks.AIR.defaultBlockState(), 3)
        }

        val failures = player.getData(ModRegistry.FAILED_ATTEMPTS)
        val currentChange = BASE_CHANCE * (1 shl failures)
        if (DEBUG_MODE){
            val msg = "BeeDebug: Failures=$failures, Chance=${currentChange * 100}%, VanillaRemoved=${vanillaNestPos != null}"
            player.sendSystemMessage(Component.literal(msg))
        }

        if (Random.nextFloat() < currentChange) {
            if (forcePlaceBeeNest(level, baseLogPos)) {
                player.setData(ModRegistry.FAILED_ATTEMPTS, 0)
                if (DEBUG_MODE) player.sendSystemMessage(Component.literal("BeeDebug: SUCCESS! Nest Placed."))
            } else {
                player.setData(ModRegistry.FAILED_ATTEMPTS, failures + 1)
            }
        } else {
            if (DEBUG_MODE) player.sendSystemMessage(Component.literal("BeeDebug: FAILED. Pity Increased."))
            player.setData(ModRegistry.FAILED_ATTEMPTS, failures + 1)
        }
    }

    private fun hasFlowerNearby(level: Level, pos: BlockPos): Boolean {
        for (x in -2..2){
            for (z in -2..2){
                if (level.getBlockState(pos.offset(x, 0, z)).`is`(BlockTags.FLOWERS)) return true
            }
        }
        return false
    }

    private fun findNearbyBeeNest(level: ServerLevel, centerPos: BlockPos): BlockPos? {
        for (y in 0..4){
            val checkPos = centerPos.above(y)
            if (level.getBlockState(checkPos).`is`(BlockTags.LOGS)){
                for (dir in Direction.Plane.HORIZONTAL){
                    val targetPos = checkPos.relative(dir)
                    if (level.getBlockState(targetPos).block == Blocks.BEE_NEST){
                        return targetPos
                    }
                }
            }
        }
        return null
    }

    private fun forcePlaceBeeNest(level: ServerLevel, basePos: BlockPos): Boolean {
        for (y in 2..4){
            val logPos = basePos.above(y)
            if (level.getBlockState(logPos).`is`(BlockTags.LOGS)){
                for (dir in Direction.Plane.HORIZONTAL){
                    val placePos = logPos.relative(dir)
                    if (level.isEmptyBlock(placePos) || level.getBlockState(placePos).`is`(BlockTags.LEAVES)){
                        val state = Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, dir.opposite)
                        level.setBlock(placePos, state, 3)

                        val tile = level.getBlockEntity(placePos)
                        if (tile is BeehiveBlockEntity){
                            for (i in 0..2){
                                val bee = EntityType.BEE.create(level)
                                if (bee != null){
                                    tile.addOccupant(bee)
                                }
                            }
                        }
                        return true
                    }
                }
            }
        }
        return false
    }


}