package com.hai046.builder;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.WriterBasedJsonGenerator;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.TextBuffer;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase;
import com.google.common.collect.HashMultimap;
import com.hai046.builder.annotations.JsonUUID;
import com.hai046.builder.annotations.ViewField;
import com.hai046.builder.utils.UuidUtils;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author hai046
 * date 3/22/21
 */
public class ViewBuilderObjectMapper extends ObjectMapper {
    public ViewBuilderObjectMapper(final ViewBuilder viewBuilder) {
        super();

        // 这个是实现 @JsonUUID 序列化
        final ToStringSerializerBase uuidToStringSerializer = new ToStringSerializerBase(Number.class) {
            @Override
            public String valueToString(Object value) {
                if (value instanceof Number) {
                    return UuidUtils.convertIdToUUid((Number) value);
                }
                return value.toString();
            }
        };

        // 这个是实现 @JsonUUID 反序列化
        final StdScalarDeserializer<Object> uuidstdScalarDeserializer = new StdScalarDeserializer<Object>(Object.class) {

            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                JsonToken t = p.getCurrentToken();
                if (t == JsonToken.VALUE_NUMBER_INT) {
                    return p.getNumberValue();
                } else if (t == JsonToken.VALUE_STRING) {
                    return UuidUtils.convertUuidToId(p.getText());
                }
                throw new RuntimeException("不支持 " + t);
            }
        };
        setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public Object findSerializer(Annotated a) {

//                final AnnotatedElement annotated = a.getAnnotated();
                // 后续可以扩展通过  Annotation 查找所有map实现，暂时不用
                if (a.hasAnnotation(JsonUUID.class)) {
                    return uuidToStringSerializer;
                }
                final ViewField annotation = a.getAnnotation(ViewField.class);
                if (annotation != null) {
                    return getSerializerBy(annotation, viewBuilder);
                }
                return super.findSerializer(a);
            }

