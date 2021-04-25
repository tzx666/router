package buct.tzx.buildtools

import buct.tzx.buildtools.utils.ScanConsts
import buct.tzx.buildtools.utils.ScanUtils
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.ImmutableSet
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream

class AutoServiceTransform : Transform() {
    companion object{
        public var scanConsts = ScanConsts("IPath")
        var targetFile:File? = null
    }
    override fun getName(): String = "autoService"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean = false


    override fun transform(
        context: Context?,
        inputs: MutableCollection<TransformInput>?,
        referencedInputs: MutableCollection<TransformInput>?,
        outputProvider: TransformOutputProvider?,
        isIncremental: Boolean
    ) {
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)
        var leftSlash = File.separator == "/";
        // 先遍历所有的文件，找到自动生成的文件并把类名储存起来
        inputs?.forEach { input->
            input.jarInputs.forEach {  jarInput ->
                var destName = jarInput.name
                        // rename jar files
                var hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length - 4)
                }
                // input file
                var src = jarInput.file
                        // output file
                var dest = outputProvider?.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                //scan jar file to find classes
//                if (ScanUtils.shouldProcessPreDexJar(src.absolutePath)) {
//                    ScanUtils.scanJar(src, dest)
//                }
                if (ScanUtils.shouldProcessPreDexJar(src.absolutePath)) {
                    ScanUtils.scanJar(src, dest)
                }
                FileUtils.copyFile(src, dest)

            }
            input.directoryInputs.forEach { directoryInput->
                var dest = outputProvider?.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                System.out.println("dest"+dest?.absolutePath)
                var root:String = directoryInput?.file?.absolutePath ?: ""
                if (root!=null&&!root.endsWith(File.separator)){
                    root += File.separator
                }
                directoryInput.file.walk().forEach { file->
                    var path = file.absolutePath.replace(root, "")
                    path = path.replace("\\", "/")
                    //System.out.println("scaning"+file.absolutePath)

                    if(path.equals(ScanConsts.GENERATE_TO_CLASS_FILE_NAME)){
                        println("注册中心"+path)
                        targetFile = dest;
                    }
                    if(file.isFile() && ScanUtils.shouldProcessClass(path)){
                        System.out.println("scan" + file.absolutePath)
                        ScanUtils.scanClass(file)
                    }
                }
                FileUtils.copyDirectory(directoryInput.file, dest)
                // copy to dest
            }
        }
        // 对每次transform扫描，扫描后都需要清空防止重复注册
        scanConsts.classList.forEach {
            System.out.println("找到了注册类" + it)
            ScanUtils.insertInitCodeTo()
        }

        // 对所有的文件进行字节码注册

    }
}