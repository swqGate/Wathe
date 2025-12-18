package dev.doctor4t.wathe.client.render.item;

import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.index.WatheCosmetics;
import dev.doctor4t.wathe.item.KnifeItem;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class KnifeDynamicItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    public static final List<Identifier> MODELS_TO_REGISTER = new ArrayList<>();

    public static final Pair<ModelIdentifier, ModelIdentifier> DEFAULT_MODEL_IDENTIFIER = registerVariantModelPair("");
    public static final Pair<ModelIdentifier, ModelIdentifier> CEREMONIAL_MODEL_IDENTIFIER = registerVariantModelPair(KnifeItem.Skin.CEREMONIAL.getName());
    public static final Pair<ModelIdentifier, ModelIdentifier> PICK_MODEL_IDENTIFIER = registerVariantModelPair(KnifeItem.Skin.PICK.getName());

    private static @NotNull Pair<ModelIdentifier, ModelIdentifier> registerVariantModelPair(String name) {
        String s = "item/knife" + (name.isEmpty() ? "" : "_") + name;

        ModelIdentifier inventoryModelIdentifier = new ModelIdentifier(Wathe.id(s + "_inventory"), "inventory");
        ModelIdentifier inHandModelIdentifier = new ModelIdentifier(Wathe.id(s + "_in_hand"), "inventory");

        MODELS_TO_REGISTER.add(inventoryModelIdentifier.id());
        MODELS_TO_REGISTER.add(inHandModelIdentifier.id());

        return new Pair<>(inventoryModelIdentifier, inHandModelIdentifier);
    }

    private static @NotNull Pair<ModelIdentifier, ModelIdentifier> getModelIdentifierModelIdentifierPair(ItemStack stack) {
        Pair<ModelIdentifier, ModelIdentifier> modelIdentifierPair = DEFAULT_MODEL_IDENTIFIER;
        KnifeItem.Skin skin = KnifeItem.Skin.fromString(WatheCosmetics.getSkin(stack));
        if (skin != null) {
            switch (skin) {
                case CEREMONIAL -> modelIdentifierPair = CEREMONIAL_MODEL_IDENTIFIER;
                case PICK -> modelIdentifierPair = PICK_MODEL_IDENTIFIER;
            }
        }
        return modelIdentifierPair;
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        boolean inHand = mode.isFirstPerson() || mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND || mode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND || mode == ModelTransformationMode.HEAD || mode == ModelTransformationMode.FIXED;
        boolean inInventory = mode == ModelTransformationMode.GUI;

        matrices.push();
        matrices.translate(.5, .5, .5);

        Pair<ModelIdentifier, ModelIdentifier> modelIdentifierPair = getModelIdentifierModelIdentifierPair(stack);
        BakedModel model = MinecraftClient.getInstance().getBakedModelManager().getModel(!inHand ? modelIdentifierPair.getLeft() : modelIdentifierPair.getRight());

        if (inInventory) {
            DiffuseLighting.disableGuiDepthLighting();
        }

        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, model);
        if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }

        if (inInventory) {
            DiffuseLighting.enableGuiDepthLighting();
        }

        matrices.pop();
    }
}
