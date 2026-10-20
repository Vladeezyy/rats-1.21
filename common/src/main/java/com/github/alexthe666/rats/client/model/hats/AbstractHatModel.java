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
		}
	}

	private static void pose(ModelPart part, Rotations rotations) {
		part.xRot = 0.017453292F * rotations.x();
		part.yRot = 0.017453292F * rotations.y();
		part.zRot = 0.017453292F * rotations.z();
	}
}
