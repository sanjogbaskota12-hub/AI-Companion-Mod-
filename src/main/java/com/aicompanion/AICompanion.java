package com.aicompanion;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
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
                            .executes(context -> {

                                context.getSource().sendFeedback(
                                    () -> Text.literal(
                                        "§a[AI Companion] §fAI Companion spawned!"
                                    ),
                                    false
                                );

                                return 1;
                            })
                        )

                        .then(CommandManager.literal("follow")
                            .executes(context -> {

                                context.getSource().sendFeedback(
                                    () -> Text.literal(
                                        "§a[AI Companion] §fFollowing you!"
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
                                        "§a[AI Companion] §fMining + Building mode enabled!"
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
                                        "§c[AI Companion] §fRemoved."
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
}
