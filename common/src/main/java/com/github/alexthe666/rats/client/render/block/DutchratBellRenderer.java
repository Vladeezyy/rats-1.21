package com.github.alexthe666.rats.client.render.block;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.render.PosedModels;
import com.github.alexthe666.rats.client.render.RatsRenderType;
import com.github.alexthe666.rats.server.block.entity.DutchratBellBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/** The Dutchrat's bell: vanilla's bell body, swinging when rung, glowing */
public class DutchratBellRenderer extends StateBlockEntityRenderer<DutchratBellBlockEntity> {
	private static final RenderType TEXTURE = RatsRenderType.getGlowingTranslucent(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/block/dutchrat_bell.png"));
	private final ModelPart bellBody;

	public DutchratBellRenderer(BlockEntityRendererProvider.Context context) {
		this.bellBody = context.bakeLayer(ModelLayers.BELL).getChild("bell_body");
	}

	@Override
	protected void submit(DutchratBellBlockEntity bell, float partialTicks, PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		float f = (float) bell.ticks + partialTicks;
		float f1 = 0.0F;
		float f2 = 0.0F;
		if (bell.shaking) {
			float f3 = Mth.sin(f / (float) Math.PI) / (4.0F + f / 3.0F);
			if (bell.clickDirection == Direction.NORTH) {
				f1 = -f3;
			} else if (bell.clickDirection == Direction.SOUTH) {
				f1 = f3;
			} else if (bell.clickDirection == Direction.EAST) {
				f2 = -f3;
			} else if (bell.clickDirection == Direction.WEST) {
				f2 = f3;
			}
		}
		float xRot = f1;
		float zRot = f2;
		PosedModels.submit(collector, stack, TEXTURE, this.bellBody, () -> {
			this.bellBody.xRot = xRot;
			this.bellBody.zRot = zRot;
		}, light, overlay, ARGB.colorFromFloat(0.5F, 1.0F, 1.0F, 1.0F));
	}
}
