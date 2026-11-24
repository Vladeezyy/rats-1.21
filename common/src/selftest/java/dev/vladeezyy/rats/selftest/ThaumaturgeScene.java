package dev.vladeezyy.rats.selftest;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * -Pscene=thaumaturge -Pwith_thaumaturge (NeoForge): the tooltips of Rats items with their Thaumaturge aspects (the
 * scene script turns on Thaumaturge's "aspects on tooltips without Shift"), after discovering every aspect, so Caseus
 * is drawn and not masked. Checks the client's aspect index (sent by the server) and the aspect's translation.
 */
final class ThaumaturgeScene {
  private static final List<String> ITEMS = List.of("cheese", "blue_cheese", "nether_cheese", "block_of_cheese",
      "raw_rat", "rat_whistle", "cheese_stick", "rat_crafting_table");

  private ThaumaturgeScene() {}

  static void steps() {
    int t = 40;
    at(t, mc -> ClientScene.server(mc, p -> {
      p.setGameMode(GameType.CREATIVE);
      var source = p.createCommandSourceStack().withPermission(4).withSuppressedOutput();
      p.getServer().getCommands().performPrefixedCommand(source, "tt aspects " + p.getGameProfile().getName() + " discover all");
    }));
    at(t += 20, mc -> {
      mc.options.guiScale().set(2);
      mc.resizeDisplay();
      mc.setScreen(new Tooltips());
    });
    at(t += 20, mc -> {
      Set<String> cheese = aspects(stack("cheese"));
      ClientScene.check("client aspect index gives cheese Caseus", cheese.contains("rats:caseus"), cheese);
      Set<String> stick = aspects(stack("cheese_stick"));
      ClientScene.check("an item crafted from cheese gets Caseus from its recipe (cheese staff)", stick.contains("rats:caseus"), stick);
      ClientScene.check("Caseus is translated", I18n.exists("aspect.rats.caseus") && I18n.exists("aspect.rats.caseus.desc"), ClientScene.LANG);
      ClientScene.shot(mc, "thaumaturge_" + ClientScene.LANG);
    });
    at(t += 10, mc -> {
      mc.setScreen(null);
      RatsMod.LOGGER.info("[scene] DONE " + ClientScene.passed() + "/" + ClientScene.checks() + (ClientScene.passed() == ClientScene.checks() ? " passed" : " (" + (ClientScene.checks() - ClientScene.passed()) + " failed)"));
      mc.level.disconnect();
      mc.disconnect(new TitleScreen());
      mc.stop();
    });
  }

  private static void at(int tick, java.util.function.Consumer<Minecraft> action) {
    ClientScene.at(tick, action);
  }

  private static ItemStack stack(String id) {
    return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, id)));
  }

  /** Thaumaturge's AspectIndexAccess.of(stack).aspects() by reflection (no compile dependency on it) */
  @SuppressWarnings("unchecked")
  private static Set<String> aspects(ItemStack stack) {
    Set<String> ids = new TreeSet<>();
    try {
      Object list = Class.forName("com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess").getMethod("of", ItemStack.class).invoke(null, stack);
      for (Holder<?> holder : (Set<Holder<?>>) list.getClass().getMethod("aspects").invoke(list)) {
        holder.unwrapKey().ifPresent(key -> ids.add(key.location().toString()));
      }
    } catch (ReflectiveOperationException e) {
      ids.add("error: " + e);
    }
    return ids;
  }

  /** Eight item tooltips in two rows and the aspect's name and description from the language file */
  private static final class Tooltips extends Screen {
    Tooltips() {
      super(Component.literal("Thaumaturge"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.render(graphics, mouseX, mouseY, partialTick);
      graphics.drawString(font, Component.translatable("aspect.rats.caseus").append(": ").append(Component.translatable("aspect.rats.caseus.desc"))
          .append(" (").append(Component.translatable("aspect.rats.caseus.help")).append(")"), 8, 8, 0xFFFFFF);
      int columns = 4;
      int cell = width / columns;
      for (int i = 0; i < ITEMS.size(); i++) {
        // a tooltip is drawn 12 px right of and above the given point
        graphics.renderTooltip(font, stack(ITEMS.get(i)), (i % columns) * cell - 8, 40 + (i / columns) * (height / 2 - 10));
      }
    }

    @Override
    public boolean isPauseScreen() {
      return false;
    }
  }
}
