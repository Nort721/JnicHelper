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

    public List<MethodNode> getMethodsAnnotated(List<ClassNode> classes, String annotation) {
        List<MethodNode> methods = new ArrayList<>();
        for (ClassNode clazz : classes)
            for (MethodNode method : clazz.methods)
                if (method.desc.contains(annotation))
                    methods.add(method);
        return methods;
    }

    public List<MethodNode> getMethodsAnnotated(ClassNode clazz, String annotation) {
        List<MethodNode> methods = new ArrayList<>();
        for (MethodNode method : clazz.methods) {
            if (method.visibleAnnotations == null) continue;
            for (AnnotationNode annotationNode : method.visibleAnnotations)
                if (annotationNode.desc.contains(annotation))
                    methods.add(method);
        }
        return methods;
    }

    public List<ClassNode> loadJar(File file) {
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
