plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "pt-app"

include("pt-api")
include("pt-numbers")
include("pt-launcher")
include("pt-db")
include("pt-db")
include("pt-process")
include("pt-product")
include("pt-calculator")
include("pt-auth")
include("pt-files")