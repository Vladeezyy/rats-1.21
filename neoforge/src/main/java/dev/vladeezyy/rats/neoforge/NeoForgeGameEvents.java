package dev.vladeezyy.rats.neoforge;

import com.github.alexthe666.rats.registry.RatsVillagerRegistry;
import com.github.alexthe666.rats.server.events.RatlantisPack;
import com.github.alexthe666.rats.server.events.RatsEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.util.TriState;
import com.github.alexthe666.rats.registry.RatsRecipeRegistry;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** The game events of Rats on NeoForge, passed to the common handlers ({@link RatsEvents}, {@link RatlantisPack}) */
final class NeoForgeGameEvents {
    private NeoForgeGameEvents() {}

    static void register() {
        var bus = NeoForge.EVENT_BUS;
        bus.addListener((PlayerInteractEvent.EntityInteract event) ->
            RatsEvents.onPlayerInteractWithEntity(event.getEntity(), event.getLevel(), event.getHand(), event.getTarget()));
        bus.addListener((LivingChangeTargetEvent event) -> {
            if (RatsEvents.cancelTargetChange(event.getEntity(), event.getNewAboutToBeSetTarget())) {
                event.setCanceled(true);
            }
        });
        bus.addListener((VanillaGameEvent event) -> {
            if (RatsEvents.cancelGameEvent(event.getCause())) {
                event.setCanceled(true);
            }
        });
        bus.addListener(EventPriority.HIGHEST, (LivingDeathEvent event) -> {
            if (RatsEvents.onLivingDeath(event.getEntity())) {
                event.setCanceled(true);
            }
        });
        bus.addListener(EventPriority.HIGHEST, (ItemExpireEvent event) -> RatsEvents.onItemExpire(event.getEntity()));
        bus.addListener((LivingIncomingDamageEvent event) -> {
            if (RatsEvents.onIncomingDamage(event.getEntity(), event.getSource(), event.getAmount())) {
                event.setCanceled(true);
            }
        });
        bus.addListener((MobEffectEvent.Applicable event) -> {
            if (!RatsEvents.canApplyEffect(event.getEntity(), event.getEffectInstance())) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            }
        });
        bus.addListener((LevelEvent.Load event) -> {
            if (event.getLevel() instanceof ServerLevel level) {
                RatsEvents.onLevelLoad(level);
                RatlantisPack.onLevelLoad(level);
            }
        });
        bus.addListener((OnDatapackSyncEvent event) -> {
            RatlantisPack.onDatapackSync(event.getPlayer(), event.getPlayerList());
            // 26.x clients have no recipes: the cauldron and JEI read these (ClientRecipes)
            event.sendRecipes(RatsRecipeRegistry.CHEF.get(), RatsRecipeRegistry.ARCHEOLOGIST.get());
        });
        bus.addListener((PlayerEvent.PlayerLoggedInEvent event) -> RatsEvents.onPlayerLoggedIn(event.getEntity()));
        bus.addListener((EntityJoinLevelEvent event) -> RatsEvents.onEntityJoin(event.getEntity()));
        bus.addListener((FinalizeSpawnEvent event) -> {
            //Carry On passes null into their difficulty when firing this event so we have to check this unfortunately. They shouldnt be doing this.
            if (event.getDifficulty() != null) {
                RatsEvents.onFinalizeSpawn((Mob) event.getEntity(), event.getLevel(), event.getDifficulty(), event.getSpawnType());
            }
        });
        bus.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            if (RatsEvents.onRightClickBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec()) == InteractionResult.FAIL) {
                event.setUseBlock(TriState.FALSE);
                event.setCancellationResult(InteractionResult.FAIL);
                event.setCanceled(true);
            }
        });
        bus.addListener((ItemFishedEvent event) -> RatsEvents.onItemFished(event.getEntity(), event.getHookEntity(), event.getDrops()));
        bus.addListener((PlayerInteractEvent.LeftClickEmpty event) -> RatsEvents.onLeftClickEmpty(event.getEntity(), event.getItemStack()));
        bus.addListener((LivingHealEvent event) -> {
            if (RatsEvents.cancelHeal(event.getEntity())) {
                event.setCanceled(true);
            }
        });
        bus.addListener((EntityTickEvent.Pre event) -> RatsEvents.onEntityTick(event.getEntity()));
        bus.addListener((MobEffectEvent.Remove event) -> RatsEvents.onEffectEnded(event.getEntity(), event.getEffectInstance()));
        bus.addListener((MobEffectEvent.Expired event) -> RatsEvents.onEffectEnded(event.getEntity(), event.getEffectInstance()));
    }
}
