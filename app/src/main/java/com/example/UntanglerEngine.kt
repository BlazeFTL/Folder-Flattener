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

    private fun getAllFilesRecursively(dir: File): List<File> {
        val result = mutableListOf<File>()
        val files = dir.listFiles() ?: return result
        for (f in files) {
            if (f.name.startsWith(".")) continue
            if (f.isDirectory) {
                result.addAll(getAllFilesRecursively(f))
            } else if (f.isFile) {
                result.add(f)
            }
        }
        return result
    }

    private fun getSubdirectoriesBottomUp(dir: File): List<File> {
        val result = mutableListOf<File>()
        val files = dir.listFiles() ?: return result
        for (f in files) {
            if (f.name.startsWith(".")) continue
            if (f.isDirectory) {
                result.addAll(getSubdirectoriesBottomUp(f))
                result.add(f)
            }
        }
        return result
    }

    /**
     * Recursively computes the action list or executes them.
     */
    fun process(
        targetPath: String,
        isDryRun: Boolean,
        onProgress: ((current: Int, total: Int, currentFolder: String) -> Unit)? = null,
        onLog: ((String) -> Unit)? = null
    ): UntangleSummary {
        val rootDir = File(targetPath)
        val logs = mutableListOf<String>()
        val actions = mutableListOf<UntangleAction>()

        fun recordLog(msg: String) {
            logs.add(msg)
            onLog?.invoke(msg)
        }

        recordLog("Initial Directory: ${rootDir.absolutePath}")
        
        // Ensure root directory exists and is valid
        try {
            if (!rootDir.exists()) {
                recordLog("❌ ERROR: Target path does not exist!")
                return UntangleSummary(0, 0, 0, 0, emptyList(), logs)
            }
            if (!rootDir.isDirectory) {
                recordLog("❌ ERROR: Target path is a file, not a directory!")
                return UntangleSummary(0, 0, 0, 0, emptyList(), logs)
            }
            if (!rootDir.canRead()) {
                recordLog("❌ ERROR: Permission denied. Cannot read directory!")
                return UntangleSummary(0, 0, 0, 0, emptyList(), logs)
            }
        } catch (e: Exception) {
            recordLog("❌ ERROR checking root directory: ${e.localizedMessage ?: "Unknown error"}")
            return UntangleSummary(0, 0, 0, 0, emptyList(), logs)
        }

        if (isDryRun) {
            recordLog("⏩ SAFE PREVIEW MODE ACTIVE — No files will be modified.")
        } else {
            recordLog("⚡ LIVE MODE ACTIVE — Moving files and organizing folders...")
        }

        // Get all top-level directories under root, ignoring hidden ones
        val dList = try {
            rootDir.listFiles { file -> file.isDirectory && !file.name.startsWith(".") } ?: emptyArray()
        } catch (e: Exception) {
            recordLog("❌ ERROR: Failed to list directories in ${rootDir.name} due to security constraints. (${e.localizedMessage})")
            return UntangleSummary(0, 0, 0, 0, emptyList(), logs)
        }
        
        val totalFolders = dList.size
        recordLog("Found $totalFolders subdirectories to inspect in ${rootDir.name}.")

        var foldersProcessed = 0
        var foldersFlattened = 0
        var filesPromoted = 0
        var foldersDeleted = 0

        for (d in dList) {
            foldersProcessed++
            onProgress?.invoke(foldersProcessed, totalFolders, d.name)

            // Check if directory can be read
            if (!d.canRead()) {
                recordLog("  ❌ Failed to inspect '${d.name}': Permission denied (Unreadable)")
                continue
            }

            // 1. Identify direct subdirectories inside d (ignoring hidden)
            val directSubDirs = try {
                d.listFiles { file -> file.isDirectory && !file.name.startsWith(".") } ?: emptyArray()
            } catch (e: Exception) {
                emptyArray()
            }

            if (directSubDirs.isNotEmpty()) {
                // Collect all files inside subfolders at any depth
                val subfolderFiles = mutableListOf<File>()
                for (subDir in directSubDirs) {
                    subfolderFiles.addAll(getAllFilesRecursively(subDir))
                }

                // Unnest all subfolder files up into d
                for (fileItem in subfolderFiles) {
                    val destination = File(d, fileItem.name)
                    actions.add(
                        UntangleAction(
                            type = ActionType.FLATTEN_SUBFOLDER,
                            sourcePath = fileItem.absolutePath,
                            destPath = destination.absolutePath,
                            folderName = d.name,
                            description = "Unnest '${fileItem.name}' into parent folder '${d.name}/'"
                        )
                    )

                    if (!isDryRun) {
                        try {
                            if (!fileItem.canRead() || !d.canWrite()) {
                                recordLog("  ❌ Failed to move '${fileItem.name}': Access denied")
                                continue
                            }
                            val uniqueDest = findUniqueFile(d, fileItem.name)
                            if (fileItem.renameTo(uniqueDest)) {
                                recordLog("  ✔ Successfully unnested subfolder file: ${fileItem.name} into ${d.name}")
                            } else {
                                recordLog("  ❌ Failed to move '${fileItem.name}': Rename failed")
                            }
                        } catch (e: Exception) {
                            recordLog("  ❌ Error moving '${fileItem.name}': ${e.localizedMessage}")
                        }
                    } else {
                        recordLog("  ⏩ [Preview] Would unnest file '${fileItem.name}' into ${d.name}/")
                    }
                }

                // Delete all empty subfolders bottom-up
                val subDirsBottomUp = mutableListOf<File>()
                for (subDir in directSubDirs) {
                    subDirsBottomUp.addAll(getSubdirectoriesBottomUp(subDir))
                    subDirsBottomUp.add(subDir)
                }

                for (subDir in subDirsBottomUp) {
                    actions.add(
                        UntangleAction(
                            type = ActionType.DELETE_EMPTY_FOLDER,
                            sourcePath = subDir.absolutePath,
                            destPath = "",
                            folderName = d.name,
                            description = "Delete empty subfolder: ${subDir.name}"
                        )
                    )

                    if (!isDryRun) {
                        try {
                            if (subDir.delete()) {
                                recordLog("  🗑 Deleted empty subfolder '${subDir.name}'")
                                foldersFlattened++
                            } else {
                                recordLog("  ⚠ Could not delete subfolder '${subDir.name}'")
                            }
                        } catch (e: Exception) {
                            recordLog("  ⚠ Error deleting subfolder '${subDir.name}': ${e.localizedMessage}")
                        }
                    } else {
                        foldersFlattened++
                        recordLog("  ⏩ [Preview] Would delete empty subfolder '${subDir.name}'")
                    }
                }
            }

            // 2. Check d's contents after unnesting subfolders
            val currentContents: List<File> = if (!isDryRun) {
                try {
                    d.listFiles()?.filter { !it.name.startsWith(".") } ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                val directFiles = try {
                    d.listFiles()?.filter { !it.name.startsWith(".") && it.isFile } ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
                val subFiles = mutableListOf<File>()
                for (subDir in directSubDirs) {
                    subFiles.addAll(getAllFilesRecursively(subDir))
                }
                directFiles + subFiles
            }

            if (currentContents.size == 1 && currentContents[0].isFile) {
                val singleItem = currentContents[0]
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
                            recordLog("  ❌ Failed to move file '${singleItem.name}': Access denied")
                            continue
                        }
                        val uniqueDest = findUniqueFile(rootDir, singleItem.name)
                        if (singleItem.renameTo(uniqueDest)) {
                            recordLog("  ✨ Successfully moved single file: ${singleItem.name} up to root directory")

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
                                    recordLog("  🗑 Deleted empty parent container '${d.name}'")
                                    foldersDeleted++
                                } else {
                                    recordLog("  ⚠ Could not delete empty parent container '${d.name}'")
                                }
                            } catch (ex: Exception) {
                                recordLog("  ⚠ Error deleting parent container '${d.name}': ${ex.localizedMessage}")
                            }
                            filesPromoted++
                        } else {
                            recordLog("  ❌ Failed to promote file '${singleItem.name}'")
                        }
                    } catch (e: Exception) {
                        recordLog("  ❌ Error promoting file '${singleItem.name}': ${e.localizedMessage}")
                    }
                } else {
                    recordLog("  ⏩ [Preview] Would move single file '${singleItem.name}' up to root directory")
                    recordLog("  ⏩ [Preview] Would delete redundant parent container '${d.name}'")
                    filesPromoted++
                    foldersDeleted++
                }
            } else if (currentContents.isEmpty()) {
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
                            recordLog("  🗑 Deleted empty folder '${d.name}'")
                            foldersDeleted++
                        } else {
                            recordLog("  ⚠ Could not delete empty folder '${d.name}'")
                        }
                    } catch (e: Exception) {
                        recordLog("  ⚠ Error deleting folder '${d.name}': ${e.localizedMessage}")
                    }
                } else {
                    recordLog("  ⏩ [Preview] Would delete redundant empty folder '${d.name}'")
                    foldersDeleted++
                }
            }
        }

        if (isDryRun) {
            recordLog("✨ Safe Preview Complete: 0 files modified on disk.")
            recordLog("   Calculated candidates: $filesPromoted files to promote, $foldersDeleted folders to delete.")
        } else {
            recordLog("✨ Clean Sweep Complete! Successfully processed $foldersProcessed folders.")
            recordLog("   Moved $filesPromoted files and deleted $foldersDeleted folders.")
        }

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
