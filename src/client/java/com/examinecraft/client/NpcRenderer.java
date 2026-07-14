package com.examinecraft.client;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

import com.examinecraft.ExaMinecraft;
import com.examinecraft.npc.CommunityNpcEntity;

/**
 * Renders a community NPC with the player model and its profile's skin PNG
 * (standard 64x64 skin at textures/entity/npc/<id>.png - drop in real skins any time).
 */
public class NpcRenderer extends HumanoidMobRenderer<CommunityNpcEntity, AvatarRenderState, PlayerModel> {
	public NpcRenderer(EntityRendererProvider.Context context) {
		super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
		this.addLayer(new HumanoidArmorLayer<>(this,
				ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), part -> new PlayerModel(part, false)),
				context.getEquipmentRenderer()));
	}

	@Override
	public AvatarRenderState createRenderState() {
		return new AvatarRenderState();
	}

	@Override
	public void extractRenderState(CommunityNpcEntity entity, AvatarRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.skin = new PlayerSkin(
				new ClientAsset.ResourceTexture(ExaMinecraft.id("entity/npc/" + entity.getProfile().id())),
				null, null, PlayerModelType.WIDE, true);
	}

	@Override
	public Identifier getTextureLocation(AvatarRenderState state) {
		return state.skin.body().texturePath();
	}
}
