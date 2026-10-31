package com.github.alexthe666.rats.client.model.hats;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Rotations;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

/** A hat (or armor) model posed from the wearer's render state; armor stands use their own part poses */
public class AbstractHatModel extends HumanoidModel<HumanoidRenderState> {

	public AbstractHatModel(ModelPart root) {
		super(root);
	}

	public AbstractHatModel(ModelPart root, Function<Identifier, RenderType> function) {
		super(root, function);
	}

	@Override
	public void setupAnim(HumanoidRenderState state) {
		if (state instanceof ArmorStandRenderState stand) {
			this.resetPose();
			pose(this.head, stand.headPose);
			this.head.setPos(0.0F, 1.0F, 0.0F);
			pose(this.body, stand.bodyPose);
			pose(this.leftArm, stand.leftArmPose);
			pose(this.rightArm, stand.rightArmPose);
			pose(this.leftLeg, stand.leftLegPose);
			this.leftLeg.setPos(1.9F, 11.0F, 0.0F);
			pose(this.rightLeg, stand.rightLegPose);
			this.rightLeg.setPos(-1.9F, 11.0F, 0.0F);
		} else {
			super.setupAnim(state);
			// 1.21.1 copied the head pose of the rat's (never animated) armor model over the hat's: the offsets and
			// tilts the hat models have on their head part (the aviator hat's 16, 32, 16) never showed. 26.x starts
			// from the model's own pose, so they are taken out here; setupAnim's changes (crouching) stay
			net.minecraft.client.model.geom.PartPose rest = this.head.getInitialPose();
			this.head.x -= rest.x();
			this.head.y -= rest.y();
			this.head.z -= rest.z();
			this.head.zRot -= rest.zRot();
		}
		if (this.onlyHead()) {
			// a hat is a head piece: the body, arms and legs of the humanoid mesh under it are not drawn (1.21.1's
			// armor layer hid them; without it they showed as loose boxes under the wearer)
			this.body.visible = false;
			this.rightArm.visible = false;
			this.leftArm.visible = false;
			this.rightLeg.visible = false;
			this.leftLeg.visible = false;
		}
	}

	/** Whether only the head (and its hat) of the model is drawn */
	protected boolean onlyHead() {
		return true;
	}

	private static void pose(ModelPart part, Rotations rotations) {
		part.xRot = 0.017453292F * rotations.x();
		part.yRot = 0.017453292F * rotations.y();
		part.zRot = 0.017453292F * rotations.z();
	}
}
