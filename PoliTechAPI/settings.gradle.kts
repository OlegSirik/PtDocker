// foojay-resolver отключён для Docker — JDK уже в образе gradle:8.7-jdk21
// Исключает загрузку с plugins.gradle.org (Read timed out)
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
include("pt-payments")