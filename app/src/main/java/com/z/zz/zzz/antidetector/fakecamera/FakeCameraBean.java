package com.z.zz.zzz.antidetector.fakecamera;

import java.io.Serializable;
import java.util.List;

public class FakeCameraBean implements Serializable {

    public List<CameraCharacteristicsBean> cameraCharacteristicsBean;

    @Override
    public String toString() {
        return "FakeCameraBean{" +
                "cameraCharacteristicsBean=" + cameraCharacteristicsBean +
                '}';
    }
}
