package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class WorldBuilderTest {
    @Test
    void buildCreatesWorldWithRequestedDimensions() {
        World world = new WorldBuilder(7, 6, 2).build();

        assertEquals(7, world.width());
        assertEquals(6, world.height());
        assertEquals(2, world.depth());
    }

    @Test
    void findRegionOverlapsRequiresMatchingFloorRegions() {
        WorldBuilder builder = new WorldBuilder(4, 4, 2);

        List<Point> candidates = builder.findRegionOverlaps(0, 1, 1);

        assertTrue(candidates.isEmpty());
    }

    @Test
    void buildKeepsAllThreeDimensionsForLargerWorld() {
        World world = new WorldBuilder(20, 20, 3).build();

        assertEquals(20, world.width());
        assertEquals(20, world.height());
        assertEquals(3, world.depth());
    }
}
