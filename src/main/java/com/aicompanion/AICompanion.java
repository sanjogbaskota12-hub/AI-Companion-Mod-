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
                                        "AI Companion: Spawn command received!"
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
                                        "AI Companion: Follow command received!"
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
