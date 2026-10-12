package dev.trexzo.custommc.platform.v1_8_9;

public interface Minecraft189PlayerPositionAccess {
    double customMcPositionX();

    double customMcPositionY();

    double customMcPositionZ();

    /** Native source-mapped Entity.lastTickPos*, NOT previous render-frame state.
     *  Default missing values veto interpolation on untransformed test doubles.
     */
    default double customMcLastTickPositionX() { return Double.NaN; }
    default double customMcLastTickPositionY() { return Double.NaN; }
    default double customMcLastTickPositionZ() { return Double.NaN; }
}
