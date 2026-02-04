package dev.mastern.plugins.hypeup.libraries;

import dev.mastern.plugins.hypeup.HypeUp;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;

public class LibraryLoader {
    
    private final HypeUp plugin;
    private final BukkitLibraryManager libraryManager;
    
    public LibraryLoader(HypeUp plugin) {
        this.plugin = plugin;
        this.libraryManager = new BukkitLibraryManager(plugin);
        libraryManager.addMavenCentral();
    }
    
    public void loadLibraries() {
        plugin.getLogger().info("Loading runtime libraries...");
        
        // SLF4J (required by HikariCP)
        Library slf4jApi = Library.builder()
                .groupId("org{}slf4j".replace("{}", "."))
                .artifactId("slf4j-api")
                .version("2.0.16")
                .build();
        
        Library slf4jSimple = Library.builder()
                .groupId("org{}slf4j".replace("{}", "."))
                .artifactId("slf4j-simple")
                .version("2.0.16")
                .build();
        
        // HikariCP
        Library hikariCP = Library.builder()
                .groupId("com{}zaxxer".replace("{}", "."))
                .artifactId("HikariCP")
                .version("6.3.3")
                .build();
        
        // H2 Database
        Library h2 = Library.builder()
                .groupId("com{}h2database".replace("{}", "."))
                .artifactId("h2")
                .version("2.2.224")
                .build();
        
        // MariaDB for MySQL support
        Library mariadb = Library.builder()
                .groupId("org{}mariadb{}jdbc".replace("{}", "."))
                .artifactId("mariadb-java-client")
                .version("3.5.7")
                .build();
        
        libraryManager.loadLibrary(slf4jApi);
        libraryManager.loadLibrary(slf4jSimple);
        libraryManager.loadLibrary(hikariCP);
        libraryManager.loadLibrary(h2);
        libraryManager.loadLibrary(mariadb);
        
        plugin.getLogger().info("Runtime libraries loaded successfully!");
    }
}
