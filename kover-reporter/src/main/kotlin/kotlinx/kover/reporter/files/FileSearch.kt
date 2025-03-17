package kotlinx.kover.reporter.files

import org.objectweb.asm.tree.ClassNode
import kotlinx.kover.reporter.utils.packageName
import java.io.File

internal fun findSourceFile(roots: List<File>, classNode: ClassNode): File {
    // TODO why?!
    if (classNode.sourceFile == null) throw Exception("WHY???")

    val packageName = classNode.packageName
    val packageSegments = packageName.split(".")
    if (packageName.isNotEmpty()) {
        roots.forEach { srcRoot ->
            val file = resolvePath(srcRoot, packageSegments).resolve(classNode.sourceFile)
            if (file.exists() && file.isFile) return file
        }
    }

    // package name does not match the directory name - looking for all files with given name
    val candidates = roots.flatMap { srcRoot ->
        srcRoot.walk().filter { file -> file.isFile && file.name == classNode.sourceFile}
    }

    if (candidates.isEmpty()) throw Exception("File ${classNode.sourceFile} not found for class ${classNode.name} in sources")
    if (candidates.size > 1) throw Exception("Multiple files named ${classNode.sourceFile} found\nCandidates: ${candidates.joinToString(",")}")

    return candidates.first()
}


internal fun resolvePath(srcRoot: File, childSegments: List<String>): File {
    var result = srcRoot
    childSegments.forEach { segment ->
        result = File(result, segment)
    }
    return result
}

/*
- пропускаем комменты
- строки до package не может быть
- fun/class/interface/object 
 */