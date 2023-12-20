#!/bin/bash
# Function to check if a command is available
check_command() {
  command -v "$1" >/dev/null 2>&1 || { echo >&2 "Error: $1 is not installed. Aborting."; exit 1; }
}

# Check required commands
check_command "cp"
check_command "unzip"
check_command "zip"

if [ "$#" -lt 2 ]; then
  echo "Usage: $0 <folder_path> <zip_file_path> [dest_folder]"
  exit 1
fi

folder_path="$1"
zip_file_path="$2"
# Generated from folder_path by default
dest="${3:-${folder_path##*/}-injected}"

# Step 1: Copy the folder recursively to a new folder
cp -r "$folder_path" "$dest"

# Step 2: Decompress the zip file into the new folder, overwriting existing files
unzip -o "$zip_file_path" -d "$dest"

# Step 3: Compress the files under the new folder to a new zip file
cd "$dest" && zip -r "../${dest}.zip" * && cd ..

# Step 4: Remove the temporary folder
rm -r "$dest"

echo "Task completed successfully."
