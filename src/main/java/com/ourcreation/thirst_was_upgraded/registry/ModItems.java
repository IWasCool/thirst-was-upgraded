package com.ourcreation.thirst_was_upgraded.registry;

import com.ourcreation.thirst_was_upgraded.ThirstWasUpgraded;
import com.ourcreation.thirst_was_upgraded.item.AutoDrinkUpgradeItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, ThirstWasUpgraded.MOD_ID);

    public static final DeferredHolder<Item, Item> AUTO_DRINK_UPGRADE =
            ITEMS.register("auto_drink_upgrade", () -> new AutoDrinkUpgradeItem(new Item.Properties()));

    private ModItems() {
    }
}
