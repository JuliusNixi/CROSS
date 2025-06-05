# All the imports should be pre-installed with recent Python3.
from platform import system as osname
from platform import machine as arch
from pathlib import Path
from sys import exit, stderr
from zipfile import ZipFile
from typing import List, Tuple
from subprocess import Popen as exec
from subprocess import PIPE
from shutil import rmtree, copyfile
from os import system as ossystem, path
import urllib.request

########## Support functions ##########

# The functions with the '_' are support functions for the main others functions.
# The support functions throw errors, don't print messages and don't exit the program.

def unzip_file(zip_path: str) -> None:

    zip_path = Path(zip_path)
    if not zip_path.exists():
        raise FileNotFoundError(f"The file {str(zip_path)} does not exist.")
    
    with ZipFile(str(zip_path), 'r') as zip_ref:
        zip_ref.extractall("./Unzipped")

def download_file(url: str) -> None:
    try:
        filename = url.split("/")[-1].split("?")[0]
        directory = "./Versions/"
        urllib.request.urlretrieve(url, directory + filename)
    except urllib.error.HTTPError as http_err:
        raise Exception(f"Error: HTTP error while downloading Java version .zip file {filename} from {url}: {http_err}")
    except urllib.error.URLError as url_err:
        raise Exception(f"Error: URL error while downloading Java version .zip file {filename} from {url}: {url_err}")
    except Exception as err:
        raise Exception(f"Error: An error occurred while downloading Java version .zip file {filename} from {url}: {err}")

def download_java_versions() -> None:
    basicurl = "https://media.githubusercontent.com/media/JuliusNixi/CROSS/refs/heads/main/Java/Versions/"
    endurl = "?download=true"
    urls = [
        basicurl + "Linux_ARM64.zip" + endurl,
        basicurl + "Linux_x64.zip" + endurl,
        basicurl + "MacOS_ARM64.zip" + endurl,
        basicurl + "MacOS_x64.zip" + endurl,
        basicurl + "Windows_x64.zip" + endurl,
    ]

    for url in urls:
        try:
            # Exception to the no print policy in the support functions rule here.
            print(f"INFO: Downloading {url}, please wait...")
            download_file(url)
            print(f"OK: Downloaded {url} successfully in './Versions/'.")
        except Exception as e:
            # Backwarding the error to the caller function.
            raise e

# Find all the .java files in the given directory, returning a list of their paths as strings.
def find_java_files(directory_path: str) -> List[str]:

    path = Path(directory_path)

    if not path.exists():
        raise FileNotFoundError(f"Error: The directory '{directory_path}' does not exist.")
    
    if not path.is_dir():
        raise NotADirectoryError(f"Error: The path '{directory_path}' is not a directory.")

    java_files = [str(file.resolve()) for file in path.rglob("*.java") if file.is_file()]

    return java_files

########## Main functions ##########

# Returns the operating system name and architecture.
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
    # arm64 on my Mac M1.
    # ARM64 on my Windows ARM64 virtual machine.
    # x86_64 on my Linux server.
    # amd64 on my Windows desktop.
    if 'arm64' in arch().lower():
        mosname += " ARM64"
    elif 'amd64' in arch().lower() or 'x86_64' in arch().lower():
        mosname += " x64"
    else:
        print("Error: Unsupported architecture.", file=stderr)
        exit(1)
    if 'arm' in mosname.lower() and 'windows' in mosname.lower():
        print("Error: Unsupported architecture for Windows.", file=stderr)
        exit(1)
    print(f"OK: Operating system and architecture detected: {mosname}.")
    return mosname

# Check if the current working directory is 'CROSS/Java'.
def cwdchecks() -> None:
    cwd = Path.cwd()
    if cwd.name == 'Java' and cwd.parent.name == 'CROSS':
        print("OK: Current working directory is 'CROSS/Java'.")
    else:
        print("Error: Current working directory is NOT 'CROSS/Java'. Change it to 'CROSS/Java' and try again.", file=stderr)
        exit(1)

