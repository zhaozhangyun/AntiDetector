package com.z.zz.zzz.xml;

import android.content.Context;

import com.z.zz.zzz.utils.L;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.util.LinkedList;
import java.util.List;

public class WhiteListXmlParser {
    private static String TAG = "WhiteListXmlParser";
    private String androidId = "";
    private List<WhiteListEntry> pluginEntries = new LinkedList<>();

    public List<WhiteListEntry> getPluginEntries() {
        return pluginEntries;
    }

    public void parse(Context action) {
        // First checking the class namespace for config.xml
        int id = action.getResources().getIdentifier("anti_detect_white_list", "xml", action.getClass().getPackage().getName());
        if (id == 0) {
            // If we couldn't find config.xml there, we'll look in the namespace from AndroidManifest.xml
            id = action.getResources().getIdentifier("anti_detect_white_list", "xml", action.getPackageName());
            if (id == 0) {
                L.e(TAG, "res/xml/anti_detect_white_list.xml is missing!");
                return;
            }
        }
        parse(action.getResources().getXml(id));
    }

    private void parse(XmlPullParser xml) {
        int eventType = -1;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                handleStartTag(xml);
            } else if (eventType == XmlPullParser.END_TAG) {
                handleEndTag(xml);
            }
            try {
                eventType = xml.next();
            } catch (XmlPullParserException e) {
                L.e(TAG, "parse error", e);
            } catch (Exception e) {
                L.e(TAG, "parse error", e);
            }
        }

        L.d(TAG, "parse: " + pluginEntries);
    }

    private void handleStartTag(XmlPullParser xml) {
        String strNode = xml.getName();
        if ("android_id".equals(strNode)) {
            androidId = xml.getAttributeValue(null, "id");
        }
    }

    private void handleEndTag(XmlPullParser xml) {
        String strNode = xml.getName();
        if ("android_id".equals(strNode)) {
            pluginEntries.add(new WhiteListEntry(androidId));

            androidId = "";
        }
    }

}
