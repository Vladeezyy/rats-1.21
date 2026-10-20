package com.github.alexthe666.rats.client.render;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.render.block.AutoCurdlerRenderer;
import com.github.alexthe666.rats.client.render.block.RatTrapRenderer;
import com.github.alexthe666.rats.client.render.block.RatlanteanAutomatonHeadRenderer;
import com.github.alexthe666.rats.client.render.block.RatlantisPortalRenderer;
import com.github.alexthe666.rats.client.render.block.TrashCanRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * The block items drawn like their block entity (1.21.1's block entity item renderer): the item definition
 * ({@code assets/rats/items/*.json}) uses the special model {@code rats:block_entity} with the kind of block.
 */
public class RatsItemSpecialRenderer implements NoDataSpecialModelRenderer {
	public static final Identifier ID = Identifier.fromNamespaceAndPath(RatsMod.MODID, "block_entity");
	private final Kind kind;

	public RatsItemSpecialRenderer(Kind kind) {
		this.kind = kind;
	}

	@Override
	public void submit(PoseStack stack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
		switch (this.kind) {
			case AUTO_CURDLER -> AutoCurdlerRenderer.submitItem(stack, collector, light, overlay);
			case TRASH_CAN -> TrashCanRenderer.submitItem(stack, collector, light, overlay);
			case RAT_TRAP -> RatTrapRenderer.submitItem(stack, collector, light, overlay);
			case RAT_HEAD -> {
				Minecraft minecraft = Minecraft.getInstance();
				float age = (minecraft.player != null ? minecraft.player.tickCount : 0) + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
				RatlanteanAutomatonHeadRenderer.submitHead(0, age, stack, collector, light, overlay);
			}
			case RATLANTIS_PORTAL -> RatlantisPortalRenderer.submitItem(stack, collector);
		}
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		// the block's cube
		for (int i = 0; i < 8; i++) {
			output.accept(new Vector3f(i & 1, (i >> 1) & 1, (i >> 2) & 1));
		}
	}

	public enum Kind implements StringRepresentable {
		AUTO_CURDLER("auto_curdler"),
		TRASH_CAN("trash_can"),
		RAT_TRAP("rat_trap"),
		RAT_HEAD("marbled_cheese_rat_head"),
		RATLANTIS_PORTAL("ratlantis_portal");

		public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);
		private final String name;

		Kind(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	public record Unbaked(Kind kind) implements NoDataSpecialModelRenderer.Unbaked {
		public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Kind.CODEC.fieldOf("block").forGetter(Unbaked::kind)).apply(i, Unbaked::new));

		@Override
		public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
			return new RatsItemSpecialRenderer(this.kind);
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
