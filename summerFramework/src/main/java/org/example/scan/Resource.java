package org.example.scan;

// java8
// 表示文件
import java.util.Objects;

public class Resource {
    private final String path;
    private final String name;

    public Resource(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        Resource resource = (Resource) obj;
        return Objects.equals(path, resource.path) && Objects.equals(name, resource.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path,name);
    }
}
