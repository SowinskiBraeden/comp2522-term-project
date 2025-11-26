package ca.bcit.comp2522.project;

public class AscendingPlacement
    extends PlacementRule
{
    private static final int FIRST = 0;
    private static final int NEXT  = 1;

    @Override
    public boolean isValidPlacement(int[] positions, int index, int value)
    {
        for (int i = 0; i < positions.length; i++)
        {
            if (positions[i] == FIRST)
            {
                continue;
            }

            boolean invalidBefore;
            boolean invalidAfter;

            invalidBefore = i < index && positions[i] > value;
            invalidAfter  = i > index && positions[i] < value;

            if (invalidBefore || invalidAfter)
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean canPlaceNext(int[] positions, int nextValue)
    {
        for (int i = 0; i < positions.length; i++)
        {
            if (positions[i] != FIRST)
            {
                continue;
            }

            int left;
            int right;

            left = Integer.MIN_VALUE;
            right = Integer.MAX_VALUE;

            // scan left
            for (int l = i - NEXT; l >= FIRST; l--)
            {
                if (positions[l] != FIRST)
                {
                    left = positions[l];
                    break;
                }
            }

            // scan right
            for (int r = i + NEXT; r < positions.length; r++)
            {
                if (positions[r] != FIRST)
                {
                    right = positions[r];
                    break;
                }
            }

            if (left < nextValue && nextValue < right)
            {
                return true;
            }
        }
        return false;
    }
}
