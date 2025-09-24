package dev.doctor4t.trainmurdermystery.client.gui.screen.ingame;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.TMMComponents;
import dev.doctor4t.trainmurdermystery.client.gui.StoreRenderer;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import dev.doctor4t.trainmurdermystery.util.StoreBuyPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class LimitedInventoryScreen extends LimitedHandledScreen<PlayerScreenHandler> {
    public static final Identifier BACKGROUND_TEXTURE = TMM.id("textures/gui/container/limited_inventory.png");
    public final ClientPlayerEntity player;

    public LimitedInventoryScreen(@NotNull ClientPlayerEntity player) {
        super(player.playerScreenHandler, player.getInventory(), Text.empty());
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();
        if (!TMMComponents.GAME.get(this.player.getWorld()).isHitman(this.player)) return;
        var entries = GameConstants.SHOP_ENTRIES;
        var x = this.width / 2 - entries.size() * 36 / 2 + 9;
        var y = this.y - 46;
        for (var i = 0; i < entries.size(); i++) this.addDrawableChild(new StoreItemWidget(this, x + 36 * i, y, entries.get(i), i));
    }

    @Override
    protected void drawBackground(@NotNull DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        StoreRenderer.renderHud(this.textRenderer, this.player, context, delta);
    }

    public static class StoreItemWidget extends ButtonWidget {
        public static final Identifier SHOP_TEXTURE = TMM.id("gui/shop_slot");
        public final LimitedInventoryScreen screen;
        public final ShopEntry entry;

        public StoreItemWidget(LimitedInventoryScreen screen, int x, int y, @NotNull ShopEntry entry, int index) {
            super(x, y, 16, 16, entry.stack().getName(), (a) -> ClientPlayNetworking.send(new StoreBuyPayload(index)), DEFAULT_NARRATION_SUPPLIER);
            this.screen = screen;
            this.entry = entry;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            context.drawGuiTexture(SHOP_TEXTURE, this.getX() - 7, this.getY() - 7, 30, 30);
            context.drawItem(this.entry.stack(), this.getX(), this.getY());
            if (this.isHovered()) {
                this.screen.renderLimitedInventoryTooltip(context, this.entry.stack());
                drawSlotHighlight(context, this.getX(), this.getY(), 0);
            }
            var price = Text.literal(this.entry.price() + "€");
            context.drawTooltip(this.screen.textRenderer, price, this.getX() - 4 - this.screen.textRenderer.getWidth(price) / 2, this.getY() - 9);
        }

        @Override
        public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {}
    }
}