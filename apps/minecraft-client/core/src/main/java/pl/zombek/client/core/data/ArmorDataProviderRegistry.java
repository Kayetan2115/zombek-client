package pl.zombek.client.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Registry holding the active IArmorDataProvider instance.
 * Defaults to empty armor slots for safe execution outside of game / in unit tests.
 */
public class ArmorDataProviderRegistry {

    private static final IArmorDataProvider DEFAULT_PROVIDER = () -> Arrays.asList(
            ArmorItemData.empty(ArmorSlot.HELMET),
            ArmorItemData.empty(ArmorSlot.CHESTPLATE),
            ArmorItemData.empty(ArmorSlot.LEGGINGS),
            ArmorItemData.empty(ArmorSlot.BOOTS)
    );

    private static volatile IArmorDataProvider provider = DEFAULT_PROVIDER;

    public static IArmorDataProvider getProvider() {
        return provider;
    }

    public static void setProvider(IArmorDataProvider newProvider) {
        provider = (newProvider != null) ? newProvider : DEFAULT_PROVIDER;
    }

    public static void reset() {
        provider = DEFAULT_PROVIDER;
    }
}
