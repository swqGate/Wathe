package dev.doctor4t.wathe.item;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheCosmetics;
import dev.doctor4t.wathe.index.WatheDataComponentTypes;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.util.KnifeStabPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Random;

public class KnifeItem extends Item {
    public KnifeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, @NotNull PlayerEntity user, Hand hand) {

        ItemStack itemStack = user.getStackInHand(hand);

        Skin currentSkin = Skin.fromString(WatheCosmetics.getSkin(itemStack));
        if (currentSkin == null) {
            currentSkin = Skin.DEFAULT;
        }
        WatheCosmetics.setSkin(user.getUuid(), itemStack, Skin.getNext(currentSkin).getName());

        user.setCurrentHand(hand);
        user.playSound(WatheSounds.ITEM_KNIFE_PREPARE, 1.0f, 1.0f);
        return TypedActionResult.consume(itemStack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user.isSpectator()) {
            return;
        }

        if (remainingUseTicks >= this.getMaxUseTime(stack, user) - 10 || !(user instanceof PlayerEntity attacker) || !world.isClient)
            return;
        HitResult collision = getKnifeTarget(attacker);
        if (collision instanceof EntityHitResult entityHitResult) {
            Entity target = entityHitResult.getEntity();
            ClientPlayNetworking.send(new KnifeStabPayload(target.getId()));
        }
    }

    public static HitResult getKnifeTarget(PlayerEntity user) {
        return ProjectileUtil.getCollision(user, entity -> entity instanceof PlayerEntity player && GameFunctions.isPlayerAliveAndSurvival(player), 3f);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    public enum Skin {
        DEFAULT(new int[]{0xFF2B2632}, new int[]{0xFF1B1B1B}, "default"),
        CEREMONIAL(new int[]{0xFFE7761F}, new int[]{0xFFA84701}, "Ceremonial Dagger"),
        PICK(new int[]{0xFFE7761F}, new int[]{0xFFA84701}, "Ice Pick");

        public final int[] colors;
        public final int[] shadowColors;
        public final @Nullable String tooltipName;
        public final Random random;

        Skin(int[] colors, int[] shadowColors, @Nullable String tooltipName) {
            this.colors = colors;
            this.shadowColors = shadowColors;
            this.tooltipName = tooltipName;
            this.random = new Random();
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public int getFirstColor() {
            return this.colors[0];
        }

        public Pair<Integer, Integer> getRandomParticleColorPair() {
            int i = this.random.nextInt(this.colors.length);
            return new Pair<>(this.colors[i], this.shadowColors[i]);
        }

        @Nullable
        public static Skin fromString(String name) {
            for (Skin skin : Skin.values()) if (skin.getName().equalsIgnoreCase(name)) return skin;
            return null;
        }

        public static Skin getNext(Skin skin) {
            Skin[] values = Skin.values();
            return values[(skin.ordinal() + 1) % values.length];
        }
    }
}