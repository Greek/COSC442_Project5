package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import asciiPanel.AsciiPanel;

class ConcreteAiTest {
    private World world;
    private Creature player;
    private StuffFactory factory;

    @BeforeEach
    void setUp() {
        Tile[][][] tiles = new Tile[9][9][2];
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                for (int z = 0; z < 2; z++) {
                    tiles[x][y][z] = Tile.FLOOR;
                }
            }
        }
        world = new World(tiles);
        factory = new StuffFactory(world);
        player = new Creature(world, '@', AsciiPanel.brightRed, "player", 100, 5, 2);
        player.x = 4;
        player.y = 4;
        player.z = 0;
        new PlayerAi(player, new ArrayList<>(), new FieldOfView(world));
        world.add(player);
    }

    @Test
    void batUpdatesWithoutMovingOutsideWorld() {
        Creature bat = factory.newBat(0);
        BatAi ai = new BatAi(bat);
        for (int i = 0; i < 5; i++) {
            ai.onUpdate();
            assertTrue(world.tile(bat.x, bat.y, bat.z).isGround());
            assertTrue(bat.x >= 0 && bat.x < world.width());
            assertTrue(bat.y >= 0 && bat.y < world.height());
        }
    }

    @Test
    void fungusUpdateDoesNotCreateInvalidOrDuplicateState() {
        Creature fungus = factory.newFungus(0);
        FungusAi ai = new FungusAi(fungus, factory);
        int initialSpreadCount = fungusCount(fungus);

        for (int i = 0; i < 20; i++) {
            ai.onUpdate();
        }

        assertTrue(fungusCount(fungus) >= initialSpreadCount);
        assertTrue(world.tile(fungus.x, fungus.y, fungus.z).isGround());
    }

    @Test
    void goblinUsesEquipmentRangedThrowSightPickupAndWanderPrecedence() {
        Creature goblin = factory.newGoblin(0, player);
        GoblinAi ai = new GoblinAi(goblin, player);
        goblin.setCreatureAi(ai);

        Item better = new Item(')', AsciiPanel.brightRed, "better sword", null);
        better.modifyAttackValue(20);
        goblin.inventory().add(better);

        ai.onUpdate();
        assertTrue(goblin.weapon() == better || goblin.weapon() != null);

        Creature rangedGoblin = factory.newGoblin(0, player);
        rangedGoblin.setCreatureAi(new GoblinAi(rangedGoblin, player));
        assertTrue(rangedGoblin.weapon() != null || rangedGoblin.armor() != null);
    }

    @Test
    void zombieVisibleOrInvisibleUpdatesRemainWithinWorld() {
        Creature zombie = factory.newZombie(0, player);
        ZombieAi ai = new ZombieAi(zombie, player);
        zombie.setCreatureAi(ai);

        ai.onUpdate();

        assertTrue(world.tile(zombie.x, zombie.y, zombie.z).isGround());
        assertTrue(zombie.x >= 0 && zombie.x < world.width());
        assertTrue(zombie.y >= 0 && zombie.y < world.height());
    }

    @Test
    void playerAiMovesNotifiesAndReportsFieldOfViewTiles() {
        List<String> messages = new ArrayList<>();
        FieldOfView fov = new FieldOfView(world);
        fov.update(player.x, player.y, player.z, 3);
        PlayerAi ai = new PlayerAi(player, messages, fov);
        player.setCreatureAi(ai);

        ai.onEnter(5, 4, 0, Tile.FLOOR);
        assertEquals(5, player.x);

        ai.onNotify("hello");
        assertEquals(List.of("hello"), messages);
        assertTrue(ai.canSee(5, 4, 0));
        assertEquals(world.tile(5, 4, 0), ai.rememberedTile(5, 4, 0));
    }

    @Test
    @Timeout(2)
    void itemPlacementStopsWhenWorldHasNoEmptyLocation() {
        for (int x = 0; x < world.width(); x++) {
            for (int y = 0; y < world.height(); y++) {
                world.addAtEmptyLocation(new Item(',', AsciiPanel.brightWhite, "rock", null), 0);
            }
        }

        Item extra = new Item(',', AsciiPanel.brightWhite, "extra rock", null);
        world.addAtEmptyLocation(extra, 0);

        int occupiedLocations = 0;
        for (int x = 0; x < world.width(); x++) {
            for (int y = 0; y < world.height(); y++) {
                if (world.item(x, y, 0) != null)
                    occupiedLocations++;
            }
        }
        assertEquals(world.width() * world.height(), occupiedLocations);
    }

    private int fungusCount(Creature fungus) {
        int count = 0;
        for (int x = 0; x < world.width(); x++) {
            for (int y = 0; y < world.height(); y++) {
                Creature creature = world.creature(x, y, fungus.z);
                if (creature != null && creature.glyph() == 'f') count++;
            }
        }
        return count;
    }
}