            @Override
            public Object findDeserializer(Annotated a) {
                if (a.hasAnnotation(JsonUUID.class)) {
                    return uuidstdScalarDeserializer;
                }
                return super.findDeserializer(a);
            }
        });
    }


    private Map<Annotation, Object> cacheSerializerMap = new ConcurrentHashMap<>();

    private Object getSerializerBy(ViewField annotation, ViewBuilder viewBuilder) {
        return cacheSerializerMap.computeIfAbsent(annotation, n -> new JsonSerializer<Object>() {
            @Override
            public final void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                Collection<Object> ids;
                boolean isArrays = false;
                if (value instanceof Collection) {
                    ids = (Collection<Object>) value;
                    isArrays = true;
                } else {
                    ids = Collections.singleton(value);
                }
                if (jgen instanceof ViewBuilderWriterBasedJsonGenerator) {
                    ViewBuilderWriterBasedJsonGenerator jsonGenerator = (ViewBuilderWriterBasedJsonGenerator) jgen;
                    if (jsonGenerator.isPreview) {
                        if (value == null) {
                            return;
                        }
                        final ViewBuilderFetchEntryByIds viewBuilderFetchEntryByIds = viewBuilder.getViewBuilder(annotation.value());
                        ids.forEach(id -> jsonGenerator.setViewBuilder(viewBuilderFetchEntryByIds, id));
                        writeValue(jgen, value);
                    } else {
                        final ViewBuilderFetchEntryByIds viewBuilderFetchEntryByIds = viewBuilder.getViewBuilder(annotation.value());

                        try {
                            if (isArrays) {
                                writeValue(jgen, ids.stream().map(id -> jsonGenerator.getEntry(viewBuilderFetchEntryByIds, id)).collect(Collectors.toList()));
                            } else {
                                final Object entry = jsonGenerator.getEntry(viewBuilderFetchEntryByIds, value);
                                writeValue(jgen, entry);
                            }
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    final ViewBuilderFetchEntryByIds viewBuilderFetchEntryByIds = viewBuilder.getViewBuilder(annotation.value());
                    try {
                        final Map entries = viewBuilderFetchEntryByIds.getEntries(ids);
                        if (isArrays) {
                            writeValue(jgen, ids.stream().map(id -> entries.get(id)).collect(Collectors.toList()));
                        } else {
                            writeValue(jgen, entries.get(value));
                        }
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 获取json，通过多级id合并来做
     *
     * @param object
     * @return
     * @throws IOException
     */
    public String writeValueAsStringByMerge(Object object) throws IOException {

        ViewBuilderSegmentedStringWriter sw = new ViewBuilderSegmentedStringWriter(_jsonFactory._getBufferRecycler());

        final IOContext ioContext = new IOContext(_jsonFactory._getBufferRecycler(), sw, false);
        ViewBuilderWriterBasedJsonGenerator viewBuilderWriterBasedJsonGenerator = new ViewBuilderWriterBasedJsonGenerator(ioContext, JsonGenerator.Feature.collectDefaults(), this, sw, JsonFactory.DEFAULT_QUOTE_CHAR);
        writeValue(viewBuilderWriterBasedJsonGenerator, object);

        //提取zhi值
        viewBuilderWriterBasedJsonGenerator.merge();
        try {
            _writeValueAndClose(viewBuilderWriterBasedJsonGenerator, object);
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e) { // shouldn't really happen, but is declared as possibility so:
            throw JsonMappingException.fromUnexpectedIOE(e);
        }
        return sw.getAndClear();
    }

    static class ViewBuilderWriterBasedJsonGenerator extends WriterBasedJsonGenerator {
        boolean isPreview = true;
        private HashMultimap<ViewBuilderFetchEntryByIds, Object> multimap = HashMultimap.create();
        private Map<Object, Map<Object, Object>> cacheEntryes = new HashMap<>();

        public ViewBuilderWriterBasedJsonGenerator(IOContext ctxt, int features, ObjectCodec codec, ViewBuilderSegmentedStringWriter w, char quoteChar) {
            super(ctxt, features, codec, w, quoteChar);
        }

        public void merge() {
            ((ViewBuilderSegmentedStringWriter) _writer).setStarted(true);
            isPreview = false;
            multimap.keySet().forEach((builder) -> {
                final Map entries = builder.getEntries(multimap.get(builder));
                cacheEntryes.computeIfAbsent(builder, n -> new HashMap<>()).putAll(entries);
            });
        }

        public Object getEntry(ViewBuilderFetchEntryByIds viewBuilderFetchEntryByIds, Object id) {
            return cacheEntryes.get(viewBuilderFetchEntryByIds).get(id);
        }

        public void setViewBuilder(ViewBuilderFetchEntryByIds viewBuilderFetchEntryByIds, Object id) {
            multimap.put(viewBuilderFetchEntryByIds, id);
        }
    }

    static class ViewBuilderSegmentedStringWriter extends Writer {
        final private TextBuffer _buffer;
        private boolean started = false;

        public ViewBuilderSegmentedStringWriter(BufferRecycler br) {
            super();
            _buffer = new TextBuffer(br);
        }

        public void setStarted(boolean started) {
            this.started = started;
        }

        @Override
        public Writer append(char c) {
            if (!started) {
                return this;
            }
            write(c);
            return this;
        }

        @Override
        public Writer append(CharSequence csq) {
            if (!started) {
                return this;
            }
            String str = csq.toString();
            _buffer.append(str, 0, str.length());
            return this;
        }

        @Override
        public Writer append(CharSequence csq, int start, int end) {
            if (!started) {
                return this;
            }
            String str = csq.subSequence(start, end).toString();
            _buffer.append(str, 0, str.length());
            return this;
        }

        @Override
        public void close() {
        } // NOP

        @Override
        public void flush() {
        } // NOP

        @Override
        public void write(char[] cbuf) {
            if (!started) {
                return;
            }
            _buffer.append(cbuf, 0, cbuf.length);
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
            if (!started) {
                return;
            }
            _buffer.append(cbuf, off, len);
        }

        @Override
        public void write(int c) {
            if (!started) {
                return;
            }
            _buffer.append((char) c);
        }

        @Override
        public void write(String str) {
            if (!started) {
                return;
            }
            _buffer.append(str, 0, str.length());
        }

        @Override
        public void write(String str, int off, int len) {
            if (!started) {
                return;
            }
            _buffer.append(str, off, len);
        }

        /**
         * Main access method that will construct a String that contains
         * all the contents, release all internal buffers we may have,
         * and return result String.
         * Note that the method is not idempotent -- if called second time,
         * will just return an empty String.
         *
         * @return String that contains all aggregated content
         */
        public String getAndClear() {
            String result = _buffer.contentsAsString();
            _buffer.releaseBuffers();
            return result;
        }
    }


}
