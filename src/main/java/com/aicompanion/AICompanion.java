package com.aicompanion;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class AICompanion implements ModInitializer {

    public static final String MOD_ID = "aicompanion";

    @Override
    public void onInitialize() {

        ModEntities.register();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(
                            CommandManager.literal("ai")

                                    .then(
                                            CommandManager.literal("spawn")
                                                    .executes(
                                                            c -> spawn(c.getSource())
                                                    )
                                    )

                                    .then(
                                            CommandManager.literal("remove")
                                                    .executes(
                                                            c -> remove(c.getSource())
                                                    )
                                    )
                    );
                }
        );
    }

    private static int spawn(ServerCommandSource source) {

        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            return 0;
        }

        ServerWorld world = source.getWorld();

        AICompanionEntity companion =
                ModEntities.AI_COMPANION.create(
                        world,
                        SpawnReason.COMMAND
                );

        if (companion == null) {
            source.sendError(
                    Text.literal("Could not create AI Companion.")
            );
            return 0;
        }

        companion.refreshPositionAndAngles(
                player.getX() + 2.0,
                player.getY(),
                player.getZ() + 2.0,
                player.getYaw(),
                0.0F
        );

        companion.setCustomName(
                Text.literal("AI Companion")
        );

        companion.setCustomNameVisible(true);

        companion.setOwner(player);

        companion.equipStack(
                EquipmentSlot.MAINHAND,
                new ItemStack(Items.IRON_SWORD)
        );

        companion.equipStack(
                EquipmentSlot.HEAD,
                new ItemStack(Items.IRON_HELMET)
        );

        companion.equipStack(
                EquipmentSlot.CHEST,
                new ItemStack(Items.IRON_CHESTPLATE)
        );

        companion.equipStack(
                EquipmentSlot.LEGS,
                new ItemStack(Items.IRON_LEGGINGS)
        );

        companion.equipStack(
                EquipmentSlot.FEET,
                new ItemStack(Items.IRON_BOOTS)
        );

        companion.setPersistent();

        world.spawnEntity(companion);

        source.sendFeedback(
                () -> Text.literal(
                        "§a[AI] Human-like AI Companion spawned!"
                ),
                false
        );

        return 1;
    }

    private static int remove(ServerCommandSource source) {

        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            return 0;
        }

        ServerWorld world = source.getWorld();

        for (
                AICompanionEntity companion :
                world.getEntitiesByType(
                        ModEntities.AI_COMPANION,
                        entity -> entity.isOwner(player)
                )
        ) {

            companion.discard();

            source.sendFeedback(
                    () -> Text.literal(
                            "§c[AI] Companion removed."
                    ),
                    false
            );

            return 1;
        }

        source.sendError(
                Text.literal(
                        "§c[AI] Companion not found."
                )
        );

        return 0;
    }
}
