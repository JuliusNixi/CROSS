from platform import system as osname
from platform import platform as arch
from pathlib import Path
from sys import exit, stderr
from zipfile import ZipFile
from typing import List, Tuple
from subprocess import Popen as exec
from subprocess import PIPE
from shutil import rmtree
from os import system as ossystem

# The functions with the '_' are support functions for the main functions.
def unzip_file(zip_path: str) -> None:

    zip_path = Path(zip_path)
    if not zip_path.exists():
        raise FileNotFoundError(f"The file {str(zip_path)} does not exist.")
    
    with ZipFile(str(zip_path), 'r') as zip_ref:
        zip_ref.extractall("./Unzipped")

def find_java_files(directory_path: str) -> List[str]:

    path = Path(directory_path)

    if not path.exists():
        raise FileNotFoundError(f"The directory '{directory_path}' does not exist.")
    
    if not path.is_dir():
        raise NotADirectoryError(f"The path '{directory_path}' is not a directory.")

    java_files = [str(file.resolve()) for file in path.rglob("*.java") if file.is_file()]

    return java_files

def make_executable(file_path: str) -> None:
    from os import stat as osstat
    from os import chmod
    import stat
    current_permissions = osstat(file_path).st_mode
    try:
        executable_permissions = current_permissions | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH
        chmod(file_path, executable_permissions)
    except Exception as e:
        print(f"Error while setting executable permissions for '{file_path}': {e}", file=stderr)
        exit(1)
    print(f"OK: Set executable permissions for '{file_path}'.")

def find_javac(start_path: str) -> str:

    start_path = Path(start_path)
    java_bin_path = ""
    for bin_dir in start_path.glob('**/bin'):
        if bin_dir.is_dir():
            java_bin_path = bin_dir
            break

    if java_bin_path == "":
        raise FileNotFoundError(f"No subfolder named 'bin' found starting searching from '{start_path}'.")
   
    javac_bin = java_bin_path / "javac"
    if javac_bin.exists():
        return str(javac_bin)
    else:
        raise FileNotFoundError(f"The 'javac' executable was not found in the 'bin' directory '{java_bin_path}'.")

def oschecks() -> str:
    # "Darwin" is MacOS.
    # "Windows" is Windows.
    # "Linux" is Linux.
    mosname = osname()
    if mosname == "Darwin":
        mosname = "MacOS"
        ossystem("clear")
    elif mosname == "Windows":
        ossystem("cls")
    elif mosname == "Linux":
        ossystem("clear")
    else:
        print("Error: Unsupported operating system.", file=stderr)
        exit(1)
    if 'arm64' in arch().lower():
        mosname += " ARM64"
    elif 'x64' in arch().lower():
        mosname += " x64"
    else:
        print("Error: Unsupported architecture.", file=stderr)
        exit(1)
    if 'arm' in mosname.lower() and 'windows' in mosname.lower():
        print("Error: Unsupported architecture for Windows.", file=stderr)
        exit(1)
    print(f"OK: Operating system and architecture detected: {mosname}.")
    return mosname

def cwdchecks() -> None:
    cwd = Path.cwd()
    if cwd.name == 'Java' and cwd.parent.name == 'CROSS':
        print("OK: Current working directory is 'CROSS/Java'.")
    else:
        print("Error: Current working directory is NOT 'CROSS/Java'.", file=stderr)
        exit(1)

def unzippingjava(mosname: str) -> None:
    basic_java_path = "Versions"
    zip_path = basic_java_path + "/" + mosname.split()[0] + "_" + mosname.split()[1] + ".zip"
    try:
        unzip_file(zip_path)
        print(f"OK: Unzipped {zip_path} to './Unzipped'.")
    except FileNotFoundError as e:
        print(f"Error while unzipping the Java version file: {e}", file=stderr)
        exit(1)

def getjavafiles() -> Tuple[Path, List[str]]:
    java_files = []
    CROSS_dir = Path("../CROSS")
    if not CROSS_dir.exists():
        print(f"Error: The directory '{str(CROSS_dir)}' does not exist.", file=stderr)
        exit(1)
    try:
        java_files = find_java_files(str(CROSS_dir) + "/src")
        print(f"Found {len(java_files)} .java file(s) to compile:")
        for file in java_files:
            print(file)
    except (FileNotFoundError, NotADirectoryError) as e:
        print(f"Error while finding Java files to compile: {e}", file=stderr)
        exit(1)
    return (CROSS_dir, java_files)

def compilejavafiles(java_files: List[str], CROSS_dir: Path, javac_bin: str) -> None:
    java_files_str = " ".join(list(map(lambda e: "'" + e + "'", java_files)))
    command = f"./{javac_bin} -cp '{CROSS_dir}/lib/*' -d '{CROSS_dir}/../bin' {java_files_str}"
    try:
        process = exec(command, shell=True, stdout=PIPE, stderr=PIPE)
        stdout, stderr = process.communicate()
        if process.returncode != 0:
            error_message = stderr.decode().strip()
            raise Exception(f"Compilation failed with error: {error_message}")
    except Exception as e:
        raise Exception(f"Compilation failed with error: {error_message}")

def cleanup() -> None:
    try:
        rmtree("./Unzipped")
        print("OK: Cleaned up the 'Unzipped' directory.")
        rmtree(f"{CROSS_dir}/../bin/")
        print("OK: Cleaned up the 'bin' directory.")
    except Exception as e:
        print(f"Error while cleaning up the 'Unzipped' directory: {e}", file=stderr)
        exit(1)

def execute() -> None:
    pass

if __name__ == "__main__":
    mosname = oschecks()
    cwdchecks()
    unzippingjava(mosname)
    CROSS_dir, java_files = getjavafiles()
    print("OK: All checks passed.")
    print("INFO: Compiling Java files...")
    javac_bin = ""
    try:
        javac_bin = find_javac(".")
    except FileNotFoundError as e:
        print(f"Error while finding the 'javac' executable: {e}", file=stderr)
        exit(1)
    if "linux" in mosname.lower() or "mac" in mosname.lower():
        make_executable(javac_bin)
    try:
        compilejavafiles(java_files, str(CROSS_dir), javac_bin)
    except Exception as e:
        print(f"Error while compiling Java files: {e}", file=stderr)
        exit(1)
    print("OK: Compilation completed successfully.")
    execute()
    cleanup()
    exit(0)
else:
    print("Error: This script is not meant to be imported as a module.", file=stderr)
    exit(1)



