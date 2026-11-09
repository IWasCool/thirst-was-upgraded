package com.ourcreation.thirst_was_upgraded.inventory;

import com.ourcreation.thirst_was_upgraded.util.WaterDrinkHelper;
import com.tiviacz.travelersbackpack.items.HoseItem;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AutoDrinkingUpgradeTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void hoseIsNeverAStoredDrinkOrWaterContainerEvenInDrinkMode() {
        ItemStack hose = mock(ItemStack.class);
        when(hose.getItem()).thenReturn(mock(HoseItem.class));
        when(hose.getUseAnimation()).thenReturn(UseAnim.DRINK);
        assertFalse(WaterDrinkHelper.isUsableDrink(mock(Player.class), hose));
        assertFalse(WaterDrinkHelper.isWaterContainer(hose));
        assertFalse(WaterDrinkHelper.isFilterItem(hose));
    }

    @Test
    void tankDrinkingRejectsLavaWithoutConsumingItOrApplyingPlayerEffects() throws Exception {
        Player player = mock(Player.class);
        var upgrade = mock(AutoDrinkingUpgrade.class, CALLS_REAL_METHODS);
        FluidTank lava = new FluidTank(1000);
        lava.setFluid(new FluidStack(Fluids.LAVA, 1000));
        var method = AutoDrinkingUpgrade.class.getDeclaredMethod("tryDrinkFromTank", Player.class, FluidTank.class);
        method.setAccessible(true);
        assertEquals(false, method.invoke(upgrade, player, lava));
        assertEquals(1000, lava.getFluidAmount());
        verifyNoInteractions(player);
    }

    @Test
    void automaticDrinkNeverRightClicksOrChangesTheHeldItem() throws Exception {
        Player player = mock(Player.class);
        Level level = mock(Level.class);
        ItemStack stored = mock(ItemStack.class);
        ItemStack single = mock(ItemStack.class);
        Item drink = mock(Item.class);
        var storage = mock(IItemHandlerModifiable.class);
        var upgrade = mock(AutoDrinkingUpgrade.class, CALLS_REAL_METHODS);
        when(stored.copyWithCount(1)).thenReturn(single);
        when(single.getItem()).thenReturn(drink);
        when(drink.finishUsingItem(single, level, player)).thenReturn(ItemStack.EMPTY);

        try (var helper = mockStatic(WaterDrinkHelper.class);
             var events = mockStatic(EventHooks.class)) {
            helper.when(() -> WaterDrinkHelper.getHydrationSource(stored, 0)).thenReturn(stored);
            helper.when(() -> WaterDrinkHelper.isUsableDrink(player, stored)).thenReturn(true);
            helper.when(() -> WaterDrinkHelper.getHydration(stored)).thenReturn(6);
            events.when(() -> EventHooks.onItemUseFinish(player, single, 0, ItemStack.EMPTY))
                    .thenReturn(ItemStack.EMPTY);
            var method = AutoDrinkingUpgrade.class.getDeclaredMethod("tryUsingDrinkItem",
                    Player.class, Level.class, int.class, ItemStack.class, IItemHandlerModifiable.class);
            method.setAccessible(true);
            assertEquals(true, method.invoke(upgrade, player, level, 0, stored, storage));
        }

        verify(single, never()).use(any(), any(), any());
        verifyNoInteractions(player);
        verify(drink).finishUsingItem(single, level, player);
        verify(stored).shrink(1);
        verify(storage).setStackInSlot(0, stored);
    }
}
