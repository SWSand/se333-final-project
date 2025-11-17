# server.py
from fastmcp import FastMCP
import os
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import List, Dict, Optional
import json
import subprocess
import re

mcp = FastMCP("Testing Agent Tools 🧪")

@mcp.tool
def add(a: int, b: int) -> int:
    """Add two numbers (example tool)"""
    return a + b

@mcp.tool
def find_source_files(base_path: str = "/Users/jsandoval/Documents/book-of-practice/final_project/se333-final-project") -> List[Dict[str, str]]:
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
def jacoco_find_path(base_path: str = "/Users/jsandoval/Documents/book-of-practice/final_project/se333-final-project") -> str:
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

@mcp.tool
def search_codebase(query: str, base_path: str = "/Users/jsandoval/Documents/book-of-practice/final_project/se333-final-project", file_pattern: str = "*.java") -> List[Dict[str, str]]:
    """
    Search for text patterns in Java source files. Useful for finding method usages, 
    class references, or understanding code relationships.
    
    Args:
        query: Text pattern to search for (case-sensitive)
        base_path: Root directory of the project
        file_pattern: File pattern to search (default: *.java)
    
    Returns:
        List of matches with file path, line number, and context
    """
    import fnmatch
    matches = []
    src_main_java = os.path.join(base_path, "src", "main", "java")
    src_test_java = os.path.join(base_path, "src", "test", "java")
    
    search_dirs = []
    if os.path.exists(src_main_java):
        search_dirs.append(src_main_java)
    if os.path.exists(src_test_java):
        search_dirs.append(src_test_java)
    
    for search_dir in search_dirs:
        for root, dirs, files in os.walk(search_dir):
            for file in files:
                if fnmatch.fnmatch(file, file_pattern):
                    file_path = os.path.join(root, file)
                    try:
                        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                            for line_num, line in enumerate(f, 1):
                                if query in line:
                                    matches.append({
                                        "file_path": file_path,
                                        "line_number": line_num,
                                        "line_content": line.strip(),
                                        "relative_path": os.path.relpath(file_path, base_path)
                                    })
                    except Exception as e:
                        continue
    
    return matches

@mcp.tool
def get_class_coverage_summary(jacoco_xml_path: str, sort_by: str = "coverage") -> List[Dict]:
    """
    Get a summary of all classes with their coverage percentages, sorted by coverage.
    Useful for identifying which classes need the most work.
    
    Args:
        jacoco_xml_path: Path to the jacoco.xml file
        sort_by: Sort order - "coverage" (ascending, lowest first) or "name" (alphabetical)
    
    Returns:
        List of classes with coverage information
    """
    if not os.path.exists(jacoco_xml_path):
        return [{"error": f"JaCoCo XML not found at {jacoco_xml_path}"}]
    
    try:
        tree = ET.parse(jacoco_xml_path)
        root = tree.getroot()
        
        class_summaries = []
        
        for package in root.findall('.//package'):
            package_name = package.get('name', '').replace('/', '.')
            
            for cls in package.findall('class'):
                class_name = cls.get('name', '').split('/')[-1]
                source_file = cls.get('sourcefilename', '')
                
                class_total = 0
                class_covered = 0
                
                # Calculate coverage
                for line in cls.findall('.//line'):
                    missed_instructions = int(line.get('mi', 0))
                    covered_instr = int(line.get('ci', 0))
                    class_total += missed_instructions + covered_instr
                    class_covered += covered_instr
                
                coverage_pct = (class_covered / class_total * 100) if class_total > 0 else 100.0
                
                class_summaries.append({
                    "package": package_name,
                    "class": class_name,
                    "source_file": source_file,
                    "coverage_percentage": round(coverage_pct, 2),
                    "covered_instructions": class_covered,
                    "total_instructions": class_total,
                    "missed_instructions": class_total - class_covered
                })
        
        # Sort
        if sort_by == "coverage":
            class_summaries.sort(key=lambda x: x["coverage_percentage"])
        elif sort_by == "name":
            class_summaries.sort(key=lambda x: (x["package"], x["class"]))
        
        return class_summaries
    
    except Exception as e:
        return [{"error": f"Error parsing JaCoCo XML: {str(e)}"}]

@mcp.tool
def read_java_file(file_path: str, start_line: int = 1, end_line: int = None) -> Dict:
    """
    Read a Java source file or specific lines from it. Useful for understanding
    class structure before writing tests.
    
    Args:
        file_path: Path to the Java file
        start_line: Starting line number (1-indexed)
        end_line: Ending line number (None for entire file)
    
    Returns:
        Dictionary with file content and metadata
    """
    if not os.path.exists(file_path):
        return {"error": f"File not found: {file_path}"}
    
    try:
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            lines = f.readlines()
        
        total_lines = len(lines)
        
        if end_line is None:
            end_line = total_lines
        
        # Adjust for 1-indexed
        start_idx = max(0, start_line - 1)
        end_idx = min(total_lines, end_line)
        
        selected_lines = lines[start_idx:end_idx]
        
        return {
            "file_path": file_path,
            "total_lines": total_lines,
            "start_line": start_line,
            "end_line": end_line,
            "content": ''.join(selected_lines),
            "line_count": len(selected_lines)
        }
    
    except Exception as e:
        return {"error": f"Error reading file: {str(e)}"}

