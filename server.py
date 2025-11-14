# server.py
from fastmcp import FastMCP
import os
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import List, Dict, Optional
import json

mcp = FastMCP("Testing Agent Tools 🧪")

@mcp.tool
def add(a: int, b: int) -> int:
    """Add two numbers (example tool)"""
    return a + b

@mcp.tool
def find_source_files(base_path: str = "/Users/evo/Documents/DePaul/2025/Autumn25/SE333/final_project/codebase") -> List[Dict[str, str]]:
    """
    Find all Java source files in the project that need testing.
    Returns a list of dictionaries with file paths, class names, and package names.
    
    Args:
        base_path: Root directory of the project
    
    Returns:
        List of source files with metadata
    """
    source_files = []
    src_main_java = os.path.join(base_path, "src", "main", "java")
    
    if not os.path.exists(src_main_java):
        return []
    
    for root, dirs, files in os.walk(src_main_java):
        for file in files:
            if file.endswith(".java"):
                full_path = os.path.join(root, file)
                rel_path = os.path.relpath(full_path, src_main_java)
                
                # Extract package and class name
                package_name = os.path.dirname(rel_path).replace(os.sep, ".")
                class_name = file[:-5]  # Remove .java extension
                
                source_files.append({
                    "file_path": full_path,
                    "relative_path": rel_path,
                    "package": package_name,
                    "class_name": class_name,
                    "test_path": os.path.join(base_path, "src", "test", "java", rel_path.replace(".java", "Test.java"))
                })
    
    return source_files

@mcp.tool
def jacoco_find_path(base_path: str = "/Users/evo/Documents/DePaul/2025/Autumn25/SE333/final_project/codebase") -> str:
    """
    Find the JaCoCo XML report path after running tests.
    
    Args:
        base_path: Root directory of the project
    
    Returns:
        Path to jacoco.xml or error message
    """
    jacoco_path = os.path.join(base_path, "target", "site", "jacoco", "jacoco.xml")
    
    if os.path.exists(jacoco_path):
        return jacoco_path
    else:
        # Check alternative location
        alt_path = os.path.join(base_path, "target", "jacoco", "jacoco.xml")
        if os.path.exists(alt_path):
            return alt_path
        return f"JaCoCo report not found. Run 'mvn test' first. Expected at: {jacoco_path}"

@mcp.tool
def missing_coverage(jacoco_xml_path: str) -> Dict:
    """
    Parse JaCoCo XML report and identify uncovered code segments.
    
    Args:
        jacoco_xml_path: Path to the jacoco.xml file
    
    Returns:
        Dictionary with missing coverage details including:
        - uncovered_classes: List of classes with uncovered lines
        - total_lines: Total lines
        - covered_lines: Covered lines
        - missed_lines: Missed lines
        - coverage_percentage: Overall coverage percentage
    """
    if not os.path.exists(jacoco_xml_path):
        return {"error": f"JaCoCo XML not found at {jacoco_xml_path}"}
    
    try:
        tree = ET.parse(jacoco_xml_path)
        root = tree.getroot()
        
        uncovered_classes = []
        total_instructions = 0
        covered_instructions = 0
        total_branches = 0
        covered_branches = 0
        
        # Parse through packages and classes
        for package in root.findall('.//package'):
            package_name = package.get('name', '').replace('/', '.')
            
            for cls in package.findall('class'):
                class_name = cls.get('name', '').split('/')[-1]
                source_file = cls.get('sourcefilename', '')
                
                class_missed_lines = []
                class_total = 0
                class_covered = 0
                
                # Analyze line coverage
                for line in cls.findall('.//line'):
                    line_num = int(line.get('nr', 0))
                    missed_instructions = int(line.get('mi', 0))
                    covered_instr = int(line.get('ci', 0))
                    
                    total_instructions += missed_instructions + covered_instr
                    covered_instructions += covered_instr
                    
                    if missed_instructions > 0 and covered_instr == 0:
                        class_missed_lines.append(line_num)
                    
                    class_total += missed_instructions + covered_instr
                    class_covered += covered_instr
                
                # Analyze branch coverage
                for counter in cls.findall('counter'):
                    if counter.get('type') == 'BRANCH':
                        missed = int(counter.get('missed', 0))
                        covered = int(counter.get('covered', 0))
                        total_branches += missed + covered
                        covered_branches += covered
                
                if class_missed_lines:
                    coverage_pct = (class_covered / class_total * 100) if class_total > 0 else 0
                    uncovered_classes.append({
                        "package": package_name,
                        "class": class_name,
                        "source_file": source_file,
                        "missed_lines": class_missed_lines,
                        "coverage_percentage": round(coverage_pct, 2),
                        "total_instructions": class_total,
                        "covered_instructions": class_covered
                    })
        
        overall_coverage = (covered_instructions / total_instructions * 100) if total_instructions > 0 else 0
        branch_coverage = (covered_branches / total_branches * 100) if total_branches > 0 else 0
        
        return {
            "uncovered_classes": uncovered_classes,
            "total_instructions": total_instructions,
            "covered_instructions": covered_instructions,
            "missed_instructions": total_instructions - covered_instructions,
            "line_coverage_percentage": round(overall_coverage, 2),
            "branch_coverage_percentage": round(branch_coverage, 2),
            "total_uncovered_classes": len(uncovered_classes)
        }
    
    except Exception as e:
        return {"error": f"Error parsing JaCoCo XML: {str(e)}"}

