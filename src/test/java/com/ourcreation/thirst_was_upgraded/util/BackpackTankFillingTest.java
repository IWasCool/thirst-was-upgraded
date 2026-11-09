package com.ourcreation.thirst_was_upgraded.util;

import com.tiviacz.travelersbackpack.inventory.FluidTankItemWrapper;
import com.tiviacz.travelersbackpack.inventory.upgrades.tanks.TanksUpgrade;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BackpackTankFillingTest {
    private FluidTankItemWrapper handler;
    private FluidStack[] tanks;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void setup() {
        tanks = new FluidStack[] {FluidStack.EMPTY, FluidStack.EMPTY};
        handler = mock(FluidTankItemWrapper.class);
        when(handler.getContainer()).thenReturn(new ItemStack(Items.CHEST));
        when(handler.getTanks()).thenReturn(2);
        when(handler.getTankCapacity(anyInt())).thenReturn(1000);
        when(handler.isFluidValid(anyInt(), any())).thenReturn(true);
        when(handler.getFluidInTank(anyInt())).thenAnswer(call -> tanks[call.getArgument(0, Integer.class)].copy());
        doAnswer(call -> {
            tanks[call.getArgument(1, Integer.class)] = call.getArgument(0, FluidStack.class).copy();
            return null;
        }).when(handler).setFluid(any(), anyInt());
    }

    @Test
    void registeredMixinFillsTheSecondTankThroughTheBackpackCapability() {
        FluidTank left = new FluidTank(1000);
        FluidTank right = new FluidTank(1000);
        left.setFluid(water(1000));
        TanksUpgrade upgrade = mock(TanksUpgrade.class, RETURNS_DEEP_STUBS);
        when(upgrade.getLeftTank()).thenReturn(left);
        when(upgrade.getRightTank()).thenReturn(right);
        when(upgrade.getUpgradeManager().getWrapper().getBackpackTankCapacity()).thenReturn(1000);
        FluidTankItemWrapper capability = new FluidTankItemWrapper(new ItemStack(Items.CHEST), upgrade) {
            @Override
            public void setFluid(FluidStack fluid, int tank) {
                (tank == 0 ? left : right).setFluid(fluid.copy());
            }
        };
        assertEquals(500, capability.fill(water(500), FluidAction.SIMULATE));
        assertEquals(0, right.getFluidAmount());
        assertEquals(500, capability.fill(water(500), FluidAction.EXECUTE));
        assertEquals(1000, left.getFluidAmount());
        assertEquals(500, right.getFluidAmount());
    }

    @Test
    void fullLeftTankDoesNotPreventFillingRightTank() {
        tanks[0] = water(1000);
        FluidStack input = water(500);
        assertEquals(500, BackpackTankFilling.fill(handler, input, FluidAction.EXECUTE));
        assertEquals(1000, tanks[0].getAmount());
        assertEquals(500, tanks[1].getAmount());
        assertEquals(500, input.getAmount());
    }

    @Test
    void simulationAndExecutionReportTheSameCombinedCapacity() {
        tanks[0] = water(900);
        FluidStack input = water(1500);
        assertEquals(1100, BackpackTankFilling.fill(handler, input, FluidAction.SIMULATE));
        assertEquals(900, tanks[0].getAmount());
        assertTrue(tanks[1].isEmpty());
        verify(handler, never()).setFluid(any(), anyInt());
        assertEquals(1100, BackpackTankFilling.fill(handler, input, FluidAction.EXECUTE));
        assertEquals(1000, tanks[0].getAmount());
        assertEquals(1000, tanks[1].getAmount());
    }

    @Test
    void existingRightTankIsToppedUpBeforeUsingEmptyLeftTank() {
        tanks[1] = water(500);
        assertEquals(250, BackpackTankFilling.fill(handler, water(250), FluidAction.EXECUTE));
        assertTrue(tanks[0].isEmpty());
        assertEquals(750, tanks[1].getAmount());
    }

    @Test
    void waterNeverReplacesLava() {
        tanks[0] = new FluidStack(Fluids.LAVA, 500);
        assertEquals(1000, BackpackTankFilling.fill(handler, water(1500), FluidAction.EXECUTE));
        assertEquals(Fluids.LAVA, tanks[0].getFluid());
        assertEquals(500, tanks[0].getAmount());
        assertEquals(1000, tanks[1].getAmount());
    }

    @Test
    void differentFluidComponentsAreKeptSeparate() {
        // Use a vanilla component as a stand-in for the registered purity component.
        tanks[0] = water(500);
        tanks[0].set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1));
        FluidStack input = water(300);
        input.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(3));
        assertEquals(300, BackpackTankFilling.fill(handler, input, FluidAction.EXECUTE));
        assertEquals(500, tanks[0].getAmount());
        assertEquals(new CustomModelData(1), tanks[0].get(DataComponents.CUSTOM_MODEL_DATA));
        assertEquals(new CustomModelData(3), tanks[1].get(DataComponents.CUSTOM_MODEL_DATA));
    }

    @Test
    void fullTanksRejectMoreFluid() {
        tanks[0] = water(1000);
        tanks[1] = water(1000);
        assertEquals(0, BackpackTankFilling.fill(handler, water(100), FluidAction.EXECUTE));
        verify(handler, never()).setFluid(any(), anyInt());
    }

    @Test
    void invalidFluidAndStackedContainersAreRejected() {
        when(handler.isFluidValid(anyInt(), any())).thenReturn(false);
        assertEquals(0, BackpackTankFilling.fill(handler, water(100), FluidAction.EXECUTE));
        when(handler.isFluidValid(anyInt(), any())).thenReturn(true);
        when(handler.getContainer()).thenReturn(new ItemStack(Items.CHEST, 2));
        assertEquals(0, BackpackTankFilling.fill(handler, water(100), FluidAction.EXECUTE));
        verify(handler, never()).setFluid(any(), anyInt());
    }

    private static FluidStack water(int amount) {
        return new FluidStack(Fluids.WATER, amount);
    }
}
