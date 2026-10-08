plugins {
    id("java-library")
}
description = "Protocol-neutral invocation, context, and parameter binding contracts for Effi RPC."
dependencies {
    api(project(":effi-rpc-component"))
}
