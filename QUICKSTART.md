# Testing Agent Quick Start Guide

## 🚀 Setup Complete!

Your testing automation environment is now configured with:
- ✅ JaCoCo plugin in [`pom.xml`](pom.xml )
- ✅ MCP tools in [`server.py`](server.py )
- ✅ Workflow guide in `.github/prompts/test-workflow.prompt.md`

## 📊 Current Status

Based on the test run output:
- **Tests Run**: 2300
- **Failures**: 45  
- **Errors**: 38
- **Test Success Rate**: ~96.4%

Main issues detected:
1. Java version compatibility (reflection issues with Java 9+ modules)
2. Some `NullPointerException` errors in SystemUtils (Java version detection)
3. JaCoCo instrumentation issues with newer Java class files

## 🎯 Next Steps

### 1. Generate Initial Coverage Report
```bash
cd /Users/evo/Documents/DePaul/2025/Autumn25/SE333/final_project/codebase
mvn clean test jacoco:report
```

### 2. Start the Agent Loop

Use these prompts with GitHub Copilot Agent (YOLO mode):

**First Prompt:**
```
Using the MCP tools in server.py:
1. Find the JaCoCo report path using jacoco_find_path()
2. Get current coverage percentage using jacoco_coverage()  
3. Get detailed missing coverage using missing_coverage()
4. Show me the top 10 classes with lowest coverage

Document the results and let's create a test generation plan.
```

**Second Prompt:**
```
For the class with 0% coverage from the list:
1. Use analyze_java_class() to understand its structure
2. Check if a test exists using check_test_exists()
3. Generate a comprehensive JUnit test class
4. Save the test file
5. Run mvn test and verify coverage improved

Continue this for the next 5 uncovered classes.
```

**Iteration Prompt:**
```
Review the latest JaCoCo report:
1. Did coverage increase from last run?
2. Are there any test failures to fix?
3. What's the next priority class to test?
4. Generate tests for that class

Repeat until we hit 100% coverage.
```

### 3. Monitor Progress

Track these metrics after each iteration:
- Line coverage %
- Branch coverage %
- Number of uncovered classes
- Test pass/fail count

### 4. Handle Common Issues

**If compilation fails:**
- Check imports
- Verify class exists in classpath
- Check for typos in class/method names

**If tests fail:**
- Review assertions
- Check for null handling
- Verify expected vs actual values
- Update test logic

**If coverage doesn't increase:**
- Add more test cases
- Test edge cases
- Test exception paths
- Test different branches

## 🛠️ MCP Tool Examples

### Find all source files
```python
files = find_source_files("/Users/evo/Documents/DePaul/2025/Autumn25/SE333/final_project/codebase")
print(f"Found {len(files)} source files")
```

### Get coverage stats
```python
jacoco_path = jacoco_find_path("/Users/evo/Documents/DePaul/2025/Autumn25/SE333/final_project/codebase")
coverage = jacoco_coverage(jacoco_path)
print(f"Current coverage: {coverage}%")
```

### Analyze a class
```python
class_info = analyze_java_class("/path/to/SourceClass.java")
print(f"Found {class_info['total_methods']} methods to test")
```

## 📈 Success Metrics

Target goals:
- **Minimum**: 80% line coverage
- **Good**: 90% line coverage
- **Excellent**: 95%+ line coverage
- **Perfect**: 100% line coverage

## 🎓 Project Deliverables

Make sure to track:
1. **Initial coverage baseline** (document current %)
2. **Test generation process** (which classes you targeted)
3. **Final coverage achieved**
4. **MCP tools created** (document any new tools you build)
5. **Challenges and solutions** (what worked, what didn't)

## 💡 Pro Tips

1. **Batch Processing**: Generate tests for 5 classes, then run mvn test
2. **Incremental Validation**: Check coverage after each batch
3. **Focus on Impact**: Prioritize classes with many uncovered lines
4. **Don't Perfect**: If a class gets to 90%, move on and come back later
5. **Tool Reuse**: If you find yourself repeating a task, create an MCP tool for it!

## 🔗 Important Paths

- **POM**: `/Users/evo/Documents/DePaul/2025/Autumn25/SE333/final_project/codebase/pom.xml`
- **MCP Server**: `/Users/evo/Documents/DePaul/2025/Autumn25/SE333/final_project/codebase/server.py`
- **Source Code**: `/Users/evo/Documents/DePaul/2025/Autumn25/SE333/final_project/codebase/src/main/java`
- **Tests**: `/Users/evo/Documents/DePaul/2025/Autumn25/SE333/final_project/codebase/src/test/java`
- **JaCoCo Report**: `/Users/evo/Documents/DePaul/2025/Autumn25/SE333/final_project/codebase/target/site/jacoco/jacoco.xml`

---

**Ready to start?** Run the first prompt above and let's achieve 100% coverage! 🎯
