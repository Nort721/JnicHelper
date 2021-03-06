package org.objectweb.asm.optimizer;

import org.objectweb.asm.*;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * A Jar file optimizer.
 * 
 * @author Eric Bruneton
 */
public class JarOptimizer {

    static final Set<String> API = new HashSet<String>();
    static final Map<String, String> HIERARCHY = new HashMap<String, String>();
    static boolean nodebug = false;

    public static void main(final String[] args) throws IOException {
        File f = new File(args[0]);
        InputStream is = new GZIPInputStream(new FileInputStream(f));
        BufferedReader lnr = new LineNumberReader(new InputStreamReader(is));
        while (true) {
            String line = lnr.readLine();
            if (line != null) {
                if (line.startsWith("class")) {
                    String c = line.substring(6, line.lastIndexOf(' '));
                    String sc = line.substring(line.lastIndexOf(' ') + 1);
                    HIERARCHY.put(c, sc);
                } else {
                    API.add(line);
                }
            } else {
                break;
            }
        }
        lnr.close();
        int argIndex = 1;
        if (args[argIndex].equals("-nodebug")) {
            nodebug = true;
            argIndex++;
        }

        optimize(new File(args[argIndex]));
    }

    public static void optimize(final File f) throws IOException {
        if (nodebug && f.getName().contains("debug")) {
            return;
        }

        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; ++i) {
                optimize(files[i]);
            }
        } else if (f.getName().endsWith(".jar")) {
            File g = new File(f.getParentFile(), f.getName() + ".new");
            ZipFile zf = new ZipFile(f);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(g));
            Enumeration<? extends ZipEntry> e = zf.entries();
            byte[] buf = new byte[10000];
            while (e.hasMoreElements()) {
                ZipEntry ze = e.nextElement();
                if (ze.isDirectory()) {
                    out.putNextEntry(ze);
                    continue;
                }
                out.putNextEntry(ze);
                if (ze.getName().endsWith(".class")) {
                    ClassReader cr = new ClassReader(zf.getInputStream(ze));
                    // cr.accept(new ClassDump(), 0);
                    cr.accept(new ClassVerifier(), 0);
                }
                InputStream is = zf.getInputStream(ze);
                int n;
                do {
                    n = is.read(buf, 0, buf.length);
                    if (n != -1) {
                        out.write(buf, 0, n);
                    }
                } while (n != -1);
                out.closeEntry();
            }
            out.close();
            zf.close();
            if (!f.delete()) {
                throw new IOException("Cannot delete file " + f);
            }
            if (!g.renameTo(f)) {
                throw new IOException("Cannot rename file " + g);
            }
        }
    }

    static class ClassDump extends ClassVisitor {

        String owner;

        public ClassDump() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visit(final int version, final int access,
                final String name, final String signature,
                final String superName, final String[] interfaces) {
            owner = name;
            if (owner.startsWith("java/")) {
                System.out.println("class " + name + ' ' + superName);
            }
        }

        @Override
        public FieldVisitor visitField(final int access, final String name,
                final String desc, final String signature, final Object value) {
            if (owner.startsWith("java/")) {
                System.out.println(owner + ' ' + name);
            }
            return null;
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name,
                final String desc, final String signature,
                final String[] exceptions) {
            if (owner.startsWith("java/")) {
                System.out.println(owner + ' ' + name + desc);
            }
            return null;
        }
    }

    static class ClassVerifier extends ClassVisitor {

        String owner;

        String method;

        public ClassVerifier() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visit(final int version, final int access,
                final String name, final String signature,
                final String superName, final String[] interfaces) {
            owner = name;
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name,
                final String desc, final String signature,
                final String[] exceptions) {
            method = name + desc;
            return new MethodVisitor(Opcodes.ASM5) {
                @Override
                public void visitFieldInsn(final int opcode,
                        final String owner, final String name, final String desc) {
                    check(owner, name);
                }

                @Override
                public void visitMethodInsn(final int opcode,
                        final String owner, final String name,
                        final String desc, final boolean itf) {
                    check(owner, name + desc);
                }
            };
        }

        void check(String owner, String member) {
            if (owner.startsWith("java/")) {
                String o = owner;
                while (o != null) {
                    if (API.contains(o + ' ' + member)) {
                        return;
                    }
                    o = HIERARCHY.get(o);
                }
            }
        }
    }
}
