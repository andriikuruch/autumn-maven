package org.anku.autumn.utils;

import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.sisu.space.asm.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class StartClassProvider {
    private static final Type STRING_ARRAY = Type.getType(String[].class);
    private static final String MAIN = "main";

    private StartClassProvider() {  }

    public static String provide(Path classes) throws MojoExecutionException {
        try (Stream<Path> walk = Files.walk(classes)) {
            final List<Path> mainClasses = new ArrayList<>();
            walk.forEach(file -> {
                if (Files.isRegularFile(file) && file.getFileName().toString().endsWith(".class")) {
                    try {
                        byte[] bytes = Files.readAllBytes(file);
                        ClassReader classReader = new ClassReader(bytes);
                        classReader.accept(new ClassVisitor(Opcodes.ASM9) {


                            @Override
                            @SuppressWarnings("ConstantValue")
                            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                                if (MAIN.equals(name)
                                        && (access & Opcodes.ACC_PUBLIC & Opcodes.ACC_STATIC) == 0
                                        && Type.getReturnType(descriptor).equals(Type.VOID_TYPE)
                                        && Arrays.stream(Type.getArgumentTypes(descriptor)).allMatch(STRING_ARRAY::equals)) {
                                    mainClasses.add(file);
                                }
                                return super.visitMethod(access, name, descriptor, signature, exceptions);
                            }
                        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            if (mainClasses.isEmpty()) {
                throw new MojoExecutionException("Main class was not defined");
            }

            if (mainClasses.size() > 1) {
                throw new MojoExecutionException("Unable to find a single main class from the following candidates " + Arrays.toString(mainClasses.toArray()));
            }

            Path mainClass = classes.relativize(mainClasses.getFirst());
            return mainClass.toString()
                    .replace("/", ".")
                    .replace("\\", ".")
                    .replace(".class", "");
        } catch (IOException | RuntimeException e) {
            throw new MojoExecutionException(e);
        }
    }
}
