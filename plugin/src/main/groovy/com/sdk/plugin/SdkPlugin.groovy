package com.sdk.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class SdkPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.android.registerTransform(new SdkPluginTransform(project))
    }
}