package com.ourcreation.thirst_was_upgraded.mixin;

import cn.mlus.thirst.api.ThirstHelper;
import cn.mlus.thirst.content.thirst.PlayerThirst;
import cn.mlus.thirst.foundation.common.capability.ModAttachment;

import com.tiviacz.travelersbackpack.inventory.upgrades.feeding.FeedingUpgrade;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.items.IItemHandlerModifiable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FeedingUpgrade.class, remap = false)
public abstract class FeedingUpgradeMixin {

    @Unique
    private boolean thirst_was_upgraded$restoreThirstAfterFeeding;

    @Unique
    private int thirst_was_upgraded$previousThirst;

    @Unique
    private int thirst_was_upgraded$previousQuenched;

    @Inject(method = "tryFeedingStack", at = @At("HEAD"))
    private void thirst_was_upgraded$captureThirstBeforeAutoFeeding(
            Level level,
            int hungerLevel,
            Player player,
            Integer slot,
            ItemStack stack,
            IItemHandlerModifiable storage,
            CallbackInfoReturnable<Boolean> callback
    ) {
        thirst_was_upgraded$restoreThirstAfterFeeding = false;

        PlayerThirst thirst = player.getData(ModAttachment.PLAYER_THIRST);
        if (thirst == null || restoresThirst(player, stack)) {
            return;
        }

        thirst_was_upgraded$previousThirst = thirst.getThirst();
        thirst_was_upgraded$previousQuenched = thirst.getQuenched();
        thirst_was_upgraded$restoreThirstAfterFeeding = true;
    }

    @Inject(method = "tryFeedingStack", at = @At("RETURN"))
    private void thirst_was_upgraded$restoreThirstAfterAutoFeeding(
            Level level,
            int hungerLevel,
            Player player,
            Integer slot,
            ItemStack stack,
            IItemHandlerModifiable storage,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (!thirst_was_upgraded$restoreThirstAfterFeeding || !callback.getReturnValue()) {
            return;
        }

        thirst_was_upgraded$restoreThirstAfterFeeding = false;

        PlayerThirst thirst = player.getData(ModAttachment.PLAYER_THIRST);
        if (thirst == null) {
            return;
        }

        thirst.setThirst(thirst_was_upgraded$previousThirst);
        thirst.setQuenched(thirst_was_upgraded$previousQuenched);
        thirst.updateThirstData(player);
    }

    @Unique
    private static boolean restoresThirst(Player player, ItemStack stack) {
        try {
            return ThirstHelper.itemRestoresThirst(stack)
                    && ThirstHelper.playerRestoresThirst(stack, player)
                    && ThirstHelper.getThirst(stack) > 0;
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
