package ca.bcit.comp2522.project;

public abstract class PlacementRule
{
    public abstract boolean isValidPlacement(
        final int[] positions,
        final int   index,
        final int   value
    );

    public abstract boolean canPlaceNext(
        final int[] positions,
        final int   nextValue
    );
}
