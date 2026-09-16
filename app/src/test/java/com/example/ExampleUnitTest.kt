package com.example

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun untangler_findUniqueFile_resolvesConflicts() {
    val tempDir = Files.createTempDirectory("test_untangle").toFile()
    try {
      val file1 = File(tempDir, "doc.txt")
      file1.writeText("first")

      val unique = UntanglerEngine.findUniqueFile(tempDir, "doc.txt")
      assertEquals("doc_1.txt", unique.name)

      val file2 = File(tempDir, "doc_1.txt")
      file2.writeText("second")

      val unique2 = UntanglerEngine.findUniqueFile(tempDir, "doc.txt")
      assertEquals("doc_2.txt", unique2.name)
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun untangler_dryRun_identifiesNestedFiles() {
    val tempDir = Files.createTempDirectory("test_untangle_structure").toFile()
    try {
      val subfolder = File(tempDir, "SubFolder1")
      subfolder.mkdirs()
      val nestedFile = File(subfolder, "video.mp4")
      nestedFile.writeText("sample data")

      val summary = UntanglerEngine.process(
        targetPath = tempDir.absolutePath,
        isDryRun = true
      )

      assertEquals(1, summary.foldersProcessed)
      assertEquals(1, summary.filesPromoted)
      // Dry run must NOT delete or move actual files
      assertTrue(nestedFile.exists())
      assertTrue(subfolder.exists())
    } finally {
      tempDir.deleteRecursively()
    }
  }
}
