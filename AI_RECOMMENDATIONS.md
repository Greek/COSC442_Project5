# AI Recommendations

Collected from Copilot using MAI Code 2 Flash.

## Priority recommendations

### 1. Test core world and entity behavior
The weakest broad area is the world simulation. `PIT reports` show:

- `World`: 31% line coverage, 5% mutation coverage
- `Creature`: 15% line coverage, 3% mutation coverage
- `Item`, `Effect`, `Spell`, and `Point`: mostly 0% mutation coverage
- `StuffFactory`, `WorldBuilder`, and all AI classes: 0%

Add tests for:

- `World tile()`: in-bounds tiles, negative coordinates, and every upper/lower boundary.
- `World creature(), item(), glyph(), and color()`: precedence among creatures, items, and tiles.
- `World addAtEmptySpace()`: occupied destinations, nearest empty destinations, null items, and failure when no space exists.
- Creature movement, digging, pickup, inventory changes, attacks, equipment, effects, XP, leveling, mana, and death.
- Deterministic world-builder and field-of-view scenarios, rather than relying on random live gameplay.

This is the highest priority because many meaningful game behaviors currently have almost no executable evidence.

### 2. Add tests that specifically kill surviving mutants

PIT found `one surviving conditional-boundary mutant in CombatRules`:

- `calculateNewHp()` currently has a mutant surviving around `newHp > maxHp`.
- The existing `CombatRulesTest` checks decrement, increment, clamping, and exact-max results, but one boundary variation remains unclear or equivalent.

Add a table-driven test set covering values immediately around boundaries:

- Max HP exactly reached
- One above max HP
- One below max HP
- Negative healing/damage
- HP crossing zero
- HP at exact zero
- HP already above max

PIT also reports surviving mutants in `World.tile()` and `addAtEmptySpace()`. Those need direct tests for the mutated conditions, not merely constructing a world and exercising unrelated behavior.

### 3. Improve encapsulation of mutable objects

SpotBugs reported 27 mutable-exposure findings:

- 19 `EI_EXPOSE_REP2`
- 8 `EI_EXPOSE_REP`

The main concentration is `Creature.java`:

- Inventory
- Weapon and armor
- Effects list
- World reference
- AI reference
- Exterior references stored during construction or equipment

Other examples include `Inventory.getItems()`, `Item.writtenSpells()`, `Line.getPoints()`, and `World.tiles`.

Potential improvements include defensive copies, unmodifiable views, or packages exposing only the operations callers need. Tests should verify that modifying a returned collection or array cannot alter game state.

This finding is more significant than a typical style warning because it can permit one subsystem to mutate combat or inventory state indirectly.

### 4. Test AI, pathfinding, visibility, and procedural generation

These areas currently have no meaningful mutation evidence:

- `CreatureAi`: 0%
- `GoblinAi`: 0%
- `FungusAi`: 0%
- `FieldOfView`: 0%
- `PathFinder`: 0%
- `WorldBuilder`: 0%

Prioritize:

- Goblin equipment selection and ranged/throw/visible/pickup fallback decisions.
- Pathfinder behavior for blocked routes, unreachable destinations, and walls.
- Field-of-view behavior around diagonals, walls, depth levels, and out-of-bounds positions.
- World generation invariants, such as ensuring valid stairs and connected regions.
- Deterministic random sources for testing fungi spreading and loot generation.

### 5. Resolve the remaining meaningful static-analysis findings

Lower-priority, but still worth addressing:

- Replace `Math.random() * n` integer calculations with `Random.nextInt(n)` in `Creature.java` and `StuffFactory.java`. SpotBugs found eight instances.
- Use `StringBuilder` in `LevelUpScreen`, which concatenates strings in a loop.
- Add a default case in `TargetBasedScreen` or explicitly document ignored keys.
- Mark the `JFrame` serializable field in `ApplicationMain.java` as transient or remove `Serializable`-related state if appropriate.
- Fix or document the remaining switch/default and random-generation warnings rather than suppressing them wholesale.

### 6. Make quality thresholds enforce the evidence

`PIT configuration` sets `mutationThreshold` to 80, but the latest run completed successfully with only 3% mutation coverage. Before changing the threshold, the test suite should be expanded and the threshold interpreted consistently.

The most valuable sequence is:

1. Add behavior tests around `World`, `Creature`, AI, and pathfinding.
2. Target surviving mutants directly.
3. Re-run PIT and SpotBugs.
4. Raise enforcement gradually toward the intended threshold.
5. Only suppress individual static-analysis findings with clear justification rather than disabling categories broadly.