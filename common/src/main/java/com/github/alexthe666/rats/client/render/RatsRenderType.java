package com.github.alexthe666.rats.client.render;

import net.minecraft.client.renderer.BindGroupLayouts;
import com.mojang.blaze3d.PrimitiveTopology;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.render.block.RatlantisPortalRenderer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * The mod's render types, on 26.x render pipelines: the colored and pride glints (vanilla's glint pipeline with the
 * mod's textures), the glowing translucent layer (the energy swirl shader with translucent blending) and the Ratlantis
 * portal (the mod's own shader).
 */
public final class RatsRenderType {

	/** The Ratlantis portal: two projected layers, assets/rats/shaders/core/rendertype_ratlantis_portal.* */
	private static final RenderPipeline RATLANTIS_PORTAL_PIPELINE = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
		.withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
		.withLocation(id("pipeline/ratlantis_portal"))
		.withVertexShader(id("core/rendertype_ratlantis_portal"))
		.withFragmentShader(id("core/rendertype_ratlantis_portal"))
		.withShaderDefine("PORTAL_LAYERS", 5)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.build();
	/** Vanilla's energy swirl (emissive, no shading) with translucent instead of additive blending */
	private static final RenderPipeline GLOWING_TRANSLUCENT_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
		.withLocation(id("pipeline/glowing_translucent"))
		.withVertexShader("core/entity")
		.withFragmentShader("core/entity")
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.withShaderDefine("EMISSIVE")
		.withShaderDefine("NO_OVERLAY")
		.withShaderDefine("NO_CARDINAL_LIGHTING")
		.withShaderDefine("APPLY_TEXTURE_MATRIX")
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withCull(false)
		.withVertexBinding(0, DefaultVertexFormat.ENTITY)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.build();

	/** The pride glints scroll up slowly (the original's texture matrix) */
	private static final TextureTransform RAINBOW_GLINT_TEXTURING = new TextureTransform("rats_rainbow_glint_texturing", () -> {
		long millis = Util.getMillis() * 8L;
		float offset = (float) (millis % 10000L) / 10000.0F;
		return new Matrix4f().translation(0.0F, offset, 0.0F).scale(0.16F);
	});

	private static final RenderType RATLANTIS_PORTAL = RenderType.create("rats_ratlantis_portal", RenderSetup.builder(RATLANTIS_PORTAL_PIPELINE)
		.withTexture("Sampler0", RatlantisPortalRenderer.PORTAL_BG)
		.withTexture("Sampler1", RatlantisPortalRenderer.PORTAL_FG)
		.createRenderSetup());

	private static final RenderType ACE_GLINT = prideGlint("ace");
	private static final RenderType AGENDER_GLINT = prideGlint("agender");
	private static final RenderType ARO_GLINT = prideGlint("aro");
	private static final RenderType BI_GLINT = prideGlint("bi");
	private static final RenderType ENBY_GLINT = prideGlint("enby");
	private static final RenderType GAY_GLINT = prideGlint("gay");
	private static final RenderType GENDERFLUID_GLINT = prideGlint("genderfluid");
	private static final RenderType LESBIAN_GLINT = prideGlint("lesbian");
	private static final RenderType PAN_GLINT = prideGlint("pan");
	private static final RenderType RAINBOW_GLINT = prideGlint("rainbow");
	private static final RenderType TRANS_GLINT = prideGlint("trans");

	private static final RenderType PISS_GLINT = prideGlint("piss");
	private static final RenderType UNPLEASANT_GLINT = prideGlint("unpleasant");

	private static final RenderType GREEN_ENTITY_GLINT = entityGlint("green");
	private static final RenderType YELLOW_ENTITY_GLINT = entityGlint("yellow");
	private static final RenderType WHITE_ENTITY_GLINT = entityGlint("white");
	private static final RenderType GOLD_ENTITY_GLINT = entityGlint("gold");

	private static final Function<Identifier, RenderType> GLOWING_TRANSLUCENT = Util.memoize(texture -> RenderType.create("rats_glowing_translucent",
		RenderSetup.builder(GLOWING_TRANSLUCENT_PIPELINE).withTexture("Sampler0", texture).useLightmap().useOverlay().sortOnUpload().createRenderSetup()));

	private RatsRenderType() {
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(RatsMod.MODID, path);
	}

	private static RenderType prideGlint(String name) {
		return RenderType.create("rats_" + name + "_glint", RenderSetup.builder(RenderPipelines.GLINT)
			.withTexture("Sampler0", id("textures/misc/special_dyes/" + name + "_glint.png"))
			.setTextureTransform(RAINBOW_GLINT_TEXTURING)
			.createRenderSetup());
	}

	private static RenderType entityGlint(String color) {
		return RenderType.create("rats_" + color + "_glint", RenderSetup.builder(RenderPipelines.GLINT)
			.withTexture("Sampler0", id("textures/misc/" + color + "_glint.png"))
			.setTextureTransform(TextureTransform.ENTITY_GLINT_TEXTURING)
			.createRenderSetup());
	}

	public static RenderType getYellowGlint() {
		return YELLOW_ENTITY_GLINT;
	}

	public static RenderType getGreenGlint() {
		return GREEN_ENTITY_GLINT;
	}

	public static RenderType getWhiteGlint() {
		return WHITE_ENTITY_GLINT;
	}

	public static RenderType getGoldGlint() {
		return GOLD_ENTITY_GLINT;
	}

	public static RenderType getRatlantisPortal() {
		return RATLANTIS_PORTAL;
	}

	public static RenderType getRainbowGlint() {
		return RAINBOW_GLINT;
	}

	/** Emissive and translucent (one render type per texture, cached) */
	public static RenderType getGlowingTranslucent(Identifier location) {
		return GLOWING_TRANSLUCENT.apply(location);
	}

	public enum GlintType {
		AGENDER(AGENDER_GLINT, true, "agender"),
		AROMANTIC(ARO_GLINT, true, "aromantic", "aro"),
		ASEXUAL(ACE_GLINT, true, "asexual", "ace"),
		BISEXUAL(BI_GLINT, true, "bisexual", "bi"),
		GAY(GAY_GLINT, true, "gay", "mlm"),
		GENDERFLUID(GENDERFLUID_GLINT, true, "genderfluid", "fluid"),
		NONBINARY(ENBY_GLINT, true, "non-binary", "nonbinary", "enby", "nb"),
		LESBIAN(LESBIAN_GLINT, true, "lesbian", "wlw"),
		PANSEXUAL(PAN_GLINT, true, "pansexual", "pan"),
		TRANSGENDER(TRANS_GLINT, true, "transgender", "trans", "tratsgender"),

		PISS(PISS_GLINT, false, "piss"),
		UNPLEASANT(UNPLEASANT_GLINT, false, "unpleasant");

		private final RenderType type;
		private final boolean changesTexture;
		private final List<String> keywords;

		GlintType(RenderType type, boolean changesItemTex, String... matchingKeywords) {
			this.type = type;
			this.changesTexture = changesItemTex;
			this.keywords = Arrays.stream(matchingKeywords).toList();
		}

		public boolean changesItemTexture() {
			return this.changesTexture;
		}

		public RenderType getRenderType() {
			return this.type;
		}

		@Nullable
		public static RenderType getRenderTypeBasedOnKeyword(String word) {
			GlintType type = getGlintBasedOnKeyword(word);
			return type == null ? null : type.getRenderType();
		}

		@Nullable
		public static GlintType getGlintBasedOnKeyword(String word) {
			for (GlintType type : GlintType.values()) {
				for (String possibleWord : type.getKeywords()) {
					if (possibleWord.equalsIgnoreCase(word)) {
						return type;
					}
				}
			}
			return null;
		}

		public List<String> getKeywords() {
			return this.keywords;
		}
	}
}
