package dev.doctor4t.wathe.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class MapVariablesCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("wathe:mapVariables")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("help")
                                .executes(
                                        context -> sendHelp(
                                                context.getSource()
                                        )
                                )
                        )
                        .then(CommandManager.literal("set")
                                .then(CommandManager.literal("spawnPosition")
                                        .then(CommandManager.argument("location", Vec3ArgumentType.vec3())
                                                .then(CommandManager.argument("rotation", RotationArgumentType.rotation())
                                                        .executes(
                                                                context -> setPosWithOrientation(
                                                                        context.getSource(), "spawnPosition",
                                                                        Vec3ArgumentType.getPosArgument(context, "location"),
                                                                        RotationArgumentType.getRotation(context, "rotation"),
                                                                        posWithOrientation -> getMapVarsComponent(context).setSpawnPos(posWithOrientation)
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(CommandManager.literal("spectatorSpawnPosition")
                                        .then(CommandManager.argument("location", Vec3ArgumentType.vec3())
                                                .then(CommandManager.argument("rotation", RotationArgumentType.rotation())
                                                        .executes(
                                                                context -> setPosWithOrientation(
                                                                        context.getSource(), "spectatorSpawnPosition",
                                                                        Vec3ArgumentType.getPosArgument(context, "location"),
                                                                        RotationArgumentType.getRotation(context, "rotation"),
                                                                        posWithOrientation -> getMapVarsComponent(context).setSpectatorSpawnPos(posWithOrientation)
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(CommandManager.literal("readyArea")
                                        .then(CommandManager.argument("from", Vec3ArgumentType.vec3())
                                                .then(CommandManager.argument("to", Vec3ArgumentType.vec3())
                                                        .executes(
                                                                context -> setBox(
                                                                        context.getSource(), "readyArea",
                                                                        Vec3ArgumentType.getPosArgument(context, "from"),
                                                                        Vec3ArgumentType.getPosArgument(context, "to"),
                                                                        box -> getMapVarsComponent(context).setReadyArea(box)
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(CommandManager.literal("playAreaOffset")
                                        .then(CommandManager.argument("offset", Vec3ArgumentType.vec3(true))
                                                .executes(
                                                        context -> setValue(
                                                                context.getSource(), "playAreaOffset",
                                                                BlockPos.ofFloored(Vec3ArgumentType.getVec3(context, "offset")),
                                                                blockPos -> getMapVarsComponent(context).setPlayAreaOffset(blockPos)
                                                        )
                                                )
                                        )
                                )
                                .then(CommandManager.literal("playArea")
                                        .then(CommandManager.argument("from", Vec3ArgumentType.vec3())
                                                .then(CommandManager.argument("to", Vec3ArgumentType.vec3())
                                                        .executes(
                                                                context -> setBox(
                                                                        context.getSource(), "playArea",
                                                                        Vec3ArgumentType.getPosArgument(context, "from"),
                                                                        Vec3ArgumentType.getPosArgument(context, "to"),
                                                                        box -> getMapVarsComponent(context).setPlayArea(box)
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(CommandManager.literal("resetTemplateArea")
                                        .then(CommandManager.argument("from", Vec3ArgumentType.vec3())
                                                .then(CommandManager.argument("to", Vec3ArgumentType.vec3())
                                                        .executes(
                                                                context -> setBox(
                                                                        context.getSource(), "resetTemplateArea",
                                                                        Vec3ArgumentType.getPosArgument(context, "from"),
                                                                        Vec3ArgumentType.getPosArgument(context, "to"),
                                                                        box -> getMapVarsComponent(context).setResetTemplateArea(box)
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(CommandManager.literal("resetPasteOffset")
                                        .then(CommandManager.argument("offset", Vec3ArgumentType.vec3(true))
                                                .executes(
                                                        context -> setValue(
                                                                context.getSource(), "resetPasteOffset",
                                                                BlockPos.ofFloored(Vec3ArgumentType.getVec3(context, "offset")),
                                                                blockPos -> getMapVarsComponent(context).setResetPasteOffset(blockPos)
                                                        )
                                                )
                                        )
                                )
                        )
        );
    }

    private static @NotNull MapVariablesWorldComponent getMapVarsComponent(CommandContext<ServerCommandSource> context) {
        return MapVariablesWorldComponent.KEY.get(context.getSource().getWorld());
    }

    private static int sendHelp(ServerCommandSource source) {
        source.sendMessage(Text.translatable("wathe.map_variables.help"));
        return 1;
    }

    private static <T> int setValue(ServerCommandSource source, String valueName, T value, Consumer<T> consumer) {
        consumer.accept(value);
        source.sendMessage(Text.translatable("wathe.map_variables.set", valueName, value));
        return 1;
    }

    private static int setPosWithOrientation(ServerCommandSource source, String valueName, PosArgument location, PosArgument rotation, Consumer<MapVariablesWorldComponent.PosWithOrientation> consumer) {
        Vec3d absolutePos = location.toAbsolutePos(source);
        Vec2f absoluteRotation = rotation.toAbsoluteRotation(source);
        setValue(source, valueName, new MapVariablesWorldComponent.PosWithOrientation(absolutePos, absoluteRotation.y, absoluteRotation.x), consumer);
        return 1;
    }

    private static int setBox(ServerCommandSource source, String valueName, PosArgument from, PosArgument to, Consumer<Box> consumer) {
        setValue(source, valueName, new Box(from.toAbsolutePos(source), to.toAbsolutePos(source)), consumer);
        return 1;
    }

}
