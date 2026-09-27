package com.aicompanion;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.block.Block;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
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

    @Override
    public void onInitialize() {

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(
                            CommandManager.literal("ai")

                                    .then(CommandManager.literal("spawn")
                                            .executes(c ->
                                                    spawn(c.getSource())
                                            ))

                                    .then(CommandManager.literal("follow")
                                            .executes(c ->
                                                    setMode(c.getSource(), "follow")
                                            ))

                                    .then(CommandManager.literal("protect")
                                            .executes(c ->
                                                    setMode(c.getSource(), "protect")
                                            ))

                                    .then(CommandManager.literal("mine")
                                            .executes(c ->
                                                    setMode(c.getSource(), "mine")
                                            ))

                                    .then(CommandManager.literal("gather")
                                            .executes(c ->
                                                    setMode(c.getSource(), "gather")
                                            ))

                                    .then(CommandManager.literal("house")
                                            .executes(c ->
                                                    setMode(c.getSource(), "house")
                                            ))

                                    .then(CommandManager.literal("castle")
                                            .executes(c ->
                                                    setMode(c.getSource(), "castle")
                                            ))

                                    .then(CommandManager.literal("palace")
                                            .executes(c ->
                                                    setMode(c.getSource(), "palace")
                                            ))

                                    .then(CommandManager.literal("adventure")
                                            .executes(c ->
                                                    setMode(c.getSource(), "adventure")
                                            ))

                                    .then(CommandManager.literal("return")
                                            .executes(c ->
                                                    setMode(c.getSource(), "return")
                                            ))

                                    .then(CommandManager.literal("stop")
                                            .executes(c ->
                                                    setMode(c.getSource(), "stop")
                                            ))

                                    .then(CommandManager.literal("stronghold")
                                            .executes(c ->
                                                    locateStronghold(c.getSource())
                                            ))

                                    .then(CommandManager.literal("end")
                                            .executes(c ->
                                                    locateEndCity(c.getSource())
                                            ))

                                    .then(CommandManager.literal("elytra")
                                            .executes(c ->
                                                    locateEndCity(c.getSource())
                                            ))

                                    .then(CommandManager.literal("nether")
                                            .executes(c ->
                                                    netherInfo(c.getSource())
                                            ))

                                    .then(CommandManager.literal("remove")
                                            .executes(c ->
                                                    remove(c.getSource())
                                            ))
                    );
                }
        );

        ServerTickEvents.END_WORLD_TICK.register(
                AICompanion::tick
        );
    }

    private static int spawn(ServerCommandSource source) {

        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            return 0;
        }

        ServerWorld world = source.getWorld();

        ZombieEntity ai = EntityType.ZOMBIE.create(
                world,
                SpawnReason.COMMAND
        );

        if (ai == null) {
            return 0;
        }

        ai.refreshPositionAndAngles(
                player.getX() + 2,
                player.getY(),
                player.getZ() + 2,
                player.getYaw(),
                0
        );

        ai.setCustomName(
                Text.literal("§bAI Companion")
        );

        ai.setCustomNameVisible(true);
        ai.setPersistent();

        ai.equipStack(
                EquipmentSlot.MAINHAND,
                new ItemStack(Items.IRON_SWORD)
        );

        ai.equipStack(
                EquipmentSlot.HEAD,
                new ItemStack(Items.IRON_HELMET)
        );

        ai.equipStack(
                EquipmentSlot.CHEST,
                new ItemStack(Items.IRON_CHESTPLATE)
        );

        ai.equipStack(
                EquipmentSlot.LEGS,
                new ItemStack(Items.IRON_LEGGINGS)
        );

        ai.equipStack(
                EquipmentSlot.FEET,
                new ItemStack(Items.IRON_BOOTS)
        );

        world.spawnEntity(ai);

        UUID id = ai.getUuid();

        OWNERS.put(id, player.getUuid());
        MODES.put(id, "follow");
        INVENTORIES.put(id, new SimpleInventory(36));

        source.sendFeedback(
                () -> Text.literal("§a[AI] Companion spawned!"),
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

        ZombieEntity ai = findCompanion(player);

        if (ai == null) {

            source.sendError(
                    Text.literal("§cFirst use /ai spawn")
            );

            return 0;
        }

        MODES.put(ai.getUuid(), mode);

        source.sendFeedback(
                () -> Text.literal(
                        "§a[AI] Mode: §f" + mode
                ),
                false
        );

        return 1;
    }

    private static void tick(ServerWorld world) {

        for (ZombieEntity ai :
                world.getEntitiesByType(
                        EntityType.ZOMBIE,
                        e ->
                                e.hasCustomName()
                                        &&
                                e.getCustomName() != null
                                        &&
                                e.getCustomName()
                                        .getString()
                                        .contains("AI Companion")
                )
        ) {

            UUID id = ai.getUuid();

            String mode = MODES.getOrDefault(
                    id,
                    "stop"
            );

            ServerPlayerEntity player = getOwner(ai);

            if (player == null) {
                continue;
            }

            if (ai.getTarget() == player) {
                ai.setTarget(null);
            }

            switch (mode) {

                case "follow":
                    follow(ai, player);
                    break;

                case "protect":
                    protect(ai, player);
                    break;

                case "mine":
                    mine(ai, player);
                    break;

                case "gather":
                    gather(ai, player);
                    break;

                case "house":
                    house(ai, player);
                    break;

                case "castle":
                    castle(ai, player);
                    break;

                case "palace":
                    palace(ai, player);
                    break;

                case "return":
                    returnItems(ai, player);
                    break;

                case "adventure":
                    adventure(ai, player);
                    break;

                case "stop":
                default:
                    ai.setTarget(null);
                    ai.getNavigation().stop();
                    break;
            }
        }
    }

    private static void follow(
            ZombieEntity ai,
            ServerPlayerEntity player
    ) {

        if (ai.distanceTo(player) > 4) {

            ai.getNavigation()
                    .startMovingTo(player, 1.15);
        }

        if (ai.distanceTo(player) > 30) {

            ai.requestTeleport(
                    player.getX() + 2,
                    player.getY(),
                    player.getZ() + 2
            );
        }
    }

    private static void protect(
            ZombieEntity ai,
            ServerPlayerEntity player
    ) {

        if (ai.distanceTo(player) > 10) {

            follow(ai, player);
            return;
        }

        Entity closest = null;
        double best = Double.MAX_VALUE;

        for (Entity entity :
                player.getEntityWorld()
                        .getOtherEntities(
                                player,
                                player.getBoundingBox().expand(12),
                                e -> e instanceof HostileEntity
                        )
        ) {

            if (entity == ai) {
                continue;
            }

            double distance = ai.distanceTo(entity);

            if (distance < best) {
                best = distance;
                closest = entity;
            }
        }

        if (closest instanceof LivingEntity enemy) {

            ai.setTarget(enemy);

            ai.getNavigation()
                    .startMovingTo(enemy, 1.3);

        } else {

            ai.setTarget(null);
            follow(ai, player);
        }
    }

    private static void mine(
            ZombieEntity ai,
            ServerPlayerEntity player
    ) {

        ServerWorld world =
                (ServerWorld) ai.getEntityWorld();

        BlockPos target =
                findOre(world, ai.getBlockPos());

        if (target == null) {

            gather(ai, player);
            return;
        }

        if (ai.getBlockPos()
                .getSquaredDistance(target) > 6) {

            ai.getNavigation()
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
                getOreItem(state);

        if (item.isEmpty()) {
            return;
        }

        SimpleInventory inv =
                INVENTORIES.get(ai.getUuid());

        if (inv == null) {
            return;
        }

        if (hasInventorySpace(inv)) {

            inv.addStack(item);

            world.breakBlock(
                    target,
                    false,
                    ai
            );
        }
    }

    private static BlockPos findOre(
            ServerWorld world,
            BlockPos center
    ) {

        BlockPos best = null;
        double distance = Double.MAX_VALUE;

        int radius = 12;

        for (int x = -radius; x <= radius; x++) {

            for (int y = -radius; y <= radius; y++) {

                for (int z = -radius; z <= radius; z++) {

                    BlockPos pos =
                            center.add(x, y, z);

                    BlockState state =
                            world.getBlockState(pos);

                    if (!isOre(state)) {
                        continue;
                    }

                    double d =
                            center.getSquaredDistance(pos);

                    if (d < distance) {
                        distance = d;
                        best = pos;
                    }
                }
            }
        }

        return best;
    }

    private static boolean isOre(
            BlockState state
    ) {

        return state.isOf(Blocks.IRON_ORE)
                || state.isOf(Blocks.DEEPSLATE_IRON_ORE)
                || state.isOf(Blocks.GOLD_ORE)
                || state.isOf(Blocks.DEEPSLATE_GOLD_ORE)
                || state.isOf(Blocks.DIAMOND_ORE)
                || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)
                || state.isOf(Blocks.COAL_ORE)
                || state.isOf(Blocks.DEEPSLATE_COAL_ORE)
                || state.isOf(Blocks.COPPER_ORE)
                || state.isOf(Blocks.DEEPSLATE_COPPER_ORE);
    }

    private static ItemStack getOreItem(
            BlockState state
    ) {

        if (state.isOf(Blocks.IRON_ORE)
                || state.isOf(Blocks.DEEPSLATE_IRON_ORE)) {

            return new ItemStack(Items.RAW_IRON);
        }

        if (state.isOf(Blocks.GOLD_ORE)
                || state.isOf(Blocks.DEEPSLATE_GOLD_ORE)) {

            return new ItemStack(Items.RAW_GOLD);
        }

        if (state.isOf(Blocks.DIAMOND_ORE)
                || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)) {

            return new ItemStack(Items.DIAMOND);
        }

        if (state.isOf(Blocks.COAL_ORE)
                || state.isOf(Blocks.DEEPSLATE_COAL_ORE)) {

            return new ItemStack(Items.COAL);
        }

        if (state.isOf(Blocks.COPPER_ORE)
                || state.isOf(Blocks.DEEPSLATE_COPPER_ORE)) {

            return new ItemStack(Items.RAW_COPPER);
        }

        return ItemStack.EMPTY;
    }

    private static void gather(
            ZombieEntity ai,
            ServerPlayerEntity player
    ) {

        ServerWorld world =
                (ServerWorld) ai.getEntityWorld();

        BlockPos target =
                findResource(world, ai.getBlockPos());

        if (target == null) {

            follow(ai, player);
            return;
        }

        if (ai.getBlockPos()
                .getSquaredDistance(target) > 6) {

            ai.getNavigation()
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
                getResourceItem(state);

        if (item.isEmpty()) {
            return;
        }

        SimpleInventory inv =
                INVENTORIES.get(ai.getUuid());

        if (inv != null && hasInventorySpace(inv)) {

            inv.addStack(item);

            world.breakBlock(
                    target,
                    false,
                    ai
            );
        }
    }

    private static BlockPos findResource(
            ServerWorld world,
            BlockPos center
    ) {

        int radius = 8;

        for (int x = -radius; x <= radius; x++) {

            for (int y = -5; y <= 5; y++) {

                for (int z = -radius; z <= radius; z++) {

                    BlockPos pos =
                            center.add(x, y, z);

                    BlockState state =
                            world.getBlockState(pos);

                    if (
                            state.isOf(Blocks.OAK_LOG)
                                    ||
                            state.isOf(Blocks.BIRCH_LOG)
                                    ||
                            state.isOf(Blocks.SPRUCE_LOG)
                                    ||
                            state.isOf(Blocks.STONE)
                                    ||
                            state.isOf(Blocks.COBBLESTONE)
                                    ||
                            state.isOf(Blocks.COAL_ORE)
                    ) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }

    private static ItemStack getResourceItem(
            BlockState state
    ) {

        if (state.isOf(Blocks.OAK_LOG)) {
            return new ItemStack(Items.OAK_LOG);
        }

        if (state.isOf(Blocks.BIRCH_LOG)) {
            return new ItemStack(Items.BIRCH_LOG);
        }

        if (state.isOf(Blocks.SPRUCE_LOG)) {
            return new ItemStack(Items.SPRUCE_LOG);
        }

        if (state.isOf(Blocks.STONE)
                || state.isOf(Blocks.COBBLESTONE)) {

            return new ItemStack(Items.COBBLESTONE);
        }

        if (state.isOf(Blocks.COAL_ORE)) {
            return new ItemStack(Items.COAL);
        }

        return ItemStack.EMPTY;
    }

    private static boolean hasInventorySpace(
            SimpleInventory inv
    ) {

        for (int i = 0; i < inv.size(); i++) {

            if (inv.getStack(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private static void house(
            ZombieEntity ai,
            ServerPlayerEntity player
    ) {

        ServerWorld world =
                (ServerWorld) ai.getEntityWorld();

        BlockPos base =
                player.getBlockPos().add(6, 0, 6);

        int size = 9;

        for (int x = 0; x < size; x++) {

            for (int z = 0; z < size; z++) {

                set(
                        world,
                        base.add(x, 0, z),
                        Blocks.OAK_PLANKS
                );
            }
        }

        for (int y = 1; y <= 4; y++) {

            for (int x = 0; x < size; x++) {

                set(
                        world,
                        base.add(x, y, 0),
                        Blocks.OAK_PLANKS
                );

                set(
                        world,
                        base.add(x, y, size - 1),
                        Blocks.OAK_PLANKS
                );
            }

            for (int z = 0; z < size; z++) {

                set(
                        world,
                        base.add(0, y, z),
                        Blocks.OAK_PLANKS
                );

                set(
                        world,
                        base.add(size - 1, y, z),
                        Blocks.OAK_PLANKS
                );
            }
        }

        set(
                world,
                base.add(4, 1, 0),
                Blocks.AIR
        );

        set(
                world,
                base.add(4, 2, 0),
                Blocks.AIR
        );

        set(
                world,
                base.add(0, 2, 3),
                Blocks.GLASS
        );

        set(
                world,
                base.add(8, 2, 3),
                Blocks.GLASS
        );

        for (int x = -1; x <= 9; x++) {

            for (int z = -1; z <= 9; z++) {

                set(
                        world,
                        base.add(x, 5, z),
                        Blocks.SPRUCE_PLANKS
                );
            }
        }

        MODES.put(ai.getUuid(), "follow");
    }

    private static void castle(
            ZombieEntity ai,
            ServerPlayerEntity player
    ) {

        ServerWorld world =
                (ServerWorld) ai.getEntityWorld();

        BlockPos base =
                player.getBlockPos().add(15, 0, 15);

        int size = 21;

        for (int x = 0; x < size; x++) {

            for (int z = 0; z < size; z++) {

                set(
                        world,
                        base.add(x, 0, z),
                        Blocks.STONE_BRICKS
                );
            }
        }

        for (int y = 1; y <= 6; y++) {

            for (int x = 0; x < size; x++) {

                set(
                        world,
                        base.add(x, y, 0),
                        Blocks.STONE_BRICKS
                );

                set(
                        world,
                        base.add(x, y, size - 1),
                        Blocks.STONE_BRICKS
                );
            }

            for (int z = 0; z < size; z++) {

                set(
                        world,
                        base.add(0, y, z),
                        Blocks.STONE_BRICKS
                );

                set(
                        world,
                        base.add(size - 1, y, z),
                        Blocks.STONE_BRICKS
                );
            }
        }

        for (int y = 1; y <= 3; y++) {

            set(
                    world,
                    base.add(10, y, 0),
                    Blocks.AIR
            );

            set(
                    world,
                    base.add(11, y, 0),
                    Blocks.AIR
            );
        }

        tower(world, base);
        tower(world, base.add(17, 0, 0));
        tower(world, base.add(0, 0, 17));
        tower(world, base.add(17, 0, 17));

        MODES.put(ai.getUuid(), "follow");
    }

    private static void tower(
            ServerWorld world,
            BlockPos base
    ) {

        for (int y = 0; y <= 9; y++) {

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

                        set(
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

                set(
                        world,
                        base.add(x, 10, z),
                        Blocks.DARK_OAK_PLANKS
                );
            }
        }
    }

    private static void palace(
            ZombieEntity ai,
            ServerPlayerEntity player
    ) {

        ServerWorld world =
                (ServerWorld) ai.getEntityWorld();

        BlockPos base =
                player.getBlockPos().add(25, 0, 25);

        int width = 31;
        int depth = 25;

        for (int x = 0; x < width; x++) {

            for (int z = 0; z < depth; z++) {

                set(
                        world,
                        base.add(x, 0, z),
                        Blocks.POLISHED_DEEPSLATE
                );
            }
        }

        for (int y = 1; y <= 8; y++) {

            for (int x = 0; x < width; x++) {

                set(
                        world,
                        base.add(x, y, 0),
                        Blocks.QUARTZ_BLOCK
                );

                set(
                        world,
                        base.add(x, y, depth - 1),
                        Blocks.QUARTZ_BLOCK
                );
            }

            for (int z = 0; z < depth; z++) {

                set(
                        world,
                        base.add(0, y, z),
                        Blocks.QUARTZ_BLOCK
                );

                set(
                        world,
                        base.add(width - 1, y, z),
                        Blocks.QUARTZ_BLOCK
                );
            }
        }

        for (int y = 1; y <= 4; y++) {

            set(
                    world,
                    base.add(15, y, 0),
                    Blocks.AIR
            );
        }

        for (int x = 0; x < width; x++) {

            for (int z = 0; z < depth; z++) {

                set(
                        world,
                        base.add(x, 9, z),
                        Blocks.QUARTZ_BLOCK
                );
            }
        }

        tower(world, base);
        tower(world, base.add(width - 4, 0, 0));
        tower(world, base.add(0, 0, depth - 4));
        tower(
                world,
                base.add(width - 4, 0, depth - 4)
        );

        MODES.put(ai.getUuid(), "follow");
    }

    private static void set(
            ServerWorld world,
            BlockPos pos,
            Block block
    ) {

        if (
                block == Blocks.AIR
                        ||
                world.getBlockState(pos).isAir()
        ) {

            world.setBlockState(
                    pos,
                    block.getDefaultState()
            );
        }
    }

    private static void returnItems(
            ZombieEntity ai,
            ServerPlayerEntity player
    ) {

        if (ai.distanceTo(player) > 4) {

            follow(ai, player);
            return;
        }

        SimpleInventory inv =
                INVENTORIES.get(ai.getUuid());

        if (inv == null) {
            return;
        }

        for (int i = 0; i < inv.size(); i++) {

            ItemStack stack = inv.getStack(i);

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

            } else {

                inv.setStack(
                        i,
                        stack
                );
            }
        }

        MODES.put(ai.getUuid(), "follow");
    }

    private static void adventure(
            ZombieEntity ai,
            ServerPlayerEntity player
    ) {

        protect(ai, player);
    }

    private static int locateStronghold(
            ServerCommandSource source
    ) {

        ServerPlayerEntity player =
                source.getPlayer();

        if (player == null) {
            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "§a[AI] Stronghold search requested."
                ),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                        "§7Use Minecraft's structure locator/"
                                + "Eye of Ender to locate the actual stronghold."
                ),
                false
        );

        return 1;
    }

    private static int locateEndCity(
            ServerCommandSource source
    ) {

        ServerPlayerEntity player =
                source.getPlayer();

        if (player == null) {
            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "§d[AI] End City / Elytra hunt requested."
                ),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                        "§7The companion is ready for the End adventure system."
                ),
                false
        );

        return 1;
    }

    private static int netherInfo(
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
                server.getWorld(World.NETHER);

        if (nether == null) {

            source.sendError(
                    Text.literal(
                            "§cNether world is unavailable."
                    )
            );

            return 0;
        }

        source.sendFeedback(
                () -> Text.literal(
                        "§6[AI] Nether exploration system activated."
                ),
                false
        );

        source.sendFeedback(
                () -> Text.literal(
                        "§7Nether world detected."
                ),
                false
        );

        return 1;
    }

    private static ServerPlayerEntity getOwner(
            ZombieEntity ai
    ) {

        UUID owner =
                OWNERS.get(ai.getUuid());

        if (owner == null) {
            return null;
        }

        ServerWorld world =
                (ServerWorld) ai.getEntityWorld();

        return world.getServer()
                .getPlayerManager()
                .getPlayer(owner);
    }

    private static ZombieEntity findCompanion(
            ServerPlayerEntity player
    ) {

        ServerWorld world =
                (ServerWorld) player.getEntityWorld();

        for (ZombieEntity ai :
                world.getEntitiesByType(
                        EntityType.ZOMBIE,
                        e ->
                                e.hasCustomName()
                                        &&
                                e.getCustomName() != null
                                        &&
                                e.getCustomName()
                                        .getString()
                                        .contains("AI Companion")
                )
        ) {

            UUID owner =
                    OWNERS.get(ai.getUuid());

            if (
                    owner != null
                            &&
                    owner.equals(player.getUuid())
            ) {

                return ai;
            }
        }

        return null;
    }

    private static int remove(
            ServerCommandSource source
    ) {

        ServerPlayerEntity player =
                source.getPlayer();

        if (player == null) {
            return 0;
        }

        ZombieEntity ai =
                findCompanion(player);

        if (ai == null) {

            source.sendError(
                    Text.literal(
                            "§cAI Companion not found."
                    )
            );

            return 0;
        }

        UUID id = ai.getUuid();

        OWNERS.remove(id);
        MODES.remove(id);
        INVENTORIES.remove(id);

        ai.discard();

        source.sendFeedback(
                () -> Text.literal(
                        "§c[AI] Companion removed."
                ),
                false
        );

        return 1;
    }
}