# Get as argument the operating system name and architecture.
# Check if the Java version .zip file exists and unzip it.
# Otherise, throw an error to ask the user if he wants to download the Java versions .zip files through this script.
def unzippingjava(mosname: str) -> None:
    basic_java_path = "./Versions"
    zip_path = basic_java_path + "/" + mosname.split()[0] + "_" + mosname.split()[1] + ".zip"
    try:
        if Path(zip_path).exists() and Path(zip_path).is_file() and path.getsize(zip_path) / 1024 < 100:
            raise FileNotFoundError(f"The Java version .zip file '{zip_path}' are not valid. Maybe are the default ones (placeholder) downloaded by git or GitHub without the git lfs support?")
        unzip_file(zip_path)
        print(f"OK: Unzipped {zip_path} to './Unzipped'.")
    except FileNotFoundError as e:
        print(f"Error: An error occurred while unzipping the Java version file: {e}", file=stderr)
        while True:
            answer = input("Do you want to download the Java versions .zip files? (Y/n): ")
            if answer.lower() == "" or answer.lower()[0] == "y":
                try:
                    download_java_versions()
                except Exception as e:
                    # Message error crafted by the called function.
                    print(e, file=stderr)
                    exit(1)
                print("OK: Downloaded the Java versions .zip files in './Versions'.")
                # Recursive call, but now the Java version .zip file exists.
                unzippingjava(mosname)
                break
            elif answer.lower() == "n":
                print("OK: Exiting the program.", file=stderr)
                exit(0)
            else:
                print("Error: Invalid answer. Please enter 'Y' or 'n'.", file=stderr)

# Return the directory of the CROSS project (with the 'src' directory inside) and a list of the .java files to compile as strings, all in a tuple.
def findjavafilestocompile() -> Tuple[Path, List[str]]:
    java_files = []
    CROSS_dir = Path("../CROSS")
    if not CROSS_dir.exists():
        print(f"Error: The directory with the source code '{str(CROSS_dir)}' does not exist.", file=stderr)
        exit(1)

    try:
        java_files = find_java_files(str(CROSS_dir) + "/src")
        print(f"OK: Found {len(java_files)} .java file(s) to compile:")
        for file in java_files:
            print(file)
    except (FileNotFoundError, NotADirectoryError) as e:
        # Message error crafted by the called function.
        print(e, file=stderr)
        exit(1)

    return (CROSS_dir, java_files)

# Find the 'javac' executable starting from the given path.
# Return the path of the 'javac' executable as a string.
def findjavac(start_path: str, mosname: str) -> str:

    start_path = Path(start_path)
    java_bin_path = ""
    # Find first 'bin' occurrency directory starting from the given path.
    for bin_dir in start_path.glob('**/bin'):
        if bin_dir.is_dir():
            java_bin_path = bin_dir
            break

    if java_bin_path == "":
        print(f"Error: No subfolder named 'bin' (containing the 'javac' executable) found starting searching from '{start_path}'.", file=stderr)
        exit(1)
   
    javac_bin = java_bin_path / "javac"

    if "windows" in mosname.lower():
        # On Windows, the 'javac' executable is named 'javac.exe'.
        javac_bin = javac_bin.with_suffix(".exe")

    if javac_bin.exists():
        return str(javac_bin)
    else:
        print(f"Error: The 'javac' executable was not found in the 'bin' directory '{java_bin_path}'.", file=stderr)
        exit(1)

# Make the given file executable, maybe not needed for Windows.
# Used to make the 'javac' executable executable.
def makeexecutable(file_path: str) -> None:
    from os import stat as osstat
    from os import chmod
    import stat

    current_permissions = osstat(file_path).st_mode

    try:
        executable_permissions = current_permissions | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH
        chmod(file_path, executable_permissions)
    except Exception as e:
        print(f"Error: An error occurred while setting executable permissions for '{file_path}': {e}", file=stderr)
        exit(1)

    print(f"OK: Set executable permissions for '{file_path}' successfully.")

# Compile the given Java files as java_files argument, in a strings list format.
# The CROSS_dir argument is the path of the CROSS (the one containing the 'src' directory inside) project directory.
# The javac_bin argument is the path of the 'javac' executable used to compile the Java files.
def compilejavafiles(java_files: List[str], CROSS_dir: Path, javac_bin: str, mosname: str) -> None:

    # This is something like: "'file1.java' 'file2.java' 'file3.java'".
    java_files_str = " ".join(list(map(lambda e: "'" + e + "'", java_files)))

    # The '-cp' argument is used to specify the classpath of the Java files, so the libraries.
    # The '-d' argument is used to specify the output directory of the compiled Java files.
    command = f"./{javac_bin} -cp '{CROSS_dir}/lib/*' -d '../bin' {java_files_str}"

    if "windows" in mosname.lower():
        # On Windows, we need to use double quotes instead of single quotes.
        command = command.replace("'", "\"")
        command = command[2:]  # Remove the leading './' to make it compatible with Windows.
        command = command.replace("/", "\\")

    try:
        process = exec(command, shell=True, stdout=PIPE, stderr=PIPE)
        stdout, stderr2 = process.communicate()
        if process.returncode != 0:
            error_message = stderr.decode().strip()
            print(f"Error: An error occured while compiling the Java files: {error_message}", file=stderr)
            exit(1)
        print("OK: Compilation completed successfully.")
    except Exception as e:
        print(f"Error: The compilation failed with error: {e}", file=stderr)
        exit(1)

