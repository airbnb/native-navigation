package com.airbnb.android.react.navigation;

import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
final class ConversionUtil {

    private static final String TAG = ConversionUtil.class.getSimpleName();

    private ConversionUtil() {
    }

    static final ReadableMap EMPTY_MAP = new WritableNativeMap();

    static Map<String, Object> toMap(ReadableMap readableMap) {
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        Map<String, Object> result = new HashMap<>();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);
            switch (type) {
                case Null:
                    result.put(key, null);
                    break;
                case Boolean:
                    result.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    try {
                        result.put(key, readableMap.getInt(key));
                    } catch (Exception e) {
                        result.put(key, readableMap.getDouble(key));
                    }
                    break;
                case String:
                    result.put(key, readableMap.getString(key));
                    break;
                case Map:
                    result.put(key, toMap(readableMap.getMap(key)));
                    break;
                case Array:
                    result.put(key, toArray(readableMap.getArray(key)));
                    break;
                default:
                    Log.e(TAG, "Could not convert object with key: " + key + ".");
            }
        }
        return result;
    }

    /**
     * Converts the provided {@code readableMap} into an object of the provided {@code targetType}
     */
    static <T> T toType(ObjectMapper objectMapper, ReadableMap readableMap, Class<T>
            targetType) {
        ObjectNode jsonNode = toJsonObject(readableMap);
        ObjectReader objectReader = JacksonUtils.readerForType(objectMapper, targetType);
        //noinspection OverlyBroadCatchBlock
        try {
            return objectReader.readValue(jsonNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a {@link ReadableMap} into an Json {@link ObjectNode}
     */
    static ObjectNode toJsonObject(ReadableMap readableMap) {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode result = nodeFactory.objectNode();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);
            switch (type) {
                case Null:
                    result.putNull(key);
                    break;
                case Boolean:
                    result.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    result.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    result.put(key, readableMap.getString(key));
                    break;
                case Map:
                    result.set(key, toJsonObject(readableMap.getMap(key)));
                    break;
                case Array:
                    result.set(key, toJsonArray(readableMap.getArray(key)));
                    break;
                default:
                    Log.e(TAG, "Could not convert object with key: " + key + ".");
            }
        }
        return result;
    }

    /**
     * Converts a {@link ReadableArray} into an Json {@link ArrayNode}
     */
    static ArrayNode toJsonArray(ReadableArray readableArray) {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ArrayNode result = nodeFactory.arrayNode();
        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType indexType = readableArray.getType(i);
            switch (indexType) {
                case Null:
                    result.addNull();
                    break;
                case Boolean:
                    result.add(readableArray.getBoolean(i));
                    break;
                case Number:
                    result.add(readableArray.getDouble(i));
                    break;
                case String:
                    result.add(readableArray.getString(i));
                    break;
                case Map:
                    result.add(toJsonObject(readableArray.getMap(i)));
                    break;
                case Array:
                    result.add(toJsonArray(readableArray.getArray(i)));
                    break;
                default:
                    Log.e(TAG, "Could not convert object at index " + i + ".");
            }
        }
        return result;
    }

    static Bundle toBundle(ReadableMap readableMap) {
        Bundle result = new Bundle();
        if (readableMap == null) {
            return result;
        }
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);
            switch (type) {
                case Null:
                    result.putString(key, null);
                    break;
                case Boolean:
                    result.putBoolean(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    try {
                        // NOTE(lmr):
                        // this is a bit of a hack right now to prefer integer types in cases where the
                        // number can be a valid int,
                        // and fall back to doubles in every other case. Long-term we will figure out a
                        // reliable way to add this meta
                        // data.
                        result.putInt(key, readableMap.getInt(key));
                    } catch (Exception e) {
                        result.putDouble(key, readableMap.getDouble(key));
                    }
                    break;
                case String:
                    result.putString(key, readableMap.getString(key));
                    break;
                case Map:
                    result.putBundle(key, toBundle(readableMap.getMap(key)));
                    break;
                case Array:
                    // NOTE(lmr): This is a limitation of the Bundle class. Wonder if there is a clean way
                    // for us
                    // to get around it. For now i'm just skipping...
                    Log.e(TAG, "Cannot put arrays of objects into bundles. Failed on: " + key + ".");
                    break;
                default:
                    Log.e(TAG, "Could not convert object with key: " + key + ".");
            }
        }
        return result;
    }

    static void merge(WritableMap target, ReadableMap map) {
        ReadableMapKeySetIterator iterator = map.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = map.getType(key);
            switch (type) {
                case Null:
                    target.putNull(key);
                    break;
                case Boolean:
                    target.putBoolean(key, map.getBoolean(key));
                    break;
                case Number:
                    try {
                        target.putInt(key, map.getInt(key));
                    } catch (Exception e) {
                        target.putDouble(key, map.getDouble(key));
                    }
                    break;
                case String:
                    target.putString(key, map.getString(key));
                    break;
                case Map:
                    target.putMap(key, cloneMap(map.getMap(key)));
                    break;
                case Array:
                    target.putArray(key, cloneArray(map.getArray(key)));
                    break;
                default:
                    Log.e(TAG, "Could not convert object with key: " + key + ".");
            }
        }
    }

    static WritableMap cloneMap(ReadableMap map) {
        WritableNativeMap target = new WritableNativeMap();
        merge(target, map);
        return target;
    }

    static WritableArray cloneArray(ReadableArray source) {
        WritableNativeArray result = new WritableNativeArray();
        for (int i = 0; i < source.size(); i++) {
            ReadableType indexType = source.getType(i);
            switch (indexType) {
                case Null:
                    result.pushNull();
                    break;
                case Boolean:
                    result.pushBoolean(source.getBoolean(i));
                    break;
                case Number:
                    try {
                        result.pushInt(source.getInt(i));
                    } catch (Exception e) {
                        result.pushDouble(source.getDouble(i));
                    }
                    break;
                case String:
                    result.pushString(source.getString(i));
                    break;
                case Map:
                    result.pushMap(cloneMap(source.getMap(i)));
                    break;
                case Array:
                    result.pushArray(cloneArray(source.getArray(i)));
                    break;
                default:
                    Log.e(TAG, "Could not convert object at index " + i + ".");
            }
        }
        return result;
    }

    static ReadableMap combine(ReadableMap a, ReadableMap b) {
        WritableMap result = new WritableNativeMap();
        merge(result, a);
        merge(result, b);
        return result;
    }

    static Map<String, String> toStringMap(ReadableMap readableMap) {
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        Map<String, String> result = new HashMap<>();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);
            switch (type) {
                case Null:
                    result.put(key, null);
                    break;
                case String:
                    result.put(key, readableMap.getString(key));
                    break;
                default:
                    Log.e(TAG, "Could not convert object with key: " + key + ".");
            }
        }
        return result;
    }

    static Map<String, Double> toDoubleMap(ReadableMap readableMap) {
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        Map<String, Double> result = new HashMap<>();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);
            switch (type) {
                case Number:
                    result.put(key, readableMap.getDouble(key));
                    break;
                default:
                    Log.e(TAG, "Could not convert object with key: " + key + ".");
            }
        }
        return result;
    }

    static Map<String, Integer> toIntegerMap(ReadableMap readableMap) {
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        Map<String, Integer> result = new HashMap<>();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);
            switch (type) {
                case Number:
                    result.put(key, readableMap.getInt(key));
                    break;
                default:
                    Log.e(TAG, "Could not convert object with key: " + key + ".");
            }
        }
        return result;
    }

    static List<Object> toArray(ReadableArray readableArray) {
        List<Object> result = new ArrayList<>(readableArray.size());
        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType indexType = readableArray.getType(i);
            switch (indexType) {
                case Null:
                    result.add(i, null);
                    break;
                case Boolean:
                    result.add(i, readableArray.getBoolean(i));
                    break;
                case Number:
                    result.add(i, readableArray.getDouble(i));
                    break;
                case String:
                    result.add(i, readableArray.getString(i));
                    break;
                case Map:
                    result.add(i, toMap(readableArray.getMap(i)));
                    break;
                case Array:
                    result.add(i, toArray(readableArray.getArray(i)));
                    break;
                default:
                    Log.e(TAG, "Could not convert object at index " + i + ".");
            }
        }
        return result;
    }

    static List<String> toStringArray(ReadableArray readableArray) {
        List<String> result = new ArrayList<>(readableArray.size());
        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType indexType = readableArray.getType(i);
            switch (indexType) {
                case Null:
                    result.add(i, null);
                    break;
                case String:
                    result.add(i, readableArray.getString(i));
                    break;
                default:
                    Log.e(TAG, "Could not convert object at index " + i + ".");
            }
        }
        return result;
    }

    static List<Double> toDoubleArray(ReadableArray readableArray) {
        List<Double> result = new ArrayList<>(readableArray.size());
        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType indexType = readableArray.getType(i);
            switch (indexType) {
                case Number:
                    result.add(i, readableArray.getDouble(i));
                    break;
                default:
                    Log.e(TAG, "Could not convert object at index " + i + ".");
            }
        }
        return result;
    }

    static List<Integer> toIntArray(ReadableArray readableArray) {
        List<Integer> result = new ArrayList<>(readableArray.size());
        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType indexType = readableArray.getType(i);
            switch (indexType) {
                case Number:
                    result.add(i, readableArray.getInt(i));
                    break;
                default:
                    Log.e(TAG, "Could not convert object at index " + i + ".");
            }
        }
        return result;
    }

    static List<Map<String, Object>> toMapArray(ReadableArray readableArray) {
        List<Map<String, Object>> result = new ArrayList<>(readableArray.size());
        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType indexType = readableArray.getType(i);
            switch (indexType) {
                case Map:
                    result.add(i, toMap(readableArray.getMap(i)));
                    break;
                default:
                    Log.e(TAG, "Could not convert object at index " + i + ".");
            }
        }
        return result;
    }

    static WritableMap toWritableMap(Map<String, Object> map) {
        WritableNativeMap result = new WritableNativeMap();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                result.putNull(key);
            } else if (value instanceof Map) {
                //noinspection unchecked,rawtypes
                result.putMap(key, toWritableMap((Map) value));
            } else if (value instanceof List) {
                //noinspection unchecked,rawtypes
                result.putArray(key, toWritableArray((List) value));
            } else if (value instanceof Boolean) {
                result.putBoolean(key, (Boolean) value);
            } else if (value instanceof Integer) {
                result.putInt(key, (Integer) value);
            } else if (value instanceof String) {
                result.putString(key, (String) value);
            } else if (value instanceof Double) {
                result.putDouble(key, (Double) value);
            } else {
                Log.e(TAG, "Could not convert object " + value.toString());
            }
        }

        return result;
    }

    private static WritableArray toWritableArray(List<Object> array) {
        WritableNativeArray result = new WritableNativeArray();

        for (Object value : array) {
            if (value == null) {
                result.pushNull();
            } else if (value instanceof Map) {
                //noinspection unchecked,rawtypes
                result.pushMap(toWritableMap((Map) value));
            } else if (value instanceof List) {
                //noinspection unchecked,rawtypes
                result.pushArray(toWritableArray((List) value));
            } else if (value instanceof Boolean) {
                result.pushBoolean((Boolean) value);
            } else if (value instanceof Integer) {
                result.pushInt((Integer) value);
            } else if (value instanceof String) {
                result.pushString((String) value);
            } else if (value instanceof Double) {
                result.pushDouble((Double) value);
            } else {
                Log.e(TAG, "Could not convert object " + value.toString());
            }
        }

        return result;
    }
}
