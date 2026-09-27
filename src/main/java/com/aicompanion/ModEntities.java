package com.aicompanion;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final RegistryKey<EntityType<?>> AI_COMPANION_KEY =
            RegistryKey.of(
                    RegistryKeys.ENTITY_TYPE,
                    Identifier.of(AICompanion.MOD_ID, "ai_companion")
            );

    public static final EntityType<AICompanionEntity> AI_COMPANION =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    AI_COMPANION_KEY,
                    EntityType.Builder.create(
                            AICompanionEntity::new,
                            SpawnGroup.CREATURE
                    )
                    .dimensions(0.6F, 1.8F)
                    .build(AI_COMPANION_KEY)
            );

    public static void register() {
        FabricDefaultAttributeRegistry.register(
                AI_COMPANION,
                AICompanionEntity.createAICompanionAttributes()
        );
    }
}
