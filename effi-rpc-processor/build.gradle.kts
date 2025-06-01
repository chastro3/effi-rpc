plugins {
    id("java-library")
}
description= "Annotation processor for generating files or classes at compile time."
dependencies{
    api(project(":effi-rpc-annotation"))
}
