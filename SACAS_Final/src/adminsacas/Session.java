package adminsacas;

/**
 * Session – stores the currently logged-in user globally.
 *
 * Usage:
 *   Session.login(userId, "jdoe", "STUDENT", profileId, "Juan Doe");
 *   Session.getFullName()   → "Juan Doe"
 *   Session.getProfileId()  → students.id  OR  instructors.id
 *   Session.logout();
 */
public class Session {

    private static int    userId    = -1;
    private static String username  = "";
    private static String role      = "";   // ADMIN | STUDENT | INSTRUCTOR
    private static int    profileId = -1;   // students.id or instructors.id
    private static String fullName  = "";
    private static String extra     = "";   // course for student, dept for instructor

    public static void login(int userId, String username, String role,
                             int profileId, String fullName, String extra) {
        Session.userId    = userId;
        Session.username  = username;
        Session.role      = role;
        Session.profileId = profileId;
        Session.fullName  = fullName;
        Session.extra     = extra;
    }

    public static void logout() {
        userId = -1; username = ""; role = ""; profileId = -1; fullName = ""; extra = "";
    }

    public static int    getUserId()    { return userId;    }
    public static String getUsername()  { return username;  }
    public static String getRole()      { return role;      }
    public static int    getProfileId() { return profileId; }
    public static String getFullName()  { return fullName;  }
    public static String getExtra()     { return extra;     }

    public static boolean isAdmin()      { return "ADMIN".equals(role);      }
    public static boolean isStudent()    { return "STUDENT".equals(role);    }
    public static boolean isInstructor() { return "INSTRUCTOR".equals(role); }
}
