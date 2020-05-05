
package com.android.gridpasswordview;

import android.content.Context;

public class MyResources {
    /**
     * 根据资源的名字获取它的ID
     * 
     * @param defType 资源的类型，如drawable, string, id, layout
     * @param name 要获取的资源的名字
     * @return 资源的id
     */

    public static int getResourcesId(Context mContext, String defType, String name) {
        String packageName = mContext.getApplicationInfo().packageName;
        return mContext.getResources().getIdentifier(name, defType, packageName);
    }
}
