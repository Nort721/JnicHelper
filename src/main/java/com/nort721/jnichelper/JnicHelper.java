package com.nort721.jnichelper;

import com.nort721.jnichelper.utils.AsmUtil;
import com.nort721.jnichelper.utils.Target;
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
                "\nversion: 1.0" +
                "\nwebsite: github link here");


        System.out.print("input jar path -> ");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String jarPath = reader.readLine();



        System.out.print("jnic config setting -> mangle (true/false/recommended) -> ");

        String mangleInput = reader.readLine();

        boolean mangle;

        if (mangleInput.equalsIgnoreCase("recommended"))
            mangle = false;
        else mangle = Boolean.parseBoolean(mangleInput);



        System.out.print("jnic config setting -> stringObf (true/false/recommended) -> ");

        String stringObfInput = reader.readLine();

        boolean stringObf;

        if (stringObfInput.equalsIgnoreCase("recommended"))
            stringObf = true;
        else stringObf = Boolean.parseBoolean(stringObfInput);


        File jar = new File(jarPath);

        List<ClassNode> classes = AsmUtil.loadJar(jar);

        List<Target> targetedMethods = new ArrayList<>();
        for (ClassNode clazz : classes) {

            List<MethodNode> annotatedMethods = AsmUtil.getMethodsAnnotated(clazz, "jnic");

            for (MethodNode method : annotatedMethods) {
                System.out.println("processing class - " + clazz.name + " -> processing method: " + method.name);
                targetedMethods.add(new Target(clazz.name, method.name, method.desc));
            }
        }

        generateJnicConfig(targetedMethods, mangle, stringObf);
    }

    public static void generateJnicConfig(List<Target> annotatedMethods, boolean mangle, boolean stringObf) throws IOException {

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

            child3.addContent(new Element("match").setAttribute("className", className)
                    .setAttribute("methodName", methodName).setAttribute("methodDesc", methodDesc));
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
        outter.output(doc, new FileWriter(new File("/Users/nort/Desktop/config.xml")));

    }

}
