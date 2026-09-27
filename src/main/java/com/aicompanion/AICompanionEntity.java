
package com.aicompanion;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class AICompanionEntity extends PathAwareEntity {

    private UUID ownerUuid;

    public AICompanionEntity(EntityType<? extends AICompanionEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAICompanionAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 40.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.ATTACK_DAMAGE, 8.0)
                .add(EntityAttributes.ARMOR, 8.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.2);
    }

    @Override
    protected void initGoals() {

        // Fight enemies
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.2, true));

        // Walk around when idle
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0));

        // Look at nearby players
        this.goalSelector.add(
                7,
                new LookAtEntityGoal(this, PlayerEntity.class, 8.0F)
        );

        // Look around
        this.goalSelector.add(8, new LookAroundGoal(this));

        // Revenge when attacked
        this.targetSelector.add(
                1,
                new RevengeGoal(this)
        );

        // Attack hostile mobs
        this.targetSelector.add(
                2,
                new ActiveTargetGoal<>(
                        this,
                        HostileEntity.class,
                        true
                )
        );
    }

    public void setOwner(PlayerEntity player) {
        this.ownerUuid = player.getUuid();
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public boolean isOwner(LivingEntity entity) {
        return ownerUuid != null
                && entity.getUuid().equals(ownerUuid);
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }
}
