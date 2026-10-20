package com.github.alexthe666.rats.server.block.entity;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import dev.vladeezyy.rats.platform.Platform;
import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.registry.RatsBlockEntityRegistry;
import com.github.alexthe666.rats.registry.RatsBlockRegistry;
import com.github.alexthe666.rats.registry.RatsParticleRegistry;
import com.github.alexthe666.rats.registry.RatsSoundRegistry;
import com.github.alexthe666.rats.server.inventory.AutoCurdlerMenu;
import com.github.alexthe666.rats.server.message.RatsNetworkHandler;
import com.github.alexthe666.rats.server.message.UpdateCurdlerFluidPacket;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import dev.vladeezyy.rats.platform.transfer.FluidStack;
import dev.vladeezyy.rats.platform.transfer.FluidHandler;
import dev.vladeezyy.rats.platform.transfer.ItemFluidHandler;
import dev.vladeezyy.rats.platform.transfer.FluidTank;
import dev.vladeezyy.rats.platform.transfer.ItemHandler;
import dev.vladeezyy.rats.platform.transfer.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AutoCurdlerBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, MenuProvider {
	private static final int[] SLOTS_TOP = new int[]{0};
	private static final int[] SLOTS_BOTTOM = new int[]{1};
	private final FluidTank tank = new FluidTank(FluidStack.BUCKET_VOLUME * 5, fluidStack -> fluidStack.getFluid().isSame(Platform.INSTANCE.milkFluid()));
	final ItemHandler[] handlers = {new SidedInvWrapper(this, Direction.UP), new SidedInvWrapper(this, Direction.DOWN)};
	private NonNullList<ItemStack> curdlerStacks = NonNullList.withSize(2, ItemStack.EMPTY);
	public int cookTime;
	public int totalCookTime;
	public final ContainerData data = new ContainerData() {
		public int get(int index) {
			return switch (index) {
				case 0 -> AutoCurdlerBlockEntity.this.cookTime;
				case 1 -> AutoCurdlerBlockEntity.this.totalCookTime;
				case 2 -> AutoCurdlerBlockEntity.this.tank.getFluidAmount();
				case 3 -> AutoCurdlerBlockEntity.this.tank.getCapacity();
				default -> 0;
			};
		}

		public void set(int index, int value) {
			switch (index) {
				case 0 -> AutoCurdlerBlockEntity.this.cookTime = value;
				case 1 -> AutoCurdlerBlockEntity.this.totalCookTime = value;
			}

		}

		public int getCount() {
			return 4;
		}
	};

	public AutoCurdlerBlockEntity(BlockPos pos, BlockState state) {
		super(RatsBlockEntityRegistry.AUTO_CURDLER.get(), pos, state);
	}

	public static boolean isMilk(ItemStack stack) {
		if (stack.is(Items.MILK_BUCKET)) {
			return true;
		}
		Optional<FluidStack> fluidStack = Platform.INSTANCE.fluidContained(stack);
		return fluidStack.isPresent() && fluidStack.get().getFluid().isSame(Platform.INSTANCE.milkFluid());
	}

	@Override
	public int getContainerSize() {
		return this.curdlerStacks.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.curdlerStacks) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack getItem(int index) {
		return this.curdlerStacks.get(index);
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		return ContainerHelper.removeItem(this.curdlerStacks, index, count);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return ContainerHelper.takeItem(this.curdlerStacks, index);
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		this.curdlerStacks.set(index, stack);

		if (stack.getCount() > this.getMaxStackSize()) {
			stack.setCount(this.getMaxStackSize());
		}
		this.setChanged();
	}

	@Override
	protected void loadAdditional(ValueInput compound) {
		super.loadAdditional(compound);
		this.tank.load(compound);
		this.curdlerStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, this.curdlerStacks);
		this.cookTime = compound.getIntOr("CookTime", 0);
		this.totalCookTime = compound.getIntOr("CookTimeTotal", 0);
	}

	@Override
	public void saveAdditional(ValueOutput compound) {
		super.saveAdditional(compound);
		this.tank.save(compound);
		compound.putInt("CookTime", (short) this.cookTime);
		compound.putInt("CookTimeTotal", (short) this.totalCookTime);
		ContainerHelper.saveAllItems(compound, this.curdlerStacks);
	}

	@Override
	public boolean stillValid(Player player) {
		if (this.getLevel() != null) {
			if (this.getLevel().getBlockEntity(this.getBlockPos()) != this) {
				return false;
			} else {
				return !(player.distanceToSqr((double) this.getBlockPos().getX() + 0.5D, (double) this.getBlockPos().getY() + 0.5D, (double) this.getBlockPos().getZ() + 0.5D) > 64.0D);
			}
		}
		return true;
	}

	public boolean hasEnoughMilk() {
		return this.tank.getFluidAmount() >= FluidStack.BUCKET_VOLUME && this.isMilkFluid(this.tank.getFluid());
	}

	public static void tick(Level level, BlockPos pos, BlockState state, AutoCurdlerBlockEntity te) {
		if (!level.isClientSide()) {
			te.totalCookTime = RatConfig.milkCauldronTime;
			if (te.hasEnoughMilk()) {
				if (te.canMakeCheese()) {
					++te.cookTime;

					if (te.cookTime == te.totalCookTime) {
						te.cookTime = 0;
						te.totalCookTime = RatConfig.milkCauldronTime;
						te.makeCheese(level);
					}
				} else {
					te.cookTime = 0;
				}
			} else if (!te.hasEnoughMilk() && te.cookTime > 0) {
				te.cookTime = Mth.clamp(te.cookTime - 2, 0, te.totalCookTime);
			}
			if (isMilk(te.getItem(0))) {
				Optional<FluidStack> fluidStackOptional = Platform.INSTANCE.fluidContained(te.getItem(0));
				fluidStackOptional.ifPresent(fluidStack -> {
					java.util.Optional<ItemFluidHandler> fluidHandlerOptional = Platform.INSTANCE.fluidHandler(te.getItem(0));
					fluidHandlerOptional.ifPresent(fluidHandler -> {
						if (fluidHandler.drain(te.tank.getCapacity() - te.tank.getFluidAmount(), FluidHandler.FluidAction.SIMULATE).getAmount() > 0) {
							if (te.tank.fill(fluidStack.copy(), FluidHandler.FluidAction.SIMULATE) != 0) {
								int amount = te.tank.fill(fluidStack.copy(), FluidHandler.FluidAction.EXECUTE);
								fluidHandler.drain(amount, FluidHandler.FluidAction.EXECUTE);
								//support container changing tanks
								ItemStack container = fluidHandler.getContainer();
								if (te.getItem(0) != container) {
									te.setItem(0, container);
								}
							}
							te.setChanged();
						}
					});
				});
			}
		}
	}

	public void makeCheese(Level level) {
		if (this.canMakeCheese()) {
			ItemStack toAdd = new ItemStack(RatsBlockRegistry.BLOCK_OF_CHEESE.get());
			if (this.getItem(1).is(toAdd.getItem()) && this.getItem(1).getCount() < 64) {
				this.getItem(1).grow(1);
			} else if (this.getItem(1).isEmpty()) {
				this.setItem(1, toAdd.copy());
			}
			this.tank.drain(FluidStack.BUCKET_VOLUME, FluidHandler.FluidAction.EXECUTE);
			level.playSound(null, this.getBlockPos(), RatsSoundRegistry.CHEESE_MADE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
			for (int i = 0; i < 10; i++) {
				((ServerLevel) level).sendParticles(RatsParticleRegistry.MILK_BUBBLE.get(),
						this.getBlockPos().getX() + 0.25F + (level.getRandom().nextFloat() * 0.5F),
						(this.getBlockPos().getY() + 0.55F) + (this.tank.getFluidAmount() * 0.0001F),
						this.getBlockPos().getZ() + 0.25F + (level.getRandom().nextFloat() * 0.5F),
						1, 0.0D, 0.0D, 0.0D, 0);
			}
			this.setChanged();
		}
	}

	public boolean canMakeCheese() {
		if (this.tank.getFluidAmount() < FluidStack.BUCKET_VOLUME) {
			return false;
		} else {
			ItemStack itemstack = new ItemStack(RatsBlockRegistry.BLOCK_OF_CHEESE.get());

			if (itemstack.isEmpty()) {
				return false;
			} else {
				ItemStack itemstack1 = this.getItem(1);
				if (itemstack1.isEmpty()) {
					return true;
				} else if (!itemstack1.is(itemstack.getItem())) {
					return false;
				} else if (itemstack1.getCount() + itemstack.getCount() <= this.getMaxStackSize() && itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize())  // Forge fix: make furnace respect stack sizes in furnace recipes
				{
					return true;
				} else {
					return itemstack1.getCount() + itemstack.getCount() <= itemstack.getMaxStackSize(); // Forge fix: make furnace respect stack sizes in furnace recipes
				}
			}
		}
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (this.getLevel() != null && !this.getLevel().isClientSide()) {
			RatsNetworkHandler.toTrackingBlock(this.getLevel(), this.getBlockPos(), new UpdateCurdlerFluidPacket(this.getBlockPos().asLong(), this.tank.getFluid()));
		}
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		if (index == 1) {
			return false;
		} else {
			return isMilk(stack);
		}
	}

	private boolean isMilkFluid(FluidStack fluid) {
		return net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid.getFluid()).getPath().contains("milk");
	}

	@Override
	public void clearContent() {
		this.curdlerStacks.clear();
	}

	public FluidTank getTank() {
		return this.tank;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		if (side == Direction.DOWN) {
			return SLOTS_BOTTOM;
		} else {
			return SLOTS_TOP;
		}
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
		return this.canPlaceItem(index, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return direction == Direction.DOWN && index == 1;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable(RatsLangConstants.CURDLER);
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
		return new AutoCurdlerMenu(id, this, inventory, this.data);
	}

	/** Hoppers and pipes per side, as in the original (registered as the block's item handler capability) */
	@Nullable
	public ItemHandler getItemHandler(@Nullable Direction side) {
		if (side == null) {
			return null;
		}
		return side == Direction.DOWN ? this.handlers[1] : this.handlers[0];
	}

	/** The milk tank (registered as the block's fluid handler capability) */
	public FluidHandler getFluidHandler(@Nullable Direction side) {
		return this.tank;
	}

	// 1.21: BaseContainerBlockEntity works on the item list
	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.curdlerStacks;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.curdlerStacks = items;
	}
}
