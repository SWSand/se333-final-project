#!/usr/bin/env python3
"""
Example script showing how to use the MCP tools for test generation.

This demonstrates the workflow without using GitHub Copilot,
but shows the same process the agent would follow.
"""

import sys
import os

# Add the codebase directory to path so we can import server
sys.path.insert(0, '/Users/evo/Documents/DePaul/2025/Autumn25/SE333/final_project/codebase')

from server import (
    find_source_files,
    analyze_java_class,
    jacoco_find_path,
    jacoco_coverage,
    missing_coverage,
    check_test_exists,
    get_uncovered_methods
)

def main():
    workspace = "/Users/evo/Documents/DePaul/2025/Autumn25/SE333/final_project/codebase"
    
    print("=" * 80)
    print("TESTING AGENT - MCP TOOLS DEMONSTRATION")
    print("=" * 80)
    
    # Step 1: Find all source files
    print("\n1️⃣  Finding all Java source files...")
    source_files = find_source_files(workspace)
    print(f"   Found {len(source_files)} source files")
    
    # Show first 5
    print("\n   First 5 files:")
    for i, file_info in enumerate(source_files[:5], 1):
        print(f"   {i}. {file_info['package']}.{file_info['class_name']}")
    
    # Step 2: Find JaCoCo report
    print("\n2️⃣  Looking for JaCoCo coverage report...")
    jacoco_path = jacoco_find_path(workspace)
    
    if os.path.exists(jacoco_path):
        print(f"   ✅ Found report at: {jacoco_path}")
    else:
        print(f"   ❌ Report not found: {jacoco_path}")
        print("   Run: mvn clean test jacoco:report")
        return
    
    # Step 3: Get overall coverage
    print("\n3️⃣  Analyzing overall coverage...")
    coverage_pct = jacoco_coverage(jacoco_path)
    print(f"   Current coverage: {coverage_pct:.2f}%")
    
    # Step 4: Get detailed coverage info
    print("\n4️⃣  Getting detailed coverage breakdown...")
    coverage_details = missing_coverage(jacoco_path)
    
    if "error" in coverage_details:
        print(f"   ❌ Error: {coverage_details['error']}")
        return
    
    print(f"   Total instructions: {coverage_details['total_instructions']}")
    print(f"   Covered instructions: {coverage_details['covered_instructions']}")
    print(f"   Missed instructions: {coverage_details['missed_instructions']}")
    print(f"   Line coverage: {coverage_details['line_coverage_percentage']:.2f}%")
    print(f"   Branch coverage: {coverage_details['branch_coverage_percentage']:.2f}%")
    print(f"   Uncovered classes: {coverage_details['total_uncovered_classes']}")
    
    # Step 5: Show top 10 uncovered classes
    print("\n5️⃣  Top 10 classes needing tests:")
    uncovered = coverage_details['uncovered_classes'][:10]
    
    for i, cls in enumerate(uncovered, 1):
        print(f"   {i:2d}. {cls['package']}.{cls['class']}")
        print(f"       Coverage: {cls['coverage_percentage']:.1f}%")
        print(f"       Missed lines: {len(cls['missed_lines'])}")
        print(f"       Lines: {cls['missed_lines'][:10]}" + 
              ("..." if len(cls['missed_lines']) > 10 else ""))
    
    # Step 6: Analyze a specific class
    if source_files:
        print("\n6️⃣  Analyzing first source file in detail...")
        first_file = source_files[0]
        file_path = first_file['file_path']
        
        print(f"   File: {file_path}")
        class_info = analyze_java_class(file_path)
        
        if "error" in class_info:
            print(f"   ❌ Error: {class_info['error']}")
        else:
            print(f"   Total methods found: {class_info['total_methods']}")
            
            # Show first 5 methods
            methods = class_info['methods'][:5]
            if methods:
                print("   First 5 methods:")
                for method in methods:
                    print(f"      - {method['visibility']} {method['signature'][:60]}")
        
        # Check if test exists
        test_path = first_file['test_path']
        test_exists = check_test_exists(test_path)
        print(f"\n   Test file exists: {'✅ Yes' if test_exists else '❌ No'}")
        print(f"   Expected at: {test_path}")
    
    # Summary
    print("\n" + "=" * 80)
    print("SUMMARY")
    print("=" * 80)
    print(f"✅ Source files discovered: {len(source_files)}")
    print(f"📊 Current coverage: {coverage_pct:.2f}%")
    print(f"🎯 Classes needing tests: {coverage_details['total_uncovered_classes']}")
    print(f"📝 Next action: Generate tests for uncovered classes")
    print("=" * 80)
    
    # Example test generation prompt
    print("\n💡 NEXT STEPS FOR COPILOT AGENT:")
    print("-" * 80)
    if uncovered:
        first_uncovered = uncovered[0]
        print(f"Generate a comprehensive JUnit test for:")
        print(f"  Class: {first_uncovered['package']}.{first_uncovered['class']}")
        print(f"  Current coverage: {first_uncovered['coverage_percentage']:.1f}%")
        print(f"  Uncovered lines: {len(first_uncovered['missed_lines'])}")
        print(f"\nPrompt to use:")
        print(f'  "Generate a JUnit test class for {first_uncovered["class"]} in package')
        print(f'   {first_uncovered["package"]}. Cover all methods and edge cases."')

if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()
