package dev.vladeezyy.rats.selftest;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * -Pscene=thaumaturge -Pwith_thaumaturge (NeoForge): the tooltips of Rats items with their Thaumaturge aspects (the
 * scene script turns on Thaumaturge's "aspects on tooltips without Shift"), after discovering every aspect, so Caseus
 * is drawn and not masked. Checks the client's aspect index (sent by the server) and the aspect's translation. Since
 * 1.21.6 a screen draws one tooltip a frame (setTooltipForNextFrame), so one screenshot per item.
 */
final class ThaumaturgeScene {
  private static final List<String> ITEMS = List.of("cheese", "block_of_nether_cheese", "cheese_stick");
  private static int shown;

  private ThaumaturgeScene() {}

  static void steps() {
    int t = 40;
    ClientScene.at(t, mc -> ClientScene.server(mc, p -> {
      p.setGameMode(GameType.CREATIVE);
      var server = p.level().getServer();
      server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "tt aspects " + p.getScoreboardName() + " discover all");
    }));
    ClientScene.at(t += 20, mc -> {
      mc.options.guiScale().set(2);
      mc.resizeGui();
      mc.setScreen(new Tooltips());
    });
    ClientScene.at(t += 20, mc -> {
      Set<String> cheese = aspects(stack("cheese"));
      ClientScene.check("client aspect index gives cheese Caseus", cheese.contains("rats:caseus"), cheese);
      Set<String> stick = aspects(stack("cheese_stick"));
      ClientScene.check("an item crafted from cheese gets Caseus from its recipe (cheese staff)", stick.contains("rats:caseus"), stick);
      ClientScene.check("Caseus is translated", I18n.exists("aspect.rats.caseus") && I18n.exists("aspect.rats.caseus.desc"), ClientScene.LANG);
    });
    for (int i = 0; i < ITEMS.size(); i++) {
      int item = i;
      ClientScene.at(t += 10, mc -> shown = item);
      ClientScene.at(t += 10, mc -> ClientScene.shot(mc, "thaumaturge_" + ClientScene.LANG + "_" + ITEMS.get(item)));
    }
    ClientScene.at(t += 10, mc -> {
      mc.setScreen(null);
      RatsMod.LOGGER.info("[scene] DONE " + ClientScene.passed() + "/" + ClientScene.checks() + (ClientScene.passed() == ClientScene.checks() ? " passed" : " (" + (ClientScene.checks() - ClientScene.passed()) + " failed)"));
      mc.disconnectWithSavingScreen();
      mc.stop();
    });
  }

  private static ItemStack stack(String id) {
    return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(RatsMod.MODID, id)));
  }

  /** Thaumaturge's AspectIndexAccess.of(stack).aspects() by reflection (no compile dependency on it) */
  @SuppressWarnings("unchecked")
  private static Set<String> aspects(ItemStack stack) {
    Set<String> ids = new TreeSet<>();
    try {
      Object list = Class.forName("com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess").getMethod("of", ItemStack.class).invoke(null, stack);
      for (Holder<?> holder : (Set<Holder<?>>) list.getClass().getMethod("aspects").invoke(list)) {
        holder.unwrapKey().ifPresent(key -> ids.add(key.identifier().toString()));
      }
    } catch (ReflectiveOperationException e) {
      ids.add("error: " + e);
    }
    return ids;
  }

  /** One item tooltip and the aspect's name and description from the language file */
  private static final class Tooltips extends Screen {
    Tooltips() {
      super(Component.literal("Thaumaturge"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(graphics, mouseX, mouseY, partialTick);
      graphics.text(font, Component.translatable("aspect.rats.caseus").append(": ").append(Component.translatable("aspect.rats.caseus.desc"))
          .append(" (").append(Component.translatable("aspect.rats.caseus.help")).append(")"), 8, 8, 0xFFFFFFFF, true);
      graphics.setTooltipForNextFrame(font, stack(ITEMS.get(shown)), width / 3, 40);
    }

    @Override
    public boolean isPauseScreen() {
      return false;
    }
  }
}
