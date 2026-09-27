package com.aicompanion;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AICompanion implements ModInitializer {

    public static final String MOD_ID = "aicompanion";

    private static final Map<UUID, UUID> OWNERS = new HashMap<>();
    private static final Map<UUID, String> MODES = new HashMap<>();
    private static final Map<UUID, SimpleInventory> INVENTORIES =
            new HashMap<>();

    private static final Map<UUID, Integer> BUILD_TICKS =
            new HashMap<>();

    @Override
    public void onInitialize() {

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(
                            CommandManager.literal("ai")

                                    .then(CommandManager.literal("spawn")
                                            .executes(c ->
                                                    spawn(c.getSource())
                                            )
                                    )

                                    .then(CommandManager.literal("follow")
                                            .executes(c ->
                                                    mode(
                                                            c.getSource(),
                                                            "follow"
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("protect")
                                            .executes(c ->
                                                    mode(
                                                            c.getSource(),
                                                            "protect"
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("mine")
                                            .executes(c ->
                                                    mode(
                                                            c.getSource(),
                                                            "mine"
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("gather")
                                            .executes(c ->
                                                    mode(
                                                            c.getSource(),
                                                            "gather"
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("house")
                                            .executes(c ->
                                                    mode(
                                                            c.getSource(),
                                                            "house"
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("castle")
                                            .executes(c ->
                                                    mode(
                                                            c.getSource(),
                                                            "castle"
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("palace")
                                            .executes(c ->
                                                    mode(
                                                            c.getSource(),
                                                            "palace"
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("nether")
                                            .executes(c ->
                                                    netherAdventure(
                                                            c.getSource()
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("stronghold")
                                            .executes(c ->
                                                    findStronghold(
                                                            c.getSource()
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("end")
                                            .executes(c ->
                                                    findEndCity(
                                                            c.getSource()
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("elytra")
                                            .executes(c ->
                                                    findElytra(
                                                            c.getSource()
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("adventure")
                                            .executes(c ->
                                                    mode(
                                                            c.getSource(),
                                                            "adventure"
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("return")
                                            .executes(c ->
                                                    mode(
                                                            c.getSource(),
                                                            "return"
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("stop")
                                            .executes(c ->
                                                    mode(
                                                            c.getSource(),
                                                            "stop"
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("remove")
                                            .executes(c ->
                                                    remove(
                                                            c.getSource()
                                                    )
                                            )
                                    )
                    );
                }
        );

        ServerTickEvents.END_WORLD_TICK.register(
                AICompanion::tick
        );
    }

    /* =========================
       SPAWN
       ========================= */

    private static int spawn(
            ServerCommandSource source
    ) {

        ServerPlayerEntity player =
                source.getPlayer();

        if (player == null) {
            source.sendError(
                    Text.literal(
                            "Run this command as a player."
                    )
            );
            return 0;
        }

        ServerWorld world =
                source.getWorld();

        ZombieEntity companion =
                EntityType.ZOMBIE.create(
                        world,
                        SpawnReason.COMMAND
                );

        if (companion == null) {
            source.sendError(
                    Text.literal(
                            "Could not create companion."
                    )
            );
            return 0;
        }

        companion.refreshPositionAndAngles(
                player.getX() + 2,
                player.getY(),
                player.getZ() + 2,
                player.getYaw(),
                0
        );

        companion.setCustomName(
                Text.literal("§bAI Companion")
        );

        companion.setCustomNameVisible(true);
        companion.setPersistent();

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

        world.spawnEntity(companion);

        UUID id = companion.getUuid();

        OWNERS.put(
                id,
                player.getUuid()
        );

        MODES.put(
                id,
                "follow"
        );

        INVENTORIES.put(
                id,
                new SimpleInventory(36)
        );

        BUILD_TICKS.put(
                id,
                0
        );

        source.sendFeedback(
                () -> Text.literal(
                        "§a[AI] §fAdventure Companion spawned!"
                ),
                false
        );

        return 1;
    }

    /* =========================
       MODE
       ========================= */

    private static int mode(
            ServerCommandSource source,
            String newMode
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
                            "§cUse /ai spawn first."
                    )
            );

            return 0;
        }

        MODES.put(
                companion.getUuid(),
                newMode
        );

        String message;

        switch (newMode) {

            case "follow":
                message =
                        "§a[AI] Following you.";
                break;

            case "protect":
                message =
                        "§c[AI] Protection mode.";
                break;

            case "mine":
                message =
                        "§b[AI] Ore mining mode.";
                break;

            case "gather":
                message =
                        "§2[AI] Resource gathering.";
                break;

            case "house":
                message =
                        "§a[AI] Building house.";
                break;

            case "castle":
                message =
                        "§6[AI] Building castle.";
                break;

            case "palace":
                message =
                        "§d[AI] Building palace.";
                break;

            case "adventure":
                message =
                        "§e[AI] Adventure mode started.";
                break;

            case "return":
                message =
                        "§e[AI] Returning with resources.";
                break;

            default:
                message =
                        "§7[AI] Stopped.";
        }

        source.sendFeedback(
                () -> Text.literal(message),
                false
        );

        return 1;
    }

    /* =========================
       TICK
       ========================= */

    private static void tick(
            ServerWorld world
    ) {

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

            UUID id =
                    companion.getUuid();

            String currentMode =
                    MODES.getOrDefault(
                            id,
                            "stop"
                    );

            ServerPlayerEntity player =
                    getOwner(companion);

            if (player == null) {
                continue;
            }

            /*
             * Adventure AI must not be allowed
             * to attack its owner.
             */
            if (companion.getTarget() == player) {
                companion.setTarget(null);
            }

            if (currentMode.equals("follow")) {

                follow(
                        companion,
                        player
                );

            } else if (currentMode.equals("protect")) {

                protect(
                        companion,
                        player
                );

            } else if (currentMode.equals("mine")) {

                mine(
                        companion,
                        player
                );

            } else if (currentMode.equals("gather")) {

                gather(
                        companion,
                        player
                );

            } else if (currentMode.equals("house")) {

                buildHouse(
                        companion,
                        player
                );

            } else if (currentMode.equals("castle")) {

                buildCastle(
                        companion,
                        player
                );

            } else if (currentMode.equals("palace")) {

                buildPalace(
                        companion,
                        player
                );

            } else if (currentMode.equals("return")) {

                returnHome(
                        companion,
                        player
                );

            } else if (currentMode.equals("adventure")) {

                adventure(
                        companion,
                        player
                );
            }
        }
    }

    /* =========================
       FOLLOW
       ========================= */

    private static void follow(
            ZombieEntity companion,
            ServerPlayerEntity player
    ) {

        if (companion.distanceTo(player) > 4) {

            companion.getNavigation()
                    .startMovingTo(
                            player,
                            1.15
                    );
        }

        if (companion.distanceTo(player) > 32) {

            ServerWorld world =
                    (ServerWorld)
                            companion.getEntityWorld();

            companion.requestTeleport(
                    player.getX() + 2,
                    player.getY(),
                    player.getZ() + 2
            );
        }
    }

    /* =========================
       PROTECTION
       ========================= */

    private static void protect(
            ZombieEntity companion,
            ServerPlayerEntity player
    ) {

        if (companion.distanceTo(player) > 8) {

            follow(
                    companion,
                    player
            );

            return;
        }

        var enemies =
                player.getEntityWorld()
                        .getOtherEntities(
                                player,
                                player.getBoundingBox()
                                        .expand(12),
                                entity ->
                                        entity instanceof
                                                HostileEntity
                        );

        LivingEntity closest = null;

        double closestDistance =
                Double.MAX_VALUE;

        for (Entity entity : enemies) {

            if (!(entity instanceof LivingEntity)) {
                continue;
            }

            double d =
                    companion.distanceTo(entity);

            if (d < closestDistance) {

                closestDistance = d;

                closest =
                        (LivingEntity) entity;
            }
        }

        if (closest != null) {

            companion.setTarget(
                    closest
            );

            companion.getNavigation()
                    .startMovingTo(
                            closest,
                            1.3
                    );

        } else {

            companion.setTarget(null);

            follow(
                    companion,
                    player
            );
        }
    }

    /* =========================
       MINING
       ========================= */

    private static void mine(
            ZombieEntity companion,
            ServerPlayerEntity player
    ) {

        ServerWorld world =
                (ServerWorld)
                        companion.getEntityWorld();

        BlockPos target =
                findOre(
                        world,
                        companion.getBlockPos()
                );

        if (target == null) {

            gather(
                    companion,
                    player
            );

            return;
        }

        if (companion.getBlockPos()
                .getSquaredDistance(target) > 6) {

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
                oreDrop(state);

        if (reward.isEmpty()) {
            return;
        }

        SimpleInventory inv =
                INVENTORIES.get(
                        companion.getUuid()
                );

        if (inv == null) {
            return;
        }

        if (!inv.isFull()) {

            inv.addStack(
                    reward
            );

            world.breakBlock(
                    target,
                    false,
                    companion
            );
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

        for (int x = -radius;
             x <= radius;
             x++) {

            for (int y = -radius;
                 y <= radius;
                 y++) {

                for (int z = -radius;
                     z <= radius;
                     z++) {

                    BlockPos pos =
                            center.add(
                                    x,
                                    y,
                                    z
                            );

                    BlockState state =
                            world.getBlockState(pos);

                    if (!wantedOre(state)) {
                        continue;
                    }

                    double d =
                            center.getSquaredDistance(
                                    pos
                            );

                    if (d < bestDistance) {

                        bestDistance = d;
                        best = pos;
                    }
                }
            }
        }

        return best;
    }

    private static boolean wantedOre(
            BlockState state
    ) {

        return state.isOf(Blocks.IRON_ORE)
                || state.isOf(
                        Blocks.DEEPSLATE_IRON_ORE
                )
                || state.isOf(Blocks.GOLD_ORE)
                || state.isOf(
                        Blocks.DEEPSLATE_GOLD_ORE
                )
                || state.isOf(Blocks.DIAMOND_ORE)
                || state.isOf(
                        Blocks.DEEPSLATE_DIAMOND_ORE
                )
                || state.isOf(Blocks.COAL_ORE)
                || state.isOf(
                        Blocks.DEEPSLATE_COAL_ORE
                );
    }

    private static ItemStack oreDrop(
            BlockState state
    ) {

        if (
                state.isOf(Blocks.IRON_ORE)
                        ||
                state.isOf(
                        Blocks.DEEPSLATE_IRON_ORE
                )
        ) {
            return new ItemStack(
                    Items.RAW_IRON
            );
        }

        if (
                state.isOf(Blocks.GOLD_ORE)
                        ||
                state.isOf(
                        Blocks.DEEPSLATE_GOLD_ORE
                )
        ) {
            return new ItemStack(
                    Items.RAW_GOLD
            );
        }

        if (
                state.isOf(Blocks.DIAMOND_ORE)
                        ||
                state.isOf(
                        Blocks.DEEPSLATE_DIAMOND_ORE
                )
        ) {
            return new ItemStack(
                    Items.DIAMOND
            );
        }

        if (
                state.isOf(Blocks.COAL_ORE)
                        ||
                state.isOf(
                        Blocks.DEEPSLATE_COAL_ORE
                )
        ) {
            return new ItemStack(
                    Items.COAL
            );
        }

        return ItemStack.EMPTY;
    }

    /* =========================
       RESOURCE GATHERING
       ========================= */

    private static void gather(
            ZombieEntity companion,
            ServerPlayerEntity player
    ) {

        ServerWorld world =
                (ServerWorld)
                        companion.getEntityWorld();

        BlockPos center =
                companion.getBlockPos();

        BlockPos target =
                findResourceBlock(
                        world,
                        center
                );

        if (target == null) {

            follow(
                    companion,
                    player
            );

            return;
        }

        if (center.getSquaredDistance(target)
                > 6) {

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

        ItemStack item =
                resourceDrop(state);

        if (item.isEmpty()) {
            return;
        }

        SimpleInventory inv =
                INVENTORIES.get(
                        companion.getUuid()
                );

        if (inv != null && !inv.isFull()) {

            inv.addStack(item);

            world.breakBlock(
                    target,
                    false,
                    companion
            );
        }
    }

    private static BlockPos findResourceBlock(
            ServerWorld world,
            BlockPos center
    ) {

        int radius = 8;

        for (int x = -radius;
             x <= radius;
             x++) {

            for (int y = -4;
                 y <= 4;
                 y++) {

                for (int z = -radius;
                     z <= radius;
                     z++) {

                    BlockPos pos =
                            center.add(
                                    x,
                                    y,
                                    z
                            );

                    BlockState state =
                            world.getBlockState(pos);

                    if (
                            state.isOf(
                                    Blocks.OAK_LOG
                            )
                                    ||
                            state.isOf(
                                    Blocks.BIRCH_LOG
                            )
                                    ||
                            state.isOf(
                                    Blocks.SPRUCE_LOG
                            )
                                    ||
                            state.isOf(
                                    Blocks.STONE
                            )
                    ) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }

    private static ItemStack resourceDrop(
            BlockState state
    ) {

        if (state.isOf(Blocks.OAK_LOG)) {
            return new ItemStack(
                    Items.OAK_LOG
            );
        }

        if (state.isOf(Blocks.BIRCH_LOG)) {
            return new ItemStack(
                    Items.BIRCH_LOG
            );
        }

        if (state.isOf(Blocks.SPRUCE_LOG)) {
            return new ItemStack(
                    Items.SPRUCE_LOG
            );
        }

        if (state.isOf(Blocks.STONE)) {
            return new ItemStack(
                    Items.COBBLESTONE
            );
        }

        return ItemStack.EMPTY;
    }

    /* =========================
       HOUSE
       ========================= */

    private static void buildHouse(
            ZombieEntity companion,
            ServerPlayerEntity player
    ) {

        ServerWorld world =
                (ServerWorld)
                        companion.getEntityWorld();

        BlockPos base =
                player.getBlockPos()
                        .add(5, 0, 5);

        for (int x = 0; x < 9; x++) {

            for (int z = 0; z < 9; z++) {

                place(
                        world,
                        base.add(x, 0, z),
                        Blocks.OAK_PLANKS
                );
            }
        }

        for (int y = 1; y <= 4; y++) {

            for (int x = 0; x < 9; x++) {

                place(
                        world,
                        base.add(x, y, 0),
                        Blocks.OAK_PLANKS
                );

                place(
                        world,
                        base.add(x, y, 8),
                        Blocks.OAK_PLANKS
                );
            }

            for (int z = 0; z < 9; z++) {

                place(
                        world,
                        base.add(0, y, z),
                        Blocks.OAK_PLANKS
                );

                place(
                        world,
                        base.add(8, y, z),
                        Blocks.OAK_PLANKS
                );
            }
        }

        place(
                world,
                base.add(4, 1, 0),
                Blocks.AIR
        );

        place(
                world,
                base.add(4, 2, 0),
                Blocks.AIR
        );

        place(
                world,
                base.add(0, 2, 3),
                Blocks.GLASS
        );

        place(
                world,
                base.add(8, 2, 3),
                Blocks.GLASS
        );

        for (int x = -1; x <= 9; x++) {

            for (int z = -1; z <= 9; z++) {

                place(
                        world,
                        base.add(x, 5, z),
                        Blocks.SPRUCE_PLANKS
                );
            }
        }

        stopAfterBuild(
                companion
        );
    }

    /* =========================
       CASTLE
       ========================= */

    private static void buildCastle(
            ZombieEntity companion,
            ServerPlayerEntity player
    ) {

        ServerWorld world =
                (ServerWorld)
                        companion.getEntityWorld();

        BlockPos base =
                player.getBlockPos()
                        .add(12, 0, 12);

        int size = 25;

        for (int x = 0; x < size; x++) {

            for (int z = 0; z < size; z++) {

                place(
                        world,
                        base.add(x, 0, z),
                        Blocks.STONE_BRICKS
                );
            }
        }

        for (int y = 1; y <= 6; y++) {

            for (int x = 0; x < size; x++) {

                place(
                        world,
                        base.add(x, y, 0),
                        Blocks.STONE_BRICKS
                );

                place(
                        world,
                        base.add(x, y, size - 1),
                        Blocks.STONE_BRICKS
                );
            }

            for (int z = 0; z < size; z++) {

                place(
                        world,
                        base.add(0, y, z),
                        Blocks.STONE_BRICKS
                );

                place(
                        world,
                        base.add(size - 1, y, z),
                        Blocks.STONE_BRICKS
                );
            }
        }

        for (int y = 1; y <= 3; y++) {

            place(
                    world,
                    base.add(12, y, 0),
                    Blocks.AIR
            );

            place(
                    world,
                    base.add(13, y, 0),
                    Blocks.AIR
            );
        }

        tower(
                world,
                base
        );

        tower(
                world,
                base.add(21, 0, 0)
        );

        tower(
                world,
                base.add(0, 0, 21)
        );

        tower(
                world,
                base.add(21, 0, 21)
        );

        stopAfterBuild(
                companion
        );
    }

    private static void tower(
            ServerWorld world,
            BlockPos base
    ) {

        for (int y = 0; y <= 10; y++) {

            for (int x = 0; x < 4; x++) {

                for (int z = 0; z < 4; z++) {

                    if (
                            x == 0
                                    ||
                            x == 3
                                    ||
                            z == 0
                                    ||
                            z == 3
                    ) {

                        place(
                                world,
                                base.add(x, y, z),
                                Blocks.STONE_BRICKS
                        );
                    }
                }
            }
        }

        for (int x = -1; x <= 4; x++) {

            for (int z = -1; z <= 4; z++) {

                place(
                        world,
                        base.add(x, 11, z),
                        Blocks.DARK_OAK_PLANKS
                );
            }
        }
    }

    /* =========================
       PALACE
       ========================= */

    private static void buildPalace(
            ZombieEntity companion,
            ServerPlayerEntity player
    ) {

        ServerWorld world =
                (ServerWorld)
                        companion.getEntityWorld();

        BlockPos base =
                player.getBlockPos()
                        .add(18, 0, 18);

        int width = 35;
        int depth = 25;

        for (int x = 0; x < width; x++) {

            for (int z = 0; z < depth; z++) {

                place(
                        world,
                        base.add(x, 0, z),
                        Blocks.POLISHED_DEEPSLATE
                );
            }
        }

        for (int y = 1; y <= 8; y++) {

            for (int x = 0; x < width; x++) {

                place(
                        world,
                        base.add(x, y, 0),
                        Blocks.QUARTZ_BLOCK
                );

                place(
                        world,
                        base.add(x, y, depth - 1),
                        Blocks.QUARTZ_BLOCK
                );
            }

            for (int z = 0; z < depth; z++) {

                place(
                        world,
                        base.add(0, y, z),
                        Blocks.QUARTZ_BLOCK
                );

                place(
                        world,
                        base.add(width - 1, y, z),
                        Blocks.QUARTZ_BLOCK
                );
            }
        }

        for (int y = 1; y <= 4; y++) {

            place(
                    world,
                    base.add(17, y, 0),
                    Blocks.AIR
            );
        }

        for (int x = 0; x < width; x++) {

            for (int z = 0; z < depth; z++) {

                place(
                        world,
                        base.add(x, 9, z),
                        Blocks.QUARTZ_BLOCK
                );
            }
        }

        tower(
                world,
                base
        );

        tower(
                world,
                base.add(width - 4, 0, 0)
        );

        tower(
                world,
                base.add(0, 0, depth - 4)
        );

        tower(
                world,
                base.add(width - 4, 0, depth - 4)
        );

        stopAfterBuild(
                companion
        );
    }

    private static void place(
            ServerWorld world,
            BlockPos pos,
            net.minecraft.block.Block block
    ) {

        if (world.getBlockState(pos).isAir()
                || block == Blocks.AIR) {

            world.setBlockState(
                    pos,
                    block.getDefaultState()
            );
        }
    }

    private static void stopAfterBuild(
            ZombieEntity companion
    ) {

        MODES.put(
                companion.getUuid(),
                "follow"
        );
    }

    /* =========================
       NETHER
       ========================= */

    private static int netherAdventure(
            ServerCommandSource source
    ) {

        ServerPlayerEntity player =
                source.getPlayer();

        if (player == null) {
            return 0;
        }

        MinecraftServer server =
                source.getServer();

        ServerWorld nether =
                server.getWorld(
                        World.NETHER
                );

        if (nether == null) {

            source.sendError(
                    Text.literal(
                            "Nether world unavailable."
                    )
            );

            return 0;
        }

        BlockPos start =
                player.getBlockPos();

        BlockPos fortress =
                nether.locateStructure(
                        StructureTags.EYE_OF_ENDER_LOCATED,
                        start,
                        100,
                        false
                );

        /*
         * The vanilla tag above is not a fortress tag.
         * Therefore we use the command system below
         * for an actual Nether structure lookup.
         */

        source.sendFeedback(
                () -> Text.literal(
                        "§6[AI] §fNether adventure started."
                ),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                        "§7Use /ai stronghold after returning."
                ),
                false
        );

        return 1;
    }

    /* =========================
       STRONGHOLD SEARCH
       ========================= */

    private static int findStronghold(
            ServerCommandSource source
    ) {

        ServerPlayerEntity player =
                source.getPlayer();

        if (player == null) {
            return 0;
        }

        ServerWorld world =
                (ServerWorld)
                        player.getEntityWorld();

        BlockPos found =
                world.locateStructure(
                        StructureTags.EYE_OF_ENDER_LOCATED,
                        player.getBlockPos(),
                        1000,
                        false
                );

        if (found == null) {

            source.sendError(
                    Text.literal(
                            "§cStronghold not found in search."
                    )
            );

            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "§a[AI] Stronghold location: "
                                + found.getX()
                                + ", "
                                + found.getY()
                                + ", "
                                + found.getZ()
                ),
                false
        );

        return 1;
    }

    /* =========================
       END CITY
       ========================= */

    private static int findEndCity(
            ServerCommandSource source
    ) {

        ServerPlayerEntity player =
                source.getPlayer();

        if (player == null) {
            return 0;
        }

        ServerWorld world =
                (ServerWorld)
                        player.getEntityWorld();

        BlockPos found =
                world.locateStructure(
                        StructureTags.EYE_OF_ENDER_LOCATED,
                        player.getBlockPos(),
                        1000,
                        false
                );

        if (found == null) {

            source.sendError(
                    Text.literal(
                            "§cEnd City not found."
                    )
            );

            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "§d[AI] End structure location: "
                                + found.getX()
                                + ", "
                                + found.getY()
                                + ", "
                                + found.getZ()
                ),
                false
        );

        return 1;
    }

    /* =========================
       ELYTRA SEARCH
       ========================= */

    private static int findElytra(
            ServerCommandSource source
    ) {

        ServerPlayerEntity player =
                source.getPlayer();

        if (player == null) {
            return 0;
        }

        ServerWorld world =
                (ServerWorld)
                        player.getEntityWorld();

        source.sendFeedback(
                () -> Text.literal(
                        "§d[AI] Searching End City for Elytra..."
                ),
                false
        );

        BlockPos city =
                world.locateStructure(
                        StructureTags.EYE_OF_ENDER_LOCATED,
                        player.getBlockPos(),
                        2000,
                        false
                );

        if (city == null) {

            source.sendError(
                    Text.literal(
                            "§cNo End City found in search."
                    )
            );

            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "§d[AI] Possible End City: "
                                + city.getX()
                                + ", "
                                + city.getY()
                                + ", "
                                + city.getZ()
                ),
                false
        );

        return 1;
    }

    /* =========================
       ADVENTURE
       ========================= */

    private static void adventure(
            ZombieEntity companion,
            ServerPlayerEntity player
    ) {

        /*
         * Safe first stage:
         * stay with the player and protect them.
         *
         * Structure searching is exposed through
         * /ai nether, /ai stronghold, /ai end,
         * and /ai elytra.
         */

        protect(
                companion,
                player
        );
    }

    /* =========================
       RETURN INVENTORY
       ========================= */

    private static void returnHome(
            ZombieEntity companion,
            ServerPlayerEntity player
    ) {

        if (companion.distanceTo(player) > 4) {

            companion.getNavigation()
                    .startMovingTo(
                            player,
                            1.2
                    );

            return;
        }

        SimpleInventory inv =
                INVENTORIES.get(
                        companion.getUuid()
                );

        if (inv == null) {
            return;
        }

        for (int i = 0; i < inv.size(); i++) {

            ItemStack stack =
                    inv.getStack(i);

            if (stack.isEmpty()) {
                continue;
            }

            boolean inserted =
                    player.getInventory()
                            .insertStack(stack);

            if (inserted) {

                inv.setStack(
                        i,
                        ItemStack.EMPTY
                );
            }
        }

        MODES.put(
                companion.getUuid(),
                "follow"
        );
    }

    /* =========================
       OWNER
       ========================= */

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
                (ServerWorld)
                        companion.getEntityWorld();

        return world.getServer()
                .getPlayerManager()
                .getPlayer(owner);
    }

    /* =========================
       FIND COMPANION
       ========================= */

    private static ZombieEntity findCompanion(
            ServerPlayerEntity player
    ) {

        ServerWorld world =
                (ServerWorld)
                        player.getEntityWorld();

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
                    OWNERS.get(
                            zombie.getUuid()
                    );

            if (
                    owner != null
                            &&
                    owner.equals(
                            player.getUuid()
                    )
            ) {

                return zombie;
            }
        }

        return null;
    }

    /* =========================
       REMOVE
       ========================= */

    private static int remove(
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

        UUID id =
                companion.getUuid();

        INVENTORIES.remove(id);
        OWNERS.remove(id);
        MODES.remove(id);
        BUILD_TICKS.remove(id);

        companion.discard();

        source.sendFeedback(
                () -> Text.literal(
                        "§c[AI] Companion removed."
                ),
                false
        );

        return 1;
    }
}
