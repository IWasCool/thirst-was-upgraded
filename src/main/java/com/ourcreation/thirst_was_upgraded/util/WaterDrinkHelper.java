package com.ourcreation.thirst_was_upgraded.util;

import cn.mlus.thirst.api.ThirstHelper;
import cn.mlus.thirst.content.purity.WaterPurity;

import com.ourcreation.thirst_was_upgraded.compat.CreateCompat;
import com.ourcreation.thirst_was_upgraded.compat.ToughAsNailsCompat;
import com.tiviacz.travelersbackpack.items.HoseItem;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public final class WaterDrinkHelper {

    private WaterDrinkHelper() {
    }

    public static boolean isAutoDrinkable(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        try {
            return isUsableDrink(player, stack) || isWaterContainer(player, stack);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static boolean isFilterItem(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof HoseItem) {
            return false;
        }

        if (CreateCompat.isBuildersTea(stack)) {
            CreateCompat.ensureBuildersTeaThirstValue();
            return true;
        }

        try {
            return ThirstHelper.itemRestoresThirst(stack)
                    || ThirstHelper.isDrink(stack)
                    || isWaterContainer(stack);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static boolean isUsableDrink(Player player, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof HoseItem
                || stack.getUseAnimation() != UseAnim.DRINK) {
            return false;
        }

        if (CreateCompat.isBuildersTea(stack)) {
            CreateCompat.ensureBuildersTeaThirstValue();
            return ThirstHelper.playerRestoresThirst(stack, player)
                    && getHydration(stack) > 0;
        }

        ItemStack hydrationSource = getHydrationSource(stack, getPurity(stack));

        return ThirstHelper.itemRestoresThirst(stack)
                && ThirstHelper.playerRestoresThirst(stack, player)
                && ThirstHelper.isDrink(stack)
                && (!isWaterContainer(stack) || isDurableToughAsNailsCanteen(stack))
                && getHydration(hydrationSource) > 0;
    }

    public static boolean isWaterContainer(Player player, ItemStack stack) {
        ItemStack hydrationSource = getHydrationSource(stack, getPurity(stack));

        return isWaterContainer(stack)
                && ThirstHelper.playerRestoresThirst(hydrationSource, player)
                && !hydrationSource.isEmpty()
                && getHydration(hydrationSource) > 0;
    }

    public static boolean isWaterContainer(ItemStack stack) {
        return !stack.isEmpty() && !(stack.getItem() instanceof HoseItem)
                && (WaterPurity.isWaterFilledContainer(stack) || stack.is(Items.WATER_BUCKET));
    }

    public static boolean isDurableToughAsNailsCanteen(ItemStack stack) {
        return ToughAsNailsCompat.isToughAsNailsFilledCanteen(stack);
    }

    public static ItemStack getHydrationSource(ItemStack stack, int purity) {
        if (CreateCompat.isBuildersTea(stack)) {
            CreateCompat.ensureBuildersTeaThirstValue();
            return stack;
        }

        if (ThirstHelper.itemRestoresThirst(stack)) {
            return stack;
        }

        ItemStack waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        WaterPurity.addPurity(waterBottle, purity);
        return waterBottle;
    }

    public static int getHydration(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        if (CreateCompat.isBuildersTea(stack)) {
            CreateCompat.ensureBuildersTeaThirstValue();
            return CreateCompat.getBuildersTeaThirst();
        }

        return ThirstHelper.getThirst(stack);
    }

    public static int getQuenched(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        if (CreateCompat.isBuildersTea(stack)) {
            CreateCompat.ensureBuildersTeaThirstValue();
            return CreateCompat.getBuildersTeaQuenched();
        }

        return ThirstHelper.getQuenched(stack);
    }

    public static int getPurity(ItemStack stack) {
        if (WaterPurity.hasPurity(stack)) {
            return WaterPurity.getPurity(stack);
        }

        return WaterPurity.getPurity(stack);
    }

    public static boolean isPureEnough(ItemStack stack) {
        if (!isWaterContainer(stack)) {
            return true;
        }

        return getPurity(stack) >= 3;
    }

    public static ItemStack getEmptyWaterContainerRemainder(ItemStack stack) {
        Item item = stack.getItem();

        if (item == Items.WATER_BUCKET) {
            return new ItemStack(Items.BUCKET);
        }

        if (item == Items.POTION) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }

        if (ToughAsNailsCompat.isToughAsNailsCanteen(stack)) {
            return ToughAsNailsCompat.getEmptyCanteen(stack);
        }

        if (item.hasCraftingRemainingItem()) {
            return new ItemStack(item.getCraftingRemainingItem());
        }

        return ItemStack.EMPTY;
    }
}