# ========== Phase 3: Git MCP Tools ==========

@mcp.tool
def git_status(base_path: str = "/Users/jsandoval/Documents/book-of-practice/final_project/se333-final-project") -> Dict:
    """
    Return Git repository status including clean status, staged changes, and conflicts.
    
    Args:
        base_path: Root directory of the Git repository
    
    Returns:
        Dictionary with:
        - is_clean: Boolean indicating if working directory is clean
        - staged_files: List of staged files
        - unstaged_files: List of modified but unstaged files
        - untracked_files: List of untracked files
        - conflicts: List of files with merge conflicts
        - current_branch: Current branch name
        - ahead: Number of commits ahead of remote
        - behind: Number of commits behind remote
    """
    try:
        os.chdir(base_path)
        
        # Get current branch
        branch_result = subprocess.run(
            ["git", "rev-parse", "--abbrev-ref", "HEAD"],
            capture_output=True,
            text=True,
            timeout=10
        )
        current_branch = branch_result.stdout.strip() if branch_result.returncode == 0 else "unknown"
        
        # Get status
        status_result = subprocess.run(
            ["git", "status", "--porcelain"],
            capture_output=True,
            text=True,
            timeout=10
        )
        
        staged_files = []
        unstaged_files = []
        untracked_files = []
        conflicts = []
        
        for line in status_result.stdout.split('\n'):
            if not line.strip():
                continue
            
            status_code = line[:2]
            file_path = line[3:].strip()
            
            # Parse status codes
            # XY format: X = index status, Y = working tree status
            # X can be: A=added, M=modified, D=deleted, R=renamed, C=copied, U=unmerged
            # Y can be: M=modified, D=deleted, A=added (in working tree)
            
            if status_code[0] == 'U' or status_code[1] == 'U' or 'AA' in status_code or 'DD' in status_code:
                conflicts.append(file_path)
            elif status_code[0] != ' ' and status_code[0] != '?':
                staged_files.append({"file": file_path, "status": status_code[0]})
            elif status_code[1] != ' ':
                unstaged_files.append({"file": file_path, "status": status_code[1]})
            elif status_code == '??':
                untracked_files.append(file_path)
        
        is_clean = len(staged_files) == 0 and len(unstaged_files) == 0 and len(untracked_files) == 0 and len(conflicts) == 0
        
        # Get ahead/behind info
        ahead = 0
        behind = 0
        try:
            tracking_result = subprocess.run(
                ["git", "rev-list", "--left-right", "--count", f"{current_branch}...origin/{current_branch}"],
                capture_output=True,
                text=True,
                timeout=10
            )
            if tracking_result.returncode == 0:
                parts = tracking_result.stdout.strip().split()
                if len(parts) == 2:
                    behind = int(parts[0])
                    ahead = int(parts[1])
        except:
            pass
        
        return {
            "is_clean": is_clean,
            "current_branch": current_branch,
            "staged_files": staged_files,
            "unstaged_files": unstaged_files,
            "untracked_files": untracked_files,
            "conflicts": conflicts,
            "ahead": ahead,
            "behind": behind
        }
    
    except subprocess.TimeoutExpired:
        return {"error": "Git command timed out"}
    except Exception as e:
        return {"error": f"Error getting git status: {str(e)}"}

