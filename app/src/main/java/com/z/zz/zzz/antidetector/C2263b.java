package com.z.zz.zzz.antidetector;

import android.os.Binder;
import java.lang.ref.WeakReference;

public class C2263b extends Binder {

    private WeakReference f8341a;

    public StepService mo8058a() {
        if (this.f8341a == null) {
            return null;
        }
        return (StepService) this.f8341a.get();
    }

    public void mo8059a(StepService stepService) {
        this.f8341a = new WeakReference(stepService);
    }
}
