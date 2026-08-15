<div align="center">

# 📂 Folder Flattener

**Collapse redundant nested subfolders in one tap**

An Android app that walks a directory and flattens folders that only contain a single nested subfolder or a single file, removing pointless folder-in-folder-in-folder structures.

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Min%20SDK](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)

</div>

---

## ⚙️ How It Works

For each top-level folder, the app finds its lone nested subfolder and merges the contents up a level — repeat this and a `Folder/Sub/Sub/file.jpg` mess becomes `Folder/file.jpg`. Equivalent shell logic:

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

<br>

## 📥 Download

Head to [**Releases**](../../releases/latest) and download the latest `.apk`.

<br>

## 🚀 Install

1. Enable **Install unknown apps** for your browser/file manager in Settings
2. Open the downloaded APK and tap **Install**

<br>

## 🔐 Permissions Required

| Permission | Notes |
|---|---|
| `READ_EXTERNAL_STORAGE` | Read folder contents |
| `WRITE_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE` | Move/delete entries (Android 11+) |

<br>

## 📋 Requirements

- Android 8.0+

<br>

## 🧭 Usage

1. Open the app
2. Tap **Run**
3. Review the log output for moved/flattened entries

<br>

## 📄 License

MIT
