# AlpsLib

## Modules
To use the latest version of AlpsLib, you need to add the following dependencies to your pom.xml. You can find a list of all dependencies here:
</br>https://mvn.alps-bte.com/#browse/browse:alps-lib

Repository
```xml
<repositories>
    <repository>
        <id>alpsbte-repo</id>
        <url>https://mvn.alps-bte.com/repository/alps-bte/</url>
    </repository>
</repositories>
```

Replace ```latest``` with the version of the module you want to use. You can find a list of all versions by clicking the link above.

### AlpsLib-Hologram
Includes an abstract HolographicDisplay which can be used to create custom holograms.
```xml
<repositories>
    <!-- HolographicDisplays -->
    <repository>
        <id>codemc-repo</id>
        <url>https://repo.codemc.io/repository/maven-public/</url>
    </repository>
</repositories>
```
```xml
<dependencies>
    <dependency>
        <groupId>com.alpsbte.alpslib</groupId>
        <artifactId>alpslib-hologram</artifactId>
        <version>latest</version>
        <scope>compile</scope>
    </dependency>
    
    <!-- HolographicDisplays -->
    <dependency>
        <groupId>me.filoghost.holographicdisplays</groupId>
        <artifactId>holographicdisplays-api</artifactId>
        <version>3.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### AlpsLib-IO
Includes Config-Manager, Language-Manager and FTP-Manager.
```xml
<dependencies>
    <dependency>
        <groupId>com.alpsbte.alpslib</groupId>
        <artifactId>alpslib-io</artifactId>
        <version>latest</version>
    </dependency>
</dependencies>
```

### AlpsLib-Utils
Includes ItemBuilder & LoreBuilder, CustomHeads and other useful utilities.
```Important: This module is already included in the AlpsLib-IO module.```
```xml
<dependencies>
    <dependency>
        <groupId>com.alpsbte.alpslib</groupId>
        <artifactId>alpslib-utils</artifactId>
        <version>latest</version>
    </dependency>
</dependencies>
```