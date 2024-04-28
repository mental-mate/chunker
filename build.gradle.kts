version = "1.0.0"

val harmonysoftLibsVersion by extra { "3.1.0" }
val mentalMateLibrariesVersion by extra { "2.1.0" }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("tech.harmonysoft:mental-mate-llm:$mentalMateLibrariesVersion")
    implementation("tech.harmonysoft:mental-mate-data-storage:$mentalMateLibrariesVersion")
    implementation("tech.harmonysoft:mental-mate-meta-storage:$mentalMateLibrariesVersion")
    implementation("tech.harmonysoft:mental-mate-util:$mentalMateLibrariesVersion")

    implementation("tech.harmonysoft:harmonysoft-slf4j-spring:$harmonysoftLibsVersion")
    implementation("tech.harmonysoft:harmonysoft-default-implementations:$harmonysoftLibsVersion")

    testImplementation("tech.harmonysoft:mental-mate-data-storage-memory:$mentalMateLibrariesVersion")
    testImplementation("tech.harmonysoft:mental-mate-data-storage-test:$mentalMateLibrariesVersion")
    testImplementation("tech.harmonysoft:mental-mate-meta-storage-mongo:$mentalMateLibrariesVersion")
    testImplementation("tech.harmonysoft:mental-mate-llm-test:$mentalMateLibrariesVersion")

    testImplementation("tech.harmonysoft:harmonysoft-mongo-cucumber:$harmonysoftLibsVersion")
    testImplementation("tech.harmonysoft:harmonysoft-mongo-environment-testcontainers:$harmonysoftLibsVersion")
    testImplementation("tech.harmonysoft:harmonysoft-http-client-apache-cucumber-spring:$harmonysoftLibsVersion")
}
