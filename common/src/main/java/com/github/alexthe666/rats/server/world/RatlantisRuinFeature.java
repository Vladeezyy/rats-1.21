package com.github.alexthe666.rats.server.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** 26.3: features are records with their own codec, the configuration (was RatlantisRuinConfiguration) is the record */
public record RatlantisRuinFeature(Map<Identifier, Float> ruins, Identifier defaultRuin, Holder<StructureProcessorList> processor) implements Feature {
	public static final MapCodec<RatlantisRuinFeature> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
							Codec.unboundedMap(Identifier.CODEC, Codec.FLOAT).fieldOf("ruins").forGetter(RatlantisRuinFeature::ruins),
							Identifier.CODEC.fieldOf("fallback_ruin").forGetter(RatlantisRuinFeature::defaultRuin),
							StructureProcessorType.LIST_CODEC.fieldOf("processor").forGetter(RatlantisRuinFeature::processor))
					.apply(instance, RatlantisRuinFeature::new));

	@Override
	public MapCodec<RatlantisRuinFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos blockpos) {
		Rotation rotation = Rotation.getRandom(random);
		RatlantisRuinFeature config = this;

		StructureTemplateManager structuretemplatemanager = level.getLevel().getServer().getStructureTemplateManager();
		StructureTemplate structuretemplate = null;

		//shuffle the map. This makes it so the last entries are actually used more often
		List<Identifier> shuffledList = new ArrayList<>(config.ruins().keySet());
		Collections.shuffle(shuffledList);

		for (Identifier entry : shuffledList) {
			if (random.nextFloat() < config.ruins().get(entry)) {
				structuretemplate = structuretemplatemanager.getOrCreate(entry);
				break;
			}
		}
		if (structuretemplate == null) structuretemplate = structuretemplatemanager.getOrCreate(config.defaultRuin());

		ChunkPos chunkpos = ChunkPos.containing(blockpos);
		BoundingBox boundingbox = new BoundingBox(chunkpos.getMinBlockX() - 16, level.getMinY(), chunkpos.getMinBlockZ() - 16, chunkpos.getMaxBlockX() + 16, level.getMaxY(), chunkpos.getMaxBlockZ() + 16);
		StructurePlaceSettings structureplacesettings = new StructurePlaceSettings().setRotation(rotation).setBoundingBox(boundingbox).setRandom(random);
		Vec3i vec3i = structuretemplate.getSize(rotation);
		BlockPos blockpos1 = blockpos.offset(-vec3i.getX() / 2, 0, -vec3i.getZ() / 2);
		BlockPos blockpos2 = structuretemplate.getZeroPositionWithTransform(blockpos1, Mirror.NONE, rotation);

		if (!level.getBlockState(blockpos2.below()).isSolidRender()) return false;

		structureplacesettings.clearProcessors();
		config.processor().value().list().forEach(structureplacesettings::addProcessor);
		return structuretemplate.placeInWorld(level, blockpos2, blockpos2, structureplacesettings, random, 20);
	}
}
