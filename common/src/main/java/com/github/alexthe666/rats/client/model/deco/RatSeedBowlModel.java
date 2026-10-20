package com.github.alexthe666.rats.client.model.deco;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

/** A static decoration model (vanilla model parts) */
public class RatSeedBowlModel extends Model.Simple {


	public RatSeedBowlModel(ModelPart root) {
		super(root, RenderTypes::entityCutout);
	}

	public static LayerDefinition create() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition partdefinition = mesh.getRoot();

		partdefinition.addOrReplaceChild("bowl", CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-2.5F, -2.0F, -2.5F, 5.0F, 2.0F, 5.0F),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(mesh, 32, 16);
	}


}
