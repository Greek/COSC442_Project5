# AI Codebase Analysis

Collected from Copilot using MAI Code 2 Flash.

## Weakest areas

1. **Test coverage and mutation strength are extremely low**
   - PIT reports only **11% line coverage**: 131 of 1,223 mutated lines.
   - **3% mutation coverage**: 34 of 989 mutants killed.
   - **911 mutants had no coverage**.
   - Test strength is also low at **44%**.
   - `PIT summary`

   The largest uncovered areas include:
   - `StuffFactory`: 0% line and mutation coverage, 280 lines, 191 mutants.
   - `CreatureAi`: 0% coverage, 88 mutants.
   - `WorldBuilder`: 0% coverage, 115 mutants.
   - `Creature`: 15% line and 3% mutation coverage.

   The overall PIT configuration is **not preventing this weak result**: the run completed successfully despite 3% mutation coverage, even though `pom.xml` specifies an 80% threshold.

2. **JUnit coverage is concentrated in a few utility classes**
   - 45 tests pass with 0 failures and 0 errors.
   - Tests are confined to only `CombatRulesTest`, `CreatureTest`, and `InventoryTest`.
   - This explains why basic correctness appears healthy while the rest of the game remains largely untested.
   - `JUnit test sources`

3. **AI, world generation, combat, and UI behavior have little evidence**
   - No meaningful test coverage appears for creature AI, pathfinding, world construction, factory generation, or UI screens.
   - The 24 PIT classes contain extensive uncovered logic, especially `StuffFactory`, `CreatureAi`, `WorldBuilder`, and `FieldOfView`.
   - `PIT class breakdown`

4. **SpotBugs found 38 issues**
   - `SARIF report`
   - The findings are dominated by mutable-object exposure:
     - 19 `EI_EXPOSE_REP2`
     - 8 `EI_EXPOSE_REP`
   - This is concentrated especially in `Creature.java` with 9 findings and `StuffFactory.java` with 6.
   - Eight findings recommend `nextInt()` instead of `nextDouble()`-based random integer generation.
   - One string-concatenation-in-loop warning and one missing-switch-default warning indicate additional performance and robustness risks.
   - SpotBugs also found a potentially invalid serializable field in `ApplicationMain.java`.

5. **The strongest evidence is for Inventory and CombatRules**
   - `Inventory`: 100% line coverage and 100% mutation coverage.
   - `CombatRules`: 100% line coverage and 88% mutation coverage.
   - These contrast sharply with the rest of the project and suggest testing quality is uneven rather than broadly established.

No dedicated JaCoCo-style coverage plugin is configured in `pom.xml`; the coverage evidence is currently primarily PIT’s line and mutation reporting.
