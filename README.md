# SE333 Final Project: Automated Testing Agent with MCP Tools

## Overview

This project implements an automated test generation system for Java projects using:
- **GitHub Copilot** in Agent Mode (YOLO/Auto-Approve)
- **Model Context Protocol (MCP)** tools for reusable automation
- **JaCoCo** for coverage analysis
- **JUnit 4** for testing
- **Maven** for build automation

## Project Structure

```
se333-final-project/
├── README.md                  # Project overview (this file)
├── docs/                      # All markdown docs and example scripts
│   ├── QUICKSTART.md
│   ├── PROJECT_SUMMARY.md
│   ├── PROGRESS_CHECKLIST.md
│   ├── PHASE_3_4_5_DOCUMENTATION.md
│   └── IMPLEMENTATION_SUMMARY.md
├── server.py                  # MCP tools server (19 tools)
├── codebase/                  # Java project (moved here)
│   ├── pom.xml                # Maven configuration with JaCoCo
│   └── src/
│       ├── main/java/         # Source code to test
│       └── test/java/         # Test code
├── .github/prompts/
│   ├── tester.prompt.md       # Agent instructions
│   └── test-workflow.prompt.md # Detailed workflow
```

## Project Status

**Status: Complete**

The project has been fully implemented with all phases complete:
- Phase 1-2: Core testing tools (10 MCP tools)
- Phase 3: Git integration tools (5 MCP tools)
- Phase 4: Intelligent test iteration (2 MCP tools)
- Phase 5: Creative extensions (2 MCP tools)

**Total: 19 MCP Tools**

Current test coverage: 94.13% instruction coverage (51,743/54,972 instructions covered)

## Quick Start

### Prerequisites

- Java Development Kit (JDK)
- Maven 3.x
- Python 3.x (for MCP server)
- GitHub Copilot access

### Setup

1. Clone the repository
2. Install dependencies: `mvn clean install`
3. Generate initial coverage report: `mvn -f codebase/pom.xml clean test jacoco:report`
4. Start MCP server: `python server.py`

### Usage

The project uses MCP tools accessible through GitHub Copilot Agent Mode. See QUICKSTART.md for detailed usage instructions.

## Available MCP Tools

**Total: 19 MCP Tools** (see IMPLEMENTATION_SUMMARY.md for complete list)

### Phase 1-2: Core Testing Tools
- `find_source_files()` - Find all Java source files
- `analyze_java_class()` - Extract methods, fields, and structure
- `jacoco_find_path()` - Locate the JaCoCo XML report
- `jacoco_coverage()` - Get overall coverage percentage
- `missing_coverage()` - Get detailed uncovered code segments
- `check_test_exists()` - Check if a test file already exists
- `get_uncovered_methods()` - Find methods that need testing
- `search_codebase()` - Search for text patterns in source files
- `get_class_coverage_summary()` - Get coverage summary for all classes
- `read_java_file()` - Read Java source files

### Phase 3: Git Integration Tools
- `git_status()` - Get repository status
- `git_add_all()` - Stage changes with intelligent filtering
- `git_commit()` - Automated commit with coverage stats
- `git_push()` - Push to remote repository
- `git_pull_request()` - Create PR with standardized templates

### Phase 4: Intelligent Test Iteration
- `detect_bugs_from_tests()` - Analyze test failures to detect bugs
- `generate_quality_metrics_dashboard()` - Track coverage and test quality

### Phase 5: Creative Extensions
- `generate_specification_based_tests()` - Generate tests using boundary value analysis
- `ai_code_review()` - Perform static analysis and code review

## Coverage Goals

| Level | Coverage | Grade Range |
|-------|----------|-------------|
| Minimum | 70-79% | 70-79 |
| Good | 80-89% | 80-89 |
| Excellent | 90-95% | 90-95 |
| Outstanding | 95-100% | 95-100 |

**Current Achievement: Excellent (94.13%)**

## Documentation Files

- **[README.md](README.md)** - Project overview (this file)
- **[QUICKSTART.md](docs/QUICKSTART.md)** - Setup guide and first steps
- **[PROJECT_SUMMARY.md](docs/PROJECT_SUMMARY.md)** - Complete project overview
- **[PROGRESS_CHECKLIST.md](docs/PROGRESS_CHECKLIST.md)** - Development progress tracking
- **[PHASE_3_4_5_DOCUMENTATION.md](docs/PHASE_3_4_5_DOCUMENTATION.md)** - Comprehensive Phase 3-5 documentation
- **[IMPLEMENTATION_SUMMARY.md](docs/IMPLEMENTATION_SUMMARY.md)** - Quick reference for all implementations
- **[.github/prompts/test-workflow.prompt.md](.github/prompts/test-workflow.prompt.md)** - Detailed workflow
- **[.github/prompts/tester.prompt.md](.github/prompts/tester.prompt.md)** - Agent instructions

## Workflow Summary

1. **Analyze**: Use MCP tools to find uncovered code
2. **Generate**: Create JUnit tests for uncovered classes
3. **Validate**: Run `mvn test` and check coverage
4. **Iterate**: Repeat until target coverage achieved
5. **Document**: Track progress in PROGRESS_CHECKLIST.md

## Key Features

### 1. Reusable MCP Tools

MCP tools provide reusable automation capabilities:

```python
# Find all source files (point at codebase/ if needed)
files = find_source_files("codebase")

# Analyze specific class
info = analyze_java_class("src/main/java/.../MyClass.java")

# Check coverage
coverage = jacoco_coverage(jacoco_find_path("."))
```

### 2. Iterative Development

The workflow supports batch processing:
- Generate tests for multiple classes
- Run `mvn test`
- Check coverage improvement
- Fix any issues
- Continue with next batch

### 3. Automated Analysis

Tools automatically:
- Find uncovered code
- Extract method signatures
- Identify test gaps
- Calculate coverage percentages

## Common Commands

```bash
# Clean build and test with coverage (for Java project in `codebase/`)
mvn -f codebase/pom.xml clean test jacoco:report

# Run specific test
mvn test -Dtest=ClassNameTest

# Compile without testing
mvn clean compile -DskipTests

# View coverage HTML report
open codebase/target/site/jacoco/index.html
```

## Troubleshooting

### Tests fail to run
```bash
# Clean everything and rebuild (Java build artifacts under codebase/target)
rm -rf codebase/target/
mvn -f codebase/pom.xml clean compile test
```

### Coverage report missing
```bash
# Ensure JaCoCo ran (run in codebase)
mvn -f codebase/pom.xml clean test jacoco:report
ls -la codebase/target/site/jacoco/jacoco.xml
```

### MCP tools not working
```bash
# Restart the MCP server
python server.py
```

## Learning Resources

- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [FastMCP Documentation](https://github.com/jlowin/fastmcp)

## Project Deliverables

The project includes the following deliverables:

1. **MCP Tools Server** (`server.py`) - 19 implemented tools
2. **Test Coverage** - 94.13% instruction coverage achieved
3. **Documentation** - Comprehensive documentation for all phases
4. **Implementation Summary** - Complete reference of all tools
5. **Progress Tracking** - Development progress documented

## Project Completion

All phases have been successfully completed:
- Phase 1-2: Core testing infrastructure
- Phase 3: Git integration and workflow automation
- Phase 4: Intelligent test iteration and bug detection
- Phase 5: Creative extensions for test generation and code review

The project demonstrates a complete automated testing workflow from test generation through code review to version control integration.

For detailed implementation information, see:
- PHASE_3_4_5_DOCUMENTATION.md for comprehensive documentation
- IMPLEMENTATION_SUMMARY.md for quick reference
