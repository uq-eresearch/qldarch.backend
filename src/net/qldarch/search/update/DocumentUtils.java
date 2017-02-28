package net.qldarch.search.update;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.util.ObjUtils;

@Slf4j
public class DocumentUtils {

  private static final String ALL = "all";

  public static Document createDocument(Map<String, Object> m) {
    Document doc = new Document();
    StringBuilder all = new StringBuilder();
    for(Map.Entry<String, Object> me : m.entrySet()) {
      boolean store = true;
      String name = me.getKey();
      Object v = me.getValue();
      if(v == null) {
        //log.debug("ignoring field {} (null)", name);
        continue;
      }
      if((StringUtils.equals(ALL, name)) && (v instanceof String)) {
        all.append((String)v);
      } else if(v instanceof Number) {
        if(v instanceof Double || v instanceof Float) {
          final double doubleVal = ((Number)v).doubleValue();
          doc.add(new DoublePoint(name, doubleVal));
          if(store) {
            doc.add(new StoredField(name, doubleVal));
          }
          // add the number with different scales to the all text so it can be found more easily
          // e.g. while searching for lat/lng
          for(int i = 0;i < 10;i++) {
            // this is useful for truncated decimals
            all.append(new BigDecimal(v.toString()).setScale(i, RoundingMode.DOWN).toString());
            all.append(' ');
            // this is useful for rounded decimals
            all.append(new BigDecimal(v.toString()).setScale(i, RoundingMode.HALF_UP).toString());
            all.append(' ');
          }
        } else if (v instanceof Integer) {
          final int intVal = ((Number)v).intValue();
          doc.add(new IntPoint(name, intVal));
          if(store) {
            doc.add(new StoredField(name, intVal));
          }
          all.append(intVal);
        } else {
          final long longVal = ((Number)v).longValue();
          doc.add(new LongPoint(name, longVal));
          if(store) {
            doc.add(new StoredField(name, longVal));
          }
          all.append(longVal);
        }
      } else if(v instanceof String) {
        doc.add(new TextField(name, (String)v, store?Field.Store.YES:Field.Store.NO));
        all.append(v);
      } else if(v instanceof Date) {
        DateTools.Resolution resolution;
        if(v instanceof java.sql.Date) {
          resolution = DateTools.Resolution.DAY;
        } else {
          resolution = DateTools.Resolution.SECOND;
        }
        String d2s = DateTools.timeToString(((Date)v).getTime(), resolution);
        doc.add(new TextField(name, d2s, store?Field.Store.YES:Field.Store.NO));
        all.append(d2s);
      } else if(v instanceof Boolean) {
        doc.add(new StringField(name, v.toString(), store?Field.Store.YES:Field.Store.NO));
      } else if(v instanceof Collection) {
        final String content = ((Collection<?>)v).stream().map(ObjUtils::asString).filter(
            Objects::nonNull).collect(Collectors.joining(", "));
        all.append(content);
        doc.add(new TextField(name, content, store?Field.Store.YES:Field.Store.NO));
      } else {
        log.warn("unknown type for field '{}' type '{}'", name, v.getClass().getName());
        continue;
      }
      all.append(" ");
    }
    doc.add(new TextField(ALL, all.toString(), Field.Store.NO));
    return doc;
  }

}
