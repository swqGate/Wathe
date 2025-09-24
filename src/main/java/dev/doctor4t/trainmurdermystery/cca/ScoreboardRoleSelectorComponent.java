package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.*;

public class ScoreboardRoleSelectorComponent implements AutoSyncedComponent {
    public static final ComponentKey<ScoreboardRoleSelectorComponent> KEY = ComponentRegistry.getOrCreate(TMM.id("rolecounter"), ScoreboardRoleSelectorComponent.class);
    public final Scoreboard scoreboard;
    public final MinecraftServer server;
    public final Map<UUID, Integer> hitmanRounds = new HashMap<>();
    public final Map<UUID, Integer> vigilanteRounds = new HashMap<>();

    public ScoreboardRoleSelectorComponent(Scoreboard scoreboard, @Nullable MinecraftServer server) {
        this.scoreboard = scoreboard;
        this.server = server;
    }

    public void assignHitmen(ServerWorld world, GameWorldComponent gameComponent, @NotNull List<ServerPlayerEntity> players, int hitmanCount) {
        var map = new HashMap<ServerPlayerEntity, Float>();
        var total = 0f;
        for (var player : players) {
            var weight = 1f / this.hitmanRounds.getOrDefault(player.getUuid(), 1);
            map.put(player, weight);
            total += weight;
        }
        var hitmen = new ArrayList<ServerPlayerEntity>();
        for (var i = 0; i < hitmanCount; i++) {
            var random = world.getRandom().nextFloat() * total;
            for (var entry : map.entrySet()) {
                random -= entry.getValue();
                if (random <= 0) {
                    hitmen.add(entry.getKey());
                    total -= entry.getValue();
                    map.remove(entry.getKey());
                    this.hitmanRounds.put(entry.getKey().getUuid(), this.hitmanRounds.getOrDefault(entry.getKey().getUuid(), 1) + 1);
                    break;
                }
            }
        }
        for (var player : hitmen) {
//            player.giveItemStack(new ItemStack(TMMItems.KNIFE));
            gameComponent.addHitman(player);
        }
    }

    public void assignVigilantes(ServerWorld world, GameWorldComponent gameComponent, @NotNull List<ServerPlayerEntity> players, int hitmanCount) {
        var map = new HashMap<ServerPlayerEntity, Float>();
        var total = 0f;
        for (var player : players) {
            if (gameComponent.isHitman(player)) continue;
            var weight = 1f / this.vigilanteRounds.getOrDefault(player.getUuid(), 1);
            map.put(player, weight);
            total += weight;
        }
        var vigilantes = new ArrayList<ServerPlayerEntity>();
        for (var i = 0; i < hitmanCount; i++) {
            var random = world.getRandom().nextFloat() * total;
            for (var entry : map.entrySet()) {
                random -= entry.getValue();
                if (random <= 0) {
                    vigilantes.add(entry.getKey());
                    total -= entry.getValue();
                    map.remove(entry.getKey());
                    this.vigilanteRounds.put(entry.getKey().getUuid(), this.vigilanteRounds.getOrDefault(entry.getKey().getUuid(), 1) + 1);
                    break;
                }
            }
        }
        for (var player : vigilantes) {
            player.giveItemStack(new ItemStack(TMMItems.REVOLVER));
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        var hitmanRounds = new NbtList();
        for (var detail : this.hitmanRounds.entrySet()) {
            var compound = new NbtCompound();
            compound.putUuid("uuid", detail.getKey());
            compound.putInt("times", detail.getValue());
            hitmanRounds.add(compound);
        }
        tag.put("hitmanRounds", hitmanRounds);
        var vigilanteRounds = new NbtList();
        for (var detail : this.vigilanteRounds.entrySet()) {
            var compound = new NbtCompound();
            compound.putUuid("uuid", detail.getKey());
            compound.putInt("times", detail.getValue());
            vigilanteRounds.add(compound);
        }
        tag.put("vigilanteRounds", vigilanteRounds);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.hitmanRounds.clear();
        for (var element : tag.getList("hitmanRounds", 10)) {
            var compound = (NbtCompound) element;
            if (!compound.contains("uuid") || !compound.contains("times")) continue;
            this.hitmanRounds.put(compound.getUuid("uuid"), compound.getInt("times"));
        }
        this.vigilanteRounds.clear();
        for (var element : tag.getList("vigilanteRounds", 10)) {
            var compound = (NbtCompound) element;
            if (!compound.contains("uuid") || !compound.contains("times")) continue;
            this.vigilanteRounds.put(compound.getUuid("uuid"), compound.getInt("times"));
        }
    }
}