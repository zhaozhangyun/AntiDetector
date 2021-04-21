package com.z.zz.zzz.antidetector;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Size;

import com.z.zz.zzz.utils.L;

import java.util.ArrayList;
import java.util.List;


public class CameraInfoV21 {

    public static List getCameraInfoList(Context arg12) {
        ArrayList list = new ArrayList();
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                CameraManager cm = (CameraManager) arg12.getSystemService(Context.CAMERA_SERVICE);
                String[] cameraIdList = cm.getCameraIdList();
                for (int i = 0; true; ++i) {
                    if (i >= cameraIdList.length) {
                        return list;
                    }

                    CameraCharacteristics cameraCharacteristics = cm.getCameraCharacteristics(cameraIdList[i]);
                    Integer v7 = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (v7 != null && (v7) != 2) {
                        if ((v7) == 0) {
//                            entity.setFront(true);
                        }

                        StreamConfigurationMap v5_1 = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                        if (v5_1 != null) {
                            Size[] sizes = v5_1.getOutputSizes(0x100);
                            if (sizes.length > 0) {
                                for (int n = 0; n < sizes.length; ++n) {
                                    Size s = sizes[n];
                                    int area = s.getWidth() * s.getHeight();
                                    L.d("CameraInfoV21", "area: " + area);
//                                    if (entity.getArea() < area) {
//                                        entity.setArea(area);
//                                        entity.setWidth(s.getWidth());
//                                        entity.setHeight(s.getHeight());
//                                    }
                                }

//                                list.add(entity);
                            }

//                            list.add(entity);
                        }
                    }
                }
            }
        } catch (Exception unused_ex) {
        }

        return list;
    }
}
