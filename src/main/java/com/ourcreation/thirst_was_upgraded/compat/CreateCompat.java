package com.ourcreation.thirst_was_upgraded.compat;

import cn.mlus.thirst.api.ThirstHelper;
import cn.mlus.thirst.foundation.common.event.RegisterThirstValueEvent;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import java.util.Optional;

public final class CreateCompat {

    public static final String MOD_ID = "create";

    private static final ResourceLocation BUILDERS_TEA_ID = id("builders_tea");
    private static final int WATER_BOTTLE_THIRST = 6;
    private static final int WATER_BOTTLE_QUENCHED = 8;

    private CreateCompat() {
    }

    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        if (!isLoaded()) {
            return;
        }

        buildersTea().ifPresent(item -> event.modify(item, builder -> builder.set(
                DataComponents.FOOD,
                new FoodProperties.Builder()
                        .nutrition(0)
                        .saturationModifier(0.0F)
                        .alwaysEdible()
                        .effect(CreateCompat::buildersTeaEffect, 1.0F)
                        .build()
        )));
    }

    public static void registerThirstValues(RegisterThirstValueEvent event) {
        if (!isLoaded()) {
            return;
        }

        ensureBuildersTeaThirstValue();
    }

    public static void registerLegacyThirstValues(dev.ghen.thirst.foundation.common.event.RegisterThirstValueEvent event) {
        if (!isLoaded()) {
            return;
        }

        ensureLegacyBuildersTeaThirstValue();
    }

    public static boolean isBuildersTea(ItemStack stack) {
        if (stack.isEmpty() || !isLoaded()) {
            return false;
        }

        return buildersTea()
                .map(stack::is)
                .orElse(false);
    }

    public static void ensureBuildersTeaThirstValue() {
        if (!isLoaded()) {
            return;
        }

        buildersTea().ifPresent(item -> {
            ThirstHelper.VALID_FOODS.remove(item);
            ThirstHelper.VALID_DRINKS.put(item, new Number[] { WATER_BOTTLE_THIRST, WATER_BOTTLE_QUENCHED });
        });
    }

    public static int getBuildersTeaThirst() {
        return WATER_BOTTLE_THIRST;
    }

    public static int getBuildersTeaQuenched() {
        return WATER_BOTTLE_QUENCHED;
    }

    private static void ensureLegacyBuildersTeaThirstValue() {
        buildersTea().ifPresent(item -> {
            dev.ghen.thirst.api.ThirstHelper.VALID_FOODS.remove(item);
            dev.ghen.thirst.api.ThirstHelper.VALID_DRINKS.put(item, new Number[] { WATER_BOTTLE_THIRST, WATER_BOTTLE_QUENCHED });
        });
    }

    private static MobEffectInstance buildersTeaEffect() {
        return new MobEffectInstance(MobEffects.DIG_SPEED, 3600, 0, false, false, false);
    }

    private static Optional<Item> buildersTea() {
        return BuiltInRegistries.ITEM.getOptional(BUILDERS_TEA_ID);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }
}
