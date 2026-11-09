package com.ourcreation.thirst_was_upgraded.mixin;

import com.ourcreation.thirst_was_upgraded.util.BackpackTankFilling;
import com.tiviacz.travelersbackpack.inventory.FluidTankItemWrapper;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidTankItemWrapper.class, remap = false)
public abstract class FluidTankItemWrapperMixin {
    @Inject(method = "fill", at = @At("HEAD"), cancellable = true)
    private void thirst_was_upgraded$fillBothTanks(
            FluidStack resource,
            IFluidHandler.FluidAction action,
            CallbackInfoReturnable<Integer> callback
    ) {
        callback.setReturnValue(BackpackTankFilling.fill((FluidTankItemWrapper) (Object) this, resource, action));
    }
}
