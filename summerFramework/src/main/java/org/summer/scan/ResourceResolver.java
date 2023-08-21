package org.summer.scan;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

public class ResourceResolver {
    String basePackage;

    public ResourceResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    public <R>List<R> scan(Function<Resource,R> mapper)  {
        String basePackagePath = this.basePackage.replace(".", "\\");
        String pkgPath = basePackagePath;
        try {
            List<R> collector = new ArrayList<>();
            scan0(basePackagePath, pkgPath,collector, mapper);
            return collector;
        } catch (IOException e){
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    <R> void scan0(String basePackagePath, String path, List<R> collector, Function<Resource,R> mapper) throws IOException, URISyntaxException{
        Enumeration<URL> en = getContextClassLoader().getResources(path);
        while (en.hasMoreElements()) {
            URL url = en.nextElement();
            URI uri = url.toURI();
            String uriStr = removeTrailingSlash(uriToString(uri));
            String uriBaseStr = uriStr.substring(0,uriStr.length() - basePackagePath.length());
            if (uriBaseStr.startsWith("file:")) {
                uriBaseStr = uriBaseStr.substring(5);
            }
            if (uriStr.startsWith("jar:")) {
                scanFile(true,uriBaseStr, jarUriToPath(basePackagePath, uri),collector, mapper);
            } else {
                scanFile(false,uriBaseStr, Paths.get(uri), collector, mapper);
            }
        }
    }

    <R> void scanFile(boolean isJar, String base, Path root, List<R> collector, Function<Resource, R> mapper) throws IOException {
        String baseDir = removeTrailingSlash(base);
        Files.walk(root).filter(Files::isRegularFile).forEach( file -> {
            Resource res = null;
            if (isJar) {
                res = new Resource(baseDir, removeLeadingSlash(file.toString()));
            } else {
                String path = file.toString();
                String name = removeLeadingSlash(path.substring(baseDir.length()));
                res = new Resource("file:" + path, name);
            }
            R r = mapper.apply(res);
            if (r != null){
                collector.add(r);
            }
        });
    }
    ClassLoader getContextClassLoader(){
        ClassLoader cl = null;
        cl = Thread.currentThread().getContextClassLoader();
        if (cl == null){
            cl = getClass().getClassLoader();
        }
        return cl;
    }
    String uriToString(URI uri) throws UnsupportedEncodingException {
        return URLDecoder.decode(uri.toString(), String.valueOf(StandardCharsets.UTF_8));
    }

    Path jarUriToPath(String basePackagePath, URI jarUri) throws IOException {
        return FileSystems.newFileSystem(jarUri, Collections.emptyMap()).getPath(basePackagePath);
    }
    String removeLeadingSlash(String s) {
        if (s.startsWith("/") || s.startsWith("\\")) {
            s = s.substring(1);
        }
        return s;
    }
    String removeTrailingSlash(String s) {
        if (s.endsWith("/") || s.endsWith("\\")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
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
