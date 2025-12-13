package org.mnight.beehivetweak

import com.mnightsoon.beehivetweak.ModConfig
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
import net.neoforged.neoforge.event.entity.player.BonemealEvent
import net.neoforged.neoforge.event.level.block.CropGrowEvent
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random


object BeeLuckHandler {
    private val bonemealCache = ConcurrentHashMap.newKeySet<BlockPos>()

    private val isDebugEnabled: Boolean
        get() = ModConfig.GENERAL.debugMode.get()

    private val baseCurrent: Double
        get() = ModConfig.GENERAL.baseChance.get()

    @SubscribeEvent
    fun onCropGrow (event: CropGrowEvent.Pre){
        val level = event.level as? ServerLevel ?: return
        val pos = event.pos

        if (bonemealCache.contains(pos)) return
        val state = event.state
        if (state.block !is SaplingBlock) return
        if (!hasFlowerNearby(level, pos)) return

        val player = level.getNearestPlayer(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 10.0, false) ?: return
        scheduleCheck(level, pos, player, "Natural/Squat")
    }

    @SubscribeEvent
    fun onBonemeal(event: BonemealEvent){
        val level = event.level as? ServerLevel ?: return
        val pos = event.pos
        val state = event.state

        if (state.block !is SaplingBlock) {
            return
        }

        if (!hasFlowerNearby(level, pos)) {
            return
        }

        val player = event.player ?: level.getNearestPlayer(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 10.0, false)

        if (player == null) return

        bonemealCache.add(pos)
        scheduleCheck(level, pos, player, "BoneMeal")

        level.server.tell(TickTask(level.server.tickCount + 1) { ->
            bonemealCache.remove(pos)
        })
    }

    private fun scheduleCheck(level: ServerLevel, pos: BlockPos, player: Player, source: String) {
        debugMsg(level, pos, "⏳ Scheduling check from $source for ${player.name.string}")
        level.server.tell(TickTask(level.server.tickCount + 1) {
            checkAndApplyPity(level, pos, player)
        })
    }

    private fun checkAndApplyPity(level: ServerLevel, saplingPos: BlockPos, player: Player){
        val baseLogPos = saplingPos

        val vanillaNestPos = findNearbyBeeNest(level, baseLogPos)

        if (!level.getBlockState(baseLogPos).`is`(BlockTags.LOGS)){
            return
        }

        if (vanillaNestPos != null) {
            debugMsg(level, saplingPos, "♻️ Vanilla spawned a nest. Removing...")
            level.setBlock(vanillaNestPos, Blocks.AIR.defaultBlockState(), 3)
        }

        val failures = player.getData(ModRegistry.FAILED_ATTEMPTS)
        val currentChange = baseCurrent * (1 shl failures)

        debugMsg(level, saplingPos, "🎲 Rolling... Failures: $failures, Chance: ${currentChange *100}%")

        if (Random.nextFloat() < currentChange) {
            if (forcePlaceBeeNest(level, baseLogPos)) {
                debugMsg(level, saplingPos, "🎉 SUCCESS! Bee nest placed.")
                player.setData(ModRegistry.FAILED_ATTEMPTS, 0)
            } else {
                debugMsg(level, saplingPos, "❌ Success roll, but NO SPACE to place nest.")
                player.setData(ModRegistry.FAILED_ATTEMPTS, failures + 1)
            }
        } else {
            debugMsg(level, saplingPos, "💀 Failed roll. Pity +1")
            player.setData(ModRegistry.FAILED_ATTEMPTS, failures + 1)
        }
    }

    private fun debugMsg(level: Level, pos: BlockPos, msg: String){
        if (!isDebugEnabled) return
        println("[BeeMod] $msg")
        level.getNearestPlayer(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 20.0, false)?.sendSystemMessage(Component.literal("§e[Debug] $msg"))
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
        for (y in 1..3){
            val logPos = basePos.above(y)
            if (level.getBlockState(logPos).`is`(BlockTags.LOGS)){
                for (dir in Direction.Plane.HORIZONTAL){
                    val placePos = logPos.relative(dir)
                    if (level.isEmptyBlock(placePos) || level.getBlockState(placePos).`is`(BlockTags.LEAVES)){
                        val state = Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, dir)
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