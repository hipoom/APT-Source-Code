package me.moolv.annotation.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.sun.tools.javac.code.Type.ClassType;

import me.moolv.annotation.DemoAnnotation;

@AutoService(Processor.class)
public class DemoProcessor extends AbstractProcessor {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    /**
     * 用于将创建的类写入到文件
     */
    private Filer mFiler;


    /* ======================================================= */
    /* Override/Implements Methods                             */
    /* ======================================================= */

    @Override
    public synchronized void init(ProcessingEnvironment environment) {
        super.init(environment);
        mFiler = environment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment environment) {

        // 获取所有被 @DemoAnnotation 注解的类
        Set<? extends Element> elements = environment.getElementsAnnotatedWith(DemoAnnotation.class);

        // 创建一个方法，返回 List<Class>
        MethodSpec method = createMethodWithElements(elements);

        // 创建一个类
        TypeSpec clazz = createClassWithMethod(method);

        // 将这个类写入文件
        writeClassToFile(clazz);

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(DemoAnnotation.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    /**
     * 创建一个方法，这个方法返回参数中的所有类信息。
     */
    private MethodSpec createMethodWithElements(Set<? extends Element> elements) {

        // getAllClasses 是生成的方法的名称
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getAllClasses");

        // public static
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        // 定义返回值类型为 Set<Class>
        ParameterizedTypeName returnType = ParameterizedTypeName.get(
                ClassName.get(Set.class),
                ClassName.get(Class.class)
        );
        builder.returns(returnType);

        // 经过上面的步骤，
        // 我们得到了 public static Set<Class> getAllClasses() {} 这个方法,
        // 接下来我们实现它的方法体：

        // 方法中的第一行: Set<Class> set = new HashSet<>();
        builder.addStatement("$T<$T> set = new $T<>()", Set.class, Class.class, HashSet.class);

        // 遍历 elements, 添加代码行
        for (Element element : elements) {

            // 因为 @Annotation 只能添加在类上，所以这里直接强转为 ClassType
            ClassType type = (ClassType) element.asType();

            // 在我们创建的方法中，新增一行代码： set.add(XXX.class);
            builder.addStatement("set.add($T.class)", type);
        }

        // 经过上面的 for 循环，我们就把所有添加了注解的类加入到 set 变量中了，
        // 最后，只需要把这个 set 作为返回值 return 就好了：
        builder.addStatement("return set");

        return builder.build();
    }

    /**
     * 创建一个类，并把参数中的方法加入到这个类中
     */
    private TypeSpec createClassWithMethod(MethodSpec method) {
        // 定义一个名字叫 OurClass 的类
        TypeSpec.Builder ourClass = TypeSpec.classBuilder("OurClass");

        // 声明为 public 的
        ourClass.addModifiers(Modifier.PUBLIC);

        // 为这个类加入一段注释
        ourClass.addJavadoc("这个类是自动创建的哦~\n\n@author ZhengHaiPeng\n");

        // 为这个类新增一个指定的方法
        ourClass.addMethod(method);

        return ourClass.build();
    }

    /**
     * 将一个创建好的类写入到文件中参与编译
     */
    private void writeClassToFile(TypeSpec clazz) {
        // 声明一个文件在 "me.moolv.apt" 下
        JavaFile file = JavaFile.builder("me.moolv.apt", clazz).build();

        // 写入文件
        try {
            file.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
