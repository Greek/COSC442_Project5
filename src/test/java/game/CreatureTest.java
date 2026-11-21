package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import asciiPanel.AsciiPanel;

public class CreatureTest {
    String CAUSE_OF_DEATH = "testing";
    Creature creature1, creature2, creature3;
    World world;

    @BeforeEach
    public void arrangeCreature() {
        Tile[][][] tiles = new Tile[12][12][1];

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tiles[x][y][0] = Tile.FLOOR;
            }
        }

        world = new World(tiles);
        creature1 = new Creature(world, 'X', AsciiPanel.brightRed, "Creature1", 100, 0, 0);
        creature1.x = 1;
        creature1.y = 1;
        creature1.z = 0;

        creature2 = new Creature(world, 'Z', AsciiPanel.brightRed, "Creature2", 80, 0, 0);
        creature2.x = 2;
        creature2.y = 1;
        creature2.z = 0;
        creature2.modifyMaxHp(100);

        creature3 = new Creature(world, 'Y', AsciiPanel.brightRed, "Creature3", 80, 0, 0);
        creature3.x = 3;
        creature3.y = 1;
        creature3.z = 0;
    }

    @Test
    public void testCalculateNewHp_decrementsHpByGivenAmount() {
        int modifier = -50;
        creature1.modifyHp(modifier, CAUSE_OF_DEATH);

        assertEquals(50, creature1.hp());
        assertFalse(CombatRules.isDead(creature1.hp()));
    }

    @Test
    public void testCalculateNewHp_incrementsHpByGivenAmount() {
        int modifier = 10;
        creature2.modifyHp(modifier, CAUSE_OF_DEATH);

        assertEquals(90, creature2.hp());
        assertFalse(CombatRules.isDead(creature2.hp()));
    }

    @Test
    public void testCalculateNewHp_clampHpToTheMaximumSetAmount() {
        int modifier = 20;
        creature3.modifyHp(modifier, CAUSE_OF_DEATH);

        assertEquals(80, creature3.hp());
        assertFalse(CombatRules.isDead(creature3.hp()));
    }

    @ParameterizedTest
    @CsvSource({ "-100, 0, true", "-101, -1, true" })
    public void testCalculateNewHp_performKillActionOnCreatureIfHpBelowZero(int modifier, int expectedHp,
            boolean isDead) {
        creature1.modifyHp(modifier, CAUSE_OF_DEATH);

        assertEquals(expectedHp, creature1.hp());
        assertTrue(CombatRules.isDead(creature1.hp()));
    }

    @Test
    public void testCalculateNewHp_doesNotChangeHpOnZeroModifier() {
        int modifier = 0;
        creature1.modifyHp(modifier, CAUSE_OF_DEATH);

        assertEquals(100, creature1.hp());
        assertFalse(CombatRules.isDead(creature1.hp()));
    }

    @Test
    public void canEnterRejectsWallsAndOccupiedCells() {
        Tile[][][] tiles = new Tile[3][3][1];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tiles[x][y][0] = Tile.FLOOR;
            }
        }
        tiles[0][1][0] = Tile.WALL;
        World wallWorld = new World(tiles);
        Creature mover = new Creature(wallWorld, '@', AsciiPanel.brightRed, "Mover", 100, 2, 1);
        Creature blocker = new Creature(wallWorld, 'Z', AsciiPanel.brightRed, "Blocker", 100, 2, 1);
        mover.x = 1;
        mover.y = 1;
        mover.z = 0;
        blocker.x = 2;
        blocker.y = 1;
        blocker.z = 0;
        mover.setCreatureAi(new CreatureAi(mover));
        blocker.setCreatureAi(new CreatureAi(blocker));
        wallWorld.add(mover);
        wallWorld.add(blocker);

        assertFalse(mover.canEnter(0, 1, 0));
        assertFalse(mover.canEnter(2, 1, 0));
    }

    @Test
    public void moveByMovesOntoGroundAndConsumesFood() {
        creature1.setCreatureAi(new CreatureAi(creature1));
        world.add(creature1);
        int startingFood = creature1.food();

        creature1.moveBy(1, 0, 0);

        assertEquals(2, creature1.x);
        assertEquals(1, creature1.y);
        assertEquals(startingFood - 1, creature1.food());
    }

    @Test
    public void moveByDoesNotMoveThroughWallAndConsumesNoPositionChange() {
        Tile[][][] tiles = new Tile[3][3][1];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tiles[x][y][0] = Tile.FLOOR;
            }
        }
        tiles[2][1][0] = Tile.WALL;
        World wallWorld = new World(tiles);
        Creature mover = new Creature(wallWorld, '@', AsciiPanel.brightRed, "Mover", 100, 2, 1);
        mover.x = 1;
        mover.y = 1;
        mover.z = 0;
        mover.setCreatureAi(new CreatureAi(mover));
        wallWorld.add(mover);

        mover.moveBy(1, 0, 0);

        assertEquals(1, mover.x);
        assertEquals(1, mover.y);
    }

    @Test
    public void pickupMovesItemFromWorldIntoInventory() {
        creature1.setCreatureAi(new CreatureAi(creature1));
        world.add(creature1);
        Item item = new Item('$', AsciiPanel.brightRed, "gold", null);
        assertTrue(world.addAtEmptySpace(item, 1, 1, 0));

        creature1.pickup();

        assertNull(world.item(1, 1, 0));
        assertSame(item, creature1.inventory().get(0));
    }

    @Test
    public void pickupWithNoItemLeavesInventoryEmpty() {
        creature1.setCreatureAi(new CreatureAi(creature1));
        world.add(creature1);

        creature1.pickup();

        assertTrue(creature1.inventory().getItems()[0] == null);
    }

    @Test
    public void pickupDoesNotExceedInventoryCapacity() {
        Creature small = new Creature(world, 'S', AsciiPanel.brightRed, "Small", 10, 0, 0);
        small.inventory().add(new Item('!', AsciiPanel.brightRed, "first", null));
        small.inventory().add(new Item('@', AsciiPanel.brightRed, "second", null));
        small.inventory().add(new Item('#', AsciiPanel.brightRed, "third", null));
        small.inventory().add(new Item('$', AsciiPanel.brightRed, "fourth", null));
        small.inventory().add(new Item('%', AsciiPanel.brightRed, "fifth", null));
        small.inventory().add(new Item('&', AsciiPanel.brightRed, "sixth", null));
        small.inventory().add(new Item('*', AsciiPanel.brightRed, "seventh", null));
        small.inventory().add(new Item('+', AsciiPanel.brightRed, "eighth", null));
        small.inventory().add(new Item('-', AsciiPanel.brightRed, "ninth", null));
        small.inventory().add(new Item('/', AsciiPanel.brightRed, "tenth", null));
        small.inventory().add(new Item(':', AsciiPanel.brightRed, "eleventh", null));
        small.inventory().add(new Item(';', AsciiPanel.brightRed, "twelfth", null));
        small.inventory().add(new Item('<', AsciiPanel.brightRed, "thirteenth", null));
        small.inventory().add(new Item('=', AsciiPanel.brightRed, "fourteenth", null));
        small.inventory().add(new Item('>', AsciiPanel.brightRed, "fifteenth", null));
        small.inventory().add(new Item('?', AsciiPanel.brightRed, "sixteenth", null));
        small.inventory().add(new Item('[', AsciiPanel.brightRed, "seventeenth", null));
        small.inventory().add(new Item(']', AsciiPanel.brightRed, "eighteenth", null));
        small.inventory().add(new Item('^', AsciiPanel.brightRed, "nineteenth", null));
        small.inventory().add(new Item('_', AsciiPanel.brightRed, "twentieth", null));
        small.setCreatureAi(new CreatureAi(small));
        small.x = 1;
        small.y = 1;
        small.z = 0;
        world.add(small);
        Item extra = new Item('&', AsciiPanel.brightRed, "extra", null);
        assertTrue(world.addAtEmptySpace(extra, 1, 1, 0));

        small.pickup();

        assertSame(extra, world.item(1, 1, 0));
        assertFalse(small.inventory().contains(extra));
    }

    @Test
    public void equipMovesItemAndUpdatesWeaponOrArmor() {
        creature1.setCreatureAi(new CreatureAi(creature1));
        world.add(creature1);
        Item weapon = new Item(')', AsciiPanel.brightRed, "sword", null);
        weapon.modifyAttackValue(5);
        creature1.inventory().add(weapon);

        creature1.equip(weapon);

        assertSame(weapon, creature1.weapon());
        assertEquals(5, creature1.attackValue());
    }

    @Test
    public void equipArmorUpdatesDefenseAndUnequipClearsSlot() {
        creature1.setCreatureAi(new CreatureAi(creature1));
        world.add(creature1);
        Item armor = new Item('[', AsciiPanel.brightRed, "mail", null);
        armor.modifyDefenseValue(4);
        creature1.inventory().add(armor);

        creature1.equip(armor);

        assertSame(armor, creature1.armor());
        assertEquals(4, creature1.defenseValue());

        creature1.unequip(armor);
        assertNull(creature1.armor());
    }

    @Test
    public void manaModificationClampsToMaxAndZero() {
        creature1.setCreatureAi(new CreatureAi(creature1));

        creature1.modifyMana(100);
        assertEquals(creature1.maxMana(), creature1.mana());

        creature1.modifyMana(-100);
        assertEquals(0, creature1.mana());
    }

    @Test
    public void eatingItemChangesFoodAndRemovesItFromInventory() {
        creature1.setCreatureAi(new CreatureAi(creature1));
        world.add(creature1);
        Item food = new Item('%', AsciiPanel.brightRed, "ration", null);
        food.modifyFoodValue(100);
        creature1.inventory().add(food);
        int startingFood = creature1.food();

        creature1.eat(food);

        assertEquals(startingFood + 100, creature1.food());
        assertFalse(creature1.inventory().contains(food));
    }
}
