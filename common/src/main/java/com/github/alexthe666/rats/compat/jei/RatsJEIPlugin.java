package com.github.alexthe666.rats.compat.jei;

import com.github.alexthe666.rats.client.util.ClientRecipes;

import com.github.alexthe666.rats.server.misc.ItemNbt;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import net.minecraft.world.item.crafting.RecipeHolder;
import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.registry.RatlantisItemRegistry;
import com.github.alexthe666.rats.registry.RatsBlockRegistry;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.registry.RatsRecipeRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@JeiPlugin
public class RatsJEIPlugin implements IModPlugin {
	public static final Identifier MOD = Identifier.fromNamespaceAndPath(RatsMod.MODID, "rats");

	private void addDescription(IRecipeRegistration registry, ItemStack itemStack) {
		registry.addIngredientInfo(itemStack, VanillaTypes.ITEM_STACK, Component.translatable(itemStack.getItem().getDescriptionId() + ".jei_desc"));
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		if (RatConfig.cheesemaking) {
			List<CauldronInfoHolder> cauldrons = new LinkedList<>();
			cauldrons.add(new CauldronInfoHolder(Items.MILK_BUCKET, Items.MILK_BUCKET, Items.CAULDRON, RatsBlockRegistry.MILK_CAULDRON.get()));
			cauldrons.add(new CauldronInfoHolder(Items.AIR, RatsBlockRegistry.BLOCK_OF_CHEESE.get(), RatsBlockRegistry.MILK_CAULDRON.get(), RatsBlockRegistry.CHEESE_CAULDRON.get()));
			cauldrons.add(new CauldronInfoHolder(Items.SUGAR, RatsBlockRegistry.BLOCK_OF_BLUE_CHEESE.get(), RatsBlockRegistry.CHEESE_CAULDRON.get(), RatsBlockRegistry.BLUE_CHEESE_CAULDRON.get()));
			cauldrons.add(new CauldronInfoHolder(Items.LAVA_BUCKET, RatsBlockRegistry.BLOCK_OF_NETHER_CHEESE.get(), RatsBlockRegistry.CHEESE_CAULDRON.get(), RatsBlockRegistry.NETHER_CHEESE_CAULDRON.get()));
			runtime.getRecipeManager().addRecipes(RatsRecipeTypes.CAULDRON, cauldrons);
		}
	}

	@Override
	public void registerRecipes(IRecipeRegistration registry) {
		// 26.x clients have no recipe manager: the server sends these (ClientRecipes)
		registry.addRecipes(RatsRecipeTypes.CHEF, ClientRecipes.chef());
		this.addDescription(registry, new ItemStack(RatsItemRegistry.CHEESE.get()));
		this.addDescription(registry, new ItemStack(RatsItemRegistry.CHEESE_STICK.get()));
		this.addDescription(registry, new ItemStack(RatsItemRegistry.RAT_FLUTE.get()));
		this.addDescription(registry, new ItemStack(RatsItemRegistry.RAT_UPGRADE_BASIC.get()));
		this.addDescription(registry, new ItemStack(RatsItemRegistry.RAT_UPGRADE_CHEF.get()));
		this.addDescription(registry, new ItemStack(RatsItemRegistry.RAT_UPGRADE_WHITELIST.get()));
		this.addDescription(registry, new ItemStack(RatsItemRegistry.RAT_UPGRADE_BLACKLIST.get()));
		this.addDescription(registry, new ItemStack(RatsBlockRegistry.RAT_TRAP.get()));
		this.addDescription(registry, new ItemStack(RatsBlockRegistry.RAT_CAGE.get()));
		this.addDescription(registry, new ItemStack(RatsBlockRegistry.RAT_CRAFTING_TABLE.get()));
		if (RatsMod.RATLANTIS_DATAPACK_ENABLED) {
			registry.addRecipes(RatsRecipeTypes.ARCHEOLOGIST, ClientRecipes.archeologist());
			this.addDescription(registry, new ItemStack(RatlantisItemRegistry.RAT_UPGRADE_ARCHEOLOGIST.get()));
		} else {
			if (!RatsMod.RATLANTIS_ITEMS.isEmpty()) {
				registry.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, RatsMod.RATLANTIS_ITEMS.stream().map(ItemStack::new).toList());
			}
		}
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new ChefRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new CauldronRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		if (RatsMod.RATLANTIS_DATAPACK_ENABLED) {
			registry.addRecipeCategories(new ArcheologistRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addCraftingStation(RatsRecipeTypes.CHEF, RatsItemRegistry.RAT_UPGRADE_CHEF.get());
		if (RatConfig.cheesemaking) {
			registry.addCraftingStation(RatsRecipeTypes.CAULDRON, Items.CAULDRON);
		}
		registry.addCraftingStation(RecipeTypes.CRAFTING, RatsItemRegistry.RAT_UPGRADE_CRAFTING.get());
		if (RatsMod.RATLANTIS_DATAPACK_ENABLED) {
			registry.addCraftingStation(RatsRecipeTypes.ARCHEOLOGIST, RatlantisItemRegistry.RAT_UPGRADE_ARCHEOLOGIST.get());
		}
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addUniversalRecipeTransferHandler(RatCraftingTableTransferHandler.INSTANCE);
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		// ore nuggets differ by the ore stored in their custom data
		registration.registerSubtypeInterpreter(RatsItemRegistry.RAT_NUGGET_ORE.get(), (stack, context) -> ItemNbt.view(stack).toString());
	}

	@Override
	public Identifier getPluginUid() {
		return MOD;
	}
}