def execute(java_bin: str, mosname: str) -> None:

    serverorclient = input("Do you want to execute the server or the client? (s/c): ")

    if len(serverorclient) > 0 and serverorclient.lower()[0] == "s":
        print("INFO: Executing the server...")
        cmd = f"cd .. && {'Java/' + java_bin} -cp \"./bin:./CROSS/lib/*\" MainServer"
        if "windows" in mosname.lower():
            cmd = cmd.replace("/", "\\").replace(":", ";")
        print(f"INFO: Executing command: {cmd}")
        process = exec(cmd, shell=True)
        try:
            process.wait()
        except KeyboardInterrupt:
            print("INFO: Server execution interrupted by user.")
            process.terminate()
    elif len(serverorclient) > 0 and serverorclient.lower()[0] == "c":
        print("INFO: Executing the client...")
        cmd = f"cd .. && {"Java/" + java_bin} -cp \"./bin:./CROSS/lib/*\" MainClient"
        print(f"INFO: Executing command: {cmd}")
        process = exec(cmd, shell=True)
        try:
            process.wait()
        except KeyboardInterrupt:
            print("INFO: Client execution interrupted by user.")
            process.terminate()
    else:
        print("Error: Invalid input. Please enter 's' for server or 'c' for client.", file=stderr)

def cleanup() -> None:
    try:
        rmtree("./Unzipped")
        print("OK: Cleaned up the 'Unzipped' directory.")
    except Exception as e:
        pass  # If the directory does not exist, we can ignore this error.
    try:
        rmtree(f"{CROSS_dir}/../bin/")
        print("OK: Cleaned up the 'bin' directory.")
    except Exception as e:
        pass # If the directory does not exist, we can ignore this error.

if __name__ == "__main__":
    mosname = oschecks()
    cwdchecks()

    # If the Java version .zip file is not found, ask the user if he wants to download the Java versions .zip files through this script.
    unzippingjava(mosname)

    CROSS_dir, java_files = findjavafilestocompile()

    javac_bin = findjavac(".", mosname)

    java_bin = str(javac_bin).replace("javac", "java")
    if not Path(java_bin).exists():
        print(f"Error: The 'java' executable was not found in the 'bin' directory '{javac_bin}'.", file=stderr)
        exit(1)

    if "linux" in mosname.lower() or "mac" in mosname.lower():
        makeexecutable(javac_bin)

    default_orders_file = "../DB/Orders/defaultOrders.json"
    orders_file = "../DB/Orders/orders.json"
    try:
        overwrite = input(f"Do you want to overwrite the current ORDERS {orders_file} file with the default one {default_orders_file}? (Y/n): ")
        if overwrite.lower() == "" or overwrite.lower()[0] == "y":
            print("INFO: Overwriting the orders file with the default one...")
        elif overwrite.lower()[0] == "n":
            print("INFO: Not overwriting the orders file.")
        else:
            print("Error: Invalid answer. Please enter 'Y' or 'n'.", file=stderr)
            exit(1)
        copyfile(default_orders_file, orders_file)
    except Exception as e:
        print(f"Error: An error occurred while overwriting the orders file: {e}", file=stderr)
        exit(1)

    default_users_file = "../DB/Users/defaultUsers.json"
    users_file = "../DB/Users/users.json"
    try:
        overwrite = input(f"Do you want to overwrite the current USERS {users_file} file with the default one {default_users_file}? (Y/n): ")
        if overwrite.lower() == "" or overwrite.lower()[0] == "y":
            print("INFO: Overwriting the users file with the default one...")
        elif overwrite.lower()[0] == "n":
            print("INFO: Not overwriting the users file.")
        else:
            print("Error: Invalid answer. Please enter 'Y' or 'n'.", file=stderr)
            exit(1)
        copyfile(default_users_file, users_file)
    except Exception as e:
        print(f"Error: An error occurred while overwriting the users file: {e}", file=stderr)
        exit(1)

    print("OK: All checks passed.")
    print("INFO: Compiling Java files...")

    compilejavafiles(java_files, str(CROSS_dir), javac_bin, mosname)

    execute(java_bin, mosname)

    cleanup()

    print("OK: All operations completed successfully, exiting the program.")
    exit(0)
else:
    print("Error: This script is not meant to be imported as a module.", file=stderr)
    exit(1)


