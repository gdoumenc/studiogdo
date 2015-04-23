package com.gdo.stencils.facet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class RestPythonSectionCompleter extends HTML5SectionCompleter {

    private boolean _full;

    public void setFullStructure() {
        _full = true;
    }

    @Override
    public FacetResult getFacetFromDOM(StclContext stclContext, PStcl stcl, String json) {

        // check parameter
        if (StringUtils.isBlank(json)) {
            return new FacetResult(FacetResult.ERROR, "No parameter defined", null);
        }

        // create facet
        try {
            JsonParser parser = new JsonParser();
            JsonElement elt = expand(stclContext, stcl, parser.parse(json), _full);
            InputStream reader = new ByteArrayInputStream(elt.toString().getBytes());
            return new FacetResult(reader, "text/plain");
        } catch (Exception e) {
            String msg = String.format("%s on %s", e, json);
            return new FacetResult(FacetResult.ERROR, msg, null);
        }
    }

    private JsonElement expand(StclContext stclContext, PStcl stcl, JsonElement elt, boolean full) {

        // expands array of objects
        if (elt.isJsonArray()) {
            JsonArray array = elt.getAsJsonArray();
            return expandArray(stclContext, stcl, array, full);
        }

        // expands object
        if (elt.isJsonObject()) {
            JsonObject object = elt.getAsJsonObject();
            return expandObject(stclContext, stcl, object, full);
        }

        throw new JsonParseException("the mode must be a list or a dictionary");
    }

    private JsonElement expandArray(StclContext stclContext, PStcl stcl, JsonArray array, boolean full) {
        JsonArray result = new JsonArray();
        for (JsonElement item : array) {
            if (item.isJsonObject())
                result.add(expandObject(stclContext, stcl, item.getAsJsonObject(), full));
            else
                result.add(item);
        }
        return result;
    }

    /**
     * Expand a data-value from a data-path.
     *
     * @param stclContext
     * @param stcl
     * @param object
     * @param full
     *            render more info (path, this) on each expansion if defined to
     *            <tt>true</tt>.
     * @return
     */
    private JsonElement expandObject(StclContext stclContext, PStcl stcl, JsonObject object, boolean full) {
        boolean return_array = false;
        JsonArray result = new JsonArray();
        if (object.has("data-path") && object.has("data-value")) {

            // get path
            JsonElement data_path = object.get("data-path");
            String path = null;
            if (data_path.isJsonArray()) {
                JsonArray array = data_path.getAsJsonArray();
                if (array.size() != 1) {
                    throw new JsonParseException("data-path can be only an array of size 1");
                }
                data_path = array.get(0);
                return_array = true;
            }
            if (data_path.isJsonPrimitive()) {
                path = data_path.getAsString();
            } else {
                throw new JsonParseException("data-path must be a string");
            }
            if (StringUtils.isBlank(path)) {
                throw new JsonParseException("data-path is not defined (should be a string or a list of string");
            }

            // get result struture
            JsonElement data_value = object.get("data-value");
            if (!data_value.isJsonArray())
                throw new JsonParseException("data-value must be a list");

            for (PStcl s : stcl.getStencils(stclContext, path)) {
                JsonObject dict = new JsonObject();
                if (full) {
                    dict.add("data-path", new JsonPrimitive(s.pwd(stclContext)));
                    dict.add(".", new JsonPrimitive(s.getString(stclContext, ".")));
                }
                for (JsonElement elt : data_value.getAsJsonArray()) {
                    try {
                        if (elt.isJsonObject()) {
                            JsonObject obj = elt.getAsJsonObject();
                            String p = obj.get("data-path").getAsString();
                            JsonElement array = expandObject(stclContext, s, obj, full);
                            JsonElement previous = dict.get(p);
                            if (previous == null) {
                                dict.add(p, array);
                            } else {
                                JsonArray new_list = new JsonArray();
                                JsonArray previous_list = previous.getAsJsonArray();
                                JsonArray added_list = array.getAsJsonArray();
                                for (int i = 0; i < previous_list.size(); i++) {
                                    JsonObject new_elt = new JsonObject();
                                    JsonObject old_elt = previous_list.get(i).getAsJsonObject();
                                    for (Map.Entry<String, JsonElement> entry : old_elt.entrySet()) {
                                        new_elt.add(entry.getKey(), entry.getValue());
                                    }
                                    JsonObject added_elt = added_list.get(i).getAsJsonObject();
                                    for (Map.Entry<String, JsonElement> entry : added_elt.entrySet()) {
                                        new_elt.add(entry.getKey(), entry.getValue());
                                    }
                                    new_list.add(new_elt);
                                }
                                dict.add(p, new_list);
                            }
                        }
                        String value = s.getString(stclContext, elt.getAsString(), null);
                        if (value != null)
                            dict.add(elt.getAsString(), new JsonPrimitive(value)); 
                    } catch (Exception e) {

                    }
                }
                if (!return_array) {
                    return dict;
                }
                result.add(dict);
            }
        } else {
            JsonObject dict = new JsonObject();
            for (Map.Entry<String, JsonElement> elt : object.entrySet()) {
                JsonElement value = expand(stclContext, stcl, elt.getValue(), full);
                dict.add(elt.getKey(), value);
            }
            if (!return_array) {
                return dict;
            }
            result.add(dict);
        }

        return result;
    }
}