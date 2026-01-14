plugins {
    id("dev.kikugie.stonecutter")
    id("dev.isxander.secrets") version "0.1.0"
    id("com.gradleup.nmcp.aggregation") version "1.4.3"
}
stonecutter active file("versions/current")

nmcpAggregation {
    centralPortal {
        username = secrets.gradleProperty("mcentral.username")
        password = secrets.gradleProperty("mcentral.password")

        publicationName = "yet-another-config-lib:$version"
    }
}
dependencies {
    allprojects {
        nmcpAggregation(project(path))
    }
}
