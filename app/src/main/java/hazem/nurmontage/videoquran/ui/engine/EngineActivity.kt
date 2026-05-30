package hazem.nurmontage.videoquran.ui.engine

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.VectorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
// import androidx.activity.EdgeToEdge  // Not available in current dependency
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.InputDeviceCompat
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.media3.common.MimeTypes
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.StreamInformation
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import hazem.nurmontage.videoquran.utils.AspectRatioCalculator
import hazem.nurmontage.videoquran.utils.audio.AudioUtils
import hazem.nurmontage.videoquran.utils.BitmapCropper
import hazem.nurmontage.videoquran.utils.ColorUtils
import hazem.nurmontage.videoquran.utils.DrawableHelper
import hazem.nurmontage.videoquran.utils.FileHelper
import hazem.nurmontage.videoquran.utils.FileUtils
import hazem.nurmontage.videoquran.utils.LocalPersistence
import hazem.nurmontage.videoquran.utils.LocaleHelper
import hazem.nurmontage.videoquran.utils.MyPreferences
import hazem.nurmontage.videoquran.utils.MyVibrationHelper
import hazem.nurmontage.videoquran.utils.NetworkUtils
import hazem.nurmontage.videoquran.utils.PCMWaveformExtractor
import hazem.nurmontage.videoquran.utils.ScreenUtils
import hazem.nurmontage.videoquran.utils.animator.SmoothTimelineAnimator
import hazem.nurmontage.videoquran.utils.video.SmoothVideoAnimator
import hazem.nurmontage.videoquran.utils.TimeFormatter
import hazem.nurmontage.videoquran.utils.Utils
import hazem.nurmontage.videoquran.utils.UtilsBitmap
import hazem.nurmontage.videoquran.utils.UtilsFileLast
import hazem.nurmontage.videoquran.adapter.DimensionAdabters
import hazem.nurmontage.videoquran.core.common.Common
import hazem.nurmontage.videoquran.core.common.Constants
import hazem.nurmontage.videoquran.constant.AyaTextPreset
import hazem.nurmontage.videoquran.core.common.Constants.AyaTextPreset as ConstantsAyaTextPreset
import hazem.nurmontage.videoquran.constant.EffectAudioType
import hazem.nurmontage.videoquran.constant.EntityAction
import hazem.nurmontage.videoquran.constant.IpadType
import hazem.nurmontage.videoquran.constant.ResizeType
import hazem.nurmontage.videoquran.constant.SurahNameStyle
import hazem.nurmontage.videoquran.entity_timeline.Entity
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.entity_timeline.EntityBismilahTimeline
import hazem.nurmontage.videoquran.entity_timeline.EntityQuranTimeline
import hazem.nurmontage.videoquran.entity_timeline.EntityTrslTimeline
import hazem.nurmontage.videoquran.fragment.AddAudioFragment
import hazem.nurmontage.videoquran.fragment.AddQuranFragment
import hazem.nurmontage.videoquran.fragment.ChangeBgFragment
import hazem.nurmontage.videoquran.fragment.ColorAyaFragment
import hazem.nurmontage.videoquran.fragment.ColorBismilahFragment
import hazem.nurmontage.videoquran.fragment.ColorS_NameFragment
import hazem.nurmontage.videoquran.fragment.ColorTrslAyaFragment
import hazem.nurmontage.videoquran.fragment.EditBismilahEntityFragment
import hazem.nurmontage.videoquran.fragment.EditEntityFragment
import hazem.nurmontage.videoquran.fragment.EditIconQuranFragment
import hazem.nurmontage.videoquran.fragment.EditIpadFragment
import hazem.nurmontage.videoquran.fragment.EditMediaFragment
import hazem.nurmontage.videoquran.fragment.EditMultipleEntityFragment
import hazem.nurmontage.videoquran.fragment.EditS_NameFragment
import hazem.nurmontage.videoquran.fragment.EditTextFragment
import hazem.nurmontage.videoquran.fragment.EditTrslEntityFragment
import hazem.nurmontage.videoquran.fragment.EffectAyaFragment
import hazem.nurmontage.videoquran.fragment.EffectBismilahFragment
import hazem.nurmontage.videoquran.fragment.FontFragment
import hazem.nurmontage.videoquran.fragment.ProgressViewFragment
import hazem.nurmontage.videoquran.fragment.ResizeFragment
import hazem.nurmontage.videoquran.fragment.SimpleProgressViewFragment
import hazem.nurmontage.videoquran.fragment.audio_effect.EchoEffectFragment
import hazem.nurmontage.videoquran.fragment.audio_effect.EnhanceVoiceFragment
import hazem.nurmontage.videoquran.fragment.audio_effect.FadeInOutFragment
import hazem.nurmontage.videoquran.fragment.audio_effect.PitchFragment
import hazem.nurmontage.videoquran.fragment.audio_effect.RemoveNoiceFragment
import hazem.nurmontage.videoquran.fragment.audio_effect.ReverbePresetFragment
import hazem.nurmontage.videoquran.fragment.audio_effect.SpeedFragment
import hazem.nurmontage.videoquran.fragment.audio_effect.VolumeFragment
import hazem.nurmontage.videoquran.model.BgItem
import hazem.nurmontage.videoquran.model.data.BismilahEntity
import hazem.nurmontage.videoquran.model.EffectAudio
import hazem.nurmontage.videoquran.model.EntityBismilahTemplate
import hazem.nurmontage.videoquran.model.EntityMedia
import hazem.nurmontage.videoquran.model.EntityProgressTemplate
import hazem.nurmontage.videoquran.model.EntityQuranTemplate
import hazem.nurmontage.videoquran.model.EntitySurahTemplate
import hazem.nurmontage.videoquran.model.EntityTranslationTemplate
import hazem.nurmontage.videoquran.model.EntityView
import hazem.nurmontage.videoquran.model.FreeElement
import hazem.nurmontage.videoquran.model.Gradient
import hazem.nurmontage.videoquran.model.MRectF
import hazem.nurmontage.videoquran.model.data.QuranEntity
import hazem.nurmontage.videoquran.model.RecitersModel
import hazem.nurmontage.videoquran.model.SurahNameEntity
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.adapter.TransitionBismilahAdabters
import hazem.nurmontage.videoquran.adapter.TransitionEntityAdabters
import hazem.nurmontage.videoquran.model.Transition
import hazem.nurmontage.videoquran.model.data.TranslationQuranEntity
import hazem.nurmontage.videoquran.views.BlurredImageView
import hazem.nurmontage.videoquran.views.ButtonCustumFont
import hazem.nurmontage.videoquran.views.CustomDiscreteSeekBar
import hazem.nurmontage.videoquran.views.text.TextCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFontBold
import hazem.nurmontage.videoquran.views.TrackEntityView
import hazem.nurmontage.videoquran.core.base.BaseActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.Locale
import kotlin.Pair
import hazem.nurmontage.videoquran.R
import java.util.concurrent.TimeUnit
import android.media.AudioManager
import java.util.concurrent.Executors
import hazem.nurmontage.videoquran.ui.search.QuranSearchActivity
import hazem.nurmontage.videoquran.ui.editor.audio.AddReaderNameActivity
import hazem.nurmontage.videoquran.ui.editor.EditTrslTxtActivity
import hazem.nurmontage.videoquran.ui.editor.EditSNameActivity
import hazem.nurmontage.videoquran.ui.editor.CropBitmapActivity
import hazem.nurmontage.videoquran.ui.editor.ChoiceBgFromVideoActivity
import hazem.nurmontage.videoquran.ui.gallery.GalleryPickerVideo
import hazem.nurmontage.videoquran.ui.gallery_photos.GalleryPickerOneImage
import hazem.nurmontage.videoquran.ui.render.ProgressViewActivity