@mcp.tool
def git_add_all(base_path: str = "/Users/jsandoval/Documents/book-of-practice/final_project/se333-final-project", 
                exclude_patterns: List[str] = None) -> Dict:
    """
    Stage all changes with intelligent filtering. Excludes build artifacts and temporary files.
    
    Args:
        base_path: Root directory of the Git repository
        exclude_patterns: Optional list of patterns to exclude (e.g., ["*.class", "target/", ".idea/"])
    
    Returns:
        Dictionary with:
        - success: Boolean indicating if staging was successful
        - staged_count: Number of files staged
        - excluded_files: List of files that were excluded
        - message: Status message
    """
    if exclude_patterns is None:
        exclude_patterns = [
            "target/",
            "*.class",
            "*.jar",
            "*.war",
            ".idea/",
            ".vscode/",
            "*.iml",
            ".DS_Store",
            "*.log",
            ".mvn/",
            "mvnw",
            "mvnw.cmd"
        ]
    
    try:
        os.chdir(base_path)
        
        # First, get list of all modified/untracked files
        status_result = subprocess.run(
            ["git", "status", "--porcelain"],
            capture_output=True,
            text=True,
            timeout=10
        )
        
        files_to_stage = []
        excluded_files = []
        
        for line in status_result.stdout.split('\n'):
            if not line.strip():
                continue
            
            file_path = line[3:].strip()
            should_exclude = False
            
            # Check against exclude patterns
            for pattern in exclude_patterns:
                if pattern.endswith('/'):
                    # Directory pattern
                    if file_path.startswith(pattern):
                        should_exclude = True
                        break
                elif '*' in pattern:
                    # Wildcard pattern
                    import fnmatch
                    if fnmatch.fnmatch(file_path, pattern):
                        should_exclude = True
                        break
                else:
                    # Exact match
                    if pattern in file_path:
                        should_exclude = True
                        break
            
            if should_exclude:
                excluded_files.append(file_path)
            else:
                files_to_stage.append(file_path)
        
        # Stage files individually to respect exclusions
        staged_count = 0
        for file_path in files_to_stage:
            add_result = subprocess.run(
                ["git", "add", file_path],
                capture_output=True,
                text=True,
                timeout=10
            )
            if add_result.returncode == 0:
                staged_count += 1
        
        return {
            "success": True,
            "staged_count": staged_count,
            "excluded_files": excluded_files,
            "message": f"Successfully staged {staged_count} files. Excluded {len(excluded_files)} files."
        }
    
    except subprocess.TimeoutExpired:
        return {"success": False, "error": "Git command timed out"}
    except Exception as e:
        return {"success": False, "error": f"Error staging files: {str(e)}"}

@mcp.tool
def git_commit(message: str, 
               base_path: str = "/Users/jsandoval/Documents/book-of-practice/final_project/se333-final-project",
               include_coverage: bool = True) -> Dict:
    """
    Automated commit with standardized messages. Optionally includes coverage statistics.
    
    Args:
        message: Commit message (will be enhanced with coverage stats if include_coverage=True)
        base_path: Root directory of the Git repository
        include_coverage: Whether to include coverage statistics in commit message
    
    Returns:
        Dictionary with:
        - success: Boolean indicating if commit was successful
        - commit_hash: SHA of the commit (if successful)
        - message: Final commit message used
        - error: Error message if failed
    """
    try:
        os.chdir(base_path)
        
        # Get coverage stats if requested
        coverage_info = ""
        if include_coverage:
            try:
                jacoco_path = jacoco_find_path(base_path)
                if os.path.exists(jacoco_path):
                    coverage = jacoco_coverage(jacoco_path)
                    missing = missing_coverage(jacoco_path)
                    missed = missing.get("missed_instructions", 0)
                    covered = missing.get("covered_instructions", 0)
                    total = missed + covered
                    
                    coverage_info = f"\n\nCoverage: {coverage:.2f}% ({covered}/{total} instructions covered, {missed} missed)"
            except:
                pass  # If coverage can't be retrieved, continue without it
        
        # Construct final message
        final_message = message + coverage_info
        
        # Commit with MCP/Agent author information
        commit_result = subprocess.run(
            ["git", "commit", "-m", final_message, 
             "--author", "Jose Sandoval - MCP/Agent <noreply@mcp-agent>"],
            capture_output=True,
            text=True,
            timeout=10
        )
        
        if commit_result.returncode == 0:
            # Get commit hash
            hash_result = subprocess.run(
                ["git", "rev-parse", "HEAD"],
                capture_output=True,
                text=True,
                timeout=10
            )
            commit_hash = hash_result.stdout.strip() if hash_result.returncode == 0 else "unknown"
            
            return {
                "success": True,
                "commit_hash": commit_hash,
                "message": final_message
            }
        else:
            error_msg = commit_result.stderr.strip() or commit_result.stdout.strip()
            if "nothing to commit" in error_msg.lower():
                return {
                    "success": False,
                    "error": "Nothing to commit (working directory clean or no staged changes)"
                }
            return {
                "success": False,
                "error": f"Commit failed: {error_msg}"
            }
    
    except subprocess.TimeoutExpired:
        return {"success": False, "error": "Git command timed out"}
    except Exception as e:
        return {"success": False, "error": f"Error committing: {str(e)}"}

