package dev.doctor4t.trainmurdermystery.client.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.TMMComponents;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RoundTextRenderer {
    private static final Map<String, Optional<GameProfile>> failCache = new HashMap<>();
    private static final int WELCOME_DURATION = 200 + GameConstants.FADE_TIME * 2 + GameConstants.FADE_PAUSE;
    private static final int END_DURATION = 200;
    private static RoleAnnouncementText role = RoleAnnouncementText.CIVILIAN;
    private static int welcomeTime = 0;
    private static int killers = 0;
    private static int targets = 0;
    private static int endTime = 0;

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public static void renderHud(TextRenderer renderer, ClientPlayerEntity player, @NotNull DrawContext context) {
        if (welcomeTime > 0) {
            context.getMatrices().push();
            context.getMatrices().translate(context.getScaledWindowWidth() / 2f, context.getScaledWindowHeight() / 2f + 3.5, 0);
            context.getMatrices().push();
            context.getMatrices().scale(2.6f, 2.6f, 1f);
            if (welcomeTime <= 180) context.drawTextWithShadow(renderer, role.welcomeText, -renderer.getWidth(role.welcomeText) / 2, -12, 0xFFFFFF);
            context.getMatrices().pop();
            context.getMatrices().push();
            context.getMatrices().scale(1.2f, 1.2f, 1f);
            if (welcomeTime <= 120) context.drawTextWithShadow(renderer, role.premiseText.apply(killers), -renderer.getWidth(role.premiseText.apply(killers)) / 2, 0, 0xFFFFFF);
            context.getMatrices().pop();
            context.getMatrices().push();
            context.getMatrices().scale(1f, 1f, 1f);
            if (welcomeTime <= 60) context.drawTextWithShadow(renderer, role.goalText.apply(targets), -renderer.getWidth(role.goalText.apply(targets)) / 2, 14, 0xFFFFFF);
            context.getMatrices().pop();
            context.getMatrices().pop();
        }
        var game = TMMComponents.GAME.get(player.getWorld());
        if (true || endTime > 0 && !game.isRunning()) {
            var roundEnd = GameRoundEndComponent.KEY.get(player.getWorld());
            if (roundEnd.getWinStatus() == GameFunctions.WinStatus.NONE) return;
            var endText = role.getEndText(roundEnd.getWinStatus());
            if (endText == null) return;
            context.getMatrices().push();
            context.getMatrices().translate(context.getScaledWindowWidth() / 2f, context.getScaledWindowHeight() / 2f - 40, 0);
            context.getMatrices().push();
            context.getMatrices().scale(2.6f, 2.6f, 1f);
            context.drawTextWithShadow(renderer, endText, -renderer.getWidth(endText) / 2, -12, 0xFFFFFF);
            context.getMatrices().pop();
            context.getMatrices().push();
            context.getMatrices().scale(1.2f, 1.2f, 1f);
            var winMessage = Text.translatable("game.win." + roundEnd.getWinStatus().name().toLowerCase().toLowerCase());
            context.drawTextWithShadow(renderer, winMessage, -renderer.getWidth(winMessage) / 2, -4, 0xFFFFFF);
            context.getMatrices().pop();
            context.getMatrices().push();
            context.getMatrices().scale(1f, 1f, 1f);
            var vigilanteTotal = 1;
            for (var entry : roundEnd.getPlayers()) if (entry.role() == RoleAnnouncementText.VIGILANTE) vigilanteTotal += 1;
            context.drawTextWithShadow(renderer, RoleAnnouncementText.CIVILIAN.titleText, -renderer.getWidth(RoleAnnouncementText.CIVILIAN.titleText) / 2 - 60, 14, 0xFFFFFF);
            context.drawTextWithShadow(renderer, RoleAnnouncementText.VIGILANTE.titleText, -renderer.getWidth(RoleAnnouncementText.VIGILANTE.titleText) / 2 + 50, 14, 0xFFFFFF);
            context.drawTextWithShadow(renderer, RoleAnnouncementText.KILLER.titleText, -renderer.getWidth(RoleAnnouncementText.KILLER.titleText) / 2 + 50, 14 + 16 + 24 * ((vigilanteTotal) / 2), 0xFFFFFF);
            context.getMatrices().pop();
            var civilians = 0;
            var vigilantes = 0;
            var killers = 0;
            for (var entry : roundEnd.getPlayers()) {
                context.getMatrices().push();
                context.getMatrices().scale(2f, 2f, 1f);
                switch (entry.role()) {
                    case CIVILIAN -> {
                        context.getMatrices().translate(-60 + (civilians % 4) * 12, 14 + (civilians / 4) * 12, 0);
                        civilians++;
                    }
                    case VIGILANTE -> {
                        context.getMatrices().translate(7 + (vigilantes % 2) * 12, 14 + (vigilantes / 2) * 12, 0);
                        vigilantes++;
                    }
                    case KILLER -> {
                        context.getMatrices().translate(0, 8 + ((vigilanteTotal) / 2) * 12, 0);
                        context.getMatrices().translate(7 + (killers % 2) * 12, 14 + (killers / 2) * 12, 0);
                        killers++;
                    }
                }
                var texture = getSkinTextures(entry.player().getName());
                if (texture != null) {
                    RenderSystem.enableBlend();
                    context.getMatrices().push();
                    context.getMatrices().translate(8, 0, 0);
                    var offColour = entry.wasDead() ? 0.4f : 1f;
                    context.drawTexturedQuad(texture.texture(), 0, 8, 0, 8, 0, 8 / 64f, 16 / 64f, 8 / 64f, 16 / 64f, 1f, offColour, offColour, 1f);
                    context.getMatrices().translate(-0.5, -0.5, 0);
                    context.getMatrices().scale(1.125f, 1.125f, 1f);
                    context.drawTexturedQuad(texture.texture(), 0, 8, 0, 8, 0, 40 / 64f, 48 / 64f, 8 / 64f, 16 / 64f, 1f, offColour, offColour, 1f);
                    context.getMatrices().pop();
                }
                if (entry.wasDead()) {
                    context.getMatrices().translate(13, 0, 0);
                    context.getMatrices().scale(2f, 1f, 1f);
                    context.drawText(renderer, "x", -renderer.getWidth("x") / 2, 0, 0xE10000, false);
                    context.drawText(renderer, "x", -renderer.getWidth("x") / 2, 1, 0x550000, false);
                }
                context.getMatrices().pop();
            }
            context.getMatrices().pop();
        }
    }

    public static void tick() {
        if (welcomeTime > 0) {
            switch (welcomeTime) {
                case 200 -> {
                    var player = MinecraftClient.getInstance().player;
                    if (player != null) player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), TMMSounds.UI_RISER, SoundCategory.MASTER, 10f, 1f, player.getRandom().nextLong());
                }
                case 180 -> {
                    var player = MinecraftClient.getInstance().player;
                    if (player != null) player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), TMMSounds.UI_PIANO, SoundCategory.MASTER, 10f, 1.25f, player.getRandom().nextLong());
                }
                case 120 -> {
                    var player = MinecraftClient.getInstance().player;
                    if (player != null) player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), TMMSounds.UI_PIANO, SoundCategory.MASTER, 10f, 1.5f, player.getRandom().nextLong());
                }
                case 60 -> {
                    var player = MinecraftClient.getInstance().player;
                    if (player != null) player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), TMMSounds.UI_PIANO, SoundCategory.MASTER, 10f, 1.75f, player.getRandom().nextLong());
                }
                case 1 -> {
                    var player = MinecraftClient.getInstance().player;
                    if (player != null) player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), TMMSounds.UI_PIANO_STINGER, SoundCategory.MASTER, 10f, 1f, player.getRandom().nextLong());
                }
            }
            welcomeTime--;
        }
        if (endTime > 0) {
            if (endTime == END_DURATION) {
                var player = MinecraftClient.getInstance().player;
                if (player != null) player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), GameRoundEndComponent.KEY.get(player.getWorld()).didWin(player.getUuid()) ? TMMSounds.UI_PIANO_WIN : TMMSounds.UI_PIANO_LOSE, SoundCategory.MASTER, 10f, 1f, player.getRandom().nextLong());
            }
            endTime--;
        }
        var options = MinecraftClient.getInstance().options;
        if (options != null && options.playerListKey.isPressed()) endTime = Math.max(2, endTime);
    }

    public static void startWelcome(RoleAnnouncementText role, int killers, int targets) {
        RoundTextRenderer.role = role;
        welcomeTime = WELCOME_DURATION;
        RoundTextRenderer.killers = killers;
        RoundTextRenderer.targets = targets;
    }

    public static void startEnd() {
        endTime = END_DURATION;
    }

    public static GameProfile getGameProfile(String disguise) {
        var optional = SkullBlockEntity.fetchProfileByName(disguise).getNow(failCache(disguise));
        return optional.orElse(failCache(disguise).get());
    }

    public static SkinTextures getSkinTextures(String disguise) {
        return MinecraftClient.getInstance().getSkinProvider().getSkinTextures(getGameProfile(disguise));
    }

    public static Optional<GameProfile> failCache(String name) {
        return failCache.computeIfAbsent(name, (d) -> Optional.of(new GameProfile(UUID.randomUUID(), name)));
    }
}