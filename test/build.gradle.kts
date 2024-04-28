version = "1.0.0"

val harmonysoftLibsVersion: String by rootProject.extra
val mentalMateLibrariesVersion: String by rootProject.extra

dependencies {
    testImplementation("tech.harmonysoft:harmonysoft-default-implementations:$harmonysoftLibsVersion")

    testImplementation("tech.harmonysoft:mental-mate-data-storage-memory:$mentalMateLibrariesVersion")
    testImplementation("tech.harmonysoft:mental-mate-data-storage-test:$mentalMateLibrariesVersion")
    testImplementation("tech.harmonysoft:mental-mate-meta-storage-mongo:$mentalMateLibrariesVersion")
    testImplementation("tech.harmonysoft:mental-mate-llm-test:$mentalMateLibrariesVersion")

    testImplementation("tech.harmonysoft:harmonysoft-mongo-cucumber:$harmonysoftLibsVersion")
    testImplementation("tech.harmonysoft:harmonysoft-mongo-environment-testcontainers:$harmonysoftLibsVersion")
    testImplementation("tech.harmonysoft:harmonysoft-http-client-apache-cucumber-spring:$harmonysoftLibsVersion")
}
