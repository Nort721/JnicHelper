package com.nort721.jnichelper.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@UtilityClass
public class AsmUtil {

    /**
     * Loops through all the methods in the class and returns all the methods
     * that are annotated with the specified annotation
     * @param clazz The class that we are scanning
     * @param annotation The name of the target annotation
     * @return A list of all the methodNodes that are annotated with the specified annotation
     */
    public static List<MethodNode> getMethodsAnnotated(ClassNode clazz, String annotation) {
        List<MethodNode> methods = new ArrayList<>();
        for (MethodNode method : clazz.methods) {
            if (method.visibleAnnotations == null) continue;
            for (AnnotationNode annotationNode : method.visibleAnnotations)
                if (annotationNode.desc.contains(annotation))
                    methods.add(method);
        }
        return methods;
    }

    /**
     * loops through all the entries in a file and returns all the compiled
     * classes in that file
     * @param file The file that we are scanning
     * @return A list of all the classNodes in that file
     */
    public static List<ClassNode> loadJar(File file) {
        List<ClassNode> classes = new ArrayList<>();
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();

                try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
                    byte[] bytes = IOUtils.toByteArray(inputStream);

                    if (jarEntry.getName().endsWith(".class")) {
                        ClassNode classNode = new ClassNode();
                        ClassReader classReader = new ClassReader(bytes);

                        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
                        classes.add(classNode);
                    }

                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return classes;
    }

}
