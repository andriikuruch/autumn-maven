# How to use it

### Step 1. Add the JitPack repository to your build file

###### For Maven
```
<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```
###### For Gradle
```
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```
### Step 2. Add the dependency

###### For Maven
```
	<dependency>
	    <groupId>com.github.andriikuruch</groupId>
	    <artifactId>autumn-maven</artifactId>
	    <version>Tag</version>
	</dependency>
```
###### For Gradle
```
dependencies {
	        implementation 'com.github.andriikuruch:autumn-maven:Tag'
	}
```
