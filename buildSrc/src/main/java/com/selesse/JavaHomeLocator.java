package com.selesse;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;

public class JavaHomeLocator {
    public static String locate(Project project) {
        return getJavaLauncher(project).getMetadata().getInstallationPath().getAsFile().getAbsolutePath();
    }

    private static JavaLauncher getJavaLauncher(Project project) {
        return getExtensions(project).getByType(JavaToolchainService.class)
                .launcherFor(getExtensions(project).getByType(JavaPluginExtension.class).getToolchain()).get();
    }

    private static ExtensionContainer getExtensions(Project project) {
        return project.getExtensions();
    }
}
