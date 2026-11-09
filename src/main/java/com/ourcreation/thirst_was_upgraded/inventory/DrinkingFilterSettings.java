package com.ourcreation.thirst_was_upgraded.inventory;

import com.ourcreation.thirst_was_upgraded.util.WaterDrinkHelper;
import com.tiviacz.travelersbackpack.inventory.upgrades.FilterSettingsBase;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

public class DrinkingFilterSettings extends FilterSettingsBase {

    public static final int ALLOW_MODE = 0;
    public static final int THIRST_MODE = 1;
    public static final int PURITY_MODE = 2;

    public static final int ALLOW = 0;
    public static final int BLOCK = 1;

    public static final int ALWAYS_DRINK = 0;
    public static final int HALF_HYDRATION = 1;
    public static final int FULL_HYDRATION = 2;

    public static final int PURE_ONLY = 0;
    public static final int ALLOW_ALL_WATER = 1;

    public DrinkingFilterSettings(ItemStackHandler storage, List<ItemStack> filterItems, List<Integer> filterSettings) {
        super(storage, filterItems, filterSettings, 9);
    }

    @Override
    public boolean matchesFilter(Player player, ItemStack stack) {
        if (!WaterDrinkHelper.isAutoDrinkable(player, stack)) {
            return false;
        }

        return allowsDrink(stack)
                && compareThirstLevel(player, stack)
                && comparePurity(stack);
    }

    public boolean allowsDrink(ItemStack stack) {
        boolean listed = filterItems.stream()
                .anyMatch(filter -> ItemStack.isSameItemSameComponents(filter, stack));
        return allowsListedDrink(listed);
    }

    /** Tank water is represented in the filter by a water bottle or water bucket. */
    public boolean allowsTankWater(ItemStack waterBottle) {
        boolean listed = filterItems.stream().anyMatch(filter ->
                ItemStack.isSameItemSameComponents(filter, waterBottle)
                        || (filter.is(Items.WATER_BUCKET)
                        && WaterDrinkHelper.getPurity(filter) == WaterDrinkHelper.getPurity(waterBottle)));
        return allowsListedDrink(listed);
    }

    private boolean allowsListedDrink(boolean listed) {
        return switch (getSetting(ALLOW_MODE, BLOCK)) {
            case ALLOW -> listed;
            case BLOCK -> !listed;
            default -> false;
        };
    }

    public boolean compareThirstLevel(Player player, ItemStack stack) {
        int hydration = WaterDrinkHelper.getHydration(
                WaterDrinkHelper.getHydrationSource(stack, WaterDrinkHelper.getPurity(stack))
        );

        return shouldDrink(player, hydration);
    }

    public boolean shouldDrink(Player player, int hydration) {
        int mode = getSetting(THIRST_MODE, HALF_HYDRATION);
        if (mode < ALWAYS_DRINK || mode > FULL_HYDRATION) {
            mode = HALF_HYDRATION;
        }

        int overflow = AutoDrinkingUpgrade.getThirst(player) + hydration - 20;

        if (mode == ALWAYS_DRINK) {
            return hydration > 0 && AutoDrinkingUpgrade.getThirst(player) < 20;
        }

        if (mode == HALF_HYDRATION) {
            return hydration > 0 && overflow <= hydration / 2;
        }

        if (mode == FULL_HYDRATION) {
            return hydration > 0 && overflow <= 0;
        }

        return false;
    }

    private boolean comparePurity(ItemStack stack) {
        if (!WaterDrinkHelper.isWaterContainer(stack)) {
            return true;
        }

        return allowsPurity(WaterDrinkHelper.getPurity(stack));
    }

    public boolean allowsPurity(int purity) {
        int mode = getSetting(PURITY_MODE, ALLOW_ALL_WATER);
        if (mode != PURE_ONLY && mode != ALLOW_ALL_WATER) {
            mode = ALLOW_ALL_WATER;
        }

        return mode == ALLOW_ALL_WATER || purity >= 3;
    }

    private int getSetting(int index, int fallback) {
        if (filterSettings.size() <= index) {
            return fallback;
        }

        return filterSettings.get(index);
    }
}
