plugins {
    id("java")
    id("maven-publish")
}

group = "net.cc"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.kyori:adventure-api:4.26.1")
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = "core"
        groupId = group.toString()
        from(components["java"])
    }
    repositories.maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/Project-Everything/cc-core")
        credentials {
            username = System.getenv("REPOSITORY_USER")
            password = System.getenv("REPOSITORY_TOKEN")
        }
    }
}