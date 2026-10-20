package com.github.alexthe666.rats.server.block.entity;
import net.minecraft.world.Containers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.github.alexthe666.rats.server.misc.Values;

import dev.vladeezyy.rats.platform.transfer.ItemHandler;
import com.github.alexthe666.rats.registry.RatsBlockEntityRegistry;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.inventory.RatCraftingTableMenu;
import com.github.alexthe666.rats.server.inventory.container.CraftingContainerWrapper;
import com.github.alexthe666.rats.server.inventory.container.TableItemHandlers;
import com.github.alexthe666.rats.server.misc.RatUpgradeUtils;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;
import dev.vladeezyy.rats.platform.transfer.ModifiableItemHandler;
import dev.vladeezyy.rats.platform.transfer.ItemHandlers;
import dev.vladeezyy.rats.platform.transfer.CombinedInvWrapper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unchecked", "unused"})
public class RatCraftingTableBlockEntity extends BlockEntity implements MenuProvider, RecipeCraftingHolder, Clearable {

	private static final Component DEFAULT_NAME = Component.translatable(RatsLangConstants.RAT_CRAFTING_TABLE);
	private Component customName;
	public int prevCookTime;
	private boolean hasRat;
	public boolean hasValidRecipe;
	private int cookTime;
	protected final StackedItemContents itemHelper = new StackedItemContents();
	protected Optional<RecipeHolder<CraftingRecipe>> guideRecipe = Optional.empty();
	protected Optional<RecipeHolder<CraftingRecipe>> recipeUsed = Optional.empty();
	protected List<RecipeHolder<CraftingRecipe>> possibleRecipes = List.of();
	public int totalCookTime = 200;
	private int selectedRecipeIndex = 0;
	/**
	 * What the menu shows (26.x clients have no recipes): cook time, total, whether a rat works the table, whether a
	 * recipe is used, how many recipes match, the guide recipe's result (item id + 1 in two 16-bit halves, count)
	 */
	private final ContainerData dataAccess = new ContainerData() {
		public int get(int index) {
			RatCraftingTableBlockEntity table = RatCraftingTableBlockEntity.this;
			return switch (index) {
				case 0 -> table.cookTime;
				case 1 -> table.totalCookTime;
				case 2 -> table.hasRat ? 1 : 0;
				case 3 -> table.recipeUsed.isPresent() ? 1 : 0;
				case 4 -> table.possibleRecipes.size();
				case 5 -> table.guideResultId() & 0xFFFF;
				case 6 -> table.guideResultId() >>> 16;
				case 7 -> table.guideResult().getCount();
				default -> 0;
			};
		}

		public void set(int index, int value) {
			switch (index) {
				case 0 -> RatCraftingTableBlockEntity.this.cookTime = value;
				case 1 -> RatCraftingTableBlockEntity.this.totalCookTime = value;
			}

		}

		public int getCount() {
			return DATA_COUNT;
		}
	};

	public final Optional<ModifiableItemHandler> bufferHandler = Optional.of(new TableItemHandlers.BufferHandler(this));
	public final Optional<ModifiableItemHandler> matrixHandler = Optional.of(new TableItemHandlers.MatrixHandler(this));
	public final Optional<ModifiableItemHandler> resultHandler = Optional.of(new TableItemHandlers.ResultHandler(this));

	protected final Optional<ModifiableItemHandler> combinedHandler = Optional.of(new CombinedInvWrapper(this.matrixHandler.get(), this.bufferHandler.get()));
	public final Optional<CraftingContainer> matrixWrapper = Optional.of(new CraftingContainerWrapper(this.matrixHandler.get()));

