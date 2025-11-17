chmod +x scripts/git_mcp.sh# 🧪 Testing Agent with MCP Tools

## Overview

This project demonstrates automated test generation for Java projects using:
- **GitHub Copilot** in Agent Mode (YOLO/Auto-Approve)
- **Model Context Protocol (MCP)** tools for reusable automation
- **JaCoCo** for coverage analysis
- **JUnit 4** for testing
- **Maven** for build automation

## 📁 Project Structure

```
codebase/
├── README_TESTING_AGENT.md   # This file
├── QUICKSTART.md              # Getting started guide
├── PROJECT_SUMMARY.md         # Detailed project summary
├── PROGRESS_CHECKLIST.md      # Track your progress
├── [`server.py`](server.py )                  # MCP tools server
├── [`pom.xml`](pom.xml )                   # Maven config with JaCoCo
├── .github/prompts/
│   ├── tester.prompt.md       # Original instructions
│   └── test-workflow.prompt.md # Detailed workflow
└── src/
    ├── main/java/             # Source code to test
    └── test/java/             # Test code
```

## 🚀 Quick Start

### 1. Initial Setup (DONE ✅)
The project is already configured with:
- JaCoCo plugin in Maven
- 8 MCP tools for automation
- Comprehensive documentation

### 2. Generate Baseline Coverage
```bash
cd /Users/evo/Documents/DePaul/2025/Autumn25/SE333/final_project/codebase
mvn clean test jacoco:report
```

### 3. Start Testing with Copilot Agent

**Prompt 1: Analyze Current State**
```
@workspace Using the MCP tools in server.py, analyze the current test coverage:
1. Find the JaCoCo report with jacoco_find_path()
2. Get the current coverage percentage with jacoco_coverage()
3. Get detailed uncovered classes with missing_coverage()
4. Show me the top 10 classes that need tests

Then create a plan for achieving 100% coverage.
```

**Prompt 2: Generate Tests (Repeat)**
```
For the next uncovered class:
1. Use analyze_java_class() to understand its structure
2. Generate a comprehensive JUnit test class covering all methods
3. Save the test file to the correct location
4. Run mvn test and show results
5. Verify coverage improved

Continue with the next 4 classes.
```

## 🛠️ Available MCP Tools

| Tool | Purpose |
|------|---------|
| `find_source_files()` | Find all Java source files in the project |
| `analyze_java_class()` | Extract methods, fields, and structure |
| `jacoco_find_path()` | Locate the JaCoCo XML report |
| `jacoco_coverage()` | Get overall coverage percentage |
| `missing_coverage()` | Get detailed uncovered code segments |
| `check_test_exists()` | Check if a test file already exists |
| `get_uncovered_methods()` | Find methods that need testing |
| `add()` | Example tool (adds two numbers) |

## 📊 Coverage Goals

| Level | Coverage | Grade Range |
|-------|----------|-------------|
| Minimum | 70-79% | 70-79 |
| Good | 80-89% | 80-89 |
| Excellent | 90-95% | 90-95 |
| Outstanding | 95-100% | 95-100 |

## 📚 Documentation Files

- **[QUICKSTART.md](QUICKSTART.md)** - Setup guide and first steps
- **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - Complete project overview
- **[PROGRESS_CHECKLIST.md](PROGRESS_CHECKLIST.md)** - Track your progress
- **[.github/prompts/test-workflow.prompt.md](.github/prompts/test-workflow.prompt.md)** - Detailed workflow
- **[.github/prompts/tester.prompt.md](.github/prompts/tester.prompt.md)** - Original instructions

## 🎯 Workflow Summary

1. **Analyze**: Use MCP tools to find uncovered code
2. **Generate**: Create JUnit tests for uncovered classes
3. **Validate**: Run `mvn test` and check coverage
4. **Iterate**: Repeat until target coverage achieved
5. **Document**: Track progress in PROGRESS_CHECKLIST.md

## 💡 Key Features

### 1. Reusable MCP Tools
Instead of repeating searches and analysis, use MCP tools:
```python
# Find all source files (do once, cache results)
files = find_source_files(".")

# Analyze specific class
info = analyze_java_class("src/main/java/.../MyClass.java")

# Check coverage
coverage = jacoco_coverage(jacoco_find_path("."))
```

### 2. Iterative Development
Work in batches:
- Generate tests for 5 classes
- Run `mvn test`
- Check coverage improved
- Fix any issues
- Move to next 5

### 3. Automated Analysis
Tools automatically:
- Find uncovered code
- Extract method signatures
- Identify test gaps
- Calculate coverage percentages

## 🔧 Common Commands

```bash
# Clean build and test with coverage
mvn clean test jacoco:report

# Run specific test
mvn test -Dtest=ClassNameTest

# Compile without testing
mvn clean compile -DskipTests

# View coverage HTML report
open target/site/jacoco/index.html
```

## 🐛 Troubleshooting

### Tests fail to run
```bash
# Clean everything and rebuild
rm -rf target/
mvn clean compile test
```

### Coverage report missing
```bash
# Ensure JaCoCo ran
mvn clean test jacoco:report
ls -la target/site/jacoco/jacoco.xml
```

### MCP tools not working
```bash
# Restart the MCP server
cd /path/to/codebase
python server.py
```

## 📖 Learning Resources

- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [FastMCP Documentation](https://github.com/jlowin/fastmcp)

## ✅ Success Checklist

- [ ] Generated baseline coverage report
- [ ] Identified uncovered classes
- [ ] Created/enhanced test files
- [ ] Achieved target coverage (70%+)
- [ ] All tests pass
- [ ] Documented process
- [ ] Created any additional MCP tools needed

## 🎓 Deliverables

For your course submission, include:
1. Final coverage report (from JaCoCo)
2. List of classes tested
3. Any new MCP tools created
4. Completed PROGRESS_CHECKLIST.md
5. Summary of challenges and solutions

## 🤝 Getting Help

If you're stuck:
1. Check QUICKSTART.md for setup issues
2. Review test-workflow.prompt.md for process
3. Look at existing test files for examples
4. Use `@workspace` with Copilot to search for relevant code
5. Ask specific questions about Java/JUnit/Maven

---

**Ready to start?** Open QUICKSTART.md and begin with Step 1!

**Need guidance?** Review test-workflow.prompt.md for detailed instructions.

**Track progress?** Use PROGRESS_CHECKLIST.md to stay organized.

Good luck achieving 100% coverage! 🚀🎯
