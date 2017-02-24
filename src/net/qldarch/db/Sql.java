package net.qldarch.db;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.resolver.ClasspathResolver;

public class Sql {

  private String path;

  public Sql(Object o) {
    this(o.getClass());
  }

  public Sql(Object o, String suffix) {
    this(o.getClass(), suffix);
  }

  public Sql(Class<?> cls) {
    this(cls, null);
  }

  public Sql(Class<?> cls, String suffix) {
    this(String.format("%s%s.sql", StringUtils.replaceChars(cls.getName(), ".", "/"),
        (suffix==null?"":suffix)));
  }

  private Sql(Package pkg, String name) {
    this(String.format("%s/%s.sql", StringUtils.replaceChars(pkg.getName(),  ".",  "/"), name));
  }

  public Sql(String path) {
    this.path = path;
  }

  public String prepare() {
    return prepare(null);
  }

  public String prepare(Map<String, Object> params) {
    try {
      MustacheFactory mf = new DefaultMustacheFactory(new ClasspathResolver());
      Mustache mustache = mf.compile(path);
      Writer writer = new StringWriter();
      mustache.execute(writer, params);
      return writer.toString();
    } catch(Exception e) {
      throw new RuntimeException(String.format("failed to prepare sql statement %s with params %s",
          path, params),e);
    }
  }

  public static String fromPkg(Object o, String name) {
    return new Sql(o.getClass().getPackage(), name).prepare();
  }

  public static String fromPkg(Object o, String name, Map<String, Object> params) {
    return new Sql(o.getClass().getPackage(), name).prepare(params);
  }

}
