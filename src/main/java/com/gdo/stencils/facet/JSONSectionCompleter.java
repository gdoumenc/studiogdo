package com.gdo.stencils.facet;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gdo.helper.ConverterHelper;
import com.gdo.stencils.StclContext;
import com.gdo.stencils.plug.PStcl;
import com.gdo.stencils.util.PathUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class JSONSectionCompleter extends HTML5SectionCompleter {

    /**
     * Retrieves a facet from a template descriptor.
     */
    public JSONSectionCompleter() {
    }

    @Override
    public FacetResult getFacetFromDOM(StclContext stclContext, PStcl stcl, String dom) {
        if (StringUtils.isBlank(dom)) {
            return new FacetResult(FacetResult.ERROR, "No dom defined", null);
        }

        JsonParser parser = new JsonParser();
        JsonElement elt = expand(stclContext, stcl, parser.parse(dom));
        InputStream reader = new ByteArrayInputStream(elt.toString().getBytes());
        return new FacetResult(reader, "text/html");

        /*
        		Gson gson = new Gson();
        		Parameters param = gson.fromJson(dom, Parameters.class);
        		String content = gson.toJson(expandToJSON(stclContext, stcl, param));
        		InputStream reader = IOUtils.toInputStream(content.toString());
        		return new FacetResult(reader, "application/json");
        		*/
    }

    @Override
    public FacetResult getFacetFromSkeleton(StclContext stclContext, PStcl stcl, String skel) {
        try {
            if (StringUtils.isBlank(skel)) {
                return new FacetResult(FacetResult.ERROR, "No JSON skeleton file defined", null);
            }

            // gets json and expands it
            JsonElement doc = getSkelDocument(stclContext, skel);
            JsonElement elt = expand(stclContext, stcl, doc);

            // FIXME gson.toJson(elt) sending unicode not UTF-8
            // Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // InputStream reader = new
            // ByteArrayInputStream(gson.toJson(elt).getBytes());
            InputStream reader = new ByteArrayInputStream(elt.toString().getBytes());
            return new FacetResult(reader, "text/html");
        } catch (Exception e) {
            e.printStackTrace();
            return new FacetResult(FacetResult.ERROR, e.toString(), null);
        }
    }

    private JsonElement getSkelDocument(StclContext stclContext, String skel) throws URISyntaxException, MalformedURLException, IOException {
        String skelDir = stclContext.getConfigParameter(StclContext.PROJECT_SKEL_DIR);
        String skelFilename = PathUtils.compose(skelDir, skel);
        FileReader skelFile = new FileReader(skelFilename);
        JsonParser parser = new JsonParser();
        JsonElement elt = parser.parse(skelFile);
        skelFile.close();
        return elt;
    }

    private JsonElement expand(StclContext stclContext, PStcl stcl, JsonElement elt) {

        // expands members on object
        if (elt.isJsonObject()) {
            JsonObject object = elt.getAsJsonObject();
            return expandObject(stclContext, stcl, object);
        }

        // expands array content
        if (elt.isJsonArray()) {
            JsonArray array = elt.getAsJsonArray();
            return expandArray(stclContext, stcl, array);
        }

        return elt;
    }

    private JsonElement expandPathObject(StclContext stclContext, PStcl stcl, JsonObject object) {
        // does nothing on node if no path or no value or no class
        String type = "s";
        String path = getPathAttribute(stclContext, stcl, object);
        String valuePath = getAttribute(object, DATA_VALUE_ATTRIBUTE);
        String cond = getAttribute(object, CONDITION_ATTRIBUTE);
        if (StringUtils.isBlank(path) && StringUtils.isBlank(valuePath) && StringUtils.isBlank(cond)) {
            return JsonNull.INSTANCE;
        }

        // checks condition
        if (!satisfyDataCondition(stclContext, cond, stcl)) {
            return JsonNull.INSTANCE;
        }

        // separate valuePath and type
        int indexOfPrct = valuePath.lastIndexOf("%");

        if (hasNewPath(object)) {
            type = "json";
        } else if (indexOfPrct > 0) {
            type = valuePath.substring(indexOfPrct + 1, valuePath.length());
            valuePath = valuePath.substring(0, indexOfPrct);
        }

        // return depending on type
        String value = getPropertyValue(stclContext, stcl, valuePath);
        if ("s".equals(type)) {
            return new JsonPrimitive(value);
        }
        if ("i".equals(type)) {
            return new JsonPrimitive(Integer.parseInt(value));
        }
        if ("b".equals(type)) {
            return new JsonPrimitive(ConverterHelper.parseBoolean(value));
        }
        if ("json".equals(type)) {
            return expandObject(stclContext, stcl, object);
        }

        return object;
    }

    private JsonElement expandObject(StclContext stclContext, PStcl stcl, JsonObject object) {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, JsonElement> member : object.entrySet()) {
            JsonElement replacement;
            if (hasNewPath(member.getValue())) {
                PStcl s = stcl.getStencil(stclContext, getAttribute(member.getValue().getAsJsonObject(), DATA_PATH_ATTRIBUTE));
                JsonElement temp = expandPathObject(stclContext, s, member.getValue().getAsJsonObject());
                // only get the data-value
                replacement = temp.getAsJsonObject().get("data-value");
            } else if (isExpandable(member.getValue())) {
                replacement = expandPathObject(stclContext, stcl, member.getValue().getAsJsonObject());
            } else {
                replacement = expand(stclContext, stcl, member.getValue());
            }
            result.add(member.getKey(), replacement);
        }
        return result;
    }

    /* check if elt has data-path or data-apath */
    private Boolean hasNewPath(JsonElement elt) {
        if (elt.isJsonObject()) {
            JsonObject object = elt.getAsJsonObject();

            String path = getAttribute(object, DATA_PATH_ATTRIBUTE);
            String apath = getAttribute(object, DATA_APATH_ATTRIBUTE);
            return (StringUtils.isNotBlank(path) || StringUtils.isNotBlank(apath));
        }
        return false;
    }

    private JsonElement expandArray(StclContext stclContext, PStcl stcl, JsonArray array) {
        JsonArray result = new JsonArray();
        Iterator<JsonElement> iter = array.iterator();
        while (iter.hasNext()) {
            JsonElement member = iter.next();
            String path = getPathAttribute(member.getAsJsonObject());
            for (PStcl child : stcl.getStencils(stclContext, path)) {

                JsonElement replacement;
                if (hasNewPath(member)) {
                    JsonElement temp = expandPathObject(stclContext, child, member.getAsJsonObject());
                    // only get the data-value
                    replacement = temp.getAsJsonObject().get("data-value");
                } else if (isExpandable(member)) {
                    replacement = expandPathObject(stclContext, child, member.getAsJsonObject());
                } else {
                    replacement = expand(stclContext, child, member);
                }
                result.add(replacement);

                // JsonElement replacement = expandObject(stclContext, child,
                // member.getAsJsonObject());
                // result.add(replacement);
            }
        }
        return result;
    }

    private boolean isExpandable(JsonElement elt) {
        if (elt.isJsonObject()) {
            JsonObject object = elt.getAsJsonObject();

            // does nothing on node if no path or no value or no class
            String path = getPathAttribute(object);
            String valuePath = getAttribute(object, DATA_VALUE_ATTRIBUTE);
            String cond = getAttribute(object, CONDITION_ATTRIBUTE);
            return (StringUtils.isNotBlank(path) || StringUtils.isNotBlank(valuePath) || StringUtils.isNotBlank(cond));
        }
        return false;
    }

    private String getPathAttribute(JsonObject elt) {
        return getPathAttribute(null, null, elt);
    }

    private String getPathAttribute(StclContext stclContext, PStcl stcl, JsonObject elt) {
        // get stencil path
        String path = "";
        if (stcl != null) {
            path = stcl.pwd(stclContext);
        }

        // get path
        String elt_path = getAttribute(elt, DATA_PATH_ATTRIBUTE);
        String elt_apath = getAttribute(elt, DATA_APATH_ATTRIBUTE);
        if (StringUtils.isNotBlank(elt_apath)) {
            elt_path = new String(decode(elt_apath));
        }
        if (StringUtils.isNotBlank(elt_path)) {
            path = PathUtils.compose(path, elt_path);
        }

        // gets complementary path
        String p1 = getAttribute(elt, DATA_COMPLEMENT_PATH_ATTRIBUTE);
        String ap1 = getAttribute(elt, DATA_ABSOLUTE_COMPLEMENT_PATH_ATTRIBUTE);
        if (StringUtils.isNotBlank(ap1)) {
            p1 = new String(decode(ap1));
        }
        if (StringUtils.isNotBlank(p1)) {
            path = PathUtils.compose(path, p1);
        }

        // gets complementary key
        String k1 = getAttribute(elt, DATA_COMPLEMENT_KEY_ATTRIBUTE);
        String ak1 = getAttribute(elt, DATA_ABSOLUTE_COMPLEMENT_KEY_ATTRIBUTE);
        if (StringUtils.isNotBlank(ak1)) {
            k1 = new String(decode(ak1));
        }
        if (StringUtils.isNotBlank(k1)) {
            path = PathUtils.createPath(path, k1);
        }

        return path;
    }

    private String getAttribute(JsonObject elt, String name) {
        JsonElement attr = elt.get(name);
        if (attr != null && !attr.isJsonNull() && attr.isJsonPrimitive()) {
            JsonPrimitive primitive = attr.getAsJsonPrimitive();
            if (primitive.isString()) {
                return primitive.getAsString();
            }
        } else if (attr != null && !attr.isJsonNull() && attr.isJsonObject()) {
            return attr.getAsJsonObject().toString();
        }
        return "";
    }

    public class Parameters {
        public String path;
        public String mode;
    }
}