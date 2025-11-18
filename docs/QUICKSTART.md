# Testing Agent Quick Start Guide

## Setup Status

The testing automation environment is configured with:
- JaCoCo plugin in `codebase/pom.xml`
- MCP tools in `server.py`
- Workflow guide in `.github/prompts/test-workflow.prompt.md` and `.github/prompts/tester.prompt.md`

## Current Status

Based on test execution:
- **Tests Run**: 2300
- **Failures**: 45  
- **Errors**: 38
- **Test Success Rate**: 96.4%

Known issues:
1. Java version compatibility (reflection issues with Java 9+ modules)
2. Some `NullPointerException` errors in SystemUtils (Java version detection)
3. JaCoCo instrumentation issues with newer Java class files

## Usage Instructions

### 1. Generate Initial Coverage Report

```bash
mvn -f codebase/pom.xml clean test jacoco:report
```

### 2. Using the Agent Workflow

The project uses GitHub Copilot Agent (YOLO mode) with MCP tools. Example workflow:

**Initial Analysis:**
```
Using the MCP tools in server.py:
1. Find the JaCoCo report path using jacoco_find_path()
2. Get current coverage percentage using jacoco_coverage()  
3. Get detailed missing coverage using missing_coverage()
4. Show the top 10 classes with lowest coverage

Document the results and create a test generation plan.
```

**Test Generation:**
```
For the class with lowest coverage from the list:
1. Use analyze_java_class() to understand its structure
2. Check if a test exists using check_test_exists()
3. Generate a comprehensive JUnit test class
4. Save the test file
5. Run mvn test and verify coverage improved

Continue this for the next 5 uncovered classes.
```

**Iteration:**
```
Review the latest JaCoCo report:
1. Did coverage increase from last run?
2. Are there any test failures to fix?
3. What's the next priority class to test?
4. Generate tests for that class
```

### 3. Monitoring Progress

Track these metrics after each iteration:
- Line coverage percentage
- Branch coverage percentage
- Number of uncovered classes
- Test pass/fail count

### 4. Common Issues

**Compilation failures:**
- Check imports
- Verify class exists in classpath
- Check for typos in class/method names

**Test failures:**
- Review assertions
- Check for null handling
- Verify expected vs actual values
- Update test logic

**Coverage not increasing:**
- Add more test cases
- Test edge cases
- Test exception paths
- Test different branches

## MCP Tool Examples

### Find all source files
```python
files = find_source_files(".")
print(f"Found {len(files)} source files")
```

### Get coverage stats
```python
jacoco_path = jacoco_find_path(".")
coverage = jacoco_coverage(jacoco_path)
print(f"Current coverage: {coverage}%")
```

### Analyze a class
```python
class_info = analyze_java_class("/path/to/SourceClass.java")
print(f"Found {class_info['total_methods']} methods to test")
```

## Success Metrics

Target goals:
- **Minimum**: 80% line coverage
- **Good**: 90% line coverage
- **Excellent**: 95%+ line coverage
- **Perfect**: 100% line coverage

**Current Achievement**: 94.13% instruction coverage

## Project Deliverables

The project tracks:
1. Initial coverage baseline
2. Test generation process and targeted classes
3. Final coverage achieved
4. MCP tools created
5. Challenges and solutions encountered

## Best Practices

1. **Batch Processing**: Generate tests for multiple classes, then run mvn test
2. **Incremental Validation**: Check coverage after each batch
3. **Focus on Impact**: Prioritize classes with many uncovered lines
4. **Iterative Approach**: Achieve reasonable coverage per class, then iterate
5. **Tool Reuse**: Create MCP tools for repetitive tasks

## Important Paths

All paths are relative to the project root; the Java project is in `codebase/`:
- **POM**: `codebase/pom.xml`
- **MCP Server**: `server.py`
- **Source Code**: `codebase/src/main/java`
- **Tests**: `codebase/src/test/java`
- **JaCoCo Report**: `codebase/target/site/jacoco/jacoco.xml`
