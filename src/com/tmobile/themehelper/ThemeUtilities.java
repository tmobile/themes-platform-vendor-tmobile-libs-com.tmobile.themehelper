package com.tmobile.themehelper;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

import java.lang.reflect.Method;

/**
 * Common utilities to access theme attributes and resources.
 * <p>
 * This class uses reflection so that it will compile as-is when theme support
 * is not present in the current build framework (for instance, if building with the
 * SDK or on a feature-branch that excludes themes).
 */
public class ThemeUtilities {
    private static boolean sThemeSupportTested;
    private static Method sGetThemePackageName;
    private static final Object[] sGetThemePackageNameArgs = {};

    /**
     * Alternative to {@link #resolveDefaultStyleAttr(Context, String)} which
     * allows you to specify a resource id for fallback. This is merely an
     * optimization which avoids by name lookup in the current application
     * package scope.
     *
     * @param context
     * @param attrName Attribute name in the currently applied theme.
     * @param fallbackAttrId Attribute id to return if the currently applied
     *            theme does not specify the supplied <code>attrName</code>.
     * @see #resolveDefaultStyleAttr(Context, String)
     */
    public static int resolveDefaultStyleAttr(Context context, String attrName,
            int fallbackAttrId) {
        /* First try to resolve in the currently applied global theme. */
        int attrId = getThemeStyleAttr(context, attrName);
        if (attrId != 0) {
            return attrId;
        }
        /* Fallback to the provided value. */
        return fallbackAttrId;
    }

    /**
     * Dynamically resolve the supplied attribute name within the theme or
     * application scope. First looks at the currently applied global theme,
     * then fallbacks to the current application package.
     *
     * @param context
     * @param attrName Attribute name in the currently applied theme.
     * @return the attribute id suitable for passing to a View's constructor or
     *         0 if neither are provided.
     * @see View#View(Context, android.util.AttributeSet, int)
     */
    public static int resolveDefaultStyleAttr(Context context, String attrName) {
        /* First try to resolve in the currently applied global theme. */
        int attrId = resolveDefaultStyleAttr(context, attrName, 0);
        if (attrId != 0) {
            return attrId;
        }
        /* Then try to lookup in the application's package. */
        return context.getResources().getIdentifier(attrName, "attr",
                context.getPackageName());
    }

    private static int getThemeStyleAttr(Context context, String attrName) {
        String themePackage = getCurrentThemePackage(context);
        if (themePackage == null) {
            return 0;
        }
        return context.getResources().getIdentifier(attrName, "attr", themePackage);
    }

    /*
     * Uses reflection because theme support may be present on the current
     * platform but the SDK used to build the module using this class might not
     * have been.
     */
    private static String getCurrentThemePackage(Context context) {
        if (!sThemeSupportTested) {
            try {
                sGetThemePackageName =
                    AssetManager.class.getMethod("getThemePackageName", (Class[])null);
            } catch (NoSuchMethodException e) {
            } finally {
                sThemeSupportTested = true;
            }
        }

        if (sGetThemePackageName != null) {
            String themePackage;
            try {
                themePackage = (String)sGetThemePackageName.invoke(context.getResources().getAssets(),
                        sGetThemePackageNameArgs);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (TextUtils.isEmpty(themePackage)) {
                return null;
            }
            return themePackage;
        } else {
            return null;
        }
    }
}