@mcp.tool
def jacoco_coverage(jacoco_xml_path: str) -> float:
    """
    Get the overall coverage percentage from JaCoCo report.
    
    Args:
        jacoco_xml_path: Path to the jacoco.xml file
    
    Returns:
        Coverage percentage (0-100)
    """
    result = missing_coverage(jacoco_xml_path)
    if "error" in result:
        return 0.0
    return result.get("line_coverage_percentage", 0.0)

@mcp.tool
def analyze_java_class(file_path: str) -> Dict:
    """
    Analyze a Java source file to extract methods, constructors, and other testable elements.
    
    Args:
        file_path: Path to the Java source file
    
    Returns:
        Dictionary with class analysis including methods, constructors, fields
    """
    if not os.path.exists(file_path):
        return {"error": f"File not found: {file_path}"}
    
    try:
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
        
        # Basic parsing - extract public methods
        methods = []
        lines = content.split('\n')
        
        in_comment = False
        current_method = None
        brace_count = 0
        
        for i, line in enumerate(lines, 1):
            stripped = line.strip()
            
            # Handle multi-line comments
            if '/*' in stripped:
                in_comment = True
            if '*/' in stripped:
                in_comment = False
                continue
            
            if in_comment or stripped.startswith('//'):
                continue
            
            # Look for method signatures
            if ('public ' in stripped or 'protected ' in stripped or 'private ' in stripped) and '(' in stripped:
                # Check if it's a method (not a class or interface)
                if not stripped.startswith('class ') and not stripped.startswith('interface '):
                    method_info = {
                        "line": i,
                        "signature": stripped,
                        "is_constructor": False,
                        "is_static": 'static ' in stripped,
                        "visibility": 'public' if 'public ' in stripped else ('protected' if 'protected ' in stripped else 'private')
                    }
                    methods.append(method_info)
        
        return {
            "file_path": file_path,
            "methods": methods,
            "total_methods": len(methods)
        }
    
    except Exception as e:
        return {"error": f"Error analyzing file: {str(e)}"}

@mcp.tool
def check_test_exists(test_path: str) -> bool:
    """
    Check if a test file exists for a given source file.
    
    Args:
        test_path: Path to the test file
    
    Returns:
        True if test exists, False otherwise
    """
    return os.path.exists(test_path)

@mcp.tool
def get_uncovered_methods(class_info: Dict, coverage_info: Dict) -> List[Dict]:
    """
    Cross-reference class methods with coverage data to find untested methods.
    
    Args:
        class_info: Output from analyze_java_class
        coverage_info: Output from missing_coverage
    
    Returns:
        List of methods that need testing
    """
    # Find the class in coverage info
    class_name = os.path.basename(class_info.get("file_path", "")).replace(".java", "")
    
    uncovered_classes = coverage_info.get("uncovered_classes", [])
    class_coverage = None
    
    for cls in uncovered_classes:
        if cls.get("class") == class_name:
            class_coverage = cls
            break
    
    if not class_coverage:
        return []
    
    missed_lines = set(class_coverage.get("missed_lines", []))
    methods = class_info.get("methods", [])
    
    uncovered_methods = []
    for method in methods:
        method_line = method.get("line", 0)
        # If the method's line is in missed lines, it's likely uncovered
        if method_line in missed_lines or any(abs(method_line - ml) <= 2 for ml in missed_lines):
            uncovered_methods.append(method)
    
    return uncovered_methods

if __name__ == "__main__":
    mcp.run(transport="sse")