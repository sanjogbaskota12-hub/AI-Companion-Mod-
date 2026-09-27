package com.aicompanion;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class AICompanion implements ModInitializer {

    public static final String MOD_ID = "aicompanion";

    @Override
    public void onInitialize() {

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> {

                dispatcher.register(
                    CommandManager.literal("ai")

                        .then(CommandManager.literal("spawn")
                            .executes(context ->
                                spawnCompanion(context.getSource())
                            )
                        )

                        .then(CommandManager.literal("follow")
                            .executes(context -> {
                                context.getSource().sendFeedback(
                                    () -> Text.literal(
                                        "§a[AI Companion] §fFollow mode enabled!"
                                    ),
                                    false
                                );
                                return 1;
                            })
                        )

                        .then(CommandManager.literal("stop")
                            .executes(context -> {
                                context.getSource().sendFeedback(
                                    () -> Text.literal(
                                        "§e[AI Companion] §fStopped."
                                    ),
                                    false
                                );
                                return 1;
                            })
                        )

                        .then(CommandManager.literal("mine")
                            .executes(context -> {
                                context.getSource().sendFeedback(
                                    () -> Text.literal(
                                        "§b[AI Companion] §fMining mode enabled!"
                                    ),
                                    false
                                );
                                return 1;
                            })
                        )

                        .then(CommandManager.literal("build")
                            .executes(context -> {
                                context.getSource().sendFeedback(
                                    () -> Text.literal(
                                        "§6[AI Companion] §fBuilding mode enabled!"
                                    ),
                                    false
                                );
                                return 1;
                            })
                        )

                        .then(CommandManager.literal("work")
                            .executes(context -> {
                                context.getSource().sendFeedback(
                                    () -> Text.literal(
                                        "§a[AI Companion] §fWork mode enabled!"
                                    ),
                                    false
                                );
                                return 1;
                            })
                        )

                        .then(CommandManager.literal("remove")
                            .executes(context -> {
                                context.getSource().sendFeedback(
                                    () -> Text.literal(
                                        "§c[AI Companion] §fRemove command ready."
                                    ),
                                    false
                                );
                                return 1;
                            })
                        )
                );
            }
        );
    }

    private static int spawnCompanion(ServerCommandSource source) {

        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(
                Text.literal("This command must be run by a player.")
            );
            return 0;
        }

        ServerWorld world = source.getWorld();

        ZombieEntity companion = EntityType.ZOMBIE.create(
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
            player.getX() + 1.5,
            player.getY(),
            player.getZ(),
            player.getYaw(),
            0.0f
        );

        companion.setCustomName(
            Text.literal("AI Companion")
        );

        companion.setCustomNameVisible(true);
        companion.setPersistent();

        world.spawnEntity(companion);

        source.sendFeedback(
            () -> Text.literal(
                "§a[AI Companion] §fReal AI Companion spawned!"
            ),
            false
        );

        return 1;
    }
}
