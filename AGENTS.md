# Agents

- **Build Environment**: JoshHacks uses GitHub Workflows to spin off CI testing for regression and mutation testing.
  - Regression testing is done with JUnit. Mutation testing is done with PIT.
- **Task Completion Pre-Requirements**: When completing a task, make sure to execute `./mvnw test` and `./mvnw clean compile spotbugs:spotbugs`.
  - Each test must have full line and decision coverage on JUnit results, and SpotBugs findings must be addressed.
  - When ALL basic task requirements are met, and there is full coverage and resolved findings, the task is considered complete.
- **Constraints**: 
  1. Do NOT use warning suppressions because it's convenient. If a SpotBugs finding cannot be resolved because it requires the code to be refactored, then please refactor the code.
  2. Do NOT push changes to GitHub automatically. The branch is automatically protected!
  3. Do NOT run dangerous operations without consulting the user.
