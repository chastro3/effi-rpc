object PublishConfig {
    /** Plugin ID for the internal-maven-publish plugin */
    const val PLUGIN_ID = "internal-maven-publish"
    /** GitHub repository URL, e.g. https://github.com/chastro3/uniplat */
    const val REPOSITORY_URL = "maven.publish.repository.url"
    /** Developer name for POM metadata */
    const val DEVELOPER_NAME = "maven.publish.developer.name"
    /** Developer email for POM metadata */
    const val DEVELOPER_EMAIL = "maven.publish.developer.email"
    /** License name (optional, defaults to Apache 2.0) */
    const val LICENSE_NAME = "maven.publish.license.name"
    /** License URL (optional, defaults to Apache 2.0 URL) */
    const val LICENSE_URL = "maven.publish.license.url"
    /** SCM connection URL (optional, derived from repository URL) */
    const val SCM_URL = "maven.publish.scm.url"
    /** GPG signing key ID (last 8 hex chars) */
    const val SIGNING_KEY_ID = "signing.keyId"
    /** GPG secret key ring file path */
    const val SIGNING_SECRET_KEY_RING_FILE = "signing.secretKeyRingFile"
    /** GPG key passphrase */
    const val SIGNING_PASSWORD = "signing.password"
    /** Maven Central Portal username (from central.sonatype.com token) */
    const val CENTRAL_USERNAME = "maven.central.username"
    /** Maven Central Portal password (from central.sonatype.com token) */
    const val CENTRAL_PASSWORD = "maven.central.password"
}
