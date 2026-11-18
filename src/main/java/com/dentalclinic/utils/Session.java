package com.dentalclinic.utils;

import com.dentalclinic.model.User;

/** Holds the user who is currently logged in and the permissions that go with the role. */
public final class Session {
    private static User currentUser;

    private Session() {
    }

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /** Administrators and dentists may edit the price list. */
    public static boolean canManageCatalog() {
        return currentUser != null && (currentUser.isAdmin() || currentUser.isDentist());
    }

    /** Administrators and dentists may record treatments in the dental file. */
    public static boolean canRecordTreatments() {
        return canManageCatalog();
    }
}