@mcp.tool
def git_push(remote: str = "origin", 
             branch: str = None,
             base_path: str = "/Users/jsandoval/Documents/book-of-practice/final_project/se333-final-project",
             setUpstream: bool = True) -> Dict:
    """
    Push to remote with upstream configuration. Handles authentication through existing credential helpers.
    
    Args:
        remote: Remote name (default: "origin")
        branch: Branch name to push (default: current branch)
        base_path: Root directory of the Git repository
        setUpstream: Whether to set upstream branch if not already set
    
    Returns:
        Dictionary with:
        - success: Boolean indicating if push was successful
        - message: Status message
        - error: Error message if failed
    """
    try:
        os.chdir(base_path)
        
        # Get current branch if not specified
        if branch is None:
            branch_result = subprocess.run(
                ["git", "rev-parse", "--abbrev-ref", "HEAD"],
                capture_output=True,
                text=True,
                timeout=10
            )
            if branch_result.returncode != 0:
                return {"success": False, "error": "Could not determine current branch"}
            branch = branch_result.stdout.strip()
        
        # Build push command
        push_cmd = ["git", "push"]
        
        if setUpstream:
            push_cmd.extend(["--set-upstream", remote, branch])
        else:
            push_cmd.extend([remote, branch])
        
        push_result = subprocess.run(
            push_cmd,
            capture_output=True,
            text=True,
            timeout=30
        )
        
        if push_result.returncode == 0:
            return {
                "success": True,
                "message": f"Successfully pushed {branch} to {remote}",
                "output": push_result.stdout.strip()
            }
        else:
            error_msg = push_result.stderr.strip() or push_result.stdout.strip()
            return {
                "success": False,
                "error": f"Push failed: {error_msg}",
                "output": push_result.stdout.strip()
            }
    
    except subprocess.TimeoutExpired:
        return {"success": False, "error": "Git command timed out"}
    except Exception as e:
        return {"success": False, "error": f"Error pushing: {str(e)}"}

@mcp.tool
def git_pull_request(base: str = "main", 
                     title: str = None,
                     body: str = None,
                     base_path: str = "/Users/jsandoval/Documents/book-of-practice/final_project/se333-final-project") -> Dict:
    """
    Create a pull request against the specified base branch. Uses standardized templates.
    Automatically includes metadata such as test coverage improvements or bug fixes.
    
    Args:
        base: Base branch name (default: "main")
        title: PR title (auto-generated if not provided)
        body: PR body (auto-generated if not provided)
        base_path: Root directory of the Git repository
    
    Returns:
        Dictionary with:
        - success: Boolean indicating if PR creation was successful
        - pr_url: URL of the created pull request
        - pr_number: Pull request number
        - error: Error message if failed
    """
    try:
        os.chdir(base_path)
        
        # Get current branch
        branch_result = subprocess.run(
            ["git", "rev-parse", "--abbrev-ref", "HEAD"],
            capture_output=True,
            text=True,
            timeout=10
        )
        if branch_result.returncode != 0:
            return {"success": False, "error": "Could not determine current branch"}
        current_branch = branch_result.stdout.strip()
        
        # Get remote URL to determine if GitHub/GitLab/etc
        remote_result = subprocess.run(
            ["git", "remote", "get-url", "origin"],
            capture_output=True,
            text=True,
            timeout=10
        )
        remote_url = remote_result.stdout.strip() if remote_result.returncode == 0 else ""
        
        # Auto-generate title if not provided
        if not title:
            # Get recent commits for context
            log_result = subprocess.run(
                ["git", "log", f"{base}..{current_branch}", "--oneline", "-5"],
                capture_output=True,
                text=True,
                timeout=10
            )
            commits = log_result.stdout.strip().split('\n') if log_result.returncode == 0 else []
            
            if commits:
                # Use first commit message as base
                first_commit = commits[0].split(' ', 1)[1] if len(commits[0].split(' ', 1)) > 1 else "Update"
                title = f"PR: {first_commit}"
            else:
                title = f"Update from {current_branch}"
        
        # Auto-generate body if not provided
        if not body:
            # Get coverage stats
            coverage_info = ""
            try:
                jacoco_path = jacoco_find_path(base_path)
                if os.path.exists(jacoco_path):
                    coverage = jacoco_coverage(jacoco_path)
                    missing = missing_coverage(jacoco_path)
                    missed = missing.get("missed_instructions", 0)
                    covered = missing.get("covered_instructions", 0)
                    total = missed + covered
                    
                    coverage_info = f"\n## Coverage Statistics\n- Overall Coverage: {coverage:.2f}%\n- Covered Instructions: {covered}\n- Missed Instructions: {missed}\n- Total Instructions: {total}\n"
            except:
                pass
            
            # Get changed files
            diff_result = subprocess.run(
                ["git", "diff", "--name-status", f"{base}..{current_branch}"],
                capture_output=True,
                text=True,
                timeout=10
            )
            changed_files = diff_result.stdout.strip().split('\n') if diff_result.returncode == 0 else []
            
            changes_list = "\n".join(f"- {f}" for f in changed_files[:20])  # Limit to first 20 files
            body = f"""## Description
This PR includes changes from `{current_branch}` branch.

{coverage_info}

## Changes
{changes_list}

## Testing
- [ ] All tests pass
- [ ] Coverage improved/maintained
- [ ] No breaking changes

## Checklist
- [ ] Code follows project style guidelines
- [ ] Tests added/updated
- [ ] Documentation updated (if needed)
"""
        
        # Try to use GitHub CLI if available
        gh_result = subprocess.run(
            ["gh", "pr", "create", "--base", base, "--title", title, "--body", body],
            capture_output=True,
            text=True,
            timeout=30
        )
        
        if gh_result.returncode == 0:
            # Parse PR URL from output
            output = gh_result.stdout.strip()
            pr_url_match = re.search(r'https://[^\s]+', output)
            pr_url = pr_url_match.group(0) if pr_url_match else output
            
            # Extract PR number
            pr_num_match = re.search(r'#(\d+)', output)
            pr_number = int(pr_num_match.group(1)) if pr_num_match else None
            
            return {
                "success": True,
                "pr_url": pr_url,
                "pr_number": pr_number,
                "message": f"Pull request created successfully"
            }
        else:
            # GitHub CLI not available or failed - return instructions
            return {
                "success": False,
                "error": "GitHub CLI (gh) not available or failed. Please create PR manually.",
                "instructions": f"""
To create this PR manually:
1. Push your branch: git push origin {current_branch}
2. Visit: {remote_url.replace('.git', '')}/compare/{base}...{current_branch}
3. Use this title: {title}
4. Use this body:
{body}
""",
                "title": title,
                "body": body
            }
    
    except subprocess.TimeoutExpired:
        return {"success": False, "error": "Command timed out"}
    except Exception as e:
        return {"success": False, "error": f"Error creating pull request: {str(e)}"}

