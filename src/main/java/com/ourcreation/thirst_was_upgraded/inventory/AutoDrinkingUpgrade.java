package com.ourcreation.thirst_was_upgraded.inventory;

import cn.mlus.thirst.api.ThirstHelper;
import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.content.thirst.PlayerThirst;
import cn.mlus.thirst.foundation.common.capability.ModAttachment;
import cn.mlus.thirst.foundation.util.FluidHelper;

import com.ourcreation.thirst_was_upgraded.util.WaterDrinkHelper;
import com.tiviacz.travelersbackpack.init.ModDataComponents;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tiviacz.travelersbackpack.inventory.upgrades.FilterUpgradeBase;
import com.tiviacz.travelersbackpack.inventory.upgrades.IEnable;
import com.tiviacz.travelersbackpack.inventory.upgrades.ITickableUpgrade;
import com.tiviacz.travelersbackpack.inventory.upgrades.Point;
import com.tiviacz.travelersbackpack.inventory.upgrades.tanks.TanksUpgrade;
import com.tiviacz.travelersbackpack.client.screens.BackpackScreen;
import com.tiviacz.travelersbackpack.client.screens.widgets.WidgetBase;
import com.tiviacz.travelersbackpack.inventory.upgrades.filter.FilterHandler;
import com.tiviacz.travelersbackpack.util.InventoryHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AutoDrinkingUpgrade
        extends FilterUpgradeBase<AutoDrinkingUpgrade, DrinkingFilterSettings>
        implements IEnable, ITickableUpgrade {

    /** Check once per second while idle. */
    private static final int TICK_RATE = 20;

    /** Retry quickly after a successful drink if the player is still thirsty. */
    private static final int STILL_THIRSTY_COOLDOWN = 10;

    /** One normal water bottle contains 250 mB (one quarter of a bucket). */
    private static final int WATER_PER_BOTTLE = 250;

    /** One thirst value is one half-icon on the thirst bar. */
    private static final int TANK_THIRST_PER_SIP = 1;

    /** Keep tank drain amounts aligned to the values shown by the backpack UI. */
    private static final int TANK_DRAIN_INCREMENT = 25;

    private static final int FILTER_SLOT_COUNT = 9;

    private static final int FILTER_SLOTS_IN_ROW = 3;

    private static final Point TAB_SIZE = new Point(66, 49);

    public AutoDrinkingUpgrade(
            UpgradeManager manager,
            int dataHolderSlot,
            NonNullList<ItemStack> filterItems
    ) {
        super(
                manager,
                dataHolderSlot,
                TAB_SIZE,
                FILTER_SLOT_COUNT,
                FILTER_SLOTS_IN_ROW,
                filterItems,
                List.of()
        );
    }

    @Override
    public boolean hasTagSelector() {
        return false;
    }

    @Override
    public List<Integer> getFilter() {
        return getDataHolderStack().getOrDefault(
                ModDataComponents.FILTER_SETTINGS,
                List.of(
                        DrinkingFilterSettings.BLOCK,
                        DrinkingFilterSettings.HALF_HYDRATION,
                        DrinkingFilterSettings.ALLOW_ALL_WATER
                )
        );
    }

    @Override
    public DrinkingFilterSettings createFilterSettings(
            UpgradeManager manager,
            NonNullList<ItemStack> filterItems,
            List<String> filterTags
    ) {
        return new DrinkingFilterSettings(
                manager.getWrapper().getStorage(),
                filterItems.stream()
                        .limit(getFilterSlotCount())
                        .filter(stack -> !stack.isEmpty())
                        .toList(),
                getFilter()
        );
    }

    @Override
    protected FilterHandler createFilter(NonNullList<ItemStack> items, int size) {
        return new FilterHandler(items, size) {
            @Override
            protected void onContentsChanged(int slot) {
                updateDataHolderUnchecked(
                        ModDataComponents.BACKPACK_CONTAINER.get(),
                        InventoryHelper.itemsToList(size, filter)
                );
                getFilterSettings().updateFilter(
                        getDataHolderStack()
                                .get(ModDataComponents.BACKPACK_CONTAINER)
                                .getItems()
                );
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return WaterDrinkHelper.isFilterItem(stack);
            }
        };
    }

    @Override
    public int getTickRate() {
        return TICK_RATE;
    }

    @Override
    public void tick(
            @Nullable Player player,
            Level level,
            BlockPos pos,
            int currentTick
    ) {
        if (level.isClientSide() || player == null || !isEnabled(this)) {
            return;
        }

        int cooldown = getCooldown();
        if (cooldown <= 0) {
            setCooldown(getTickRate());
            return;
        }

        if (currentTick % cooldown != 0) {
            return;
        }

        if (drinkPlayerAndGetThirsty(player, level)) {
            setCooldown(STILL_THIRSTY_COOLDOWN);
            return;
        }

        if (!hasCooldown() || getCooldown() != getTickRate()) {
            setCooldown(getTickRate());
        }
    }

    private boolean drinkPlayerAndGetThirsty(Player player, Level level) {
        if (!isPlayerThirsty(player) || level.isClientSide()) {
            return false;
        }

        BackpackWrapper wrapper = getUpgradeManager().getWrapper();
        wrapper.loadAdditionally(BackpackWrapper.STORAGE_ID);

        if (tryDrinkingItemFromStorage(player, level, wrapper)) {
            return isPlayerThirsty(player);
        }

        if (tryDrinkingFromTanks(player, wrapper)) {
            return isPlayerThirsty(player);
        }

        return false;
    }

    private boolean isPlayerThirsty(Player player) {
        PlayerThirst thirst = player.getData(ModAttachment.PLAYER_THIRST);
        return thirst != null && thirst.getThirst() < 20;
    }

    private boolean tryDrinkingItemFromStorage(
            Player player,
            Level level,
            BackpackWrapper wrapper
    ) {
        IItemHandlerModifiable storage = wrapper.getStorageForInputOutput();
        return InventoryHelper.iterate(
                storage,
                (slot, stack) -> tryDrinkingStack(player, level, slot, stack, storage)
        );
    }

    private boolean tryDrinkingStack(
            Player player,
            Level level,
            int slot,
            ItemStack stack,
            IItemHandlerModifiable storage
    ) {
        if (!canAutoDrink(player, stack)) {
            return false;
        }

        if (tryDrinkingWaterContainer(player, slot, stack, storage)) {
            return true;
        }

        return tryUsingDrinkItem(player, level, slot, stack, storage);
    }

    private boolean tryUsingDrinkItem(
            Player player,
            Level level,
            int slot,
            ItemStack stack,
            IItemHandlerModifiable storage
    ) {
        if (!canUseDrinkItem(player, stack)) {
            return false;
        }

        ItemStack singleItemCopy = stack.copyWithCount(1);
        // Complete only the stored drink. Calling use() can spill fluids or
        // leave the player's held hose as an active use item.
        ItemStack resultItem = EventHooks.onItemUseFinish(
                player,
                singleItemCopy,
                0,
                singleItemCopy.getItem().finishUsingItem(singleItemCopy, level, player)
        );

        stack.shrink(1);
        storage.setStackInSlot(slot, stack);

        if (!resultItem.isEmpty()) {
            ItemStack insertResult = InventoryHelper.addItemStackToHandler(storage, resultItem, false);
            if (!insertResult.isEmpty()) {
                player.drop(insertResult, true);
            }
        }

        return true;
    }

    private boolean tryDrinkingWaterContainer(
            Player player,
            int slot,
            ItemStack stack,
            IItemHandlerModifiable storage
    ) {
        if (!canManuallyDrinkWaterContainer(player, stack)) {
            return false;
        }

        ItemStack remainder = getEmptyWaterContainerRemainder(stack);
        if (remainder.isEmpty()) {
            return false;
        }

        PlayerThirst thirst = player.getData(ModAttachment.PLAYER_THIRST);
        if (thirst == null) {
            return false;
        }

        int purity = WaterDrinkHelper.getPurity(stack);
        if (!WaterPurity.givePurityEffects(player, purity)) {
            return false;
        }

        ItemStack hydrationSource = WaterDrinkHelper.getHydrationSource(stack, purity);
        thirst.drink(
                ThirstHelper.getThirst(hydrationSource),
                ThirstHelper.getQuenched(hydrationSource)
        );
        thirst.updateThirstData(player);

        stack.shrink(1);
        storage.setStackInSlot(slot, stack);

        ItemStack insertResult = InventoryHelper.addItemStackToHandler(storage, remainder, false);
        if (!insertResult.isEmpty()) {
            player.drop(insertResult, true);
        }

        return true;
    }

    private boolean canAutoDrink(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return false;
        }

        try {
            if (!getFilterSettings().allowsDrink(stack)) {
                return false;
            }

            if (!canUseDrinkItem(player, stack) && !canManuallyDrinkWaterContainer(player, stack)) {
                return false;
            }

            int purity = WaterDrinkHelper.getPurity(stack);
            ItemStack hydrationSource = WaterDrinkHelper.getHydrationSource(stack, purity);
            int hydration = WaterDrinkHelper.getHydration(hydrationSource);

            return getFilterSettings().shouldDrink(player, hydration)
                    && (!WaterDrinkHelper.isWaterContainer(stack) || getFilterSettings().allowsPurity(purity));
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    static int getThirst(Player player) {
        try {
            PlayerThirst thirst = player.getData(ModAttachment.PLAYER_THIRST);
            if (thirst == null) {
                return 20;
            }

            return thirst.getThirst();
        } catch (RuntimeException ignored) {
            return 20;
        }
    }

    private boolean canUseDrinkItem(Player player, ItemStack stack) {
        ItemStack hydrationSource = WaterDrinkHelper.getHydrationSource(stack, WaterDrinkHelper.getPurity(stack));

        return WaterDrinkHelper.isUsableDrink(player, stack)
                && WaterDrinkHelper.getHydration(hydrationSource) > 0;
    }

    private boolean canManuallyDrinkWaterContainer(Player player, ItemStack stack) {
        if (WaterDrinkHelper.isDurableToughAsNailsCanteen(stack)) {
            return false;
        }

        ItemStack hydrationSource = WaterDrinkHelper.getHydrationSource(stack, WaterDrinkHelper.getPurity(stack));

        return WaterDrinkHelper.isWaterContainer(player, stack)
                && WaterDrinkHelper.getHydration(hydrationSource) > 0;
    }

    private ItemStack getEmptyWaterContainerRemainder(ItemStack stack) {
        return WaterDrinkHelper.getEmptyWaterContainerRemainder(stack);
    }

    private boolean tryDrinkingFromTanks(Player player, BackpackWrapper wrapper) {
        TanksUpgrade tanks = wrapper
                .getUpgradeManager()
                .getUpgrade(TanksUpgrade.class)
                .orElse(null);

        if (tanks == null) {
            return false;
        }

        if (tryDrinkFromTank(player, tanks.getLeftTank())) {
            return true;
        }

        return tryDrinkFromTank(player, tanks.getRightTank());
    }

    private boolean tryDrinkFromTank(Player player, FluidTank tank) {
        FluidStack fluid = tank.getFluid();

        if (fluid.isEmpty()) {
            return false;
        }

        // Thirst Was Reclaimed stores purity on vanilla still or flowing water.
        if (FluidHelper.convertToStill(fluid.getFluid()) != Fluids.WATER) {
            return false;
        }

        PlayerThirst thirst = player.getData(ModAttachment.PLAYER_THIRST);

        if (thirst == null) {
            return false;
        }

        int purity = WaterPurity.getPurity(fluid);
        // Read configured drink values using a bottle with the tank's exact purity.
        ItemStack waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        WaterPurity.addPurity(waterBottle, purity);
        int hydration = ThirstHelper.getThirst(waterBottle);
        int quenched = ThirstHelper.getQuenched(waterBottle);

        if (hydration <= 0) {
            return false;
        }

        int waterPerSip = roundUpToTankDrainIncrement(
                Math.max(1, (int) Math.ceil((double) WATER_PER_BOTTLE / hydration))
        );
        if (fluid.getAmount() < waterPerSip) {
            return false;
        }

        if (!getFilterSettings().allowsTankWater(waterBottle)
                || !getFilterSettings().shouldDrink(player, TANK_THIRST_PER_SIP)
                || !getFilterSettings().allowsPurity(purity)) {
            return false;
        }

        // Purity effects may prevent restoration; in that case leave the tank alone.
        if (!WaterPurity.givePurityEffects(player, purity)) {
            return false;
        }

        thirst.drink(
                TANK_THIRST_PER_SIP,
                getQuenchedPerTankSip(hydration, quenched)
        );
        thirst.updateThirstData(player);

        tank.drain(waterPerSip, FluidAction.EXECUTE);

        return true;
    }

    private int roundUpToTankDrainIncrement(int amount) {
        return ((amount + TANK_DRAIN_INCREMENT - 1) / TANK_DRAIN_INCREMENT) * TANK_DRAIN_INCREMENT;
    }

    private int getQuenchedPerTankSip(int hydration, int quenched) {
        if (quenched <= 0) {
            return 0;
        }

        return Math.max(1, (int) Math.round((double) quenched / hydration));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public WidgetBase<BackpackScreen> createWidget(
            BackpackScreen screen,
            int x,
            int y
    ) {
        return new DrinkingWidget(
                screen,
                this,
                new Point(screen.getGuiLeft() + x, screen.getGuiTop() + y)
        );
    }
}
