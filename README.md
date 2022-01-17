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
and run it with the command  
```java -jar JnicHelper.jar myJar.jar"```

you also can specify output folder path
```java -jar JnicHelper.jar myJar.jar OutputFolderPath"```  
  
arguments:  
```-mangle``` or ```-m``` - to set the mangle option in the generated config to true  
```-stringobf``` or ```-s``` - to set the stringobf option in the generated config to true  
```-desc``` or ```-d``` - to make jnic also generate the methods description  
  
recommended settings:  
```java -jar JnicHelper.jar MyJar.jar -s ```  
After running the command JnicHelper will generate a config.xml file
that will protect all the methods that are annotated with the @jnic annotation

### Note: if you use another java obfuscator before obfuscating with jnic make sure to exclude the @jnic interface from being name obfuscated
