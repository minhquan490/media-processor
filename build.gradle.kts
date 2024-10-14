plugins {
    id("java")
}

group = "org.media.processor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.bytedeco:javacv:1.5.10")
    implementation("org.bytedeco:javacpp:1.5.10")
    implementation("org.bytedeco:javacv-platform:1.5.10")
    implementation("org.bytedeco:ffmpeg-platform-gpl:6.1.1-1.5.10")
    implementation("org.apache.xmlgraphics:batik-transcoder:1.18")
    implementation("org.apache.xmlgraphics:batik-codec:1.18")
    implementation("org.apache.xmlgraphics:batik-dom:1.18")
    implementation("org.apache.xmlgraphics:batik-svggen:1.18")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("commons-io:commons-io:2.17.0")
    implementation("org.jetbrains:annotations:26.0.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}