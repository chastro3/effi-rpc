package io.effi.rpc.option;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OptionsStrategyTest {

    private static final OptionName<String> ONLY_CURRENT_NAME =
            OptionTypes.STRING.onlyCurrent("onlyCurrent", "default");

    private static final OptionName<String> CURRENT_FIRST_NAME =
            OptionTypes.STRING.currentFirst("currentFirst", "default");

    private static final OptionName<String> PARENT_FIRST_NAME =
            OptionTypes.STRING.parentFirst("parentFirst", "default");

    private static final OptionName<String[]> MERGE_NAME =
            OptionTypes.STRING_ARRAY.mergeParent("merge");

    @Test
    void readsOnlyCurrentValue() {
        DefaultHierarchicalOptions parent = options("parent");
        DefaultHierarchicalOptions current = options("current").withParent(parent);

        assertEquals("current", current.option(ONLY_CURRENT_NAME));

        current.removeOption(ONLY_CURRENT_NAME);
        assertEquals("default", current.option(ONLY_CURRENT_NAME));
    }

    private static DefaultHierarchicalOptions options(String value) {
        DefaultHierarchicalOptions options = options();
        options.addOption(CURRENT_FIRST_NAME, value);
        options.addOption(ONLY_CURRENT_NAME, value);
        options.addOption(PARENT_FIRST_NAME, value);
        options.addOption(MERGE_NAME, new String[]{value});
        return options;
    }

    private static DefaultHierarchicalOptions options() {
        return new DefaultHierarchicalOptions();
    }

    @Test
    void prefersCurrentThenParent() {
        DefaultHierarchicalOptions parent = options("parent");
        DefaultHierarchicalOptions current = options().withParent(parent);

        assertEquals("parent", current.option(CURRENT_FIRST_NAME));

        current.addOption(CURRENT_FIRST_NAME, "current");
        assertEquals("current", current.option(CURRENT_FIRST_NAME));
    }

    @Test
    void prefersParentThenCurrent() {
        DefaultHierarchicalOptions parent = options("parent");
        DefaultHierarchicalOptions current = options("current").withParent(parent);

        assertEquals("parent", current.option(PARENT_FIRST_NAME));

        parent.removeOption(PARENT_FIRST_NAME);
        assertEquals("current", current.option(PARENT_FIRST_NAME));
    }

    @Test
    void mergesParentAndCurrentArrays() {
        DefaultHierarchicalOptions parent = options();
        parent.addOption(MERGE_NAME, new String[]{"a", "b"});

        DefaultHierarchicalOptions current = options().withParent(parent);
        current.addOption(MERGE_NAME, new String[]{"b", "c"});

        assertArrayEquals(
                new String[]{"a", "b", "c"},
                current.option(MERGE_NAME)
        );
    }

    @Test
    void rejectsMergeForScalarType() {
        OptionName<String> invalid = OptionTypes.STRING.name(
                "invalidMerge",
                OptionStrategy.MERGE_PARENT,
                "default"
        );

        assertThrows(
                IllegalStateException.class,
                () -> new DefaultHierarchicalOptions().option(invalid)
        );
    }

    @Test
    void returnsDefaultWhenNoValueExists() {
        assertEquals("default", new DefaultHierarchicalOptions().option(CURRENT_FIRST_NAME));
    }

    @Test
    void usesNullDefaultWhenDefaultValueIsOmitted() {
        OptionName<String> name =
                OptionTypes.STRING.onlyCurrent("withoutDefault");

        assertNull(new DefaultHierarchicalOptions().option(name));
    }

    @Test
    void ignoresNullWhenAddingOption() {
        DefaultHierarchicalOptions options = options("current");
        options.addOption(CURRENT_FIRST_NAME, null);

        assertEquals("current", options.option(CURRENT_FIRST_NAME));
    }
}
