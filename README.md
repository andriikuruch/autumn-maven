# How to use it

### Step 1. Add the JitPack repository to your build file

###### For Maven
```xml
<pluginRepositories>
	<pluginRepository>
			<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</pluginRepository>
</pluginRepositories>
```

### Step 2. Add the dependency

###### For Maven
```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.github.andriikuruch.autumn-maven</groupId>
			<artifactId>autumn-maven-plugin</artifactId>
			<version>1.0.2</version> <!-- change it with higher version if you need. see tags here https://github.com/andriikuruch/autumn-maven/tags -->
			<executions>
				<execution>
					<goals>
						<goal>repackage</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```
