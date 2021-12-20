package at.rtr.rmbt.utils.smoothing;

import java.util.List;

public interface SmoothingFunctionCalculation {

    double smoothXPoint(final List<? extends Smoothable> valueList, final int index, final int dataAmount);

    double smoothYPoint(final List<? extends Smoothable> valueList, final int index, final int dataAmount);

    int getStartingIndex(final List<? extends Smoothable> valueList, final int dataAmount);

    int getEndingIndex(final List<? extends Smoothable> valueList, final int dataAmount);
}
