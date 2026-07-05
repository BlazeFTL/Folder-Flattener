# Folder Flattener

An Android app that flattens subdirectory structures.

## What It Does

For each top-level folder:
- If it contains a **subfolder**, moves all files from that subfolder up one level and removes the empty subfolder.
- If the folder then contains a **single file**, moves that file to the parent directory and removes the now-empty folder.

**Example:**
Step 1: It looks inside a folder. If it finds an inner subfolder (like data inside Android), it pulls the contents up into the parent folder and deletes the empty inner folder.
Step 2: Right after, it checks if that folder now contains only a single file (like File.txt or your newly flattened folders). If it does, it moves that file out to the main directory and deletes the empty parent folder.

```
Before:
Folder/A/A_inner/file.txt
or
B/only_file.jpg

After:
Folder/A/file.txt
only_file.jpg(In Parent Folder)
```

Hidden files/folders are left untouched.
<img width="702" height="1560" alt="Screenshot_20260705-145221_Spark Launcher" src="https://github.com/user-attachments/assets/05e490ee-29e6-4239-936e-818dc95fafe7" />


</p>
</details>

## Equivalent Shell Logic

```sh
cd "/storage/emulated/0/Folder/SubFolder" && \
for d in */; do
  d=${d%/}
  subdir=$(find "$d" -mindepth 1 -maxdepth 1 -type d | head -n 1)
  if [ -n "$subdir" ]; then
    mv "$subdir"/* "$d/" 2>/dev/null
    rmdir "$subdir" 2>/dev/null
  fi
  [ $(find "$d" -maxdepth 1 | wc -l) -eq 2 ] && [ -f "$d"/* ] && \
    mv "$d"/* . 2>/dev/null && rmdir "$d"
done
```

## Download

Head to [**Releases**](../../releases/latest) and download the latest `.apk`.

## Install

1. Enable **Install unknown apps** for your browser/file manager in Settings.
2. Open the downloaded APK and tap **Install**.

## Permissions Required

- `READ_EXTERNAL_STORAGE`
- `WRITE_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE` (Android 11+)

## Requirements

- Android 8.0+

## Usage

1. Open the app.
2. Tap **Run**.
3. Review the log output for moved/flattened entries.

## License

MIT
