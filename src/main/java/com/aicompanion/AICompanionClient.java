package com.aicompanion;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class AICompanionClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(
                ModEntities.AI_COMPANION,
                AICompanionRenderer::new
        );
    }

    public static class AICompanionRenderer
            extends BipedEntityRenderer<
                    AICompanionEntity,
                    BipedEntityRenderState,
                    BipedEntityModel<BipedEntityRenderState>
                    > {

        private static final Identifier TEXTURE =
                Identifier.ofVanilla(
                        "textures/entity/player/wide/steve.png"
                );

        public AICompanionRenderer(
                EntityRendererFactory.Context context
        ) {

            super(
                    context,
                    new BipedEntityModel<>(
                            context.getPart(
                                    EntityModelLayers.PLAYER
                            )
                    ),
                    0.5F
            );
        }

        @Override
        public Identifier getTexture(
                BipedEntityRenderState state
        ) {
            return TEXTURE;
        }

        @Override
        public BipedEntityRenderState createRenderState() {
            return new BipedEntityRenderState();
        }
    }
}
