package hazem.nurmontage.videoquran.constant

/**
 * Enum representing all possible actions that can be performed on
 * timeline entities (Quran ayahs, translations, media clips, etc.).
 *
 * Used by the undo/redo system and the entity editing fragments
 * to identify which property of an entity was modified.
 */
enum class EntityAction {
    SPLIT,
    TRIM,
    MOVE,
    ADD,
    ROTATE,
    TO_BACK,
    TO_FRONT,
    LAYER,
    COLOR_OUTLINE_TEXT,
    SIZE_OUTLINE_TEXT,
    COLOR_TEXT,
    SHADOW_IMAGE,
    ROUND_IMAGE,
    COLOR_OUTLINE_IMG,
    SIZE_OUTLINE_IMG,
    COLOR_TACHKIL,
    BG_TEXT,
    OPACITY_IMAGE,
    OPACITY_TEXT,
    DELETE,
    DELETE_MULTIPLE,
    SHADOW_TEXT,
    TO_HORIZONTAL_RIGHT,
    TO_HORIZONTAL_LEFT,
    TO_HORIZONTAL_CENTER,
    TO_VERTICAL_CENTER,
    TO_VERTICAL_TOP,
    TO_VERTICAL_BOTTOM,
    FONT_TEXT,
    TEXT_SIZE,
    GLOW_SHADOW,
    ICON_QURAN,
    BOLD_STYLE,
    STRIKE_LINE_STYLE,
    ITALIC_STYLE,
    UNDERLINE_STYLE,
    ALINGMENT_STYLE,
    TIME_LINE_VIEW,
    MOTION_VIEW,
    MOTION_AND_TIME_VIEW,
    ANIMATION
}
