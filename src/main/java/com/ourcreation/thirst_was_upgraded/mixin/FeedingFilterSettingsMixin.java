package com.ourcreation.thirst_was_upgraded.mixin;

import cn.mlus.thirst.api.ThirstHelper;
import cn.mlus.thirst.content.thirst.PlayerThirst;
import cn.mlus.thirst.foundation.common.capability.ModAttachment;

import com.tiviacz.travelersbackpack.inventory.upgrades.feeding.FeedingFilterSettings;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FeedingFilterSettings.class, remap = false)
public abstract class FeedingFilterSettingsMixin {

    @Inject(method = "matchesFilter", at = @At("RETURN"), cancellable = true)
    private void thirst_was_upgraded$matchThirstRestoredByFood(
            Player player,
            ItemStack stack,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (!callback.getReturnValue() || !restoresThirstAsFood(player, stack)) {
            return;
        }

        FeedingFilterSettings settings = (FeedingFilterSettings) (Object) this;
        List<Integer> filterSettings = settings.getSettings();

        if (filterSettings.size() <= FeedingFilterSettings.HUNGER_MODE) {
            return;
        }

        if (!compareThirstLevel(player, stack, filterSettings.get(FeedingFilterSettings.HUNGER_MODE))) {
            callback.setReturnValue(false);
        }
    }

    private static boolean restoresThirstAsFood(Player player, ItemStack stack) {
        try {
            return ThirstHelper.isFood(stack)
                    && ThirstHelper.playerRestoresThirst(stack, player)
                    && ThirstHelper.getThirst(stack) > 0;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean compareThirstLevel(Player player, ItemStack stack, int mode) {
        PlayerThirst thirst = player.getData(ModAttachment.PLAYER_THIRST);

        if (thirst == null) {
            return true;
        }

        int thirstDifference = getThirstDifference(thirst.getThirst(), stack);

        if (mode == FeedingFilterSettings.HALF_NUTRITION) {
            return thirstDifference <= getHalfOfStackThirst(stack);
        }

        if (mode == FeedingFilterSettings.FULL_NUTRITION) {
            return thirstDifference <= 0;
        }

        return true;
    }

    private static int getThirstDifference(int thirstLevel, ItemStack stack) {
        return thirstLevel + ThirstHelper.getThirst(stack) - 20;
    }

    private static int getHalfOfStackThirst(ItemStack stack) {
        return ThirstHelper.getThirst(stack) / 2;
    }
}