	public RatCraftingTableBlockEntity(BlockPos pos, BlockState state) {
		super(RatsBlockEntityRegistry.RAT_CRAFTING_TABLE.get(), pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, RatCraftingTableBlockEntity te) {
		te.hasRat = false;
		te.totalCookTime = 200;

		for (TamedRat rat : level.getEntitiesOfClass(TamedRat.class, new AABB(pos.getX(), (double) pos.getY() + 1, pos.getZ(), (double) pos.getX() + 1, (double) pos.getY() + 2, (double) pos.getZ() + 1))) {
			if (RatUpgradeUtils.hasUpgrade(rat, RatsItemRegistry.RAT_UPGRADE_CRAFTING.get())) {
				te.hasRat = true;
				if (RatUpgradeUtils.hasUpgrade(rat, RatsItemRegistry.RAT_UPGRADE_SPEED.get())) {
					te.totalCookTime = 100;
				}
			}
		}

		if (!level.isClientSide()) {
			te.prevCookTime = te.cookTime;

			if (te.getRecipeUsed() != null && te.hasRat && te.cookTime < te.totalCookTime) {
				te.cookTime++;
			} else {
				te.cookTime = Mth.clamp(te.cookTime - 2, 0, te.totalCookTime);
			}
			if (te.cookTime >= te.totalCookTime) {
				te.cookTime = 0;
				ItemStack addStack = te.recipeUsed.map(r -> r.value().assemble(te.craftingInput())).orElse(ItemStack.EMPTY);
				te.resultHandler.ifPresent(h -> h.setStackInSlot(0, addStack.copyWithCount(addStack.getCount() + h.getStackInSlot(0).getCount())));
				te.consumeIngredients(null);
				te.updateRecipe();
			}
		}
	}

	/** The crafting recipes the grid matches, sorted by their result (1.21.1's RecipeManager.getRecipesFor) */
	@SuppressWarnings("unchecked")
	private static List<RecipeHolder<CraftingRecipe>> matchingRecipes(ServerLevel level, CraftingInput input) {
		return level.recipeAccess().getRecipes().stream()
				.filter(holder -> holder.value().getType() == RecipeType.CRAFTING)
				.map(holder -> (RecipeHolder<CraftingRecipe>) holder)
				.filter(holder -> holder.value().matches(input, level))
				.sorted(java.util.Comparator.comparing(holder -> holder.value().assemble(input).getItem().getDescriptionId()))
				.toList();
	}

	public boolean hasRat() {
		return this.hasRat;
	}

	public int getCookTime() {
		return this.cookTime;
	}

	public void updateHelper() {
		this.bufferHandler.ifPresent(handler -> ((TableItemHandlers.BufferHandler) handler).fillStackedContents(this.itemHelper));
		this.checkIfRecipeIsValid(this.recipeUsed, this.itemHelper);
	}

	public void updateRecipe() {
		AtomicBoolean flag = new AtomicBoolean(true);
		if (this.getLevel() != null) {
			this.matrixWrapper.ifPresent(w -> {
				// 26.x: only the server has the recipes
				this.possibleRecipes = this.getLevel() instanceof ServerLevel server ? matchingRecipes(server, this.craftingInput()) : List.of();
				if (this.possibleRecipes.isEmpty()) {
					flag.set(false);
				} else {
					this.selectedRecipeIndex = Mth.clamp(this.selectedRecipeIndex, 0, this.possibleRecipes.size() - 1);
					this.guideRecipe = Optional.of(this.possibleRecipes.get(this.selectedRecipeIndex));
					this.guideResult = ItemStack.EMPTY;
					if (!this.checkIfResultFits(this.getLevel(), this.guideRecipe)) {
						flag.set(false);
					}
					this.recipeUsed = Optional.of(this.possibleRecipes.get(this.selectedRecipeIndex))
							.filter(r -> this.setRecipeUsed(this.getLevel(), null, r)); // Set new recipe or null if missing/can't craft
				}
			});
			if (flag.get()) {
				this.checkIfRecipeIsValid(this.recipeUsed, this.itemHelper);
				if (!this.hasValidRecipe)
					this.setRecipeUsed(null);
			} else {
				this.guideRecipe = Optional.empty();
				this.guideResult = ItemStack.EMPTY;
				this.setRecipeUsed(null);
			}
		}
	}

	private boolean checkIfResultFits(Level level, Optional<RecipeHolder<CraftingRecipe>> recipe) {
		if (recipe.isPresent() && this.resultHandler.isPresent()) {
			ItemStack checkStack = this.resultHandler.get().getStackInSlot(0);
			ItemStack resultStack = recipe.get().value().assemble(this.craftingInput());
			return (ItemStack.isSameItemSameComponents(checkStack, resultStack) && checkStack.getCount() + resultStack.getCount() <= checkStack.getMaxStackSize()) || checkStack.isEmpty();
		}
		return false;
	}

	private void checkIfRecipeIsValid(Optional<RecipeHolder<CraftingRecipe>> recipe, StackedItemContents helper) {
		this.hasValidRecipe = recipe.isPresent() && helper.getBiggestCraftableStack(recipe.get().value(), null) > 0;
	}

	@Override
	public void clearContent() {
		this.combinedHandler.ifPresent(h -> {
			for (int i = 0; i < h.getSlots(); i++) {
				h.setStackInSlot(i, ItemStack.EMPTY);
			}
		});
		this.updateRecipe();
	}

	public void incrementSelectedRecipe(boolean negative) {
		if (negative) {
			this.selectedRecipeIndex--;
		} else {
			this.selectedRecipeIndex++;
		}
		this.updateRecipe();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
		this.recipeUsed = Optional.ofNullable((RecipeHolder<CraftingRecipe>) recipe);
	}

	public boolean setRecipeUsed(Level level, @Nullable ServerPlayer player, RecipeHolder<?> recipe) {
		return !(level instanceof ServerLevel server) || !server.getGameRules().get(GameRules.LIMITED_CRAFTING) || recipe.value().isSpecial();
	}

	/** What the recipe in use makes from the grid now, or empty */
	public ItemStack recipeResult() {
		return this.recipeUsed.map(r -> r.value().assemble(this.craftingInput())).orElse(ItemStack.EMPTY);
	}

	@Nullable
	@Override
	public RecipeHolder<?> getRecipeUsed() {
		return this.recipeUsed.orElse(null);
	}

	/** The 3x3 grid as a 1.21 recipe input */
	public CraftingInput craftingInput() {
		ModifiableItemHandler matrix = this.matrixHandler.get();
		java.util.List<ItemStack> items = new java.util.ArrayList<>(9);
		for (int i = 0; i < 9; i++) {
			items.add(matrix.getStackInSlot(i));
		}
		return CraftingInput.of(3, 3, items);
	}

	/** Values the menu syncs */
	public static final int DATA_COUNT = 8;
	private ItemStack guideResult = ItemStack.EMPTY;

	/** The guide recipe's result for the grid now (computed once per change of the guide recipe) */
	private ItemStack guideResult() {
		if (this.guideRecipe.isEmpty()) {
			this.guideResult = ItemStack.EMPTY;
		} else if (this.guideResult.isEmpty()) {
			this.guideResult = this.guideRecipe.get().value().assemble(this.craftingInput());
		}
		return this.guideResult;
	}

	private int guideResultId() {
		ItemStack result = this.guideResult();
		return result.isEmpty() ? 0 : net.minecraft.core.registries.BuiltInRegistries.ITEM.getId(result.getItem()) + 1;
	}

	public Optional<RecipeHolder<CraftingRecipe>> getGuideRecipe() {
		return this.guideRecipe;
	}

	public List<RecipeHolder<CraftingRecipe>> getPossibleRecipes() {
		return this.possibleRecipes;
	}

	// NeoForge's onLoad: once the table is in a level (vanilla calls setLevel then, on both loaders)
	@Override
	public void setLevel(net.minecraft.world.level.Level level) {
		super.setLevel(level);
		this.updateRecipe();
	}

	@Override
	protected void loadAdditional(ValueInput tag) {
		super.loadAdditional(tag);
		tag.child("Buffer").ifPresent(input -> this.bufferHandler.ifPresent(handler -> ((dev.vladeezyy.rats.platform.transfer.ItemStackHandler) handler).load(input)));
		tag.child("Matrix").ifPresent(input -> this.matrixHandler.ifPresent(handler -> ((dev.vladeezyy.rats.platform.transfer.ItemStackHandler) handler).load(input)));
		tag.child("Result").ifPresent(input -> this.resultHandler.ifPresent(handler -> ((dev.vladeezyy.rats.platform.transfer.ItemStackHandler) handler).load(input)));
		this.customName = tag.read("CustomName", ComponentSerialization.CODEC).orElse(null);
		this.cookTime = tag.getIntOr("CookTime", 0);
		this.selectedRecipeIndex = tag.getIntOr("SelectedRecipe", 0);
	}

	@Override
	public void saveAdditional(ValueOutput tag) {
		super.saveAdditional(tag);
		this.bufferHandler.ifPresent(h -> ((dev.vladeezyy.rats.platform.transfer.ItemStackHandler) h).save(tag.child("Buffer")));
		this.matrixHandler.ifPresent(h -> ((dev.vladeezyy.rats.platform.transfer.ItemStackHandler) h).save(tag.child("Matrix")));
		this.resultHandler.ifPresent(h -> ((dev.vladeezyy.rats.platform.transfer.ItemStackHandler) h).save(tag.child("Result")));
		tag.storeNullable("CustomName", ComponentSerialization.CODEC, this.customName);
		tag.putInt("CookTime", this.cookTime);
		tag.putInt("SelectedRecipe", this.selectedRecipeIndex);
	}

	@Nonnull
	@Override
	public Component getDisplayName() {
		return this.hasCustomName() ? this.customName : DEFAULT_NAME;
	}

	public boolean hasCustomName() {
		return this.customName != null;
	}

	public void setCustomName(@Nullable Component name) {
		this.customName = name;
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new RatCraftingTableMenu(id, inventory, this, this.dataAccess);
	}


	/** Hoppers: the result below, the buffer on the other sides (registered as the block's item handler capability) */
	public ItemHandler getItemHandler(@Nullable Direction side) {
		return side == Direction.DOWN ? this.resultHandler.get() : this.bufferHandler.get();
	}

	public void consumeIngredients(@Nullable Player player) {
		this.recipeUsed.ifPresent(recipe -> {
			NonNullList<ItemStack> remainingStacks = recipe.value().getRemainingItems(this.craftingInput());

			if (this.hasValidRecipe) {
				this.bufferHandler.ifPresent(h ->
						recipe.value().placementInfo().ingredients().forEach(i -> {
							for (int j = 0; j < h.getSlots(); j++) {
								if (i.test(h.getStackInSlot(j))) {
									h.extractItem(j, 1, false);
									break;
								}
							}
						}));
			}

			// Handle container items
			IntStream.range(0, remainingStacks.size())
					.mapToObj(i -> {
						ItemStack stack = remainingStacks.get(i);
						return this.hasValidRecipe ? stack : this.matrixHandler.map(h -> h.insertItem(i, stack, false)).orElse(stack);
					}) // Insert back the corresponding matrix slot if crafted from there
					.filter(stack -> !stack.isEmpty())
					.map(stack -> this.bufferHandler.map(h ->
							ItemHandlers.insertItemStacked(h, stack, false)).orElse(stack))
					.filter(stack -> !stack.isEmpty())
					.forEach(stack -> {
						if (player != null) {
							ItemHandlers.giveItemToPlayer(player, stack);
						} else {
							this.outputStack(stack);
						}
					});
		});
	}

	private void outputStack(ItemStack stack) {
		ItemStack newStack = this.bufferHandler
				.map(h -> ItemHandlers.insertItemStacked(h, stack, false))
				.orElse(stack);

		if (!newStack.isEmpty() && this.getLevel() != null) {
			ItemEntity item = new ItemEntity(this.getLevel(), this.getBlockPos().getX() + 0.5F, this.getBlockPos().getY() + 1.0D, this.getBlockPos().getZ() + 0.5F, newStack);
			item.setDeltaMovement(Vec3.ZERO);
			item.setNoPickUpDelay();
			item.setExtendedLifetime();
			this.getLevel().addFreshEntity(item);
		}
	}

	// 26.x: the buffer drops when the table is removed (1.21.1: the block's onRemove)
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (this.level != null) {
			this.bufferHandler.ifPresent(handler -> {
				for (int i = 0; i < handler.getSlots(); i++) {
					Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(i));
				}
			});
		}
	}
}
