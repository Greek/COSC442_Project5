package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StuffFactoryTest {
    private World world;
    private StuffFactory factory;

    @BeforeEach
    void setUp() {
        Tile[][][] tiles = new Tile[8][8][3];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 3; z++) {
                    tiles[x][y][z] = Tile.FLOOR;
                }
            }
        }
        world = new World(tiles);
        factory = new StuffFactory(world);
    }

    @Test
    void factoryCreatesPlayerAndBaseMonstersWithExpectedIdentity() {
        Creature player = factory.newPlayer(new ArrayList<>(), new FieldOfView(world));
        Creature fungus = factory.newFungus(0);
        Creature bat = factory.newBat(1);
        Creature zombie = factory.newZombie(2, player);
        Creature goblin = factory.newGoblin(1, player);

        assertEquals('@', player.glyph());
        assertEquals("player", player.name());
        assertTrue(player.hp() > 0);

        assertEquals('f', fungus.glyph());
        assertEquals('b', bat.glyph());
        assertEquals('z', zombie.glyph());
        assertEquals('g', goblin.glyph());
        assertNotNull(goblin.weapon());
        assertNotNull(goblin.armor());
    }

    @Test
    void factoryCreatesItemsWithExpectedModifiers() {
        assertEquals(400, factory.newBread(0).foodValue());
        assertEquals(100, factory.newFruit(0).foodValue());
        assertEquals(5, factory.newDagger(0).attackValue());
        assertEquals(10, factory.newSword(0).attackValue());
        assertEquals(5, factory.newStaff(0).attackValue());
        assertEquals(5, factory.newBow(0).rangedAttackValue());
        assertEquals(100, factory.newEdibleWeapon(0).foodValue());
        assertEquals(2, factory.newLightArmor(0).defenseValue());
        assertEquals(4, factory.newMediumArmor(0).defenseValue());
        assertEquals(6, factory.newHeavyArmor(0).defenseValue());
        assertEquals("teddy bear", factory.newVictoryItem(0).name());
        assertEquals(6, factory.newRock(0).thrownAttackValue());
    }

    @Test
    void factoryCreatesPotionAndSpellbookTypes() {
        assertEquals("health potion", factory.newPotionOfHealth(0).name());
        assertEquals("mana potion", factory.newPotionOfMana(0).name());
        assertEquals("slow health potion", factory.newPotionOfSlowHealth(0).name());
        assertEquals("poison potion", factory.newPotionOfPoison(0).name());
        assertEquals("warrior's potion", factory.newPotionOfWarrior(0).name());
        assertEquals("archers potion", factory.newPotionOfArcher(0).name());
        assertEquals("experience potion", factory.newPotionOfExperience(0).name());

        Item white = factory.newWhiteMagesSpellbook(0);
        assertEquals(4, white.writtenSpells().size());
        assertTrue(white.writtenSpells().stream().anyMatch(spell -> spell.name().equals("minor heal")));

        Item blue = factory.newBlueMagesSpellbook(0);
        assertEquals(4, blue.writtenSpells().size());
        assertTrue(blue.writtenSpells().stream().anyMatch(spell -> spell.name().equals("blood to mana")));
    }

    @Test
    void healthPotionHealsDamagedCreatureWithoutExceedingMaxHp() {
        Creature creature = new Creature(world, '@', java.awt.Color.WHITE, "hero", 100, 2, 1);
        new CreatureAi(creature);
        creature.modifyHp(-30, "test");
        Item potion = factory.newPotionOfHealth(0);

        potion.quaffEffect().start(creature);

        assertEquals(85, creature.hp());
    }

    @Test
    void manaPotionAddsManaWithoutExceedingMaximum() {
        Creature creature = new Creature(world, '@', java.awt.Color.WHITE, "hero", 100, 2, 1);
        new CreatureAi(creature);
        creature.modifyMana(-3);
        Item potion = factory.newPotionOfMana(0);

        potion.quaffEffect().start(creature);

        assertEquals(5, creature.mana());
    }

    @Test
    void warriorPotionAddsAndRemovesTemporaryModifiers() {
        Creature creature = new Creature(world, '@', java.awt.Color.WHITE, "hero", 100, 2, 1);
        new CreatureAi(creature);
        Item potion = factory.newPotionOfWarrior(0);
        Effect effect = potion.quaffEffect();

        effect.start(creature);
        assertEquals(7, creature.attackValue());
        assertEquals(6, creature.defenseValue());

        effect.end(creature);
        assertEquals(2, creature.attackValue());
        assertEquals(1, creature.defenseValue());
    }

    @Test
    void archerPotionAddsAndRemovesVisionRadius() {
        Creature creature = new Creature(world, '@', java.awt.Color.WHITE, "hero", 100, 2, 1);
        new CreatureAi(creature);
        Item potion = factory.newPotionOfArcher(0);
        Effect effect = potion.quaffEffect();

        effect.start(creature);
        assertEquals(12, creature.visionRadius());

        effect.end(creature);
        assertEquals(9, creature.visionRadius());
    }

    @Test
    void randomFactoriesReturnSupportedTypes() {
        assertTrue(factory.randomWeapon(0) instanceof Item);
        assertTrue(factory.randomArmor(0) instanceof Item);
        assertTrue(factory.randomPotion(0) instanceof Item);
        assertTrue(factory.randomSpellBook(0) instanceof Item);
    }
}
