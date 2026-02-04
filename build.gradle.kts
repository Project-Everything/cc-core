import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.3.0"
    id("io.freefair.lombok") version "9.1.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("de.eldoria.plugin-yml.paper") version "0.8.0"
}

group = "net.cc"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.glaremasters.me/repository/towny/")
}

dependencies {
    implementation(project(":core-api"))

    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("redis.clients:jedis:7.2.0")
    implementation("org.spongepowered:configurate-hocon:4.3.0-SNAPSHOT")
    implementation(platform("com.intellectualsites.bom:bom-newest:1.55"))

    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("me.clip:placeholderapi:2.11.7")
    compileOnly("com.intellectualsites.plotsquared:plotsquared-core")
    compileOnly("com.palmergames.bukkit.towny:towny:0.102.0.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    runServer {
        minecraftVersion("1.21.8")
    }
    shadowJar {
        archiveClassifier.set("")
    }
}

paper {
    name = "cc-core"
    version = project.version.toString()
    main = "net.cc.core.CorePlugin"
    description = "Core plugin for Creative Central"
    apiVersion = "1.21.8"
    website = "https://creative-central.net"
    authors = listOf("SpektrSoyuz", "scorch5000")
    foliaSupported = false

    serverDependencies {
        register("LuckPerms") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
            joinClasspath = true
        }
        register("PlotSquared") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }
        register("Towny") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }
        register("PlaceholderAPI") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
            joinClasspath = true
        }
    }

    permissions {
        register("cc.chat.color")

        register("cc.channel.global")
        register("cc.channel.staffchat")
        register("cc.channel.modchat")
        register("cc.channel.adminchat")
        register("cc.channel.friendchat")
        register("cc.channel.plots.local")
        register("cc.channel.earth.global")
        register("cc.channel.earth.local")
        register("cc.channel.earth.townchat")
        register("cc.channel.earth.nationchat")

        register("cc.command.block")
        register("cc.command.broadcast")
        register("cc.command.chatspy")
        register("cc.command.clearchat")
        register("cc.command.core")
        register("cc.command.displayname")
        register("cc.command.leaderboard")
        register("cc.command.list")
        register("cc.command.message.color")
        register("cc.command.message")
        register("cc.command.near")
        register("cc.command.reply")
        register("cc.command.seen")
        register("cc.command.stafflist")
        register("cc.command.tokens.other")
        register("cc.command.tokens")
        register("cc.command.unblock")
        register("cc.command.vanish")
        register("cc.command.votes.other")
        register("cc.command.votes")

        register("cc.channel.*") {
            this.description = "Grants access to all channels"
            this.default = BukkitPluginDescription.Permission.Default.OP
            this.children = listOf(
                "cc.channel.global",
                "cc.channel.staffchat",
                "cc.channel.modchat",
                "cc.channel.adminchat",
                "cc.channel.friendchat",
                "cc.channel.plots.local",
                "cc.channel.towny.global",
                "cc.channel.towny.local",
                "cc.channel.towny.townchat",
                "cc.channel.towny.nationchat",
            )
        }

        register("ae.command.*") {
            this.description = "Grants access to all commands"
            this.default = BukkitPluginDescription.Permission.Default.OP
            this.children = listOf(
                "cc.command.block",
                "cc.command.broadcast",
                "cc.command.chatspy",
                "cc.command.core",
                "cc.command.displayname",
                "cc.command.leaderboard",
                "cc.command.list",
                "cc.command.message.color",
                "cc.command.message",
                "cc.command.near",
                "cc.command.reply",
                "cc.command.seen",
                "cc.command.stafflist",
                "cc.command.tokens.other",
                "cc.command.tokens",
                "cc.command.unblock",
                "cc.command.vanish",
                "cc.command.votes.other",
                "cc.command.votes"
            )
        }

        register("cc.core.*") {
            this.description = "Grants all permissions"
            this.default = BukkitPluginDescription.Permission.Default.OP
            this.children = listOf(
                "cc.chat.color",
                "cc.channel.*",
                "cc.command.*"
            )
        }
    }
}