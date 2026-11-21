package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.List;

import org.junit.jupiter.api.Test;

import asciiPanel.AsciiPanel;

class ValueObjectTest {
    @Test
    void effectTracksDurationCopiesAndUpdates() {
        Effect original = new Effect(3);
        Effect copy = new Effect(original);

        assertFalse(original.isDone());
        copy.update(null);
        assertFalse(copy.isDone());
        copy.update(null);
        assertFalse(copy.isDone());
        copy.update(null);
        assertTrue(copy.isDone());
        assertFalse(original.isDone());
    }

    @Test
    void spellCopiesEffectAndExposesNameManaCostAndTargetRequirement() {
        Effect source = new Effect(4);
        Spell spell = new Spell("heal", 8, source);
        Effect result = spell.effect();

        assertEquals("heal", spell.name());
        assertEquals(8, spell.manaCost());
        assertTrue(spell.requiresTarget());
        assertNotSame(source, result);
        assertFalse(result.isDone());
    }

    @Test
    void itemConstructorModifiersSpellAndDetailsAndAppearance() {
        Item item = new Item(')', Color.WHITE, "sword", null);
        Item hiddenAppearance = new Item('!', AsciiPanel.brightRed, "red potion", "vial");

        assertEquals("sword", item.name());
        assertEquals("sword", item.appearance());
        assertEquals("vial", hiddenAppearance.appearance());
        assertEquals(1, item.thrownAttackValue());

        item.modifyAttackValue(5);
        item.modifyDefenseValue(2);
        item.modifyThrownAttackValue(3);
        item.modifyRangedAttackValue(4);
        item.modifyFoodValue(-10);
        item.addWrittenSpell("heal", 4, new Effect(1));

        assertEquals(5, item.attackValue());
        assertEquals(2, item.defenseValue());
        assertEquals(4, item.thrownAttackValue());
        assertEquals(4, item.rangedAttackValue());
        assertEquals(-10, item.foodValue());
        assertEquals("heal", item.writtenSpells().get(0).name());
        assertTrue(item.details().contains("attack:5"));
        assertTrue(item.details().contains("thrown:4"));
        assertTrue(item.details().contains("ranged:4"));
        assertTrue(item.details().contains("defense:2"));
        assertTrue(item.details().contains("food:-10"));

        Effect quaffEffect = new Effect(2);
        item.setQuaffEffect(quaffEffect);
        assertSame(quaffEffect, item.quaffEffect());
    }

    @Test
    void itemDetailsAreEmptyForDefaultItemValues() {
        Item item = new Item('%', Color.WHITE, "bread", null);

        assertEquals("", item.details());
    }

    @Test
    void pointEqualityHashCodeAndNeighbors() {
        Point point = new Point(2, 3, 1);
        Point same = new Point(2, 3, 1);
        Point different = new Point(2, 4, 1);

        assertEquals(point, same);
        assertEquals(point.hashCode(), same.hashCode());
        assertNotEquals(point, different);
        assertNotEquals(point, null);
        assertFalse(point.equals("not a point"));

        List<Point> neighbors = point.neighbors8();
        assertEquals(8, neighbors.size());
        assertFalse(neighbors.contains(point));
        assertEquals(8, neighbors.stream().distinct().count());

        assertTrue(neighbors.contains(new Point(1, 2, 1)));
        assertTrue(neighbors.contains(new Point(3, 4, 1)));
    }

}
