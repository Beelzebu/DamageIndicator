package cl.mastercode.DamageIndicator.dependency;

import cl.mastercode.DamageIndicator.DIMain;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.relocation.Relocation;

public class DependencyManager {
    public static final String KYORI_VERSION = "4.13.1";
    public static final String KYORI_EXAMINATION_VERSION = "1.3.0";
    public static final String KYORI_PLATFORM_VERSION = "4.3.4";

    private final BukkitLibraryManager libraryManager;

    public DependencyManager(DIMain plugin) {
        libraryManager = new BukkitLibraryManager(plugin);
    }

    public void loadDependencies() {
        Relocation relocation = Relocation.builder().pattern("net{}kyori").relocatedPattern("cl{}mastercode{}DamageIndicator{}libs{}kyori").build();
        libraryManager.addMavenCentral();
        for (Dependency dependency : Dependency.values()) {
            Library library = Library.builder().groupId(dependency.getGroupId()).artifactId(dependency.getArtifactId()).version(dependency.getVersion()).relocate(relocation).build();
            libraryManager.loadLibrary(library);
        }
    }
}
