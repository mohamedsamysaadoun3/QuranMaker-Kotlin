/*
 * EngineActivity.kt — Thin UI Shell
 *
 * This is a structural skeleton of the original 8000-line EngineActivity.java.
 * It preserves the UI shell, callback wiring, and lifecycle structure while
 * delegating heavy logic (FFmpeg, timeline math) to dedicated classes.
 *
 * Migration notes:
 *   - Executor/Handler/Thread → Kotlin Coroutines (lifecycleScope + Dispatchers)
 *   - runOnUiThread → lifecycleScope.launch(Dispatchers.Main)
 *   - ViewBinding will replace findViewById once layout XML is migrated
 *   - FFmpeg command building is delegated to FfmpegCommandBuilder
 */
package hazem.nurmontage.videoquran.ui.engine

import android.content.Intent
import android.content.res.Resources
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.constant.AyaTextPreset
import hazem.nurmontage.videoquran.constant.EntityAction
import hazem.nurmontage.videoquran.constant.IpadType
import hazem.nurmontage.videoquran.constant.ResizeType
import hazem.nurmontage.videoquran.constant.SurahNameStyle
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.entity_timeline.EntityBismilahTimeline
import hazem.nurmontage.videoquran.entity_timeline.EntityQuranTimeline
import hazem.nurmontage.videoquran.entity_timeline.EntityTrslTimeline
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.utils.MyVibrationHelper
import hazem.nurmontage.videoquran.utils.TimeFormatter
import hazem.nurmontage.videoquran.views.TrackEntityView
// TODO: import hazem.nurmontage.videoquran.views.BlurredImageView
// TODO: import hazem.nurmontage.videoquran.views.CustomButtonCustumFont as ButtonCustumFont
// TODO: import hazem.nurmontage.videoquran.views.CustomTextCustumFont as TextCustumFont
// TODO: import hazem.nurmontage.videoquran.views.CustomDiscreteSeekBar
// TODO: import hazem.nurmontage.videoquran.views.SmoothVideoAnimator
// TODO: import hazem.nurmontage.videoquran.views.SmoothTimelineAnimator
// TODO: import hazem.nurmontage.videoquran.ui.engine.FfmpegCommandBuilder
// TODO: import hazem.nurmontage.videoquran.ui.engine.fragments.*
// TODO: import hazem.nurmontage.videoquran.ui.engine.adapters.DimensionAdabters
// TODO: import hazem.nurmontage.videoquran.ui.engine.adapters.RecitersModel
// TODO: import hazem.nurmontage.videoquran.persistence.LocalPersistence
// TODO: import hazem.nurmontage.videoquran.utils.FontProvider
// TODO: import hazem.nurmontage.videoquran.utils.ColorUtils
// TODO: import hazem.nurmontage.videoquran.utils.DrawableHelper
// TODO: import hazem.nurmontage.videoquran.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EngineActivity : BaseActivity() {

    // ============================================================
    // ViewBinding — TODO: switch to ViewBinding after layout XML migration
    // ============================================================
    // private lateinit var binding: ActivityTimeLineBinding

    // ============================================================
    // Animation
    // ============================================================
    // TODO: private lateinit var animator_frame_video: SmoothVideoAnimator
    // TODO: private lateinit var valueAnimator: SmoothTimelineAnimator

    // ============================================================
    // Views — using findViewById until ViewBinding is available
    // ============================================================
    // TODO: private lateinit var blurredImageView: BlurredImageView  // not yet migrated
    private lateinit var trackViewEntity: TrackEntityView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnUndo: ImageButton
    private lateinit var btnRedo: ImageButton
    private lateinit var btnToStart: ImageButton
    private lateinit var btnToEnd: ImageButton
    private lateinit var btn_cancel: ImageButton
    // TODO: private lateinit var btn_export: ButtonCustumFont
    // TODO: private lateinit var btnChangeResize: LinearLayout
    // TODO: private lateinit var btnIpod: LinearLayout
    // TODO: private lateinit var btn_setup_fps: LinearLayout
    // TODO: private lateinit var layout_resolution: LinearLayout
    // TODO: private lateinit var ivIpod: ImageView
    // TODO: private lateinit var ivResize: ImageView
    // TODO: private lateinit var seekBar_fps: CustomDiscreteSeekBar
    // TODO: private lateinit var seekBar_res: CustomDiscreteSeekBar
    private lateinit var tv_currentTime: TextView
    private lateinit var tv_endTime: TextView
    // TODO: private lateinit var tv_resolution: TextCustumFont
    // TODO: private lateinit var tv_tittle_fragment: TextCustumFont
    // TODO: private lateinit var textChangeResize: TextCustumFont

    // ============================================================
    // State
    // ============================================================
    private var mIsPlaying: Boolean = false
    private var isOnScroll: Boolean = false
    private var isToCrop: Boolean = false
    private var oneExport: Boolean = false
    private var isSaveTmpTemplate: Boolean = true
    private var mCurrentFragment: Fragment? = null
    private var mTemplate: Template? = null
    private var mPlayer: MediaPlayer? = null
    private var mResources: Resources? = null
    private var entityAudio_player: EntityAudio? = null
    private var entityAudio_visible: EntityAudio? = null
    private var uri_bg: String? = null
    private var endFrame: Int = 0
    private var endTimeAudioVisible: Int = 0
    private var lastIndexVisible: Int = 0
    private var current_position_time: Int = 0
    private var startCursur: Int = 0

    // ============================================================
    // FFmpeg session tracking
    // ============================================================
    private val id_ffmpeg = mutableListOf<Long>()

    // ============================================================
    // Helpers — initialized in onCreate
    // ============================================================
    private var vibrationHelper: MyVibrationHelper? = null
    private lateinit var timeFormatter: TimeFormatter

    // ============================================================
    // FFmpeg command builder — delegates all command construction
    // ============================================================
    // TODO: private lateinit var ffmpegCommandBuilder: FfmpegCommandBuilder

    // ============================================================
    // OnBackPressedCallback
    // ============================================================
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (mCurrentFragment != null) {
                hideFragment()
            } else {
                dialog()
            }
        }
    }

    // ============================================================
    // ActivityResultLaunchers
    // ============================================================

    /** Launcher for QuranSearchActivity — search/select an ayah */
    private val searchAyaResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                // TODO: Extract ayah data from result intent and add entity
                // val ayaText = data.getStringExtra("aya_text")
                // val ayaNumber = data.getIntExtra("aya_number", 0)
                // val surahNumber = data.getIntExtra("surah_number", 0)
                // addEntity(ayaText, ayaNumber, surahNumber)
            }
        }

    /** Launcher for AddReaderNameActivity — select reciter name overlay */
    private val nameReaderResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                // TODO: Extract reader name and apply to template
                // val readerName = data.getStringExtra("reader_name")
                // mTemplate?.readerName = readerName
            }
        }

    /** Launcher for EditS_NameActivity — edit surah name style/text */
    private val editSurahNameResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                // TODO: Extract surah name edits and apply
                // val surahNameStyle = data.getSerializableExtra("surah_name_style") as? SurahNameStyle
                // val surahNameText = data.getStringExtra("surah_name_text")
                // updateSurahName(surahNameStyle, surahNameText)
            }
        }

    /** Launcher for translation edit activity */
    private val editTrslResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                // TODO: Extract translation edits and apply
                // val trslText = data.getStringExtra("trsl_text")
                // val trslNumber = data.getIntExtra("trsl_number", 0)
                // addTranslationEntity(trslText, trslNumber, false)
            }
        }

    /** Launcher for background selection activity */
    private val launchChoiceBgActivity: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                // TODO: Extract selected background and apply
                // val bgUri = data.getStringExtra("bg_uri")
                // val bgType = data.getIntExtra("bg_type", 0)
                // changeBackground(bgUri, bgType)
            }
        }

    /** Launcher for crop activity */
    private val launchCropActivity: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                // TODO: Extract cropped image URI and apply
                // val croppedUri = data.getStringExtra("cropped_uri")
                // applyCroppedImage(croppedUri)
            }
        }

    /** Launcher for image picker */
    private val launchImg: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val uri = data.data ?: return@registerForActivityResult
                // TODO: Handle picked image — set as background or entity image
                // handlePickedImage(uri)
            }
        }

    /** Launcher for video picker */
    private val launchVideo: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val uri = data.data ?: return@registerForActivityResult
                // TODO: Handle picked video — set as background or extract audio
                // handlePickedVideo(uri)
            }
        }

    /** Launcher for video audio extraction */
    private val launchVideoExtract: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val uri = data.data ?: return@registerForActivityResult
                // TODO: Extract audio track from video and add as entity
                // val path = FileUtils.getPath(this, uri)
                // addAudioFromVideo(uri, path)
            }
        }

    // ============================================================
    // Callback interfaces — wired to fragment interactions
    // ============================================================

    /**
     * Timeline trim/callback interface — handles all interactions
     * from TrackEntityView (entity selection, deletion, seeking, etc.)
     */
    private val iTrimLineCallback = object : TrackEntityView.ITrimLineCallback {
        override fun onSeekPlayer(frame: Int) {
            // TODO: Seek the player and timeline to the specified frame
            current_position_time = frame
            updateFrame()
            updateTime()
        }

        override fun onSelectEntity(entity: Any, index: Int) {
            // TODO: Handle entity selection on the timeline
            // Show entity-specific editing fragment
            lastIndexVisible = index
            updateBtnCutState()
        }

        override fun onDelete(entity: Any) {
            // TODO: Handle entity deletion from timeline
            vibrationHelper?.vibrate()
            // Remove entity and refresh timeline
        }

        override fun onEmptySelect() {
            // TODO: Deselect all entities
            updateBtnCutState()
        }

        override fun onEntityMove(fromIndex: Int, toIndex: Int) {
            // TODO: Handle entity reordering on the timeline
        }

        override fun onEntityResize(entity: Any, newStart: Int, newEnd: Int) {
            // TODO: Handle entity resize on the timeline
        }

        override fun onEntitySplit(entity: Any, splitPoint: Int) {
            // TODO: Handle entity split at the given point
        }

        override fun onScrollStateChanged(isScrolling: Boolean) {
            isOnScroll = isScrolling
        }

        override fun getEndTime(): Int {
            return endTimeAudioVisible
        }

        override fun getCurrentPosition(): Int {
            return current_position_time
        }
    }

    /**
     * AddQuran fragment callback — handles adding Quran ayahs
     */
    // TODO: Uncomment when AddQuranFragment is migrated
    // private val iAddQuran = object : AddQuranFragment.IAddQuran {
    //     override fun onAddQuran(ayaText: String, ayaNumber: Int, surahNumber: Int) {
    //         addEntity(ayaText, ayaNumber, surahNumber)
    //     }
    //     override fun onAddIste3adha() {
    //         addEntityIste3adha()
    //     }
    //     override fun onAddBismilah() {
    //         addEntityBissmilah()
    //     }
    //     override fun onCancel() {
    //         hideFragment()
    //     }
    // }

    /**
     * Audio fragment callback — handles adding audio tracks
     */
    // TODO: Uncomment when AddAudioFragment is migrated
    // private val iAudioCallback = object : AddAudioFragment.IAudioCallback {
    //     override fun onAddAudio(uri: Uri) {
    //         addAudio(uri)
    //     }
    //     override fun onAddAudioFromVideo(uri: Uri, path: String) {
    //         addAudioFromVideo(uri, path)
    //     }
    //     override fun onAddAudioReciters(list: List<RecitersModel>) {
    //         addAudioReciters(list)
    //     }
    //     override fun onCancel() {
    //         hideFragment()
    //     }
    // }

    /**
     * iPad edit fragment callback — handles iPad overlay edits
     */
    // TODO: Uncomment when EditIpadFragment is migrated
    // private val iIpadEditCallback = object : EditIpadFragment.IIpadEditCallback {
    //     override fun onIpadTypeChanged(ipadType: IpadType) {
    //         mTemplate?.ipadType = ipadType
    //         updateFrame()
    //     }
    //     override fun onIpadVisibilityChanged(visible: Boolean) {
    //         mTemplate?.ipadVisible = visible
    //         updateFrame()
    //     }
    //     override fun onCancel() {
    //         hideFragment()
    //     }
    // }

    /**
     * Background change fragment callback
     */
    // TODO: Uncomment when ChangeBgFragment is migrated
    // private val iChangeBgCallback = object : ChangeBgFragment.IChangeBgCallback {
    //     override fun onBgColorSelected(color: Int) {
    //         mTemplate?.bgColor = color
    //         uri_bg = null
    //         updateFrame()
    //     }
    //     override fun onBgImageSelected(uri: String) {
    //         uri_bg = uri
    //         updateFrame()
    //     }
    //     override fun onBgVideoSelected(uri: String) {
    //         uri_bg = uri
    //         updateFrame()
    //     }
    //     override fun onBgGradientSelected(colors: IntArray, angle: Float) {
    //         mTemplate?.bgGradientColors = colors.toList()
    //         mTemplate?.bgGradientAngle = angle
    //         uri_bg = null
    //         updateFrame()
    //     }
    //     override fun onCancel() {
    //         hideFragment()
    //     }
    // }

    /**
     * Dimension adapter callback — handles resolution/aspect ratio changes
     */
    // TODO: Uncomment when DimensionAdabters is migrated
    // private val iDimensionCallback = object : DimensionAdabters.IDimensionCallback {
    //     override fun onDimensionChanged(width: Int, height: Int) {
    //         mTemplate?.width = width
    //         mTemplate?.height = height
    //         updateFrame()
    //     }
    //     override fun onCancel() {
    //         hideFragment()
    //     }
    // }

    /**
     * Quran icon edit callback
     */
    // TODO: Uncomment when EditIconQuranFragment is migrated
    // private val iQuranIconCallback = object : EditIconQuranFragment.IQuranIconCallback {
    //     override fun onIconChanged(iconResId: Int) {
    //         mTemplate?.quranIconResId = iconResId
    //         updateFrame()
    //     }
    //     override fun onIconVisibilityChanged(visible: Boolean) {
    //         mTemplate?.quranIconVisible = visible
    //         updateFrame()
    //     }
    //     override fun onCancel() {
    //         hideFragment()
    //     }
    // }

    /**
     * Surah name edit callback
     */
    // TODO: Uncomment when EditS_NameFragment is migrated
    // private val iEditSName = object : EditS_NameFragment.IEditS_Name {
    //     override fun onSurahNameStyleChanged(style: SurahNameStyle) {
    //         mTemplate?.surahNameStyle = style
    //         updateFrame()
    //     }
    //     override fun onSurahNameTextChanged(text: String) {
    //         mTemplate?.surahNameText = text
    //         updateFrame()
    //     }
    //     override fun onCancel() {
    //         hideFragment()
    //     }
    // }

    /**
     * Font selection callback
     */
    // TODO: Uncomment when FontFragment is migrated
    // private val iFontCallback = object : FontFragment.IFontCallback {
    //     override fun onFontSelected(fontPath: String, fontSize: Float) {
    //         mTemplate?.fontPath = fontPath
    //         mTemplate?.fontSize = fontSize
    //         updateFrame()
    //     }
    //     override fun onCancel() {
    //         hideFragment()
    //     }
    // }

    // ============================================================
    // Lifecycle
    // ============================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge display
        // TODO: EdgeToEdge.enable(this) — requires androidx.activity:activity-ktx 1.8+

        // TODO: Switch to ViewBinding after layout XML migration:
        //   binding = ActivityTimeLineBinding.inflate(layoutInflater)
        //   setContentView(binding.root)
        setContentView(R.layout.activity_time_line)

        // Register back press handler
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // System bar appearance
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        // System bar insets padding
        // TODO: Replace R.id.main with binding.main after ViewBinding migration
        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                insets
            }
        }

        mResources = resources
        setStatusBarColor(-15658735)   // Dark green status bar
        setNavigationBarColor(-14935010) // Slightly different dark green nav bar
        wakeLockAquire()

        // Initialize helpers
        timeFormatter = TimeFormatter()
        vibrationHelper = MyVibrationHelper(this)

        // Startup sequence
        showProgress()
        loadTemplate()
        initLauncher()
        initTimeLineView()
        initViews()
        checkUriShared()
    }

    override fun onResume() {
        super.onResume()
        // TODO: Resume timeline animation if was playing
        // if (mIsPlaying) startTimelineAnimation()
    }

    override fun onPause() {
        super.onPause()
        // TODO: Pause playback and animation
        pausePlayer()
        pauseTimelineAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release media player
        mPlayer?.release()
        mPlayer = null
        // Cancel any running FFmpeg sessions
        cancelFfmpegSessions()
    }

    // ============================================================
    // Template Loading
    // ============================================================

    /**
     * Load the template from local persistence.
     * If a template URI was passed via intent, load from that;
     * otherwise load the last-saved temporary template.
     */
    private fun loadTemplate() {
        // TODO: Implement template loading
        // lifecycleScope.launch(Dispatchers.IO) {
        //     val templateUri = intent.getStringExtra("template_uri")
        //     mTemplate = if (templateUri != null) {
        //         LocalPersistence.loadTemplate(this@EngineActivity, templateUri)
        //     } else {
        //         LocalPersistence.loadTmpTemplate(this@EngineActivity)
        //     }
        //     withContext(Dispatchers.Main) {
        //         if (mTemplate == null) {
        //             mTemplate = Template() // Create default
        //         }
        //         hideProgressFragment()
        //         initTemplateViews()
        //     }
        // }
    }

    // ============================================================
    // Launcher Initialization
    // ============================================================

    /**
     * Register any ActivityResultLaunchers that couldn't be registered
     * as field initializers (e.g., those needing dynamic data).
     * Most launchers are already registered as class-level properties.
     */
    private fun initLauncher() {
        // All launchers are registered as field initializers above.
        // This method exists for parity with Java source and for any
        // future launchers that need dynamic registration.
        // TODO: Add any dynamically-registered launchers here
    }

    // ============================================================
    // Timeline View Initialization
    // ============================================================

    /**
     * Initialize the TrackEntityView with its callback and
     * set up the timeline scrubber/track UI.
     */
    private fun initTimeLineView() {
        // TODO: Uncomment when TrackEntityView is migrated
        // trackViewEntity = findViewById(R.id.trackViewEntity)
        // trackViewEntity.setTrimLineCallback(iTrimLineCallback)
        // trackViewEntity.setTemplate(mTemplate)
        // trackViewEntity.setMaxDuration(endTimeAudioVisible)
    }

    // ============================================================
    // View Initialization & Click Listeners
    // ============================================================

    /**
     * Bind all views via findViewById and set up click listeners.
     * TODO: Replace all findViewById with ViewBinding references
     *       after layout XML migration.
     */
    private fun initViews() {
        // --- Playback controls ---
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnPlayPause.setOnClickListener {
            if (mIsPlaying) pausePlayer() else playPlayer()
        }

        btnToStart = findViewById(R.id.btn_to_start)
        btnToStart.setOnClickListener {
            seekToStart()
        }

        btnToEnd = findViewById(R.id.btn_to_end)
        btnToEnd.setOnClickListener {
            seekToEnd()
        }

        // --- Undo / Redo ---
        btnUndo = findViewById(R.id.btn_undo)
        btnUndo.setOnClickListener {
            performUndo()
        }

        btnRedo = findViewById(R.id.btn_redo)
        btnRedo.setOnClickListener {
            performRedo()
        }

        // --- Cancel / Export ---
        btn_cancel = findViewById(R.id.btn_cancel)
        btn_cancel.setOnClickListener {
            dialog()
        }

        // TODO: btn_export = findViewById(R.id.btn_export)
        // btn_export.setOnClickListener { toExport() }

        // --- Toolbar option buttons ---
        // TODO: btnChangeResize = findViewById(R.id.btn_change_resize)
        // btnChangeResize.setOnClickListener { onChangeResizeClicked() }

        // TODO: btnIpod = findViewById(R.id.btn_ipod)
        // btnIpod.setOnClickListener { onIpodClicked() }

        // TODO: btn_setup_fps = findViewById(R.id.btn_setup_fps)
        // btn_setup_fps.setOnClickListener { onSetupFpsClicked() }

        // TODO: layout_resolution = findViewById(R.id.layout_resolution)
        // layout_resolution.setOnClickListener { onResolutionClicked() }

        // --- Time labels ---
        tv_currentTime = findViewById(R.id.tv_current_time)
        tv_endTime = findViewById(R.id.tv_end_time)

        // --- SeekBars ---
        // TODO: seekBar_fps = findViewById(R.id.seekBar_fps)
        // seekBar_fps.setOnProgressChangeListener { progress ->
        //     mTemplate?.fps = progress
        //     updateTime()
        // }

        // TODO: seekBar_res = findViewById(R.id.seekBar_res)
        // seekBar_res.setOnProgressChangeListener { progress ->
        //     onResolutionProgressChanged(progress)
        // }

        // --- Initial button states ---
        disableUndoBtn()
        disableRedoBtn()
        updateBtnToStart()
        updateBtnToEnd()
    }

    // ============================================================
    // Intent / Shared URI Handling
    // ============================================================

    /**
     * Check if this activity was launched with a shared URI
     * (e.g., from another app sharing an image/video to QuranMaker).
     */
    private fun checkUriShared() {
        val action = intent?.action
        if (Intent.ACTION_SEND == action) {
            val sharedUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (sharedUri != null) {
                // TODO: Handle shared URI — set as background or add to timeline
                // handlePickedImage(sharedUri)
            }
        } else if (Intent.ACTION_VIEW == action) {
            val dataUri = intent?.data
            if (dataUri != null) {
                // TODO: Handle view intent — load as template or project
                // loadTemplateFromUri(dataUri)
            }
        }
    }

    // ============================================================
    // Media Playback Control
    // ============================================================

    /**
     * Start or resume playback of the current audio entity.
     */
    private fun playPlayer() {
        if (mPlayer == null && entityAudio_player == null) return

        mIsPlaying = true
        btnPlayPause.setImageResource(R.drawable.ic_pause) // TODO: verify icon name

        // Start audio playback
        mPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
            }
        }

        startTimelineAnimation()
    }

    /**
     * Pause the current audio playback.
     */
    private fun pausePlayer() {
        mIsPlaying = false
        btnPlayPause.setImageResource(R.drawable.ic_play) // TODO: verify icon name

        mPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            }
        }

        pauseTimelineAnimation()
    }

    /**
     * Fully stop playback and reset position.
     */
    private fun stop() {
        mIsPlaying = false
        btnPlayPause.setImageResource(R.drawable.ic_play) // TODO: verify icon name

        mPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.prepareAsync()
        }

        pauseTimelineAnimation()
        current_position_time = 0
        updateTime()
    }

    /**
     * Seek playback to the start position.
     */
    private fun seekToStart() {
        mPlayer?.seekTo(0)
        current_position_time = 0
        updateFrame()
        updateTime()
        updateBtnToStart()
    }

    /**
     * Seek playback to the end position.
     */
    private fun seekToEnd() {
        val endMs = endTimeAudioVisible
        mPlayer?.seekTo(endMs)
        current_position_time = endMs
        updateFrame()
        updateTime()
        updateBtnToEnd()
    }

    // ============================================================
    // Timeline Animation
    // ============================================================

    /**
     * Start the timeline scrubber animation synchronized with playback.
     */
    private fun startTimelineAnimation() {
        // TODO: Implement with SmoothTimelineAnimator
        // valueAnimator.start(current_position_time, endTimeAudioVisible) { frame ->
        //     current_position_time = frame
        //     updateViewTime(endTimeAudioVisible, frame)
        //     trackViewEntity.setCurrentPosition(frame)
        // }
    }

    /**
     * Pause the timeline scrubber animation.
     */
    private fun pauseTimelineAnimation() {
        // TODO: valueAnimator.pause()
    }

    // ============================================================
    // Time Display Updates
    // ============================================================

    /**
     * Update the current time display from the player position.
     */
    private fun updateTime() {
        val ms = current_position_time
        tv_currentTime.text = timeFormatter.format(ms)
        tv_endTime.text = timeFormatter.format(endTimeAudioVisible)
    }

    /**
     * Update the current time display with a specific millisecond value.
     */
    private fun updateTime(ms: Int) {
        current_position_time = ms
        tv_currentTime.text = timeFormatter.format(ms)
    }

    /**
     * Update both max and current time labels.
     */
    private fun updateViewTime(max: Int, current: Int) {
        tv_currentTime.text = timeFormatter.format(current)
        tv_endTime.text = timeFormatter.format(max)
    }

    // ============================================================
    // Frame Rendering
    // ============================================================

    /**
     * Refresh the current video frame preview based on
     * the current timeline position and template state.
     */
    private fun updateFrame() {
        // TODO: Render the current frame using SmoothVideoAnimator
        // This must draw:
        //   - Background (color/image/video frame)
        //   - Quran text entities
        //   - Translation text entities
        //   - Bismilah / Iste3adha entities
        //   - iPad overlay (if enabled)
        //   - Surah name (if enabled)
        //   - Quran icon (if enabled)
        // animator_frame_video.renderFrame(mTemplate, current_position_time)
    }

    // ============================================================
    // Button State Updates
    // ============================================================

    /**
     * Update the cut/delete button enabled state based on selection.
     */
    private fun updateBtnCutState() {
        // TODO: Enable/disable cut button based on whether an entity is selected
        // val hasSelection = trackViewEntity.hasSelectedEntity()
        // btn_cut.isEnabled = hasSelection
        // btn_cut.alpha = if (hasSelection) 1.0f else 0.4f
    }

    /**
     * Update the "seek to start" button enabled state.
     */
    private fun updateBtnToStart() {
        btnToStart.isEnabled = current_position_time > 0
        btnToStart.alpha = if (current_position_time > 0) 1.0f else 0.4f
    }

    /**
     * Update the "seek to end" button enabled state.
     */
    private fun updateBtnToEnd() {
        val atEnd = current_position_time >= endTimeAudioVisible
        btnToEnd.isEnabled = !atEnd
        btnToEnd.alpha = if (!atEnd) 1.0f else 0.4f
    }

    // --- Undo / Redo button states ---

    private fun enableUndoBtn() {
        btnUndo.isEnabled = true
        btnUndo.alpha = 1.0f
    }

    private fun disableUndoBtn() {
        btnUndo.isEnabled = false
        btnUndo.alpha = 0.4f
    }

    private fun enableRedoBtn() {
        btnRedo.isEnabled = true
        btnRedo.alpha = 1.0f
    }

    private fun disableRedoBtn() {
        btnRedo.isEnabled = false
        btnRedo.alpha = 0.4f
    }

    // ============================================================
    // Undo / Redo Operations
    // ============================================================

    private fun performUndo() {
        // TODO: Delegate to undo/redo manager
        // undoRedoManager.undo()
        // updateFrame()
        // updateBtnCutState()
        // enableRedoBtn()
        // if (!undoRedoManager.canUndo()) disableUndoBtn()
    }

    private fun performRedo() {
        // TODO: Delegate to undo/redo manager
        // undoRedoManager.redo()
        // updateFrame()
        // updateBtnCutState()
        // enableUndoBtn()
        // if (!undoRedoManager.canRedo()) disableRedoBtn()
    }

    // ============================================================
    // Fragment Management
    // ============================================================

    /**
     * Show a fragment in the bottom sheet / panel area.
     * @param fragment The fragment to display
     * @param title Optional title to show in the fragment title bar
     */
    private fun showFragment(fragment: Fragment, title: String?) {
        mCurrentFragment = fragment
        setupShowFragment(title)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // TODO: verify container ID
            .commitAllowingStateLoss()
    }

    /**
     * Hide the currently shown fragment.
     */
    private fun hideFragment() {
        mCurrentFragment = null

        supportFragmentManager.beginTransaction()
            .apply {
                supportFragmentManager.findFragmentById(R.id.fragment_container)?.let {
                    remove(it)
                }
            }
            .commitAllowingStateLoss()

        // TODO: Hide the fragment title bar
        // tv_tittle_fragment.visibility = View.GONE
        // binding.fragmentTitleBar.visibility = View.GONE
    }

    /**
     * Set up the fragment title bar when showing a fragment.
     * @param title The title to display
     */
    private fun setupShowFragment(title: String?) {
        // TODO: Show and configure the fragment title bar
        // tv_tittle_fragment.visibility = View.VISIBLE
        // tv_tittle_fragment.text = title ?: ""
        // binding.fragmentTitleBar.visibility = View.VISIBLE
    }

    // ============================================================
    // Progress Indicator
    // ============================================================

    /**
     * Show a progress/loading indicator (typically during template load or export).
     */
    private fun showProgress() {
        // TODO: Show progress fragment or loading overlay
        // binding.progressOverlay.visibility = View.VISIBLE
    }

    /**
     * Hide the progress/loading indicator.
     */
    private fun hideProgressFragment() {
        // TODO: Hide progress fragment or loading overlay
        // binding.progressOverlay.visibility = View.GONE
    }

    // ============================================================
    // Dialogs
    // ============================================================

    /**
     * Exit confirmation dialog — warns the user about unsaved changes.
     */
    private fun dialog() {
        if (isSaveTmpTemplate) {
            saveTmpTemplate()
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.app_name) // TODO: use proper dialog title string
            .setMessage("Are you sure you want to exit? Unsaved changes will be lost.")
            .setPositiveButton("Exit") { _, _ ->
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Premium upsell dialog — shown when a premium feature is attempted
     * without a valid premium license.
     * @param code The feature code that triggered the premium gate
     */
    private fun dialogPremium(code: Int) {
        // TODO: Implement premium dialog with feature-specific messaging
        // AlertDialog.Builder(this)
        //     .setTitle("Premium Feature")
        //     .setMessage("This feature requires a premium subscription. Code: $code")
        //     .setPositiveButton("Upgrade") { _, _ ->
        //         // Navigate to premium/upgrade screen
        //     }
        //     .setNegativeButton("Cancel", null)
        //     .show()
    }

    /**
     * Copyright notice dialog — shown when user attempts to use
     * copyrighted content without permission.
     */
    private fun dialogCopyRight() {
        // TODO: Implement copyright warning dialog
        // AlertDialog.Builder(this)
        //     .setTitle("Copyright Notice")
        //     .setMessage("This content is protected by copyright. Please ensure you have the right to use it.")
        //     .setPositiveButton("Understood", null)
        //     .show()
    }

    /**
     * No internet connection dialog — with retry option.
     * @param uri The URI that was being accessed when connectivity was lost
     */
    private fun dialogNoInternet(uri: Uri) {
        // TODO: Implement no-internet dialog with retry
        // AlertDialog.Builder(this)
        //     .setTitle("No Internet Connection")
        //     .setMessage("An internet connection is required to download this resource.")
        //     .setPositiveButton("Retry") { _, _ ->
        //         // Retry the network request with the given URI
        //     }
        //     .setNegativeButton("Cancel", null)
        //     .show()
    }

    /**
     * No internet connection dialog for reciters list — with retry option.
     * @param list The list of reciters that failed to load
     */
    // TODO: Uncomment when RecitersModel is migrated
    // private fun dialogNoInternetList(list: List<RecitersModel>) {
    //     AlertDialog.Builder(this)
    //         .setTitle("No Internet Connection")
    //         .setMessage("Unable to load reciters list. Please check your connection.")
    //         .setPositiveButton("Retry") { _, _ ->
    //             // Retry loading the reciters list
    //         }
    //         .setNegativeButton("Cancel", null)
    //         .show()
    // }

    // ============================================================
    // Entity Creation
    // ============================================================

    /**
     * Add a Quran ayah entity to the timeline.
     * @param ayaText The ayah text
     * @param ayaNumber The ayah number within the surah
     * @param surahNumber The surah number
     */
    private fun addEntity(ayaText: String, ayaNumber: Int, surahNumber: Int) {
        // TODO: Create EntityQuranTimeline and add to track
        // val entity = EntityQuranTimeline().apply {
        //     text = ayaText
        //     this.ayaNumber = ayaNumber
        //     this.surahNumber = surahNumber
        //     startTime = current_position_time
        //     endTime = current_position_time + DEFAULT_AYA_DURATION
        // }
        // trackViewEntity.addEntity(entity)
        // updateFrame()
        // enableUndoBtn()
    }

    /**
     * Add a translation entity to the timeline.
     * @param text The translation text
     * @param number The ayah number
     * @param isQuran Whether this is a Quran translation (vs. regular text)
     */
    private fun addEntityTrsl(text: String, number: Int, isQuran: Boolean) {
        // TODO: Create EntityTrslTimeline and add to track
        // val entity = EntityTrslTimeline().apply {
        //     this.text = text
        //     this.number = number
        //     this.isQuran = isQuran
        //     startTime = current_position_time
        //     endTime = current_position_time + DEFAULT_TRSL_DURATION
        // }
        // trackViewEntity.addEntity(entity)
        // updateFrame()
        // enableUndoBtn()
    }

    /**
     * Add an Iste3adha (seeking refuge) entity to the timeline.
     */
    private fun addEntityIste3adha() {
        // TODO: Create EntityQuranTimeline with Iste3adha text and add to track
        // val entity = EntityQuranTimeline().apply {
        //     text = "أَعُوذُ بِاللهِ مِنَ الشَّيْطَانِ الرَّجِيمِ"
        //     entityAction = EntityAction.ISTEADHA
        //     startTime = current_position_time
        //     endTime = current_position_time + DEFAULT_ISTE3ADHA_DURATION
        // }
        // trackViewEntity.addEntity(entity)
        // updateFrame()
        // enableUndoBtn()
    }

    /**
     * Add a Bismilah entity to the timeline.
     */
    private fun addEntityBissmilah() {
        // TODO: Create EntityBismilahTimeline and add to track
        // val entity = EntityBismilahTimeline().apply {
        //     startTime = current_position_time
        //     endTime = current_position_time + DEFAULT_BISMILAH_DURATION
        // }
        // trackViewEntity.addEntity(entity)
        // updateFrame()
        // enableUndoBtn()
    }

    /**
     * Add a translation entity with specific parameters.
     * @param text The translation text
     * @param number The ayah/verse number
     * @param isQuran Whether the source is Quran text
     */
    private fun addTranslationEntity(text: String, number: Int, isQuran: Boolean) {
        addEntityTrsl(text, number, isQuran)
    }

    // ============================================================
    // Audio Management
    // ============================================================

    /**
     * Add an audio file from a URI to the timeline.
     * @param uri The URI of the audio file
     */
    private fun addAudio(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            // TODO: Copy audio file to app storage and create EntityAudio
            // val audioPath = FileUtils.copyToInternal(this@EngineActivity, uri)
            // val duration = FileUtils.getAudioDuration(audioPath)
            // val entity = EntityAudio().apply {
            //     this.uri = uri.toString()
            //     this.path = audioPath
            //     this.duration = duration
            // }
            // withContext(Dispatchers.Main) {
            //     trackViewEntity.addEntity(entity)
            //     entityAudio_visible = entity
            //     endTimeAudioVisible = duration
            //     updateFrame()
            //     updateTime()
            // }
        }
    }

    /**
     * Extract and add audio from a video file.
     * @param uri The URI of the video file
     * @param path The local file path of the video
     */
    private fun addAudioFromVideo(uri: Uri, path: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // TODO: Use FfmpegCommandBuilder to extract audio from video
            // val outputPath = FileUtils.getAudioCachePath(this@EngineActivity)
            // val command = ffmpegCommandBuilder.buildExtractAudioCommand(path, outputPath)
            // FFmpeg.execute(command)
            //
            // val duration = FileUtils.getAudioDuration(outputPath)
            // val entity = EntityAudio().apply {
            //     this.uri = uri.toString()
            //     this.path = outputPath
            //     this.duration = duration
            // }
            // withContext(Dispatchers.Main) {
            //     trackViewEntity.addEntity(entity)
            //     entityAudio_visible = entity
            //     endTimeAudioVisible = duration
            //     updateFrame()
            //     updateTime()
            // }
        }
    }

    /**
     * Add audio from a reciters list (downloaded from server).
     * @param list The list of reciters to add
     */
    // TODO: Uncomment when RecitersModel is migrated
    // private fun addAudioReciters(list: List<RecitersModel>) {
    //     lifecycleScope.launch(Dispatchers.IO) {
    //         // TODO: Download audio from reciter URL and add to timeline
    //         // for (reciter in list) {
    //         //     val audioPath = downloadReciterAudio(reciter)
    //         //     val duration = FileUtils.getAudioDuration(audioPath)
    //         //     val entity = EntityAudio().apply {
    //         //         this.uri = reciter.url
    //         //         this.path = audioPath
    //         //         this.duration = duration
    //         //     }
    //         //     withContext(Dispatchers.Main) {
    //         //         trackViewEntity.addEntity(entity)
    //         //     }
    //         // }
    //         // withContext(Dispatchers.Main) {
    //         //     hideProgressFragment()
    //         //     updateFrame()
    //         //     updateTime()
    //         // }
    //     }
    // }

    // ============================================================
    // Surah Name Selection
    // ============================================================

    /**
     * Open surah name selection — either as a fragment or activity.
     */
    private fun selectSurahName() {
        // TODO: Show surah name selection fragment
        // val fragment = EditS_NameFragment.newInstance(mTemplate?.surahNameStyle)
        // showFragment(fragment, "Edit Surah Name")
    }

    // ============================================================
    // Export
    // ============================================================

    /**
     * Navigate to the export progress activity.
     * Builds the FFmpeg command via FfmpegCommandBuilder and passes it
     * to ProgressViewActivity.
     */
    private fun toExport() {
        if (oneExport) return  // Prevent double-tap
        oneExport = true

        val template = mTemplate ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            // TODO: Build FFmpeg command using FfmpegCommandBuilder
            // val command = ffmpegCommandBuilder.buildExportCommand(
            //     template = template,
            //     audioPath = entityAudio_visible?.path,
            //     bgPath = uri_bg,
            //     outputPath = FileUtils.getExportPath(this@EngineActivity, template)
            // )

            withContext(Dispatchers.Main) {
                // TODO: Launch ProgressViewActivity with the command
                // val intent = Intent(this@EngineActivity, ProgressViewActivity::class.java).apply {
                //     putExtra("ffmpeg_command", command.toTypedArray())
                //     putExtra("output_path", command.outputPath)
                //     putExtra("template", template)
                // }
                // startActivity(intent)
                oneExport = false
            }
        }
    }

    // ============================================================
    // Template Persistence
    // ============================================================

    /**
     * Save the current template state as a temporary file.
     * Called before exit and before showing certain fragments
     * to preserve the user's work.
     */
    private fun saveTmpTemplate() {
        val template = mTemplate ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            // TODO: Save template to local persistence
            // LocalPersistence.saveTmpTemplate(this@EngineActivity, template)
        }
    }

    // ============================================================
    // Timeline Scroll
    // ============================================================

    /**
     * Scroll the timeline to the end of the current ayah.
     * Used after adding an entity to position the cursor
     * at the new entity's end time.
     */
    private fun updateTimeToEndAya() {
        // TODO: Calculate end time of the last entity and scroll
        // val lastEntity = trackViewEntity.getLastEntity()
        // if (lastEntity != null) {
        //     current_position_time = lastEntity.endTime
        //     trackViewEntity.setCurrentPosition(current_position_time)
        //     updateFrame()
        //     updateTime()
        // }
    }

    // ============================================================
    // Toolbar Actions
    // ============================================================

    /**
     * Handle resize mode change button click.
     * Cycles through available resize types or opens a selector.
     */
    private fun onChangeResizeClicked() {
        // TODO: Show resize type selector (Fit, Fill, Crop, etc.)
        // val resizeTypes = ResizeType.values()
        // val current = mTemplate?.resizeType ?: ResizeType.FIT
        // val nextIndex = (resizeTypes.indexOf(current) + 1) % resizeTypes.size
        // mTemplate?.resizeType = resizeTypes[nextIndex]
        // textChangeResize.text = resizeTypes[nextIndex].displayName
        // updateFrame()
    }

    /**
     * Handle iPad overlay toggle button click.
     * Opens the iPad edit fragment or toggles visibility.
     */
    private fun onIpodClicked() {
        // TODO: Toggle iPad overlay or open edit fragment
        // val fragment = EditIpadFragment.newInstance(mTemplate?.ipadType ?: IpadType.NONE)
        // showFragment(fragment, "Edit iPad Frame")
    }

    /**
     * Handle FPS setup button click.
     * Shows or toggles the FPS seekbar.
     */
    private fun onSetupFpsClicked() {
        // TODO: Show/hide FPS seekbar
        // seekBar_fps.visibility = if (seekBar_fps.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    /**
     * Handle resolution layout click.
     * Shows or toggles the resolution selector.
     */
    private fun onResolutionClicked() {
        // TODO: Show resolution selection fragment or seekbar
        // seekBar_res.visibility = if (seekBar_res.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    /**
     * Handle resolution seekbar progress change.
     */
    private fun onResolutionProgressChanged(progress: Int) {
        // TODO: Map progress to resolution and update template
        // val resolutions = listOf(Pair(720, 1280), Pair(1080, 1920), Pair(1080, 1080))
        // val (w, h) = resolutions.getOrElse(progress) { resolutions.first() }
        // mTemplate?.width = w
        // mTemplate?.height = h
        // tv_resolution.text = "${w}x${h}"
        // updateFrame()
    }

    // ============================================================
    // FFmpeg Session Management
    // ============================================================

    /**
     * Track an FFmpeg session ID for later cancellation.
     */
    private fun trackFfmpegSession(sessionId: Long) {
        id_ffmpeg.add(sessionId)
    }

    /**
     * Cancel all running FFmpeg sessions.
     */
    private fun cancelFfmpegSessions() {
        // TODO: Cancel all tracked FFmpeg sessions
        // for (id in id_ffmpeg) {
        //     FFmpeg.cancel(id)
        // }
        id_ffmpeg.clear()
    }

    // ============================================================
    // Utility — Coroutines Helpers
    // ============================================================

    /**
     * Execute a suspend block on IO dispatcher.
     * Replacement for the old Executor pattern.
     *
     * Usage:
     *   launchOnIO {
     *       val result = heavyComputation()
     *       launchOnMain { updateUI(result) }
     *   }
     */
    private fun launchOnIO(block: suspend () -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) { block() }
    }

    /**
     * Execute a block on the Main dispatcher.
     * Replacement for runOnUiThread {}.
     */
    private fun launchOnMain(block: suspend () -> Unit) {
        lifecycleScope.launch(Dispatchers.Main) { block() }
    }

    /**
     * Execute a blocking operation on IO and return result on Main.
     * Replacement for the old Thread + Handler pattern.
     */
    private fun <T> withIOThenMain(
        ioBlock: suspend () -> T,
        mainBlock: suspend (T) -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = ioBlock()
            withContext(Dispatchers.Main) {
                mainBlock(result)
            }
        }
    }

    // ============================================================
    // Companion
    // ============================================================

    companion object {
        private const val TAG = "EngineActivity"

        // Default durations for entity types (in milliseconds)
        private const val DEFAULT_AYA_DURATION = 5000
        private const val DEFAULT_TRSL_DURATION = 5000
        private const val DEFAULT_ISTE3ADHA_DURATION = 3000
        private const val DEFAULT_BISMILAH_DURATION = 3000
    }
}
