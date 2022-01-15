package com.nort721.jnichelper;

import com.nort721.jnichelper.utils.AsmUtil;
import com.nort721.jnichelper.utils.Target;
import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.util.*;

public class JnicHelper {

    public static void main(String[] args) throws Exception {

        System.out.println("=+=+= JnicHelper =+=+=" +
                "\nversion: 0.3" +
                "\nsource-code: https://github.com/Nort721/JnicHelper.git");

        File jar = new File(args[0]);

        List<ClassNode> classes = AsmUtil.loadJar(jar);

        List<Target> targetedMethods = new ArrayList<>();
        for (ClassNode clazz : classes) {

            List<MethodNode> annotatedMethods = AsmUtil.getMethodsAnnotated(clazz, "jnic");

            for (MethodNode method : annotatedMethods) {
                System.out.println("processing class - " + clazz.name + " -> processing method: " + method.name);
                targetedMethods.add(new Target(clazz.name, method.name, method.desc));
            }
        }

        boolean mangle = false;
        boolean stringObf = false;
        boolean methodDesc = false;

        // default output destination (in the same folder that the input jar is in)
        String dest = "/" + FilenameUtils.getPath(jar.getPath());

        for (int i = 1; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-m")
                    || args[i].equalsIgnoreCase("-mangle")) {
                mangle = true;
            } else if (args[i].equalsIgnoreCase("-s")
                    || args[i].equalsIgnoreCase("-stringobf")) {
                stringObf = true;
            } else if (args[i].equalsIgnoreCase("-desc")
                    || args[i].equalsIgnoreCase("-d")) {
                methodDesc = true;
            } else if (i == 1 && args[i].length() > 2) {
                // if it's not one of the files we assume it's an output path
                dest = args[i];
            }
        }

        generateJnicConfig(targetedMethods, mangle, stringObf, methodDesc, dest);

        System.out.println("Done!");
    }

    public static void generateJnicConfig(List<Target> annotatedMethods, boolean mangle, boolean stringObf, boolean desc, String dest) throws IOException {

        Element root = new Element("jnic");
        Document doc = new Document();

        Element child1 = new Element("mangle");
        child1.addContent(mangle + "");

        Element child2 = new Element("stringObf");
        child2.addContent(stringObf + "");

        Element child3 = new Element("include");

        for (Target target : annotatedMethods) {
            String className = target.getClassCore();
            String methodName = target.getMethodName();
            String methodDesc = target.getMethodDesc();

            if (desc)
                child3.addContent(new Element("match").setAttribute("className", className)
                        .setAttribute("methodName", methodName).setAttribute("methodDesc", methodDesc));
            else
                child3.addContent(new Element("match").setAttribute("className", className)
                        .setAttribute("methodName", methodName));
        }

        Element child4 = new Element("exclude");

        root.addContent(child1);
        root.addContent(child2);
        root.addContent(child3);
        root.addContent(child4);

        doc.setRootElement(root);

        //Create the XML
        XMLOutputter outter=new XMLOutputter();
        outter.setFormat(Format.getPrettyFormat());
        outter.output(doc, new FileWriter(new File(dest + "/config.xml")));

    }

}
