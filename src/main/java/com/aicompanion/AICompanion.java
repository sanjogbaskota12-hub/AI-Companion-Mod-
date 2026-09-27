package com.aicompanion;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AICompanion implements ModInitializer {

    public static final String MOD_ID = "aicompanion";

    private static final Map<UUID, SimpleInventory> INVENTORIES = new HashMap<>();
    private static final Map<UUID, UUID> OWNERS = new HashMap<>();

    private static final Map<UUID, String> MODES = new HashMap<>();

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
                            .executes(context ->
                                setMode(context.getSource(), "follow")
                            )
                        )

                        .then(CommandManager.literal("stop")
                            .executes(context ->
                                setMode(context.getSource(), "stop")
                            )
                        )

                        .then(CommandManager.literal("mine")
                            .executes(context ->
                                setMode(context.getSource(), "mine")
                            )
                        )

                        .then(CommandManager.literal("return")
                            .executes(context ->
                                setMode(context.getSource(), "return")
                            )
                        )

                        .then(CommandManager.literal("work")
                            .executes(context ->
                                setMode(context.getSource(), "work")
                            )
                        )

                        .then(CommandManager.literal("build")
                            .executes(context ->
                                setMode(context.getSource(), "build")
                            )
                        )

                        .then(CommandManager.literal("remove")
                            .executes(context ->
                                removeCompanion(context.getSource())
                            )
                        )
                );
            }
        );

        ServerTickEvents.END_WORLD_TICK.register(world ->
            tickCompanions(world)
        );
    }

    private static int spawnCompanion(ServerCommandSource source) {

        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(
                Text.literal("Command must be run by a player.")
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
                Text.literal("Could not create companion.")
            );
            return 0;
        }

        companion.refreshPositionAndAngles(
            player.getX() + 2,
            player.getY(),
            player.getZ(),
            player.getYaw(),
            0
        );

        companion.setCustomName(
            Text.literal("§bAI Companion")
        );

        companion.setCustomNameVisible(true);
        companion.setPersistent();

        // Companion should not attack the player.
        companion.setTarget(null);

        // Give it a pickaxe.
        companion.equipStack(
            net.minecraft.entity.EquipmentSlot.MAINHAND,
            new ItemStack(Items.IRON_PICKAXE)
        );

        world.spawnEntity(companion);

        INVENTORIES.put(
            companion.getUuid(),
            new SimpleInventory(27)
        );

        OWNERS.put(
            companion.getUuid(),
            player.getUuid()
        );

        MODES.put(
            companion.getUuid(),
            "follow"
        );

        source.sendFeedback(
            () -> Text.literal(
                "§a[AI Companion] §fCompanion created!"
            ),
            false
        );

        return 1;
    }

    private static int setMode(
        ServerCommandSource source,
        String mode
    ) {

        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            return 0;
        }

        ZombieEntity companion =
            findCompanion(player);

        if (companion == null) {

            source.sendError(
                Text.literal(
                    "§cNo AI Companion found. Use /ai spawn"
                )
            );

            return 0;
        }

        MODES.put(
            companion.getUuid(),
            mode
        );

        String message;

        switch (mode) {

            case "follow":
                message =
                    "§a[AI] §fFollowing you.";
                break;

            case "mine":
                message =
                    "§b[AI] §fMining iron, gold and diamond.";
                break;

            case "return":
                message =
                    "§e[AI] §fReturning to you.";
                break;

            case "build":
                message =
                    "§6[AI] §fBuilding mode started.";
                break;

            case "work":
                message =
                    "§a[AI] §fWorking.";
                break;

            default:
                message =
                    "§e[AI] §fStopped.";
        }

        source.sendFeedback(
            () -> Text.literal(message),
            false
        );

        return 1;
    }

    private static void tickCompanions(ServerWorld world) {

        for (ZombieEntity companion :
            world.getEntitiesByType(
                EntityType.ZOMBIE,
                entity ->
                    entity.hasCustomName()
                    &&
                    entity.getCustomName() != null
                    &&
                    entity.getCustomName()
                        .getString()
                        .contains("AI Companion")
            )
        ) {

            UUID id = companion.getUuid();

            String mode = MODES.get(id);

            if (mode == null) {
                mode = "stop";
            }

            ServerPlayerEntity player =
                getOwner(companion);

            if (player == null) {
                continue;
            }

            // Stop normal zombie attacking.
            companion.setTarget(null);

            if (mode.equals("follow")) {
                followPlayer(companion, player);
            }

            else if (mode.equals("mine")) {
                miningAI(companion, player);
            }

            else if (mode.equals("return")) {
                returnToPlayer(companion, player);
            }

            else if (mode.equals("build")) {
                buildPalace(companion, player);
            }
        }
    }

    private static void followPlayer(
        ZombieEntity companion,
        ServerPlayerEntity player
    ) {

        double distance =
            companion.distanceTo(player);

        if (distance > 4) {

            companion.getNavigation()
                .startMovingTo(
                    player,
                    1.15
                );
        }

        if (distance > 30) {

            companion.teleport(
                player.getServerWorld(),
                player.getX() + 2,
                player.getY(),
                player.getZ() + 2,
                false
            );
        }
    }

    private static void returnToPlayer(
        ZombieEntity companion,
        ServerPlayerEntity player
    ) {

        double distance =
            companion.distanceTo(player);

        if (distance > 3) {

            companion.getNavigation()
                .startMovingTo(
                    player,
                    1.2
                );
        }

        else {

            giveInventoryToPlayer(
                companion,
                player
            );

            MODES.put(
                companion.getUuid(),
                "follow"
            );
        }
    }

    private static void miningAI(
        ZombieEntity companion,
        ServerPlayerEntity player
    ) {

        ServerWorld world =
            (ServerWorld) companion.getEntityWorld();

        BlockPos target =
            findOre(
                world,
                companion.getBlockPos()
            );

        if (target == null) {

            companion.getNavigation()
                .startMovingTo(
                    player,
                    1.0
                );

            return;
        }

        double distance =
            companion.getBlockPos()
                .getSquaredDistance(target);

        if (distance > 6) {

            companion.getNavigation()
                .startMovingTo(
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    1.0
                );

            return;
        }

        BlockState state =
            world.getBlockState(target);

        ItemStack reward =
            getOreDrop(state);

        if (!reward.isEmpty()) {

            SimpleInventory inventory =
                INVENTORIES.get(
                    companion.getUuid()
                );

            if (inventory != null) {

                inventory.addStack(reward);

                world.breakBlock(
                    target,
                    false,
                    companion
                );
            }
        }
    }

    private static BlockPos findOre(
        ServerWorld world,
        BlockPos center
    ) {

        BlockPos best = null;

        double bestDistance =
            Double.MAX_VALUE;

        int radius = 12;

        for (int x = -radius; x <= radius; x++) {

            for (int y = -radius; y <= radius; y++) {

                for (int z = -radius; z <= radius; z++) {

                    BlockPos pos =
                        center.add(x, y, z);

                    BlockState state =
                        world.getBlockState(pos);

                    if (!isWantedOre(state)) {
                        continue;
                    }

                    double distance =
                        center.getSquaredDistance(pos);

                    if (distance < bestDistance) {

                        bestDistance = distance;
                        best = pos;
                    }
                }
            }
        }

        return best;
    }

    private static boolean isWantedOre(
        BlockState state
    ) {

        return state.isOf(Blocks.IRON_ORE)
            || state.isOf(Blocks.DEEPSLATE_IRON_ORE)
            || state.isOf(Blocks.GOLD_ORE)
            || state.isOf(Blocks.DEEPSLATE_GOLD_ORE)
            || state.isOf(Blocks.DIAMOND_ORE)
            || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE);
    }

    private static ItemStack getOreDrop(
        BlockState state
    ) {

        if (
            state.isOf(Blocks.IRON_ORE)
            ||
            state.isOf(Blocks.DEEPSLATE_IRON_ORE)
        ) {

            return new ItemStack(
                Items.RAW_IRON
            );
        }

        if (
            state.isOf(Blocks.GOLD_ORE)
            ||
            state.isOf(Blocks.DEEPSLATE_GOLD_ORE)
        ) {

            return new ItemStack(
                Items.RAW_GOLD
            );
        }

        if (
            state.isOf(Blocks.DIAMOND_ORE)
            ||
            state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)
        ) {

            return new ItemStack(
                Items.DIAMOND
            );
        }

        return ItemStack.EMPTY;
    }

    private static void giveInventoryToPlayer(
        ZombieEntity companion,
        ServerPlayerEntity player
    ) {

        SimpleInventory inventory =
            INVENTORIES.get(
                companion.getUuid()
            );

        if (inventory == null) {
            return;
        }

        for (int i = 0; i < inventory.size(); i++) {

            ItemStack stack =
                inventory.getStack(i);

            if (stack.isEmpty()) {
                continue;
            }

            ItemStack remaining =
                player.getInventory()
                    .insertStack(stack.copy());

            inventory.setStack(
                i,
                remaining
            );
        }
    }

    private static ServerPlayerEntity getOwner(
        ZombieEntity companion
    ) {

        UUID owner =
            OWNERS.get(
                companion.getUuid()
            );

        if (owner == null) {
            return null;
        }

        ServerWorld world =
            (ServerWorld) companion.getEntityWorld();

        return world.getServer()
            .getPlayerManager()
            .getPlayer(owner);
    }

    private static ZombieEntity findCompanion(
        ServerPlayerEntity player
    ) {

        ServerWorld world =
            player.getServerWorld();

        for (ZombieEntity zombie :
            world.getEntitiesByType(
                EntityType.ZOMBIE,
                entity ->
                    entity.hasCustomName()
                    &&
                    entity.getCustomName() != null
                    &&
                    entity.getCustomName()
                        .getString()
                        .contains("AI Companion")
            )
        ) {

            UUID owner =
                OWNERS.get(zombie.getUuid());

            if (
                owner != null
                &&
                owner.equals(player.getUuid())
            ) {
                return zombie;
            }
        }

        return null;
    }

    private static int removeCompanion(
        ServerCommandSource source
    ) {

        ServerPlayerEntity player =
            source.getPlayer();

        if (player == null) {
            return 0;
        }

        ZombieEntity companion =
            findCompanion(player);

        if (companion == null) {

            source.sendError(
                Text.literal(
                    "§cNo AI Companion found."
                )
            );

            return 0;
        }

        INVENTORIES.remove(
            companion.getUuid()
        );

        OWNERS.remove(
            companion.getUuid()
        );

        MODES.remove(
            companion.getUuid()
        );

        companion.discard();

        source.sendFeedback(
            () -> Text.literal(
                "§c[AI] §fCompanion removed."
            ),
            false
        );

        return 1;
    }

    private static void buildPalace(
        ZombieEntity companion,
        ServerPlayerEntity player
    ) {

        ServerWorld world =
            (ServerWorld) companion.getEntityWorld();

        BlockPos center =
            player.getBlockPos()
                .add(0, 0, 6);

        // First prototype:
        // 15 x 15 foundation.

        for (int x = -7; x <= 7; x++) {

            for (int z = -7; z <= 7; z++) {

                BlockPos pos =
                    center.add(x, 0, z);

                if (
                    world.getBlockState(pos)
                        .isAir()
                ) {

                    world.setBlockState(
                        pos,
                        Blocks.STONE_BRICKS
                            .getDefaultState()
                    );
                }
            }
        }

        MODES.put(
            companion.getUuid(),
            "follow"
        );
    }
}
