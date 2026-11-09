package com.ourcreation.thirst_was_upgraded.util;

import com.tiviacz.travelersbackpack.inventory.FluidTankItemWrapper;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class BackpackTankFilling {
    private BackpackTankFilling() {
    }

    public static int fill(FluidTankItemWrapper handler, FluidStack resource, IFluidHandler.FluidAction action) {
        if (handler.getContainer().getCount() != 1 || resource.isEmpty()) {
            return 0;
        }

        int remaining = resource.getAmount();
        // Top up matching fluids first, then use empty tanks. Preserve fluid
        // components (including purity) and leave incompatible tanks alone.
        for (int pass = 0; pass < 2 && remaining > 0; pass++) {
            for (int tank = 0; tank < handler.getTanks() && remaining > 0; tank++) {
                FluidStack contained = handler.getFluidInTank(tank);
                if (pass == 0 ? contained.isEmpty() : !contained.isEmpty()) {
                    continue;
                }
                if ((!contained.isEmpty() && !FluidStack.isSameFluidSameComponents(contained, resource))
                        || !handler.isFluidValid(tank, resource)) {
                    continue;
                }

                int amount = Math.min(remaining,
                        Math.max(0, handler.getTankCapacity(tank) - contained.getAmount()));
                if (amount > 0 && action.execute()) {
                    handler.setFluid(resource.copyWithAmount(contained.getAmount() + amount), tank);
                }
                remaining -= amount;
            }
        }
        return resource.getAmount() - remaining;
    }
}
