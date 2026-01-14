plugins {
    id("dev.kikugie.stonecutter")
    id("dev.isxander.secrets") version "0.1.0"
    id("com.gradleup.nmcp.aggregation") version "1.4.3"
}
stonecutter active file("versions/current")

version = property("modVersion").toString()

repositories {
    mavenCentral()
}

nmcpAggregation {
    centralPortal {
        username = secrets.gradleProperty("mcentral.username")
        password = secrets.gradleProperty("mcentral.password")

        publicationName = "zoomify:$version"
    }
}
dependencies {
    allprojects {
        nmcpAggregation(project(path))
    }
}
