package dev.doctor4t.trainmurdermystery;

import com.google.common.reflect.Reflection;
import dev.doctor4t.trainmurdermystery.block.DoorPartBlock;
import dev.doctor4t.trainmurdermystery.command.*;
import dev.doctor4t.trainmurdermystery.command.argument.TMMGameModeArgumentType;
import dev.doctor4t.trainmurdermystery.command.argument.TimeOfDayArgumentType;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.index.*;
import dev.doctor4t.trainmurdermystery.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TMM implements ModInitializer {
    public static final String MOD_ID = "trainmurdermystery";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static @NotNull Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        // Init constants
        GameConstants.init();

        // Registry initializers
        Reflection.initialize(TMMDataComponentTypes.class);
        TMMSounds.initialize();
        TMMEntities.initialize();
        TMMBlocks.initialize();
        TMMItems.initialize();
        TMMBlockEntities.initialize();
        TMMParticles.initialize();

        // Register command argument types
        ArgumentTypeRegistry.registerArgumentType(id("timeofday"), TimeOfDayArgumentType.class, ConstantArgumentSerializer.of(TimeOfDayArgumentType::timeofday));
        ArgumentTypeRegistry.registerArgumentType(id("gamemode"), TMMGameModeArgumentType.class, ConstantArgumentSerializer.of(TMMGameModeArgumentType::gamemode));

        // Register commands
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            GiveRoomKeyCommand.register(dispatcher);
            StartCommand.register(dispatcher);
            StopCommand.register(dispatcher);
            CheckWeightsCommand.register(dispatcher);
            ResetWeightsCommand.register(dispatcher);
            SetVisualCommand.register(dispatcher);
            ForceRoleCommand.register(dispatcher);
            UpdateDoorsCommand.register(dispatcher);
            SetTimerCommand.register(dispatcher);
            SetMoneyCommand.register(dispatcher);
            SetBoundCommand.register(dispatcher);
        }));

        PayloadTypeRegistry.playS2C().register(ShootMuzzleS2CPayload.ID, ShootMuzzleS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PoisonUtils.PoisonOverlayPayload.ID, PoisonUtils.PoisonOverlayPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GunDropPayload.ID, GunDropPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TaskCompletePayload.ID, TaskCompletePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AnnounceWelcomePayload.ID, AnnounceWelcomePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AnnounceEndingPayload.ID, AnnounceEndingPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(KnifeStabPayload.ID, KnifeStabPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GunShootPayload.ID, GunShootPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StoreBuyPayload.ID, StoreBuyPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NoteEditPayload.ID, NoteEditPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KnifeStabPayload.ID, new KnifeStabPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(GunShootPayload.ID, new GunShootPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(StoreBuyPayload.ID, new StoreBuyPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(NoteEditPayload.ID, new NoteEditPayload.Receiver());

        Scheduler.init();
    }

    public static boolean isSkyVisibleAdjacent(@NotNull Entity player) {
        var mutable = new BlockPos.Mutable();
        var playerPos = BlockPos.ofFloored(player.getEyePos());
        for (var x = -1; x <= 1; x += 2) {
            for (var z = -1; z <= 1; z += 2) {
                mutable.set(playerPos.getX() + x, playerPos.getY(), playerPos.getZ() + z);
                if (player.getWorld().isSkyVisible(mutable)) {
                    return !(player.getWorld().getBlockState(playerPos).getBlock() instanceof DoorPartBlock);
                }
            }
        }
        return false;
    }

    public static boolean isExposedToWind(@NotNull Entity player) {
        var mutable = new BlockPos.Mutable();
        var playerPos = BlockPos.ofFloored(player.getEyePos());
        for (var x = 0; x <= 10; x++) {
            mutable.set(playerPos.getX() - x, player.getEyePos().getY(), playerPos.getZ());
            if (!player.getWorld().isSkyVisible(mutable)) {
                return false;
            }
        }
        return true;
    }
}


// POST RECORDING
// TORECORD: Ability to customize time of day for supporters + snow density
// TODO: Watermark?
// TODO: Sticky note price cheaper
// TODO: note reset button on the screen to remove all typed text
// TODO: Have knife be red on full charge only
