package buct.tzx.buildtools

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AutoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        System.out.println("开始注册")
        var android = project.extensions.getByType(BaseExtension::class.java)
        var transform = AutoServiceTransform()
        android.registerTransform(transform)
    }

}