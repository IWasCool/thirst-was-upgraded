package com.ourcreation.thirst_was_upgraded.inventory;

import com.tiviacz.travelersbackpack.client.screens.BackpackScreen;
import com.tiviacz.travelersbackpack.client.screens.widgets.FilterUpgradeWidgetBase;
import com.tiviacz.travelersbackpack.inventory.upgrades.Point;
import com.tiviacz.travelersbackpack.inventory.upgrades.filter.ButtonStates;
import com.tiviacz.travelersbackpack.inventory.upgrades.filter.FilterButton;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class DrinkingWidget extends FilterUpgradeWidgetBase<DrinkingWidget, AutoDrinkingUpgrade> {

    private static final ResourceLocation THIRST_ICONS = ResourceLocation.fromNamespaceAndPath(
            "thirst",
            "textures/gui/thirst_icons.png"
    );

    private static final int BUTTON_SIZE = 18;
    private static final int BUTTON_BORDER = 2;
    private static final int BACKPACK_ATLAS_SIZE = 256;
    private static final int WATER_ICON_SIZE = 9;
    private static final int WATER_ICON_OFFSET = (BUTTON_SIZE - WATER_ICON_SIZE) / 2;
    private static final List<Component> ALLOW_MODE_TOOLTIPS = List.of(
            Component.translatable("screen.thirst_was_upgraded.filter_allow_drinks"),
            Component.translatable("screen.thirst_was_upgraded.filter_block_drinks")
    );

    private static final List<Component> THIRST_MODE_TOOLTIPS = List.of(
            Component.translatable("screen.thirst_was_upgraded.filter_always_drink"),
            Component.translatable("screen.thirst_was_upgraded.filter_half_hydration"),
            Component.translatable("screen.thirst_was_upgraded.filter_full_hydration")
    );

    private static final List<Component> PURITY_TOOLTIPS = List.of(
            Component.translatable("screen.thirst_was_upgraded.filter_pure_only"),
            Component.translatable("screen.thirst_was_upgraded.filter_allow_all_water")
    );

    public DrinkingWidget(BackpackScreen screen, AutoDrinkingUpgrade upgrade, Point pos) {
        super(screen, upgrade, pos, new Point(137, 0), "screen.thirst_was_upgraded.drinking_upgrade");

        addFilterButton(new FilterButton<>(
                this,
                upgrade.getFilter().get(DrinkingFilterSettings.ALLOW_MODE),
                ButtonStates.ALLOW_FEEDING,
                new Point(pos.x() + 6, pos.y() + 22)
        ));
        addFilterButton(new DrinkingFilterButton(
                this,
                upgrade.getFilter().get(DrinkingFilterSettings.THIRST_MODE),
                ButtonStates.HUNGER_MODE,
                new Point(pos.x() + 6 + BUTTON_SIZE, pos.y() + 22),
                0
        ));
        addFilterButton(new DrinkingFilterButton(
                this,
                upgrade.getFilter().get(DrinkingFilterSettings.PURITY_MODE),
                ButtonStates.IGNORE_EFFECT_MODE,
                new Point(pos.x() + 6 + 2 * BUTTON_SIZE, pos.y() + 22),
                1
        ));
    }

    @Override
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        if (!isTabOpened()) {
            return;
        }

        renderButtonTooltip(guiGraphics, mouseX, mouseY, ButtonStates.ALLOW_FEEDING, ALLOW_MODE_TOOLTIPS);
        renderButtonTooltip(guiGraphics, mouseX, mouseY, ButtonStates.HUNGER_MODE, THIRST_MODE_TOOLTIPS);
        renderButtonTooltip(guiGraphics, mouseX, mouseY, ButtonStates.IGNORE_EFFECT_MODE, PURITY_TOOLTIPS);
    }

    private void renderButtonTooltip(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            ButtonStates.ButtonState state,
            List<Component> tooltips
    ) {
        FilterButton<DrinkingWidget> button = getFilterButton(state);
        if (button.isMouseOver(mouseX, mouseY)) {
            guiGraphics.renderTooltip(screen.getFont(), tooltips.get(button.getCurrentState()), mouseX, mouseY);
        }
    }

    private static class DrinkingFilterButton extends FilterButton<DrinkingWidget> {
        private final Point pos;
        private final int iconColumn;

        private DrinkingFilterButton(
                DrinkingWidget widget,
                int currentState,
                ButtonStates.ButtonState state,
                Point pos,
                int iconColumn
        ) {
            super(widget, currentState, state, pos);
            this.pos = pos;
            this.iconColumn = iconColumn;
        }

        @Override
        public void renderButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            // Borrow the same filter-button skin as the allow/block control.
            // Its two-pixel frame and blank inner corner contain no filter symbol.
            Point background = ButtonStates.ALLOW_FEEDING.getButtonIcon(0);
            renderButtonFrame(guiGraphics, background);

            // Keep this blit in renderButton: older Create New World Core builds
            // inject here. Sampling below row zero leaves their icon-only hook inert.
            guiGraphics.blit(
                    BackpackScreen.ICONS,
                    pos.x() + BUTTON_BORDER,
                    pos.y() + BUTTON_BORDER,
                    BUTTON_SIZE - 2 * BUTTON_BORDER,
                    BUTTON_SIZE - 2 * BUTTON_BORDER,
                    (float) background.x() + BUTTON_BORDER,
                    (float) background.y() + BUTTON_BORDER,
                    1,
                    1,
                    BACKPACK_ATLAS_SIZE,
                    BACKPACK_ATLAS_SIZE
            );

            renderWaterIcon(guiGraphics);

            if (isMouseOver(mouseX, mouseY)) {
                guiGraphics.blit(BackpackScreen.ICONS, pos.x(), pos.y(), 24, 18, BUTTON_SIZE, BUTTON_SIZE);
            }
        }

        private void renderButtonFrame(GuiGraphics guiGraphics, Point background) {
            int innerSize = BUTTON_SIZE - 2 * BUTTON_BORDER;
            int farEdge = BUTTON_SIZE - BUTTON_BORDER;
            guiGraphics.blit(BackpackScreen.ICONS, pos.x(), pos.y(),
                    background.x(), background.y(), BUTTON_SIZE, BUTTON_BORDER);
            guiGraphics.blit(BackpackScreen.ICONS, pos.x(), pos.y() + farEdge,
                    background.x(), background.y() + farEdge, BUTTON_SIZE, BUTTON_BORDER);
            guiGraphics.blit(BackpackScreen.ICONS, pos.x(), pos.y() + BUTTON_BORDER,
                    background.x(), background.y() + BUTTON_BORDER, BUTTON_BORDER, innerSize);
            guiGraphics.blit(BackpackScreen.ICONS, pos.x() + farEdge, pos.y() + BUTTON_BORDER,
                    background.x() + farEdge, background.y() + BUTTON_BORDER, BUTTON_BORDER, innerSize);
        }

        private void renderWaterIcon(GuiGraphics guiGraphics) {
            // The HUD atlas contains empty, half-full, and full drops at U=0/8/16.
            // Sampling it directly also picks up thirst-bar resource packs.
            int iconU = iconColumn == 0 ? (2 - getCurrentState()) * 8 : 16;
            boolean allowImpureWater = iconColumn == 1
                    && getCurrentState() == DrinkingFilterSettings.ALLOW_ALL_WATER;

            if (allowImpureWater) {
                guiGraphics.setColor(1.0F, 1.0F, 0.35F, 1.0F);
            }
            try {
                guiGraphics.blit(
                        THIRST_ICONS,
                        pos.x() + WATER_ICON_OFFSET,
                        pos.y() + WATER_ICON_OFFSET,
                        (float) iconU,
                        0.0F,
                        WATER_ICON_SIZE,
                        WATER_ICON_SIZE,
                        25,
                        9
                );
            } finally {
                if (allowImpureWater) {
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }
    }
}
