package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import asciiPanel.AsciiPanel;

class WorldTest {
    private World world;

    @BeforeEach
    void setUp() {
        Tile[][][] tiles = new Tile[5][5][2];
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = 0; z < 2; z++) {
                    tiles[x][y][z] = Tile.FLOOR;
                }
            }
        }
        world = new World(tiles);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0, 0",
        "4, 4, 1",
        "2, 3, 1"
    })
    void tileReturnsFloorAtValidCoordinates(int x, int y, int z) {
        assertEquals(Tile.FLOOR, world.tile(x, y, z));
    }

    @ParameterizedTest
    @CsvSource({
        "-1, 0, 0",
        "0, -1, 0",
        "0, 0, -1",
        "5, 0, 0",
        "0, 5, 0",
        "0, 0, 2"
    })
    void tileReturnsBoundsOutsideWorld(int x, int y, int z) {
        assertEquals(Tile.BOUNDS, world.tile(x, y, z));
    }

    @Test
    void dimensionsMatchTheTileArray() {
        assertEquals(5, world.width());
        assertEquals(5, world.height());
        assertEquals(2, world.depth());
    }

    @Test
    void creatureTakesPrecedenceOverItemsAndTilesInGlyphAndColor() {
        Item item = new Item('%', Color.RED, "item", null);
        Creature creature = new Creature(world, '@', AsciiPanel.brightBlue, "hero", 10, 2, 1);
        creature.x = 2;
        creature.y = 2;
        creature.z = 0;

        world.addAtEmptySpace(item, 2, 2, 0);
        world.add(creature);

        assertEquals('@', world.glyph(2, 2, 0));
        assertEquals(AsciiPanel.brightBlue, world.color(2, 2, 0));
        assertSame(creature, world.creature(2, 2, 0));
    }

    @Test
    void itemTakesPrecedenceOverUnderlyingTileInGlyphAndColor() {
        Item item = new Item('%', Color.RED, "item", null);
        assertTrue(world.addAtEmptySpace(item, 2, 2, 0));

        assertEquals('%', world.glyph(2, 2, 0));
        assertEquals(Color.RED, world.color(2, 2, 0));
        assertSame(item, world.item(2, 2, 0));
    }

    @Test
    void emptyLocationUsesUnderlyingTileGlyphAndColor() {
        assertEquals(Tile.FLOOR.glyph(), world.glyph(1, 1, 0));
        assertEquals(Tile.FLOOR.color(), world.color(1, 1, 0));
        assertNull(world.creature(1, 1, 0));
        assertNull(world.item(1, 1, 0));
    }

    @Test
    void digChangesOnlyDiggableWalls() {
        Tile[][][] tiles = new Tile[3][3][1];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tiles[x][y][0] = Tile.WALL;
            }
        }
        World digWorld = new World(tiles);

        digWorld.dig(1, 1, 0);
        digWorld.dig(0, 0, 0);

        assertEquals(Tile.FLOOR, digWorld.tile(1, 1, 0));
        assertEquals(Tile.FLOOR, digWorld.tile(0, 0, 0));
    }

    @Test
    void digDoesNotChangeFloorOrOutOfBounds() {
        world.dig(1, 1, 0);
        world.dig(-1, 1, 0);

        assertEquals(Tile.FLOOR, world.tile(1, 1, 0));
        assertEquals(Tile.BOUNDS, world.tile(-1, 1, 0));
    }

    @Test
    void addAtEmptySpacePlacesItemAtRequestedEmptyLocation() {
        Item item = new Item('%', Color.RED, "item", null);

        assertTrue(world.addAtEmptySpace(item, 2, 2, 0));
        assertSame(item, world.item(2, 2, 0));
    }

    @Test
    void addAtEmptySpaceRejectsNullWithoutMutation() {
        assertTrue(world.addAtEmptySpace(null, 2, 2, 0));
        assertNull(world.item(2, 2, 0));
    }

    @Test
    void addAtEmptySpacePlacesItemInReachableNeighborWhenOriginIsOccupied() {
        Item first = new Item('%', Color.RED, "first", null);
        Item second = new Item('$', Color.GREEN, "second", null);

        assertTrue(world.addAtEmptySpace(first, 2, 2, 0));
        assertTrue(world.addAtEmptySpace(second, 2, 2, 0));

        assertSame(first, world.item(2, 2, 0));
        assertEquals(1, countItemsNear(second, 2, 2, 0));
    }

    @Test
    void addAtEmptySpaceReturnsFalseWhenNoGroundLocationIsAvailable() {
        Tile[][][] tiles = new Tile[3][3][1];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tiles[x][y][0] = Tile.WALL;
            }
        }
        World blockedWorld = new World(tiles);
        Item item = new Item('%', Color.RED, "item", null);

        assertFalse(blockedWorld.addAtEmptySpace(item, 1, 1, 0));
        assertNull(blockedWorld.item(1, 1, 0));
    }

    @Test
    void removeCreatureAndItemClearTheirWorldLocations() {
        Creature creature = new Creature(world, '@', AsciiPanel.brightBlue, "hero", 10, 2, 1);
        creature.x = 1;
        creature.y = 1;
        creature.z = 0;
        world.add(creature);

        Item item = new Item('%', Color.RED, "item", null);
        world.addAtEmptySpace(item, 2, 2, 0);

        world.remove(creature);
        world.remove(item);

        assertNull(world.creature(1, 1, 0));
        assertNull(world.item(2, 2, 0));
    }

    @Test
    void removeCoordinateClearsOnlyTheRequestedItem() {
        Item item = new Item('%', Color.RED, "item", null);
        world.addAtEmptySpace(item, 2, 2, 0);

        world.remove(2, 2, 0);

        assertNull(world.item(2, 2, 0));
    }

    private int countItemsNear(Item item, int x, int y, int z) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (world.item(x + dx, y + dy, z) == item) {
                    count++;
                }
            }
        }
        return count;
    }
}
