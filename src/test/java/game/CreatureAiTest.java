package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import asciiPanel.AsciiPanel;

class CreatureAiTest {
    private World world;
    private Creature creature;
    private CreatureAi ai;

    @BeforeEach
    void setUp() {
        Tile[][][] tiles = new Tile[7][7][1];
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                tiles[x][y][0] = Tile.FLOOR;
            }
        }
        world = new World(tiles);
        creature = new Creature(world, '@', AsciiPanel.brightRed, "hero", 100, 5, 2);
        creature.x = 3;
        creature.y = 3;
        creature.z = 0;
        ai = new CreatureAi(creature);
        world.add(creature);
    }

    @Test
    void itemNamesUseConfiguredNameAndDefaultToAppearance() {
        Item item = new Item('!', Color.WHITE, "potion", "vial");

        assertEquals("vial", ai.getName(item));
        ai.setName(item, "magic potion");
        assertEquals("magic potion", ai.getName(item));
    }

    @Test
    void onEnterMovesOntoGroundAndBumpsIntoWalls() {
        ai.onEnter(4, 3, 0, Tile.FLOOR);
        assertEquals(4, creature.x);
        assertEquals(3, creature.y);

        ai.onEnter(5, 3, 0, Tile.WALL);
        assertEquals(4, creature.x);
    }

    @Test
    void canSeeUsesDepthRadiusAndLineOfSight() {
        assertTrue(ai.canSee(3, 3, 0));
        assertTrue(ai.canSee(4, 3, 0));
        assertFalse(ai.canSee(3, 3, 1));

        world.dig(3, 3, 0);
        assertTrue(ai.canSee(3, 3, 0));

        world.dig(4, 3, 0);
    }

    @Test
    void wanderDoesNotEnterWallOrOccupiedLocation() {
        Tile[][][] tiles = new Tile[3][3][1];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tiles[x][y][0] = Tile.FLOOR;
            }
        }
        tiles[1][2][0] = Tile.WALL;
        World isolatedWorld = new World(tiles);
        Creature isolated = new Creature(isolatedWorld, '@', AsciiPanel.brightRed, "isolated", 10, 1, 1);
        isolated.x = 1;
        isolated.y = 1;
        isolated.z = 0;
        CreatureAi isolatedAi = new CreatureAi(isolated);
        isolatedWorld.add(isolated);

        isolatedAi.wander();

        assertTrue(isolatedWorld.tile(isolated.x, isolated.y, isolated.z).isGround());
    }

    @Test
    void equipmentHelpersChooseWeaponArmorAndThrownItems() {
        Item sword = new Item(')', Color.WHITE, "sword", null);
        sword.modifyAttackValue(8);
        sword.modifyThrownAttackValue(2);
        Item armor = new Item('[', Color.WHITE, "armor", null);
        armor.modifyDefenseValue(6);
        Item rock = new Item(',', Color.WHITE, "rock", null);
        rock.modifyThrownAttackValue(5);

        creature.inventory().add(sword);
        creature.inventory().add(armor);
        creature.inventory().add(rock);

        assertTrue(ai.canUseBetterEquipment());
        ai.useBetterEquipment();
        assertEquals(13, creature.attackValue());
        assertEquals(8, creature.defenseValue());

        assertSame(rock, ai.getWeaponToThrow());
        assertTrue(ai.canThrowAt(creature));
    }

    @Test
    void pickupAndRangedHelpersReflectWorldAndEquipmentState() {
        Item potion = new Item('!', Color.WHITE, "potion", null);
        world.addAtEmptySpace(potion, 3, 3, 0);
        creature.inventory().add(potion);

        assertTrue(ai.canPickup());

        Item bow = new Item(')', Color.WHITE, "bow", null);
        bow.modifyRangedAttackValue(5);
        creature.inventory().add(bow);
        creature.equip(bow);
        assertTrue(ai.canRangedWeaponAttack(creature));
    }

    @Test
    void canSeeAtSelfAndBeyondWallIsDeterministic() {
        assertTrue(ai.canSee(3, 3, 0));
        world.dig(4, 3, 0);
        world.dig(5, 3, 0);
        assertTrue(ai.canSee(5, 3, 0));
    }

    @Test
    void huntMovesTowardTargetUsingPathfinder() {
        Creature target = new Creature(world, 'Z', AsciiPanel.brightRed, "target", 20, 2, 1);
        target.x = 5;
        target.y = 3;
        target.z = 0;
        world.add(target);

        ai.hunt(target);

        assertTrue(target.x != creature.x || target.y != creature.y);
        assertTrue(creature.canSee(target.x, target.y, target.z));
    }
}