@Suppress("TYPE_CHECKING_HAS_RUN_INTO_RECURSIVE_PROBLEM")
class EngineActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_line)

        mResources = resources

        // Load or create template
        val projectPath = intent.getStringExtra("project_path")
        if (projectPath != null) {
            mTemplate = LocalPersistence.readObjectFromFile(this, projectPath) as? Template
        }
        if (mTemplate == null) {
            mTemplate = LocalPersistence.readObjectFromFile(this, Constants.TEMPLATE_TMP) as? Template
        }
        if (mTemplate == null) {
            mTemplate = Template().apply {
                uri_bg = "android.resource://$packageName/drawable/bg_19"
                name_drawable = "bg_19"
                val size = AspectRatioCalculator.getSize(resizeType, resolution)
                setWidthAndHeight(size.first, size.second)
            }
        }

        // Initialize the engine UI (order matters: timeline first, then views)
        initTimeLineView()
        initViews()

        // Register back-press handler
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    // Helper functions to break recursive type inference at getInstance call sites
    private fun createAddQuranFragment(cb: AddQuranFragment.IAddQuran, res: Resources): AddQuranFragment =
        AddQuranFragment.getInstance(cb, res)

    // Getter functions to provide explicitly typed callbacks, breaking recursive type inference
    private fun getEditSNameCb(): EditS_NameFragment.IEditS_Name = iEditSName
    private fun getBismilahEntityCb(): EditBismilahEntityFragment.IBismilahEntityCallback = iBismilahEntityCallback
    private fun getEditTrstEntityCb(): EditTrslEntityFragment.IEditEntityCallback = iEditTrstEntityCallback

    private fun createColorSNameFragment(cb: EditS_NameFragment.IEditS_Name, entity: SurahNameEntity, res: Resources): ColorS_NameFragment =
        ColorS_NameFragment.getInstance(cb, entity, res)

    private fun createColorBismilahFragment(cb: EditBismilahEntityFragment.IBismilahEntityCallback, entity: BismilahEntity, res: Resources): ColorBismilahFragment =
        ColorBismilahFragment.getInstance(cb, entity, res)

    private fun createColorTrslAyaFragment(cb: EditTrslEntityFragment.IEditEntityCallback, entity: TranslationQuranEntity, res: Resources): ColorTrslAyaFragment =
        ColorTrslAyaFragment.getInstance(cb, entity, res)

    companion object {
        private const val EXTRACT_AUDIO_VIDEO_PERMISSION_REQUEST_CODE = 12
        private const val FPS = 25
        private const val IMAGE_PERMISSION_REQUEST_CODE = 10
        private const val REQUEST_CODE_AUDIO = 2
        private const val REQUEST_WRITE_EXTERNAL_STORAGE = 1
        private const val VIDEO_PERMISSION_REQUEST_CODE = 11
    }

    private var activityLauncher: ActivityResultLauncher<Intent>? = null
    private lateinit var animator_frame_video: SmoothVideoAnimator
    private lateinit var blurredImageView: BlurredImageView
    private lateinit var btnChangeResize: LinearLayout
    private lateinit var btnIpod: LinearLayout
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnRedo: ImageButton
    private lateinit var btnToEnd: ImageButton
    private lateinit var btnToStart: ImageButton
    private lateinit var btnUndo: ImageButton
    private lateinit var btn_cancel: ImageButton
    private lateinit var btn_export: ButtonCustumFont
    private lateinit var btn_setup_fps: LinearLayout
    private var dialog: Dialog? = null
    private var dialogInternet: Dialog? = null
    private var endFrame: Int = 0
    private var endTimeAudioVisible: Int = 0
    private var entityAudio_player: EntityAudio? = null
    private var entityAudio_visible: EntityAudio? = null
    private var isOnScroll: Boolean = false
    private var isToCrop: Boolean = false
    private lateinit var ivIpod: ImageView
    private lateinit var ivResize: ImageView
    private var lastIndexVisible: Int = 0
    private lateinit var layout_resolution: LinearLayout
    private var mCurrentFragment: androidx.fragment.app.Fragment? = null
    private var mIsPlaying: Boolean = false
    private var mPlayer: MediaPlayer? = null
    private var mResources: Resources? = null
    private var mTemplate: Template? = null
    private var oneExport: Boolean = false
    private lateinit var seekBar_fps: CustomDiscreteSeekBar
    private lateinit var seekBar_res: CustomDiscreteSeekBar
    private lateinit var textChangeResize: TextCustumFont
    private var timeFormatter: TimeFormatter? = null
    private lateinit var trackViewEntity: TrackEntityView
    private lateinit var tv_currentTime: TextView
    private lateinit var tv_endTime: TextView
    private lateinit var tv_resolution: TextCustumFont
    private lateinit var tv_tittle_fragment: TextCustumFont
    private var uri_bg: String? = null
    private var valueAnimator: SmoothTimelineAnimator? = null
    private var vibrationHelper: MyVibrationHelper? = null
    private var isSaveTmpTemplate: Boolean = true
    private val executor: java.util.concurrent.Executor = java.util.concurrent.Executors.newSingleThreadExecutor()
    private val id_ffmpeg = mutableListOf<Long>()
    private var current_position_time: Int = 0
    private var startCursur: Int = 0

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (mCurrentFragment != null) {
                hideFragment()
            } else {
                showExitDialog()
            }
        }
    }

    private val iTrimLineCallback = object : TrackEntityView.ITrimLineCallback {
        override fun fadeInAudio(f: Float) {}

        override fun fadeOutAudio(f: Float) {}

        override fun onMove() {}

        override fun onUpdatePlayerAudio(entityAudio: EntityAudio) {}

        override fun onSelectMultiple(i: Int) {
            showEditMultipleEntity(i)
        }

        override fun onDelete(entityView: EntityView) {
            try {
                blurredImageView.entity_select = null
                blurredImageView.postInvalidate()
                hideFragment()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onEmptySelect() {
            blurredImageView.entity_select = null
            blurredImageView.postInvalidate()
            stop()
            hideFragment()
        }

        override fun onUpdate() {
            if (::blurredImageView.isInitialized) {
                blurredImageView.postInvalidate()
            }
        }

        override fun onUp() {
            isOnScroll = false
            updateBtnCutState()
        }

        override fun onAddStack(entityAction: EntityAction) {
            enableUndoBtn()
        }

        override fun onSeekPlayer(f: Float) {
            try {
                isOnScroll = true
                for (entityAudio in trackViewEntity.entityListAudio) {
                    try {
                        if (entityAudio.mediaPlayer != null && entityAudio.mediaPlayer!!.isPlaying) {
                            entityAudio.mediaPlayer!!.pause()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (mIsPlaying) {
                    if (::btnPlayPause.isInitialized) {
                        btnPlayPause.setImageResource(R.drawable.play_btn)
                    }
                    mIsPlaying = false
                    trackViewEntity.isPlaying = false
                    blurredImageView.isPlaying = false
                }
                pauseTimelineAnimation()
                stop()
                val round = Math.round(Math.abs((f / trackViewEntity.getSecond_in_screen()) * (-1000.0f))).toInt()
                if (::blurredImageView.isInitialized && (round <= trackViewEntity.maxTime || blurredImageView.progress < 1.0f)) {
                    val min = Math.min(1.0f, round.toFloat() / trackViewEntity.maxTime)
                    updateTime(round.toLong())
                    blurredImageView.progress = min
                }
                trackViewEntity.update_current_cursur_position(round)
                current_position_time = System.currentTimeMillis().toInt()
                startCursur = trackViewEntity.current_cursur_position
                updateViewTime(trackViewEntity.maxTime, trackViewEntity.current_cursur_position)
                updateBtnCutState()
                updateBtnToStart()
                updateBtnToEnd()
                updateFrame()
            } catch (unused: Exception) {
            }
        }

        override fun pause() {
            stop()
        }

        override fun onPlayVibration() {
            stop()
            runOnUiThread {
                if (vibrationHelper != null) {
                    vibrationHelper!!.vibrate()
                }
            }
        }

        override fun onSelectEntity(entity: Entity, f: Float) {
            stop()
            if (entity is EntityQuranTimeline) {
                blurredImageView.entity_select = entity.getEntityView()
                blurredImageView.invalidate()
                if (EditEntityFragment.instance != null) {
                    EditEntityFragment.instance!!.checkSplitEntity(entity, -trackViewEntity.getCurrentPosition())
                    EditEntityFragment.instance!!.checkIcon(entity)
                    return
                } else if (EditTextFragment.instance != null) {
                    EditTextFragment.instance!!.update((entity as EntityQuranTimeline).quranEntity)
                    return
                } else {
                    showEditEntity(entity)
                    return
                }
            }
            if (entity is EntityTrslTimeline) {
                blurredImageView.entity_select = entity.getEntityView()
                blurredImageView.invalidate()
                if (EditTrslEntityFragment.instance != null) {
                    EditTrslEntityFragment.instance!!.checkSplitEntity(entity, -trackViewEntity.getCurrentPosition())
                    return
                } else {
                    showEditTrslEntity(entity)
                    return
                }
            }
            if (entity is EntityBismilahTimeline) {
                blurredImageView.entity_select = entity.getEntityView()
                blurredImageView.invalidate()
                showEditBismilahEntity(entity)
            } else if (entity is EntityAudio) {
                val entityAudio = entity as EntityAudio
                if (EditMediaFragment.instance != null) {
                    EditMediaFragment.instance!!.checkSplit(entityAudio, -trackViewEntity.getCurrentPosition())
                } else {
                    showEditAudioEntity(entityAudio)
                }
            }
        }

        override fun enableRedo(z: Boolean) {
            if (z) {
                enableRedoBtn()
            } else {
                disableRedoBtn()
            }
        }

        override fun enableUndo(z: Boolean) {
            if (z) {
                enableUndoBtn()
            } else {
                disableUndoBtn()
            }
        }

        override fun progress(z: Boolean) {
            runOnUiThread {
                if (z) {
                    showProgress()
                } else {
                    hideProgressFragment()
                }
            }
        }

        override fun onUpdateTime() {
            startCursur = trackViewEntity.current_cursur_position
            updateTime()
        }
    }

    private val iAddQuran: AddQuranFragment.IAddQuran = object : AddQuranFragment.IAddQuran {
        override fun onBismilah() {
            blurredImageView.addBismilahEntity(BismilahEntity("بسم الله الرحمن الرحيم", RectF(), Typeface.DEFAULT, -1))
        }

        override fun onVuCopyRight() {
            dialogCopyRight()
        }

        override fun progress() {
            runOnUiThread {
                showProgress()
            }
        }

        override fun onSearch() {
            isToCrop = true
            searchAyaResult.launch(Intent(this@EngineActivity, QuranSearchActivity::class.java))
        }

        override fun uploadRecitation() {
            try {
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = AddAudioFragment.getInstance(iAudioCallback, mResources!!)
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.audio))
            } catch (unused: Exception) {
            }
        }

        override fun onAddTranslation(str: String, i: Int, z: Boolean) {
            addTranslationEntity(str, i, z)
        }

        override fun onAdd(str: String, str2: String, str3: String?, str4: String?, i: Int, i2: Int, str5: String, i3: Int, i4: Int) {
            addEntity(str!!, "$str2 $i2", str3!!, str4!!, i, i2, str5!!, i3, i4)
        }

        override fun onDone(str: String, i: Int, str2: String?, uri: Uri?, str3: String?) {
            runOnUiThread {
                hideFragment()
            }
            blurredImageView.updateSizeAya()
            blurredImageView.updateSizeAyaTrsl()
            blurredImageView.setSurahNameEntity(
                str!!, str2!!, "", 1.0f, "\u062E\u0637 \u0627\u0644\u0625\u0628\u0644.otf",
                blurredImageView.clr_aya, 0,
                if (blurredImageView.surahNameEntity != null) blurredImageView.surahNameEntity!!.style else SurahNameStyle.NONE.ordinal,
                i,
                blurredImageView.surahNameEntity != null && blurredImageView.surahNameEntity!!.isHaveBg,
                if (blurredImageView.surahNameEntity != null) blurredImageView.surahNameEntity!!.clrBg else ViewCompat.MEASURED_STATE_MASK
            )
            if (str3 == null) {
                addAudio(uri!!)
            } else {
                addAudioFromVideo(uri!!, str3!!)
            }
        }

        override fun onDone(str: String, i: Int, str2: String?, list: List<RecitersModel>?) {
            runOnUiThread {
                hideFragment()
            }
            blurredImageView.updateSizeAya()
            blurredImageView.updateSizeAyaTrsl()
            blurredImageView.setSurahNameEntity(
                str!!, str2!!, "", 1.0f, "\u062E\u0637 \u0627\u0644\u0625\u0628\u0644.otf",
                blurredImageView.clr_aya, 0,
                if (blurredImageView.surahNameEntity != null) blurredImageView.surahNameEntity!!.style else SurahNameStyle.NONE.ordinal,
                i,
                blurredImageView.surahNameEntity != null && blurredImageView.surahNameEntity!!.isHaveBg,
                if (blurredImageView.surahNameEntity != null) blurredImageView.surahNameEntity!!.clrBg else ViewCompat.MEASURED_STATE_MASK
            )
            if (NetworkUtils.isNetworkAvailable(this@EngineActivity) && list != null && list.isNotEmpty()) {
                addAudioReciters(list)
            } else {
                runOnUiThread {
                    updateTimeToEndAya()
                    updateBtnToEnd()
                    updateBtnToStart()
                    hideProgressFragment()
                }
            }
        }

        override fun onCancel() {
            hideFragment()
        }

        override fun onErrorLimitation() {
            runOnUiThread {
                Toast.makeText(this@EngineActivity, mResources!!.getString(R.string.error_limit), Toast.LENGTH_SHORT).show()
            }
        }

        override fun onAddReaderName(str: String?, str2: String?, uri: Uri?) {
            isToCrop = true
            val intent = Intent(this@EngineActivity, AddReaderNameActivity::class.java)
            intent.putExtra("name", str)
            if (uri != null) {
                intent.putExtra(MimeTypes.BASE_TYPE_AUDIO, uri.toString())
            }
            intent.putExtra("path_video_copy", str2)
            nameReaderResult.launch(intent)
        }
    }

    private val searchAyaResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult: ActivityResult ->
        isToCrop = false
        try {
            if (AddQuranFragment.instance != null) {
                AddQuranFragment.instance!!.addAyaIndex()
            } else {
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = AddQuranFragment.getInstance(iAddQuran, mResources!!) as Fragment?
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                runOnUiThread {
                    setupShowFragment(mResources!!.getString(R.string.quran))
                }
            }
        } catch (unused: Exception) {
        }
    }

    private val nameReaderResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult: ActivityResult ->
        isToCrop = false
        val data = activityResult.data
        if (data != null) {
            if (AddQuranFragment.instance != null) {
                val parse = Uri.parse(data.getStringExtra(MimeTypes.BASE_TYPE_AUDIO)!!)
                val stringExtra = data.getStringExtra("path_video_copy")!!
                AddQuranFragment.instance!!.setNameReader(data.getStringExtra("name")!!, parse, stringExtra)
                return@registerForActivityResult
            }
            try {
                val parse2 = Uri.parse(data.getStringExtra(MimeTypes.BASE_TYPE_AUDIO)!!)
                val stringExtra2 = data.getStringExtra("path_video_copy")!!
                val stringExtra3 = data.getStringExtra("name")!!
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = AddQuranFragment.getInstance(iAddQuran, mResources!!, parse2, stringExtra2, stringExtra3)
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                runOnUiThread {
                    setupShowFragment(mResources!!.getString(R.string.quran))
                }
            } catch (unused: Exception) {
            }
        }
    }

    private val editSurahNameResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult: ActivityResult ->
        isToCrop = false
        if (activityResult.resultCode != -1) return@registerForActivityResult
        val data = activityResult.data ?: return@registerForActivityResult
        val stringExtra = data.getStringExtra(Common.READER)!!
        val booleanExtra = data.getBooleanExtra("isBg", false)
        val intExtra = data.getIntExtra("style", 0)
        if (blurredImageView.surahNameEntity!!.index_surah == 0) {
            blurredImageView.surahNameEntity!!.index_surah = data.getIntExtra(StreamInformation.KEY_INDEX, 1)
        }
        blurredImageView.surahNameEntity!!.clrBg = data.getIntExtra("clrBg", ViewCompat.MEASURED_STATE_MASK)
        if (intExtra == SurahNameStyle.NONE.ordinal) {
            blurredImageView.surahNameEntity!!.setAlignment(android.text.Layout.Alignment.ALIGN_CENTER)
                    blurredImageView.updateAlignmentSurah(android.text.Layout.Alignment.ALIGN_CENTER)
        }
        blurredImageView.surahNameEntity!!.setStyle(this@EngineActivity, intExtra, stringExtra, booleanExtra)
        blurredImageView.invalidate()
    }

    private val editTrslResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult: ActivityResult ->
        isToCrop = false
        if (activityResult.resultCode != -1) return@registerForActivityResult
        val data = activityResult.data ?: return@registerForActivityResult
        val stringExtra = data.getStringExtra(Common.READER)!!
        val booleanExtra = data.getBooleanExtra("isBg", true)
        val translationQuranEntity = blurredImageView.entity_select as TranslationQuranEntity
        translationQuranEntity.clrBg = data.getIntExtra("clrBg", ViewCompat.MEASURED_STATE_MASK)
        translationQuranEntity.txt = stringExtra
        translationQuranEntity.setHaveBg(booleanExtra)
        blurredImageView.invalidate()
    }

    private val iChangeBgCallback = object : ChangeBgFragment.IChangeBgCallback {
        override fun onCancel() {
            hideFragment()
        }

        override fun onDone() {
            hideFragment()
        }

        override fun onCrop() {
            toCrop()
        }

        override fun onAdd(bgItem: BgItem) {
            if (bgItem.name_drawable == mTemplate!!.name_drawable) {
                return
            }
            if (ChangeBgFragment.instance != null) {
                ChangeBgFragment.instance!!.scrollToSelected()
            }
            mTemplate!!.name_drawable = bgItem.name_drawable
            uri_bg = "android.resource://" + packageName + "/drawable/" + bgItem.id
            showProgressSimple()
            executor.execute {
                var engineActivity: EngineActivity
                var runnable: Runnable
                var cropTo16x9: Bitmap? = null
                var bitmap: Bitmap
                var bitmap2: Bitmap
                var rect: Rect
                try {
                    try {
                        try {
                            mTemplate!!.uri_bg = uri_bg
                            var i = 0
                            mTemplate!!.isVideoSquare = false
                            blurredImageView.isVideo = false
                            val height = blurredImageView.getHeight()
                            blurredImageView.bitmapOriginal = Glide.with(this@EngineActivity as FragmentActivity)
                                    .asBitmap()
                                    .load(uri_bg)
                                    .override(height, height)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .submit()
                                    .get()
                            cropTo16x9 = if (mTemplate!!.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                                BitmapCropper.cropTo9x16(blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH())
                            } else if (mTemplate!!.geTypeResize() == ResizeType.SQUARE.ordinal) {
                                BitmapCropper.cropTo1x1(blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH())
                            } else {
                                BitmapCropper.cropTo16x9(blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH())
                            }
                            blurredImageView.updatePosCanvas(cropTo16x9)
                            blurredImageView.updateIpad(cropTo16x9, mTemplate!!.ipad_type, mTemplate!!.geTypeResize())
                            if (mTemplate!!.ipad_type == IpadType.IPAD_NEOMORPHIC.ordinal) {
                                val width = (blurredImageView.ipad_rect!!.width() * 0.6f).toInt()
                                var round = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                                var round2 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                                var i2 = width + round
                                if (i2 > blurredImageView.bitmapOriginal!!.width) {
                                    round -= i2 - blurredImageView.bitmapOriginal!!.width
                                    i2 = blurredImageView.bitmapOriginal!!.width
                                }
                                var i3 = width + round2
                                if (i3 > blurredImageView.bitmapOriginal!!.height) {
                                    round2 -= i3 - blurredImageView.bitmapOriginal!!.height
                                    i3 = blurredImageView.bitmapOriginal!!.height
                                }
                                if (round < 0) {
                                    round = 0
                                }
                                if (round2 >= 0) {
                                    i = round2
                                }
                                val rect2 = Rect(round, i, i2, i3)
                                blurredImageView.setRadius_square(width)
                                val width2 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                                val height2 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                                val cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.bitmapOriginal!!, rect2, width, width2, height2)
                                rect2.right = rect2.left + width2
                                rect2.bottom = rect2.top + height2
                                blurredImageView.rectSquare = rect2
                                bitmap2 = cropToSquareWithRoundCorners
                                rect = rect2
                            } else {
                                if (mTemplate!!.ipad_type != IpadType.IPAD.ordinal && mTemplate!!.ipad_type != IpadType.IPAD_UNBLUR.ordinal && mTemplate!!.ipad_type != IpadType.IPAD_CLASSIC.ordinal) {
                                    val width3 = (blurredImageView.ipad_rect!!.width() * 1.0f).toInt()
                                    val height3 = (cropTo16x9!!.height * 0.5355f).toInt()
                                    var round3 = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                                    var round4 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                                    var i4 = width3 + round3
                                    if (i4 > blurredImageView.bitmapOriginal!!.width) {
                                        round3 -= i4 - blurredImageView.bitmapOriginal!!.width
                                        i4 = blurredImageView.bitmapOriginal!!.width
                                    }
                                    var i5 = height3 + round4
                                    if (i5 > blurredImageView.bitmapOriginal!!.height) {
                                        round4 -= i5 - blurredImageView.bitmapOriginal!!.height
                                        i5 = blurredImageView.bitmapOriginal!!.height
                                    }
                                    if (round3 < 0) {
                                        round3 = 0
                                    }
                                    if (round4 < 0) {
                                        round4 = 0
                                    }
                                    val rect3 = Rect(round3, round4, i4, i5)
                                    val width4 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                                    val height4 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                                    val cropToSquare = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal!!, rect3, width4, height4)
                                    blurredImageView.bitmapSquare = cropToSquare
                                    blurredImageView.setRadius_square(0)
                                    rect3.right = rect3.left + width4
                                    rect3.bottom = rect3.top + height4
                                    blurredImageView.rectSquare = rect3
                                    bitmap2 = cropToSquare
                                    rect = rect3
                                }
                                val width5 = (blurredImageView.ipad_rect!!.width() * 0.87530595f).toInt()
                                val i6 = (width5 * 1.13f).toInt()
                                val min = Math.min(width5, i6)
                                var round5 = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                                var round6 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                                var i7 = width5 + round5
                                if (i7 > blurredImageView.bitmapOriginal!!.width) {
                                    round5 -= i7 - blurredImageView.bitmapOriginal!!.width
                                    i7 = blurredImageView.bitmapOriginal!!.width
                                }
                                var i8 = i6 + round6
                                if (i8 > blurredImageView.bitmapOriginal!!.height) {
                                    round6 -= i8 - blurredImageView.bitmapOriginal!!.height
                                    i8 = blurredImageView.bitmapOriginal!!.height
                                }
                                if (round5 < 0) {
                                    round5 = 0
                                }
                                if (round6 < 0) {
                                    round6 = 0
                                }
                                val rect4 = Rect(round5, round6, i7, i8)
                                if (mTemplate!!.ipad_type == IpadType.IPAD_CLASSIC.ordinal) {
                                    val width6 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                                    val height5 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                                    val cropToSquare2 = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal!!, rect4, width6, height5)
                                    blurredImageView.bitmapSquare = cropToSquare2
                                    blurredImageView.setRadius_square(0)
                                    rect4.right = rect4.left + width6
                                    rect4.bottom = rect4.top + height5
                                    blurredImageView.rectSquare = rect4
                                    bitmap = cropToSquare2
                                } else {
                                    val i9 = (min * 0.10800001f).toInt()
                                    blurredImageView.setRadius_square(i9)
                                    val width7 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                                    val height6 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                                    val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.bitmapOriginal!!, rect4, i9, width7, height6)
                                    rect4.right = rect4.left + width7
                                    rect4.bottom = rect4.top + height6
                                    blurredImageView.rectSquare = rect4
                                    bitmap = cropToSquareWithRoundCorners2
                                }
                                bitmap2 = bitmap
                                rect = rect4
                            }
                            if (mTemplate!!.ipad_type == IpadType.GRADIENT.ordinal) {
                                blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1), bitmap2!!, ViewCompat.MEASURED_STATE_MASK, mTemplate!!.ipad_type, mTemplate!!.geTypeResize(), rect)
                            } else if (mTemplate!!.ipad_type == IpadType.BLUE_TYPE.ordinal) {
                                if (blurredImageView.color_gradient != null) {
                                    blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1), bitmap2!!, blurredImageView.color_gradient!!, mTemplate!!.ipad_type, mTemplate!!.geTypeResize(), rect)
                                } else {
                                    blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1), bitmap2!!, blurredImageView.color_ipad, mTemplate!!.ipad_type, mTemplate!!.geTypeResize(), rect)
                                }
                            } else {
                                blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1), bitmap2!!, -1, mTemplate!!.ipad_type, mTemplate!!.geTypeResize(), rect)
                            }
                            mTemplate!!.color_ipad = blurredImageView.colorIpad()
                            runOnUiThread {
                                blurredImageView.invalidate()
                            }
                            engineActivity = this@EngineActivity
                            runnable = Runnable { hideProgressFragment() }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            engineActivity = this@EngineActivity
                            runnable = Runnable { hideProgressFragment() }
                        }
                        engineActivity.runOnUiThread(runnable)
                    } catch (unused: Exception) {
                    }
                } finally {
                }
            }
        }

        override fun onUploadVideo() {
            pickVideoFromGallery()
        }

        override fun onUploadImg() {
            pickImageFromGallery()
        }


    }

    private val iDimensionCallback = object : DimensionAdabters.IDimensionCallback {
        override fun isCustomSize(z: Boolean, resizeType: ResizeType) {}

        override fun done() {
            hideFragment()
        }

        override fun onCustumSize(i: Int, i2: Int, i3: Int, str: String, i4: Int) {
            updateHitRatio(i3, str)
            if (i3 == mTemplate!!.geTypeResize()) {
                return
            }
            if (ResizeFragment.instance != null) {
                ResizeFragment.instance!!.scrollToSelectedPosition()
            }
            showProgressSimple()
            executor.execute {
                var engineActivity: EngineActivity
                var runnable: Runnable
                var cropTo16x9: Bitmap? = null
                var i5: Int
                var cropToSquareWithRoundCorners: Bitmap? = null
                var bitmap: Bitmap
                var rect: Rect
                try {
                    try {
                        try {
                            blurredImageView.invalidate()
                            mTemplate!!.resizeType = i3
                            mTemplate!!.imgResize = str
                            val size = AspectRatioCalculator.getSize(i3, mTemplate!!.resolution)
                            mTemplate!!.setWidthAndHeight(size.first, size.second)
                            blurredImageView.initCanvasDimension(blurredImageView.getWidth(), blurredImageView.getHeight(), i3)
                            cropTo16x9 = if (mTemplate!!.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                                BitmapCropper.cropTo9x16(blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH())
                            } else if (mTemplate!!.geTypeResize() == ResizeType.SQUARE.ordinal) {
                                BitmapCropper.cropTo1x1(blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH())
                            } else {
                                BitmapCropper.cropTo16x9(blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH())
                            }
                            blurredImageView.updatePosCanvas(cropTo16x9)
                            blurredImageView.bitmapBlured = cropTo16x9
                            blurredImageView.updateIpad(cropTo16x9, mTemplate!!.ipad_type, mTemplate!!.geTypeResize())
                            i5 = 0
                        } finally {
                        }
                        if (mTemplate!!.ipad_type != IpadType.GRADIENT.ordinal && mTemplate!!.ipad_type != IpadType.BLACK_LAYER.ordinal && mTemplate!!.ipad_type != IpadType.MASK_BRUSH.ordinal && mTemplate!!.ipad_type != IpadType.BLUE_TYPE.ordinal && mTemplate!!.ipad_type != IpadType.CASSET_IMG.ordinal && mTemplate!!.ipad_type != IpadType.CASSET_IMG_BLUR.ordinal) {
                            if (mTemplate!!.ipad_type == IpadType.IPAD_NEOMORPHIC.ordinal) {
                                val width = (blurredImageView.ipad_rect!!.width() * 0.6f).toInt()
                                var round = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                                var round2 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                                var i6 = width + round
                                if (i6 > blurredImageView.bitmapOriginal!!.width) {
                                    round -= i6 - blurredImageView.bitmapOriginal!!.width
                                    i6 = blurredImageView.bitmapOriginal!!.width
                                }
                                var i7 = width + round2
                                if (i7 > blurredImageView.bitmapOriginal!!.height) {
                                    round2 -= i7 - blurredImageView.bitmapOriginal!!.height
                                    i7 = blurredImageView.bitmapOriginal!!.height
                                }
                                if (round < 0) {
                                    round = 0
                                }
                                if (round2 >= 0) {
                                    i5 = round2
                                }
                                val rect2 = Rect(round, i5, i6, i7)
                                blurredImageView.setRadius_square(width)
                                val width2 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                                val height = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                                val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.bitmapOriginal!!, rect2, width, width2, height)
                                rect2.right = rect2.left + width2
                                rect2.bottom = rect2.top + height
                                blurredImageView.rectSquare = rect2
                                bitmap = cropToSquareWithRoundCorners2
                                rect = rect2
                            } else {
                                if (mTemplate!!.ipad_type != IpadType.IPAD.ordinal && mTemplate!!.ipad_type != IpadType.IPAD_UNBLUR.ordinal && mTemplate!!.ipad_type != IpadType.IPAD_CLASSIC.ordinal) {
                                    val width3 = (blurredImageView.ipad_rect!!.width() * 1.0f).toInt()
                                    val height2 = (cropTo16x9!!.height * 0.5355f).toInt()
                                    var round3 = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                                    var round4 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                                    var i8 = width3 + round3
                                    if (i8 > blurredImageView.bitmapOriginal!!.width) {
                                        round3 -= i8 - blurredImageView.bitmapOriginal!!.width
                                        i8 = blurredImageView.bitmapOriginal!!.width
                                    }
                                    var i9 = height2 + round4
                                    if (i9 > blurredImageView.bitmapOriginal!!.height) {
                                        round4 -= i9 - blurredImageView.bitmapOriginal!!.height
                                    }
                                }
                                // Additional ipad type handling continues in full implementation
                                bitmap = blurredImageView.bitmapSquare!!
                                rect = blurredImageView.rectSquare!!!!
                            }
                        } else {
                            bitmap = blurredImageView.bitmapSquare!!
                            rect = blurredImageView.rectSquare!!!!
                        }
                        engineActivity = this@EngineActivity
                        runnable = Runnable { hideProgressFragment() }
                    } catch (e: Exception) {
                        Log.e("Tag resize : ", "init " + e.message)
                        engineActivity = this@EngineActivity
                        runnable = Runnable { hideProgressFragment() }
                    }
                    engineActivity.runOnUiThread(runnable)
                } catch (unused: Exception) {
                }
            }
        }
    }

    private val iAudioCallback = object : AddAudioFragment.IAudioCallback {
        override fun upload() {
            if (checkPermissionAudio()) {
                pickAudio()
            }
        }

        override fun extract() {
            pickVideoForAudio()
        }

        override fun cancel() {
            hideFragment()
            try {
                setupShowFragment(mResources!!.getString(R.string.quran))
                val beginTransaction = supportFragmentManager.beginTransaction()
                val addQuranInstance = createAddQuranFragment(iAddQuran, mResources!!)
                mCurrentFragment = addQuranInstance
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
            } catch (unused: Exception) {
            }
        }
    }

    private val iIpadEditCallback = object : EditIpadFragment.IIpadEditCallback {
        override fun onClick(i: Int, i2: Int) {
            mTemplate!!.color_ipad = i
            mTemplate!!.index_color = i2
            mTemplate!!.gradient = null
            blurredImageView.setColorIpad(i)
            blurredImageView.invalidate()
        }

        override fun onClick(gradient: Gradient, i: Int) {
            mTemplate!!.gradient = gradient
            mTemplate!!.index_color = i
            blurredImageView.setColorIpad(gradient)
            blurredImageView.invalidate()
        }

        fun onDialogPremium() {
            dialogPremium(0)
        }

        override fun onGlassType(z: Boolean) {
            mTemplate!!.isGlass = z
            blurredImageView.isGlass = z
            blurredImageView.invalidate()
        }

        override fun onChangeType(i: Int) {
            if (blurredImageView.getmIpadType() == i) {
                return
            }
            if (EditIpadFragment.instance != null) {
                EditIpadFragment.instance!!.scrollToSelectedPosition()
            }
            try {
                mTemplate!!.ipad_type = i
                mTemplate!!.changeTypeIpad(i)
                if (mTemplate!!.isVideoSquare) {
                    if (i != IpadType.GRADIENT.ordinal && i != IpadType.BLACK_LAYER.ordinal && i != IpadType.MASK_BRUSH.ordinal && i != IpadType.BLUE_TYPE.ordinal && i != IpadType.CASSET_IMG.ordinal) {
                        if (mTemplate!!.ipad_type == IpadType.CASSET_IMG_BLUR.ordinal) {
                            blurredImageView.bitmapSquare = blurredImageView.bitmapBlured
                            blurredImageView.setRadius_square(0)
                        }
                    }
                    blurredImageView.bitmapSquare = blurredImageView.bitmapNotBlur
                    blurredImageView.setRadius_square(0)
                }
                if (i == IpadType.IPAD.ordinal || i == IpadType.IPAD_UNBLUR.ordinal) {
                    val width = (blurredImageView.ipad_rect!!.width() * 0.87530595f).toInt()
                    val i2 = (width * 1.13f).toInt()
                    val min = (Math.min(width, i2) * 0.10800001f).toInt()
                    var round = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                    var round2 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                    var i3 = width + round
                    if (i3 > blurredImageView.bitmapOriginal!!.width) {
                        round -= i3 - blurredImageView.bitmapOriginal!!.width
                        i3 = blurredImageView.bitmapOriginal!!.width
                    }
                    var i4 = i2 + round2
                    if (i4 > blurredImageView.bitmapOriginal!!.height) {
                        round2 -= i4 - blurredImageView.bitmapOriginal!!.height
                        i4 = blurredImageView.bitmapOriginal!!.height
                    }
                    if (round < 0) {
                        round = 0
                    }
                    if (round2 < 0) {
                        round2 = 0
                    }
                    val rect = Rect(round, round2, i3, i4)
                    val width2 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                    val height = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                    blurredImageView.bitmapSquare = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.bitmapOriginal!!, rect, min, width2, height)
                    blurredImageView.setRadius_square(min)
                    rect.right = rect.left + width2
                    rect.bottom = rect.top + height
                    blurredImageView.rectSquare = rect
                }
                if (i == IpadType.IPAD_CLASSIC.ordinal) {
                    val width3 = (blurredImageView.ipad_rect!!.width() * 0.87530595f).toInt()
                    val i5 = (width3 * 1.13f).toInt()
                    var round3 = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                    var round4 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                    var i6 = width3 + round3
                    if (i6 > blurredImageView.bitmapOriginal!!.width) {
                        round3 -= i6 - blurredImageView.bitmapOriginal!!.width
                        i6 = blurredImageView.bitmapOriginal!!.width
                    }
                    var i7 = i5 + round4
                    if (i7 > blurredImageView.bitmapOriginal!!.height) {
                        round4 -= i7 - blurredImageView.bitmapOriginal!!.height
                        i7 = blurredImageView.bitmapOriginal!!.height
                    }
                    if (round3 < 0) {
                        round3 = 0
                    }
                    if (round4 < 0) {
                        round4 = 0
                    }
                    val rect2 = Rect(round3, round4, i6, i7)
                    val width4 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                    val height2 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                    blurredImageView.bitmapSquare = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal!!, rect2, width4, height2)
                    blurredImageView.setRadius_square(0)
                    rect2.right = rect2.left + width4
                    rect2.bottom = rect2.top + height2
                    blurredImageView.rectSquare = rect2
                }
                if (i == IpadType.IPAD_NEOMORPHIC.ordinal) {
                    val width5 = (blurredImageView.ipad_rect!!.width() * 0.6f).toInt()
                    var round5 = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                    var round6 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                    var i8 = width5 + round5
                    if (i8 > blurredImageView.bitmapOriginal!!.width) {
                        round5 -= i8 - blurredImageView.bitmapOriginal!!.width
                        i8 = blurredImageView.bitmapOriginal!!.width
                    }
                    var i9 = width5 + round6
                    if (i9 > blurredImageView.bitmapOriginal!!.height) {
                        round6 -= i9 - blurredImageView.bitmapOriginal!!.height
                        i9 = blurredImageView.bitmapOriginal!!.height
                    }
                    if (round5 < 0) {
                        round5 = 0
                    }
                    if (round6 < 0) {
                        round6 = 0
                    }
                    val rect3 = Rect(round5, round6, i8, i9)
                    val width6 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                    val height3 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                    blurredImageView.bitmapSquare = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.bitmapOriginal!!, rect3, width5, width6, height3)
                    blurredImageView.setRadius_square(width5)
                    rect3.right = rect3.left + width6
                    rect3.bottom = rect3.top + height3
                    blurredImageView.rectSquare = rect3
                }
                if (i == IpadType.BOTTOM_RECT.ordinal) {
                    val width7 = (blurredImageView.ipad_rect!!.width() * 1.0f).toInt()
                    val height4 = (blurredImageView.bitmapBlured!!.height * 0.5355f).toInt()
                    var round7 = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                    var round8 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                    var i10 = width7 + round7
                    if (i10 > blurredImageView.bitmapOriginal!!.width) {
                        round7 -= i10 - blurredImageView.bitmapOriginal!!.width
                        i10 = blurredImageView.bitmapOriginal!!.width
                    }
                    var i11 = height4 + round8
                    if (i11 > blurredImageView.bitmapOriginal!!.height) {
                        round8 -= i11 - blurredImageView.bitmapOriginal!!.height
                        i11 = blurredImageView.bitmapOriginal!!.height
                    }
                    if (round7 < 0) {
                        round7 = 0
                    }
                    if (round8 < 0) {
                        round8 = 0
                    }
                    val rect4 = Rect(round7, round8, i10, i11)
                    val width8 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                    val height5 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                    blurredImageView.bitmapSquare = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal!!, rect4, width8, height5)
                    blurredImageView.setRadius_square(0)
                    rect4.right = rect4.left + width8
                    rect4.bottom = rect4.top + height5
                    blurredImageView.rectSquare = rect4
                }
                if (i == IpadType.BORDER.ordinal) {
                    if (ColorUtils.isColorDark(blurredImageView.bitmapOriginal!!.getPixel(
                        (blurredImageView.bitmapOriginal!!.width * 0.5f).toInt(),
                        (blurredImageView.bitmapOriginal!!.height * 0.5f).toInt()
                    ))) {
                        mTemplate!!.color_ipad = -1
                    } else {
                        mTemplate!!.color_ipad = ViewCompat.MEASURED_STATE_MASK
                    }
                    blurredImageView.setColorIpad(mTemplate!!.color_ipad!!)
                }
                blurredImageView.createRectWithoutSurahName()
                blurredImageView.resizeEntity()
                if (blurredImageView.surahNameEntity != null && blurredImageView.surahNameEntity!!.style != SurahNameStyle.ZAGHRAFAT.ordinal && !blurredImageView.surahNameEntity!!.isHaveBg) {
                    blurredImageView.updatePosSurahName()
                }
                blurredImageView.changeColorIpad()
                blurredImageView.invalidate()
            } catch (e: Exception) {
                Log.e("execption", "onChangeType" + e.message)
            }
        }

        override fun onDone() {
            hideFragment()
        }

        override fun onCancel() {
            hideFragment()
        }
    }

    private val launchChoiceBgActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult: ActivityResult ->
        onChoiceBgResult(activityResult)
    }

    private val launchCropActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult: ActivityResult ->
        onCropResult(activityResult)
    }

    private val launchImg: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult: ActivityResult ->
        onImgResult(activityResult)
    }

    private val launchVideo: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult: ActivityResult ->
        onVideoResult(activityResult)
    }

    private val launchVideoExtract: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult: ActivityResult ->
        onVideoExtractResult(activityResult)
    }

    private val extentions = arrayOf(".mp3", ".ogg", ".acc", ".m4a", ".wav", ".mpeg")
    private var start_extenstion: Int = 0

    private val iQuranIconCallback = object : EditIconQuranFragment.IQuranIconCallback {
        override fun add(str: String) {
            try {
                val quranEntity = trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                quranEntity.vectorDrawable = ContextCompat.getDrawable(applicationContext, DrawableHelper.getIDDrawableIconByName(str)) as VectorDrawable
                quranEntity.icon = str
                quranEntity.updateIconDraw()
                quranEntity.initPreset(quranEntity.getmPreset())
                blurredImageView.invalidate()
            } catch (unused: Exception) {
                Log.e("icon  e ", "" + str)
            }
        }

        override fun onDone(str: String) {
            try {
                blurredImageView.setIcon(str, ContextCompat.getDrawable(applicationContext, DrawableHelper.getIDDrawableIconByName(str)) as VectorDrawable)
                hideFragment()
                iTrimLineCallback.onSelectEntity(trackViewEntity.selectedEntity!!, -1.0f)
            } catch (unused: Exception) {
            }
        }

        override fun onCancel(str: String) {
            try {
                val quranEntity = trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                quranEntity.vectorDrawable = ContextCompat.getDrawable(applicationContext, DrawableHelper.getIDDrawableIconByName(str)) as VectorDrawable
                quranEntity.icon = str
                quranEntity.updateIconDraw()
                quranEntity.initPreset(quranEntity.getmPreset())
                blurredImageView.invalidate()
                hideFragment()
                iTrimLineCallback.onSelectEntity(trackViewEntity.selectedEntity!!, -1.0f)
            } catch (unused: Exception) {
            }
        }
    }

    @Suppress("TYPE_CHECKING_HAS_RUN_INTO_RECURSIVE_PROBLEM") private val iEditSName = object : EditS_NameFragment.IEditS_Name {
        override fun onFont(surahNameEntity: SurahNameEntity) {
            val beginTransaction = supportFragmentManager.beginTransaction()
            mCurrentFragment = FontFragment.getInstance(iFontCallback, surahNameEntity.nameFont!!, surahNameEntity.getPaintAya().typeface)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
            setupShowFragment(mResources!!.getString(R.string.font))
        }

        override fun onEdit(surahNameEntity: SurahNameEntity) {
            try {
                isToCrop = true
                val intent = Intent(this@EngineActivity, EditSNameActivity::class.java)
                intent.putExtra("surah_name", blurredImageView.surahNameEntity!!.name)
                intent.putExtra("reader_name", blurredImageView.surahNameEntity!!.reader)
                intent.putExtra("style", blurredImageView.surahNameEntity!!.style)
                intent.putExtra(StreamInformation.KEY_INDEX, blurredImageView.surahNameEntity!!.index_surah)
                intent.putExtra("isBg", blurredImageView.surahNameEntity!!.isHaveBg)
                intent.putExtra("clrBg", blurredImageView.surahNameEntity!!.clrBg)
                editSurahNameResult.launch(intent)
                overridePendingTransition(0, 0)
            } catch (unused: Exception) {
            }
        }

        override fun update() {
            blurredImageView.postInvalidate()
        }

        override fun onDone() {
            selectSurahName()
        }

        override fun onColor(surahNameEntity: SurahNameEntity) {
            try {
                stop()
                val beginTransaction = supportFragmentManager.beginTransaction()
                val fragSNameObj = createColorSNameFragment(getEditSNameCb(), surahNameEntity, mResources!!)
                mCurrentFragment = fragSNameObj
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(null)
            } catch (unused: Exception) {
            }
        }
    }

    private val iFontCallback = object : FontFragment.IFontCallback {
        override fun onAdd(str: String?, typeface: Typeface?) {
            try {
                if (blurredImageView.entity_select is SurahNameEntity) {
                    blurredImageView.surahNameEntity!!.setTypeface(typeface!!, str!!)
                    blurredImageView.invalidate()
                } else if (str != null && typeface != null) {
                    blurredImageView.setTypeface(typeface!!, str!!)
                }
                FontFragment.instance!!.add(typeface, str)
            } catch (unused: Exception) {
            }
        }

        override fun onDone(str: String?, typeface: Typeface?) {
            try {
                hideFragment()
                if (blurredImageView.entity_select is SurahNameEntity) {
                    selectSurahName()
                } else {
                    iTrimLineCallback.onSelectEntity(trackViewEntity.selectedEntity!!, -1.0f)
                }
            } catch (unused: Exception) {
            }
        }

        override fun onCancel(str: String?, typeface: Typeface?) {
            try {
                if (blurredImageView.entity_select is SurahNameEntity) {
                    blurredImageView.surahNameEntity!!.setTypeface(typeface!!, str!!)
                    blurredImageView.invalidate()
                    selectSurahName()
                } else {
                    if (str != null && typeface != null) {
                        blurredImageView.setTypeface(typeface!!, str!!)
                    }
                    hideFragment()
                    iTrimLineCallback.onSelectEntity(trackViewEntity.selectedEntity!!, -1.0f)
                }
            } catch (unused: Exception) {
            }
        }
    }

    @Suppress("TYPE_CHECKING_HAS_RUN_INTO_RECURSIVE_PROBLEM") private val iBismilahEntityCallback = object : EditBismilahEntityFragment.IBismilahEntityCallback {
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {
            blurredImageView.setPreset(hazem.nurmontage.videoquran.core.common.Constants.AyaTextPreset.values()[ayaTextPreset.ordinal])
        }

        override fun updateAya(i: Int) {
            blurredImageView.setColorAya(i)
        }

        override fun onAnim() {
            try {
                stop()
                val bismilahEntity = trackViewEntity.selectedEntity!!.getEntityView() as BismilahEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = EffectBismilahFragment.get(
                    bismilahEntity.bismilahTimeline!!.getTransition(),
                    mResources!!, iTransitionBismilahCallback,
                    trackViewEntity.selectedEntity as EntityBismilahTimeline
                )
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.animtion))
            } catch (unused: Exception) {
            }
        }

        override fun onDelete() {
            try {
                stop()
                trackViewEntity.deleteEntity(false)
                updateTime()
                iTrimLineCallback.onEmptySelect()
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun update() {
            blurredImageView.postInvalidate()
        }

        override fun onDone() {
            hideFragment()
            if (blurredImageView.entity_select is QuranEntity || blurredImageView.entity_select is BismilahEntity) {
                iTrimLineCallback.onSelectEntity(trackViewEntity.selectedEntity!!, -1.0f)
            }
        }

        override fun onColor() {
            try {
                stop()
                val bismilahEntity = trackViewEntity.selectedEntity!!.getEntityView() as BismilahEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                val fragBismilahObj = createColorBismilahFragment(getBismilahEntityCb(), bismilahEntity, mResources!!)
                mCurrentFragment = fragBismilahObj
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(null)
            } catch (unused: Exception) {
            }
        }

        override fun fromTheStart() {
            stop()
            trackViewEntity.translateFromStart()
        }

        override fun fromNow() {
            stop()
            trackViewEntity.translateFromNow()
        }

        override fun untilNow() {
            stop()
            trackViewEntity.translateUntilNow()
        }

        override fun untilTheEnd() {
            stop()
            trackViewEntity.translateEndNow()
        }
    }

    private val iEditEntityCallback: EditEntityFragment.IEditEntityCallback = object : EditEntityFragment.IEditEntityCallback {
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {
            blurredImageView.setPreset(hazem.nurmontage.videoquran.core.common.Constants.AyaTextPreset.values()[ayaTextPreset.ordinal])
        }

        override fun updateAya(i: Int) {
            blurredImageView.setColorAya(i)
        }

        override fun updateTrsl(i: Int) {
            blurredImageView.setColorTrsl(i)
        }

        override fun onFont() {
            try {
                stop()
                val quranEntity = trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = FontFragment.getInstance(iFontCallback, quranEntity.nameFont!!, quranEntity.getPaintAya().typeface!!)
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.font))
            } catch (unused: Exception) {
            }
        }

        override fun onIcon() {
            try {
                stop()
                val quranEntity = trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = EditIconQuranFragment.getInstance(iQuranIconCallback, quranEntity.icon)
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.icon))
            } catch (unused: Exception) {
            }
        }

        override fun onAnim() {
            try {
                stop()
                val quranEntity = trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = EffectAyaFragment.get(
                    quranEntity.entityQuran!!.getTransition(),
                    mResources!!, iTransitionCallback,
                    trackViewEntity.selectedEntity as EntityQuranTimeline
                )
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.animtion))
            } catch (unused: Exception) {
            }
        }

        override fun onDelete() {
            try {
                stop()
                trackViewEntity.deleteEntity(false)
                updateTime()
                iTrimLineCallback.onEmptySelect()
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onDone() {
            hideFragment()
            if (blurredImageView.entity_select is QuranEntity) {
                iTrimLineCallback.onSelectEntity(trackViewEntity.selectedEntity!!, -1.0f)
            }
        }

        override fun onColor() {
            try {
                stop()
                val quranEntity = trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                val fragAyaObj: Any = ColorAyaFragment.getInstance(this@EngineActivity.iEditTrstEntityCallback, quranEntity, mResources!!)
                mCurrentFragment = fragAyaObj as Fragment?
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(null)
            } catch (unused: Exception) {
            }
        }

        override fun onEdit() {
            try {
                stop()
                val quranEntity = trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = EditTextFragment.getInstance(iEdiTextCallback, quranEntity)
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(null)
            } catch (unused: Exception) {
            }
        }

        override fun onCut() {
            try {
                stop()
                splitEntity(trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity)
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onDuplicate() {
            try {
                stop()
                duplicateEntity(trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity)
                updateTime()
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun fromTheStart() {
            stop()
            trackViewEntity.translateFromStart()
        }

        override fun fromNow() {
            stop()
            trackViewEntity.translateFromNow()
        }

        override fun untilNow() {
            stop()
            trackViewEntity.translateUntilNow()
        }

        override fun untilTheEnd() {
            stop()
            trackViewEntity.translateEndNow()
        }
    }

    @Suppress("TYPE_CHECKING_HAS_RUN_INTO_RECURSIVE_PROBLEM") private val iEditTrstEntityCallback = object : EditTrslEntityFragment.IEditEntityCallback {
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {
            blurredImageView.setTrslPreset(hazem.nurmontage.videoquran.core.common.Constants.AyaTextPreset.values()[ayaTextPreset.ordinal])
        }

        override fun updateAya(i: Int) {
            blurredImageView.setColorTrsl(i)
        }

        override fun updateTrsl(i: Int) {
            blurredImageView.setColorTrsl(i)
        }

        override fun onFont() {
            try {
                stop()
                val translationQuranEntity = trackViewEntity.selectedEntity!!.getEntityView() as TranslationQuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = FontFragment.getInstance(iFontCallback, translationQuranEntity.nameFont!!, translationQuranEntity.getPaintAya().typeface)
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.font))
            } catch (unused: Exception) {
            }
        }

        override fun onIcon() {
            try {
                stop()
                val quranEntity = trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = EditIconQuranFragment.getInstance(iQuranIconCallback, quranEntity.icon)
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.icon))
            } catch (unused: Exception) {
            }
        }

        override fun onAnim() {
            try {
                stop()
                val quranEntity = trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = EffectAyaFragment.get(
                    quranEntity.entityQuran!!.getTransition(),
                    mResources!!, iTransitionCallback,
                    trackViewEntity.selectedEntity as EntityQuranTimeline
                )
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.animtion))
            } catch (unused: Exception) {
            }
        }

        override fun onDelete() {
            try {
                stop()
                trackViewEntity.deleteEntity(true)
                updateTime()
                iTrimLineCallback.onEmptySelect()
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onDone() {
            hideFragment()
            if (blurredImageView.entity_select is TranslationQuranEntity) {
                iTrimLineCallback.onSelectEntity(trackViewEntity.selectedEntity!!, -1.0f)
            }
        }

        override fun onColor() {
            try {
                stop()
                val translationQuranEntity = trackViewEntity.selectedEntity!!.getEntityView() as TranslationQuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                val fragTrslObj = createColorTrslAyaFragment(getEditTrstEntityCb(), translationQuranEntity, mResources!!)
                mCurrentFragment = fragTrslObj
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(null)
            } catch (unused: Exception) {
            }
        }

        override fun onEdit() {
            try {
                stop()
                isToCrop = true
                val translationQuranEntity = trackViewEntity.selectedEntity!!.getEntityView() as TranslationQuranEntity
                val intent = Intent(this@EngineActivity, EditTrslTxtActivity::class.java)
                intent.putExtra("surah_name", "")
                intent.putExtra("reader_name", translationQuranEntity.txt!!)
                intent.putExtra("isBg", translationQuranEntity.isHaveBg)
                intent.putExtra("clrBg", translationQuranEntity.clrBg)
                editTrslResult.launch(intent)
                overridePendingTransition(0, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCut() {
            try {
                stop()
                splitEntity(trackViewEntity.selectedEntity!!.getEntityView() as TranslationQuranEntity)
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onDuplicate() {
            try {
                stop()
                duplicateEntity(trackViewEntity.selectedEntity!!.getEntityView() as TranslationQuranEntity)
                updateTime()
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun fromTheStart() {
            stop()
            trackViewEntity.translateFromStart()
        }

        override fun fromNow() {
            stop()
            trackViewEntity.translateFromNow()
        }

        override fun untilNow() {
            stop()
            trackViewEntity.translateUntilNow()
        }

        override fun untilTheEnd() {
            stop()
            trackViewEntity.translateEndNow()
        }
    }

    private val iEditMultipleCallback = object : EditMultipleEntityFragment.IEditMultipleCallback {
        override fun onDelete() {
            stop()
            dialogDeleteSelected()
        }
    }

    private val iEditMediaCallback: EditMediaFragment.IEditMediaCallback = object : EditMediaFragment.IEditMediaCallback {
        override fun onReplace() {}



        override fun updateEntity(effectAudioType: EffectAudioType, entityAudio: EntityAudio) {
            for (i in trackViewEntity.entityListAudio.indices) {
                val entityAudio2 = trackViewEntity.entityListAudio[i]
                if (entityAudio2 !== entityAudio && entityAudio2.visible()) {
                    if (effectAudioType == EffectAudioType.ECHO) {
                        entityAudio2.effectAudio.decays = entityAudio.effectAudio.decays
                        entityAudio2.effectAudio.delays = entityAudio.effectAudio.delays
                        entityAudio2.effectAudio.outGain = entityAudio.effectAudio.outGain
                        entityAudio2.effectAudio.decays_cmd = entityAudio.effectAudio.decays_cmd
                        entityAudio2.effectAudio.delays_cmd = entityAudio.effectAudio.delays_cmd
                    }
                    if (effectAudioType == EffectAudioType.NOICE) {
                        entityAudio2.effectAudio.isRemoveNoice = entityAudio.effectAudio.isRemoveNoice
                    }
                    if (effectAudioType == EffectAudioType.ENHANCE) {
                        entityAudio2.effectAudio.isEnhance = entityAudio.effectAudio.isEnhance
                    }
                    if (effectAudioType == EffectAudioType.SPEED) {
                        entityAudio2.effectAudio.speed = entityAudio.effectAudio.speed
                    }
                    if (effectAudioType == EffectAudioType.REVERB) {
                        entityAudio2.effectAudio.reverbPreset = entityAudio.effectAudio.reverbPreset
                        entityAudio2.effectAudio.reverbPreset_index_list = entityAudio.effectAudio.reverbPreset_index_list
                    }
                    if (effectAudioType == EffectAudioType.VOLUME) {
                        entityAudio2.effectAudio.volume = entityAudio.effectAudio.volume
                    }
                    if (effectAudioType == EffectAudioType.FADE) {
                        entityAudio2.effectAudio.fade_in = entityAudio.effectAudio.fade_in
                        entityAudio2.effectAudio.fade_out = entityAudio.effectAudio.fade_out
                    }
                }
            }
        }

        override fun onDone() {
            pausePreview()
            hideFragment()
            iTrimLineCallback.onSelectEntity(trackViewEntity.selectedEntity!!, -1.0f)
        }

        override fun startPreview() {
            if (trackViewEntity.selectedEntity is EntityAudio) {
                val entityAudio = trackViewEntity.selectedEntity!! as EntityAudio
                if (entityAudio.mediaPlayer!!.isPlaying) {
                    return
                }
                trackViewEntity.previewEntity(entityAudio)
                mIsPlaying = true
                trackViewEntity.translateToStart(entityAudio)
                startCursur = trackViewEntity.current_cursur_position
                startTimelineAnimationPreview(entityAudio)
            }
        }

        override fun pausePreview() {
            if (mIsPlaying && trackViewEntity.selectedEntity is EntityAudio) {
                val entityAudio = trackViewEntity.selectedEntity!! as EntityAudio
                mIsPlaying = false
                pauseTimelineAnimation()
                trackViewEntity.isPlaying = mIsPlaying
                blurredImageView.isPlaying = mIsPlaying
                try {
                    if (entityAudio.mediaPlayer == null || !entityAudio.mediaPlayer!!.isPlaying) {
                        return
                    }
                    entityAudio.mediaPlayer!!.pause()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onCmdPlay(str: String) {
            pausePreview()
            if (trackViewEntity.selectedEntity is EntityAudio) {
                applyffectPlayAuto(str, trackViewEntity.selectedEntity!! as EntityAudio)
            }
        }

        override fun onCmd(str: String) {
            pausePreview()
            if (trackViewEntity.selectedEntity is EntityAudio) {
                applyffect(str, trackViewEntity.selectedEntity!! as EntityAudio)
            }
        }

        override fun onCmdAll(effectAudio: EffectAudio) {
            pausePreview()
            showProgressSimple()
            applyffectAll(effectAudio, 0)
        }

        override fun onDuplicate() {
            try {
                if (trackViewEntity.selectedEntity is EntityAudio) {
                    val entityAudio = trackViewEntity.selectedEntity!! as EntityAudio
                    stop()
                    duplicateEntityAudio(entityAudio.mediaPlayer!!.duration, entityAudio)
                    updateTime()
                }
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onDelete() {
            try {
                stop()
                trackViewEntity.deleteMediaEntity()
                updateTime()
                iTrimLineCallback.onEmptySelect()
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onCut() {
            try {
                stop()
                if (trackViewEntity.selectedEntity is EntityAudio) {
                    val entityAudio = trackViewEntity.selectedEntity!! as EntityAudio
                    val abs = Math.abs(trackViewEntity.getCurrentPosition())
                    if (abs >= entityAudio.rect.left && abs <= entityAudio.rect.right) {
                        val second_in_screenNoScale = trackViewEntity.second_in_screenNoScale * 0.1f
                        if (abs <= entityAudio.rect.left || abs >= entityAudio.rect.left + second_in_screenNoScale) {
                            if (abs >= entityAudio.rect.right || abs <= entityAudio.rect.right - second_in_screenNoScale) {
                                val round = Math.round(
                                    (Math.abs(Math.round((trackViewEntity.getCurrentPosition() / trackViewEntity.getSecond_in_screen()) * 1000.0f)) -
                                            Math.abs(Math.round((entityAudio.rect.left / trackViewEntity.getSecond_in_screen()) * 1000.0f))).toFloat()
                                ) + entityAudio.start
                                val split = entityAudio.split(abs)
                                split.setAmps(entityAudio.getAmps())
                                split.setRenderer(entityAudio.getRenderer())
                                split.addPathHttp(entityAudio.pathsHttp)
                                split.setPathFfmpegEffect(entityAudio.getPathFfmpegEffect()!!)
                                split.setVideoPath(entityAudio.videoPath!!)
                                split.setApplyEffectInPreview(entityAudio.isApplyEffectInPreview)
                                split.effectAudio = entityAudio.effectAudio
                                split.scaleFactor = entityAudio.scaleFactor
                                split.mediaPlayer = entityAudio.mediaPlayer
                                split.setPathFfmpeg(entityAudio.getPathFfmpeg()!!)
                                split.index = entityAudio.index + 1
                                split.end = entityAudio.end
                                val f = round.toFloat()
                                split.start = f
                                split.setMinDuration(round.toInt())
                                trackViewEntity.splitAudio(split, split.index)
                                trackViewEntity.stackSplit(entityAudio)
                                entityAudio.setCurrentRect()
                                entityAudio.right = abs
                                entityAudio.max = (entityAudio.rect.right / entityAudio.scaleFactor) - ((entityAudio.rect.left / entityAudio.scaleFactor) - entityAudio.getOffsetLeft())
                                entityAudio.end = f
                                split.setOffsetRight(entityAudio.getOffsetRight())
                                entityAudio.setOffsetRight(0.0f)
                                split.offset = entityAudio.offset + entityAudio.getOffsetLeft() + (entityAudio.rect.width() / entityAudio.scaleFactor)
                                entityAudio.onChange()
                                split.secondInScreen = trackViewEntity.second_in_screenNoScale
                                split.updateEffect()
                                entityAudio.updateEffect()
                                trackViewEntity.stackSplit(split)
                                trackViewEntity.invalidate()
                            }
                        }
                    }
                }
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun reverbEffect() {
            stop()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.selectedEntity!! as EntityAudio
            mCurrentFragment = ReverbePresetFragment.getInstance(this, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun echoEffect() {
            stop()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.selectedEntity!! as EntityAudio
            mCurrentFragment = EchoEffectFragment.getInstance(this, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun noice() {
            stop()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.selectedEntity!! as EntityAudio
            mCurrentFragment = RemoveNoiceFragment.getInstance(this, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun enhanceVoice() {
            stop()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.selectedEntity!! as EntityAudio
            mCurrentFragment = EnhanceVoiceFragment.getInstance(this, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun speedEffect() {
            stop()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.selectedEntity!! as EntityAudio
            mCurrentFragment = SpeedFragment.getInstance(this, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun volumeEffect() {
            stop()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.selectedEntity!! as EntityAudio
            mCurrentFragment = VolumeFragment.getInstance(this, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun pitchEffect() {
            stop()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.selectedEntity!! as EntityAudio
            mCurrentFragment = PitchFragment.getInstance(this, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun fadeEffect() {
            stop()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.selectedEntity!! as EntityAudio
            mCurrentFragment = FadeInOutFragment.getInstance(this, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }
    }

    private val iEdiTextCallback = object : EditTextFragment.IEdiTextCallback {
        override fun onDone(entityQuranTimeline: EntityQuranTimeline?) {
            setupHideFragment()
            if (entityQuranTimeline != null) {
                showEditEntity(entityQuranTimeline)
            }
        }

        override fun onUpdate(quranEntity: QuranEntity?) {
            blurredImageView.postInvalidate()
            trackViewEntity.postInvalidate()
        }
    }

    private val iTransitionCallback = object : TransitionEntityAdabters.ITransition {

        override fun destroy(entityQuranTimeline: EntityQuranTimeline) {
            if (entityQuranTimeline == null) {
                return
            }
            entityQuranTimeline!!.quranEntity.isAnimTest = false
            entityQuranTimeline!!.quranEntity.endAnimator()
            blurredImageView.invalidate()
        }

        override fun playing(entityQuranTimeline: EntityQuranTimeline) {
            entityQuranTimeline!!.quranEntity.isAnimTest = true
        }

        override fun onHideFragment(entityQuranTimeline: EntityQuranTimeline) {
            hideFragment()
            try {
                iTrimLineCallback.onSelectEntity(entityQuranTimeline, -1.0f)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun `in`(str: String, entityQuranTimeline: EntityQuranTimeline) {
            if (entityQuranTimeline == null) {
                return
            }
            if (entityQuranTimeline!!.getTransition() == null) {
                entityQuranTimeline!!.setTransition(Transition())
            }
            entityQuranTimeline!!.getTransition()!!.isIn = true
            entityQuranTimeline!!.getTransition()!!.type_in = str
            EffectAyaFragment.instance!!.updateView(entityQuranTimeline!!.getTransition()!!.duration_in, entityQuranTimeline!!.getTransition()!!)
            entityQuranTimeline!!.quranEntity.endAnimator()
            entityQuranTimeline!!.quranEntity.runIn((entityQuranTimeline!!.getTransition()!!.duration_in * 1000.0f).toInt(), true, entityQuranTimeline!!.getTransition()!!.type_in)
        }

        override fun `out`(str: String, entityQuranTimeline: EntityQuranTimeline) {
            if (entityQuranTimeline == null) {
                return
            }
            if (entityQuranTimeline!!.getTransition() == null) {
                entityQuranTimeline!!.setTransition(Transition())
            }
            entityQuranTimeline!!.getTransition()!!.isOut = true
            entityQuranTimeline!!.getTransition()!!.type_out = str
            EffectAyaFragment.instance!!.updateView(entityQuranTimeline!!.getTransition()!!.duration_out, entityQuranTimeline!!.getTransition()!!)
            entityQuranTimeline!!.quranEntity.endAnimator()
            entityQuranTimeline!!.quranEntity.runOut((entityQuranTimeline!!.getTransition()!!.duration_out * 1000.0f).toInt(), true, entityQuranTimeline!!.getTransition()!!.type_out)
        }

        override fun remove(i: Int, entityQuranTimeline: EntityQuranTimeline) {
            if (i == 0) {
                entityQuranTimeline!!.getTransition()!!.isIn = false
                entityQuranTimeline!!.quranEntity.endAnimator()
            }
            if (i == 1) {
                entityQuranTimeline!!.getTransition()!!.isOut = false
                entityQuranTimeline!!.quranEntity.endAnimator()
            }
        }

        override fun updateDurationIn(f: Float, entityQuranTimeline: EntityQuranTimeline) {
            if (entityQuranTimeline == null) {
                return
            }
            entityQuranTimeline!!.getTransition()!!.duration_in = f
            entityQuranTimeline!!.quranEntity.endAnimator()
            entityQuranTimeline!!.quranEntity.runIn((entityQuranTimeline!!.getTransition()!!.duration_in * 1000.0f).toInt(), true, entityQuranTimeline!!.getTransition()!!.type_in)
        }

        override fun updateDurationOut(f: Float, entityQuranTimeline: EntityQuranTimeline) {
            if (entityQuranTimeline == null) {
                return
            }
            entityQuranTimeline!!.getTransition()!!.duration_out = f
            entityQuranTimeline!!.quranEntity.endAnimator()
            entityQuranTimeline!!.quranEntity.runOut((entityQuranTimeline!!.getTransition()!!.duration_out * 1000.0f).toInt(), true, entityQuranTimeline!!.getTransition()!!.type_out)
        }

        override fun applyAll(i: Int, entityQuranTimeline: EntityQuranTimeline) {
            showProgress()
            addUpdateAnim(trackViewEntity.getmIsi3adaTimeline(), entityQuranTimeline)
            addUpdateAnim(trackViewEntity.bismilahTimeline, entityQuranTimeline)
            for (entityQuranTimeline2 in trackViewEntity.entityListQuran) {
                if (entityQuranTimeline2 !== entityQuranTimeline) {
                    if (entityQuranTimeline!!.getTransition() == null) {
                        entityQuranTimeline2!!.setTransition(null)
                        return
                    }
                    if (entityQuranTimeline2!!.getTransition() == null) {
                        entityQuranTimeline2!!.setTransition(Transition())
                    }
                    entityQuranTimeline2!!.getTransition()!!.isOut = entityQuranTimeline!!.getTransition()!!.isOut
                    entityQuranTimeline2!!.getTransition()!!.type_out = entityQuranTimeline!!.getTransition()!!.type_out
                    entityQuranTimeline2!!.getTransition()!!.duration_out = entityQuranTimeline!!.getTransition()!!.duration_out
                    entityQuranTimeline2!!.getTransition()!!.isIn = entityQuranTimeline!!.getTransition()!!.isIn
                    entityQuranTimeline2!!.getTransition()!!.type_in = entityQuranTimeline!!.getTransition()!!.type_in
                    entityQuranTimeline2!!.getTransition()!!.duration_in = entityQuranTimeline!!.getTransition()!!.duration_in
                }
            }
            hideProgressFragment()
        }
    }

    private val iTransitionBismilahCallback = object : TransitionBismilahAdabters.ITransition {
        override fun destroy(entityBismilahTimeline: EntityBismilahTimeline) {
            entityBismilahTimeline.quranEntity.isAnimTest = false
            entityBismilahTimeline.quranEntity.endAnimator()
            blurredImageView.invalidate()
        }

        override fun playing(entityBismilahTimeline: EntityBismilahTimeline) {
            entityBismilahTimeline.quranEntity.isAnimTest = true
        }

        override fun onHideFragment(entityBismilahTimeline: EntityBismilahTimeline) {
            hideFragment()
            try {
                iTrimLineCallback.onSelectEntity(entityBismilahTimeline, -1.0f)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun `in`(str: String, entityBismilahTimeline: EntityBismilahTimeline) {
            if (entityBismilahTimeline == null) {
                return
            }
            if (entityBismilahTimeline.getTransition() == null) {
                entityBismilahTimeline.setTransition(Transition())
            }
            entityBismilahTimeline.getTransition()!!.isIn = true
            entityBismilahTimeline.getTransition()!!.type_in = str
            EffectBismilahFragment.instance!!.updateView(entityBismilahTimeline.getTransition()!!.duration_in, entityBismilahTimeline.getTransition()!!)
            entityBismilahTimeline.quranEntity.endAnimator()
            entityBismilahTimeline.quranEntity.runIn((entityBismilahTimeline.getTransition()!!.duration_in * 1000.0f).toInt(), true, entityBismilahTimeline.getTransition()!!.type_in)
        }

        override fun `out`(str: String, entityBismilahTimeline: EntityBismilahTimeline) {
            if (entityBismilahTimeline == null) {
                return
            }
            if (entityBismilahTimeline.getTransition() == null) {
                entityBismilahTimeline.setTransition(Transition())
            }
            entityBismilahTimeline.getTransition()!!.isOut = true
            entityBismilahTimeline.getTransition()!!.type_out = str
            EffectBismilahFragment.instance!!.updateView(entityBismilahTimeline.getTransition()!!.duration_out, entityBismilahTimeline.getTransition()!!)
            entityBismilahTimeline.quranEntity.endAnimator()
            entityBismilahTimeline.quranEntity.runOut((entityBismilahTimeline.getTransition()!!.duration_out * 1000.0f).toInt(), true, entityBismilahTimeline.getTransition()!!.type_out)
        }

        override fun remove(i: Int, entityBismilahTimeline: EntityBismilahTimeline) {
            if (i == 0) {
                entityBismilahTimeline.getTransition()!!.isIn = false
                entityBismilahTimeline.quranEntity.endAnimator()
            }
            if (i == 1) {
                entityBismilahTimeline.getTransition()!!.isOut = false
                entityBismilahTimeline.quranEntity.endAnimator()
            }
        }

        override fun updateDurationIn(f: Float, entityBismilahTimeline: EntityBismilahTimeline) {
            if (entityBismilahTimeline == null) {
                return
            }
            entityBismilahTimeline.getTransition()!!.duration_in = f
            entityBismilahTimeline.quranEntity.endAnimator()
            entityBismilahTimeline.quranEntity.runIn((entityBismilahTimeline.getTransition()!!.duration_in * 1000.0f).toInt(), true, entityBismilahTimeline.getTransition()!!.type_in)
        }

        override fun updateDurationOut(f: Float, entityBismilahTimeline: EntityBismilahTimeline) {
            if (entityBismilahTimeline == null) {
                return
            }
            entityBismilahTimeline.getTransition()!!.duration_out = f
            entityBismilahTimeline.quranEntity.endAnimator()
            entityBismilahTimeline.quranEntity.runOut((entityBismilahTimeline.getTransition()!!.duration_out * 1000.0f).toInt(), true, entityBismilahTimeline.getTransition()!!.type_out)
        }

        override fun applyAll(entityBismilahTimeline: EntityBismilahTimeline) {
            showProgress()
            addUpdateAnim(
                if (trackViewEntity.getmIsi3adaTimeline() !== entityBismilahTimeline) trackViewEntity.getmIsi3adaTimeline() else trackViewEntity.bismilahTimeline,
                entityBismilahTimeline
            )
            for (entityQuranTimeline in trackViewEntity.entityListQuran) {
                if (entityBismilahTimeline.getTransition() == null) {
                    entityQuranTimeline!!.setTransition(null)
                    return
                }
                if (entityQuranTimeline!!.getTransition() == null) {
                    entityQuranTimeline!!.setTransition(Transition())
                }
                entityQuranTimeline!!.getTransition()!!.isOut = entityBismilahTimeline.getTransition()!!.isOut
                entityQuranTimeline!!.getTransition()!!.type_out = entityBismilahTimeline.getTransition()!!.type_out
                entityQuranTimeline!!.getTransition()!!.duration_out = entityBismilahTimeline.getTransition()!!.duration_out
                entityQuranTimeline!!.getTransition()!!.isIn = entityBismilahTimeline.getTransition()!!.isIn
                entityQuranTimeline!!.getTransition()!!.type_in = entityBismilahTimeline.getTransition()!!.type_in
                entityQuranTimeline!!.getTransition()!!.duration_in = entityBismilahTimeline.getTransition()!!.duration_in
            }
            hideProgressFragment()
        }
    }

    private val frameLock = Any()
    private var pendingFramePath: String? = null
    private var isProcessingFrame: Boolean = false

    private val frameProcessorRunnable = Runnable {
        var str: String?
        while (true) {
            synchronized(frameLock) {
                if (pendingFramePath == null) {
                    isProcessingFrame = false
                    return@Runnable
                } else {
                    str = pendingFramePath
                    pendingFramePath = null
                }
            }
            processFrame(str!!)
        }
    }


// --- Continuation of initTypeVideo anonymous Runnable (from Part 1) ---
// Lines 2700-2868 are the tail end of the initTypeVideo executor Runnable.
// The full method was started in Part 1; this fragment closes that block.

// =====================================================================
// iniTypeImg() - private method
// =====================================================================

private fun iniTypeImg() {
    executor.execute(Runnable {
        var bitmap: Bitmap
        var cropTo16x9: Bitmap
        var cropToSquareWithRoundCorners: Bitmap? = null
        var bitmap2: Bitmap
        var rect: Rect
        var clrTrsl: Int
        try {
            blurredImageView.initCanvasDimension(blurredImageView.getW(), blurredImageView.getH(), mTemplate!!.geTypeResize())
            val height = blurredImageView.getH()
            try {
                bitmap = Glide.with(this@EngineActivity as FragmentActivity).asBitmap()
                    .load(mTemplate!!.uri_bg)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(height, height)
                    .submit().get() as Bitmap
            } catch (unused: Exception) {
                mTemplate!!.color_ipad = -1
                bitmap = Glide.with(this@EngineActivity as FragmentActivity).asBitmap()
                    .load(R.drawable.bg_19)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(height, height)
                    .submit().get() as Bitmap
            }
            blurredImageView.bitmapOriginal = setupOriginalBitmap(bitmap, height)
            cropTo16x9 = when (mTemplate!!.geTypeResize()) {
                ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(blurredImageView.bitmapOriginal!!, blurredImageView.getW(), blurredImageView.getH())!!
                ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(blurredImageView.bitmapOriginal!!, blurredImageView.getW(), blurredImageView.getH())!!
                else -> BitmapCropper.cropTo16x9(blurredImageView.bitmapOriginal!!, blurredImageView.getW(), blurredImageView.getH())!!
            }
            blurredImageView.isGlass = mTemplate!!.isGlass
            blurredImageView.isVideo = false
            blurredImageView.updatePosCanvas(cropTo16x9)
            blurredImageView.updateIpad(cropTo16x9, mTemplate!!.ipad_type, mTemplate!!.geTypeResize())

            if (mTemplate!!.ipad_type == IpadType.IPAD_NEOMORPHIC.ordinal) {
                val width = (blurredImageView.ipad_rect!!.width() * 0.6f).toInt()
                var round = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                var round2 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                var i3 = width + round
                if (i3 > blurredImageView.bitmapOriginal!!.width) {
                    round -= i3 - blurredImageView.bitmapOriginal!!.width
                    i3 = blurredImageView.bitmapOriginal!!.width
                }
                var i4 = width + round2
                if (i4 > blurredImageView.bitmapOriginal!!.height) {
                    round2 -= i4 - blurredImageView.bitmapOriginal!!.height
                    i4 = blurredImageView.bitmapOriginal!!.height
                }
                if (round < 0) round = 0
                if (round2 >= 0) {
                    // i2 = round2 (in original decompiled code)
                }
                val i2 = if (round2 >= 0) round2 else 0
                val rect2 = Rect(round, i2, i3, i4)
                blurredImageView.radius_square = width
                val width2 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                val height2 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.bitmapOriginal!!, rect2, width, width2, height2)
                rect2.right = rect2.left + width2
                rect2.bottom = rect2.top + height2
                blurredImageView.rectSquare = rect2
                bitmap2 = cropToSquareWithRoundCorners2
                rect = rect2
            } else {
                if (mTemplate!!.ipad_type != IpadType.IPAD.ordinal && mTemplate!!.ipad_type != IpadType.IPAD_UNBLUR.ordinal && mTemplate!!.ipad_type != IpadType.IPAD_CLASSIC.ordinal) {
                    val width3 = (blurredImageView.ipad_rect!!.width() * 1.0f).toInt()
                    val height3 = (cropTo16x9!!.height * 0.5355f).toInt()
                    var round3 = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                    var round4 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                    var i5 = width3 + round3
                    if (i5 > blurredImageView.bitmapOriginal!!.width) {
                        round3 -= i5 - blurredImageView.bitmapOriginal!!.width
                        i5 = blurredImageView.bitmapOriginal!!.width
                    }
                    var i6 = height3 + round4
                    if (i6 > blurredImageView.bitmapOriginal!!.height) {
                        round4 -= i6 - blurredImageView.bitmapOriginal!!.height
                        i6 = blurredImageView.bitmapOriginal!!.height
                    }
                    if (round3 < 0) round3 = 0
                    if (round4 < 0) round4 = 0
                    val rect3 = Rect(round3, round4, i5, i6)
                    val width4 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                    val height4 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                    val cropToSquare = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal!!, rect3, width4, height4)
                    blurredImageView.bitmapSquare = cropToSquare
                    blurredImageView.radius_square = 0
                    rect3.right = rect3.left + width4
                    rect3.bottom = rect3.top + height4
                    blurredImageView.rectSquare = rect3
                    bitmap2 = cropToSquare
                    rect = rect3
                }
                val width5 = (blurredImageView.ipad_rect!!.width() * 0.87530595f).toInt()
                val i7 = (width5 * 1.13f).toInt()
                val min = Math.min(width5, i7)
                var round5 = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                var round6 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                var i8 = width5 + round5
                if (i8 > blurredImageView.bitmapOriginal!!.width) {
                    round5 -= i8 - blurredImageView.bitmapOriginal!!.width
                    i8 = blurredImageView.bitmapOriginal!!.width
                }
                var i9 = i7 + round6
                if (i9 > blurredImageView.bitmapOriginal!!.height) {
                    round6 -= i9 - blurredImageView.bitmapOriginal!!.height
                    i9 = blurredImageView.bitmapOriginal!!.height
                }
                if (round5 < 0) round5 = 0
                if (round6 < 0) round6 = 0
                val rect4 = Rect(round5, round6, i8, i9)
                if (mTemplate!!.ipad_type == IpadType.IPAD_CLASSIC.ordinal) {
                    val width6 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                    val height5 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                    val cropToSquare2 = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal!!, rect4, width6, height5)
                    blurredImageView.bitmapSquare = cropToSquare2
                    blurredImageView.radius_square = 0
                    rect4.right = rect4.left + width6
                    rect4.bottom = rect4.top + height5
                    blurredImageView.rectSquare = rect4
                    cropToSquareWithRoundCorners = cropToSquare2
                } else {
                    val i10 = (min * 0.10800001f).toInt()
                    blurredImageView.radius_square = i10
                    val width7 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                    val height6 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                    cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.bitmapOriginal!!, rect4, i10, width7, height6)
                    rect4.right = rect4.left + width7
                    rect4.bottom = rect4.top + height6
                    blurredImageView.rectSquare = rect4
                }
                bitmap2 = cropToSquareWithRoundCorners
                rect = rect4
            }
            if (mTemplate!!.gradient != null) {
                blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1), bitmap2, mTemplate!!.gradient!!, mTemplate!!.ipad_type, mTemplate!!.geTypeResize(), rect)
            } else {
                blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1), bitmap2, mTemplate!!.color_ipad, mTemplate!!.ipad_type, mTemplate!!.geTypeResize(), rect)
            }
            clrTrsl = if (mTemplate!!.ipad_type == IpadType.BLUE_TYPE.ordinal) {
                blurredImageView.paintLecture.color
            } else {
                if (blurredImageView.paintLecture.color == -1) InputDeviceCompat.SOURCE_ANY else Constants.COLOR_TRANSLATION
            }
            blurredImageView.clr_trsl = clrTrsl
            blurredImageView.clr_aya = blurredImageView.paintLecture.color
            addEntityFromTemplate(EntityQuranTemplate())
        } catch (e: Exception) {
            Log.e("Tag : ", "init ${e.message}")
        }
    })
}

// =====================================================================
// initResolution() - private method
// =====================================================================

private fun initResolution() {
    tv_resolution = findViewById<TextCustumFont>(R.id.tv_resolution)
    layout_resolution = findViewById<LinearLayout>(R.id.layout_resolution)
    val linearLayout = findViewById<LinearLayout>(R.id.btn_setup_fps)
    btn_setup_fps = linearLayout
    linearLayout.setOnClickListener {
        if (layout_resolution == null) return@setOnClickListener
        if (layout_resolution?.visibility != View.VISIBLE) {
            layout_resolution?.visibility = View.VISIBLE
        } else {
            layout_resolution?.visibility = View.GONE
        }
    }
    seekBar_fps = findViewById(R.id.seekBar_fps)
    when (mTemplate!!.fps) {
        15 -> seekBar_fps.setProgress(0)
        25 -> seekBar_fps.setProgress(1)
        30 -> seekBar_fps.setProgress(2)
        50 -> seekBar_fps.setProgress(3)
        else -> seekBar_fps.setProgress(4)
    }
    seekBar_fps.setOnProgressChangeListener(object : CustomDiscreteSeekBar.OnProgressChangeListener {
        override fun onProgressChanged(customDiscreteSeekBar: CustomDiscreteSeekBar, i: Int, str: String, z: Boolean) {}
        override fun onStartTrackingTouch(customDiscreteSeekBar: CustomDiscreteSeekBar) {}
        override fun onStopTrackingTouch(customDiscreteSeekBar: CustomDiscreteSeekBar) {
            if (mTemplate != null) {
                mTemplate!!.fps = seekBar_fps.getCurrentLabel().toInt()
            }
        }
    })
    tv_resolution.text = mTemplate!!.resolution
    seekBar_res = findViewById(R.id.seekBar_res)
    when (mTemplate!!.resolution) {
        "480p" -> seekBar_res.setProgress(0)
        "720p" -> seekBar_res.setProgress(1)
        "1080p" -> seekBar_res.setProgress(2)
        else -> seekBar_res.setProgress(3)
    }
    seekBar_res.setOnProgressChangeListener(object : CustomDiscreteSeekBar.OnProgressChangeListener {
        override fun onProgressChanged(customDiscreteSeekBar: CustomDiscreteSeekBar, i: Int, str: String, z: Boolean) {}
        override fun onStartTrackingTouch(customDiscreteSeekBar: CustomDiscreteSeekBar) {}
        override fun onStopTrackingTouch(customDiscreteSeekBar: CustomDiscreteSeekBar) {
            if (mTemplate != null) {
                mTemplate!!.resolution = seekBar_res.getCurrentLabel()
                val size = AspectRatioCalculator.getSize(mTemplate!!.geTypeResize(), mTemplate!!.resolution)
                tv_resolution.text = mTemplate!!.resolution
                mTemplate!!.setWidthAndHeight(size.first, size.second)
            }
        }
    })
}

// =====================================================================
// initViews() - private method
// =====================================================================

private fun initViews() {
    initResolution()
    val imageButton = findViewById<ImageButton>(R.id.btn_play_pause)
    btnPlayPause = imageButton
    imageButton.setOnClickListener {
        hideLayoutResolution()
        if (mIsPlaying) {
            mIsPlaying = false
            pauseTimelineAnimation()
            trackViewEntity.isPlaying = mIsPlaying
            blurredImageView.isPlaying = mIsPlaying
            trackViewEntity.invalidate()
            for (entityAudio in trackViewEntity.entityListAudio) {
                try {
                    if (entityAudio.visible() && entityAudio.mediaPlayer != null && entityAudio.mediaPlayer!!.isPlaying) {
                        entityAudio.mediaPlayer!!.pause()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            btnPlayPause.setImageResource(R.drawable.play_btn)
            return@setOnClickListener
        }
        if (current_position_time == 0) {
            trackViewEntity.updateCursur(0)
        }
        trackViewEntity.calculMaxTime()
        btnPlayPause.setImageResource(R.drawable.pause_24px)
        updateBtnToEnd(); updateBtnToStart()
        current_position_time = System.currentTimeMillis().toInt()
        mIsPlaying = true
        trackViewEntity.isPlaying = true
        blurredImageView.isPlaying = true
        startTimelineAnimation()
    }
    val imageButton2 = findViewById<ImageButton>(R.id.btn_to_end)
    btnToEnd = imageButton2
    imageButton2.setOnClickListener {
        if (trackViewEntity.current_cursur_position == trackViewEntity.maxTime) return@setOnClickListener
        blurredImageView.progress = 1.0f
        stop()
        startCursur = 0
        trackViewEntity.translateToEnd()
        updateViewTime(trackViewEntity.maxTime, trackViewEntity.current_cursur_position)
        updateBtnToEnd()
        updateBtnToStart()
    }
    val imageButton3 = findViewById<ImageButton>(R.id.btn_to_start)
    btnToStart = imageButton3
    imageButton3.setOnClickListener {
        if (trackViewEntity.current_cursur_position == 0) return@setOnClickListener
        blurredImageView.progress = 0.0f
        stop()
        startCursur = 0
        trackViewEntity.translateToStart()
        updateViewTime(trackViewEntity.maxTime, trackViewEntity.current_cursur_position)
        updateBtnToStart()
        updateBtnToEnd()
    }
    updateBtnToStart()
    btnRedo = findViewById(R.id.btn_redo)
    btnUndo = findViewById(R.id.btn_undo)
    disableUndoBtn()
    disableRedoBtn()
    btnRedo.setOnClickListener(object : View.OnClickListener {
        override fun onClick(view: View) {
            stop()
            showProgressSimple()
            Thread(Runnable {
                runOnUiThread {
                    trackViewEntity.redo()
                    hideProgressFragment()
                }
            }).start()
        }
    })
    btnUndo.setOnClickListener(object : View.OnClickListener {
        override fun onClick(view: View) {
            stop()
            showProgressSimple()
            Thread(Runnable {
                runOnUiThread {
                    trackViewEntity.undo()
                    hideProgressFragment()
                }
            }).start()
        }
    })
    trackViewEntity.setRedoUndo(btnRedo, btnUndo)
    val blurredImgView = findViewById<BlurredImageView>(R.id.view)
    blurredImageView = blurredImgView
    blurredImgView.setPro(false) // MyPreferences.isSubscribed(this@EngineActivity) // TODO: implement
    blurredImageView.setiViewCallback(object : BlurredImageView.IViewCallback {
        override fun onDrawFinish() {}
        override fun onSquare() {}
        override fun onEndMove() {
            if (blurredImageView.entity_select != null) {
                blurredImageView.applyAll(blurredImageView.entity_select!!.scaleFactor, blurredImageView.entity_select!!.rect, blurredImageView.entity_select!!.max_w, blurredImageView.entity_select!!.max_h)
            }
        }
        override fun onEndScale() {
            if (blurredImageView.entity_select != null) {
                blurredImageView.applyAll(blurredImageView.entity_select!!.scaleFactor, blurredImageView.entity_select!!.rect, blurredImageView.entity_select!!.max_w, blurredImageView.entity_select!!.max_h)
            }
        }
        override fun onSelect(entityView: EntityView) {
            if (entityView is SurahNameEntity) {
                try {
                    if (EditS_NameFragment.instance != null) return
                    stop()
                    selectSurahName()
                    return
                } catch (unused: Exception) {
                    return
                }
            }
            if (entityView is QuranEntity) {
                trackViewEntity.selectEntity(entityView.entityQuran, true)
                iTrimLineCallback.onSelectEntity(entityView.entityQuran!!, 0.0f)
            } else if (entityView is BismilahEntity) {
                val bismilahTimeline = (entityView as BismilahEntity).bismilahTimeline
                trackViewEntity.selectEntity(bismilahTimeline, true)
                iTrimLineCallback.onSelectEntity(bismilahTimeline!!, 0.0f)
            } else if (entityView is TranslationQuranEntity) {
                trackViewEntity.selectEntity(entityView.entityTrslTimeline, true)
                iTrimLineCallback.onSelectEntity(entityView.entityTrslTimeline!!, 0.0f)
            }
        }
        override fun onEmtyClick() {
            iTrimLineCallback.onEmptySelect()
        }
        override fun onWattermark() {
            dialogWatermark()
        }
    })
    if (blurredImageView.isPro) {
        findViewById<View>(R.id.to_pro).visibility = View.GONE
    } else {
        findViewById<View>(R.id.to_pro).setOnClickListener {
            toProVersion()
        }
    }
    blurredImageView.post {
        if (mTemplate!!.isVideoSquare) {
            initTypeVideo()
        } else {
            iniTypeImg()
        }
    }
    val buttonCustumFont = findViewById<ButtonCustumFont>(R.id.btn_export)
    btn_export = buttonCustumFont
    buttonCustumFont.text = mResources!!.getString(R.string.export)
    btn_export.setOnClickListener {
        isSaveTmpTemplate = false
        stop()
        if (Build.VERSION.SDK_INT >= 33) {
            save()
        } else if (ContextCompat.checkSelfPermission(this@EngineActivity, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            save()
        } else {
            ActivityCompat.requestPermissions(this@EngineActivity, arrayOf("android.permission.WRITE_EXTERNAL_STORAGE"), 1)
        }
    }
    val imageButton4 = findViewById<ImageButton>(R.id.btn_cancel)
    btn_cancel = imageButton4
    imageButton4.setOnClickListener { dialog?.dismiss() }
    tv_tittle_fragment = findViewById(R.id.tv_tittle_fragment)
    (findViewById<TextCustumFont>(R.id.tv_quran)).text = mResources!!.getString(R.string.quran)
    (findViewById<TextCustumFont>(R.id.tv_bg)).text = mResources!!.getString(R.string.bg)
    val textCustumFont = findViewById<TextCustumFont>(R.id.tv_ipad)
    textCustumFont.text = mResources!!.getString(R.string.ipad)
    findViewById<View>(R.id.btn_add_quran).setOnClickListener {
        stop()
        try {
            val beginTransaction = supportFragmentManager.beginTransaction()
            mCurrentFragment = AddQuranFragment.getInstance(iAddQuran, mResources)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
            setupShowFragment(mResources!!.getString(R.string.quran))
        } catch (unused: Exception) {
        }
    }
    findViewById<View>(R.id.btn_bg).setOnClickListener {
        stop()
        try {
            val beginTransaction = supportFragmentManager.beginTransaction()
            mCurrentFragment = ChangeBgFragment.getInstance(iChangeBgCallback, mResources, mTemplate!!.name_drawable)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commitNow()
            setupShowFragment(mResources!!.getString(R.string.bg))
        } catch (unused: Exception) {
        }
    }
    btnIpod = findViewById(R.id.btn_ipad)
    textChangeResize = findViewById(R.id.tv_ratio)
    ivResize = findViewById(R.id.iv_ratio)
    ivIpod = findViewById(R.id.iv_ipod)
    btnChangeResize = findViewById(R.id.btn_change_aspect)
    if (blurredImageView.isPro) {
        btnChangeResize?.setOnClickListener {
            stop()
            try {
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = ResizeFragment.getInstance(iDimensionCallback, mResources, "16")
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(null)
            } catch (unused: Exception) {
            }
        }
    } else {
        textChangeResize?.setTextColor(-8355712)
        ivResize?.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
        btnChangeResize?.setBackgroundColor(0)
        btnChangeResize?.setOnClickListener {
            stop()
            dialogPremium(R.drawable.iv_layout_ipod)
        }
        textCustumFont.setTextColor(-8355712)
        ivIpod?.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
        btnIpod?.setBackgroundColor(0)
    }
    btnIpod?.setOnClickListener {
        stop()
        try {
            val beginTransaction = supportFragmentManager.beginTransaction()
            mCurrentFragment = EditIpadFragment.getInstance(mResources, mTemplate!!.ipad_type, iIpadEditCallback, mTemplate!!.index_color, mTemplate!!.gradient != null, mTemplate!!.isGlass)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
            setupShowFragment(mResources!!.getString(R.string.ipad))
        } catch (unused: Exception) {
        }
    }
    updateHitRatio(mTemplate!!.geTypeResize(), mTemplate!!.imgResize)
}

// =====================================================================
// save() - private method
// =====================================================================

private fun save() {
    if (oneExport) return
    oneExport = true
    trackViewEntity.finishScroll()
    trackViewEntity.setOnProgress(true)
    blurredImageView.setNotDraw(true)
    if (!blurredImageView.isPro) {
        blurredImageView.isRemoveWattermark = false
    }
    stop()
    showProgress()
    executor.execute(Runnable {
        try {
            trackViewEntity.calculMaxTime()
            blurredImageView.invalidate()
            blurredImageView.initCanvasDimension(mTemplate!!.width, mTemplate!!.height, mTemplate!!.geTypeResize())
            val max = Math.max(mTemplate!!.width, mTemplate!!.height)

            if (mTemplate!!.ipad_type != IpadType.HEART.ordinal && mTemplate!!.ipad_type != IpadType.BATTERY.ordinal) {
                if (mTemplate!!.isVideoSquare && (mTemplate!!.ipad_type == IpadType.GRADIENT.ordinal || mTemplate!!.ipad_type == IpadType.BLACK_LAYER.ordinal || mTemplate!!.ipad_type == IpadType.MASK_BRUSH.ordinal || mTemplate!!.ipad_type == IpadType.BLUE_TYPE.ordinal || mTemplate!!.ipad_type == IpadType.CASSET_IMG.ordinal)) {
                    blurredImageView.bitmapOriginal = Bitmap.createBitmap(max, max, Bitmap.Config.ARGB_8888)
                    val cropTo16x92 = when (mTemplate!!.geTypeResize()) {
                        ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(blurredImageView.bitmapOriginal)
                        ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(blurredImageView.bitmapOriginal)
                        else -> BitmapCropper.cropTo16x9(blurredImageView.bitmapOriginal)
                    }
                    blurredImageView.updatePosCanvas(mTemplate!!.width, mTemplate!!.height, cropTo16x92)
                    blurredImageView.updateIpad(cropTo16x92, mTemplate!!.ipad_type, mTemplate!!.geTypeResize())
                    val width = (blurredImageView.ipad_rect!!.width() * 1.0f).toInt()
                    val height = (cropTo16x92!!.height * 0.5355f).toInt()
                    mTemplate!!.setDrawingTranslation(blurredImageView.btmX, blurredImageView.btmY)
                    var round = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                    var round2 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                    var i4 = width + round
                    if (i4 > blurredImageView.bitmapOriginal!!.width) {
                        round -= i4 - blurredImageView.bitmapOriginal!!.width
                        i4 = blurredImageView.bitmapOriginal!!.width
                    }
                    var i5 = height + round2
                    if (i5 > blurredImageView.bitmapOriginal!!.height) {
                        round2 -= i5 - blurredImageView.bitmapOriginal!!.height
                        i5 = blurredImageView.bitmapOriginal!!.height
                    }
                    if (round < 0) round = 0
                    if (round2 < 0) round2 = 0
                    val rect2 = Rect(round, round2, i4, i5)
                    val width2 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                    val height2 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                    val cropToSquare = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal!!, rect2, width2, height2)
                    blurredImageView.bitmapSquare = cropToSquare
                    blurredImageView.radius_square = 0
                    rect2.right = rect2.left + width2
                    rect2.bottom = rect2.top + height2
                    blurredImageView.rectSquare = rect2
                    val tmpl1 = mTemplate!!; tmpl1.uri_bg_ffmpeg = blurredImageView.setupBitmapDraw(cropTo16x92, cropToSquare, tmpl1)
                    mTemplate!!.squareBitmapModel!!.set(blurredImageView.left_square, blurredImageView.top_square, round.toFloat(), round2.toFloat(), rect2.width().toFloat(), rect2.height().toFloat(), cropToSquare.width.toFloat(), cropToSquare.height.toFloat(), 0f)
                } else {
                    blurredImageView.bitmapOriginal = setupOriginalBitmap(
                        Glide.with(this@EngineActivity as FragmentActivity).asBitmap()
                            .load(if (mTemplate!!.isVideoSquare) mTemplate!!.frame_bg else mTemplate!!.uri_bg)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .override(max, max)
                            .submit().get() as Bitmap,
                        max
                    )
                    val cropTo16x9 = when (mTemplate!!.geTypeResize()) {
                        ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(blurredImageView.bitmapOriginal, mTemplate!!.width, mTemplate!!.height)
                        ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(blurredImageView.bitmapOriginal, mTemplate!!.width, mTemplate!!.height)
                        else -> BitmapCropper.cropTo16x9(blurredImageView.bitmapOriginal, mTemplate!!.width, mTemplate!!.height)
                    }
                    val bitmap = cropTo16x9
                    blurredImageView.updatePosCanvas(mTemplate!!.width, mTemplate!!.height, bitmap)
                    blurredImageView.updateIpad(bitmap, mTemplate!!.ipad_type, mTemplate!!.geTypeResize())

                    var rect: Rect
                    var cropToSquareWithRoundCorners: Bitmap? = null
                    var i2: Int
                    var i3: Int

                    if (mTemplate!!.ipad_type == IpadType.IPAD_NEOMORPHIC.ordinal) {
                        val radius = (blurredImageView.ipad_rect!!.width() * 0.6f).toInt()
                        mTemplate!!.setDrawingTranslation(blurredImageView.btmX, blurredImageView.btmY)
                        i2 = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                        i3 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                        var i6 = radius + i2
                        if (i6 > blurredImageView.bitmapOriginal!!.width) {
                            i2 -= i6 - blurredImageView.bitmapOriginal!!.width
                            i6 = blurredImageView.bitmapOriginal!!.width
                        }
                        var i7 = radius + i3
                        if (i7 > blurredImageView.bitmapOriginal!!.height) {
                            i3 -= i7 - blurredImageView.bitmapOriginal!!.height
                            i7 = blurredImageView.bitmapOriginal!!.height
                        }
                        if (i2 < 0) i2 = 0
                        if (i3 < 0) i3 = 0
                        rect = Rect(i2, i3, i6, i7)
                        val width3 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                        val height3 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                        cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.bitmapOriginal!!, rect, radius, width3, height3)
                        rect.right = rect.left + width3
                        rect.bottom = rect.top + height3
                        blurredImageView.rectSquare = rect
                    } else {
                        if (mTemplate!!.ipad_type != IpadType.IPAD.ordinal && mTemplate!!.ipad_type != IpadType.IPAD_UNBLUR.ordinal && mTemplate!!.ipad_type != IpadType.IPAD_CLASSIC.ordinal) {
                            val width4 = (blurredImageView.ipad_rect!!.width() * 1.0f).toInt()
                            val height4 = (bitmap!!.height * 0.5355f).toInt()
                            mTemplate!!.setDrawingTranslation(blurredImageView.btmX, blurredImageView.btmY)
                            var round3 = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                            var round4 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                            var i8 = width4 + round3
                            if (i8 > blurredImageView.bitmapOriginal!!.width) {
                                round3 -= i8 - blurredImageView.bitmapOriginal!!.width
                                i8 = blurredImageView.bitmapOriginal!!.width
                            }
                            var i9 = height4 + round4
                            if (i9 > blurredImageView.bitmapOriginal!!.height) {
                                round4 -= i9 - blurredImageView.bitmapOriginal!!.height
                                i9 = blurredImageView.bitmapOriginal!!.height
                            }
                            if (round3 < 0) round3 = 0
                            if (round4 < 0) round4 = 0
                            rect = Rect(round3, round4, i8, i9)
                            val width5 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                            val height5 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                            cropToSquareWithRoundCorners = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal!!, rect, width5, height5)
                            blurredImageView.bitmapSquare = cropToSquareWithRoundCorners
                            blurredImageView.radius_square = 0
                            rect.right = rect.left + width5
                            rect.bottom = rect.top + height5
                            blurredImageView.rectSquare = rect
                            i2 = round3
                            i3 = round4
                        } else {
                            i2 = 0
                            i3 = 0
                            rect = Rect() // placeholder, will be reassigned
                        }

                        if (mTemplate!!.ipad_type == IpadType.IPAD.ordinal || mTemplate!!.ipad_type == IpadType.IPAD_UNBLUR.ordinal || mTemplate!!.ipad_type == IpadType.IPAD_CLASSIC.ordinal) {
                            val width6 = (blurredImageView.ipad_rect!!.width() * 0.87530595f).toInt()
                            val i10 = (width6 * 1.13f).toInt()
                            val min = Math.min(width6, i10)
                            mTemplate!!.setDrawingTranslation(blurredImageView.btmX, blurredImageView.btmY)
                            var round5 = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                            var round6 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                            var i11 = width6 + round5
                            if (i11 > blurredImageView.bitmapOriginal!!.width) {
                                round5 -= i11 - blurredImageView.bitmapOriginal!!.width
                                i11 = blurredImageView.bitmapOriginal!!.width
                            }
                            var i12 = i10 + round6
                            if (i12 > blurredImageView.bitmapOriginal!!.height) {
                                round6 -= i12 - blurredImageView.bitmapOriginal!!.height
                                i12 = blurredImageView.bitmapOriginal!!.height
                            }
                            if (round5 < 0) round5 = 0
                            if (round6 < 0) round6 = 0
                            rect = Rect(round5, round6, i11, i12)
                            var radiusVal = 0
                            if (mTemplate!!.ipad_type == IpadType.IPAD_CLASSIC.ordinal) {
                                val width7 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                                val height6 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                                val cropToSquare2 = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal!!, rect, width7, height6)
                                blurredImageView.bitmapSquare = cropToSquare2
                                blurredImageView.radius_square = 0
                                rect.right = rect.left + width7
                                rect.bottom = rect.top + height6
                                blurredImageView.rectSquare = rect
                                cropToSquareWithRoundCorners = cropToSquare2
                                radiusVal = 0
                            } else {
                                radiusVal = (min * 0.10800001f).toInt()
                                val width8 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                                val height7 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                                cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.bitmapOriginal!!, rect, radiusVal, width8, height7)
                                rect.right = rect.left + width8
                                rect.bottom = rect.top + height7
                                blurredImageView.rectSquare = rect
                            }
                            i2 = round5
                            i3 = round6
                        }

                        val rect3 = rect
                        val bitmap2 = cropToSquareWithRoundCorners
                        val engineActivity = this@EngineActivity
                        val tmpl2 = mTemplate!!; tmpl2.uri_bg_ffmpeg = blurredImageView.setupBitmapDraw(UtilsBitmap.blurInSave(engineActivity, bitmap!!, 20, 1, engineActivity.mTemplate!!.width, tmpl2.height)!!, bitmap2!!, tmpl2)
                        mTemplate!!.squareBitmapModel!!.set(blurredImageView.left_square, blurredImageView.top_square, i2.toFloat(), i3.toFloat(), rect3.width().toFloat(), rect3.height().toFloat(), bitmap2.width.toFloat(), bitmap2.height.toFloat(), 0f)
                    }

                    // Heart/Battery fallback
                    mTemplate!!.uri_bg_ffmpeg = blurredImageView.setupBitmapDraw(cropTo16x9!!, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), mTemplate!!)
                }
                saveTemplate()
                val intent = Intent(this@EngineActivity, ProgressViewActivity::class.java)
                intent.putExtra(Constants.TEMPLATE, mTemplate!!.idTemplate)
                intent.addFlags(65536)
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
            // HEART/BATTERY type handling
            val createBitmap = Bitmap.createBitmap(mTemplate!!.width, mTemplate!!.height, Bitmap.Config.RGB_565)
            createBitmap.eraseColor(ViewCompat.MEASURED_STATE_MASK)
            blurredImageView.updatePosCanvas(mTemplate!!.width, mTemplate!!.height, createBitmap)
            blurredImageView.updateIpad(createBitmap, mTemplate!!.ipad_type, mTemplate!!.geTypeResize())
            mTemplate!!.uri_bg_ffmpeg = blurredImageView.setupBitmapDraw(createBitmap, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), mTemplate!!)
            saveTemplate()
            val intent2 = Intent(this@EngineActivity, ProgressViewActivity::class.java)
            intent2.putExtra(Constants.TEMPLATE, mTemplate!!.idTemplate)
            intent2.addFlags(65536)
            startActivity(intent2)
            overridePendingTransition(0, 0)
            finish()
        } catch (e: Exception) {
            Log.e("Tag : ", "init ${e.message}")
        }
    })
}

// =====================================================================
// setupShowFragment() / setupHideFragment()
// =====================================================================

private fun setupShowFragment(str: String?) {
    findViewById<View>(R.id.layout_time).visibility = View.INVISIBLE
    findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
    if (str != null) {
        tv_tittle_fragment?.text = str
        tv_tittle_fragment?.visibility = View.VISIBLE
        btnChangeResize?.let { it.visibility = View.INVISIBLE }
    }
    btn_cancel?.visibility = View.INVISIBLE
    btn_export?.visibility = View.INVISIBLE
    btn_setup_fps?.visibility = View.INVISIBLE
}

private fun setupHideFragment() {
    findViewById<View>(R.id.layout_time).visibility = View.VISIBLE
    findViewById<View>(R.id.layout_menu).visibility = View.VISIBLE
    tv_tittle_fragment?.visibility = View.GONE
    btnChangeResize?.let { it.visibility = View.VISIBLE }
    btn_cancel?.visibility = View.VISIBLE
    btn_export?.visibility = View.VISIBLE
    btn_setup_fps?.visibility = View.VISIBLE
}

// =====================================================================
// showEdit*Entity() methods
// =====================================================================

private fun showEditAudioEntity(entityAudio: EntityAudio) {
    findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
    val beginTransaction = supportFragmentManager.beginTransaction()
    mCurrentFragment = EditMediaFragment.getInstance(iEditMediaCallback, mResources, entityAudio, -trackViewEntity.currentPosition)
    beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
    beginTransaction.commit()
}

private fun showEditMultipleEntity(i: Int) {
    if (EditMultipleEntityFragment.instance != null) {
        EditMultipleEntityFragment.instance!!.setCount_select(i)
        return
    }
    findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
    val beginTransaction = supportFragmentManager.beginTransaction()
    mCurrentFragment = EditMultipleEntityFragment.getInstance(iEditMultipleCallback, mResources, i)
    beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
    beginTransaction.commit()
}

private fun showEditEntity(entity: Entity) {
    val beginTransaction = supportFragmentManager.beginTransaction()
    mCurrentFragment = EditEntityFragment.getInstance(iEditEntityCallback, mResources, entity, -trackViewEntity.currentPosition)
    beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
    beginTransaction.commit()
}

private fun showEditTrslEntity(entity: Entity) {
    val beginTransaction = supportFragmentManager.beginTransaction()
    mCurrentFragment = EditTrslEntityFragment.getInstance(iEditTrstEntityCallback, mResources, entity, -trackViewEntity.currentPosition)
    beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
    beginTransaction.commit()
}

private fun showEditBismilahEntity(entity: Entity) {
    val beginTransaction = supportFragmentManager.beginTransaction()
    mCurrentFragment = EditBismilahEntityFragment.getInstance(iBismilahEntityCallback, mResources, entity, -trackViewEntity.currentPosition)
    beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
    beginTransaction.commit()
}

// =====================================================================
// saveTemplateTmp() - private method
// =====================================================================

private fun saveTemplateTmp() {
    var str: String
    try {
        if (mTemplate == null) {
            mTemplate = Template()
        }
        // mTemplate!!.setNewCode() // TODO: fix
        mTemplate!!.isGlass = blurredImageView.isGlass
        mTemplate!!.currentCursur = trackViewEntity.current_cursur_position
        mTemplate!!.scale_timeline = trackViewEntity.scaleFactor
        mTemplate!!.gradient = blurredImageView.color_gradient
        mTemplate!!.duration = trackViewEntity.maxTime
        mTemplate!!.color_ipad = blurredImageView.colorIpad()
        mTemplate!!.quranEntityList.clear()
        mTemplate!!.translationTemplateList.clear()
        mTemplate!!.uri_bg = uri_bg

        try {
            for (entityQuranTimeline in trackViewEntity.entityListQuran) {
                if (entityQuranTimeline!!.visible()) {
                    val f2 = Utils.f2(Math.abs(entityQuranTimeline!!.rect.left / trackViewEntity.second_in_screen))
                    val f22 = Utils.f2(Math.abs(entityQuranTimeline!!.rect.right / trackViewEntity.second_in_screen))
                    if (entityQuranTimeline!!.quranEntity.copyRect == null) {
                        entityQuranTimeline!!.quranEntity.setCopyRect()
                        if (entityQuranTimeline!!.quranEntity.copyRect == null) {
                            // skip
                        }
                    }
                    val entityQuranTemplate = EntityQuranTemplate(
                        transition = entityQuranTimeline!!.transition,
                        start = f2,
                        end = f22,
                        btm_x = entityQuranTimeline!!.quranEntity.copyRect!!.left * mTemplate!!.width,
                        btm_y = mTemplate!!.height * entityQuranTimeline!!.quranEntity.copyRect!!.top,
                        left = entityQuranTimeline!!.rect.left / entityQuranTimeline!!.scaleFactor,
                        right = entityQuranTimeline!!.rect.right / entityQuranTimeline!!.scaleFactor,
                        aya = entityQuranTimeline!!.quranEntity.txt!!,
                        complete_aya = entityQuranTimeline!!.quranEntity.complete_aya!!,
                        indexNumber = entityQuranTimeline!!.quranEntity.number,
                        number = entityQuranTimeline!!.quranEntity.number,
                        color = entityQuranTimeline!!.quranEntity.clrAya,
                        name_font = entityQuranTimeline!!.quranEntity.nameFont,
                        colorTrsl = if (entityQuranTimeline!!.quranEntity.paintTranslationAya != null) entityQuranTimeline!!.quranEntity.clrTrsl else InputDeviceCompat.SOURCE_ANY,
                        preset = entityQuranTimeline!!.quranEntity.mPreset
                    )
                    entityQuranTemplate.height = (entityQuranTimeline!!.quranEntity.copyRect!!.bottom * mTemplate!!.height) - (entityQuranTimeline!!.quranEntity.copyRect!!.top * mTemplate!!.height)
                    entityQuranTemplate.factor_size = entityQuranTimeline!!.quranEntity.factorSize
                    entityQuranTemplate.factor_sizeTrl = entityQuranTimeline!!.quranEntity.factorSizeTrl
                    entityQuranTemplate.scale = entityQuranTimeline!!.quranEntity.scaleFactor
                    entityQuranTemplate.translation = entityQuranTimeline!!.quranEntity.translation!!
                    entityQuranTemplate.translation_complete = entityQuranTimeline!!.quranEntity.translation_complete!!
                    entityQuranTemplate.startWord_index = entityQuranTimeline!!.quranEntity.startWord_index
                    entityQuranTemplate.endWord_index = entityQuranTimeline!!.quranEntity.endWord_index
                    entityQuranTemplate.icon = entityQuranTimeline!!.quranEntity.icon!!
                    entityQuranTemplate.file = entityQuranTimeline!!.file
                    entityQuranTemplate.file_in = entityQuranTimeline!!.file_in
                    entityQuranTemplate.file_out = entityQuranTimeline!!.file_out
                    entityQuranTemplate.rectF = MRectF(entityQuranTimeline!!.quranEntity.copyRect!!.left, entityQuranTimeline!!.quranEntity.copyRect!!.top, entityQuranTimeline!!.quranEntity.copyRect!!.right, entityQuranTimeline!!.quranEntity.copyRect!!.bottom)
                    mTemplate!!.addQuranEntityList(entityQuranTemplate)
                }
            }
        } catch (e: Exception) {
            Log.e("save templete quran", "" + e.message)
        }

        try {
            for (entityTrslTimeline in trackViewEntity.entityListTrslQuran) {
                if (entityTrslTimeline!!.visible()) {
                    val f23 = Utils.f2(Math.abs(entityTrslTimeline!!.rect.left / trackViewEntity.second_in_screen))
                    val f24 = Utils.f2(Math.abs(entityTrslTimeline!!.rect.right / trackViewEntity.second_in_screen))
                    if (entityTrslTimeline!!.quranEntity.copyRect == null) {
                        entityTrslTimeline!!.quranEntity.setCopyRect()
                        if (entityTrslTimeline!!.quranEntity.copyRect == null) {
                            // skip
                        }
                    }
                    val entityTranslationTemplate = EntityTranslationTemplate(
                        entityTrslTimeline!!.transition, f23, f24,
                        entityTrslTimeline!!.quranEntity.copyRect!!.left * mTemplate!!.width,
                        mTemplate!!.height * entityTrslTimeline!!.quranEntity.copyRect!!.top,
                        entityTrslTimeline!!.rect.left / entityTrslTimeline!!.scaleFactor,
                        entityTrslTimeline!!.rect.right / entityTrslTimeline!!.scaleFactor,
                        entityTrslTimeline!!.quranEntity.txt!!,
                        entityTrslTimeline!!.quranEntity.nameFont,
                        entityTrslTimeline!!.quranEntity.number,
                        entityTrslTimeline!!.quranEntity.clrAya,
                        entityTrslTimeline!!.quranEntity.mPreset
                    )
                    entityTranslationTemplate.height = (entityTrslTimeline!!.quranEntity.copyRect!!.bottom * mTemplate!!.height) - (entityTrslTimeline!!.quranEntity.copyRect!!.top * mTemplate!!.height)
                    entityTranslationTemplate.factor_size = entityTrslTimeline!!.quranEntity.factorSize
                    entityTranslationTemplate.factor_sizeTrl = entityTrslTimeline!!.quranEntity.factorSizeTrl
                    entityTranslationTemplate.scale = entityTrslTimeline!!.quranEntity.scaleFactor
                    entityTranslationTemplate.file = entityTrslTimeline!!.file
                    entityTranslationTemplate.file_in = entityTrslTimeline!!.file_in
                    entityTranslationTemplate.file_out = entityTrslTimeline!!.file_out
                    entityTranslationTemplate.clr_bg = entityTrslTimeline!!.quranEntity.clrBg
                    entityTranslationTemplate.isHaveBg = entityTrslTimeline!!.quranEntity.isHaveBg
                    entityTranslationTemplate.rectF = MRectF(entityTrslTimeline!!.quranEntity.copyRect!!.left, entityTrslTimeline!!.quranEntity.copyRect!!.top, entityTrslTimeline!!.quranEntity.copyRect!!.right, entityTrslTimeline!!.quranEntity.copyRect!!.bottom)
                    mTemplate!!.addTrslEntityList(entityTranslationTemplate)
                }
            }
        } catch (e2: Exception) {
            Log.e("save templete trsl quran", "" + e2.message)
        }

        mTemplate!!.entityIsti3adaTemplate = null
        if (blurredImageView.mIsti3adhaEntity != null && blurredImageView.mIsti3adhaEntity!!.bismilahTimeline!!.visible()) {
            val bismilahTimeline = blurredImageView.mIsti3adhaEntity!!.bismilahTimeline
            val f25 = Utils.f2(Math.abs(bismilahTimeline!!.rect.left / trackViewEntity.second_in_screen))
            val f26 = Utils.f2(Math.abs(bismilahTimeline!!.rect.right / trackViewEntity.second_in_screen))
            if (bismilahTimeline!!.quranEntity.copyRect == null) {
                bismilahTimeline!!.quranEntity.setCopyRect()
            }
            val entityBismilahTemplate = EntityBismilahTemplate(
                bismilahTimeline!!.transition, f25, f26,
                bismilahTimeline!!.quranEntity.copyRect!!.left * mTemplate!!.width,
                mTemplate!!.height * bismilahTimeline!!.quranEntity.copyRect!!.top,
                bismilahTimeline!!.rect.left / bismilahTimeline!!.scaleFactor,
                bismilahTimeline!!.rect.right / bismilahTimeline!!.scaleFactor,
                bismilahTimeline!!.quranEntity.txt!!,
                bismilahTimeline!!.quranEntity.clrAya,
                bismilahTimeline!!.quranEntity.mPreset
            )
            entityBismilahTemplate.height = (bismilahTimeline!!.quranEntity.copyRect!!.bottom * mTemplate!!.height) - (bismilahTimeline!!.quranEntity.copyRect!!.top * mTemplate!!.height)
            entityBismilahTemplate.factor_size = bismilahTimeline!!.quranEntity.factorSize
            entityBismilahTemplate.scale = bismilahTimeline!!.quranEntity.scaleFactor
            entityBismilahTemplate.file = bismilahTimeline!!.file
            entityBismilahTemplate.file_in = bismilahTimeline!!.file_in
            entityBismilahTemplate.file_out = bismilahTimeline!!.file_out
            entityBismilahTemplate.rectF = MRectF(bismilahTimeline!!.quranEntity.copyRect!!.left, bismilahTimeline!!.quranEntity.copyRect!!.top, bismilahTimeline!!.quranEntity.copyRect!!.right, bismilahTimeline!!.quranEntity.copyRect!!.bottom)
            mTemplate!!.entityIsti3adaTemplate = entityBismilahTemplate
        }

        mTemplate!!.entityBismilahTemplate = null
        if (blurredImageView.bismilahEntity != null && blurredImageView.bismilahEntity!!.bismilahTimeline!!.visible()) {
            val bismilahTimeline2 = blurredImageView.bismilahEntity!!.bismilahTimeline
            val f27 = Utils.f2(Math.abs(bismilahTimeline2!!.rect.left / trackViewEntity.second_in_screen))
            val f28 = Utils.f2(Math.abs(bismilahTimeline2!!.rect.right / trackViewEntity.second_in_screen))
            if (bismilahTimeline2!!.quranEntity.copyRect == null) {
                bismilahTimeline2!!.quranEntity.setCopyRect()
            }
            val entityBismilahTemplate2 = EntityBismilahTemplate(
                bismilahTimeline2!!.transition, f27, f28,
                bismilahTimeline2!!.quranEntity.copyRect!!.left * mTemplate!!.width,
                mTemplate!!.height * bismilahTimeline2!!.quranEntity.copyRect!!.top,
                bismilahTimeline2!!.rect.left / bismilahTimeline2!!.scaleFactor,
                bismilahTimeline2!!.rect.right / bismilahTimeline2!!.scaleFactor,
                bismilahTimeline2!!.quranEntity.txt!!,
                bismilahTimeline2!!.quranEntity.clrAya,
                bismilahTimeline2!!.quranEntity.mPreset
            )
            entityBismilahTemplate2.height = (bismilahTimeline2!!.quranEntity.copyRect!!.bottom * mTemplate!!.height) - (bismilahTimeline2!!.quranEntity.copyRect!!.top * mTemplate!!.height)
            entityBismilahTemplate2.factor_size = bismilahTimeline2!!.quranEntity.factorSize
            entityBismilahTemplate2.scale = bismilahTimeline2!!.quranEntity.scaleFactor
            entityBismilahTemplate2.file = bismilahTimeline2!!.file
            entityBismilahTemplate2.file_in = bismilahTimeline2!!.file_in
            entityBismilahTemplate2.file_out = bismilahTimeline2!!.file_out
            entityBismilahTemplate2.rectF = MRectF(bismilahTimeline2!!.quranEntity.copyRect!!.left, bismilahTimeline2!!.quranEntity.copyRect!!.top, bismilahTimeline2!!.quranEntity.copyRect!!.right, bismilahTimeline2!!.quranEntity.copyRect!!.bottom)
            mTemplate!!.entityBismilahTemplate = entityBismilahTemplate2
        }

        str = if (blurredImageView.surahNameEntity == null) {
            ""
        } else if (mTemplate!!.entitySurahTemplate == null) {
            ""
        } else {
            ""
        }

        if (blurredImageView.surahNameEntity != null) {
            if (mTemplate!!.entitySurahTemplate == null) {
                mTemplate!!.entitySurahTemplate = EntitySurahTemplate(
                    blurredImageView.surahNameEntity!!.name,
                    blurredImageView.surahNameEntity!!.reader,
                    mTemplate!!.mDrawingTranslationX + blurredImageView.rectFSurahName!!.left,
                    mTemplate!!.mDrawingTranslationY + blurredImageView.rectFSurahName!!.top,
                    MRectF(blurredImageView.surahNameEntity!!.copyRect!!.left, blurredImageView.surahNameEntity!!.copyRect!!.top, blurredImageView.surahNameEntity!!.copyRect!!.right, blurredImageView.surahNameEntity!!.copyRect!!.bottom),
                    blurredImageView.surahNameEntity!!.scaleFactor,
                    blurredImageView.surahNameEntity!!.nameFont,
                    blurredImageView.surahNameEntity!!.clrS_name,
                    blurredImageView.surahNameEntity!!.mPreset,
                    blurredImageView.surahNameEntity!!.style,
                    blurredImageView.surahNameEntity!!.index_surah,
                    blurredImageView.surahNameEntity!!.isHaveBg,
                    blurredImageView.surahNameEntity!!.clrBg
                )
            } else {
                mTemplate!!.entitySurahTemplate!!.clrBg = blurredImageView.surahNameEntity!!.clrBg
                mTemplate!!.entitySurahTemplate!!.isHaveBg = blurredImageView.surahNameEntity!!.isHaveBg
                mTemplate!!.entitySurahTemplate!!.index_surah = blurredImageView.surahNameEntity!!.index_surah
                mTemplate!!.entitySurahTemplate!!.style = blurredImageView.surahNameEntity!!.style
                mTemplate!!.entitySurahTemplate!!.clr = blurredImageView.surahNameEntity!!.clrS_name
                mTemplate!!.entitySurahTemplate!!.preset = blurredImageView.surahNameEntity!!.mPreset
                mTemplate!!.entitySurahTemplate!!.name_font = blurredImageView.surahNameEntity!!.nameFont
                mTemplate!!.entitySurahTemplate!!.factor_scale = blurredImageView.surahNameEntity!!.scaleFactor
                mTemplate!!.entitySurahTemplate!!.rectF = MRectF(blurredImageView.surahNameEntity!!.copyRect!!.left, blurredImageView.surahNameEntity!!.copyRect!!.top, blurredImageView.surahNameEntity!!.copyRect!!.right, blurredImageView.surahNameEntity!!.copyRect!!.bottom)
                mTemplate!!.entitySurahTemplate!!.name = blurredImageView.surahNameEntity!!.name
                mTemplate!!.entitySurahTemplate!!.reader = blurredImageView.surahNameEntity!!.reader
                mTemplate!!.entitySurahTemplate!!.setPos(blurredImageView.rectFSurahName!!.left + mTemplate!!.mDrawingTranslationX, blurredImageView.rectFSurahName!!.top + mTemplate!!.mDrawingTranslationY)
            }
        }

        if (mTemplate!!.entityProgressTemplate == null) {
            mTemplate!!.entityProgressTemplate = EntityProgressTemplate(Utils.f2(blurredImageView.rectFProgress!!.left + mTemplate!!.mDrawingTranslationX), Utils.f2(blurredImageView.rectFProgress!!.top + mTemplate!!.mDrawingTranslationY))
        } else {
            mTemplate!!.entityProgressTemplate!!.left = Utils.f2(blurredImageView.rectFProgress!!.left + mTemplate!!.mDrawingTranslationX)
            mTemplate!!.entityProgressTemplate!!.top = Utils.f2(blurredImageView.rectFProgress!!.top + mTemplate!!.mDrawingTranslationY)
        }

        mTemplate!!.entityMediaList.clear()
        for (entityAudio in trackViewEntity.entityListAudio) {
            if (entityAudio.visible() && entityAudio.end > entityAudio.start) {
                val entityMedia = EntityMedia(
                    entityAudio.uri.toString(), entityAudio.minDuration, entityAudio.start, entityAudio.end,
                    (entityAudio.rect.left / trackViewEntity.scaleFactor).toInt(), (entityAudio.rect.right / trackViewEntity.scaleFactor).toFloat(),
                    Math.round(entityAudio.end - entityAudio.start).toFloat(), entityAudio.offset, entityAudio.getOffsetRight(), entityAudio.getOffsetLeft(),
                    entityAudio.max, entityAudio.effectAudio.fade_in.toFloat(), entityAudio.effectAudio.fade_out.toFloat(),
                    (entityAudio.rect.left / trackViewEntity.second_in_screen) * 1000.0f
                )
                entityMedia.paths_https = entityAudio.pathsHttp
                entityMedia.effectAudio = entityAudio.effectAudio
                entityMedia.path_ffmpeg = entityAudio.getPathFfmpeg()
                entityMedia.path_ffmpeg_effect = entityAudio.getPathFfmpegEffect()
                entityMedia.video_path = entityAudio.videoPath
                entityMedia.isApplyEffectInPreview = entityAudio.isApplyEffectInPreview
                mTemplate!!.addMedia(entityMedia)
            }
        }
        mTemplate!!.uri_video = FileHelper(this).createPublicVideoFolder(mResources!!.getString(R.string.app_name))!!.absolutePath!! + "/" + System.currentTimeMillis() + "_NurMontage.mp4"
        LocalPersistence.writeTemplate(this, mTemplate, str, Constants.TEMPLATE_TMP)
    } catch (unused: Exception) {
    }
}

// =====================================================================
// saveTemplate() - private method
// =====================================================================

private fun saveTemplate() {
    var engineActivity = this
    try {
        if (engineActivity.mTemplate == null) {
            engineActivity.mTemplate = Template()
        }
        // engineActivity.mTemplate!!.setNewCode() // TODO: fix
        // // TODO: mTemplate?.isGlass = blurredImageView.isGlass // TODO: fix isGlass assignment
        engineActivity.mTemplate!!.currentCursur = engineActivity.trackViewEntity.current_cursur_position
        engineActivity.mTemplate!!.scale_timeline = engineActivity.trackViewEntity.scaleFactor
        engineActivity.mTemplate!!.duration = engineActivity.trackViewEntity.maxTime
        engineActivity.mTemplate!!.gradient = engineActivity.blurredImageView.color_gradient
        engineActivity.mTemplate!!.color_ipad = engineActivity.blurredImageView.colorIpad()
        engineActivity.mTemplate!!.quranEntityList.clear()
        engineActivity.mTemplate!!.translationTemplateList.clear()
        engineActivity.mTemplate!!.uri_bg = engineActivity.uri_bg

        try {
            for (entityQuranTimeline in engineActivity.trackViewEntity.entityListQuran) {
                if (entityQuranTimeline!!.visible()) {
                    val f2 = Utils.f2(Math.abs(entityQuranTimeline!!.rect.left / engineActivity.trackViewEntity.second_in_screen))
                    val f22 = Utils.f2(Math.abs(entityQuranTimeline!!.rect.right / engineActivity.trackViewEntity.second_in_screen))
                    if (entityQuranTimeline!!.quranEntity.copyRect == null) {
                        entityQuranTimeline!!.quranEntity.setCopyRect()
                    }
                    val entityQuranTemplate = EntityQuranTemplate(
                        transition = entityQuranTimeline!!.transition,
                        start = f2,
                        end = f22,
                        btm_x = entityQuranTimeline!!.quranEntity.copyRect!!.left * engineActivity.mTemplate!!.width,
                        btm_y = engineActivity.mTemplate!!.height * entityQuranTimeline!!.quranEntity.copyRect!!.top,
                        left = entityQuranTimeline!!.rect.left / entityQuranTimeline!!.scaleFactor,
                        right = entityQuranTimeline!!.rect.right / entityQuranTimeline!!.scaleFactor,
                        aya = entityQuranTimeline!!.quranEntity.txt!!,
                        complete_aya = entityQuranTimeline!!.quranEntity.complete_aya!!,
                        indexNumber = entityQuranTimeline!!.quranEntity.number,
                        number = entityQuranTimeline!!.quranEntity.number,
                        color = entityQuranTimeline!!.quranEntity.clrAya,
                        name_font = entityQuranTimeline!!.quranEntity.nameFont,
                        colorTrsl = if (entityQuranTimeline!!.quranEntity.paintTranslationAya != null) entityQuranTimeline!!.quranEntity.clrTrsl else InputDeviceCompat.SOURCE_ANY,
                        preset = entityQuranTimeline!!.quranEntity.mPreset
                    )
                    entityQuranTemplate.height = (entityQuranTimeline!!.quranEntity.copyRect!!.bottom * engineActivity.mTemplate!!.height) - (entityQuranTimeline!!.quranEntity.copyRect!!.top * engineActivity.mTemplate!!.height)
                    entityQuranTemplate.factor_size = entityQuranTimeline!!.quranEntity.factorSize
                    entityQuranTemplate.factor_sizeTrl = entityQuranTimeline!!.quranEntity.factorSizeTrl
                    entityQuranTemplate.scale = entityQuranTimeline!!.quranEntity.scaleFactor
                    entityQuranTemplate.translation = entityQuranTimeline!!.quranEntity.translation!!
                    entityQuranTemplate.translation_complete = entityQuranTimeline!!.quranEntity.translation_complete!!
                    entityQuranTemplate.startWord_index = entityQuranTimeline!!.quranEntity.startWord_index
                    entityQuranTemplate.endWord_index = entityQuranTimeline!!.quranEntity.endWord_index
                    entityQuranTemplate.icon = entityQuranTimeline!!.quranEntity.icon!!
                    entityQuranTemplate.file = entityQuranTimeline!!.file
                    entityQuranTemplate.file_in = entityQuranTimeline!!.file_in
                    entityQuranTemplate.file_out = entityQuranTimeline!!.file_out
                    entityQuranTemplate.rectF = MRectF(entityQuranTimeline!!.quranEntity.copyRect!!.left, entityQuranTimeline!!.quranEntity.copyRect!!.top, entityQuranTimeline!!.quranEntity.copyRect!!.right, entityQuranTimeline!!.quranEntity.copyRect!!.bottom)
                    engineActivity.mTemplate!!.addQuranEntityList(entityQuranTemplate)
                }
            }
        } catch (e: Exception) {
            Log.e("save templete quran", "" + e.message)
        }

        try {
            for (entityTrslTimeline in engineActivity.trackViewEntity.entityListTrslQuran) {
                if (entityTrslTimeline!!.visible()) {
                    val f23 = Utils.f2(Math.abs(entityTrslTimeline!!.rect.left / engineActivity.trackViewEntity.second_in_screen))
                    val f24 = Utils.f2(Math.abs(entityTrslTimeline!!.rect.right / engineActivity.trackViewEntity.second_in_screen))
                    if (entityTrslTimeline!!.quranEntity.copyRect == null) {
                        entityTrslTimeline!!.quranEntity.setCopyRect()
                    }
                    val entityTranslationTemplate = EntityTranslationTemplate(
                        entityTrslTimeline!!.transition, f23, f24,
                        entityTrslTimeline!!.quranEntity.copyRect!!.left * engineActivity.mTemplate!!.width,
                        engineActivity.mTemplate!!.height * entityTrslTimeline!!.quranEntity.copyRect!!.top,
                        entityTrslTimeline!!.rect.left / entityTrslTimeline!!.scaleFactor,
                        entityTrslTimeline!!.rect.right / entityTrslTimeline!!.scaleFactor,
                        entityTrslTimeline!!.quranEntity.txt!!, entityTrslTimeline!!.quranEntity.nameFont,
                        entityTrslTimeline!!.quranEntity.number, entityTrslTimeline!!.quranEntity.clrAya,
                        entityTrslTimeline!!.quranEntity.mPreset
                    )
                    entityTranslationTemplate.height = (entityTrslTimeline!!.quranEntity.copyRect!!.bottom * engineActivity.mTemplate!!.height) - (entityTrslTimeline!!.quranEntity.copyRect!!.top * engineActivity.mTemplate!!.height)
                    entityTranslationTemplate.factor_size = entityTrslTimeline!!.quranEntity.factorSize
                    entityTranslationTemplate.factor_sizeTrl = entityTrslTimeline!!.quranEntity.factorSizeTrl
                    entityTranslationTemplate.scale = entityTrslTimeline!!.quranEntity.scaleFactor
                    entityTranslationTemplate.file = entityTrslTimeline!!.file
                    entityTranslationTemplate.file_in = entityTrslTimeline!!.file_in
                    entityTranslationTemplate.file_out = entityTrslTimeline!!.file_out
                    entityTranslationTemplate.clr_bg = entityTrslTimeline!!.quranEntity.clrBg
                    entityTranslationTemplate.isHaveBg = entityTrslTimeline!!.quranEntity.isHaveBg
                    entityTranslationTemplate.rectF = MRectF(entityTrslTimeline!!.quranEntity.copyRect!!.left, entityTrslTimeline!!.quranEntity.copyRect!!.top, entityTrslTimeline!!.quranEntity.copyRect!!.right, entityTrslTimeline!!.quranEntity.copyRect!!.bottom)
                    engineActivity.mTemplate!!.addTrslEntityList(entityTranslationTemplate)
                }
            }
        } catch (e2: Exception) {
            Log.e("save templete trsl quran", "" + e2.message)
        }

        engineActivity.mTemplate!!.entityIsti3adaTemplate = null
        if (engineActivity.blurredImageView.mIsti3adhaEntity != null && engineActivity.blurredImageView.mIsti3adhaEntity!!.bismilahTimeline!!.visible()) {
            val bismilahTimeline = engineActivity.blurredImageView.mIsti3adhaEntity!!.bismilahTimeline
            val f25 = Utils.f2(Math.abs(bismilahTimeline!!.rect.left / engineActivity.trackViewEntity.second_in_screen))
            val f26 = Utils.f2(Math.abs(bismilahTimeline!!.rect.right / engineActivity.trackViewEntity.second_in_screen))
            if (bismilahTimeline!!.quranEntity.copyRect == null) {
                bismilahTimeline!!.quranEntity.setCopyRect()
            }
            val entityBismilahTemplate = EntityBismilahTemplate(
                bismilahTimeline!!.transition, f25, f26,
                bismilahTimeline!!.quranEntity.copyRect!!.left * engineActivity.mTemplate!!.width,
                engineActivity.mTemplate!!.height * bismilahTimeline!!.quranEntity.copyRect!!.top,
                bismilahTimeline!!.rect.left / bismilahTimeline!!.scaleFactor,
                bismilahTimeline!!.rect.right / bismilahTimeline!!.scaleFactor,
                bismilahTimeline!!.quranEntity.txt!!, bismilahTimeline!!.quranEntity.clrAya,
                bismilahTimeline!!.quranEntity.mPreset
            )
            entityBismilahTemplate.height = (bismilahTimeline!!.quranEntity.copyRect!!.bottom * engineActivity.mTemplate!!.height) - (bismilahTimeline!!.quranEntity.copyRect!!.top * engineActivity.mTemplate!!.height)
            entityBismilahTemplate.factor_size = bismilahTimeline!!.quranEntity.factorSize
            entityBismilahTemplate.scale = bismilahTimeline!!.quranEntity.scaleFactor
            entityBismilahTemplate.file = bismilahTimeline!!.file
            entityBismilahTemplate.file_in = bismilahTimeline!!.file_in
            entityBismilahTemplate.file_out = bismilahTimeline!!.file_out
            entityBismilahTemplate.rectF = MRectF(bismilahTimeline!!.quranEntity.copyRect!!.left, bismilahTimeline!!.quranEntity.copyRect!!.top, bismilahTimeline!!.quranEntity.copyRect!!.right, bismilahTimeline!!.quranEntity.copyRect!!.bottom)
            engineActivity.mTemplate!!.entityIsti3adaTemplate = entityBismilahTemplate
        }

        engineActivity.mTemplate!!.entityBismilahTemplate = null
        if (engineActivity.blurredImageView.bismilahEntity != null && engineActivity.blurredImageView.bismilahEntity!!.bismilahTimeline!!.visible()) {
            val bismilahTimeline2 = engineActivity.blurredImageView.bismilahEntity!!.bismilahTimeline
            val f27 = Utils.f2(Math.abs(bismilahTimeline2!!.rect.left / engineActivity.trackViewEntity.second_in_screen))
            val f28 = Utils.f2(Math.abs(bismilahTimeline2!!.rect.right / engineActivity.trackViewEntity.second_in_screen))
            if (bismilahTimeline2!!.quranEntity.copyRect == null) {
                bismilahTimeline2!!.quranEntity.setCopyRect()
            }
            val entityBismilahTemplate2 = EntityBismilahTemplate(
                bismilahTimeline2!!.transition, f27, f28,
                bismilahTimeline2!!.quranEntity.copyRect!!.left * engineActivity.mTemplate!!.width,
                engineActivity.mTemplate!!.height * bismilahTimeline2!!.quranEntity.copyRect!!.top,
                bismilahTimeline2!!.rect.left / bismilahTimeline2!!.scaleFactor,
                bismilahTimeline2!!.rect.right / bismilahTimeline2!!.scaleFactor,
                bismilahTimeline2!!.quranEntity.txt!!, bismilahTimeline2!!.quranEntity.clrAya,
                bismilahTimeline2!!.quranEntity.mPreset
            )
            entityBismilahTemplate2.height = (bismilahTimeline2!!.quranEntity.copyRect!!.bottom * engineActivity.mTemplate!!.height) - (bismilahTimeline2!!.quranEntity.copyRect!!.top * engineActivity.mTemplate!!.height)
            entityBismilahTemplate2.factor_size = bismilahTimeline2!!.quranEntity.factorSize
            entityBismilahTemplate2.scale = bismilahTimeline2!!.quranEntity.scaleFactor
            entityBismilahTemplate2.file = bismilahTimeline2!!.file
            entityBismilahTemplate2.file_in = bismilahTimeline2!!.file_in
            entityBismilahTemplate2.file_out = bismilahTimeline2!!.file_out
            entityBismilahTemplate2.rectF = MRectF(bismilahTimeline2!!.quranEntity.copyRect!!.left, bismilahTimeline2!!.quranEntity.copyRect!!.top, bismilahTimeline2!!.quranEntity.copyRect!!.right, bismilahTimeline2!!.quranEntity.copyRect!!.bottom)
            engineActivity.mTemplate!!.entityBismilahTemplate = entityBismilahTemplate2
        }

        if (engineActivity.blurredImageView.surahNameEntity != null) {
            if (engineActivity.mTemplate!!.entitySurahTemplate == null) {
                try {
                    if (engineActivity.blurredImageView.surahNameEntity!!.copyRect == null) {
                        engineActivity.blurredImageView.surahNameEntity!!.setCopyRect()
                    }
                    engineActivity.mTemplate!!.entitySurahTemplate = EntitySurahTemplate(
                        engineActivity.blurredImageView.surahNameEntity!!.name,
                        engineActivity.blurredImageView.surahNameEntity!!.reader,
                        engineActivity.mTemplate!!.mDrawingTranslationX + engineActivity.blurredImageView.rectFSurahName!!.left,
                        engineActivity.mTemplate!!.mDrawingTranslationY + engineActivity.blurredImageView.rectFSurahName!!.top,
                        MRectF(engineActivity.blurredImageView.surahNameEntity!!.copyRect!!.left, engineActivity.blurredImageView.surahNameEntity!!.copyRect!!.top, engineActivity.blurredImageView.surahNameEntity!!.copyRect!!.right, engineActivity.blurredImageView.surahNameEntity!!.copyRect!!.bottom),
                        engineActivity.blurredImageView.surahNameEntity!!.scaleFactor,
                        engineActivity.blurredImageView.surahNameEntity!!.nameFont,
                        engineActivity.blurredImageView.surahNameEntity!!.clrS_name,
                        engineActivity.blurredImageView.surahNameEntity!!.mPreset,
                        engineActivity.blurredImageView.surahNameEntity!!.style,
                        engineActivity.blurredImageView.surahNameEntity!!.index_surah,
                        engineActivity.blurredImageView.surahNameEntity!!.isHaveBg,
                        engineActivity.blurredImageView.surahNameEntity!!.clrBg
                    )
                } catch (e3: Exception) {
                    e3.printStackTrace()
                }
            } else {
                engineActivity.mTemplate!!.entitySurahTemplate!!.clrBg = engineActivity.blurredImageView.surahNameEntity!!.clrBg
                engineActivity.mTemplate!!.entitySurahTemplate!!.isHaveBg = engineActivity.blurredImageView.surahNameEntity!!.isHaveBg
                engineActivity.mTemplate!!.entitySurahTemplate!!.index_surah = engineActivity.blurredImageView.surahNameEntity!!.index_surah
                engineActivity.mTemplate!!.entitySurahTemplate!!.style = engineActivity.blurredImageView.surahNameEntity!!.style
                engineActivity.mTemplate!!.entitySurahTemplate!!.clr = engineActivity.blurredImageView.surahNameEntity!!.clrS_name
                engineActivity.mTemplate!!.entitySurahTemplate!!.preset = engineActivity.blurredImageView.surahNameEntity!!.mPreset
                engineActivity.mTemplate!!.entitySurahTemplate!!.name_font = engineActivity.blurredImageView.surahNameEntity!!.nameFont
                engineActivity.mTemplate!!.entitySurahTemplate!!.factor_scale = engineActivity.blurredImageView.surahNameEntity!!.scaleFactor
                engineActivity.mTemplate!!.entitySurahTemplate!!.rectF = MRectF(engineActivity.blurredImageView.surahNameEntity!!.copyRect!!.left, engineActivity.blurredImageView.surahNameEntity!!.copyRect!!.top, engineActivity.blurredImageView.surahNameEntity!!.copyRect!!.right, engineActivity.blurredImageView.surahNameEntity!!.copyRect!!.bottom)
                engineActivity.mTemplate!!.entitySurahTemplate!!.name = engineActivity.blurredImageView.surahNameEntity!!.name
                engineActivity.mTemplate!!.entitySurahTemplate!!.reader = engineActivity.blurredImageView.surahNameEntity!!.reader
                engineActivity.mTemplate!!.entitySurahTemplate!!.setPos(engineActivity.blurredImageView.rectFSurahName!!.left + engineActivity.mTemplate!!.mDrawingTranslationX, engineActivity.blurredImageView.rectFSurahName!!.top + engineActivity.mTemplate!!.mDrawingTranslationY)
            }
        }

        if (engineActivity.mTemplate!!.entityProgressTemplate == null) {
            engineActivity.mTemplate!!.entityProgressTemplate = EntityProgressTemplate(Utils.f2(engineActivity.blurredImageView.rectFProgress!!.left + engineActivity.mTemplate!!.mDrawingTranslationX), Utils.f2(engineActivity.blurredImageView.rectFProgress!!.top + engineActivity.mTemplate!!.mDrawingTranslationY))
        } else {
            engineActivity.mTemplate!!.entityProgressTemplate!!.left = Utils.f2(engineActivity.blurredImageView.rectFProgress!!.left + engineActivity.mTemplate!!.mDrawingTranslationX)
            engineActivity.mTemplate!!.entityProgressTemplate!!.top = Utils.f2(engineActivity.blurredImageView.rectFProgress!!.top + engineActivity.mTemplate!!.mDrawingTranslationY)
        }

        engineActivity.mTemplate!!.entityMediaList.clear()
        val it2 = engineActivity.trackViewEntity.entityListAudio.iterator()
        while (it2.hasNext()) {
            val next = it2.next()
            if (next.visible() && next.end > next.start) {
                val entityMedia = EntityMedia(
                    next.uri.toString(), next.minDuration, next.start, next.end,
                    (next.rect.left / engineActivity.trackViewEntity.scaleFactor).toInt(),
                    (next.rect.right / engineActivity.trackViewEntity.scaleFactor).toFloat(),
                    Math.round(next.end - next.start).toFloat(), next.offset, next.getOffsetRight(), next.getOffsetLeft(),
                    next.max, next.effectAudio.fade_in.toFloat(), next.effectAudio.fade_out.toFloat(),
                    (next.rect.left / engineActivity.trackViewEntity.second_in_screen) * 1000.0f
                )
                entityMedia.paths_https = next.pathsHttp
                entityMedia.effectAudio = next.effectAudio
                entityMedia.path_ffmpeg = next.getPathFfmpeg()
                entityMedia.video_path = next.videoPath
                entityMedia.path_ffmpeg_effect = next.getPathFfmpegEffect()
                entityMedia.isApplyEffectInPreview = next.isApplyEffectInPreview
                engineActivity.mTemplate!!.addMedia(entityMedia)
                next.release()
            }
        }
        val idStr = "Template_" + System.currentTimeMillis()
        val idTemplate = engineActivity.mTemplate!!.idTemplate
        engineActivity.mTemplate!!.idTemplate = idStr
        engineActivity.mTemplate!!.uri_video = FileHelper(engineActivity).createPublicVideoFolder(engineActivity.mResources!!.getString(R.string.app_name))!!.absolutePath!! + "/" + System.currentTimeMillis() + "_NurMontage.mp4"
        val template = engineActivity.mTemplate
        LocalPersistence.writeTemplate(engineActivity, template!!, idTemplate!!, template.idTemplate!!)
        LocalPersistence.deleteTemplate(engineActivity, Constants.TEMPLATE_TMP)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// =====================================================================
// checkPermissionAudio() / pickAudio() / onRequestPermissionsResult()
// =====================================================================

private fun checkPermissionAudio(): Boolean {
    if (Build.VERSION.SDK_INT < 33 || ActivityCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_AUDIO") == 0) {
        return true
    }
    ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_MEDIA_AUDIO"), 2)
    return false
}

fun pickAudio() {
    try {
        val intent = Intent("android.intent.action.OPEN_DOCUMENT")
        intent.addCategory("android.intent.category.OPENABLE")
        intent.type = "audio/*"
        activityLauncher!!.launch(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

override fun onRequestPermissionsResult(i: Int, strArr: Array<String>, iArr: IntArray) {
    super.onRequestPermissionsResult(i, strArr, iArr)
    if (i == 1) {
        if (iArr.isNotEmpty() && iArr[0] == 0) {
            save()
        } else {
            Toast.makeText(this, mResources!!.getString(R.string.permission_img), Toast.LENGTH_SHORT).show()
        }
    }
    if (i == 2) {
        if (iArr.isNotEmpty() && iArr[0] == 0) {
            pickAudio()
        } else {
            Toast.makeText(this, mResources!!.getString(R.string.permission_audio), Toast.LENGTH_SHORT).show()
        }
    }
    if (i == 10) {
        if ((Build.VERSION.SDK_INT >= 34 && ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == 0) || (iArr.isNotEmpty() && iArr[0] == 0)) {
            imageChooser()
        } else {
            Toast.makeText(this, mResources!!.getString(R.string.permission_img), Toast.LENGTH_SHORT).show()
        }
    }
    if (i == 11) {
        if ((Build.VERSION.SDK_INT >= 34 && ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == 0) || (iArr.isNotEmpty() && iArr[0] == 0)) {
            videoChooser()
        } else {
            Toast.makeText(this, mResources!!.getString(R.string.permission_video), Toast.LENGTH_SHORT).show()
        }
    }
    if (i == 12) {
        if ((Build.VERSION.SDK_INT >= 34 && ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == 0) || (iArr.isNotEmpty() && iArr[0] == 0)) {
            videoChooserForAudio()
        } else {
            Toast.makeText(this, mResources!!.getString(R.string.permission_video), Toast.LENGTH_SHORT).show()
        }
    }
}

// =====================================================================
// startTimelineAnimation() / startTimelineAnimationPreview()
// =====================================================================

fun startTimelineAnimation() {
    entityAudio_visible = null
    entityAudio_player = null
    lastIndexVisible = 0
    val maxTime = trackViewEntity.maxTime
    val timeLineW = trackViewEntity.timeLineW
    timeFormatter = TimeFormatter(maxTime.toLong())
    val smoothTimelineAnimator = SmoothTimelineAnimator(startCursur, maxTime, object : SmoothTimelineAnimator.AnimatorListener {
        override fun onUpdate(i: Int) {
            if (!mIsPlaying || i == 0) return
            val f = i.toFloat() / maxTime
            if (blurredImageView != null) {
                updateTime(i.toLong())
                blurredImageView.progress = f
            }
            trackViewEntity.updateCursur(f * timeLineW)
            trackViewEntity.current_cursur_position = i
            val abs = Math.abs(Math.round((trackViewEntity.currentPosition / trackViewEntity.second_in_screen) * 1000.0f))
            if (abs > endTimeAudioVisible) {
                entityAudio_visible = null
            }
            if (entityAudio_visible == null) {
                for (i2 in lastIndexVisible until trackViewEntity.entityListAudio.size) {
                    val ea = trackViewEntity.entityListAudio[i2]
                    if (ea.visible() && ea.isVisible) {
                        entityAudio_visible = ea
                        endTimeAudioVisible = Math.round((entityAudio_visible!!.rect.right / trackViewEntity.second_in_screen) * 1000.0f)
                        lastIndexVisible = i2
                        break
                    }
                }
            }
            try {
                if (entityAudio_visible != null) {
                    if (entityAudio_player !== entityAudio_visible && mPlayer != null && mPlayer!!.isPlaying) {
                        mPlayer!!.pause()
                    }
                    mPlayer = entityAudio_visible!!.mediaPlayer
                    if (mPlayer != null && !mPlayer!!.isPlaying) {
                        entityAudio_player = entityAudio_visible
                        val abs2 = ((abs - Math.abs(Math.round((entityAudio_visible!!.rect.left / trackViewEntity.second_in_screen) * 1000.0f))) + entityAudio_visible!!.start).toInt()
                        if (abs2 <= mPlayer!!.duration) {
                            mPlayer!!.seekTo(abs2)
                        }
                        Log.e("data", "" + mPlayer!!.currentPosition)
                        mPlayer!!.start()
                        Log.e("mPlayer c ", "" + mPlayer!!.isPlaying)
                    }
                } else if (mPlayer != null && mPlayer!!.isPlaying) {
                    mPlayer!!.pause()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            updateStartViewTime(trackViewEntity.current_cursur_position)
            updateBtnCutState()
        }

        override fun onEnd() {
            if (mIsPlaying) {
                mIsPlaying = false
                trackViewEntity.isPlaying = mIsPlaying
                blurredImageView.isPlaying = mIsPlaying
                stop()
                trackViewEntity.current_cursur_position = trackViewEntity.maxTime
                trackViewEntity.updateCursur(trackViewEntity.maxTime)
                try {
                    if (entityAudio_visible != null && entityAudio_visible!!.mediaPlayer != null && entityAudio_visible!!.mediaPlayer!!.isPlaying) {
                        entityAudio_visible!!.mediaPlayer!!.pause()
                    }
                    if (mPlayer != null && mPlayer!!.isPlaying) {
                        mPlayer!!.pause()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                startCursur = 0
                current_position_time = 0
                if (btnPlayPause != null) {
                    btnPlayPause!!.setImageResource(R.drawable.play_btn)
                }
                updateBtnToEnd()
                updateBtnToStart()
            }
        }
    })
    valueAnimator = smoothTimelineAnimator
    smoothTimelineAnimator.start()
    if (mTemplate!!.isVideoSquare) {
        start()
    }
}

fun startTimelineAnimationPreview(entityAudio: EntityAudio) {
    val maxTime = trackViewEntity.maxTime
    val timeLineW = trackViewEntity.timeLineW
    timeFormatter = TimeFormatter(maxTime.toLong())
    val smoothTimelineAnimator = SmoothTimelineAnimator(startCursur, maxTime, object : SmoothTimelineAnimator.AnimatorListener {
        override fun onUpdate(i: Int) {
            if (!mIsPlaying || i == 0) return
            val f = i.toFloat() / maxTime
            if (blurredImageView != null) {
                updateTime(i.toLong())
                blurredImageView.progress = f
            }
            trackViewEntity.updateCursur(f * timeLineW)
            trackViewEntity.current_cursur_position = i
            try {
                if (entityAudio.mediaPlayer != null && !entityAudio.mediaPlayer!!.isPlaying) {
                    val abs = ((Math.abs(Math.round((trackViewEntity.currentPosition / trackViewEntity.second_in_screen) * 1000.0f)) - Math.abs(Math.round((entityAudio.rect.left / trackViewEntity.second_in_screen) * 1000.0f))) + entityAudio.start).toInt()
                    if (abs <= entityAudio.mediaPlayer!!.duration) {
                        entityAudio.mediaPlayer!!.seekTo(abs)
                    }
                    entityAudio.mediaPlayer!!.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            updateStartViewTime(trackViewEntity.current_cursur_position)
        }

        override fun onEnd() {
            if (mIsPlaying) {
                mIsPlaying = false
                trackViewEntity.isPlaying = mIsPlaying
                blurredImageView.isPlaying = mIsPlaying
                stop()
                try {
                    if (entityAudio.mediaPlayer != null && entityAudio.mediaPlayer!!.isPlaying) {
                        entityAudio.mediaPlayer!!.pause()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                startCursur = trackViewEntity.current_cursur_position
            }
            VolumeFragment.instance?.updateButton()
            SpeedFragment.instance?.updateButton()
            FadeInOutFragment.instance?.updateButton()
            EchoEffectFragment.instance?.updateButton()
            EnhanceVoiceFragment.instance?.updateButton()
            RemoveNoiceFragment.instance?.updateButton()
        }
    })
    valueAnimator = smoothTimelineAnimator
    smoothTimelineAnimator.start()
    if (mTemplate!!.isVideoSquare) {
        start()
    }
}

// =====================================================================
// updateTime(long) / initTimeLineView()
// =====================================================================

private fun updateTime(j: Long) {
    val tf = timeFormatter
    if (tf == null) {
        timeFormatter = TimeFormatter(trackViewEntity.maxTime.toLong())
    } else {
        tf.setTotalDurationMs(trackViewEntity.maxTime.toLong())
    }
    val formatTime = timeFormatter!!.formatTime(j)
    blurredImageView.setCurrentTime(formatTime.first as String, formatTime.second as String)
}

private fun initTimeLineView() {
    tv_currentTime = findViewById(R.id.tv_current_time)
    tv_endTime = findViewById(R.id.tv_end_time)
    val trackEntityView = findViewById<TrackEntityView>(R.id.time_line_view)
    trackViewEntity = trackEntityView
    trackEntityView.setiTrimLineCallback(iTrimLineCallback)
    trackViewEntity.scaleFactor = mTemplate!!.scale_timeline
    trackViewEntity.post {
        val screenWidth = ScreenUtils.getScreenWidth(this@EngineActivity)
        val f = screenWidth * 0.12f
        // // trackViewEntity.secondInScreen = f // TODO // TODO: fix
        // // trackViewEntity.secondInScreen = f // TODO // TODO: fix // TODO: fix second_in_screenNoScale call
        trackViewEntity.maxTime = 0
        trackViewEntity.init(screenWidth, trackViewEntity.height)
        trackViewEntity.setPosCursur(mTemplate!!.currentCursur)
        startCursur = trackViewEntity.current_cursur_position
        updateViewTime(trackViewEntity.maxTime, trackViewEntity.current_cursur_position)
    }
}

// =====================================================================
// addAudioTemplateHttp() / buildSpeedFilters() / addAudioFromVideo()
// =====================================================================

private fun addAudioTemplateHttp(uri: Uri?, i: Int, str: String?) {
    try {
        if (isDestroyed) return
        if (uri == null) {
            hideProgressFragment()
            return
        }
        if (mTemplate!!.entityMediaList != null) {
            updateProgress(i + 1, mTemplate!!.entityMediaList.size)
        }
        val uri2 = if (str != null) {
            uri.path
        } else if (!uri.toString().contains("share_with_me")) {
            AudioUtils.copyFromUri(this, uri, mTemplate!!.folder_template!!)!!
        } else {
            uri.toString()
        }
        val str2 = uri2 ?: return
        val entityMedia = mTemplate!!.entityMediaList[i]

        if (entityMedia.isApplyEffectInPreview) {
            val file = File(mTemplate!!.folder_template, System.currentTimeMillis().toString() + "_audio_echo.mp3")
            val effectAudio = entityMedia.effectAudio
            val start = effectAudio!!.start / 1000.0f
            val end = effectAudio!!.end / 1000.0f
            val arrayList = ArrayList<String>()
            arrayList.add("atrim=start=$start:end=$end")
            arrayList.add("asetpts=N/SR/TB")
            if (effectAudio!!.isRemoveNoice) {
                arrayList.add("afftdn=nf=-25")
            }
            arrayList.add(String.format(Locale.US, "volume=%.2f", effectAudio!!.volume))
            if (effectAudio!!.fade_in > 0) {
                arrayList.add("afade=t=in:st=0:d=${effectAudio!!.fade_in}")
            }
            if (effectAudio!!.fade_out > 0) {
                val fade_out = effectAudio!!.fade_out
                arrayList.add("afade=t=out:st=${(end - start) - fade_out}:d=$fade_out")
            }
            if (effectAudio!!.isEnhance) {
                arrayList.add(Common.ENHANCE_CMD)
            }
            if (effectAudio!!.reverbPreset != null) {
                arrayList.add(effectAudio!!.reverbPreset!!)
            }
            if (effectAudio!!.decays > 0) {
                arrayList.add(String.format(Locale.US, "aecho=%.2f:%.2f:%s:%s", 1.0f, effectAudio!!.outGain, effectAudio!!.delays_cmd, effectAudio!!.decays_cmd))
            }
            if (effectAudio!!.speed != 1.0f) {
                arrayList.addAll(buildSpeedFilters(effectAudio!!.speed))
            }
            id_ffmpeg.add(FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", str2, "-af", TextUtils.join(",", arrayList), "-y", file.absolutePath), object : FFmpegSessionCompleteCallback {
                override fun apply(fFmpegSession: FFmpegSession) {
                    try {
                        mPlayer = MediaPlayer()
                        mPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                        val fromFile = Uri.fromFile(file)
                        if (fromFile.scheme != null && fromFile.scheme!!.startsWith("http")) {
                            mPlayer!!.setDataSource(fromFile.toString())
                        } else {
                            mPlayer!!.setDataSource(this@EngineActivity, fromFile)
                        }
                        mPlayer!!.prepareAsync()
                        mPlayer!!.setOnPreparedListener { mediaPlayer ->
                            if (mediaPlayer == null) return@setOnPreparedListener
                            try {
                                addEntitMediaHttp(entityMedia, effectAudio!!.duration, uri, mediaPlayer, entityMedia.paths_https!!, i, str2, str)
                            } catch (unused: Exception) {
                                hideProgressFragment()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }).sessionId)
            return
        }

        val mediaPlayer = MediaPlayer()
        mPlayer = mediaPlayer
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        if (uri.scheme != null && uri.scheme!!.startsWith("http")) {
            mPlayer!!.setDataSource(uri.toString())
        } else {
            mPlayer!!.setDataSource(this, uri)
        }
        mPlayer!!.prepareAsync()
        mPlayer!!.setOnPreparedListener { mediaPlayer2 ->
            if (mediaPlayer2 == null) return@setOnPreparedListener
            try {
                addEntitMediaHttp(entityMedia, mediaPlayer2.duration, uri, mediaPlayer2, entityMedia.paths_https!!, i, str2, str)
            } catch (unused: Exception) {
                hideProgressFragment()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        hideProgressFragment()
    }
}

private fun buildSpeedFilters(f: Float): List<String> {
    val arrayList = ArrayList<String>()
    var speed = f
    if (speed < 0.5f) {
        while (speed < 0.5f) {
            arrayList.add("atempo=0.5")
            speed /= 0.5f
        }
        arrayList.add(String.format(Locale.US, "atempo=%.2f", speed))
    } else if (speed > 2.0f) {
        while (speed > 2.0f) {
            arrayList.add("atempo=2.0")
            speed /= 2.0f
        }
        arrayList.add(String.format(Locale.US, "atempo=%.2f", speed))
    } else {
        arrayList.add(String.format(Locale.US, "atempo=%.2f", speed))
    }
    return arrayList
}

private fun addAudioFromVideo(uri: Uri, str: String) {
    try {
        val mediaPlayer = MediaPlayer()
        mPlayer = mediaPlayer
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        if (uri.scheme != null && uri.scheme!!.startsWith("http")) {
            mPlayer!!.setDataSource(uri.toString())
        } else {
            mPlayer!!.setDataSource(this, uri)
        }
        mPlayer!!.prepareAsync()
        mPlayer!!.setOnPreparedListener { mediaPlayer2 ->
            if (mediaPlayer2 == null) return@setOnPreparedListener
            changeEntityAudioFromVideo(mediaPlayer2.duration, uri, str)
            try {
                runOnUiThread { updateTimeToEndAya() }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    hideProgressFragment()
                    hideFragment()
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        hideFragment()
        hideProgressFragment()
    }
}

// =====================================================================
// updateProgress() / addAudioReciters()
// =====================================================================

private fun updateProgress(i: Int, i2: Int) {
    runOnUiThread {
        if (ProgressViewFragment.getInstance() != null) {
            ProgressViewFragment.getInstance().update(i, i2)
        }
    }
}

private fun addAudioReciters(list: List<RecitersModel>, i: Int) {
    try {
        if (isDestroyed) return
        updateProgress(i + 1, list.size)
        if (i >= list.size) {
            runOnUiThread {
                updateTime()
                trackViewEntity.translateToEnd()
                updateBtnToEnd()
                updateBtnToStart()
                hideProgressFragment()
                hideFragment()
            }
            return
        }
        val recitersModel = list[i]
        val parse = if (recitersModel.isTarteel) {
            Uri.parse("https://audio-cdn.tarteel.ai/quran/${recitersModel.identifer}/${recitersModel.surah_index}${recitersModel.number_aya}.mp3")
        } else {
            Uri.parse("https://everyayah.com/data/${recitersModel.identifer}/${recitersModel.surah_index}${recitersModel.number_aya}.mp3")
        }
        val mediaPlayer = MediaPlayer()
        mPlayer = mediaPlayer
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        if (parse.scheme != null && parse.scheme!!.startsWith("http")) {
            mPlayer!!.setDataSource(parse.toString())
        } else {
            mPlayer!!.setDataSource(this, parse)
        }
        mPlayer!!.prepareAsync()
        mPlayer!!.setOnPreparedListener { mediaPlayer2 ->
            if (mediaPlayer2 == null) {
                hideProgressFragment()
            } else {
                changeEntityAudioReciters(mediaPlayer2.duration, parse, mediaPlayer2, list, i)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        hideProgressFragment()
    }
}

// =====================================================================
// addEntitMediaHttp() - 8-param version
// =====================================================================

private fun addEntitMediaHttp(entityMedia: EntityMedia, i: Int, uri: Uri, mediaPlayer: MediaPlayer, list: List<String>, i2: Int, str: String, str2: String?) {
    val round = Math.round(trackViewEntity.width * 0.077f)
    val round2 = Math.round(trackViewEntity.second_in_screenNoScale * (i / 1000.0f))

    val entityAudio: EntityAudio? = if (entityMedia.start != entityMedia.end) {
        val posX = if (mTemplate!!.isNewCode) entityMedia.posX else (entityMedia.posX / 1000.0f) * trackViewEntity.second_in_screen
        val posY = if (mTemplate!!.isNewCode) entityMedia.posY else (entityMedia.posY / 1000.0f) * trackViewEntity.second_in_screen
        EntityAudio(null, uri, posX, 0.0f, round.toFloat(), posY, entityMedia.max, trackViewEntity.second_in_screenNoScale, i, entityMedia.offset, entityMedia.offset_right, entityMedia.offset_left).also { ea ->
            ea.setPathHttp(list)
            ea.mediaPlayer = mediaPlayer
            ea.videoPath = str2
            ea.start = entityMedia.start
            ea.minDuration = entityMedia.start_original
            if (entityMedia.end != 0.0f) {
                ea.end = entityMedia.end
            }
            ea.effectAudio = entityMedia.effectAudio!!
            ea.setFadeIn(entityMedia.duration_fade_in)
            ea.setFadeOut(entityMedia.duration_fade_out)
            trackViewEntity.addAudio(ea)
        }
    } else null

    if (round2 <= 0 || round <= 0) {
        trackViewEntity.invalidate()
        hideProgressFragment()
        return
    }
    try {
        val file = File(mTemplate!!.folder_template, System.currentTimeMillis().toString() + "_output.pcm")
        val arrayList = ArrayList<String>()
        arrayList.add("-i")
        arrayList.add(str)
        arrayList.add("-map")
        arrayList.add("0:a")
        arrayList.add("-ac")
        arrayList.add("1")
        arrayList.add("-ar")
        arrayList.add("44100")
        arrayList.add("-f")
        arrayList.add("s16le")
        arrayList.add(file.absolutePath)
        arrayList.add("-y")
        id_ffmpeg.add(FFmpegKit.executeWithArgumentsAsync(arrayList.toTypedArray(), object : FFmpegSessionCompleteCallback {
            override fun apply(fFmpegSession: FFmpegSession) {
                if (fFmpegSession.returnCode.isValueSuccess()) {
                    try {
                        entityAudio?.setAmps(PCMWaveformExtractor.extractWaveform(file.absolutePath, round2 / ((round * 0.1f).toInt() + (round * 0.07f).toInt())))
                        entityAudio?.pathFfmpeg = str
                        val i4 = i2 + 1
                        if (i4 >= mTemplate!!.entityMediaList.size) {
                            try {
                                runOnUiThread {
                                    updateTime()
                                    trackViewEntity.invalidate()
                                    hideProgressFragment()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            val entityMedia2 = mTemplate!!.entityMediaList[i4]
                            if (entityMedia2.video_path != null) {
                                entityMedia.video_path = AudioUtils.copyFromUri(this@EngineActivity, Uri.parse(mTemplate!!.uri_upload_extract_audio_video), mTemplate!!.folder_template!!)
                                if (mTemplate!!.extension != null) {
                                    addAudioFromVideoWithExtention(mTemplate!!.extension!!, entityMedia.video_path!!, i4)
                                } else {
                                    start_extenstion = 0
                                    extractAudioFromVideoRecursive(entityMedia.video_path!!, 0, true, i4)
                                }
                            } else if (entityMedia2.paths_https != null) {
                                addAudioRecitersTemplate(entityMedia2.paths_https!!, i4, "")
                            } else {
                                addAudioTemplateHttp(Uri.parse(entityMedia2.uri), i4, null)
                            }
                        }
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                        runOnUiThread {
                            hideProgressFragment()
                            hideFragment()
                        }
                    }
                }
            }
        }).sessionId)
    } catch (e: Exception) {
        e.printStackTrace()
        hideProgressFragment()
    }
    trackViewEntity.invalidate()
}

// =====================================================================
// changeEntityAudio(int, Uri, List, int, String) + lambda
// =====================================================================

private fun changeEntityAudio(i: Int, uri: Uri, list: List<String>, i2: Int, str: String) {
    try {
        val audio = trackViewEntity.getAudio()
        val scaleFactor = if (trackViewEntity.entityListAudio.isEmpty() || audio == null) 0.0f else audio.rect.right / trackViewEntity.scaleFactor
        val round = Math.round(trackViewEntity.width * 0.077f)
        val round2 = Math.round(trackViewEntity.second_in_screenNoScale * (i / 1000.0f))
        val f = round2.toFloat()
        val entityAudio = EntityAudio(null, uri, scaleFactor, 0.0f, round.toFloat(), f + scaleFactor, f, trackViewEntity.second_in_screenNoScale, i)
        entityAudio.mediaPlayer = mPlayer
        entityAudio.setPathHttp(list)
        entityAudio.effectAudio.end = entityAudio.end
        entityAudio.effectAudio.start = entityAudio.start
        entityAudio.effectAudio.duration = (entityAudio.end - entityAudio.start).toInt()
        trackViewEntity.addAudio(entityAudio)
        if (round2 > 0 && round > 0) {
            executor.execute {
                changeEntityAudioLambda(uri, round, round2, str, entityAudio, i2)
            }
            trackViewEntity.invalidate()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        hideProgressFragment()
        hideFragment()
    }
}

private fun changeEntityAudioLambda(uri: Uri, i: Int, i2: Int, str: String, entityAudio: EntityAudio, i3: Int) {
    try {
        val copyFromUri = AudioUtils.copyFromUri(this, uri, mTemplate!!.folder_template!!)!!
        val f = i.toFloat()
        entityAudio.setAmps(PCMWaveformExtractor.extractWaveform(str, i2 / ((0.1f * f).toInt() + (f * 0.07f).toInt())))
        entityAudio.pathFfmpeg = copyFromUri
        if (i3 != -1) {
            val i4 = i3 + 1
            if (i4 >= mTemplate!!.entityMediaList.size) {
                try {
                    runOnUiThread {
                        updateTimeToEndAya()
                        updateBtnToEnd()
                        updateBtnToStart()
                        hideProgressFragment()
                        hideFragment()
                    }
                    return
                } catch (e: Exception) {
                    e.printStackTrace()
                    hideProgressFragment()
                    hideFragment()
                    return
                }
            }
            val entityMedia = mTemplate!!.entityMediaList[i3]
            val entityMedia2 = mTemplate!!.entityMediaList[i4]
            if (entityMedia2.video_path != null) {
                entityMedia.video_path = AudioUtils.copyFromUri(this, Uri.parse(mTemplate!!.uri_upload_extract_audio_video), mTemplate!!.folder_template!!)
                if (mTemplate!!.extension != null) {
                    addAudioFromVideoWithExtention(mTemplate!!.extension!!, entityMedia.video_path!!, i4)
                    return
                } else {
                    start_extenstion = 0
                    extractAudioFromVideoRecursive(entityMedia.video_path!!, 0, true, i4)
                    return
                }
            }
            if (entityMedia2.paths_https != null) {
                addAudioRecitersTemplate(entityMedia2.paths_https!!, i4, "")
                return
            } else {
                addAudioTemplateHttp(Uri.parse(entityMedia2.uri), i4, null)
                return
            }
        }
        try {
            runOnUiThread {
                trackViewEntity.calculMaxTime()
                updateViewTime(trackViewEntity.maxTime, trackViewEntity.current_cursur_position)
                trackViewEntity.translateToEnd()
                updateTimeToEndAya()
                updateBtnToEnd()
                updateBtnToStart()
                trackViewEntity.invalidate()
                hideProgressFragment()
                hideFragment()
            }
            return
        } catch (e2: Exception) {
            e2.printStackTrace()
            hideProgressFragment()
            hideFragment()
            return
        }
    } catch (e3: Exception) {
        e3.printStackTrace()
        hideProgressFragment()
        hideFragment()
    }
}

// =====================================================================
// addEntitMediaHttp() - 9-param version
// =====================================================================

private fun addEntitMediaHttp(entityMedia: EntityMedia, i: Int, uri: Uri, mediaPlayer: MediaPlayer, list: List<String>, i2: Int, str: String, str2: String, str3: String?) {
    val round = Math.round(trackViewEntity.width * 0.077f)
    val round2 = Math.round(trackViewEntity.second_in_screenNoScale * (i / 1000.0f))

    val entityAudioVal: EntityAudio? = if (entityMedia.start != entityMedia.end) {
        val posX = if (mTemplate!!.isNewCode) entityMedia.posX else (entityMedia.posX / 1000.0f) * trackViewEntity.second_in_screen
        val posY = if (mTemplate!!.isNewCode) entityMedia.posY else (entityMedia.posY / 1000.0f) * trackViewEntity.second_in_screen
        EntityAudio(null, uri, posX, 0.0f, round.toFloat(), posY, entityMedia.max, trackViewEntity.second_in_screenNoScale, i, entityMedia.offset, entityMedia.offset_right, entityMedia.offset_left).also { ea ->
            ea.setPathHttp(list)
            ea.mediaPlayer = mediaPlayer
            ea.videoPath = str3
            ea.start = entityMedia.start
            ea.minDuration = entityMedia.start_original
            if (entityMedia.end != 0.0f) {
                ea.end = entityMedia.end
            }
            ea.effectAudio = entityMedia.effectAudio!!
            ea.setFadeIn(entityMedia.duration_fade_in)
            ea.setFadeOut(entityMedia.duration_fade_out)
            trackViewEntity.addAudio(ea)
        }
    } else null

    val entityAudio2 = entityAudioVal
    if (round2 <= 0 || round <= 0) {
        trackViewEntity.invalidate()
        hideProgressFragment()
    } else {
        executor.execute {
            try {
                entityAudio2?.setAmps(PCMWaveformExtractor.extractWaveform(str2, round2 / ((round * 0.1f).toInt() + (round * 0.07f).toInt())))
                entityAudio2?.pathFfmpeg = str
                val i4 = i2 + 1
                if (i4 >= mTemplate!!.entityMediaList.size) {
                    try {
                        runOnUiThread {
                            updateTime()
                            trackViewEntity.invalidate()
                            hideProgressFragment()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    val entityMedia2 = mTemplate!!.entityMediaList[i4]
                    if (entityMedia2.video_path != null) {
                        entityMedia.video_path = AudioUtils.copyFromUri(this@EngineActivity, Uri.parse(mTemplate!!.uri_upload_extract_audio_video), mTemplate!!.folder_template!!)
                        if (mTemplate!!.extension != null) {
                            addAudioFromVideoWithExtention(mTemplate!!.extension!!, entityMedia.video_path!!, i4)
                        } else {
                            start_extenstion = 0
                            extractAudioFromVideoRecursive(entityMedia.video_path!!, 0, true, i4)
                        }
                    } else if (entityMedia2.paths_https != null) {
                        addAudioRecitersTemplate(entityMedia2.paths_https!!, i4, "")
                    } else {
                        addAudioTemplateHttp(Uri.parse(entityMedia2.uri), i4, null)
                    }
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
                runOnUiThread {
                    hideProgressFragment()
                    hideFragment()
                }
            }
        }
    }
}

// =====================================================================
// addAudio() overloads
// =====================================================================

private fun addAudio(uri: Uri) {
    try {
        val mediaPlayer = MediaPlayer()
        mPlayer = mediaPlayer
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        if (uri.scheme != null && uri.scheme!!.startsWith("http")) {
            mPlayer!!.setDataSource(uri.toString())
        } else {
            mPlayer!!.setDataSource(this, uri)
        }
        mPlayer!!.prepareAsync()
        mPlayer!!.setOnPreparedListener { mediaPlayer2 ->
            if (mediaPlayer2 == null) return@setOnPreparedListener
            changeEntityAudio(mediaPlayer2.duration, uri)
        }
    } catch (e: Exception) {
        hideProgressFragment()
        hideFragment()
        e.printStackTrace()
    }
}

private fun addAudio(uri: Uri, list: List<String>, i: Int, str: String) {
    try {
        val mediaPlayer = MediaPlayer()
        mPlayer = mediaPlayer
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        if (uri.scheme != null && uri.scheme!!.startsWith("http")) {
            mPlayer!!.setDataSource(uri.toString())
        } else {
            mPlayer!!.setDataSource(this, uri)
        }
        mPlayer!!.prepareAsync()
        mPlayer!!.setOnPreparedListener { mediaPlayer2 ->
            if (mediaPlayer2 == null) return@setOnPreparedListener
            changeEntityAudio(mediaPlayer2.duration, uri, list, i, str)
        }
    } catch (e: Exception) {
        hideProgressFragment()
        hideFragment()
        e.printStackTrace()
    }
}

// =====================================================================
// addAudioTemplate()
// =====================================================================

private fun addAudioTemplate(uri: Uri, list: List<String>, i: Int, str: String, str2: String, str3: String) {
    try {
        val mediaPlayer = MediaPlayer()
        mPlayer = mediaPlayer
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        if (uri.scheme != null && uri.scheme!!.startsWith("http")) {
            mPlayer!!.setDataSource(uri.toString())
        } else {
            mPlayer!!.setDataSource(this, uri)
        }
        mPlayer!!.prepareAsync()
        mPlayer!!.setOnPreparedListener { mediaPlayer2 ->
            if (mediaPlayer2 != null && i < mTemplate!!.entityMediaList.size) {
                addEntitMediaHttp(mTemplate!!.entityMediaList[i], mediaPlayer2.duration, uri, mPlayer!!, list, i, str, str2, str3)
            }
        }
    } catch (e: Exception) {
        hideProgressFragment()
        hideFragment()
        e.printStackTrace()
    }
}

// =====================================================================
// changeEntityAudioFromVideo()
// =====================================================================

private fun changeEntityAudioFromVideo(i: Int, uri: Uri, str: String) {
    try {
        val audio = trackViewEntity.getAudio()
        val scaleFactor = if (trackViewEntity.entityListAudio.isEmpty() || audio == null) 0.0f else audio.rect.right / trackViewEntity.scaleFactor
        val round = Math.round(trackViewEntity.width * 0.077f)
        val round2 = Math.round(trackViewEntity.second_in_screenNoScale * (i / 1000.0f))
        val f = round2.toFloat()
        val entityAudio = EntityAudio(null, uri, scaleFactor, 0.0f, round.toFloat(), f + scaleFactor, f, trackViewEntity.second_in_screenNoScale, i)
        entityAudio.mediaPlayer = mPlayer
        entityAudio.effectAudio.end = entityAudio.end
        entityAudio.effectAudio.start = entityAudio.start
        entityAudio.effectAudio.duration = (entityAudio.end - entityAudio.start).toInt()
        trackViewEntity.addAudio(entityAudio)
        if (round2 > 0 && round > 0) {
            val copyFromUri = AudioUtils.copyFromUri(this, uri, mTemplate!!.folder_template!!)!!
            val file = File(mTemplate!!.folder_template, System.currentTimeMillis().toString() + "_output.pcm")
            val arrayList = ArrayList<String>()
            arrayList.add("-i")
            arrayList.add(copyFromUri)
            arrayList.add("-map")
            arrayList.add("0:a")
            arrayList.add("-ac")
            arrayList.add("1")
            arrayList.add("-ar")
            arrayList.add("44100")
            arrayList.add("-f")
            arrayList.add("s16le")
            arrayList.add(file.absolutePath)
            arrayList.add("-y")
            id_ffmpeg.add(FFmpegKit.executeWithArgumentsAsync(arrayList.toTypedArray(), object : FFmpegSessionCompleteCallback {
                override fun apply(fFmpegSession: FFmpegSession) {
                    if (fFmpegSession.returnCode.isValueSuccess()) {
                        try {
                            entityAudio.setAmps(PCMWaveformExtractor.extractWaveform(file.absolutePath, round2 / ((round * 0.1f).toInt() + (round * 0.07f).toInt())))
                            entityAudio.pathFfmpeg = uri.path
                            entityAudio.videoPath = str
                            runOnUiThread {
                                trackViewEntity.invalidate()
                                hideProgressFragment()
                                hideFragment()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            runOnUiThread {
                                hideProgressFragment()
                                hideFragment()
                            }
                        }
                    }
                }
            }).sessionId)
            trackViewEntity.invalidate()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        runOnUiThread {
            hideProgressFragment()
            hideFragment()
        }
    }
}

// =====================================================================
// changeEntityAudioReciters()
// =====================================================================

private fun changeEntityAudioReciters(i: Int, uri: Uri, mediaPlayer: MediaPlayer, list: List<RecitersModel>, i2: Int) {
    try {
        val audio = trackViewEntity.getAudio()
        val scaleFactor = if (trackViewEntity.entityListAudio.isEmpty() || audio == null) 0.0f else audio.rect.right / trackViewEntity.scaleFactor
        val round = Math.round(trackViewEntity.width * 0.077f)
        val round2 = Math.round(trackViewEntity.second_in_screenNoScale * (i / 1000.0f))
        val f = round2.toFloat()
        val entityAudio = EntityAudio(null, uri, scaleFactor, 0.0f, round.toFloat(), f + scaleFactor, f, trackViewEntity.second_in_screenNoScale, i)
        entityAudio.effectAudio.end = entityAudio.end
        entityAudio.effectAudio.start = entityAudio.start
        entityAudio.effectAudio.duration = (entityAudio.end - entityAudio.start).toInt()
        entityAudio.mediaPlayer = mediaPlayer
        trackViewEntity.addAudio(entityAudio)
        if (round2 > 0 && round > 0) {
            AudioUtils.copyToLocalAsync(this, uri.toString(), mTemplate!!.folder_template!!, object : AudioUtils.Callback {
                override fun onSuccess(str: String) {
                    try {
                        val file = File(mTemplate!!.folder_template, System.currentTimeMillis().toString() + "_audio_wave.png")
                        id_ffmpeg.add(FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", str, "-filter_complex", "aformat=channel_layouts=mono,showwavespic=s=${round}x${round2}:colors=#522123", "-frames:v", "1", "-y", file.absolutePath), object : FFmpegSessionCompleteCallback {
                            override fun apply(fFmpegSession: FFmpegSession) {
                                if (fFmpegSession.returnCode.isValueSuccess()) {
                                    try {
                                        Glide.with(this@EngineActivity as FragmentActivity).asBitmap().load(Uri.fromFile(file)).submit().get()
                                        entityAudio.pathFfmpeg = str
                                        runOnUiThread { trackViewEntity.invalidate() }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        hideProgressFragment()
                                    }
                                }
                                addAudioReciters(list, i2 + 1)
                            }
                        }).sessionId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        hideProgressFragment()
                    }
                }

                override fun onError(exc: Exception) {
                    exc.printStackTrace()
                    hideProgressFragment()
                }
            })
        }
    } catch (e: Exception) {
        e.printStackTrace()
        hideProgressFragment()
    }
}

// =====================================================================
// duplicateEntityAudio()
// =====================================================================

private fun duplicateEntityAudio(i: Int, entityAudio: EntityAudio) {
    try {
        val f = entityAudio.rect.right
        val entityAudio2 = EntityAudio(null, entityAudio.uri, f, entityAudio.rect.top, entityAudio.h, f + entityAudio.rect.width().toFloat(), entityAudio.max, entityAudio.secondInScreen, (i / 1000.0f).toInt(), 0.0f, 0.0f, 0.0f)
        entityAudio2.setAmps(entityAudio.amps)
        entityAudio2.setRenderer(entityAudio.getRenderer())
        entityAudio2.addPathHttp(entityAudio.pathsHttp)
        entityAudio2.mediaPlayer = entityAudio.mediaPlayer
        entityAudio2.rect.bottom = entityAudio.rect.bottom
        entityAudio2.pathFfmpeg = entityAudio.getPathFfmpeg()
        entityAudio2.effectAudio = entityAudio.effectAudio
        entityAudio2.videoPath = entityAudio.videoPath
        entityAudio2.isApplyEffectInPreview = entityAudio.isApplyEffectInPreview
        entityAudio2.scaleFactor = entityAudio.scaleFactor
        entityAudio2.index = entityAudio.index + 1
        entityAudio2.setOffsetRight(entityAudio.getOffsetRight())
        entityAudio2.setOffsetLeft(entityAudio.getOffsetLeft())
        entityAudio2.offset = entityAudio.offset
        entityAudio2.end = Math.round((Math.abs(Math.round((entityAudio.rect.right / trackViewEntity.second_in_screen) * 1000.0f)) - Math.abs(Math.round((entityAudio.rect.left / trackViewEntity.second_in_screen) * 1000.0f))) + entityAudio.start).toFloat()
        entityAudio2.start = entityAudio.start
        entityAudio2.minDuration = entityAudio.minDuration
        trackViewEntity.addAudio(entityAudio2, entityAudio.index + 1)
        trackViewEntity.invalidate()
    } catch (e: Exception) {
        e.printStackTrace()
        hideProgressFragment()
        hideFragment()
    }
}

// =====================================================================
// changeEntityAudio(int, Uri) - simple overload
// =====================================================================

private fun changeEntityAudio(i: Int, uri: Uri) {
    try {
        val audio = trackViewEntity.getAudio()
        val scaleFactor = if (trackViewEntity.entityListAudio.isEmpty() || audio == null) 0.0f else audio.rect.right / trackViewEntity.scaleFactor
        val round = Math.round(trackViewEntity.width * 0.077f)
        val round2 = Math.round(trackViewEntity.second_in_screenNoScale * (i / 1000.0f))
        val f = round2.toFloat()
        val entityAudio = EntityAudio(null, uri, scaleFactor, 0.0f, round.toFloat(), f + scaleFactor, f, trackViewEntity.second_in_screenNoScale, i)
        entityAudio.mediaPlayer = mPlayer
        entityAudio.effectAudio.end = entityAudio.end
        entityAudio.effectAudio.start = entityAudio.start
        entityAudio.effectAudio.duration = (entityAudio.end - entityAudio.start).toInt()
        trackViewEntity.addAudio(entityAudio)
        if (round2 > 0 && round > 0) {
            val uri2 = if (!uri.toString().contains("share_with_me")) {
                AudioUtils.copyFromUri(this, uri, mTemplate!!.folder_template!!)!!
            } else {
                uri.toString()
            }
            val file = File(mTemplate!!.folder_template, System.currentTimeMillis().toString() + "_output.pcm")
            val arrayList = ArrayList<String>()
            arrayList.add("-i")
            arrayList.add(uri2)
            arrayList.add("-map")
            arrayList.add("0:a")
            arrayList.add("-ac")
            arrayList.add("1")
            arrayList.add("-ar")
            arrayList.add("44100")
            arrayList.add("-f")
            arrayList.add("s16le")
            arrayList.add(file.absolutePath)
            arrayList.add("-y")
            val str = uri2
            id_ffmpeg.add(FFmpegKit.executeWithArgumentsAsync(arrayList.toTypedArray(), object : FFmpegSessionCompleteCallback {
                override fun apply(fFmpegSession: FFmpegSession) {
                    if (fFmpegSession.returnCode.isValueSuccess()) {
                        try {
                            entityAudio.setAmps(PCMWaveformExtractor.extractWaveform(file.absolutePath, round2 / ((round * 0.1f).toInt() + (round * 0.07f).toInt())))
                            entityAudio.pathFfmpeg = str
                            runOnUiThread {
                                updateTimeToEndAya()
                                updateBtnToEnd()
                                updateBtnToStart()
                                hideProgressFragment()
                                hideFragment()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            runOnUiThread {
                                hideProgressFragment()
                                hideFragment()
                            }
                        }
                    }
                }
            }).sessionId)
            trackViewEntity.invalidate()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        hideProgressFragment()
        hideFragment()
    }
}

// =====================================================================
// createCmd() / applyffectAll() / applyffect()
// =====================================================================

private fun createCmd(effectAudio: EffectAudio, f: Float, f2: Float): String {
    val arrayList = ArrayList<String>()
    arrayList.add(String.format(Locale.US, "atrim=start=%.2f:end=%.2f", f, f2))
    arrayList.add("asetpts=N/SR/TB")
    if (effectAudio.isRemoveNoice) {
        arrayList.add("afftdn=nf=-25")
    }
    arrayList.add(String.format(Locale.US, "volume=%.2f", effectAudio.volume))
    if (effectAudio.fade_in > 0) {
        arrayList.add("afade=t=in:st=0:d=${effectAudio.fade_in}")
    }
    if (effectAudio.fade_out > 0) {
        val fade_out = effectAudio.fade_out
        arrayList.add("afade=t=out:st=${(f2 - f) - fade_out}:d=$fade_out")
    }
    if (effectAudio.isEnhance) {
        arrayList.add(Common.ENHANCE_CMD)
    }
    if (effectAudio.reverbPreset != null) {
        arrayList.add(effectAudio.reverbPreset!!)
    }
    if (effectAudio.decays > 0) {
        arrayList.add(String.format(Locale.US, "aecho=%.2f:%.2f:%s:%s", 1.0f, effectAudio.outGain, effectAudio.delays_cmd, effectAudio.decays_cmd))
    }
    if (effectAudio.speed != 1.0f) {
        arrayList.addAll(buildSpeedFilters(effectAudio.speed))
    }
    return TextUtils.join(",", arrayList)
}

fun applyffectAll(effectAudio: EffectAudio, i: Int) {
    if (i >= trackViewEntity.entityListAudio.size) {
        runOnUiThread {
            trackViewEntity.invalidate()
            hideProgressFragment()
            if (iEditMediaCallback != null) {
                iEditMediaCallback!!.onDone()
            }
        }
        return
    }
    val entityAudioNotDeleted = trackViewEntity.getEntityAudioNotDeleted(i)
    if (entityAudioNotDeleted == null) {
        runOnUiThread {
            trackViewEntity.invalidate()
            hideProgressFragment()
            if (iEditMediaCallback != null) {
                iEditMediaCallback!!.onDone()
            }
        }
        return
    }
    val entityAudio = entityAudioNotDeleted.second as EntityAudio
    val intValue = entityAudioNotDeleted.first as Int
    val createCmd = createCmd(effectAudio, entityAudio.effectAudio.start / 1000.0f, entityAudio.effectAudio.end / 1000.0f)
    val file = File(mTemplate!!.folder_template, System.currentTimeMillis().toString() + "_audio_echo.mp3")
    val fromFile = Uri.fromFile(file)
    id_ffmpeg.add(FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", entityAudio.getPathFfmpeg(), "-af", createCmd, "-y", file.absolutePath), object : FFmpegSessionCompleteCallback {
        override fun apply(fFmpegSession: FFmpegSession) {
            if (fFmpegSession.returnCode.isValueSuccess()) {
                try {
                    mPlayer = MediaPlayer()
                    mPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    if (fromFile.scheme != null && fromFile.scheme!!.startsWith("http")) {
                        mPlayer!!.setDataSource(fromFile.toString())
                    } else {
                        mPlayer!!.setDataSource(this@EngineActivity, fromFile)
                    }
                    mPlayer!!.prepareAsync()
                    mPlayer!!.setOnPreparedListener { mediaPlayer ->
                        if (entityAudio.mediaPlayer != null && mediaPlayer.duration != entityAudio.mediaPlayer!!.duration) {
                            entityAudio.right = entityAudio.rect.left + Math.round(trackViewEntity.second_in_screen * (mediaPlayer.duration / 1000.0f))
                            entityAudio.duration = mediaPlayer.duration * 1000
                            entityAudio.end = mediaPlayer.duration.toFloat()
                            entityAudio.start = 0.0f
                            entityAudio.max = (entityAudio.rect.right / entityAudio.scaleFactor) - ((entityAudio.rect.left / entityAudio.scaleFactor) - entityAudio.getOffsetLeft())
                            trackViewEntity.updateWhenEffect(entityAudio)
                        }
                        entityAudio.mediaPlayer = this@EngineActivity.mPlayer
                        applyffectAll(effectAudio, intValue + 1)
                    }
                    entityAudio.pathFfmpegEffect = file.absolutePath
                    entityAudio.isApplyEffectInPreview = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        hideProgressFragment()
                        if (iEditMediaCallback != null) {
                            iEditMediaCallback!!.onDone()
                        }
                    }
                }
            }
        }
    }).sessionId)
}

fun applyffect(str: String, entityAudio: EntityAudio) {
    showProgressSimple()
    val file = File(mTemplate!!.folder_template, System.currentTimeMillis().toString() + "_audio_echo.mp3")
    val uri = Uri.fromFile(file)
    id_ffmpeg.add(FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", entityAudio.getPathFfmpeg(), "-af", str, "-y", file.absolutePath), object : FFmpegSessionCompleteCallback {
        override fun apply(fFmpegSession: FFmpegSession) {
            if (fFmpegSession.returnCode.isValueSuccess()) {
                try {
                    mPlayer = MediaPlayer()
                    mPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    if (uri.scheme != null && uri.scheme!!.startsWith("http")) {
                        mPlayer!!.setDataSource(uri.toString())
                    } else {
                        mPlayer!!.setDataSource(this@EngineActivity, uri)
                    }
                    mPlayer!!.prepareAsync()
                    mPlayer!!.setOnPreparedListener { mediaPlayer ->
                        if (entityAudio.mediaPlayer != null && mediaPlayer.duration != entityAudio.mediaPlayer!!.duration) {
                            entityAudio.right = entityAudio.rect.left + Math.round(trackViewEntity.second_in_screen * (mediaPlayer.duration / 1000.0f))
                            entityAudio.duration = mediaPlayer.duration * 1000
                            entityAudio.max = (entityAudio.rect.right / entityAudio.scaleFactor) - ((entityAudio.rect.left / entityAudio.scaleFactor) - entityAudio.getOffsetLeft())
                            trackViewEntity.updateWhenEffect(entityAudio)
                            runOnUiThread {
                                trackViewEntity.invalidate()
                                hideProgressFragment()
                            }
                        } else {
                            runOnUiThread { hideProgressFragment() }
                        }
                        entityAudio.mediaPlayer = mediaPlayer
                    }
                    entityAudio.pathFfmpegEffect = file.absolutePath
                    entityAudio.isApplyEffectInPreview = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread { hideProgressFragment() }
                }
            }
        }
    }).sessionId)
}

// =====================================================================
    fun addTimeLineTrslQuran(translationQuranEntity: TranslationQuranEntity): EntityTrslTimeline {
        var xCursur = trackViewEntity.getXCursur()
        val trslQuran = trackViewEntity.getTrslQuran()
        if (trslQuran != null) {
            xCursur = trslQuran.rect.right
        }
        val entityTrslTimeline = EntityTrslTimeline(
            translationQuranEntity, xCursur, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            trackViewEntity.getQuran()!!.rect.right,
            trackViewEntity.getSecond_in_screen()
        )
        trackViewEntity.addTrslQuran(entityTrslTimeline)
        return entityTrslTimeline
    }

    fun addTimeLineBismilah(bismilahEntity: BismilahEntity): EntityBismilahTimeline {
        val f = trackViewEntity.getmIsi3adaTimeline()?.rect?.right ?: 0.0f
        val entityBismilahTimeline = EntityBismilahTimeline(
            bismilahEntity, f, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            f + trackViewEntity.getSecond_in_screen() * 4.0f,
            trackViewEntity.getSecond_in_screen()
        )
        trackViewEntity.bismilahTimeline = entityBismilahTimeline
        return entityBismilahTimeline
    }

    fun addTimeLineIsti3ada(bismilahEntity: BismilahEntity): EntityBismilahTimeline {
        val entityBismilahTimeline = EntityBismilahTimeline(
            bismilahEntity, 0.0f, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            trackViewEntity.getSecond_in_screen() * 4.0f + 0.0f,
            trackViewEntity.getSecond_in_screen()
        )
        trackViewEntity.setmIsi3adaTimeline(entityBismilahTimeline)
        return entityBismilahTimeline
    }

    fun addTimeLineBismilah(bismilahEntity: BismilahEntity, index: Int, quranEntity: QuranEntity, left: Float, right: Float): EntityBismilahTimeline {
        val entityBismilahTimeline = EntityBismilahTimeline(
            bismilahEntity, left, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            right,
            trackViewEntity.getSecond_in_screen()
        )
        trackViewEntity.bismilahTimeline = entityBismilahTimeline
        return entityBismilahTimeline
    }

    fun addTimeLineIsti3ada(bismilahEntity: BismilahEntity, index: Int, quranEntity: QuranEntity, left: Float, right: Float): EntityBismilahTimeline {
        val entityBismilahTimeline = EntityBismilahTimeline(
            bismilahEntity, left, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            right,
            trackViewEntity.getSecond_in_screen()
        )
        trackViewEntity.setmIsi3adaTimeline(entityBismilahTimeline)
        return entityBismilahTimeline
    }


    fun addTimeLineQuran(quranEntity: QuranEntity): EntityQuranTimeline {
        var xCursur = trackViewEntity.getXCursur()
        val quran = trackViewEntity.getQuran()
        if (quran != null) {
            xCursur = quran.rect.right
        }
        val entityQuranTimeline = EntityQuranTimeline(
            quranEntity, xCursur, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            trackViewEntity.getQuran()!!.rect.right,
            trackViewEntity.getSecond_in_screen()
        )
        trackViewEntity.addQuran(entityQuranTimeline)
        return entityQuranTimeline
    }

    fun addTimeLineQuran(index: Int, quranEntity: QuranEntity, left: Float, right: Float): EntityQuranTimeline {
        val entityQuranTimeline = EntityQuranTimeline(
            quranEntity, left, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            right,
            trackViewEntity.getSecond_in_screen()
        )
        trackViewEntity.addQuran_split(entityQuranTimeline, index)
        return entityQuranTimeline
    }

    fun addTimeLineTrslQuran(index: Int, translationQuranEntity: TranslationQuranEntity, left: Float, right: Float): EntityTrslTimeline {
        val entityTrslTimeline = EntityTrslTimeline(
            translationQuranEntity, left, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            right,
            trackViewEntity.getSecond_in_screen()
        )
        trackViewEntity.addTrslQuran(entityTrslTimeline, index)
        return entityTrslTimeline
    }

    fun splitTimeLineQuran(index: Int, quranEntity: QuranEntity, left: Float, right: Float, scaleFactor: Float): EntityQuranTimeline {
        val entityQuranTimeline = EntityQuranTimeline(
            quranEntity, left, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            right,
            trackViewEntity.getSecond_in_screen()
        )
        entityQuranTimeline!!.scaleFactor = scaleFactor
        trackViewEntity.addQuran_split(entityQuranTimeline, index)
        return entityQuranTimeline
    }

    fun splitTimeLineQuran(index: Int, translationQuranEntity: TranslationQuranEntity, left: Float, right: Float, scaleFactor: Float): EntityTrslTimeline {
        val entityTrslTimeline = EntityTrslTimeline(
            translationQuranEntity, left, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            right,
            trackViewEntity.getSecond_in_screen()
        )
        entityTrslTimeline!!.scaleFactor = scaleFactor
        trackViewEntity.addTrslQuran(entityTrslTimeline, index)
        return entityTrslTimeline
    }


    private fun enableUndoBtn() {
        try {
            val imageButton = btnUndo
            if (imageButton == null || imageButton.isEnabled) {
                return
            }
            runOnUiThread {
                btnUndo!!.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
                btnUndo!!.isEnabled = true
                btnUndo!!.isClickable = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun enableRedoBtn() {
        try {
            val imageButton = btnRedo
            if (imageButton == null || imageButton.isEnabled) {
                return
            }
            runOnUiThread {
                btnRedo!!.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
                btnRedo!!.isEnabled = true
                btnRedo!!.isClickable = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun disableRedoBtn() {
        try {
            val imageButton = btnRedo
            if (imageButton == null || !imageButton.isEnabled) {
                return
            }
            runOnUiThread {
                btnRedo!!.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
                btnRedo!!.isEnabled = false
                btnRedo!!.isClickable = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun disableUndoBtn() {
        try {
            val imageButton = btnUndo
            if (imageButton == null || !imageButton.isEnabled) {
                return
            }
            runOnUiThread {
                btnUndo!!.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
                btnUndo!!.isEnabled = false
                btnUndo!!.isClickable = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateBtnCutState() {
        try {
            checkSplitEntity()
            checkSplitTrslEntity()
            checkSplitAudio()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addEntity(
        str: String, str2: String, str3: String, str4: String,
        i: Int, i2: Int, str5: String, i3: Int, i4: Int
    ) {
        val z = mTemplate!!.ipad_type == IpadType.GRADIENT.ordinal ||
                mTemplate!!.ipad_type == IpadType.MASK_BRUSH.ordinal ||
                mTemplate!!.ipad_type == IpadType.BLACK_LAYER.ordinal
        val nameFont = if (blurredImageView.getQuranEntities().isEmpty()) {
            "arabic/Quran.ttf"
        } else {
            blurredImageView.getQuranEntities()[0].nameFont
        }
        val str6 = nameFont
        val quranEntity = QuranEntity(
            this, DrawableHelper.getIDDrawableIconByName(str5),
            str, str2, str3, str4,
            blurredImageView.getRectFAya()!!,
            UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/$str6")!!,
            Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")!!,
            i, i2,
            UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/خط فارس الكوفي.otf")!!,
            blurredImageView.clr_aya,
            blurredImageView.clr_trsl,
            str6, z
        )
        quranEntity.ipad_type = mTemplate!!.ipad_type
        quranEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        quranEntity.startWord_index = i3
        quranEntity.endWord_index = i4
        quranEntity.icon = str5
        quranEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        val addTimeLineQuran = addTimeLineQuran(quranEntity)
        addTimeLineQuran.scaleFactor = trackViewEntity.scaleFactor
        quranEntity.entityQuran = addTimeLineQuran
        addTimeLineQuran.setEntityView(quranEntity)
        blurredImageView.addEntity(quranEntity)
    }

    private fun addTranslationEntity(str: String, i: Int, z: Boolean) {
        val translationQuranEntity = TranslationQuranEntity(
            str, blurredImageView.getRectFAya()!!,
            Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")!!,
            i, InputDeviceCompat.SOURCE_ANY, "ReadexPro_Medium.ttf",
            blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height()
        )
        translationQuranEntity.ipad_type = mTemplate!!.ipad_type
        translationQuranEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        translationQuranEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        val addTimeLineTrslQuran = addTimeLineTrslQuran(translationQuranEntity)
        addTimeLineTrslQuran.scaleFactor = trackViewEntity.scaleFactor
        translationQuranEntity.entityTrslTimeline = addTimeLineTrslQuran
        addTimeLineTrslQuran.setEntityView(translationQuranEntity)
        blurredImageView.addEntity(translationQuranEntity)
    }

    private fun addEntityBissmilah(
        str: String, f: Float, f2: Float, i: Int,
        transition: Transition, f3: Float, f4: Float,
        rectF: RectF?, i2: Int
    ) {
        val loadFontFromAsset = UtilsFileLast.loadFontFromAsset(this, "fonts/خط البسملة.ttf")!!
        val rectF2 = if (rectF == null) {
            blurredImageView.getRectFAya()!!
        } else {
            RectF(
                rectF.left * blurredImageView.getmCanvas_width(),
                rectF.top * blurredImageView.getmCanvas_height(),
                rectF.right * blurredImageView.getmCanvas_width(),
                rectF.bottom * blurredImageView.getmCanvas_height()
            )
        }
        val bismilahEntity = BismilahEntity(str, rectF2, loadFontFromAsset!!, i, i2)
        bismilahEntity.setFcSize(f4)
        bismilahEntity.setFactor_scale(f3)
        bismilahEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        if (bismilahEntity.factorSize == 1.0f) {
            bismilahEntity.createStaticLayout()
        } else {
            bismilahEntity.setupScaleSave(bismilahEntity.factorSize, blurredImageView.getmCanvas_width())
        }
        bismilahEntity.initPreset(i2)
        bismilahEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        val addTimeLineBismilah = addTimeLineBismilah(bismilahEntity, f, f2)
        bismilahEntity.bismilahTimeline = addTimeLineBismilah
        addTimeLineBismilah.setTransition(transition)
        addTimeLineBismilah.setEntityView(bismilahEntity)
        blurredImageView.addBismilahEntity(bismilahEntity)
    }

    private fun addEntityIsti3ada(
        str: String, f: Float, f2: Float, i: Int,
        transition: Transition, f3: Float, f4: Float,
        rectF: RectF?, i2: Int
    ) {
        val loadFontFromAsset = UtilsFileLast.loadFontFromAsset(this, "fonts/خط الاستعاذه.ttf")!!
        val rectF2 = if (rectF == null) {
            blurredImageView.getRectFAya()!!
        } else {
            RectF(
                rectF.left * blurredImageView.getmCanvas_width(),
                rectF.top * blurredImageView.getmCanvas_height(),
                rectF.right * blurredImageView.getmCanvas_width(),
                rectF.bottom * blurredImageView.getmCanvas_height()
            )
        }
        val bismilahEntity = BismilahEntity(str, rectF2, loadFontFromAsset!!, i, i2)
        bismilahEntity.setFcSize(f4)
        bismilahEntity.setFactor_scale(f3)
        bismilahEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        if (bismilahEntity.factorSize == 1.0f) {
            bismilahEntity.createStaticLayout()
        } else {
            bismilahEntity.setupScaleSave(bismilahEntity.factorSize, blurredImageView.getmCanvas_width())
        }
        bismilahEntity.initPreset(i2)
        bismilahEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        val addTimeLineIsti3ada = addTimeLineIsti3ada(bismilahEntity, f, f2)
        bismilahEntity.bismilahTimeline = addTimeLineIsti3ada
        addTimeLineIsti3ada.setTransition(transition)
        addTimeLineIsti3ada.setEntityView(bismilahEntity)
        blurredImageView.addIsti3adhaEntity(bismilahEntity)
    }

    private fun addEntityBissmilah(): Boolean {
        if (blurredImageView.getBismilahEntity() != null) {
            if (blurredImageView.getBismilahEntity()!!.bismilahTimeline!!.visible()) {
                return false
            }
            blurredImageView.getBismilahEntity()!!.bismilahTimeline!!.visible(true)
            return false
        }
        val bismilahEntity = BismilahEntity(
            "1", blurredImageView.getRectFAya()!!,
            UtilsFileLast.loadFontFromAsset(this, "fonts/خط البسملة.ttf")!!,
            blurredImageView.clr_aya
        )
        bismilahEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        bismilahEntity.setFcSize(bismilahEntity.getPaintAya().textSize / blurredImageView.getmCanvas_width())
        bismilahEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        val addTimeLineBismilah = addTimeLineBismilah(bismilahEntity)
        addTimeLineBismilah.scaleFactor = trackViewEntity.scaleFactor
        bismilahEntity.bismilahTimeline = addTimeLineBismilah
        addTimeLineBismilah.setEntityView(bismilahEntity)
        blurredImageView.addBismilahEntity(bismilahEntity)
        if (trackViewEntity.getQuran() != null) {
            trackViewEntity.translateToRightBismilah(addTimeLineBismilah)
        }
        return true
    }

    private fun addEntityIste3adha(): Boolean {
        if (blurredImageView.getmIsti3adhaEntity() != null) {
            if (blurredImageView.getmIsti3adhaEntity()!!.bismilahTimeline!!.visible()) {
                return false
            }
            blurredImageView.getmIsti3adhaEntity()!!.bismilahTimeline!!.visible(true)
            return false
        }
        val bismilahEntity = BismilahEntity(
            "4", blurredImageView.getRectFAya()!!,
            UtilsFileLast.loadFontFromAsset(this, "fonts/خط الاستعاذه.ttf")!!,
            blurredImageView.clr_aya
        )
        bismilahEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        bismilahEntity.setFcSize(bismilahEntity.getPaintAya().textSize / blurredImageView.getmCanvas_width())
        bismilahEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        val addTimeLineIsti3ada = addTimeLineIsti3ada(bismilahEntity)
        addTimeLineIsti3ada.scaleFactor = trackViewEntity.scaleFactor
        bismilahEntity.bismilahTimeline = addTimeLineIsti3ada
        addTimeLineIsti3ada.setEntityView(bismilahEntity)
        blurredImageView.addIsti3adhaEntity(bismilahEntity)
        if (trackViewEntity.getQuran() != null) {
            trackViewEntity.translateToRightBismilah(addTimeLineIsti3ada)
        }
        return true
    }

    private fun duplicateEntity(quranEntity: QuranEntity) {
        var typefaceNumber = quranEntity.getTypefaceNumber()
        if (typefaceNumber == null) {
            typefaceNumber = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/خط فارس الكوفي.otf")!!
        }
        val typeface = typefaceNumber
        var typeface2 = quranEntity.getPaintAya().typeface!!
        if (typeface2 == null) {
            typeface2 = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/${quranEntity.nameFont!!}")!!
        }
        val typeface3 = typeface2
        var typeface4: Typeface? = quranEntity.getPaintTranslationAya()?.typeface
        if (typeface4 == null) {
            typeface4 = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")!!
        }
        val quranEntity2 = QuranEntity(
            quranEntity.txt!!, quranEntity.getComplete_aya()!!,
            quranEntity.translation!!, quranEntity.translation_complete!!,
            blurredImageView.getRectFAya()!!, typeface3, typeface4,
            quranEntity.getIndexNumber(), quranEntity.getNumber(), typeface,
            quranEntity.clrAya, quranEntity.clrTrsl, quranEntity.nameFont!!,
            quranEntity.getPaintAya().textSize,
            quranEntity.getPaintTranslationAya()?.textSize ?: 0f,
            quranEntity.getPaintAya().isUnderlineText,
            quranEntity.getVectorDrawable()!!
        )
        quranEntity2.setFcSize(quranEntity.factorSize)
        quranEntity2.factorSizeTrl = quranEntity.factorSizeTrl
        quranEntity2.setFactor_scale(quranEntity.getFactor_scale())
        quranEntity2.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        quranEntity2.ipad_type = mTemplate!!.ipad_type
        quranEntity2.startWord_index = quranEntity.startWord_index
        quranEntity2.endWord_index = quranEntity.endWord_index
        quranEntity2.icon = quranEntity.icon
        quranEntity2.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        quranEntity2.isVisible = false
        quranEntity2.setupScaleSave(quranEntity2.factorSize, blurredImageView.getmCanvas_width())
        quranEntity2.setColor(quranEntity.clrAya)
        quranEntity2.setColorTranslation(
            if (quranEntity.getPaintTranslationAya() != null) quranEntity.clrTrsl else InputDeviceCompat.SOURCE_ANY
        )
        quranEntity2.initPreset(quranEntity.getmPreset())
        val addTimeLineQuran = addTimeLineQuran(
            quranEntity.entityQuran!!.index + 1, quranEntity2,
            quranEntity.entityQuran!!.rect.right,
            quranEntity.entityQuran!!.rect.right + quranEntity.entityQuran!!.rect.width()
        )
        addTimeLineQuran.scaleFactor = quranEntity.entityQuran!!.scaleFactor
        quranEntity2.entityQuran = addTimeLineQuran
        addTimeLineQuran.setEntityView(quranEntity2)
        if (quranEntity.entityQuran!!.getTransition() != null) {
            addTimeLineQuran.setTransition(quranEntity.entityQuran!!.getTransition()!!.duplicate())
        }
        blurredImageView.addEntity(quranEntity2, quranEntity.index + 1)
        trackViewEntity.selectEntity(quranEntity2.entityQuran, false)
        iTrimLineCallback!!.onSelectEntity(quranEntity2.entityQuran!!, -1.0f)
        trackViewEntity.updateCursurToSelectEntity()
    }

    private fun duplicateEntity(translationQuranEntity: TranslationQuranEntity) {
        var typeface = translationQuranEntity.getPaintAya().typeface
        if (typeface == null) {
            typeface = UtilsFileLast.loadFontFromAsset(this, "fonts/${translationQuranEntity.nameFont}")
        }
        val translationQuranEntity2 = TranslationQuranEntity(
            translationQuranEntity.txt!!, translationQuranEntity.rect,
            typeface, translationQuranEntity.getNumber(),
            translationQuranEntity.clrAya, translationQuranEntity.nameFont!!,
            translationQuranEntity.getPaintAya().textSize
        )
        translationQuranEntity2.setFcSize(translationQuranEntity.factorSize)
        translationQuranEntity2.factorSizeTrl = translationQuranEntity.factorSizeTrl
        translationQuranEntity2.setFactor_scale(translationQuranEntity.getFactor_scale())
        translationQuranEntity2.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        translationQuranEntity2.ipad_type = mTemplate!!.ipad_type
        translationQuranEntity2.isVisible = false
        translationQuranEntity2.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        translationQuranEntity2.updatePaint(
            translationQuranEntity.getPaintAya().textSize,
            translationQuranEntity.getStaticLayout()!!.width
        )
        translationQuranEntity2.setColor(translationQuranEntity.clrAya)
        translationQuranEntity2.initPreset(translationQuranEntity.getmPreset())
        val addTimeLineTrsl = addTimeLineTrslQuran(
            translationQuranEntity.entityTrslTimeline!!.index + 1,
            translationQuranEntity2,
            translationQuranEntity.entityTrslTimeline!!.rect.right,
            translationQuranEntity.entityTrslTimeline!!.rect.right + translationQuranEntity.entityTrslTimeline!!.rect.width()
        )
        val transition = translationQuranEntity.entityTrslTimeline!!.getTransition()
        if (transition != null) {
            addTimeLineTrsl.setTransition(transition.duplicate())
            if (transition.isIn && transition.isOut) {
                addTimeLineTrsl.getTransition()!!.isIn = false
                transition.isOut = false
            } else if (transition.isIn) {
                addTimeLineTrsl.getTransition()!!.isIn = false
            } else if (transition.isOut) {
                transition.isOut = false
            }
        }
        addTimeLineTrsl.scaleFactor = translationQuranEntity.entityTrslTimeline!!.scaleFactor
        translationQuranEntity2.entityTrslTimeline = addTimeLineTrsl
        addTimeLineTrsl.setEntityView(translationQuranEntity2)
        if (translationQuranEntity.entityTrslTimeline!!.getTransition() != null) {
            addTimeLineTrsl.setTransition(translationQuranEntity.entityTrslTimeline!!.getTransition()!!.duplicate())
        }
        blurredImageView.addEntity(translationQuranEntity2, translationQuranEntity.index + 1)
        trackViewEntity.selectEntity(translationQuranEntity2.entityTrslTimeline, false)
        iTrimLineCallback!!.onSelectEntity(translationQuranEntity2.entityTrslTimeline!!, -1.0f)
        trackViewEntity.updateCursurToSelectEntity()
    }

    private fun splitEntity(translationQuranEntity: TranslationQuranEntity) {
        val abs = Math.abs(trackViewEntity.getXCursur())
        if (abs <= translationQuranEntity.entityTrslTimeline!!.rect.left ||
            abs >= translationQuranEntity.entityTrslTimeline!!.rect.right
        ) {
            return
        }
        val secondInScreen = trackViewEntity.getSecond_in_screen() * 0.2f
        if (abs <= translationQuranEntity.entityTrslTimeline!!.rect.left ||
            abs >= translationQuranEntity.entityTrslTimeline!!.rect.left + secondInScreen
        ) {
            if (abs >= translationQuranEntity.entityTrslTimeline!!.rect.right ||
                abs <= translationQuranEntity.entityTrslTimeline!!.rect.right - secondInScreen
            ) {
                var typeface = translationQuranEntity.getPaintAya().typeface
                if (typeface == null) {
                    typeface = UtilsFileLast.loadFontFromAsset(this, "fonts/${translationQuranEntity.nameFont}")
                }
                val translationQuranEntity2 = TranslationQuranEntity(
                    translationQuranEntity.txt!!, translationQuranEntity.rect,
                    typeface, translationQuranEntity.getNumber(),
                    translationQuranEntity.clrAya, translationQuranEntity.nameFont!!,
                    translationQuranEntity.getPaintAya().textSize
                )
                translationQuranEntity2.setFcSize(translationQuranEntity.factorSize)
                translationQuranEntity2.factorSizeTrl = translationQuranEntity.factorSizeTrl
                translationQuranEntity2.setFactor_scale(translationQuranEntity.getFactor_scale())
                translationQuranEntity2.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
                translationQuranEntity2.ipad_type = mTemplate!!.ipad_type
                translationQuranEntity2.setViewWeakReference(
                    WeakReference(trackViewEntity), WeakReference(blurredImageView)
                )
                translationQuranEntity2.updatePaint(
                    translationQuranEntity.getPaintAya().textSize,
                    translationQuranEntity.getStaticLayout()!!.width
                )
                translationQuranEntity2.setColor(translationQuranEntity.clrAya)
                translationQuranEntity2.initPreset(translationQuranEntity.getmPreset())
                trackViewEntity.stackSplit(translationQuranEntity.entityTrslTimeline!!)
                val splitTimeLineQuran = splitTimeLineQuran(
                    translationQuranEntity.entityTrslTimeline!!.index + 1,
                    translationQuranEntity2,
                    Math.abs(trackViewEntity.getCurrentPosition()),
                    translationQuranEntity.entityTrslTimeline!!.rect.right,
                    translationQuranEntity.entityTrslTimeline!!.scaleFactor
                )
                val transition = translationQuranEntity.entityTrslTimeline!!.getTransition()
                if (transition != null) {
                    splitTimeLineQuran.setTransition(transition.duplicate())
                    if (transition.isIn && transition.isOut) {
                        splitTimeLineQuran.getTransition()!!.isIn = false
                        transition.isOut = false
                    } else if (transition.isIn) {
                        splitTimeLineQuran.getTransition()!!.isIn = false
                    } else if (transition.isOut) {
                        transition.isOut = false
                    }
                }
                translationQuranEntity.entityTrslTimeline!!.setCurrentRect()
                translationQuranEntity.entityTrslTimeline!!.right = Math.abs(trackViewEntity.getCurrentPosition())
                translationQuranEntity.entityTrslTimeline!!.onChange()
                translationQuranEntity2.entityTrslTimeline = splitTimeLineQuran
                splitTimeLineQuran.setEntityView(translationQuranEntity2)
                if (translationQuranEntity.entityTrslTimeline!!.getTransition() != null) {
                    splitTimeLineQuran.setTransition(
                        translationQuranEntity.entityTrslTimeline!!.getTransition()!!.duplicate()
                    )
                }
                blurredImageView.addEntity(translationQuranEntity2, translationQuranEntity.index + 1)
                trackViewEntity.invalidate()
            }
        }
    }

    private fun splitEntity(quranEntity: QuranEntity) {
        val abs = Math.abs(trackViewEntity.getXCursur())
        if (abs <= quranEntity.entityQuran!!.rect.left ||
            abs >= quranEntity.entityQuran!!.rect.right
        ) {
            return
        }
        val secondInScreen = trackViewEntity.getSecond_in_screen() * 0.2f
        if (abs <= quranEntity.entityQuran!!.rect.left ||
            abs >= quranEntity.entityQuran!!.rect.left + secondInScreen
        ) {
            if (abs >= quranEntity.entityQuran!!.rect.right ||
                abs <= quranEntity.entityQuran!!.rect.right - secondInScreen
            ) {
                var typefaceNumber = quranEntity.getTypefaceNumber()
                if (typefaceNumber == null) {
                    typefaceNumber = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/خط فارس الكوفي.otf")!!
                }
                val typeface = typefaceNumber
                var typeface2 = quranEntity.getPaintAya().typeface!!
                if (typeface2 == null) {
                    typeface2 = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/${quranEntity.nameFont!!}")!!
                }
                val typeface3 = typeface2
                var typeface4: Typeface? = quranEntity.getPaintTranslationAya()?.typeface
                if (typeface4 == null) {
                    typeface4 = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")!!
                }
                val quranEntity2 = QuranEntity(
                    quranEntity.txt!!, quranEntity.getComplete_aya()!!,
                    quranEntity.translation!!, quranEntity.translation_complete!!,
                    blurredImageView.getRectFAya()!!, typeface3, typeface4,
                    quranEntity.getIndexNumber(), quranEntity.getNumber(), typeface,
                    quranEntity.clrAya, quranEntity.clrTrsl, quranEntity.nameFont!!,
                    quranEntity.getPaintAya().textSize,
                    quranEntity.getPaintTranslationAya()?.textSize ?: 0f,
                    quranEntity.getPaintAya().isUnderlineText,
                    quranEntity.getVectorDrawable()!!
                )
                quranEntity2.setFcSize(quranEntity.factorSize)
                quranEntity2.factorSizeTrl = quranEntity.factorSizeTrl
                quranEntity2.setFactor_scale(quranEntity.getFactor_scale())
                quranEntity2.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
                quranEntity2.ipad_type = mTemplate!!.ipad_type
                quranEntity2.startWord_index = quranEntity.startWord_index
                quranEntity2.endWord_index = quranEntity.endWord_index
                quranEntity2.icon = quranEntity.icon
                quranEntity2.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
                quranEntity2.setupScaleSave(quranEntity2.factorSize, blurredImageView.getmCanvas_width())
                quranEntity2.setColor(quranEntity.clrAya)
                quranEntity2.setColorTranslation(
                    if (quranEntity.getPaintTranslationAya() != null) quranEntity.clrTrsl else InputDeviceCompat.SOURCE_ANY
                )
                quranEntity2.initPreset(quranEntity.getmPreset())
                trackViewEntity.stackSplit(quranEntity.entityQuran!!)
                val splitTimeLineQuran = splitTimeLineQuran(
                    quranEntity.entityQuran!!.index + 1, quranEntity2,
                    Math.abs(trackViewEntity.getCurrentPosition()),
                    quranEntity.entityQuran!!.rect.right,
                    quranEntity.entityQuran!!.scaleFactor
                )
                val transition = quranEntity.entityQuran!!.getTransition()
                if (transition != null) {
                    splitTimeLineQuran.setTransition(transition.duplicate())
                    if (transition.isIn && transition.isOut) {
                        splitTimeLineQuran.getTransition()!!.isIn = false
                        transition.isOut = false
                    } else if (transition.isIn) {
                        splitTimeLineQuran.getTransition()!!.isIn = false
                    } else if (transition.isOut) {
                        transition.isOut = false
                    }
                }
                quranEntity.entityQuran!!.setCurrentRect()
                quranEntity.entityQuran!!.right = Math.abs(trackViewEntity.getCurrentPosition())
                quranEntity.entityQuran!!.onChange()
                quranEntity2.entityQuran = splitTimeLineQuran
                splitTimeLineQuran.setEntityView(quranEntity2)
                if (quranEntity.entityQuran!!.getTransition() != null) {
                    splitTimeLineQuran.setTransition(quranEntity.entityQuran!!.getTransition()!!.duplicate())
                }
                blurredImageView.addEntity(quranEntity2, quranEntity.index + 1)
                trackViewEntity.invalidate()
            }
        }
    }

    private fun addEntity(
        str: String, str2: String, str3: String, str4: String,
        f: Float, f2: Float, i: Int, i2: Int, i3: Int,
        str5: String, transition: Transition, z: Boolean,
        str6: String?, i4: Int, i5: Int, f3: Float, f4: Float,
        f5: Float, rectF: RectF?, typeface: Typeface,
        typeface2: Typeface, i6: Int, i7: Int
    ) {
        val str7 = str6 ?: "hafes"
        val loadFontFromAsset = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/$str5")
        val rectF2 = if (rectF == null) {
            blurredImageView.getRectFAya()!!
        } else {
            RectF(
                rectF.left * blurredImageView.getmCanvas_width(),
                rectF.top * blurredImageView.getmCanvas_height(),
                rectF.right * blurredImageView.getmCanvas_width(),
                rectF.bottom * blurredImageView.getmCanvas_height()
            )
        }
        val quranEntity = QuranEntity(
            this, str, str2, str3, str4, rectF2, loadFontFromAsset!!,
            typeface2, i, i2, typeface, i3, i6, str5, z,
            DrawableHelper.getIDDrawableIconByName(str7)
        )
        quranEntity.setFcSize(f4)
        quranEntity.factorSizeTrl = f5
        quranEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        quranEntity.setFactor_scale(f3)
        quranEntity.ipad_type = mTemplate!!.ipad_type
        quranEntity.startWord_index = i4
        quranEntity.endWord_index = i5
        quranEntity.icon = str7
        quranEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        if (quranEntity.factorSize == 1.0f) {
            quranEntity.setTextSize(quranEntity.calculateTextSize())
        } else {
            quranEntity.setupScaleSave(quranEntity.factorSize, blurredImageView.getmCanvas_width())
        }
        quranEntity.initPreset(i7)
        val addTimeLineQuran = addTimeLineQuran(quranEntity, f, f2)
        quranEntity.entityQuran = addTimeLineQuran
        addTimeLineQuran.setTransition(transition)
        addTimeLineQuran.setEntityView(quranEntity)
        blurredImageView.addEntity(quranEntity)
    }

    private fun addEntityTrsl(
        str: String, f: Float, f2: Float, i: Int, i2: Int,
        str2: String, transition: Transition, f3: Float, f4: Float,
        rectF: RectF?, i3: Int, i4: Int, z: Boolean
    ) {
        val loadFontFromAsset = UtilsFileLast.loadFontFromAsset(this, "fonts/$str2")
        val rectF2 = if (rectF == null) {
            blurredImageView.getRectFAya()!!
        } else {
            RectF(
                rectF.left * blurredImageView.getmCanvas_width(),
                rectF.top * blurredImageView.getmCanvas_height(),
                rectF.right * blurredImageView.getmCanvas_width(),
                rectF.bottom * blurredImageView.getmCanvas_height()
            )
        }
        val translationQuranEntity = TranslationQuranEntity(
            blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height(),
            str, rectF2, loadFontFromAsset!!, i, i2, str2
        )
        translationQuranEntity.setHaveBg(z)
        translationQuranEntity.clrBg = i4
        translationQuranEntity.setFcSize(f4)
        translationQuranEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        translationQuranEntity.setFactor_scale(f3)
        translationQuranEntity.ipad_type = mTemplate!!.ipad_type
        translationQuranEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        if (translationQuranEntity.factorSize == 1.0f) {
            translationQuranEntity.setTextSize(translationQuranEntity.calculateTextSize())
        } else {
            translationQuranEntity.setupScaleSave(translationQuranEntity.factorSize, blurredImageView.getmCanvas_width())
        }
        translationQuranEntity.initPreset(i3)
        val addTimeLineQuran = addTimeLineQuran(translationQuranEntity, f, f2)
        translationQuranEntity.entityTrslTimeline = addTimeLineQuran
        addTimeLineQuran.setTransition(transition)
        addTimeLineQuran.setEntityView(translationQuranEntity)
        blurredImageView.addEntity(translationQuranEntity)
    }

    fun addAudioReciters(list: List<RecitersModel>) {
        val newSingleThreadExecutor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        newSingleThreadExecutor.execute {
            addAudioRecitersBackground(list, handler)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun addAudioRecitersBackground(list: List<RecitersModel>, handler: Handler) {
        val arrayList = ArrayList<String>()
        val arrayList2 = ArrayList<String>()
        val sb = StringBuilder()
        try {
            val it = list.iterator()
            var i = 0
            while (it.hasNext()) {
                val recitersModel = it.next()
                try {
                    val str = if (recitersModel.isTarteel) {
                        "https://audio-cdn.tarteel.ai/quran/${recitersModel.identifer}/${recitersModel.surah_index}${recitersModel.number_aya}.mp3"
                    } else {
                        "https://everyayah.com/data/${recitersModel.identifer}/${recitersModel.surah_index}${recitersModel.number_aya}.mp3"
                    }
                    val downloadFile = AudioUtils.downloadFile(this, str, mTemplate!!.folder_template!!)!!
                    if (downloadFile != null) {
                        arrayList.add(downloadFile)
                        arrayList2.add(str)
                        sb.append("file '").append(downloadFile.replace("'", "\\'")).append("'\n")
                        i++
                        try {
                            handler.post {
                                updateProgress(i, list.size)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val file = File(mTemplate!!.folder_template, "concat_${System.currentTimeMillis()}.txt")
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(sb.toString().toByteArray())
            fileOutputStream.close()
            val file2 = File(mTemplate!!.folder_template, "${System.currentTimeMillis()}_output.mp3")
            val file3 = File(mTemplate!!.folder_template, "${System.currentTimeMillis()}_output.pcm")
            val arrayList3 = ArrayList<String>()
            arrayList3.add("-f")
            arrayList3.add("concat")
            arrayList3.add("-safe")
            arrayList3.add("0")
            arrayList3.add("-i")
            arrayList3.add(file.absolutePath)
            arrayList3.add("-map")
            arrayList3.add("0:a")
            arrayList3.add("-c")
            arrayList3.add("copy")
            arrayList3.add(file2.absolutePath)
            arrayList3.add("-map")
            arrayList3.add("0:a")
            arrayList3.add("-ac")
            arrayList3.add("1")
            arrayList3.add("-ar")
            arrayList3.add("44100")
            arrayList3.add("-f")
            arrayList3.add("s16le")
            arrayList3.add(file3.absolutePath)
            arrayList3.add("-y")
            val strArr = arrayList3.toTypedArray()
            handler.post {
                addAudioRecitersFfmpeg(strArr, file2, arrayList2, file3)
            }
        } catch (e3: Exception) {
            e3.printStackTrace()
            handler.post {
                hideProgressFragment()
                hideFragment()
            }
        }
    }

    private fun addAudioRecitersFfmpeg(
        strArr: Array<String>, file: File, list: List<String>, file2: File
    ) {
        id_ffmpeg.add(
            FFmpegKit.executeWithArgumentsAsync(strArr) { fFmpegSession ->
                if (fFmpegSession.returnCode.isValueSuccess()) {
                    addAudio(Uri.fromFile(file), list, -1, file2.absolutePath)
                } else {
                    Log.e("FFMPEG", "Failed: ${fFmpegSession.failStackTrace}")
                }
            }.sessionId
        )
    }

    fun addAudioRecitersTemplate(list: List<String>, index: Int, pathVideo: String) {
        Executors.newSingleThreadExecutor().execute(
            addAudioRecitersTemplateRunnable(list, index, pathVideo)
        )
    }

    private fun addAudioRecitersTemplateRunnable(
        pathes: List<String>, valIndex: Int, valPathVideo: String
    ): Runnable = Runnable {
        try {
            val arrayList2 = ArrayList<String>()
            val sb = StringBuilder()
            val it = pathes.iterator()
            var i = 0
            while (it.hasNext()) {
                val parse = Uri.parse(it.next())
                val uri = parse.toString()
                var downloadFile: String?
                if (!uri.startsWith("http://") && !uri.startsWith("https://")) {
                    downloadFile = AudioUtils.copyFromUri(this@EngineActivity, parse, mTemplate!!.folder_template!!)
                    if (downloadFile == null) {
                        sb.append("file '").append(downloadFile!!.replace("'", "\\'")).append("'\n")
                        i++
                        updateProgress(i, pathes.size)
                    }
                }
                downloadFile = AudioUtils.downloadFile(this@EngineActivity, uri, mTemplate!!.folder_template!!)!!
                if (downloadFile == null) {
                    sb.append("file '").append(downloadFile!!.replace("'", "\\'")).append("'\n")
                    i++
                    updateProgress(i, pathes.size)
                }
            }
            val file = File(mTemplate!!.folder_template, "concat.txt")
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(sb.toString().toByteArray())
            fileOutputStream.close()
            val file2 = File(mTemplate!!.folder_template, "${System.currentTimeMillis()}_output.mp3")
            val file3 = File(mTemplate!!.folder_template, "${System.currentTimeMillis()}_output.pcm")
            val arrayList = ArrayList<String>()
            arrayList.add("-f")
            arrayList.add("concat")
            arrayList.add("-safe")
            arrayList.add("0")
            arrayList.add("-i")
            arrayList.add(file.absolutePath)
            arrayList.add("-map")
            arrayList.add("0:a")
            arrayList.add("-c")
            arrayList.add("copy")
            arrayList.add(file2.absolutePath)
            arrayList.add("-map")
            arrayList.add("0:a")
            arrayList.add("-ac")
            arrayList.add("1")
            arrayList.add("-ar")
            arrayList.add("44100")
            arrayList.add("-f")
            arrayList.add("s16le")
            arrayList.add(file3.absolutePath)
            arrayList.add("-y")
            id_ffmpeg.add(
                FFmpegKit.executeWithArgumentsAsync(arrayList.toTypedArray()) { fFmpegSession ->
                    if (fFmpegSession.returnCode.isValueSuccess()) {
                        if (valIndex >= 0 && valIndex < mTemplate!!.entityMediaList.size) {
                            val entityMedia = mTemplate!!.entityMediaList[valIndex]
                            if (entityMedia.isApplyEffectInPreview) {
                                val file4 = File(
                                    mTemplate!!.folder_template,
                                    "${System.currentTimeMillis()}_audio_echo.mp3"
                                )
                                val effectAudio = entityMedia.effectAudio
                                val start = effectAudio!!.start / 1000.0f
                                val end = effectAudio!!.end / 1000.0f
                                val arrayList2Inner = ArrayList<String>()
                                arrayList2Inner.add("atrim=start=$start:end=$end")
                                arrayList2Inner.add("asetpts=N/SR/TB")
                                if (effectAudio!!.isRemoveNoice) {
                                    arrayList2Inner.add("afftdn=nf=-25")
                                }
                                arrayList2Inner.add(
                                    String.format(Locale.US, "volume=%.2f", effectAudio!!.volume)
                                )
                                if (effectAudio!!.fade_in > 0) {
                                    arrayList2Inner.add("afade=t=in:st=0:d=${effectAudio!!.fade_in}")
                                }
                                if (effectAudio!!.fade_out > 0) {
                                    val fadeOut = effectAudio!!.fade_out
                                    arrayList2Inner.add(
                                        "afade=t=out:st=${(end - start) - fadeOut}:d=$fadeOut"
                                    )
                                }
                                if (effectAudio!!.isEnhance) {
                                    arrayList2Inner.add(Common.ENHANCE_CMD)
                                }
                                if (effectAudio!!.reverbPreset != null) {
                                    arrayList2Inner.add(effectAudio!!.reverbPreset!!)
                                }
                                if (effectAudio!!.decays > 0) {
                                    arrayList2Inner.add(
                                        String.format(
                                            Locale.US, "aecho=%.2f:%.2f:%s:%s",
                                            1.0f, effectAudio!!.outGain,
                                            effectAudio!!.delays_cmd, effectAudio!!.decays_cmd
                                        )
                                    )
                                }
                                if (effectAudio!!.speed != 1.0f) {
                                    arrayList2Inner.addAll(buildSpeedFilters(effectAudio!!.speed))
                                }
                                id_ffmpeg.add(
                                    FFmpegKit.executeWithArgumentsAsync(
                                        arrayOf(
                                            "-i", file2.absolutePath,
                                            "-af", TextUtils.join(",", arrayList2Inner),
                                            "-y", file4.absolutePath
                                        )
                                    ) { _ ->
                                        addAudioTemplate(
                                            Uri.fromFile(file4), pathes, valIndex,
                                            file2.absolutePath, file3.absolutePath, valPathVideo
                                        )
                                    }.sessionId
                                )
                                return@executeWithArgumentsAsync
                            }
                        }
                        addAudioTemplate(
                            Uri.fromFile(file2), pathes, valIndex,
                            file2.absolutePath, file3.absolutePath, valPathVideo
                        )
                    }
                }.sessionId
            )
        } catch (e: Exception) {
            hideProgressFragment()
            hideFragment()
            e.printStackTrace()
        }
    }

    fun dialogCopyRight() {
        try {
            val dialog = Dialog(this)
            this.dialog = dialog
            dialog.setCancelable(false)
            this.dialog!!.requestWindowFeature(1)
            this.dialog!!.window!!.setLayout(-1, -2)
            this.dialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog_copyright, null)
            this.dialog!!.setContentView(inflate)
            val textCustumFontBold = inflate.findViewById<TextCustumFontBold>(R.id.dialog_title)
            val textCustumFont = inflate.findViewById<TextCustumFont>(R.id.tv_msj)
            inflate.findViewById<View>(R.id.dialog_no).setOnClickListener {
                cancelDialog()
            }
            if (LocaleHelper.getLanguage(this) == "ar") {
                textCustumFontBold.text = "تنبيه حقوق الاستخدام ⚠️"
                textCustumFont.text = "بعض تسجيلات تلاوات القرّاء محمية بحقوق النشر، وهي مخصّصة للاستخدام الشخصي فقط.\n\nقد تسمح بعض المنصات باستخدام هذه الأصوات دون مشاكل، لكن ذلك لا يُعدّ تصريحًا بالنشر أو الاستخدام التجاري.\n\nللنشر الآمن، يُرجى اختيار قارئ مذكور على أنه مسموح بالنشر أو استخدام صوتك الخاص.\n\nالمستخدم مسؤول بالكامل عن الالتزام بسياسات حقوق النشر الخاصة بكل منصة."
            } else {
                textCustumFontBold.text = "⚠️ Copyright Notice"
                textCustumFont.text = "Some reciters' audio recordings are protected by copyright and are intended for personal use only.\n\nCertain platforms may allow these sounds without issues, but this does not constitute permission to publish or use them commercially.\n\nFor safe publishing, please select a reciter marked as allowed for publishing or use your own audio.\n\nThe user is solely responsible for complying with the copyright policies of each platform."
            }
            this.dialog!!.show()
            MyPreferences.putVuCopyRight(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateEndViewTime(i: Int) {
        val j = i.toLong()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(j) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(j))
        val str = if (seconds < 10) {
            "${TimeUnit.MILLISECONDS.toMinutes(j)}:0$seconds"
        } else {
            "${TimeUnit.MILLISECONDS.toMinutes(j)}:$seconds"
        }
        tv_endTime!!.text = "/$str"
    }

    fun updateStartViewTime(i: Int) {
        val j = i.toLong()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(j) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(j))
        val str = if (seconds < 10) {
            "${TimeUnit.MILLISECONDS.toMinutes(j)}:0$seconds"
        } else {
            "${TimeUnit.MILLISECONDS.toMinutes(j)}:$seconds"
        }
        tv_currentTime!!.text = str
    }

    fun updateViewTime(i: Int, i2: Int) {
        val j = i2.toLong()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(j) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(j))
        val str = if (seconds < 10) {
            "${TimeUnit.MILLISECONDS.toMinutes(j)}:0$seconds"
        } else {
            "${TimeUnit.MILLISECONDS.toMinutes(j)}:$seconds"
        }
        val j2 = i.toLong()
        val seconds2 = TimeUnit.MILLISECONDS.toSeconds(j2) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(j2))
        val str2 = if (seconds2 < 10) {
            "${TimeUnit.MILLISECONDS.toMinutes(j2)}:0$seconds2"
        } else {
            "${TimeUnit.MILLISECONDS.toMinutes(j2)}:$seconds2"
        }
        tv_currentTime!!.text = str
        tv_endTime!!.text = "/$str2"
    }

    private fun hideLayoutResolution() {
        val linearLayout = layout_resolution
        if (linearLayout == null || linearLayout.visibility != 0) {
            return
        }
        layout_resolution!!.post {
            layout_resolution!!.visibility = 8
        }
    }

    private fun hideFragment() {
        try {
            if (!isFinishing && !supportFragmentManager.isDestroyed) {
                val supportFragmentManager = supportFragmentManager
                val beginTransaction = supportFragmentManager.beginTransaction()
                val findFragmentById = supportFragmentManager.findFragmentById(R.id.m_container)
                if (findFragmentById != null) {
                    beginTransaction.remove(findFragmentById)
                }
                beginTransaction.commit()
                setupHideFragment()
            }
        } catch (_: Exception) {
        }
        mCurrentFragment = null
    }

    private fun showProgress() {
        try {
            setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
            setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)
            findViewById<View>(R.id.container_progress).visibility = 0
            if (isFinishing || supportFragmentManager.isDestroyed) {
                return
            }
            val beginTransaction = supportFragmentManager.beginTransaction()
            beginTransaction.replace(R.id.container_progress, ProgressViewFragment.getInstance())
            beginTransaction.commit()
        } catch (_: Exception) {
        }
    }

    private fun showProgressSimple() {
        try {
            findViewById<View>(R.id.container_progress).visibility = 0
            if (isFinishing || supportFragmentManager.isDestroyed) {
                return
            }
            val beginTransaction = supportFragmentManager.beginTransaction()
            beginTransaction.replace(R.id.container_progress, SimpleProgressViewFragment.getInstance())
            beginTransaction.commit()
        } catch (_: Exception) {
        }
    }

    private fun hideProgressFragment() {
        try {
            setStatusBarColor(-15658735)
            setNavigationBarColor(-14803426)
            if (!isFinishing && !supportFragmentManager.isDestroyed) {
                val supportFragmentManager = supportFragmentManager
                val beginTransaction = supportFragmentManager.beginTransaction()
                val findFragmentById = supportFragmentManager.findFragmentById(R.id.container_progress)
                if (findFragmentById != null) {
                    beginTransaction.remove(findFragmentById)
                }
                beginTransaction.commit()
            }
            findViewById<View>(R.id.container_progress).visibility = 8
        } catch (_: Exception) {
        }
    }

    private fun toCrop() {
        isSaveTmpTemplate = false
        isToCrop = true
        Common.bitmap = blurredImageView.bitmapOriginal
        Common.rect = blurredImageView.rectSquare!!
        if (blurredImageView.bitmapSquare != null) {
            Common.minSquareW = blurredImageView.bitmapSquare!!.width
            Common.minSquareH = blurredImageView.bitmapSquare!!.height
        }
        Common.radius = blurredImageView.getRadius_square()
        launchCropActivity!!.launch(Intent(this, CropBitmapActivity::class.java))
    }

    fun dialogWatermark() {
        try {
            if (dialog != null) {
                cancelDialog()
            }
            isSaveTmpTemplate = false
            isToCrop = true
            val dialog = Dialog(this)
            this.dialog = dialog
            dialog.setCancelable(true)
            this.dialog!!.requestWindowFeature(1)
            this.dialog!!.window!!.setLayout(-1, -2)
            this.dialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null)
            this.dialog!!.setContentView(inflate)
            inflate.findViewById<View>(R.id.dialog_title).visibility = 8
            inflate.findViewById<View>(R.id.img_pro).visibility = 0
            val textCustumFont = inflate.findViewById<TextCustumFont>(R.id.dialog_message)
            textCustumFont.text = mResources!!.getString(R.string.do_want_delete_watermark)
            textCustumFont.gravity = 17
            val buttonCustumFont = inflate.findViewById<ButtonCustumFont>(R.id.dialog_no)
            buttonCustumFont.text = mResources!!.getString(R.string.no)
            buttonCustumFont.setOnClickListener {
                cancelDialog()
            }
            val buttonCustumFont2 = inflate.findViewById<ButtonCustumFont>(R.id.dialog_yes)
            buttonCustumFont2.text = mResources!!.getString(R.string.yes)
            buttonCustumFont2.setOnClickListener {
                toProVersion()
                cancelDialog()
            }
            this.dialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dialogPremium(i: Int) {
        try {
            if (dialog != null) {
                cancelDialog()
            }
            isSaveTmpTemplate = false
            val dialog = Dialog(this)
            this.dialog = dialog
            dialog.setCancelable(true)
            this.dialog!!.requestWindowFeature(1)
            this.dialog!!.window!!.setLayout(-1, -2)
            this.dialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null)
            this.dialog!!.setContentView(inflate)
            inflate.findViewById<View>(R.id.dialog_title).visibility = 8
            inflate.findViewById<View>(R.id.img_pro).visibility = 0
            val textCustumFont = inflate.findViewById<TextCustumFont>(R.id.dialog_message)
            textCustumFont.text = mResources!!.getString(R.string.unlock_premium)
            textCustumFont.gravity = 17
            val buttonCustumFont = inflate.findViewById<ButtonCustumFont>(R.id.dialog_no)
            buttonCustumFont.text = mResources!!.getString(R.string.no)
            buttonCustumFont.setOnClickListener {
                cancelDialog()
            }
            val buttonCustumFont2 = inflate.findViewById<ButtonCustumFont>(R.id.dialog_yes)
            buttonCustumFont2.text = mResources!!.getString(R.string.yes)
            buttonCustumFont2.setOnClickListener {
                toProVersion()
                cancelDialog()
            }
            this.dialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

        fun updateHitRatio(i: Int, str: String) {
        if (i == ResizeType.SOCIAL_STORY.ordinal) {
            textChangeResize!!.text = "9:16"
        } else if (i == ResizeType.SQUARE.ordinal) {
            textChangeResize!!.text = "1:1"
        } else {
            textChangeResize!!.text = "16:9"
        }
        ivResize!!.setImageResource(DrawableHelper.getIdResource(str))
    }

        fun dialogPremiumIpad() {
        isSaveTmpTemplate = false
        try {
            val dialog = Dialog(this)
            this.dialog = dialog
            dialog.setCancelable(true)
            this.dialog!!.requestWindowFeature(1)
            this.dialog!!.window!!.setLayout(-1, -2)
            this.dialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog_premuim, null)
            this.dialog!!.setContentView(inflate)
            inflate.findViewById<View>(R.id.dialog_title).visibility = 8
            val textCustumFont = inflate.findViewById<TextCustumFont>(R.id.dialog_message)
            val textCustumFont2 = inflate.findViewById<TextCustumFont>(R.id.tv_subscribe)
            inflate.findViewById<View>(R.id.dialog_no).setOnClickListener {
                cancelDialog()
            }
            val relativeLayout = inflate.findViewById<RelativeLayout>(R.id.dialog_yes)
            relativeLayout.setBackgroundResource(R.drawable.btn_dialog_premium_state)
            relativeLayout.setOnClickListener {
                toProVersion()
                cancelDialog()
            }
            if (LocaleHelper.getLanguage(this) == "ar") {
                textCustumFont.text = "🎁 هذه الميزة فقط للمشتركين في التطبيق."
                textCustumFont2.text = "النسخة المدفوعة"
            } else {
                textCustumFont.text = "🎁 This feature is only for app subscribers."
                textCustumFont2.text = "Upgrade premium"
            }
            this.dialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pickVideoForAudio() {
        if (Build.VERSION.SDK_INT >= 34) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") != 0 &&
                ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") != 0
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf("android.permission.READ_MEDIA_VIDEO", "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"),
                    12
                )
                return
            }
        } else if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") != 0) {
                ActivityCompat.requestPermissions(
                    this, arrayOf("android.permission.READ_MEDIA_VIDEO"), 12
                )
                return
            }
        } else if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(
                this, arrayOf("android.permission.READ_EXTERNAL_STORAGE"), 12
            )
            return
        }
        videoChooserForAudio()
    }

    fun pickVideoFromGallery() {
        if (Build.VERSION.SDK_INT >= 34) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") != 0 &&
                ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") != 0
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        "android.permission.READ_MEDIA_IMAGES",
                        "android.permission.READ_MEDIA_VIDEO",
                        "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
                    ),
                    11
                )
                return
            }
        } else if (Build.VERSION.SDK_INT == 33) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_IMAGES") != 0 ||
                ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") != 0
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf("android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO"),
                    11
                )
                return
            }
        } else if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(
                this, arrayOf("android.permission.READ_EXTERNAL_STORAGE"), 11
            )
            return
        }
        videoChooser()
    }

    fun pickImageFromGallery() {
        if (Build.VERSION.SDK_INT >= 34) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") != 0 ||
                ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_IMAGES") != 0
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        "android.permission.READ_MEDIA_IMAGES",
                        "android.permission.READ_MEDIA_VIDEO",
                        "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
                    ),
                    10
                )
                return
            }
        } else if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_IMAGES") != 0 ||
                ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") != 0
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf("android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO"),
                    10
                )
                return
            }
        } else if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(
                this, arrayOf("android.permission.READ_EXTERNAL_STORAGE"), 10
            )
            return
        }
        imageChooser()
    }

    private fun videoChooserForAudio() {
        isToCrop = true
        launchVideoExtract!!.launch(Intent(this, GalleryPickerVideo::class.java))
    }

    private fun videoChooser() {
        launchVideo!!.launch(Intent(this, GalleryPickerVideo::class.java))
    }

    private fun imageChooser() {
        launchImg!!.launch(Intent(this, GalleryPickerOneImage::class.java))
    }

    @Suppress("unused")
    fun onCropActivityResult(activityResult: ActivityResult) {
        var cropTo16x9: Bitmap?
        if (activityResult.resultCode != -1 || activityResult.data == null ||
            Common.bitmap == null || Common.bitmap!!.isRecycled
        ) {
            return
        }
        Common.bitmap = Bitmap.createScaledBitmap(
            Common.bitmap!!, blurredImageView.getH(), blurredImageView.getH(), false
        )
        blurredImageView.bitmapOriginal = Common.bitmap
        cropTo16x9 = when (mTemplate!!.geTypeResize()) {
            ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(
                blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH()
            )
            ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(
                blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH()
            )
            else -> BitmapCropper.cropTo16x9(
                blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH()
            )
        }
        blurredImageView.bitmapBlured = UtilsBitmap.blur(this, cropTo16x9!!, 20, 1)!!
        blurredImageView.invalidate()
    }

    @Suppress("unused")
    fun onCropDataActivityResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == -1) {
            val data = activityResult.data ?: return
            mTemplate!!.x_square = data.getFloatExtra("x", 0.3f)
            mTemplate!!.y_square = data.getFloatExtra("y", 0.4f)
            mTemplate!!.width_square = data.getFloatExtra("w", 1.0f)
            mTemplate!!.height_square = data.getFloatExtra("h", 0.5f)
            blurredImageView.bitmapSquare = Common.bitmap
            blurredImageView.rectSquare = Common.rect
            blurredImageView.invalidate()
        }
        isToCrop = false
    }

    @Suppress("unused")
    fun onImgActivityResult(activityResult: ActivityResult) {
        val data: Intent?
        if (activityResult.resultCode != -1) return
        data = activityResult.data ?: return
        if (data.data == null) return
        handleImg(data.data!!)
    }

    @Suppress("unused")
    fun onVideoActivityResult(activityResult: ActivityResult) {
        val data: Intent?
        if (activityResult.resultCode != -1) return
        data = activityResult.data ?: return
        if (data.data == null) return
        val data2 = data.data!!
        try {
            contentResolver.takePersistableUriPermission(data2, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        handleVideo(data2)
    }

    @Suppress("unused")
    fun onVideoExtractActivityResult(activityResult: ActivityResult) {
        val data: Intent?
        isToCrop = false
        if (activityResult.resultCode != -1) return
        data = activityResult.data ?: return
        if (data.data == null) return
        try {
            val data2 = data.data!!
            try {
                contentResolver.takePersistableUriPermission(data2, 1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            runOnUiThread {
                showProgress()
            }
            mTemplate!!.uri_upload_extract_audio_video = data2.toString()
            val copyFromUri = AudioUtils.copyFromUri(this, data2, mTemplate!!.folder_template!!)!!
            start_extenstion = 0
            extractAudioFromVideoRecursive(copyFromUri, 0, false, 0)
        } catch (e2: Exception) {
            e2.printStackTrace()
        }
    }

    private fun addAudioFromVideoWithExtention(str: String, str2: String, i: Int) {
        try {
            val file = File(File(mTemplate!!.folder_template!!), "${System.currentTimeMillis()}_audio$str")
            FFmpegKit.executeWithArgumentsAsync(
                arrayOf("-i", str2, "-vn", "-acodec", "copy", "-y", file.absolutePath)
            ) { fFmpegSession ->
                if (fFmpegSession.returnCode.isValueSuccess()) {
                    addAudioTemplateHttp(Uri.fromFile(file), i, str2)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun extractAudioFromVideoRecursive(str: String, i: Int, z: Boolean, i2: Int) {
        if (isDestroyed) {
            return
        }
        if (i < extentions.size) {
            try {
                val file = File(File(mTemplate!!.folder_template!!), "${System.currentTimeMillis()}_audio${extentions[i]}")
                FFmpegKit.executeWithArgumentsAsync(
                    arrayOf("-i", str, "-vn", "-acodec", "copy", "-y", file.absolutePath)
                ) { fFmpegSession ->
                    if (fFmpegSession.returnCode.isValueSuccess()) {
                        mTemplate!!.extension = extentions[i]
                        val fromFile = Uri.fromFile(file)
                        if (!z) {
                            runOnUiThread {
                                hideFragment()
                                hideProgressFragment()
                            }
                            addUriAudioToQuranFragment(fromFile, str, 0)
                        } else {
                            addAudioTemplateHttp(fromFile, i2, str)
                        }
                        return@executeWithArgumentsAsync
                    }
                    start_extenstion++
                    extractAudioFromVideoRecursive(str, start_extenstion, z, i)
                }
                return
            } catch (e: Exception) {
                e.printStackTrace()
                extractAudioFromVideo(str, z)
                return
            }
        }
        extractAudioFromVideo(str, z)
    }

    private fun extractAudioFromVideo(str: String, z: Boolean) {
        try {
            val file = File(File(mTemplate!!.folder_template!!), "${System.currentTimeMillis()}_audio.mp3")
            FFmpegKit.executeWithArgumentsAsync(
                arrayOf("-i", str, "-vn", "-acodec", "copy", "-y", file.absolutePath)
            ) { fFmpegSession ->
                if (fFmpegSession == null) {
                    runOnUiThread {
                        hideFragment()
                        hideProgressFragment()
                    }
                    return@executeWithArgumentsAsync
                }
                if (fFmpegSession.returnCode.isValueSuccess) {
                    val fromFile = Uri.fromFile(file)
                    mTemplate!!.extension = ".mp3"
                    if (!z) {
                        addUriAudioToQuranFragment(fromFile, str, 0)
                    } else {
                        addAudioTemplateHttp(fromFile, 0, str)
                    }
                    return@executeWithArgumentsAsync
                }
                runOnUiThread {
                    hideProgressFragment()
                    hideFragment()
                    Toast.makeText(
                        this@EngineActivity,
                        mResources!!.getString(R.string.video_not_have_sound),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                hideFragment()
                hideProgressFragment()
            }
        }
    }

    private fun toChoiceBgFromVideo(uri: Uri) {
        val intent = Intent(this, ChoiceBgFromVideoActivity::class.java)
        intent.data = uri
        launchChoiceBgActivity!!.launch(intent)
    }

    private fun handleVideo(uri: Uri) {
        showProgress()
        id_ffmpeg.clear()
        executor.execute(handleVideoRunnable(uri))
    }

    private fun handleVideoRunnable(uri: Uri): Runnable = Runnable {
        try {
            val copyFromUri = AudioUtils.copyFromUri(this@EngineActivity, uri, mTemplate!!.folder_template!!)!!
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(this@EngineActivity, uri)
            mediaPlayer.setOnPreparedListener { mp ->
                if (mp == null) {
                    return@setOnPreparedListener
                }
                val height = blurredImageView.getH()
                mTemplate!!.isVideoSquare = true
                blurredImageView.isVideo = true
                mTemplate!!.name_drawable = null
                mTemplate!!.uri_original_upload_video = uri.toString()
                mTemplate!!.uri_media_video = copyFromUri
                mTemplate!!.duration_video_media = mp.duration / 1000
                val fileVideo = FileUtils.getFileVideo(mTemplate!!.folder_template!!)!!
                val file = File(fileVideo, "frame_%04d.jpg")
                val file2 = File(fileVideo, "frame_0001.jpg")
                mTemplate!!.frame_bg = file2.absolutePath
                endFrame = Math.min(Math.round(trackViewEntity.maxTime / 1000.0f), 3)
                if (endFrame == 0) {
                    endFrame = 3
                }
                val valCopyFromUri = copyFromUri
                id_ffmpeg.add(
                    FFmpegKit.executeWithArgumentsAsync(
                        arrayOf(
                            "-i", valCopyFromUri, "-ss", "0", "-t", "$endFrame",
                            "-r", "25", "-vf",
                            "scale=$height:$height:force_original_aspect_ratio=increase",
                            "-q:v", "0", "-threads", "4", "-an", "-y",
                            file.absolutePath
                        )
                    ) { _ ->
                        changeBitmap(file2.absolutePath)
                        runOnUiThread {
                            hideProgressFragment()
                        }
                        id_ffmpeg.add(
                            FFmpegKit.executeWithArgumentsAsync(
                                arrayOf(
                                    "-i", valCopyFromUri, "-ss", "$endFrame",
                                    "-r", "25", "-vf",
                                    "scale=$height:$height:force_original_aspect_ratio=increase",
                                    "-start_number", "${endFrame * 25}",
                                    "-q:v", "0", "-threads", "4", "-an", "-y",
                                    file.absolutePath
                                )
                            ) { _ ->
                            }.sessionId
                        )
                    }.sessionId
                )
            }
            mediaPlayer.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                hideProgressFragment()
            }
        }
    }

    private fun changeBitmap(str: String) {
        executor.execute(Runnable {
            var cropTo16x9: Bitmap?
            var cropToSquareWithRoundCorners: Bitmap? = null
            var bitmap: Bitmap?
            var rect: Rect?
            try {
                val height = blurredImageView.getH()
                val bitmap2 = Glide.with(this@EngineActivity as FragmentActivity)
                    .asBitmap()
                    .load(str)
                    .override(height, height)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .submit().get()
                if (bitmap2 == null) {
                    return@Runnable
                }
                blurredImageView.bitmapOriginal = bitmap2
                if (mTemplate!!.ipad_type == IpadType.RECT.ordinal ||
                    mTemplate!!.ipad_type == IpadType.ROUND_RECT.ordinal
                ) {
                    mTemplate!!.ipad_type = IpadType.IPAD.ordinal
                }
                cropTo16x9 = when (mTemplate!!.geTypeResize()) {
                    ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(
                        blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH()
                    )
                    ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(
                        blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH()
                    )
                    else -> BitmapCropper.cropTo16x9(
                        blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH()
                    )
                }
                blurredImageView.updatePosCanvas(cropTo16x9)
                blurredImageView.updateIpad(cropTo16x9, mTemplate!!.ipad_type, mTemplate!!.geTypeResize())
                if (mTemplate!!.ipad_type != IpadType.BLACK_LAYER.ordinal &&
                    mTemplate!!.ipad_type != IpadType.GRADIENT.ordinal &&
                    mTemplate!!.ipad_type != IpadType.MASK_BRUSH.ordinal &&
                    mTemplate!!.ipad_type != IpadType.BLUE_TYPE.ordinal &&
                    mTemplate!!.ipad_type != IpadType.CASSET_IMG.ordinal
                ) {
                    if (mTemplate!!.ipad_type == IpadType.CASSET_IMG_BLUR.ordinal) {
                        blurredImageView.bitmapBlured = UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1)
                        blurredImageView.bitmapSquare = blurredImageView.bitmapBlured
                    } else {
                        val min = Math.min(
                            blurredImageView.bitmapOriginal!!.width,
                            blurredImageView.bitmapOriginal!!.height
                        )
                        var i = 0
                        if (mTemplate!!.ipad_type == IpadType.IPAD_NEOMORPHIC.ordinal) {
                            val width = (blurredImageView.ipad_rect!!.width() * 0.6f).toInt()
                            val f = min.toFloat()
                            var round = Math.round(mTemplate!!.x_square * f)
                            var round2 = Math.round(mTemplate!!.y_square * f)
                            var i2 = width + round
                            if (i2 > blurredImageView.bitmapOriginal!!.width) {
                                round -= i2 - blurredImageView.bitmapOriginal!!.width
                                i2 = blurredImageView.bitmapOriginal!!.width
                            }
                            var i3 = width + round2
                            if (i3 > blurredImageView.bitmapOriginal!!.height) {
                                round2 -= i3 - blurredImageView.bitmapOriginal!!.height
                                i3 = blurredImageView.bitmapOriginal!!.height
                            }
                            if (round < 0) {
                                round = 0
                            }
                            if (round2 >= 0) {
                                i = round2
                            }
                            val rect2 = Rect(round, i, i2, i3)
                            blurredImageView.setRadius_square(width)
                            val widthSquare = (mTemplate!!.width_square * f).toInt()
                            val heightSquare = (f * mTemplate!!.height_square).toInt()
                            val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(
                                blurredImageView.bitmapOriginal!!, rect2, width, widthSquare, heightSquare
                            )
                            rect2.right = rect2.left + widthSquare
                            rect2.bottom = rect2.top + heightSquare
                            blurredImageView.rectSquare = rect2
                            bitmap = cropToSquareWithRoundCorners2
                            rect = rect2
                        } else {
                            if (mTemplate!!.ipad_type != IpadType.IPAD.ordinal &&
                                mTemplate!!.ipad_type != IpadType.IPAD_UNBLUR.ordinal &&
                                mTemplate!!.ipad_type != IpadType.IPAD_CLASSIC.ordinal
                            ) {
                                val width2 = (blurredImageView.ipad_rect!!.width() * 1.0f).toInt()
                                val height2 = (cropTo16x9!!.height * 0.5355f).toInt()
                                val f2 = min.toFloat()
                                var round3 = Math.round(mTemplate!!.x_square * f2)
                                var round4 = Math.round(mTemplate!!.y_square * f2)
                                var i4 = width2 + round3
                                if (i4 > blurredImageView.bitmapOriginal!!.width) {
                                    round3 -= i4 - blurredImageView.bitmapOriginal!!.width
                                    i4 = blurredImageView.bitmapOriginal!!.width
                                }
                                var i5 = height2 + round4
                                if (i5 > blurredImageView.bitmapOriginal!!.height) {
                                    round4 -= i5 - blurredImageView.bitmapOriginal!!.height
                                    i5 = blurredImageView.bitmapOriginal!!.height
                                }
                                if (round3 < 0) {
                                    round3 = 0
                                }
                                if (round4 < 0) {
                                    round4 = 0
                                }
                                val rect3 = Rect(round3, round4, i4, i5)
                                val widthSquare2 = (mTemplate!!.width_square * f2).toInt()
                                val heightSquare2 = (f2 * mTemplate!!.height_square).toInt()
                                val cropToSquare = UtilsBitmap.cropToSquare(
                                    blurredImageView.bitmapOriginal!!, rect3, widthSquare2, heightSquare2
                                )
                                blurredImageView.bitmapSquare = cropToSquare
                                blurredImageView.setRadius_square(0)
                                rect3.right = rect3.left + widthSquare2
                                rect3.bottom = rect3.top + heightSquare2
                                blurredImageView.rectSquare = rect3
                                bitmap = cropToSquare
                                rect = rect3
                            } else {
                                val width3 = (blurredImageView.ipad_rect!!.width() * 0.87530595f).toInt()
                                val i6 = (width3 * 1.13f).toInt()
                                val min2 = Math.min(width3, i6)
                                val f3 = min.toFloat()
                                var round5 = Math.round(mTemplate!!.x_square * f3)
                                var round6 = Math.round(mTemplate!!.y_square * f3)
                                var i7 = width3 + round5
                                if (i7 > blurredImageView.bitmapOriginal!!.width) {
                                    round5 -= i7 - blurredImageView.bitmapOriginal!!.width
                                    i7 = blurredImageView.bitmapOriginal!!.width
                                }
                                var i8 = i6 + round6
                                if (i8 > blurredImageView.bitmapOriginal!!.height) {
                                    round6 -= i8 - blurredImageView.bitmapOriginal!!.height
                                    i8 = blurredImageView.bitmapOriginal!!.height
                                }
                                if (round5 < 0) {
                                    round5 = 0
                                }
                                if (round6 < 0) {
                                    round6 = 0
                                }
                                val rect4 = Rect(round5, round6, i7, i8)
                                cropToSquareWithRoundCorners = if (mTemplate!!.ipad_type == IpadType.IPAD_CLASSIC.ordinal) {
                                    val widthSquare3 = (mTemplate!!.width_square * f3).toInt()
                                    val heightSquare3 = (f3 * mTemplate!!.height_square).toInt()
                                    val cropToSquare2 = UtilsBitmap.cropToSquare(
                                        blurredImageView.bitmapOriginal!!, rect4, widthSquare3, heightSquare3
                                    )
                                    blurredImageView.bitmapSquare = cropToSquare2
                                    blurredImageView.setRadius_square(0)
                                    rect4.right = rect4.left + widthSquare3
                                    rect4.bottom = rect4.top + heightSquare3
                                    blurredImageView.rectSquare = rect4
                                    cropToSquare2
                                } else {
                                    val i9 = (min2 * 0.10800001f).toInt()
                                    blurredImageView.setRadius_square(i9)
                                    val widthSquare4 = (mTemplate!!.width_square * f3).toInt()
                                    val heightSquare4 = (f3 * mTemplate!!.height_square).toInt()
                                    val result = UtilsBitmap.cropToSquareWithRoundCorners(
                                        blurredImageView.bitmapOriginal!!, rect4, i9, widthSquare4, heightSquare4
                                    )
                                    rect4.right = rect4.left + widthSquare4
                                    rect4.bottom = rect4.top + heightSquare4
                                    blurredImageView.rectSquare = rect4
                                    result
                                }
                                bitmap = cropToSquareWithRoundCorners
                                rect = rect4
                            }
                            blurredImageView.setBitmap(
                                UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1),
                                bitmap, -1, mTemplate!!.ipad_type, mTemplate!!.geTypeResize(), rect
                            )
                        }
                        mTemplate!!.color_ipad = blurredImageView.colorIpad()
                        runOnUiThread {
                            blurredImageView.invalidate()
                        }
                    }
                    if (mTemplate!!.ipad_type == IpadType.GRADIENT.ordinal) {
                        blurredImageView.setColorIpad(ViewCompat.MEASURED_STATE_MASK)
                    }
                    blurredImageView.bitmapSquare = cropTo16x9
                    blurredImageView.bitmapBlured = UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1)
                    mTemplate!!.color_ipad = blurredImageView.colorIpad()
                    runOnUiThread {
                        blurredImageView.invalidate()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun updateSquareBitmap(str: String) {
        if (isOnScroll) {
            if (mIsPlaying) {
                return
            }
        } else if (!mIsPlaying) {
            return
        }
        executor.execute {
            var bitmap: Bitmap?
            var cropTo16x9: Bitmap?
            try {
                try {
                    val height = blurredImageView.getH()
                    bitmap = Glide.with(this@EngineActivity as FragmentActivity)
                        .asBitmap()
                        .load(str)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .override(height, height)
                        .submit().get()
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        if (!isOnScroll) {
                            blurredImageView.isDrawingSquareVideo = true
                        }
                        blurredImageView.invalidate()
                    }
                    return@execute
                }
                if (bitmap == null) {
                    return@execute
                }
                if (mTemplate!!.ipad_type != IpadType.BLACK_LAYER.ordinal &&
                    mTemplate!!.ipad_type != IpadType.GRADIENT.ordinal &&
                    mTemplate!!.ipad_type != IpadType.MASK_BRUSH.ordinal &&
                    mTemplate!!.ipad_type != IpadType.BLUE_TYPE.ordinal &&
                    mTemplate!!.ipad_type != IpadType.CASSET_IMG.ordinal &&
                    mTemplate!!.ipad_type != IpadType.CASSET_IMG_BLUR.ordinal
                ) {
                    if (mTemplate!!.ipad_type != IpadType.IPAD.ordinal &&
                        mTemplate!!.ipad_type != IpadType.IPAD_UNBLUR.ordinal &&
                        mTemplate!!.ipad_type != IpadType.BOTTOM_RECT.ordinal &&
                        mTemplate!!.ipad_type != IpadType.IPAD_CLASSIC.ordinal &&
                        mTemplate!!.ipad_type != IpadType.IPAD_NEOMORPHIC.ordinal
                    ) {
                        val width = (blurredImageView.ipad_rect!!.width() * 0.87530595f).toInt()
                        val i = (width * 1.13f).toInt()
                        val min = (Math.min(width, i) * 0.10800001f).toInt()
                        var round = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                        var round2 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                        var i2 = width + round
                        if (i2 > blurredImageView.bitmapOriginal!!.width) {
                            round -= i2 - blurredImageView.bitmapOriginal!!.width
                            i2 = blurredImageView.bitmapOriginal!!.width
                        }
                        var i3 = i + round2
                        if (i3 > blurredImageView.bitmapOriginal!!.height) {
                            round2 -= i3 - blurredImageView.bitmapOriginal!!.height
                            i3 = blurredImageView.bitmapOriginal!!.height
                        }
                        if (round < 0) {
                            round = 0
                        }
                        if (round2 < 0) {
                            round2 = 0
                        }
                        val rect = Rect(round, round2, i2, i3)
                        val width2 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                        val height2 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                        blurredImageView.setBitmapSquare(
                            UtilsBitmap.cropToSquareWithRoundCorners(bitmap, rect, min, width2, height2)
                        )
                        rect.right = rect.left + width2
                        rect.bottom = rect.top + height2
                        blurredImageView.rectSquare = rect
                    } else {
                        blurredImageView.setBitmapSquare(
                            UtilsBitmap.cropToSquareWithRoundCornersPlusScale(
                                bitmap, blurredImageView.rectSquare!!, blurredImageView.getRadius_square(),
                                blurredImageView.bitmapSquare!!.width, blurredImageView.bitmapSquare!!.height
                            )
                        )
                    }
                    runOnUiThread {
                        if (!isOnScroll) {
                            blurredImageView.isDrawingSquareVideo = true
                        }
                        blurredImageView.invalidate()
                    }
                }
                cropTo16x9 = when (mTemplate!!.geTypeResize()) {
                    ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(
                        bitmap, blurredImageView.getW(), blurredImageView.getH()
                    )
                    ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(
                        bitmap, blurredImageView.getW(), blurredImageView.getH()
                    )
                    else -> BitmapCropper.cropTo16x9(
                        bitmap, blurredImageView.getW(), blurredImageView.getH()
                    )
                }
                blurredImageView.bitmapSquare = cropTo16x9
                runOnUiThread {
                    if (!isOnScroll) {
                        blurredImageView.isDrawingSquareVideo = true
                    }
                    blurredImageView.invalidate()
                }
            } finally {
                runOnUiThread {
                    if (!isOnScroll) {
                        blurredImageView.isDrawingSquareVideo = true
                    }
                    blurredImageView.invalidate()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun setupOriginalBitmap(uri: Uri): Bitmap {
        val height = blurredImageView.getH()
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val min = height / Math.min(bitmap.width, bitmap.height).toFloat()
        return Bitmap.createScaledBitmap(
            bitmap, Math.round(bitmap.width * min), Math.round(bitmap.height * min), true
        )
    }

    private fun setupOriginalBitmap(bitmap: Bitmap, i: Int): Bitmap {
        val min = i / Math.min(bitmap.width, bitmap.height).toFloat()
        return Bitmap.createScaledBitmap(
            bitmap, Math.round(bitmap.width * min), Math.round(bitmap.height * min), true
        )
    }

    private fun handleImg(uri: Uri) {
        showProgress()
        executor.execute {
            var cropTo16x9: Bitmap?
            var cropToSquareWithRoundCorners: Bitmap? = null
            var bitmap: Bitmap?
            var rect: Rect?
            try {
                try {
                    contentResolver.takePersistableUriPermission(uri, 1)
                } catch (_: Exception) {
                }
                try {
                    try {
                        uri_bg = uri.toString()
                        mTemplate!!.name_drawable = null
                        mTemplate!!.uri_bg = uri_bg
                        var i = 0
                        mTemplate!!.isVideoSquare = false
                        blurredImageView.isVideo = false
                        blurredImageView.bitmapOriginal = setupOriginalBitmap(uri)
                        cropTo16x9 = when (mTemplate!!.geTypeResize()) {
                            ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(
                                blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH()
                            )
                            ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(
                                blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH()
                            )
                            else -> BitmapCropper.cropTo16x9(
                                blurredImageView.bitmapOriginal, blurredImageView.getW(), blurredImageView.getH()
                            )
                        }
                        blurredImageView.updatePosCanvas(cropTo16x9)
                        blurredImageView.updateIpad(cropTo16x9, mTemplate!!.ipad_type, mTemplate!!.geTypeResize())
                        val min = Math.min(
                            blurredImageView.bitmapOriginal!!.width,
                            blurredImageView.bitmapOriginal!!.height
                        )
                        if (mTemplate!!.ipad_type == IpadType.IPAD_NEOMORPHIC.ordinal) {
                            val width = (blurredImageView.ipad_rect!!.width() * 0.6f).toInt()
                            val f = min.toFloat()
                            var round = Math.round(mTemplate!!.x_square * f)
                            var round2 = Math.round(mTemplate!!.y_square * f)
                            var i2 = width + round
                            if (i2 > blurredImageView.bitmapOriginal!!.width) {
                                round -= i2 - blurredImageView.bitmapOriginal!!.width
                                i2 = blurredImageView.bitmapOriginal!!.width
                            }
                            var i3 = width + round2
                            if (i3 > blurredImageView.bitmapOriginal!!.height) {
                                round2 -= i3 - blurredImageView.bitmapOriginal!!.height
                                i3 = blurredImageView.bitmapOriginal!!.height
                            }
                            if (round < 0) {
                                round = 0
                            }
                            if (round2 >= 0) {
                                i = round2
                            }
                            val rect2 = Rect(round, i, i2, i3)
                            blurredImageView.setRadius_square(width)
                            val widthSquare = (mTemplate!!.width_square * f).toInt()
                            val heightSquare = (f * mTemplate!!.height_square).toInt()
                            val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(
                                blurredImageView.bitmapOriginal!!, rect2, width, widthSquare, heightSquare
                            )
                            blurredImageView.bitmapSquare = cropToSquareWithRoundCorners2
                            rect2.right = rect2.left + widthSquare
                            rect2.bottom = rect2.top + heightSquare
                            blurredImageView.rectSquare = rect2
                            bitmap = cropToSquareWithRoundCorners2
                            rect = rect2
                        } else {
                            if (mTemplate!!.ipad_type != IpadType.IPAD.ordinal &&
                                mTemplate!!.ipad_type != IpadType.IPAD_UNBLUR.ordinal &&
                                mTemplate!!.ipad_type != IpadType.IPAD_CLASSIC.ordinal
                            ) {
                                if (mTemplate!!.ipad_type == IpadType.BOTTOM_RECT.ordinal) {
                                    val width2 = (blurredImageView.ipad_rect!!.width() * 1.0f).toInt()
                                    val height = (cropTo16x9!!.height * 0.5355f).toInt()
                                    val f2 = min.toFloat()
                                    var round3 = Math.round(mTemplate!!.x_square * f2)
                                    var round4 = Math.round(mTemplate!!.y_square * f2)
                                    var i4 = width2 + round3
                                    if (i4 > blurredImageView.bitmapOriginal!!.width) {
                                        round3 -= i4 - blurredImageView.bitmapOriginal!!.width
                                        i4 = blurredImageView.bitmapOriginal!!.width
                                    }
                                    var i5 = height + round4
                                    if (i5 > blurredImageView.bitmapOriginal!!.height) {
                                        round4 -= i5 - blurredImageView.bitmapOriginal!!.height
                                        i5 = blurredImageView.bitmapOriginal!!.height
                                    }
                                    if (round3 < 0) {
                                        round3 = 0
                                    }
                                    if (round4 < 0) {
                                        round4 = 0
                                    }
                                    val rect3 = Rect(round3, round4, i4, i5)
                                    val widthSquare2 = (mTemplate!!.width_square * f2).toInt()
                                    val heightSquare2 = (f2 * mTemplate!!.height_square).toInt()
                                    val cropToSquare = UtilsBitmap.cropToSquare(
                                        blurredImageView.bitmapOriginal!!, rect3, widthSquare2, heightSquare2
                                    )
                                    blurredImageView.bitmapSquare = cropToSquare
                                    blurredImageView.setRadius_square(0)
                                    rect3.right = rect3.left + widthSquare2
                                    rect3.bottom = rect3.top + heightSquare2
                                    blurredImageView.rectSquare = rect3
                                    bitmap = cropToSquare
                                    rect = rect3
                                } else {
                                    bitmap = null
                                    rect = null
                                }
                            } else {
                                val width3 = (blurredImageView.ipad_rect!!.width() * 0.87530595f).toInt()
                                val i6 = (width3 * 1.13f).toInt()
                                val min2 = Math.min(width3, i6)
                                val f3 = min.toFloat()
                                var round5 = Math.round(mTemplate!!.x_square * f3)
                                var round6 = Math.round(mTemplate!!.y_square * f3)
                                var i7 = width3 + round5
                                if (i7 > blurredImageView.bitmapOriginal!!.width) {
                                    round5 -= i7 - blurredImageView.bitmapOriginal!!.width
                                    i7 = blurredImageView.bitmapOriginal!!.width
                                }
                                var i8 = i6 + round6
                                if (i8 > blurredImageView.bitmapOriginal!!.height) {
                                    round6 -= i8 - blurredImageView.bitmapOriginal!!.height
                                    i8 = blurredImageView.bitmapOriginal!!.height
                                }
                                if (round5 < 0) {
                                    round5 = 0
                                }
                                if (round6 < 0) {
                                    round6 = 0
                                }
                                val rect4 = Rect(round5, round6, i7, i8)
                                cropToSquareWithRoundCorners = if (mTemplate!!.ipad_type == IpadType.IPAD_CLASSIC.ordinal) {
                                    val widthSquare3 = (mTemplate!!.width_square * f3).toInt()
                                    val heightSquare3 = (f3 * mTemplate!!.height_square).toInt()
                                    val cropToSquare2 = UtilsBitmap.cropToSquare(
                                        blurredImageView.bitmapOriginal!!, rect4, widthSquare3, heightSquare3
                                    )
                                    blurredImageView.bitmapSquare = cropToSquare2
                                    blurredImageView.setRadius_square(0)
                                    rect4.right = rect4.left + widthSquare3
                                    rect4.bottom = rect4.top + heightSquare3
                                    blurredImageView.rectSquare = rect4
                                    cropToSquare2
                                } else {
                                    val i9 = (min2 * 0.10800001f).toInt()
                                    blurredImageView.setRadius_square(i9)
                                    val widthSquare4 = (mTemplate!!.width_square * f3).toInt()
                                    val heightSquare4 = (f3 * mTemplate!!.height_square).toInt()
                                    val result = UtilsBitmap.cropToSquareWithRoundCorners(
                                        blurredImageView.bitmapOriginal!!, rect4, i9, widthSquare4, heightSquare4
                                    )
                                    blurredImageView.bitmapSquare = result
                                    rect4.right = rect4.left + widthSquare4
                                    rect4.bottom = rect4.top + heightSquare4
                                    blurredImageView.rectSquare = rect4
                                    result
                                }
                                bitmap = cropToSquareWithRoundCorners
                                rect = rect4
                            }
                            when (mTemplate!!.ipad_type) {
                                IpadType.GRADIENT.ordinal -> blurredImageView.setBitmap(
                                    UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1),
                                    bitmap, ViewCompat.MEASURED_STATE_MASK,
                                    mTemplate!!.ipad_type, mTemplate!!.geTypeResize(), rect
                                )
                                IpadType.BLUE_TYPE.ordinal -> {
                                    if (blurredImageView.color_gradient != null) {
                                        blurredImageView.setBitmap(
                                            UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1),
                                            bitmap, blurredImageView.color_gradient!!,
                                            mTemplate!!.ipad_type, mTemplate!!.geTypeResize(), rect
                                        )
                                    } else {
                                        blurredImageView.setBitmap(
                                            UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1),
                                            bitmap, blurredImageView.color_ipad,
                                            mTemplate!!.ipad_type, mTemplate!!.geTypeResize(), rect
                                        )
                                    }
                                }
                                else -> blurredImageView.setBitmap(
                                    UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1),
                                    bitmap, -1, mTemplate!!.ipad_type, mTemplate!!.geTypeResize(), rect
                                )
                            }
                            blurredImageView.invalidate()
                            runOnUiThread {
                                hideProgressFragment()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            hideProgressFragment()
                        }
                    }
                } catch (_: Exception) {
                }
            } finally {
            }
        }
    }

    private fun updateTime() {
        trackViewEntity.calculMaxTime()
        updateViewTime(trackViewEntity.maxTime, trackViewEntity.current_cursur_position)
        if (trackViewEntity.current_cursur_position <= trackViewEntity.maxTime) {
            updateTime(trackViewEntity.current_cursur_position.toLong())
            val trackEntityView = trackViewEntity
            trackEntityView.current_cursur_position = trackEntityView.current_cursur_position
            blurredImageView.progress = trackViewEntity.current_cursur_position.toFloat() / trackViewEntity.maxTime
        }
    }

    private fun updateTimeToEndAya() {
        trackViewEntity.calculMaxTime()
        trackViewEntity.translateToEnd()
        updateViewTime(trackViewEntity.maxTime, trackViewEntity.current_cursur_position)
        if (trackViewEntity.current_cursur_position <= trackViewEntity.maxTime) {
            updateTime(trackViewEntity.current_cursur_position.toLong())
            val trackEntityView = trackViewEntity
            trackEntityView.current_cursur_position = trackEntityView.current_cursur_position
            blurredImageView.progress = trackViewEntity.current_cursur_position.toFloat() / trackViewEntity.maxTime
        }
    }

    private fun selectSurahName() {
        findViewById<View>(R.id.layout_menu).visibility = 4
        val surahNameEntity = blurredImageView.surahNameEntity
        val beginTransaction = supportFragmentManager.beginTransaction()
        mCurrentFragment = EditS_NameFragment.getInstance(iEditSName, mResources, surahNameEntity)
        beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
        beginTransaction.commit()
    }

    fun dialogDeleteSelected() {
        try {
            val dialog = Dialog(this)
            this.dialog = dialog
            dialog.setCancelable(true)
            this.dialog!!.requestWindowFeature(1)
            this.dialog!!.window!!.setLayout(-1, -2)
            this.dialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null)
            this.dialog!!.setContentView(inflate)
            inflate.findViewById<View>(R.id.dialog_title).visibility = 8
            (inflate.findViewById<View>(R.id.dialog_message) as TextCustumFont).text =
                mResources!!.getString(R.string.are_you_sure_to_delete_this_work)
            val buttonCustumFont = inflate.findViewById<ButtonCustumFont>(R.id.dialog_no)
            buttonCustumFont.text = mResources!!.getString(R.string.delete)
            buttonCustumFont.setTextColor(-1499549)
            // buttonCustumFont.setBackgroundResource(android.R.drawable.ic_delete)
            buttonCustumFont.setOnClickListener {
                buttonCustumFont.isClickable = false
                showProgress()
                Thread {
                    trackViewEntity.deleteEntityAllSelect()
                    runOnUiThread {
                        trackViewEntity.invalidate()
                        updateTime()
                        hideProgressFragment()
                        iTrimLineCallback!!.onEmptySelect()
                    }
                }.start()
                if (this@EngineActivity.dialog != null) {
                    this@EngineActivity.dialog!!.dismiss()
                }
            }
            val buttonCustumFont2 = inflate.findViewById<ButtonCustumFont>(R.id.dialog_yes)
            buttonCustumFont2.text = mResources!!.getString(R.string.no)
            buttonCustumFont2.setOnClickListener {
                this@EngineActivity.dialog!!.dismiss()
            }
            this.dialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyffectPlayAuto(str: String, entityAudio: EntityAudio) {
        showProgressSimple()
        val file = File(mTemplate!!.folder_template, "${System.currentTimeMillis()}_audio_echo.mp3")
        id_ffmpeg.add(
            FFmpegKit.executeWithArgumentsAsync(
                arrayOf("-i", entityAudio.getPathFfmpeg()!!, "-af", str, "-y", file.absolutePath)
            ) { fFmpegSession ->
                if (fFmpegSession.returnCode.isValueSuccess()) {
                    try {
                        val uri = Uri.fromFile(file)
                        mPlayer = MediaPlayer()
                        mPlayer!!.setAudioStreamType(3)
                        if (uri.scheme != null && uri.scheme!!.startsWith("http")) {
                            mPlayer!!.setDataSource(uri.toString())
                        } else {
                            mPlayer!!.setDataSource(this@EngineActivity, uri)
                        }
                        mPlayer!!.prepareAsync()
                        mPlayer!!.setOnPreparedListener { mediaPlayer ->
                            if (entityAudio.mediaPlayer != null &&
                                mediaPlayer.duration != entityAudio.mediaPlayer!!.duration
                            ) {
                                entityAudio.right = 
                                    entityAudio.rect.left + Math.round(
                                        trackViewEntity.getSecond_in_screen() * (mediaPlayer.duration / 1000.0f)
                                    )
                                entityAudio.end = mediaPlayer.duration.toFloat()
                                entityAudio.start = 0.0f
                                entityAudio.max =
                                    (entityAudio.rect.right / entityAudio.scaleFactor) -
                                        ((entityAudio.rect.left / entityAudio.scaleFactor) - entityAudio.getOffsetLeft())
                                trackViewEntity.updateWhenEffect(entityAudio)
                                runOnUiThread {
                                    trackViewEntity.invalidate()
                                    entityAudio.mediaPlayer = mediaPlayer
                                    iEditMediaCallback!!.startPreview()
                                    hideProgressFragment()
                                }
                            } else {
                                runOnUiThread {
                                    entityAudio.mediaPlayer = mediaPlayer
                                    iEditMediaCallback!!.startPreview()
                                    hideProgressFragment()
                                }
                            }
                        }
                        entityAudio.setApplyEffectInPreview(true)
                        entityAudio.setPathFfmpegEffect(file.absolutePath)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            hideProgressFragment()
                        }
                    }
                }
            }.sessionId
        )
    }

    fun checkSplitEntity() {
        if (EditEntityFragment.instance == null || trackViewEntity.selectedEntity == null) {
            return
        }
        EditEntityFragment.instance!!.checkSplitEntity(
            trackViewEntity.selectedEntity!!, -trackViewEntity.getCurrentPosition()
        )
    }

    fun checkSplitTrslEntity() {
        if (EditTrslEntityFragment.instance == null || trackViewEntity.selectedEntity == null) {
            return
        }
        EditTrslEntityFragment.instance!!.checkSplitEntity(
            trackViewEntity.selectedEntity!!, -trackViewEntity.getCurrentPosition()
        )
    }

    fun checkSplitAudio() {
        if (EditMediaFragment.instance == null || trackViewEntity.selectedEntity !is EntityAudio) {
            return
        }
        val f = -trackViewEntity.getCurrentPosition()
        EditMediaFragment.instance!!.checkSplit(trackViewEntity.selectedEntity!! as EntityAudio, f)
    }

    private fun clearCallback() {
        // These are val properties and cannot be reassigned to null
        // In the original Java code these were nullable interface references
    }

    private fun addUpdateAnim(
        entityBismilahTimeline: EntityBismilahTimeline?,
        entityBismilahTimeline2: EntityBismilahTimeline
    ) {
        if (entityBismilahTimeline == null) {
            return
        }
        if (entityBismilahTimeline.getTransition() == null) {
            entityBismilahTimeline.setTransition(Transition())
        }
        entityBismilahTimeline.getTransition()!!.isOut = entityBismilahTimeline2.getTransition()!!.isOut
        entityBismilahTimeline.getTransition()!!.type_out = entityBismilahTimeline2.getTransition()!!.type_out
        entityBismilahTimeline.getTransition()!!.duration_out = entityBismilahTimeline2.getTransition()!!.duration_out
        entityBismilahTimeline.getTransition()!!.isIn = entityBismilahTimeline2.getTransition()!!.isIn
        entityBismilahTimeline.getTransition()!!.type_in = entityBismilahTimeline2.getTransition()!!.type_in
        entityBismilahTimeline.getTransition()!!.duration_in = entityBismilahTimeline2.getTransition()!!.duration_in
    }

    private fun addUpdateAnim(
        entityBismilahTimeline: EntityBismilahTimeline?,
        entityQuranTimeline: EntityQuranTimeline
    ) {
        if (entityBismilahTimeline == null) {
            return
        }
        if (entityBismilahTimeline.getTransition() == null) {
            entityBismilahTimeline.setTransition(Transition())
        }
        entityBismilahTimeline.getTransition()!!.isOut = entityQuranTimeline!!.getTransition()!!.isOut
        entityBismilahTimeline.getTransition()!!.type_out = entityQuranTimeline!!.getTransition()!!.type_out
        entityBismilahTimeline.getTransition()!!.duration_out = entityQuranTimeline!!.getTransition()!!.duration_out
        entityBismilahTimeline.getTransition()!!.isIn = entityQuranTimeline!!.getTransition()!!.isIn
        entityBismilahTimeline.getTransition()!!.type_in = entityQuranTimeline!!.getTransition()!!.type_in
        entityBismilahTimeline.getTransition()!!.duration_in = entityQuranTimeline!!.getTransition()!!.duration_in
    }

    fun start() {
        if (mTemplate!!.ipad_type == IpadType.RECT.ordinal ||
            mTemplate!!.ipad_type == IpadType.ROUND_RECT.ordinal ||
            mTemplate!!.ipad_type == IpadType.CASSET_IMG_BLUR.ordinal ||
            mTemplate!!.ipad_type == IpadType.CASSET.ordinal
        ) {
            return
        }
        isOnScroll = false
        val smoothVideoAnimator = SmoothVideoAnimator(
            trackViewEntity, mTemplate!!, 25,
            object : SmoothVideoAnimator.FrameUpdateListener {
                override fun onAnimationEnd() {}

                override fun onFrameUpdate(str: String) {
                    synchronized(frameLock) {
                        pendingFramePath = str
                        if (!isProcessingFrame) {
                            isProcessingFrame = true
                            executor.execute(frameProcessorRunnable)
                        }
                    }
                }
            }
        )
        animator_frame_video = smoothVideoAnimator
        smoothVideoAnimator.start()
    }

    fun stop() {
        blurredImageView.isDrawingSquareVideo = false
        val smoothVideoAnimator = animator_frame_video
        if (smoothVideoAnimator != null) {
            smoothVideoAnimator.stop()
        }
    }

    private fun updateFrame() {
        val template = mTemplate
        if (template == null || !template.isVideoSquare ||
            mTemplate!!.ipad_type == IpadType.RECT.ordinal ||
            mTemplate!!.ipad_type == IpadType.ROUND_RECT.ordinal ||
            mTemplate!!.ipad_type == IpadType.CASSET_IMG_BLUR.ordinal ||
            mTemplate!!.ipad_type == IpadType.CASSET.ordinal ||
            mIsPlaying
        ) {
            return
        }
        var max = Math.max(1, Math.round((trackViewEntity.current_cursur_position / 1000.0f) * 25.0f))
        val min = Math.min(
            mTemplate!!.duration_video_media * 25,
            trackViewEntity.duration * 25
        )
        if (max > min) {
            max = ((max - 1) % min) + 1
        }
        val str = when {
            max < 10 -> "frame_000$max.jpg"
            max < 100 -> "frame_00$max.jpg"
            max < 1000 -> "frame_0$max.jpg"
            else -> "frame_$max.jpg"
        }
        isOnScroll = true
        updateSquareBitmap(
            File(mTemplate!!.folder_template + "/VideoFrame", str).absolutePath
        )
    }

    private fun processFrame(str: String) {
        var cropTo16x9: Bitmap?
        try {
            if (!(isOnScroll && mIsPlaying) && mIsPlaying) {
                val height = blurredImageView.getH()
                val bitmap = Glide.with(this as FragmentActivity)
                    .asBitmap()
                    .load(str)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(height, height)
                    .submit().get()
                if (bitmap == null) {
                    return
                }
                if (mTemplate!!.ipad_type != IpadType.BLACK_LAYER.ordinal &&
                    mTemplate!!.ipad_type != IpadType.GRADIENT.ordinal &&
                    mTemplate!!.ipad_type != IpadType.MASK_BRUSH.ordinal &&
                    mTemplate!!.ipad_type != IpadType.BLUE_TYPE.ordinal &&
                    mTemplate!!.ipad_type != IpadType.CASSET_IMG.ordinal
                ) {
                    if (mTemplate!!.ipad_type != IpadType.IPAD.ordinal &&
                        mTemplate!!.ipad_type != IpadType.IPAD_UNBLUR.ordinal &&
                        mTemplate!!.ipad_type != IpadType.BOTTOM_RECT.ordinal &&
                        mTemplate!!.ipad_type != IpadType.IPAD_CLASSIC.ordinal &&
                        mTemplate!!.ipad_type != IpadType.IPAD_NEOMORPHIC.ordinal
                    ) {
                        val width = (blurredImageView.ipad_rect!!.width() * 0.87530595f).toInt()
                        val i = (width * 1.13f).toInt()
                        val min = (Math.min(width, i) * 0.10800001f).toInt()
                        var round = Math.round(blurredImageView.bitmapOriginal!!.width * mTemplate!!.x_square)
                        var round2 = Math.round(blurredImageView.bitmapOriginal!!.height * mTemplate!!.y_square)
                        var i2 = width + round
                        if (i2 > blurredImageView.bitmapOriginal!!.width) {
                            round -= i2 - blurredImageView.bitmapOriginal!!.width
                            i2 = blurredImageView.bitmapOriginal!!.width
                        }
                        var i3 = i + round2
                        if (i3 > blurredImageView.bitmapOriginal!!.height) {
                            round2 -= i3 - blurredImageView.bitmapOriginal!!.height
                            i3 = blurredImageView.bitmapOriginal!!.height
                        }
                        if (round < 0) {
                            round = 0
                        }
                        if (round2 < 0) {
                            round2 = 0
                        }
                        val rect = Rect(round, round2, i2, i3)
                        val width2 = (blurredImageView.bitmapOriginal!!.width * mTemplate!!.width_square).toInt()
                        val height2 = (blurredImageView.bitmapOriginal!!.height * mTemplate!!.height_square).toInt()
                        cropTo16x9 = UtilsBitmap.cropToSquareWithRoundCorners(
                            bitmap, rect, min, width2, height2
                        )
                        rect.right = rect.left + width2
                        rect.bottom = rect.top + height2
                        blurredImageView.rectSquare = rect
                    } else {
                        cropTo16x9 = UtilsBitmap.cropToSquareWithRoundCornersPlusScale(
                            bitmap, blurredImageView.rectSquare!!, blurredImageView.getRadius_square(),
                            blurredImageView.bitmapSquare!!.width, blurredImageView.bitmapSquare!!.height
                        )
                    }
                    runOnUiThread {
                        blurredImageView.bitmapSquare = cropTo16x9
                        if (!isOnScroll) {
                            blurredImageView.isDrawingSquareVideo = true
                        }
                        blurredImageView.invalidate()
                    }
                }
                cropTo16x9 = when (mTemplate!!.geTypeResize()) {
                    ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(
                        bitmap, blurredImageView.getW(), blurredImageView.getH()
                    )
                    ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(
                        bitmap, blurredImageView.getW(), blurredImageView.getH()
                    )
                    else -> BitmapCropper.cropTo16x9(
                        bitmap, blurredImageView.getW(), blurredImageView.getH()
                    )
                }
                runOnUiThread {
                    blurredImageView.bitmapSquare = cropTo16x9
                    if (!isOnScroll) {
                        blurredImageView.isDrawingSquareVideo = true
                    }
                    blurredImageView.invalidate()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Missing method stubs ──────────────────────────────────────────────

    private fun pauseTimelineAnimation() {
        valueAnimator?.stop()
    }

    private fun updateBtnToStart() {
        try {
            if (::btnToStart.isInitialized) {
                btnToStart.isEnabled = trackViewEntity.current_cursur_position > 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateBtnToEnd() {
        try {
            if (::btnToEnd.isInitialized) {
                btnToEnd.isEnabled = trackViewEntity.current_cursur_position < trackViewEntity.maxTime
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelDialog() {
        try {
            if (mIsPlaying) {
                stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toProVersion() {
        try {
            dialogPremium(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showExitDialog() {
        try {
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("")
            builder.setMessage("")
            builder.setPositiveButton("") { _, _ -> finish() }
            builder.setNegativeButton("") { dialog, _ -> dialog.dismiss() }
            builder.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onChoiceBgResult(activityResult: ActivityResult) {
        // TODO: implement
    }

    private fun onCropResult(activityResult: ActivityResult) {
        // TODO: implement
    }

    private fun onImgResult(activityResult: ActivityResult) {
        // TODO: implement
    }

    private fun onVideoResult(activityResult: ActivityResult) {
        // TODO: implement
    }

    private fun onVideoExtractResult(activityResult: ActivityResult) {
        // TODO: implement
    }


    private fun addUriAudioToQuranFragment(uri: Uri, str: String, i: Int) {
        // TODO: implement
    }

    private fun addEntityFromTemplate(entityQuranTemplate: EntityQuranTemplate) {
        // TODO: implement
    }

    private fun initTypeVideo() {
        // TODO: implement
    }

    private val isSubscribed: Boolean
        get() = false

    private lateinit var seekbar_fps: CustomDiscreteSeekBar
    private lateinit var seekbar_resolution: CustomDiscreteSeekBar


    fun addTimeLineBismilah(bismilahEntity: BismilahEntity, left: Float, right: Float): EntityBismilahTimeline {
        val entityBismilahTimeline = EntityBismilahTimeline(
            bismilahEntity, left, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            right,
            trackViewEntity.getSecond_in_screen()
        )
        trackViewEntity.bismilahTimeline = entityBismilahTimeline
        return entityBismilahTimeline
    }

    fun addTimeLineIsti3ada(bismilahEntity: BismilahEntity, left: Float, right: Float): EntityBismilahTimeline {
        val entityBismilahTimeline = EntityBismilahTimeline(
            bismilahEntity, left, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            right,
            trackViewEntity.getSecond_in_screen()
        )
        trackViewEntity.setmIsi3adaTimeline(entityBismilahTimeline)
        return entityBismilahTimeline
    }

    fun addTimeLineQuran(quranEntity: QuranEntity, left: Float, right: Float): EntityQuranTimeline {
        val entityQuranTimeline = EntityQuranTimeline(
            quranEntity, left, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            right,
            trackViewEntity.getSecond_in_screen()
        )
        trackViewEntity.addQuran(entityQuranTimeline)
        return entityQuranTimeline
    }

    fun addTimeLineQuran(translationQuranEntity: TranslationQuranEntity, left: Float, right: Float): EntityTrslTimeline {
        val entityTrslTimeline = EntityTrslTimeline(
            translationQuranEntity, left, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            right,
            trackViewEntity.getSecond_in_screen()
        )
        trackViewEntity.addTrslQuran(entityTrslTimeline)
        return entityTrslTimeline
    }

}
