package com.ourcreation.thirst_was_upgraded.compat;

import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.foundation.common.event.RegisterThirstValueEvent;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ToughAsNailsCompat {

    public static final String MOD_ID = "toughasnails";
    public static final int MB_PER_CANTEEN_USE = 250;

    private static final CanteenTier[] TIERS = {
            new CanteenTier("leather"),
            new CanteenTier("copper"),
            new CanteenTier("iron"),
            new CanteenTier("gold"),
            new CanteenTier("diamond"),
            new CanteenTier("netherite")
    };

    private ToughAsNailsCompat() {
    }

    public static void registerThirstValues(RegisterThirstValueEvent event) {
        if (!isLoaded()) {
            return;
        }

        for (CanteenTier tier : TIERS) {
            addDrink(event, tier.dirtyId(), 8, 10);
            addDrink(event, tier.waterId(), 9, 11);
            addDrink(event, tier.purifiedId(), 10, 12);
        }
    }

    public static void registerLegacyThirstValues(dev.ghen.thirst.foundation.common.event.RegisterThirstValueEvent event) {
        registerThirstValues(event);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        if (!isLoaded()) {
            return;
        }

        ItemLike[] canteens = allCanteenItems().toArray(ItemLike[]::new);
        if (canteens.length == 0) {
            return;
        }

        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new CanteenFluidHandler(stack),
                canteens
        );
    }

    public static boolean isToughAsNailsFilledCanteen(ItemStack stack) {
        return getTier(stack)
                .map(tier -> tier.getPurity(stack).isPresent())
                .orElse(false);
    }

    public static boolean isToughAsNailsCanteen(ItemStack stack) {
        return getTier(stack).isPresent();
    }

    public static ItemStack getEmptyCanteen(ItemStack stack) {
        return getTier(stack)
                .flatMap(CanteenTier::empty)
                .map(item -> stack.transmuteCopy(item, 1))
                .orElse(ItemStack.EMPTY);
    }

    private static List<Item> allCanteenItems() {
        List<Item> items = new ArrayList<>();
        for (CanteenTier tier : TIERS) {
            tier.empty().ifPresent(items::add);
            tier.dirty().ifPresent(items::add);
            tier.water().ifPresent(items::add);
            tier.purified().ifPresent(items::add);
        }
        return items;
    }

    private static void addDrink(RegisterThirstValueEvent event, ResourceLocation id, int thirst, int quenched) {
        item(id).ifPresent(item -> event.addDrink(item, thirst, quenched));
    }

    private static Optional<CanteenTier> getTier(ItemStack stack) {
        if (stack.isEmpty() || !isLoaded()) {
            return Optional.empty();
        }

        for (CanteenTier tier : TIERS) {
            if (tier.isCanteen(stack)) {
                return Optional.of(tier);
            }
        }

        return Optional.empty();
    }

    private static Optional<Item> item(ResourceLocation id) {
        return BuiltInRegistries.ITEM.getOptional(id);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    private record CanteenTier(String material) {

        private ResourceLocation emptyId() {
            return id("empty_" + material + "_canteen");
        }

        private ResourceLocation dirtyId() {
            return id(material + "_dirty_water_canteen");
        }

        private ResourceLocation waterId() {
            return id(material + "_water_canteen");
        }

        private ResourceLocation purifiedId() {
            return id(material + "_purified_water_canteen");
        }

        private Optional<Item> empty() {
            return item(emptyId());
        }

        private Optional<Item> dirty() {
            return item(dirtyId());
        }

        private Optional<Item> water() {
            return item(waterId());
        }

        private Optional<Item> purified() {
            return item(purifiedId());
        }

        private Optional<Item> filledForPurity(int purity) {
            if (purity >= 3) {
                return purified();
            }

            if (purity == 2) {
                return water();
            }

            return dirty();
        }

        private boolean isCanteen(ItemStack stack) {
            return is(stack, emptyId())
                    || is(stack, dirtyId())
                    || is(stack, waterId())
                    || is(stack, purifiedId());
        }

        private Optional<Integer> getPurity(ItemStack stack) {
            if (is(stack, dirtyId())) {
                return Optional.of(0);
            }

            if (is(stack, waterId())) {
                return Optional.of(2);
            }

            if (is(stack, purifiedId())) {
                return Optional.of(3);
            }

            return Optional.empty();
        }

        private int capacity() {
            return water()
                    .map(item -> item.getDefaultInstance().getMaxDamage() * MB_PER_CANTEEN_USE)
                    .orElse(MB_PER_CANTEEN_USE);
        }

        private static boolean is(ItemStack stack, ResourceLocation id) {
            return item(id).map(stack::is).orElse(false);
        }
    }

    private static final class CanteenFluidHandler implements IFluidHandlerItem {

        private ItemStack container;

        private CanteenFluidHandler(ItemStack container) {
            this.container = container;
        }

        @Override
        public ItemStack getContainer() {
            return container;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            if (tank != 0) {
                return FluidStack.EMPTY;
            }

            return getTier(container)
                    .flatMap(tier -> tier.getPurity(container)
                            .map(purity -> waterStack(getStoredAmount(), purity)))
                    .orElse(FluidStack.EMPTY);
        }

        @Override
        public int getTankCapacity(int tank) {
            if (tank != 0) {
                return 0;
            }

            return getTier(container)
                    .map(CanteenTier::capacity)
                    .orElse(0);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && isWater(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!isFluidValid(0, resource)) {
                return 0;
            }

            Optional<CanteenTier> optionalTier = getTier(container);
            if (optionalTier.isEmpty()) {
                return 0;
            }

            CanteenTier tier = optionalTier.get();
            int capacity = tier.capacity();
            int stored = getStoredAmount();
            int fillableUses = Math.min(resource.getAmount(), capacity - stored) / MB_PER_CANTEEN_USE;
            if (fillableUses <= 0) {
                return 0;
            }

            int filledAmount = fillableUses * MB_PER_CANTEEN_USE;
            if (action.execute()) {
                int currentPurity = tier.getPurity(container).orElse(WaterPurity.getPurity(resource));
                int resultingPurity = Math.min(currentPurity, WaterPurity.getPurity(resource));
                Item filledItem = tier.filledForPurity(resultingPurity).orElse(null);
                if (filledItem == null) {
                    return 0;
                }

                int remainingAmount = Math.min(capacity, stored + filledAmount);
                int damage = Math.max(0, capacity / MB_PER_CANTEEN_USE - remainingAmount / MB_PER_CANTEEN_USE);
                container = container.transmuteCopy(filledItem, 1);
                container.setDamageValue(damage);
                WaterPurity.addPurity(container, resultingPurity);
            }

            return filledAmount;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!isWater(resource)) {
                return FluidStack.EMPTY;
            }

            FluidStack contained = getFluidInTank(0);
            if (contained.isEmpty() || !FluidStack.isSameFluidSameComponents(contained, resource)) {
                return FluidStack.EMPTY;
            }

            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            Optional<CanteenTier> optionalTier = getTier(container);
            if (optionalTier.isEmpty()) {
                return FluidStack.EMPTY;
            }

            CanteenTier tier = optionalTier.get();
            Optional<Integer> optionalPurity = tier.getPurity(container);
            if (optionalPurity.isEmpty()) {
                return FluidStack.EMPTY;
            }

            int drainedUses = Math.min(maxDrain, getStoredAmount()) / MB_PER_CANTEEN_USE;
            if (drainedUses <= 0) {
                return FluidStack.EMPTY;
            }

            int drainedAmount = drainedUses * MB_PER_CANTEEN_USE;
            int purity = optionalPurity.get();

            if (action.execute()) {
                int remainingAmount = getStoredAmount() - drainedAmount;
                if (remainingAmount <= 0) {
                    tier.empty().ifPresent(item -> container = container.transmuteCopy(item, 1));
                } else {
                    int damage = Math.max(0, tier.capacity() / MB_PER_CANTEEN_USE - remainingAmount / MB_PER_CANTEEN_USE);
                    container.setDamageValue(damage);
                }
            }

            return waterStack(drainedAmount, purity);
        }

        private int getStoredAmount() {
            return getTier(container)
                    .flatMap(tier -> tier.getPurity(container)
                            .map(purity -> Math.max(0, tier.capacity() - container.getDamageValue() * MB_PER_CANTEEN_USE)))
                    .orElse(0);
        }

        private static boolean isWater(FluidStack stack) {
            if (stack.isEmpty()) {
                return false;
            }

            Fluid fluid = stack.getFluid();
            return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
        }

        private static FluidStack waterStack(int amount, int purity) {
            FluidStack stack = new FluidStack(Fluids.WATER, amount);
            WaterPurity.addPurity(stack, purity);
            return stack;
        }
    }
}
