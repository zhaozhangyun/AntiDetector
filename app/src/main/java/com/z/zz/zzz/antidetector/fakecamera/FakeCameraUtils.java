package com.z.zz.zzz.antidetector.fakecamera;

import android.graphics.Rect;
import android.util.Log;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.util.SizeF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FakeCameraUtils {

    private static final String TAG = "FakeCameraUtils";
    private static final String StreamConfigurationMap =
            "android.hardware.camera2.params.StreamConfigurationMap";
    private static final String StreamConfiguration =
            "android.hardware.camera2.params.StreamConfiguration";
    private static final String StreamConfigurationDuration =
            "android.hardware.camera2.params.StreamConfigurationDuration";
    private static final String HighSpeedVideoConfiguration =
            "android.hardware.camera2.params.HighSpeedVideoConfiguration";
    private static final String ReprocessFormatsMap =
            "android.hardware.camera2.params.ReprocessFormatsMap";
    private Pattern p1 = Pattern.compile("\\[(.*?)\\]");
    private List<String> fakeCameraIdList = new LinkedList<>();
    private Map<String, Map<String, Object>> cameraCharacteristics = new LinkedHashMap<>();
    private String mCurrentCameraId;
    private String cameraCharacteristicsMd5;

    private static class Holder {
        private static volatile FakeCameraUtils INSTANCE = new FakeCameraUtils();
    }

    private FakeCameraUtils() {
    }

    public static FakeCameraUtils get() {
        return Holder.INSTANCE;
    }

    public void fakeCameraCharacteristics(String jsonStr) throws JSONException {
        List<CameraCharacteristicsBean> ccBean = new LinkedList<>();

        JSONArray ja = new JSONObject(jsonStr).getJSONArray("cameraCharacteristicsBean");

        for (int i = 0; i < ja.length(); ++i) {
            CameraCharacteristicsBean bean = new CameraCharacteristicsBean();
            JSONObject jo = ja.getJSONObject(i);
            Field[] fields = CameraCharacteristicsBean.class.getDeclaredFields();
            for (Field f : fields) {
                try {
                    f.setAccessible(true);
                    String fieldName = f.getName();
                    f.set(bean, jo.getString(fieldName.replaceAll("_", ".")));
                } catch (Exception e) {
                    Log.e(TAG, "Error to set field: " + e.getMessage());
                } finally {
                    f.setAccessible(false);
                }
            }
            ccBean.add(bean);
        }

        fakeCameraCharacteristics(ccBean);
    }

    public void fakeCameraCharacteristics(List<CameraCharacteristicsBean> ccBean) {
        if (ccBean == null) {
            Log.w(TAG, "cameraCharacteristicsBean is null");
            return;
        }

        String md5 = Md5Util.getMd5(Arrays.toString(new List[]{ccBean}));

        if (cameraCharacteristicsMd5 != null && cameraCharacteristicsMd5.equals(md5)) {
            Log.w(TAG, "same md5, so return");
            return;
        }

        cameraCharacteristicsMd5 = md5;
        Log.d(TAG, "cameraCharacteristicsMd5: " + cameraCharacteristicsMd5);

        fakeCameraIdList.clear();
        cameraCharacteristics.clear();

        Iterator<CameraCharacteristicsBean> it = ccBean.listIterator();
        while (it.hasNext()) {
            CameraCharacteristicsBean bean = it.next();
            String cameraId = bean.cameraId;
            fakeCameraIdList.add(cameraId);
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("cameraId", cameraId);
            params.put("android.colorCorrection.availableAberrationModes", strToIntArr(
                    bean.android_colorCorrection_availableAberrationModes));
            params.put("android.control.aeAvailableAntibandingModes", strToIntArr(
                    bean.android_control_aeAvailableAntibandingModes));
            params.put("android.control.aeAvailableModes", strToIntArr(
                    bean.android_control_aeAvailableModes));
            params.put("android.control.aeAvailableTargetFpsRanges", strToRangeIntArr(
                    bean.android_control_aeAvailableTargetFpsRanges));
            params.put("android.control.aeCompensationRange", strToRangeInt(
                    bean.android_control_aeCompensationRange));
            params.put("android.control.aeCompensationStep", strToRational(
                    bean.android_control_aeCompensationStep));
            params.put("android.control.aeLockAvailable", Boolean.valueOf(
                    bean.android_control_aeLockAvailable));
            params.put("android.control.afAvailableModes", strToIntArr(
                    bean.android_control_afAvailableModes));
            params.put("android.control.availableEffects", strToIntArr(
                    bean.android_control_availableEffects));
            params.put("android.control.availableModes", strToIntArr(
                    bean.android_control_availableModes));
            params.put("android.control.availableSceneModes", strToIntArr(
                    bean.android_control_availableSceneModes));
            params.put("android.control.availableVideoStabilizationModes", strToIntArr(
                    bean.android_control_availableVideoStabilizationModes));
            params.put("android.control.awbAvailableModes", strToIntArr(
                    bean.android_control_awbAvailableModes));
            params.put("android.control.awbLockAvailable", Boolean.valueOf(
                    bean.android_control_awbLockAvailable));
            params.put("android.control.maxRegionsAe", Integer.valueOf(
                    bean.android_control_maxRegionsAe));
            params.put("android.control.maxRegionsAf", Integer.valueOf(
                    bean.android_control_maxRegionsAf));
            params.put("android.control.maxRegionsAwb", Integer.valueOf(
                    bean.android_control_maxRegionsAwb));
            params.put("android.control.postRawSensitivityBoostRange", strToRangeInt(
                    bean.android_control_postRawSensitivityBoostRange));
            params.put("android.depth.depthIsExclusive", Boolean.valueOf(
                    bean.android_depth_depthIsExclusive));
            params.put("android.distortionCorrection.availableModes", strToIntArr(
                    bean.android_distortionCorrection_availableModes));
            params.put("android.edge.availableEdgeModes", strToIntArr(
                    bean.android_edge_availableEdgeModes));
            params.put("android.flash.info.available", Boolean.valueOf(
                    bean.android_flash_info_available));
            params.put("android.hotPixel.availableHotPixelModes", strToIntArr(
                    bean.android_hotPixel_availableHotPixelModes));
            params.put("android.info.supportedHardwareLevel", Integer.valueOf(
                    bean.android_info_supportedHardwareLevel));
            params.put("android.jpeg.availableThumbnailSizes", strToSizeArr(
                    bean.android_jpeg_availableThumbnailSizes));
            params.put("android.lens.facing", Integer.valueOf(bean.android_lens_facing));
            params.put("android.lens.info.availableApertures", strToFloatArr(
                    bean.android_lens_info_availableApertures));
            params.put("android.lens.info.availableFilterDensities", strToFloatArr(
                    bean.android_lens_info_availableFilterDensities));
            params.put("android.lens.info.availableFocalLengths", strToFloatArr(
                    bean.android_lens_info_availableFocalLengths));
            params.put("android.lens.info.availableOpticalStabilization", strToIntArr(
                    bean.android_lens_info_availableOpticalStabilization));
            params.put("android.lens.info.focusDistanceCalibration", Integer.valueOf(
                    bean.android_lens_info_focusDistanceCalibration));
            params.put("android.noiseReduction.availableNoiseReductionModes", strToIntArr(
                    bean.android_noiseReduction_availableNoiseReductionModes));
            params.put("android.reprocess.maxCaptureStall", Integer.valueOf(
                    bean.android_reprocess_maxCaptureStall));
            params.put("android.request.availableCapabilities", strToIntArr(
                    bean.android_request_availableCapabilities));
            params.put("android.request.maxNumInputStreams", Integer.valueOf(
                    bean.android_request_maxNumInputStreams));
            params.put("android.request.maxNumOutputProc", Integer.valueOf(
                    bean.android_request_maxNumOutputProc));
            params.put("android.request.maxNumOutputProcStalling", Integer.valueOf(
                    bean.android_request_maxNumOutputProcStalling));
            params.put("android.request.maxNumOutputRaw", Integer.valueOf(
                    bean.android_request_maxNumOutputRaw));
            params.put("android.request.partialResultCount", Integer.valueOf(
                    bean.android_request_partialResultCount));
            params.put("android.request.pipelineMaxDepth", Byte.valueOf(
                    bean.android_request_pipelineMaxDepth));
            params.put("android.scaler.availableMaxDigitalZoom", Float.valueOf(
                    bean.android_scaler_availableMaxDigitalZoom));
            params.put("android.scaler.croppingType", Integer.valueOf(
                    bean.android_scaler_croppingType));
//            params.put("android.scaler.mandatoryStreamCombinations", strToMandatoryStreamCombinations(
//                    bean.android_scaler_mandatoryStreamCombinations));
            params.put("android.scaler.streamConfigurationMap", strToStreamConfigurationMap(
                    bean.android_scaler_streamConfigurationMap));
            params.put("android.sensor.availableTestPatternModes", strToIntArr(
                    bean.android_sensor_availableTestPatternModes));
            params.put("android.sensor.blackLevelPattern", strToBlackLevelPattern(
                    bean.android_sensor_blackLevelPattern));
            params.put("android.sensor.info.activeArraySize", strToRect(
                    bean.android_sensor_info_activeArraySize));
            params.put("android.sensor.info.colorFilterArrangement", Integer.valueOf(
                    bean.android_sensor_info_colorFilterArrangement));
            params.put("android.sensor.info.exposureTimeRange", strToRangeLong(
                    bean.android_sensor_info_exposureTimeRange));
            params.put("android.sensor.info.lensShadingApplied", Boolean.valueOf(
                    bean.android_sensor_info_lensShadingApplied));
            params.put("android.sensor.info.maxFrameDuration", Long.valueOf(
                    bean.android_sensor_info_maxFrameDuration));
            params.put("android.sensor.info.physicalSize", strToSizeF(
                    bean.android_sensor_info_physicalSize));
            params.put("android.sensor.info.pixelArraySize", strToSize(
                    bean.android_sensor_info_pixelArraySize));
            params.put("android.sensor.info.preCorrectionActiveArraySize", strToRect(
                    bean.android_sensor_info_preCorrectionActiveArraySize));
            params.put("android.sensor.info.sensitivityRange", strToRangeInt(
                    bean.android_sensor_info_sensitivityRange));
            params.put("android.sensor.info.timestampSource", Integer.valueOf(
                    bean.android_sensor_info_timestampSource));
            params.put("android.sensor.info.whiteLevel", Integer.valueOf(
                    bean.android_sensor_info_whiteLevel));
            params.put("android.sensor.maxAnalogSensitivity", Integer.valueOf(
                    bean.android_sensor_maxAnalogSensitivity));
            params.put("android.sensor.orientation", Integer.valueOf(
                    bean.android_sensor_orientation));
            params.put("android.shading.availableModes", strToIntArr(
                    bean.android_shading_availableModes));
            params.put("android.statistics.info.availableFaceDetectModes", strToIntArr(
                    bean.android_statistics_info_availableFaceDetectModes));
            params.put("android.statistics.info.availableHotPixelMapModes", strToBooleanArr(
                    bean.android_statistics_info_availableHotPixelMapModes));
            params.put("android.statistics.info.availableLensShadingMapModes", strToIntArr(
                    bean.android_statistics_info_availableLensShadingMapModes));
            params.put("android.statistics.info.availableOisDataModes", strToIntArr(
                    bean.android_statistics_info_availableOisDataModes));
            params.put("android.statistics.info.maxFaceCount", Integer.valueOf(
                    bean.android_statistics_info_maxFaceCount));
            params.put("android.sync.maxLatency", Integer.valueOf(bean.android_sync_maxLatency));
            params.put("android.tonemap.availableToneMapModes", strToIntArr(
                    bean.android_tonemap_availableToneMapModes));
            params.put("android.tonemap.maxCurvePoints", Integer.valueOf(
                    bean.android_tonemap_maxCurvePoints));
            cameraCharacteristics.put(cameraId, params);
        }
        Log.i(TAG, "finish to fake cameraCharacteristics");
    }

    public String[] getFakeCameraIds() {
        if (fakeCameraIdList == null) {
            return new String[0];
        }

        return fakeCameraIdList.toArray(new String[0]);
    }

    public void setCurrentCameraId(String cameraId) {
        mCurrentCameraId = cameraId;
    }

    public String getCurrentCameraId() {
        return mCurrentCameraId;
    }

    public <T> T getProperties(String keyName) {
        if (cameraCharacteristics == null || mCurrentCameraId == null) {
            return null;
        }

        Map<String, Object> ccKey = cameraCharacteristics.get(mCurrentCameraId);
        return (T) ccKey.get(keyName);
    }

    public void saveFakeCameraObject(String name, Object obj) throws Exception {
        Log.i(TAG, "save fakecamera object to " + name);
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(name));
            oos.writeObject(obj);
            oos.flush();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public <T> T loadFakeCameraObject(String name) throws Exception {
        Log.i(TAG, "load fakecamera object from " + name);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(name));
            return (T) ois.readObject();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String trimStr(String arr) {
        if (arr == null || arr.length() == 0) {
            return null;
        }
        if (arr.startsWith("[") && arr.endsWith("]")) {
            return arr.substring(arr.indexOf("[") + 1, arr.lastIndexOf("]"));
        }
        return arr;
    }

    private int[] strToIntArr(String arr) {
        String newStr = trimStr(arr);
        if (newStr == null) {
            return null;
        }

        String[] strs = newStr.split(", ");
        int[] intArr = new int[strs.length];
        for (int i = 0; i < strs.length; ++i) {
            intArr[i] = Integer.valueOf(strs[i].trim());
        }

        assert arr.equals(Arrays.toString(intArr));
        return intArr;
    }

    private float[] strToFloatArr(String arr) {
        String newStr = trimStr(arr);
        if (newStr == null) {
            return null;
        }

        String[] strs = newStr.split(", ");
        float[] floatArr = new float[strs.length];
        for (int i = 0; i < strs.length; ++i) {
            floatArr[i] = Float.valueOf(strs[i].trim());
        }

        assert arr.equals(Arrays.toString(floatArr));
        return floatArr;
    }

    private boolean[] strToBooleanArr(String arr) {
        String newStr = trimStr(arr);
        if (newStr == null) {
            return null;
        }

        String[] strs = newStr.split(", ");
        boolean[] booleanArr = new boolean[strs.length];
        for (int i = 0; i < strs.length; ++i) {
            booleanArr[i] = Boolean.valueOf(strs[i].trim());
        }

        assert arr.equals(Arrays.toString(booleanArr));
        return booleanArr;
    }

    private Range strToRangeInt(String arr) {
        String newStr = trimStr(arr);
        if (newStr == null) {
            return null;
        }

        String[] strs = newStr.split(", ");
        Range<Integer> r = new Range<>(Integer.valueOf(strs[0].trim()), Integer.valueOf(strs[1].trim()));

        assert arr.equals(r.toString());
        return r;
    }

    private Range strToRangeLong(String arr) {
        String newStr = trimStr(arr);
        if (newStr == null) {
            return null;
        }

        String[] strs = newStr.split(", ");
        Range<Long> r = new Range<>(Long.valueOf(strs[0].trim()), Long.valueOf(strs[1].trim()));

        assert arr.equals(r.toString());
        return r;
    }

    private Range[] strToRangeIntArr(String arr) {
        String newStr = trimStr(arr);
        if (newStr == null) {
            return null;
        }

        int N = 0;
        Matcher m = p1.matcher(newStr);
        while (m.find()) {
            ++N;
        }

        Range<Integer>[] ranges = new Range[N];

        int i = 0;
        Matcher m1 = p1.matcher(newStr);
        while (m1.find()) {
            String str = m1.group();
            str = str.substring(1, str.length() - 1);
            String[] strs = str.split(", ");
            ranges[i] = new Range<>(Integer.valueOf(strs[0].trim()), Integer.valueOf(strs[1].trim()));
            ++i;
        }

        assert arr.equals(Arrays.toString(ranges));
        return ranges;
    }

    private Range[] strToRangeLongArr(String arr) {
        String newStr = trimStr(arr);
        if (newStr == null) {
            return null;
        }

        int N = 0;
        Matcher m = p1.matcher(newStr);
        while (m.find()) {
            ++N;
        }

        Range<Long>[] ranges = new Range[N];

        int i = 0;
        Matcher m1 = p1.matcher(newStr);
        while (m1.find()) {
            String str = m1.group();
            str = str.substring(1, str.length() - 1);
            String[] strs = str.split(", ");
            ranges[i] = new Range<>(Long.valueOf(strs[0].trim()), Long.valueOf(strs[1].trim()));
            ++i;
        }

        assert arr.equals(Arrays.toString(ranges));
        return ranges;
    }

    private Rational strToRational(String arr) {
        if (arr == null || arr.length() == 0) {
            return null;
        }

        String[] strs = arr.split("/");
        Rational r = new Rational(Integer.valueOf(strs[0].trim()), Integer.valueOf(strs[1].trim()));

        assert arr.equals(r.toString());
        return r;
    }

    private Size[] strToSizeArr(String arr) {
        String newStr = trimStr(arr);
        if (newStr == null) {
            return null;
        }

        String[] strs = newStr.split(", ");
        Size[] sizeArr = new Size[strs.length];
        for (int i = 0; i < strs.length; ++i) {
            String[] strArr = strs[i].split("x");
            sizeArr[i] = new Size(Integer.valueOf(strArr[0].trim()), Integer.valueOf(strArr[1].trim()));
        }

        assert arr.equals(Arrays.toString(sizeArr));
        return sizeArr;
    }

    private Size strToSize(String arr) {
        String newStr = trimStr(arr);
        if (newStr == null) {
            return null;
        }

        String[] strArr = newStr.split("x");
        Size size = new Size(Integer.valueOf(strArr[0].trim()), Integer.valueOf(strArr[1].trim()));

        assert arr.equals(size.toString());
        return size;
    }

    private SizeF strToSizeF(String arr) {
        String newStr = trimStr(arr);
        if (newStr == null) {
            return null;
        }

        String[] strArr = newStr.split("x");
        SizeF sizeF = new SizeF(Float.valueOf(strArr[0].trim()), Float.valueOf(strArr[1].trim()));

        assert arr.equals(sizeF.toString());
        return sizeF;
    }

    private Rect strToRect(String arr) {
        if (arr == null || arr.length() == 0 || !arr.startsWith("Rect(")) {
            return null;
        }

        String newStr = arr.substring(findStrIndex(arr, "Rect("), arr.lastIndexOf(")"));
        String[] strs = newStr.split(" - ");
        int left = Integer.parseInt(strs[0].split(", ")[0]);
        int top = Integer.parseInt(strs[0].split(", ")[1]);
        int right = Integer.parseInt(strs[1].split(", ")[0]);
        int bottom = Integer.parseInt(strs[1].split(", ")[1]);

        Rect rect = new Rect(left, top, right, bottom);
        assert arr.equals(rect.toString());
        return rect;
    }

    private Object strToStreamConfigurationMap(String arr) {
        if (arr == null || arr.length() == 0 || !arr.startsWith("StreamConfiguration(")) {
            return null;
        }

        Map<String, JSONArray> map = new LinkedHashMap<>();

        String newStr = arr.substring(findStrIndex(arr, "StreamConfiguration("),
                arr.lastIndexOf(")"));

        String outputs = newStr.substring(findStrIndex(newStr, "Outputs("),
                newStr.indexOf("), HighResolutionOutputs("));
        JSONArray jOutputs = arrToJson(outputs, "Outputs");
        map.put("Outputs", jOutputs);

        String highResolutionOutputs = newStr.substring(findStrIndex(newStr,
                "HighResolutionOutputs("), newStr.indexOf("), Inputs("));
        JSONArray jHighResolutionOutputs = arrToJson(highResolutionOutputs, "HighResolutionOutputs");
        map.put("HighResolutionOutputs", jHighResolutionOutputs);

        String inputs = newStr.substring(findStrIndex(newStr, "Inputs("),
                newStr.indexOf("), ValidOutputFormatsForInput("));
        JSONArray jInputs = arrToJson(inputs, "Inputs");
        map.put("Inputs", jInputs);

        String validOutputFormatsForInput = newStr.substring(findStrIndex(newStr,
                "ValidOutputFormatsForInput("), newStr.indexOf("), HighSpeedVideoConfigurations("));
        JSONArray jValidOutputFormatsForInput = arrToJson(validOutputFormatsForInput,
                "ValidOutputFormatsForInput");
        map.put("ValidOutputFormatsForInput", jValidOutputFormatsForInput);

        String highSpeedVideoConfigurations = newStr.substring(findStrIndex(newStr,
                "HighSpeedVideoConfigurations("), newStr.length() - 1);
        JSONArray jHighSpeedVideoConfigurations = arrToJson(highSpeedVideoConfigurations,
                "HighSpeedVideoConfigurations");
        map.put("HighSpeedVideoConfigurations", jHighSpeedVideoConfigurations);

        return rebuildStreamConfigurationMap(map);
    }

    private int[] strToBlackLevelPattern(String arr) {
        if (arr == null || arr.length() == 0 || !arr.startsWith("BlackLevelPattern(")) {
            return null;
        }

        String newStr = arr.substring(findStrIndex(arr, "BlackLevelPattern("),
                arr.lastIndexOf(")"));

        int[] offsets = new int[4];

        int i = 0;
        Matcher m1 = p1.matcher(newStr);
        while (m1.find()) {
            String str = m1.group();
            str = str.substring(1, str.length() - 1);
            String[] strs = str.split(", ");
            if (i == 0) {
                offsets[0] = Integer.parseInt(strs[0]);
                offsets[1] = Integer.parseInt(strs[1]);
            } else if (i == 1) {
                offsets[2] = Integer.parseInt(strs[0]);
                offsets[3] = Integer.parseInt(strs[1]);
            }
            ++i;
        }

        return offsets;
    }

    private int findStrIndex(String src, String findStr) {
        return src.indexOf(findStr) + findStr.length();
    }

    private JSONArray arrToJson(String content, String name) {
        JSONArray ja = new JSONArray();
        try {
            Matcher m1 = p1.matcher(content);
            while (m1.find()) {
                String str = m1.group();
                if (str.contains("in:") && str.contains("out:")) {
                    str = trimStr(str);
                    String[] strs = str.split(", ");
                    JSONObject jo = new JSONObject();
                    try {
                        String in = strs[0].substring("in:".length());
                        if (in != null && in.contains("(") && in.contains(")")) {
                            int fmt = Integer.valueOf(in.substring(in.indexOf("(") + 1,
                                    in.indexOf(")")));
                            jo.put("in", fmt);
                        }
                        String out1 = strs[1].substring("out:".length());
                        String out2 = strs[2];
                        if (out1 != null && out1.contains("(") && out1.contains(")")
                                && out2 != null && out2.contains("(") && out2.contains(")")) {
                            int fmt1 = Integer.valueOf(out1.substring(out1.indexOf("(") + 1,
                                    out1.indexOf(")")));
                            int fmt2 = Integer.valueOf(out2.substring(out2.indexOf("(") + 1,
                                    out2.indexOf(")")));
                            jo.put("out", new JSONArray(new int[]{fmt1, fmt2}));
                        }
                        ja.put(jo);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    str = str.replaceAll("\\[", "{").replaceAll("\\]", "}");
                    try {
                        JSONObject jo = new JSONObject(str);
                        String format = jo.optString("format");
                        if (format != null && format.contains("(") && format.contains(")")) {
                            int fmt = Integer.valueOf(format.substring(format.indexOf("(") + 1,
                                    format.indexOf(")")));
                            jo.put("format", fmt);
                        }
                        ja.put(jo);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return ja;
    }

    private Object rebuildStreamConfigurationMap(Map<String, JSONArray> map) {
        JSONArray jOutputs = map.get("Outputs");
//        Log.d(TAG, "Outputs size: " + jOutputs.length());

        JSONArray jHighResolutionOutputs = map.get("HighResolutionOutputs");
//        Log.d(TAG, "HighResolutionOutputs size: "
//                + jHighResolutionOutputs.length());

        JSONArray jInputs = map.get("Inputs");
//        Log.d(TAG, "Inputs size: " + jInputs.length());

        JSONArray jValidOutputFormatsForInput = map.get("ValidOutputFormatsForInput");
//        Log.d(TAG, "ValidOutputFormatsForInput size: "
//                + jValidOutputFormatsForInput.length());

        JSONArray jHighSpeedVideoConfigurations = map.get("HighSpeedVideoConfigurations");
//        Log.d(TAG, "HighSpeedVideoConfigurations size: "
//                + jHighSpeedVideoConfigurations.length());

        List mConfigurationsList = new LinkedList<>();
        List mMinFrameDurationsList = new LinkedList<>();
        List mStallDurationsList = new LinkedList<>();

        List mDepthConfigurationsList = new LinkedList<>();
        List mDepthMinFrameDurationsList = new LinkedList<>();
        List mDepthStallDurationsList = new LinkedList<>();

        List mHighSpeedVideoConfigurationsList = new LinkedList<>();

        List<Integer> mEntryList = new LinkedList<>();

        /* Outputs */
        for (int i = 0; i < jOutputs.length(); ++i) {
            try {
                JSONObject jo = jOutputs.getJSONObject(i);
                int format = jo.getInt("format");
                int width = jo.getInt("w");
                int height = jo.getInt("h");
                long minDuration = jo.getLong("min_duration");
                long stall = jo.getLong("stall");

//                Log.d(TAG, "format: "
//                        + StreamConfMap.formatToString(format) + "(" + format + ")");

                int fmt;
                boolean isDepthFormat;
                try {
                    fmt = StreamConfMap.publicToDepthFormat(format);
                    isDepthFormat = true;
                } catch (IllegalArgumentException e) {
                    fmt = StreamConfMap.publicToImageFormat(format);
                    isDepthFormat = false;
                }

                if (isDepthFormat) {
//                    mDepthConfigurationsList.add(new StreamConfiguration(format, width, height,
//                            false));
//                    mDepthMinFrameDurationsList.add(new StreamConfigurationDuration(format, width, height,
//                            minDuration));
//                    mDepthStallDurationsList.add(new StreamConfigurationDuration(format, width, height,
//                            stall));
                    try {
                        mDepthConfigurationsList.add(Reflect.on(StreamConfiguration).create(
                                fmt, width, height, false).get());
                    } catch (Exception e) {
//                        e.printStackTrace();
                        Log.e(TAG, "Error: " + jo, e);
                    }
                    try {
                        mDepthMinFrameDurationsList.add(Reflect.on(StreamConfigurationDuration)
                                .create(fmt, width, height, minDuration).get());
                    } catch (Exception e) {
//                        e.printStackTrace();
                        Log.e(TAG, "Error: " + jo, e);
                    }
                    try {
                        mDepthStallDurationsList.add(Reflect.on(StreamConfigurationDuration)
                                .create(fmt, width, height, stall).get());
                    } catch (Exception e) {
//                        e.printStackTrace();
                        Log.e(TAG, "Error: " + jo, e);
                    }
                } else {
//                    mConfigurationsList.add(new StreamConfiguration(format, width, height, false));
//                    mMinFrameDurationsList.add(new StreamConfigurationDuration(format, width, height,
//                            minDuration));
//                    mStallDurationsList.add(new StreamConfigurationDuration(format, width, height,
//                            stall));
                    try {
                        mConfigurationsList.add(Reflect.on(StreamConfiguration).create(
                                fmt, width, height, false).get());
                    } catch (Exception e) {
//                        e.printStackTrace();
                        Log.e(TAG, "Error: " + jo, e);
                    }
                    try {
                        mMinFrameDurationsList.add(Reflect.on(StreamConfigurationDuration)
                                .create(fmt, width, height, minDuration).get());
                    } catch (Exception e) {
//                        e.printStackTrace();
                        Log.e(TAG, "Error: " + jo, e);
                    }
                    try {
                        mStallDurationsList.add(Reflect.on(StreamConfigurationDuration)
                                .create(fmt, width, height, stall).get());
                    } catch (Exception e) {
//                        e.printStackTrace();
                        Log.e(TAG, "Error: " + jo, e);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /* HighResolutionOutputs */
        for (int i = 0; i < jHighResolutionOutputs.length(); ++i) {
        }

        /* Inputs */
        for (int i = 0; i < jInputs.length(); ++i) {
            try {
                JSONObject jo = jInputs.getJSONObject(i);
                int format = jo.getInt("format");
                int width = jo.getInt("w");
                int height = jo.getInt("h");
//                mConfigurationsList.add(new StreamConfiguration(format, width, height, true));
                try {
                    mConfigurationsList.add(Reflect.on(StreamConfiguration).create(
                            format, width, height, true).get());
                } catch (Exception e) {
//                    e.printStackTrace();
                    Log.e(TAG, "Error: " + jo, e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /* ValidOutputFormatsForInput */
        for (int i = 0; i < jValidOutputFormatsForInput.length(); ++i) {
            try {
                JSONObject jo = jValidOutputFormatsForInput.getJSONObject(i);
                mEntryList.add(jo.getInt("in"));

                JSONArray ja = jo.getJSONArray("out");
                mEntryList.add(ja.length());
                for (int j = 0; j < ja.length(); ++j) {
                    mEntryList.add(StreamConfMap.publicToImageFormat(ja.getInt(j)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /* HighSpeedVideoConfigurations */
        for (int i = 0; i < jHighSpeedVideoConfigurations.length(); ++i) {
            try {
                JSONObject jo = jHighSpeedVideoConfigurations.getJSONObject(i);
                int width = jo.getInt("w");
                int height = jo.getInt("h");
                int minFps = jo.getInt("min_fps");
                int maxFps = jo.getInt("max_fps");
                int batchSizeMax = maxFps == 240 ? 8 : 4;
//                mHighSpeedVideoConfigurationsList.add(new HighSpeedVideoConfiguration(width, height,
//                        minFps, maxFps, batchSizeMax));
                try {
                    mHighSpeedVideoConfigurationsList.add(Reflect.on(HighSpeedVideoConfiguration)
                            .create(width, height, minFps, maxFps, batchSizeMax).get());
                } catch (Exception e) {
//                    e.printStackTrace();
                    Log.e(TAG, "Error: " + jo, e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Class streamConfigurationCls = null;
        try {
            streamConfigurationCls = Reflect.on(StreamConfiguration).getCls();
        } catch (Reflect.ReflectException e) {
            e.printStackTrace();
        }

        Class streamConfigurationDurationCls = null;
        try {
            streamConfigurationDurationCls = Reflect.on(StreamConfigurationDuration).getCls();
        } catch (Reflect.ReflectException e) {
            e.printStackTrace();
        }

        Class highSpeedVideoConfigurationCls = null;
        try {
            highSpeedVideoConfigurationCls = Reflect.on(HighSpeedVideoConfiguration).getCls();
        } catch (Reflect.ReflectException e) {
            e.printStackTrace();
        }

        try {
            Object streamConfigurationMapObj = Reflect.on(StreamConfigurationMap).create(
                    mConfigurationsList.toArray((Object[]) Array.newInstance(
                            streamConfigurationCls, 0)),
                    mMinFrameDurationsList.toArray((Object[]) Array.newInstance(
                            streamConfigurationDurationCls, 0)),
                    mStallDurationsList.toArray((Object[]) Array.newInstance(
                            streamConfigurationDurationCls, 0)),
                    mDepthConfigurationsList.toArray((Object[]) Array.newInstance(
                            streamConfigurationCls, 0)),
                    mDepthMinFrameDurationsList.toArray((Object[]) Array.newInstance(
                            streamConfigurationDurationCls, 0)),
                    mDepthStallDurationsList.toArray((Object[]) Array.newInstance(
                            streamConfigurationDurationCls, 0)),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    mHighSpeedVideoConfigurationsList.toArray((Object[]) Array.newInstance(
                            highSpeedVideoConfigurationCls, 0)),
                    Reflect.on(ReprocessFormatsMap).create(
                            toPrimitiveInts(mEntryList.toArray(new Integer[0]))).get(),
                    true,
                    true
            ).get();
            Log.d(TAG, "++++++++++++++++++++++++++++++++++++++++++ StreamConfigurationMapObj:\n"
                    + streamConfigurationMapObj, null);
            return streamConfigurationMapObj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private int[] toPrimitiveInts(Integer[] ints) {
        int[] primitiveInts = new int[ints.length];
        for (int i = 0; i < ints.length; i++) {
            primitiveInts[i] = ints[i] == null ? 0 : ints[i];
        }
        return primitiveInts;
    }

}
