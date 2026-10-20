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
public class RatIglooModel extends Model.Simple {


	public RatIglooModel(ModelPart root) {
		super(root, RenderTypes::entityCutout);
	}

	public static LayerDefinition create() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition partdefinition = mesh.getRoot();

		partdefinition.addOrReplaceChild("igloo", CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-5.0F, -8.0F, -5.0F, 10.0F, 8.0F, 10.0F)
						.texOffs(30, 0)
						.addBox(-2.5F, -5.0F, -8.0F, 5.0F, 5.0F, 3.0F)
						.texOffs(0, 19)
						.addBox(-5.0F, -9.0F, -5.0F, 10.0F, 1.0F, 10.0F),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(mesh, 64, 32);
	}


}
