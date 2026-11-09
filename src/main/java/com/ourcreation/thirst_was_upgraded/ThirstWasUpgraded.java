package com.ourcreation.thirst_was_upgraded;

import com.ourcreation.thirst_was_upgraded.compat.ToughAsNailsCompat;
import com.ourcreation.thirst_was_upgraded.compat.CreateCompat;
import com.ourcreation.thirst_was_upgraded.registry.ModItems;
import com.tiviacz.travelersbackpack.init.ModCreativeTabs;

import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.Mod;

@Mod(ThirstWasUpgraded.MOD_ID)
public class ThirstWasUpgraded {

    public static final String MOD_ID = "thirst_was_upgraded";

    public ThirstWasUpgraded(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreativeTabItems);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(CreateCompat::modifyDefaultComponents);
        NeoForge.EVENT_BUS.addListener(CreateCompat::registerThirstValues);
        NeoForge.EVENT_BUS.addListener(CreateCompat::registerLegacyThirstValues);
        NeoForge.EVENT_BUS.addListener(ToughAsNailsCompat::registerThirstValues);
        NeoForge.EVENT_BUS.addListener(ToughAsNailsCompat::registerLegacyThirstValues);
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(ModCreativeTabs.TRAVELERS_BACKPACK.getKey())) {
            event.accept(ModItems.AUTO_DRINK_UPGRADE.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        ToughAsNailsCompat.registerCapabilities(event);
    }
}
