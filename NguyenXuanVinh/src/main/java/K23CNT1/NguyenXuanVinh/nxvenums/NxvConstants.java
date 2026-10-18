package K23CNT1.NguyenXuanVinh.nxvenums;

public final class NxvConstants {
    private NxvConstants() {}

    // Users.Gender
    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;

    // Users.Role
    public static final int ROLE_USER = 0;
    public static final int ROLE_MODERATOR = 1;
    public static final int ROLE_SUPER_ADMIN = 2;

    // NxvTasks.Status
    public static final int TASK_PENDING = 0;
    public static final int TASK_COMPLETED = 1;
    public static final int TASK_FAILED = 2;
    public static final int TASK_SKIPPED = 3;

    // NxvTasks.Difficulty
    public static final int DIFF_EASY = 1;
    public static final int DIFF_NORMAL = 2;
    public static final int DIFF_HARD = 3;

    // NxvUserPets.Stage
    public static final int PET_STAGE_EGG = 0;
    public static final int PET_STAGE_BABY = 1;
    public static final int PET_STAGE_ADULT = 2;

    // NxvItems.ItemType
    public static final int ITEM_EQUIPMENT = 0;
    public static final int ITEM_CONSUMABLE = 1;
    public static final int ITEM_AVATAR = 2;
    public static final int ITEM_AVATAR_FRAME = 3;
    public static final int ITEM_PET_EGG = 4;
    public static final int ITEM_TITLE = 5;
}