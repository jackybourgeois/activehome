package org.activehome.com.helper;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.kevoree.log.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * This class facilitate transition from/to Json.
 *
 * @author Jacky Bourgeois
 * @version %I%, %G%
 */
public final class JsonHelper {

    /**
     * Utility class.
     */
    private JsonHelper() {
    }

    /**
     * Convert an object into a Json.
     *
     * @param obj The object to convert
     * @return The converted object
     */
    public static JsonValue objectToJson(final Object obj) {
        if (obj == null) {
            return JsonValue.NULL;
        }
        if (obj instanceof Boolean) {
            return JsonValue.valueOf((boolean) obj);
        }
        if (obj instanceof Integer) {
            return JsonValue.valueOf((int) obj);
        }
        if (obj instanceof Double) {
            return JsonValue.valueOf((double) obj);
        }
        if (obj instanceof Float) {
            return JsonValue.valueOf((float) obj);
        }
        if (obj instanceof Long) {
            return JsonValue.valueOf((long) obj);
        }
        if (obj instanceof String) {
            return JsonValue.valueOf((String) obj);
        }
        if (obj instanceof JsonObject) {
            return (JsonObject) obj;
        }
        if (obj instanceof JsonArray) {
            return (JsonArray) obj;
        }
        if (obj instanceof UUID) {
            return JsonValue.valueOf(obj.toString());
        }

        if (obj instanceof Object[]) {
            Object[] objectArray = (Object[]) obj;
            JsonArray jsonArray = new JsonArray();
            for (Object aObj : objectArray) {
                jsonArray.add(objectToJson(aObj));
            }
            return jsonArray;
        }

        try {
            Method method = obj.getClass().getMethod("toJson");
            return (JsonValue) method.invoke(obj);
        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException e) {
            Log.debug("objectToJson - can't jsonify " + obj);
            e.printStackTrace();
        }
        return JsonValue.valueOf(obj.toString());
    }

    /**
     * Convert an object into a Json (Current Class Loader).
     *
     * @param json The Json to convert into Object
     * @return The object, array of object, json array or json object
     */
    public static Object jsonToObject(final JsonValue json) {
        return jsonToObject(json, null);
    }

    /**
     * Convert an object into a Json.
     *
     * @param json        The Json to convert into Object
     * @param classLoader The classLoader to look for object classes
     * @return The object, array of object, json array or json object
     */
    public static Object jsonToObject(final JsonValue json,
                                      final ClassLoader classLoader) {
        if (json == null || json.isNull()) {
            return null;
        }
        if (json.isBoolean()) {
            return json.asBoolean();
        }
        if (json.isNumber()) {
            return json.asDouble();
        }
        if (json.isString()) {
            if (json.asString().matches("[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}"
                    + "-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}")) {
                return UUID.fromString(json.asString());
            }
            return json.asString();
        }

        if (json.isArray()) {
            JsonArray jsonArray = json.asArray();
            if (jsonArray.size() > 0) {
                Object obj = jsonToObject(jsonArray.get(0), classLoader);
                if (obj != null) {
                    Class<?> c = obj.getClass();
                    Object objectArray = Array.newInstance(c, jsonArray.size());
                    for (int i = 0; i < jsonArray.size(); i++) {
                        Array.set(objectArray, i,
                                jsonToObject(jsonArray.get(i), classLoader));
                    }
                    return objectArray;
                }
            }
            return jsonArray;
        }

        if (json.asObject().get("type") != null) {
            try {
                Class<?> cl;
                if (classLoader != null) {
                    cl = Class.forName(json.asObject()
                            .get("type").asString(), false, classLoader);
                } else {
                    cl = Class.forName(json.asObject()
                            .get("type").asString());
                }
                Constructor con = cl.getConstructor(JsonObject.class);
                return con.newInstance(json);
            } catch (InstantiationException | IllegalAccessException
                    | ClassNotFoundException | NoSuchMethodException
                    | InvocationTargetException e) {
//                Log.debug("jsonToObject - Unknown class " + json.asObject().get("type").asString());
//                if (classLoader!=null) {
//                    Log.debug("Class loader from: " + classLoader.getClass().getName());
//                } else {
//                    Log.debug("Class loader on current thread: " + ClassLoader.getSystemClassLoader());
//                }
            }
        }

        return json;
    }

}
