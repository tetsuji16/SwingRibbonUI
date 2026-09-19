plugins {
	`java-library`
}

group = "io.github.tetsuji16"
version = "0.2.0"

repositories {
	mavenCentral()
}

java {
	toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }
	withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(25)
	options.encoding = "UTF-8"
}

dependencies {
	testImplementation(platform("org.junit:junit-bom:5.10.2"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
	useJUnitPlatform()
}

tasks.register<JavaExec>("generatePreview") {
	group = "documentation"
	description = "Renders the deterministic README ribbon preview without capturing the desktop."
	classpath = sourceSets.main.get().runtimeClasspath
	mainClass.set("io.github.tetsuji16.swingribbonui.RibbonPreview")
}