# ========== Phase 4: Intelligent Test Iteration & Bug Detection ==========

@mcp.tool
def detect_bugs_from_tests(base_path: str = "/Users/jsandoval/Documents/book-of-practice/final_project/se333-final-project") -> Dict:
    """
    Analyze test failures to detect bugs in source code. If tests expose bugs, provides fix suggestions.
    
    Args:
        base_path: Root directory of the project
    
    Returns:
        Dictionary with:
        - bugs_detected: List of detected bugs with details
        - test_failures: List of failing tests
        - fix_suggestions: Suggested fixes for each bug
        - source_files_to_fix: List of source files that need fixes
    """
    try:
        os.chdir(base_path)
        
        # Run tests and capture output
        test_result = subprocess.run(
            ["mvn", "test", "-DtestFailureIgnore=true"],
            capture_output=True,
            text=True,
            timeout=120
        )
        
        bugs_detected = []
        test_failures = []
        fix_suggestions = []
        source_files_to_fix = []
        
        # Parse test output for failures
        output_lines = test_result.stdout.split('\n') + test_result.stderr.split('\n')
        
        current_failure = None
        for i, line in enumerate(output_lines):
            # Detect test failure patterns
            if "Tests run:" in line or "FAILURE" in line or "ERROR" in line:
                if current_failure:
                    test_failures.append(current_failure)
                current_failure = {"test": "", "error": "", "stack_trace": []}
            
            if current_failure:
                if "test" in line.lower() and ("failed" in line.lower() or "error" in line.lower()):
                    # Extract test name
                    test_match = re.search(r'([A-Za-z0-9_]+Test\.test[A-Za-z0-9_]+)', line)
                    if test_match:
                        current_failure["test"] = test_match.group(1)
                
                if "Exception" in line or "Error" in line:
                    current_failure["error"] = line.strip()
                    # Look for source file in stack trace
                    if "at " in line:
                        source_match = re.search(r'at\s+([A-Za-z0-9_.]+)\.([A-Za-z0-9_]+)\(([A-Za-z0-9_/]+\.java):(\d+)\)', line)
                        if source_match:
                            package = source_match.group(1)
                            method = source_match.group(2)
                            file_name = source_match.group(3)
                            line_num = source_match.group(4)
                            
                            source_path = os.path.join(base_path, "src", "main", "java", file_name.replace("/", os.sep))
                            
                            if os.path.exists(source_path) and source_path not in source_files_to_fix:
                                source_files_to_fix.append(source_path)
                                
                                # Generate fix suggestion based on error type
                                error_type = current_failure.get("error", "")
                                suggestion = f"Check {file_name}:{line_num} in method {method}"
                                
                                if "NullPointerException" in error_type:
                                    suggestion += ". Add null check before accessing object."
                                elif "IllegalArgumentException" in error_type:
                                    suggestion += ". Validate input parameters."
                                elif "IndexOutOfBoundsException" in error_type:
                                    suggestion += ". Check array/list bounds before access."
                                elif "ClassCastException" in error_type:
                                    suggestion += ". Verify type compatibility before casting."
                                
                                bugs_detected.append({
                                    "file": source_path,
                                    "line": int(line_num),
                                    "method": method,
                                    "error_type": error_type,
                                    "test": current_failure.get("test", "")
                                })
                                
                                fix_suggestions.append({
                                    "file": source_path,
                                    "line": int(line_num),
                                    "suggestion": suggestion
                                })
        
        if current_failure:
            test_failures.append(current_failure)
        
        return {
            "bugs_detected": bugs_detected,
            "test_failures": test_failures,
            "fix_suggestions": fix_suggestions,
            "source_files_to_fix": source_files_to_fix,
            "total_bugs": len(bugs_detected),
            "total_failures": len(test_failures)
        }
    
    except subprocess.TimeoutExpired:
        return {"error": "Test execution timed out"}
    except Exception as e:
        return {"error": f"Error detecting bugs: {str(e)}"}

