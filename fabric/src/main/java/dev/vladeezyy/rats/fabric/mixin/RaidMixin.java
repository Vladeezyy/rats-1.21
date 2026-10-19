package dev.vladeezyy.rats.fabric.mixin;

import com.github.alexthe666.rats.registry.RatsEntityRegistry;
import com.github.alexthe666.rats.server.entity.monster.PiedPiper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The Pied Piper in raid waves (NeoForge: the raider type enum extension with 0,0,1,0,0,1,1,2 per wave) */
@Mixin(Raid.class)
public abstract class RaidMixin {
    private static final int[] RATS$PIPERS_PER_WAVE = {0, 0, 1, 0, 0, 1, 1, 2};

    @Shadow
    private int groupsSpawned;
    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    public abstract void joinRaid(int wave, net.minecraft.world.entity.raid.Raider raider, BlockPos pos, boolean fromSpawn);

    @Inject(method = "spawnGroup", at = @At("TAIL"))
    private void rats$spawnPipers(BlockPos pos, CallbackInfo ci) {
        // groupsSpawned was just counted up: it is this wave's number
        int wave = this.groupsSpawned;
        int count = RATS$PIPERS_PER_WAVE[Math.min(wave, RATS$PIPERS_PER_WAVE.length - 1)];
        for (int i = 0; i < count; i++) {
            PiedPiper piper = RatsEntityRegistry.PIED_PIPER.get().create(this.level);
            if (piper == null) {
                break;
            }
            this.joinRaid(wave, piper, pos, false);
            piper.setOnGround(true);
        }
    }
}
