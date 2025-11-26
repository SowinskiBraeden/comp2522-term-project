package ca.bcit.comp2522.project;

import java.util.Random;

public class RandomNumberGenerator
    implements NumberGenerator
{
    private final Random random;
    private final int min;
    private final int max;

    public RandomNumberGenerator(int min, int max)
    {
        random = new Random();

        this.min = min;
        this.max = max;
    }

    @Override
    public int generate()
    {
        return random.nextInt(max - min) + min;
    }
}
