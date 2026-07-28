package com.example

import java.io.File

enum class ActionType {
    FLATTEN_SUBFOLDER,
    PROMOTE_SINGLE_FILE,
    DELETE_EMPTY_FOLDER,
    ERROR
}

data class UntangleAction(
    val type: ActionType,
    val sourcePath: String,
    val destPath: String,
    val folderName: String,
    val description: String
)

data class UntangleSummary(
    val foldersProcessed: Int,
    val foldersFlattened: Int,
    val filesPromoted: Int,
    val foldersDeleted: Int,
    val actions: List<UntangleAction>,
    val logs: List<String>
)

object UntanglerEngine {

    /**
     * Finds a unique filename in [parentDir] if [name] already exists, using numeric suffixing (e.g. file_1.mp4)
     */
    fun findUniqueFile(parentDir: File, name: String): File {
        var candidate = File(parentDir, name)
        if (!candidate.exists()) return candidate

        val baseName = candidate.nameWithoutExtension
        val extension = candidate.extension
        val extStr = if (extension.isNotEmpty()) ".$extension" else ""

        var counter = 1
        while (candidate.exists()) {
            candidate = File(parentDir, "${baseName}_$counter$extStr")
            counter++
        }
        return candidate
    }

    /**
     * Recursively computes the action list or executes them.
     */
    fun process(targetPath: String, isDryRun: Boolean): UntangleSummary {
        val rootDir = File(targetPath)
        val logs = mutableListOf<String>()
        val actions = mutableListOf<UntangleAction>()

        logs.add("Initial Directory: ${rootDir.absolutePath}")
        
        // Ensure root directory exists and is valid
        try {
            if (!rootDir.exists()) {
                logs.add("❌ ERROR: Target path does not exist!")
                return UntangleSummary(0, 0, 0, 0, emptyList(), logs)
            }
            if (!rootDir.isDirectory) {
                logs.add("❌ ERROR: Target path is a file, not a directory!")
                return UntangleSummary(0, 0, 0, 0, emptyList(), logs)
            }
            if (!rootDir.canRead()) {
                logs.add("❌ ERROR: Permission denied. Cannot read directory!")
                return UntangleSummary(0, 0, 0, 0, emptyList(), logs)
            }
        } catch (e: Exception) {
            logs.add("❌ ERROR checking root directory: ${e.localizedMessage ?: "Unknown error"}")
            return UntangleSummary(0, 0, 0, 0, emptyList(), logs)
        }

        if (isDryRun) {
            logs.add("⏩ SAFE PREVIEW MODE ACTIVE — No files will be modified.")
        } else {
            logs.add("⚡ LIVE MODE ACTIVE — Moving files and organizing folders...")
        }

        // Get all top-level directories under root, ignoring hidden ones
        val dList = try {
            rootDir.listFiles { file -> file.isDirectory && !file.name.startsWith(".") } ?: emptyArray()
        } catch (e: Exception) {
            logs.add("❌ ERROR: Failed to list directories in ${rootDir.name} due to security constraints. (${e.localizedMessage})")
            return UntangleSummary(0, 0, 0, 0, emptyList(), logs)
        }
        
        var foldersProcessed = 0
        var foldersFlattened = 0
        var filesPromoted = 0
        var foldersDeleted = 0

        for (d in dList) {
            foldersProcessed++

            // Check if directory can be read/written
            if (!d.canRead()) {
                logs.add("  ❌ Failed to inspect '${d.name}': Permission denied (Unreadable)")
                continue
            }

            // 1. Identify if d has direct subdirectories (not hidden)
            val nestedDirs = try {
                d.listFiles { file -> file.isDirectory && !file.name.startsWith(".") } ?: emptyArray()
            } catch (e: Exception) {
                logs.add("  ❌ Failed to read nested folders inside '${d.name}': Permission denied")
                emptyArray()
            }

            val firstNestedDir = nestedDirs.firstOrNull()

            if (firstNestedDir != null) {
                logs.add("ℹ Found subfolder inside custom folder: ${d.name}/${firstNestedDir.name}")
                val nestedContents = try {
                    firstNestedDir.listFiles() ?: emptyArray()
                } catch (e: Exception) {
                    logs.add("  ❌ Failed to retrieve subfolder contents of '${firstNestedDir.name}': Access denied")
                    emptyArray()
                }
                
                if (nestedContents.isNotEmpty()) {
                    for (item in nestedContents) {
                        val destination = File(d, item.name)
                        actions.add(
                            UntangleAction(
                                type = ActionType.FLATTEN_SUBFOLDER,
                                sourcePath = item.absolutePath,
                                destPath = destination.absolutePath,
                                folderName = d.name,
                                description = "Unnest '${firstNestedDir.name}/${item.name}' into parent folder '${d.name}/'"
                            )
                        )

                        if (!isDryRun) {
                            try {
                                if (!item.canRead() || !d.canWrite()) {
                                    logs.add("  ❌ Failed to move Nested Element '${item.name}' due to permissions")
                                    continue
                                }
                                val uniqueDest = findUniqueFile(d, item.name)
                                if (item.renameTo(uniqueDest)) {
                                    logs.add("  ✔ Successfully unnested subfolder element: ${item.name} -> ${uniqueDest.name}")
                                } else {
                                    logs.add("  ❌ Failed to move Nested Element: ${item.name} (File rename failed)")
                                }
                            } catch (e: Exception) {
                                logs.add("  ❌ Error moving Nested Element '${item.name}': ${e.localizedMessage}")
                            }
                        }
                    }
                }

                // Delete the subfolder
                actions.add(
                    UntangleAction(
                        type = ActionType.DELETE_EMPTY_FOLDER,
                        sourcePath = firstNestedDir.absolutePath,
                        destPath = "",
                        folderName = d.name,
                        description = "Delete empty subfolder: ${d.name}/${firstNestedDir.name}"
                    )
                )

                if (!isDryRun) {
                    try {
                        if (firstNestedDir.delete()) {
                            logs.add("  🗑 Deleted empty subfolder '${firstNestedDir.name}'")
                            foldersFlattened++
                        } else {
                            logs.add("  ⚠ Could not delete subfolder '${firstNestedDir.name}' (might not be empty or locked)")
                        }
                    } catch (e: Exception) {
                        logs.add("  ⚠ Error deleting subfolder '${firstNestedDir.name}': ${e.localizedMessage}")
                    }
                } else {
                    foldersFlattened++
                }
            }

            // 2. Refresh contents check: Is there exactly one regular file and nothing else?
            val currentContents = try {
                d.listFiles() ?: emptyArray()
            } catch (e: Exception) {
                emptyArray()
            }
            val activeContents = currentContents.filter { !it.name.startsWith(".") }

            if (activeContents.size == 1) {
                val singleItem = activeContents[0]
                if (singleItem.isFile) {
                    val finalDest = File(rootDir, singleItem.name)
                    actions.add(
                        UntangleAction(
                            type = ActionType.PROMOTE_SINGLE_FILE,
                            sourcePath = singleItem.absolutePath,
                            destPath = finalDest.absolutePath,
                            folderName = d.name,
                            description = "Move single file '${singleItem.name}' up to root folder '${rootDir.name}/'"
                        )
                    )

                    if (!isDryRun) {
                        try {
                            if (!singleItem.canRead() || !rootDir.canWrite()) {
                                logs.add("  ❌ Failed to move file '${singleItem.name}' due to permissions")
                                continue
                            }
                            val uniqueDest = findUniqueFile(rootDir, singleItem.name)
                            if (singleItem.renameTo(uniqueDest)) {
                                logs.add("  ✨ Successfully moved single file: ${singleItem.name} up to root directory")
                                
                                // Delete the empty outer directory d
                                actions.add(
                                    UntangleAction(
                                        type = ActionType.DELETE_EMPTY_FOLDER,
                                        sourcePath = d.absolutePath,
                                        destPath = "",
                                        folderName = d.name,
                                        description = "Delete empty container directory: ${d.name}"
                                    )
                                )
                                try {
                                    if (d.delete()) {
                                        logs.add("  🗑 Deleted empty parent container '${d.name}'")
                                        foldersDeleted++
                                    } else {
                                        logs.add("  ⚠ Could not delete empty parent container '${d.name}'")
                                    }
                                } catch (ex: Exception) {
                                    logs.add("  ⚠ Error deleting parent container '${d.name}': ${ex.localizedMessage}")
                                }
                                filesPromoted++
                            } else {
                                logs.add("  ❌ Failed to promote file '${singleItem.name}' (File rename returned false)")
                            }
                        } catch (e: Exception) {
                            logs.add("  ❌ Error promoting file '${singleItem.name}': ${e.localizedMessage}")
                        }
                    } else {
                        filesPromoted++
                        foldersDeleted++
                    }
                }
            } else if (activeContents.isEmpty()) {
                // Folder is empty, notify deletion
                actions.add(
                    UntangleAction(
                        type = ActionType.DELETE_EMPTY_FOLDER,
                        sourcePath = d.absolutePath,
                        destPath = "",
                        folderName = d.name,
                        description = "Delete redundant empty folder: ${d.name}"
                    )
                )
                if (!isDryRun) {
                    try {
                        if (d.delete()) {
                            logs.add("  🗑 Deleted empty folder '${d.name}'")
                            foldersDeleted++
                        } else {
                            logs.add("  ⚠ Could not delete empty folder '${d.name}'")
                        }
                    } catch (e: Exception) {
                        logs.add("  ⚠ Error deleting folder '${d.name}': ${e.localizedMessage}")
                    }
                } else {
                    foldersDeleted++
                }
            }
        }

        logs.add("All tasks successfully parsed!")

        return UntangleSummary(
            foldersProcessed = foldersProcessed,
            foldersFlattened = foldersFlattened,
            filesPromoted = filesPromoted,
            foldersDeleted = foldersDeleted,
            actions = actions,
            logs = logs
        )
    }
}
