package com.gdo.stencils.facet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class PythonSectionCompleter extends HTML5SectionCompleter {
    
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

    // expand a data-value from a data-path
    private JsonElement expandObject(StclContext stclContext, PStcl stcl, JsonObject object, boolean full) {
        if (object.has("data-path") && object.has("data-value")) {

            // get path
            JsonElement data_path = object.get("data-path");
            if (!data_path.isJsonPrimitive())
                throw new JsonParseException("data-path must be a string");
            String path = data_path.getAsString();

            // get result struture
            JsonElement data_value = object.get("data-value");
            if (!data_value.isJsonArray())
                throw new JsonParseException("data-value must be a list");

            JsonArray result = new JsonArray();
            for (PStcl s : stcl.getStencils(stclContext, path)) {
                JsonObject dict = new JsonObject();
                if (full) {
                    dict.add("data-path", new JsonPrimitive(s.pwd(stclContext)));
                    dict.add(".", new JsonPrimitive(s.getString(stclContext, ".")));
                }
                for (JsonElement elt : data_value.getAsJsonArray()) {
                    try {
                        String value = s.getString(stclContext, elt.getAsString());
                        dict.add(elt.getAsString(), new JsonPrimitive(value));
                    } catch (Exception e) {

                    }
                }
                result.add(dict);
            }

            object.remove("data-path");
            object.remove("data-value");
            object.add("data-values", result);
        }
        return object;
    }
}