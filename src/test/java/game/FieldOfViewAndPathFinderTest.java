package game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import asciiPanel.AsciiPanel;

class FieldOfViewAndPathFinderTest {
    @Test
    void fieldOfViewTracksVisibleBoundsAndRememberedTiles() {
        Tile[][][] tiles = new Tile[7][7][1];
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                tiles[x][y][0] = Tile.FLOOR;
            }
        }
        World world = new World(tiles);
        FieldOfView fov = new FieldOfView(world);

        fov.update(3, 3, 0, 2);

        assertTrue(fov.isVisible(3, 3, 0));
        assertTrue(fov.isVisible(4, 3, 0));
        assertFalse(fov.isVisible(-1, 3, 0));
        assertFalse(fov.isVisible(3, 3, 1));
        assertEqualsTile(Tile.FLOOR, fov.tile(4, 3, 0));
    }

    @Test
    void fieldOfViewWallBlocksLightPastIt() {
        Tile[][][] tiles = new Tile[7][7][1];
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                tiles[x][y][0] = Tile.FLOOR;
            }
        }
        tiles[4][3][0] = Tile.WALL;
        World world = new World(tiles);
        FieldOfView fov = new FieldOfView(world);

        fov.update(3, 3, 0, 4);

        assertFalse(fov.isVisible(5, 3, 0));
        assertEqualsTile(Tile.WALL, fov.tile(4, 3, 0));
    }

    @Test
    void pathFinderReturnsRouteAroundWalls() {
        Tile[][][] tiles = new Tile[5][5][1];
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                tiles[x][y][0] = Tile.FLOOR;
            }
        }
        tiles[2][1][0] = Tile.WALL;
        tiles[2][2][0] = Tile.WALL;
        tiles[2][3][0] = Tile.WALL;
        World world = new World(tiles);
        Creature creature = new Creature(world, '@', AsciiPanel.brightRed, "hero", 100, 2, 1);
        creature.x = 1;
        creature.y = 2;
        creature.z = 0;
        new CreatureAi(creature);

        ArrayList<Point> path = new PathFinder().findPath(creature,
                new Point(1, 2, 0), new Point(3, 2, 0), 300);

        assertNotNull(path);
        assertTrue(path.get(path.size() - 1).equals(new Point(3, 2, 0)));
    }

    @Test
    void pathFinderReturnsNullWhenDestinationIsUnreachable() {
        Tile[][][] tiles = new Tile[3][3][1];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tiles[x][y][0] = Tile.WALL;
            }
        }
        tiles[0][0][0] = Tile.FLOOR;
        tiles[2][2][0] = Tile.FLOOR;
        World world = new World(tiles);
        Creature creature = new Creature(world, '@', AsciiPanel.brightRed, "hero", 100, 2, 1);
        creature.x = 0;
        creature.y = 0;
        creature.z = 0;
        new CreatureAi(creature);

        ArrayList<Point> path = new PathFinder().findPath(creature,
                new Point(0, 0, 0), new Point(2, 2, 0), 300);

        assertTrue(path == null);
    }

    private void assertEqualsTile(Tile expected, Tile actual) {
        assertTrue(expected == actual);
    }
}
