package org.bbottema.javareflection.testmodel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;

public class Kraa {
    private void testMethod(
            Integer argOne,
            @Nullable
            List argTwo,
            @Nonnull
            HashSet<Double> argThree) {
    }
}
