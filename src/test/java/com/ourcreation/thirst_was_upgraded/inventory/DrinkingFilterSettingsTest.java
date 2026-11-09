package com.ourcreation.thirst_was_upgraded.inventory;

import com.ourcreation.thirst_was_upgraded.util.WaterDrinkHelper;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DrinkingFilterSettingsTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void emptyBlockListPreservesExistingBehaviorAndEmptyAllowListDisablesDrinks() {
        ItemStack water = waterBottle();
        assertTrue(settings(DrinkingFilterSettings.BLOCK, List.of()).allowsDrink(water));
        assertFalse(settings(DrinkingFilterSettings.ALLOW, List.of()).allowsDrink(water));
        assertTrue(settings(DrinkingFilterSettings.BLOCK, List.of()).allowsTankWater(water));
        assertFalse(settings(DrinkingFilterSettings.ALLOW, List.of()).allowsTankWater(water));
    }

    @Test
    void allowAndBlockModesMatchComponentsAndIgnoreStackCount() {
        ItemStack listed = waterBottle();
        ItemStack otherPotion = PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);
        var allow = settings(DrinkingFilterSettings.ALLOW, List.of(listed));
        var block = settings(DrinkingFilterSettings.BLOCK, List.of(listed));
        assertTrue(allow.allowsDrink(listed.copyWithCount(4)));
        assertFalse(block.allowsDrink(listed));
        assertFalse(allow.allowsDrink(otherPotion));
        assertTrue(block.allowsDrink(otherPotion));
    }

    @Test
    void changingSavedSettingsAndGhostSlotsImmediatelyUpdatesMatching() {
        var filter = settings(DrinkingFilterSettings.BLOCK, List.of());
        ItemStack water = waterBottle();
        filter.updateFilter(List.of(water));
        assertFalse(filter.allowsDrink(water));
        filter.updateSettings(List.of(DrinkingFilterSettings.ALLOW, 1, 1));
        assertTrue(filter.allowsDrink(water));
        filter.updateFilter(List.of(ItemStack.EMPTY));
        assertFalse(filter.allowsDrink(water));
    }

    @Test
    void waterBottleFiltersAlsoApplyToTankWater() {
        ItemStack water = waterBottle();
        var filter = settings(DrinkingFilterSettings.ALLOW, List.of(water));
        assertTrue(filter.allowsTankWater(water));
        ItemStack differentPurity = water.copy();
        differentPurity.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(2));
        assertFalse(filter.allowsTankWater(differentPurity));
    }

    @Test
    void waterBucketFiltersMatchTankWaterOfTheSamePurity() {
        ItemStack bucket = new ItemStack(Items.WATER_BUCKET);
        ItemStack water = waterBottle();
        try (var helper = mockStatic(WaterDrinkHelper.class)) {
            helper.when(() -> WaterDrinkHelper.getPurity(bucket)).thenReturn(3);
            helper.when(() -> WaterDrinkHelper.getPurity(water)).thenReturn(3);
            var filter = settings(DrinkingFilterSettings.ALLOW, List.of(bucket));
            assertTrue(filter.allowsTankWater(water));
            helper.when(() -> WaterDrinkHelper.getPurity(water)).thenReturn(0);
            assertFalse(filter.allowsTankWater(water));
        }
    }

    @Test
    void legacyMissingSettingsUseBlockListAndPurityDefaults() {
        var filter = new DrinkingFilterSettings(new ItemStackHandler(1), List.of(), List.of());
        assertTrue(filter.allowsDrink(waterBottle()));
        assertTrue(filter.allowsPurity(0));
        filter.updateSettings(List.of(1, 1, DrinkingFilterSettings.PURE_ONLY));
        assertFalse(filter.allowsPurity(2));
        assertTrue(filter.allowsPurity(3));
    }

    private static DrinkingFilterSettings settings(int mode, List<ItemStack> items) {
        return new DrinkingFilterSettings(new ItemStackHandler(1), items, List.of(mode, 1, 1));
    }

    private static ItemStack waterBottle() {
        return PotionContents.createItemStack(Items.POTION, Potions.WATER);
    }
}