@mcp.tool
def generate_quality_metrics_dashboard(base_path: str = "/Users/jsandoval/Documents/book-of-practice/final_project/se333-final-project") -> Dict:
    """
    Generate comprehensive coverage reports and track test quality metrics.
    Includes assertions per test, edge case coverage, and bug fixes.
    
    Args:
        base_path: Root directory of the project
    
    Returns:
        Dictionary with:
        - overall_coverage: Overall coverage percentage
        - coverage_by_package: Coverage breakdown by package
        - test_quality_metrics: Metrics like assertions per test, edge case coverage
        - bug_fixes_count: Number of bugs fixed
        - coverage_trend: Coverage improvement over time (if available)
    """
    try:
        jacoco_path = jacoco_find_path(base_path)
        if not os.path.exists(jacoco_path):
            return {"error": "JaCoCo report not found. Run tests first."}
        
        # Get overall coverage
        overall_coverage = jacoco_coverage(jacoco_path)
        missing = missing_coverage(jacoco_path)
        
        # Get coverage by package
        tree = ET.parse(jacoco_path)
        root = tree.getroot()
        
        coverage_by_package = {}
        for package in root.findall('.//package'):
            package_name = package.get('name', '').replace('/', '.')
            
            package_total = 0
            package_covered = 0
            
            for cls in package.findall('class'):
                for line in cls.findall('.//line'):
                    missed = int(line.get('mi', 0))
                    covered = int(line.get('ci', 0))
                    package_total += missed + covered
                    package_covered += covered
            
            if package_total > 0:
                coverage_by_package[package_name] = {
                    "coverage_percentage": round((package_covered / package_total) * 100, 2),
                    "covered_instructions": package_covered,
                    "total_instructions": package_total,
                    "missed_instructions": package_total - package_covered
                }
        
        # Analyze test quality metrics
        test_files = []
        test_dir = os.path.join(base_path, "src", "test", "java")
        if os.path.exists(test_dir):
            for root_dir, dirs, files in os.walk(test_dir):
                for file in files:
                    if file.endswith("Test.java"):
                        test_files.append(os.path.join(root_dir, file))
        
        total_tests = 0
        total_assertions = 0
        edge_case_tests = 0
        
        for test_file in test_files:
            try:
                with open(test_file, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()
                    
                    # Count test methods
                    test_methods = re.findall(r'@Test\s+public\s+void\s+test\w+', content)
                    total_tests += len(test_methods)
                    
                    # Count assertions
                    assertions = re.findall(r'assert\w+\(', content)
                    total_assertions += len(assertions)
                    
                    # Count edge case tests (tests with "Edge", "Null", "Empty", "Boundary" in name)
                    edge_case_pattern = r'test\w*(Edge|Null|Empty|Boundary|Exception|Invalid|Zero|Max|Min)\w*'
                    edge_cases = re.findall(edge_case_pattern, content, re.IGNORECASE)
                    edge_case_tests += len(edge_cases)
            except:
                continue
        
        assertions_per_test = (total_assertions / total_tests) if total_tests > 0 else 0
        edge_case_coverage = (edge_case_tests / total_tests * 100) if total_tests > 0 else 0
        
        return {
            "overall_coverage": {
                "percentage": overall_coverage,
                "covered_instructions": missing.get("covered_instructions", 0),
                "missed_instructions": missing.get("missed_instructions", 0),
                "total_instructions": missing.get("total_instructions", 0),
                "branch_coverage": missing.get("branch_coverage_percentage", 0)
            },
            "coverage_by_package": coverage_by_package,
            "test_quality_metrics": {
                "total_test_files": len(test_files),
                "total_test_methods": total_tests,
                "total_assertions": total_assertions,
                "assertions_per_test": round(assertions_per_test, 2),
                "edge_case_tests": edge_case_tests,
                "edge_case_coverage_percentage": round(edge_case_coverage, 2)
            },
            "bug_fixes_count": 0,  # Would need to track this over time
            "coverage_trend": "N/A - Historical data not available"
        }
    
    except Exception as e:
        return {"error": f"Error generating quality metrics: {str(e)}"}

# ========== Phase 5: Creative Extensions ==========

@mcp.tool
def generate_specification_based_tests(class_name: str,
                                       base_path: str = "/Users/jsandoval/Documents/book-of-practice/final_project/se333-final-project",
                                       method_name: str = None) -> Dict:
    """
    Specification-Based Testing Generator using boundary value analysis, equivalence classes,
    and decision tables. Generates comprehensive test cases based on method specifications.
    
    Args:
        class_name: Name of the class to generate tests for
        base_path: Root directory of the project
        method_name: Optional specific method to focus on
    
    Returns:
        Dictionary with:
        - test_cases: List of generated test cases with boundary values, equivalence classes
        - decision_table: Decision table for complex logic
        - test_code: Generated JUnit test code
    """
    try:
        # Find the source file
        source_files = find_source_files(base_path)
        target_file = None
        
        for file_info in source_files:
            if file_info["class_name"] == class_name:
                target_file = file_info["file_path"]
                break
        
        if not target_file:
            return {"error": f"Class {class_name} not found"}
        
        # Analyze the class
        class_info = analyze_java_class(target_file)
        methods = class_info.get("methods", [])
        
        if method_name:
            methods = [m for m in methods if method_name in m.get("signature", "")]
        
        test_cases = []
        decision_table = []
        
        for method in methods:
            signature = method.get("signature", "")
            
            # Extract parameter types
            param_match = re.search(r'\(([^)]*)\)', signature)
            if param_match:
                params_str = param_match.group(1)
                param_types = [p.strip().split()[-1] if p.strip() else "" for p in params_str.split(',')]
                
                # Generate boundary value test cases for numeric types
                for param_type in param_types:
                    if param_type in ["int", "Integer", "long", "Long"]:
                        test_cases.append({
                            "method": signature,
                            "type": "boundary_value",
                            "values": {
                                "min": "Integer.MIN_VALUE" if "int" in param_type.lower() else "Long.MIN_VALUE",
                                "zero": "0",
                                "max": "Integer.MAX_VALUE" if "int" in param_type.lower() else "Long.MAX_VALUE",
                                "negative": "-1",
                                "positive": "1"
                            }
                        })
                    elif param_type in ["String"]:
                        test_cases.append({
                            "method": signature,
                            "type": "equivalence_class",
                            "values": {
                                "null": "null",
                                "empty": '""',
                                "single_char": '"a"',
                                "normal": '"test"',
                                "whitespace": '"   "',
                                "special_chars": '"!@#$%^&*()"'
                            }
                        })
                    elif param_type in ["List", "Collection"]:
                        test_cases.append({
                            "method": signature,
                            "type": "equivalence_class",
                            "values": {
                                "null": "null",
                                "empty": "Collections.emptyList()",
                                "single_element": "Collections.singletonList(item)",
                                "multiple_elements": "Arrays.asList(item1, item2, item3)"
                            }
                        })
                
                # Generate decision table for boolean logic
                if len(param_types) <= 3:  # Keep decision tables manageable
                    decision_table.append({
                        "method": signature,
                        "conditions": param_types,
                        "test_cases": []  # Would generate based on method logic
                    })
        
        # Generate test code template
        test_code = f"""package {class_info.get('package', 'org.apache.commons.lang3')};

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;

public class {class_name}SpecificationTest {{
    
    // Boundary Value Analysis Tests
"""
        
        for test_case in test_cases[:5]:  # Limit to first 5 for template
            if test_case["type"] == "boundary_value":
                test_code += f"""
    @Test
    public void test{test_case['method'].split('(')[0]}_BoundaryValues() {{
        // Test with MIN, MAX, ZERO, NEGATIVE, POSITIVE values
        // TODO: Implement based on method signature
    }}
"""
        
        test_code += """
    // Equivalence Class Tests
"""
        
        for test_case in test_cases[5:10]:  # Next 5
            if test_case["type"] == "equivalence_class":
                test_code += f"""
    @Test
    public void test{test_case['method'].split('(')[0]}_EquivalenceClasses() {{
        // Test with NULL, EMPTY, NORMAL, EDGE cases
        // TODO: Implement based on method signature
    }}
"""
        
        test_code += "}\n"
        
        return {
            "test_cases": test_cases,
            "decision_table": decision_table,
            "test_code": test_code,
            "total_test_cases": len(test_cases)
        }
    
    except Exception as e:
        return {"error": f"Error generating specification-based tests: {str(e)}"}

@mcp.tool
def ai_code_review(file_path: str,
                   base_path: str = "/Users/jsandoval/Documents/book-of-practice/final_project/se333-final-project") -> Dict:
    """
    AI Code Review Agent that performs static analysis, detects code smells,
    security vulnerabilities, and style guide violations. Provides refactoring suggestions.
    
    Args:
        file_path: Path to the Java file to review
        base_path: Root directory of the project
    
    Returns:
        Dictionary with:
        - code_smells: List of detected code smells
        - security_issues: List of security vulnerabilities
        - style_violations: List of style guide violations
        - refactoring_suggestions: Suggested refactorings
        - complexity_metrics: Code complexity metrics
    """
    try:
        if not os.path.exists(file_path):
            return {"error": f"File not found: {file_path}"}
        
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
            lines = content.split('\n')
        
        code_smells = []
        security_issues = []
        style_violations = []
        refactoring_suggestions = []
        
        # Detect code smells
        # Long method detection
        method_lengths = {}
        current_method = None
        method_start = 0
        brace_count = 0
        
        for i, line in enumerate(lines, 1):
            # Detect method start
            if re.search(r'(public|private|protected)\s+\w+\s+\w+\s*\(', line):
                if current_method:
                    method_lengths[current_method] = i - method_start
                current_method = line.strip().split('(')[0].split()[-1]
                method_start = i
                brace_count = 0
            
            brace_count += line.count('{') - line.count('}')
            if current_method and brace_count == 0 and '{' in line:
                method_lengths[current_method] = i - method_start
        
        for method, length in method_lengths.items():
            if length > 50:
                code_smells.append({
                    "type": "Long Method",
                    "severity": "medium",
                    "location": f"{file_path}:{method}",
                    "description": f"Method '{method}' is {length} lines long. Consider breaking it into smaller methods.",
                    "suggestion": "Extract methods for logical sections of the code."
                })
        
        # Detect magic numbers
        magic_numbers = re.findall(r'\b\d{2,}\b', content)
        if len(magic_numbers) > 5:
            code_smells.append({
                "type": "Magic Numbers",
                "severity": "low",
                "location": file_path,
                "description": f"Found {len(magic_numbers)} potential magic numbers. Consider using named constants.",
                "suggestion": "Define constants with meaningful names."
            })
        
        # Security issues detection
        # SQL injection patterns
        if re.search(r'Statement\.|executeQuery|executeUpdate', content):
            security_issues.append({
                "type": "Potential SQL Injection",
                "severity": "high",
                "location": file_path,
                "description": "Direct SQL statement execution detected. Use PreparedStatement instead.",
                "suggestion": "Use PreparedStatement with parameterized queries."
            })
        
        # Hardcoded passwords/secrets
        if re.search(r'(password|secret|api[_-]?key)\s*=\s*["\'][^"\']+["\']', content, re.IGNORECASE):
            security_issues.append({
                "type": "Hardcoded Credentials",
                "severity": "critical",
                "location": file_path,
                "description": "Potential hardcoded password or API key detected.",
                "suggestion": "Use environment variables or secure configuration management."
            })
        
        # Style violations
        # Long lines
        for i, line in enumerate(lines, 1):
            if len(line) > 120:
                style_violations.append({
                    "type": "Line Too Long",
                    "severity": "low",
                    "location": f"{file_path}:{i}",
                    "description": f"Line {i} is {len(line)} characters long (recommended: < 120).",
                    "suggestion": "Break long lines for better readability."
                })
        
        # Missing JavaDoc for public methods
        public_methods = re.findall(r'public\s+\w+\s+(\w+)\s*\(', content)
        javadoc_methods = re.findall(r'/\*\*.*?\*/\s*public\s+\w+\s+(\w+)\s*\(', content, re.DOTALL)
        for method in public_methods:
            if method not in javadoc_methods:
                style_violations.append({
                    "type": "Missing JavaDoc",
                    "severity": "low",
                    "location": f"{file_path}:{method}",
                    "description": f"Public method '{method}' is missing JavaDoc documentation.",
                    "suggestion": "Add JavaDoc comments describing the method's purpose, parameters, and return value."
                })
        
        # Complexity metrics
        cyclomatic_complexity = 0
        for line in lines:
            # Count decision points
            cyclomatic_complexity += len(re.findall(r'\b(if|else|while|for|switch|case|catch|&&|\|\|)\b', line))
        
        return {
            "code_smells": code_smells,
            "security_issues": security_issues,
            "style_violations": style_violations,
            "refactoring_suggestions": refactoring_suggestions,
            "complexity_metrics": {
                "cyclomatic_complexity": cyclomatic_complexity,
                "lines_of_code": len(lines),
                "method_count": len(public_methods),
                "average_method_length": sum(method_lengths.values()) / len(method_lengths) if method_lengths else 0
            },
            "summary": {
                "total_issues": len(code_smells) + len(security_issues) + len(style_violations),
                "critical": len([i for i in security_issues if i["severity"] == "critical"]),
                "high": len([i for i in security_issues if i["severity"] == "high"]),
                "medium": len([i for i in code_smells if i["severity"] == "medium"]),
                "low": len([i for i in code_smells + style_violations if i["severity"] == "low"])
            }
        }
    
    except Exception as e:
        return {"error": f"Error performing code review: {str(e)}"}

if __name__ == "__main__":
    mcp.run(transport="sse")