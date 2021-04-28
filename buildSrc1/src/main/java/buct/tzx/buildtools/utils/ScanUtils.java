package buct.tzx.buildtools.utils;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.Spring;

import buct.tzx.buildtools.AutoServiceTransform;

public class ScanUtils {
    public static boolean shouldProcessPreDexJar(String path) {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository");
    }

    public static boolean shouldProcessClass(String entryName) {
        //System.out.println("判断"+entryName);
        return entryName != null && entryName.startsWith(ScanConsts.ROUTER_CLASS_PACKAGE_NAME);
    }
    /**
     * scan class file
     */
    public static void scanClass(File file) {
        try {
            scanClass(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void scanClass(InputStream inputStream) {
        try {
            ClassReader cr = new ClassReader(inputStream);
            ClassWriter cw = new ClassWriter(cr, 0);
            ScanClassVisitor cv = new ScanClassVisitor(Opcodes.ASM5, cw);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void insertCode(File file) {
        try {
            System.out.println("");
            FileInputStream stream =new FileInputStream(file);
            ClassReader cr = new ClassReader(stream);
            ClassWriter cw = new ClassWriter(cr, 0);
            MyClassVisitor cv = new MyClassVisitor(Opcodes.ASM5, cw);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            FileOutputStream stream1 = new FileOutputStream(file.getParentFile().getAbsolutePath() + File.separator + file.getName());
            stream1.write(cw.toByteArray());
            stream1.close();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static class ScanClassVisitor extends ClassVisitor {

        ScanClassVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }

        public void visit(int version, int access, String name, String signature,
                   String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            if(AutoServiceTransform.Companion.getScanConsts().interfaceName!=null&&AutoServiceTransform.Companion.getScanConsts().interfaceName.length()>0){
                for(String intf:interfaces){
                    //System.out.println("比对"+intf +" "+AutoServiceTransform.Companion.getScanConsts().interfaceName);
                    if(intf.equals(AutoServiceTransform.Companion.getScanConsts().interfaceName)){
                        if(!AutoServiceTransform.Companion.getScanConsts().classList.contains(intf)){
                            AutoServiceTransform.Companion.getScanConsts().classList.add(name);
                        }
                    }
                }
            }
        }
    }
    static class MyClassVisitor extends ClassVisitor {

        MyClassVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }

        public void visit(int version, int access, String name, String signature,
                   String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                  String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            //generate code into this method
            System.out.println(name);
            if (name.equals(ScanConsts.GENERATE_TO_METHOD_NAME)) {
                mv = new RouteMethodVisitor(Opcodes.ASM5, mv);
            }
            return mv;
        }
    }

    static class RouteMethodVisitor extends MethodVisitor {

        RouteMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            //generate code before return

            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                System.out.println("开始注入");
                for(String name:AutoServiceTransform.Companion.getScanConsts().classList){
                    name = name.replaceAll("/", ".");
                    System.out.println(name);
                    mv.visitLdcInsn(name);//类名
                    // generate invoke register method into LogisticsCenter.loadRouterMap()
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC
                            , ScanConsts.GENERATE_TO_CLASS_NAME
                            , ScanConsts.REGISTER_METHOD_NAME
                            , "(Ljava/lang/String;)V"
                            , false);
                }
            }
            super.visitInsn(opcode);
        }
        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 4, maxLocals);
        }
    }
    public static void scanJar(File jarFile, File destFile) {
        if (jarFile!=null) {
            JarFile file = null;
            try {
                file = new JarFile(jarFile);
                Enumeration enumeration = file.entries();
                while (enumeration.hasMoreElements()) {
                    JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                    String entryName = jarEntry.getName();
                    System.out.println(entryName);
                    if (entryName.startsWith(ScanConsts.ROUTER_CLASS_PACKAGE_NAME)) {
                        System.out.println("jar文件里找到了file"+entryName);
                        InputStream inputStream = file.getInputStream(jarEntry);
                        scanClass(inputStream);
                        inputStream.close();
                    } else if (ScanConsts.GENERATE_TO_CLASS_FILE_NAME.equals(entryName)) {
                        System.out.println("jar文件里找到了注册中心"+entryName);
                        // mark this jar file contains LogisticsCenter.class
                        // After the scan is complete, we will generate register code into this file
                        AutoServiceTransform.Companion.setTargetFile(destFile);
                    }
                }
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void insertInitCodeTo() {
        if (!AutoServiceTransform.Companion.getScanConsts().classList.isEmpty()) {
            File file = AutoServiceTransform.Companion.getTargetFile();
            if (file.getName().endsWith(".jar")){
                System.out.println("开始对jar注入字节码");
                insertInitCodeIntoJarFile(file);
            }

        }
    }

    /**
     * generate code into jar file
     * @param jarFile the jar file which contains LogisticsCenter.class
     * @return
     */
    private static File insertInitCodeIntoJarFile(File jarFile) {
        if (jarFile !=null) {
            File optJar = new File(jarFile.getParent(), jarFile.getName() + ".opt");
            if (optJar.exists())
                optJar.delete();
            JarFile file = null;
            try {
                file = new JarFile(jarFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Enumeration enumeration = file.entries();
            JarOutputStream jarOutputStream = null;
            try {
                jarOutputStream = new JarOutputStream(new FileOutputStream(optJar));
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                String entryName = jarEntry.getName();
                ZipEntry zipEntry = new ZipEntry(entryName);
                InputStream inputStream = null;
                try {
                    inputStream = file.getInputStream(jarEntry);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    jarOutputStream.putNextEntry(zipEntry);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (ScanConsts.GENERATE_TO_CLASS_FILE_NAME.equals(entryName)) {

                    System.out.println("('Insert init code to class >> '" + entryName);

                    try {
                        byte[]  bytes = referHackWhenInit(inputStream);
                        jarOutputStream.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        jarOutputStream.write(IOUtils.toByteArray(inputStream));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    inputStream.close();
                    jarOutputStream.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                jarOutputStream.close();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (jarFile.exists()) {
                jarFile.delete();
            }
            optJar.renameTo(jarFile);
        }
        return jarFile;
    }
    //refer hack class when object init
    private static byte[] referHackWhenInit(InputStream inputStream) throws IOException {
        ClassReader cr = new ClassReader(inputStream);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new MyClassVisitor(Opcodes.ASM5, cw);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }
}
