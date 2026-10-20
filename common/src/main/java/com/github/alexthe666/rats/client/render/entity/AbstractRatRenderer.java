package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.client.model.entity.AbstractRatModel;
import com.github.alexthe666.rats.client.model.entity.BiplaneModel;
import com.github.alexthe666.rats.client.model.entity.RattlingGunModel;
import com.github.alexthe666.rats.client.render.entity.layer.PartyHatLayer;
import com.github.alexthe666.rats.client.render.entity.layer.RatHeldItemLayer;
import com.github.alexthe666.rats.client.render.entity.layer.RatHelmetLayer;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.entity.misc.RattlingGun;
import com.github.alexthe666.rats.server.entity.monster.boss.RatBaronPlane;
import com.github.alexthe666.rats.server.entity.mount.RatBiplaneMount;
import com.github.alexthe666.rats.server.entity.rat.AbstractRat;
import com.github.alexthe666.rats.server.items.HatItem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.rampart.client.render.RampartMobRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public abstract class AbstractRatRenderer<T extends AbstractRat, M extends AbstractRatModel<T>> extends RampartMobRenderer<T, RatRenderState, M> {
	/** Models posed only to find where a riding rat sits (the drawn models are posed when they are drawn) */
	private static final RattlingGunModel<RattlingGun> GUN_POSE = new RattlingGunModel<>();
	@SuppressWarnings("rawtypes")
	private static final BiplaneModel PLANE_POSE = new BiplaneModel<>();

	public AbstractRatRenderer(EntityRendererProvider.Context context, M model) {
		super(context, model, 0.15F);
		this.addLayer(new RatHelmetLayer<>(this, context));
		this.addLayer(new RatHeldItemLayer<>(this, context));
		this.addLayer(new PartyHatLayer<>(this));
	}

	@Override
	public RatRenderState createRenderState() {
		return new RatRenderState();
	}

	@Override
	public boolean shouldRender(T rat, Frustum camera, double camX, double camY, double camZ) {
		if (rat.isPassenger() && rat.getVehicle() != null && !rat.getVehicle().getPassengers().isEmpty() && rat.getVehicle().getPassengers().get(0) == rat && rat.getVehicle() instanceof LivingEntity living) {
			if (living.getItemBySlot(EquipmentSlot.HEAD).is(RatsItemRegistry.CHEF_TOQUE.get())) {
				return false;
			}
		}
		return super.shouldRender(rat, camera, camX, camY, camZ);
	}

	@Override
	public void extractRenderState(T rat, RatRenderState state, float partialTicks) {
		super.extractRenderState(rat, state, partialTicks);
		state.texture = this.getTexture(rat);
		// rats dead in a trap lie as they are (the original's flip degrees of 0)
		if (rat.isDeadInTrap()) {
			state.deathTime = 0.0F;
		}
		state.riderTransform = riderTransform(rat, partialTicks);
	}

	/** The texture of the rat (the state's texture) */
	protected Identifier getTexture(T rat) {
		return rat.getColorVariant().getTexture();
	}

	@Override
	public Identifier getTextureLocation(RatRenderState state) {
		return state.texture;
	}

	@Override
	protected void scale(RatRenderState state, PoseStack stack) {
		stack.scale(0.6F, 0.6F, 0.6F);
		if (state.riderTransform != null) {
			stack.mulPose(state.riderTransform);
		}
	}

	/** The original moved a riding rat with the vehicle's model parts; those poses are computed here from the vehicle */
	@SuppressWarnings("unchecked")
	private static Matrix4f riderTransform(AbstractRat rat, float partialTicks) {
		Entity vehicle = rat.getVehicle();
		if (vehicle == null || vehicle.getPassengers().isEmpty() || vehicle.getPassengers().get(0) != rat) {
			return null;
		}
		PoseStack pose = new PoseStack();
		if (vehicle instanceof Player player) {
			// the player model's head part (HumanoidModel.setupAnim): the head turn and pitch, lower when crouching
			float headYaw = Mth.wrapDegrees(Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot) - Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot));
			float headPitch = player.getXRot(partialTicks);
			pose.translate(0.0F, player.getPose() == Pose.CROUCHING ? 4.2F / 16.0F : 0.0F, 0.0F);
			pose.mulPose(new Quaternionf().rotationZYX(0.0F, headYaw * Mth.DEG_TO_RAD, headPitch * Mth.DEG_TO_RAD));
			pose.translate(0.0F, -0.7F, 0.25F);
			if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof HatItem hatItem) {
				pose.translate(0.0F, hatItem.getRatOffsetOnHead(), 0.0F);
			}
		} else if (vehicle instanceof RattlingGun) {
			GUN_POSE.resetToDefaultPose();
			GUN_POSE.pivot.translateRotate(pose);
		} else if (vehicle instanceof RatBaronPlane || vehicle instanceof RatBiplaneMount) {
			PLANE_POSE.setupAnim(vehicle, 0.0F, 0.0F, vehicle.tickCount + partialTicks, partialTicks, 0.0F);
			pose.translate(0.0F, -0.1F, 0.45F);
			PLANE_POSE.body1.translateRotate(pose);
		} else {
			return null;
		}
		return new Matrix4f(pose.last().pose());
	}
}
