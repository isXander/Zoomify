plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.1" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "mod"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("chiseledReleaseMod", stonecutter.chiseled) {
    group = "mod"
    ofTask("releaseMod")
}
