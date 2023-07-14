package org.example.scan;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
public class ResourceResolver {
    String basePackage;

    public ResourceResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    public <R>List<R> scan(Function<Resource,R> mapper) throws URISyntaxException, IOException {
        String basePackagePath = this.basePackage.replace(".", "\\");
        String pkgPath = basePackagePath;
        //通过classloader获取资源位置
        URI pkg = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(pkgPath)).toURI();
        List<R> allClasses = new ArrayList<>();

        Path root;
        if (pkg.toString().startsWith("jar:")) {
            try {
                // 传入jar包会从包路径获取路径
                root = FileSystems.getFileSystem(pkg).getPath(pkgPath);
            } catch (final FileSystemNotFoundException e) {
                // 将压缩文件的URI转成对应的Path
                root = FileSystems.newFileSystem(pkg, Collections.emptyMap()).getPath(pkgPath);
            }
        } else {
            // uri转为路径
            root = Paths.get(pkg);
        }

        final String extension = ".class";
        // 一个遍历目录下所有文件的api，传入参数是一个Path，后面的标准写法就是接:isRegularFile
        try (final Stream<Path> allPaths = Files.walk(root)) {
            allPaths.filter(Files::isRegularFile).forEach(file -> {
                try {

                    final String path = file.toString().replace('\\', '.');
                    final String name = path.substring(path.indexOf(this.basePackage), path.length());
                    Resource res = new Resource(path, name);
                    allClasses.add((mapper.apply(res)));
                } catch (final StringIndexOutOfBoundsException ignored) {
                }
            });
        }
        return allClasses;
    }



    public static void main(String[] args) throws URISyntaxException, IOException {
        ResourceResolver resourceResolver = new ResourceResolver("org.example");
        // lambda表达式 定义mapper
        List<String> list = resourceResolver.scan(res -> {
            String name = res.getName();
            if(name.endsWith(".class")){
                return name.substring(0,name.length() - ".class".length()).replace("/",".").replace("\\",".");
            }
            return  null;
        });
        list.forEach(System.out::println);
    }

}
