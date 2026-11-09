package com.ourcreation.thirst_was_upgraded.item;

import com.ourcreation.thirst_was_upgraded.inventory.AutoDrinkingUpgrade;
import com.tiviacz.travelersbackpack.components.BackpackContainerContents;
import com.tiviacz.travelersbackpack.init.ModDataComponents;
import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tiviacz.travelersbackpack.inventory.upgrades.UpgradeBase;
import com.tiviacz.travelersbackpack.items.upgrades.UpgradeItem;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;

public class AutoDrinkUpgradeItem extends UpgradeItem {

    public AutoDrinkUpgradeItem(Properties properties) {
        super(properties, "auto_drink_upgrade");
    }

    @Override
    public boolean isTickingUpgrade() {
        return true;
    }

    @Override
    public Class<? extends UpgradeBase<?>> getUpgradeClass() {
        return AutoDrinkingUpgrade.class;
    }

    @Override
    public TriFunction<UpgradeManager, Integer, ItemStack, Optional<? extends UpgradeBase<?>>> getUpgrade() {
        return (upgradeManager, dataHolderSlot, provider) -> {
            BackpackContainerContents contents = provider.getOrDefault(
                    ModDataComponents.BACKPACK_CONTAINER,
                    new BackpackContainerContents(9)
            );
            return Optional.of(new AutoDrinkingUpgrade(upgradeManager, dataHolderSlot, contents.getItems()));
        };
    }
}
