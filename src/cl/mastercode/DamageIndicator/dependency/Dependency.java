package cl.mastercode.DamageIndicator.dependency;

public enum Dependency {
    KYORI_API("net{}kyori", "adventure-api", DependencyManager.KYORI_VERSION),
    KYORI_KEY("net{}kyori", "adventure-key", DependencyManager.KYORI_VERSION),
    KYORI_EXAMINATION_API("net{}kyori", "examination-api", DependencyManager.KYORI_EXAMINATION_VERSION),
    KYORI_EXAMINATION_STRING("net{}kyori", "examination-string", DependencyManager.KYORI_EXAMINATION_VERSION),
    KYORI_NBT("net{}kyori", "adventure-nbt", DependencyManager.KYORI_VERSION),
    KYORI_GSON("net{}kyori", "adventure-text-serializer-gson", DependencyManager.KYORI_VERSION),
    KYORI_GSON_LEGACY("net{}kyori", "adventure-text-serializer-gson-legacy-impl", DependencyManager.KYORI_VERSION),
    KYORI_LEGACY_SERIALIZER("net{}kyori", "adventure-text-serializer-legacy", DependencyManager.KYORI_VERSION),
    KYORI_MINI_MESSAGE("net{}kyori", "adventure-text-minimessage", DependencyManager.KYORI_VERSION),
    KYORI_PLATFORM_API("net{}kyori", "adventure-platform-api", DependencyManager.KYORI_PLATFORM_VERSION),
    KYORI_PLATFORM_BUKKIT("net{}kyori", "adventure-platform-bukkit", DependencyManager.KYORI_PLATFORM_VERSION),
    KYORI_PLATFORM_BUNGEECORD("net{}kyori", "adventure-text-serializer-bungeecord", DependencyManager.KYORI_PLATFORM_VERSION),
    KYORI_PLATFORM_FACET("net{}kyori", "adventure-platform-facet", DependencyManager.KYORI_PLATFORM_VERSION),
    KYORI_PLATFORM_VIAVERSION("net{}kyori", "adventure-platform-viaversion", DependencyManager.KYORI_PLATFORM_VERSION);

    private final String groupId;
    private final String artifactId;
    private final String version;

    Dependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }
}
