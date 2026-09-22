plugins {
    id("dev.kikugie.stonecutter")
    id("dev.isxander.secrets") version "0.1.0"
}
stonecutter active file("versions/current")

version = property("modVersion").toString()

repositories {
    mavenCentral()
}
