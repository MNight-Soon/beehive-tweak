package com.mnightsoon.beehivetweak

import net.neoforged.neoforge.common.ModConfigSpec
import org.apache.commons.lang3.tuple.Pair

object ModConfig {
    val SPEC: ModConfigSpec
    val GENERAL: General

    init {
        val pair: Pair<General, ModConfigSpec> = ModConfigSpec.Builder()
            .configure { builder -> General(builder) }
        SPEC = pair.right
        GENERAL = pair.left
    }

    class General(builder: ModConfigSpec.Builder) {
        val debugMode: ModConfigSpec.BooleanValue
        val baseChance: ModConfigSpec.DoubleValue

        init {
            builder.push("general")

            debugMode = builder
                .comment("Enable debug messages in chat (Default: false)")
                .define("debugMode", false) // ตั้งค่าเริ่มต้นเป็น false (ปิด)

            baseChance = builder
                .comment("Base chance for a bee nest to spawn (Default: 0.05 = 5%)")
                .defineInRange("baseChance", 0.05, 0.0, 1.0)

            builder.pop()
        }
    }
}