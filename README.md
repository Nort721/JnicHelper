# JnicHelper
An annotation based config generator for the jnic java obfuscator

JNIC: https://jnic.dev/

# Usage

In the project that you plan on protecting with jnic
implement the following interface
```java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface jnic {}
```


Then proceed to annotate every method that you want to protect
in your project with the @jnic annotation, like so:
```java
    @jnic
    public void logger(String msg) {
        System.out.println(msg);
    }
```

## Running JnicHelper
When you want to protect your jar, download the latest release of JnicHelper from the releases tab
and run it with the
```java -jar JnicHelper.jar myJar.jar"```
arguments:
```-mangle``` or ```-m``` - to set the mangle option in the generated config to true
```-stringobf``` or ```-s``` - to set the stringobf option in the generated config to true
recommended settings:
```java -jar JnicHelper.jar -s ```
After running the command JnicHelper wilp generate a config.xml file
that will protect all the methods that are annotated with the @jnic annotation
