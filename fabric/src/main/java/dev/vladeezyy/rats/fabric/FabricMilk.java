package dev.vladeezyy.rats.fabric;

import com.github.alexthe666.rats.RatsMod;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Milk as a fluid on Fabric (NeoForge has one, enabled by the mod): rats carry it, the curdler stores it. It exists in
 * tanks only (no block); a milk bucket holds a bucket of it.
 */
final class FabricMilk {
    static final Fluid MILK = new MilkFluid();

    private FabricMilk() {}

    static void register() {
        Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "milk"), MILK);
        FluidStorage.combinedItemApiProvider(Items.MILK_BUCKET).register(context ->
            new FullItemFluidStorage(context, Items.BUCKET, FluidVariant.of(MILK), FluidConstants.BUCKET));
        FluidStorage.combinedItemApiProvider(Items.BUCKET).register(context ->
            new EmptyItemFluidStorage(context, Items.MILK_BUCKET, MILK, FluidConstants.BUCKET));
    }

    private static final class MilkFluid extends Fluid {
        @Override
        public Item getBucket() {
            return Items.MILK_BUCKET;
        }

        @Override
        protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
            return true;
        }

        @Override
        protected Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
            return Vec3.ZERO;
        }

        @Override
        public int getTickDelay(LevelReader level) {
            return 5;
        }

        @Override
        protected float getExplosionResistance() {
            return 100.0F;
        }

        @Override
        public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
            return 0;
        }

        @Override
        public float getOwnHeight(FluidState state) {
            return 0;
        }

        @Override
        protected BlockState createLegacyBlock(FluidState state) {
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }

        @Override
        public int getAmount(FluidState state) {
            return 0;
        }

        @Override
        public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
            return Shapes.empty();
        }
    }
}
