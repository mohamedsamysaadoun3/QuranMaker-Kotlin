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
import androidx.activity.EdgeToEdge
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
import hazem.nurmontage.videoquran.utils.AudioUtils
import hazem.nurmontage.videoquran.utils.BillingPreferences
import hazem.nurmontage.videoquran.utils.BitmapCropper
import hazem.nurmontage.videoquran.utils.ColorUtils
import hazem.nurmontage.videoquran.utils.DrawableHelper
import hazem.nurmontage.videoquran.utils.FileHelper
import hazem.nurmontage.videoquran.utils.FileUtils
import hazem.nurmontage.videoquran.utils.LocalPersistence
import hazem.nurmontage.videoquran.utils.LocaleHelper
import hazem.nurmontage.videoquran.utils.MyPrefereces
import hazem.nurmontage.videoquran.utils.MyVibrationHelper
import hazem.nurmontage.videoquran.utils.NetworkUtils
import hazem.nurmontage.videoquran.utils.PCMWaveformExtractor
import hazem.nurmontage.videoquran.utils.ScreenUtils
import hazem.nurmontage.videoquran.utils.SmoothTimelineAnimator
import hazem.nurmontage.videoquran.utils.SmoothVideoAnimator
import hazem.nurmontage.videoquran.utils.TimeFormatter
import hazem.nurmontage.videoquran.utils.Utils
import hazem.nurmontage.videoquran.utils.UtilsBitmap
import hazem.nurmontage.videoquran.utils.UtilsFileLast
import hazem.nurmontage.videoquran.adapter.DimensionAdabters
import hazem.nurmontage.videoquran.common.Common
import hazem.nurmontage.videoquran.constant.AyaTextPreset
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
import hazem.nurmontage.videoquran.model.BismilahEntity
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
import hazem.nurmontage.videoquran.model.QuranEntity
import hazem.nurmontage.videoquran.model.RecitersModel
import hazem.nurmontage.videoquran.model.SurahNameEntity
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.model.Transition
import hazem.nurmontage.videoquran.model.TranslationQuranEntity
import hazem.nurmontage.videoquran.views.BlurredImageView
import hazem.nurmontage.videoquran.views.ButtonCustumFont
import hazem.nurmontage.videoquran.views.CustomDiscreteSeekBar
import hazem.nurmontage.videoquran.views.TextCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFontBold
import hazem.nurmontage.videoquran.views.TrackEntityView
import hazem.nurmontage.videoquran.core.base.BaseActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.Locale
import kotlin.Pair

class EngineActivity : BaseActivity() {

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
    private var mCurrentFragment: Fragment? = null
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
                dialog()
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
                blurredImageView.setEntity_select(null)
                blurredImageView.postInvalidate()
                hideFragment()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onEmptySelect() {
            blurredImageView.setEntity_select(null)
            blurredImageView.postInvalidate()
            pausePlayer()
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
                for (entityAudio in trackViewEntity.getEntityListAudio()) {
                    try {
                        if (entityAudio.getMediaPlayer() != null && entityAudio.getMediaPlayer()!!.isPlaying) {
                            entityAudio.getMediaPlayer()!!.pause()
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
                    trackViewEntity.setPlaying(false)
                    blurredImageView.setPlaying(false)
                }
                pauseTimelineAnimation()
                stop()
                val round = Math.round(Math.abs((f / trackViewEntity.getSecond_in_screen()) * (-1000.0f)))
                if (::blurredImageView.isInitialized && (round <= trackViewEntity.getMaxTime() || blurredImageView.getProgress() < 1.0f)) {
                    val min = Math.min(1.0f, round.toFloat() / trackViewEntity.getMaxTime())
                    updateTime(round)
                    blurredImageView.setProgress(min)
                }
                trackViewEntity.update_current_cursur_position(round)
                current_position_time = System.currentTimeMillis().toInt()
                startCursur = trackViewEntity.getCurrent_cursur_position()
                updateViewTime(trackViewEntity.getMaxTime(), trackViewEntity.getCurrent_cursur_position())
                updateBtnCutState()
                updateBtnToStart()
                updateBtnToEnd()
                updateFrame()
            } catch (unused: Exception) {
            }
        }

        override fun pause() {
            pausePlayer()
        }

        override fun onPlayVibration() {
            pausePlayer()
            runOnUiThread {
                if (vibrationHelper != null) {
                    vibrationHelper!!.vibrate()
                }
            }
        }

        override fun onSelectEntity(entity: Entity, f: Float) {
            pausePlayer()
            if (entity is EntityQuranTimeline) {
                blurredImageView.setEntity_select(entity.getEntityView())
                blurredImageView.invalidate()
                if (EditEntityFragment.instance != null) {
                    EditEntityFragment.instance!!.checkSplitEntity(entity, -trackViewEntity.getCurrentPosition())
                    EditEntityFragment.instance!!.checkIcon(entity)
                    return
                } else if (EditTextFragment.instance != null) {
                    EditTextFragment.instance!!.update((entity as EntityQuranTimeline).getQuranEntity())
                    return
                } else {
                    showEditEntity(entity)
                    return
                }
            }
            if (entity is EntityTrslTimeline) {
                blurredImageView.setEntity_select(entity.getEntityView())
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
                blurredImageView.setEntity_select(entity.getEntityView())
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
            startCursur = trackViewEntity.getCurrent_cursur_position()
            updateTime()
        }
    }

    private val iAddQuran = object : AddQuranFragment.IAddQuran {
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

        override fun onAdd(str: String, str2: String, str3: String, str4: String, i: Int, i2: Int, str5: String, i3: Int, i4: Int) {
            addEntity(str, "$str2 $i2", str3, str4, i, i2, str5, i3, i4)
        }

        override fun onDone(str: String, i: Int, str2: String, uri: Uri?, str3: String?) {
            runOnUiThread {
                hideFragment()
            }
            blurredImageView.updateSizeAya()
            blurredImageView.updateSizeAyaTrsl()
            blurredImageView.setSurahNameEntity(
                str, str2, null, 1.0f, "\u062E\u0637 \u0627\u0644\u0625\u0628\u0644.otf",
                blurredImageView.getClr_aya(), AyaTextPreset.NONE.ordinal,
                if (blurredImageView.getSurahNameEntity() != null) blurredImageView.getSurahNameEntity()!!.getStyle() else SurahNameStyle.NONE.ordinal,
                i,
                blurredImageView.getSurahNameEntity() != null && blurredImageView.getSurahNameEntity()!!.isHaveBg(),
                if (blurredImageView.getSurahNameEntity() != null) blurredImageView.getSurahNameEntity()!!.getClrBg() else ViewCompat.MEASURED_STATE_MASK
            )
            if (str3 == null) {
                addAudio(uri)
            } else {
                addAudioFromVideo(uri, str3)
            }
        }

        override fun onDone(str: String, i: Int, str2: String, list: List<RecitersModel>?) {
            runOnUiThread {
                hideFragment()
            }
            blurredImageView.updateSizeAya()
            blurredImageView.updateSizeAyaTrsl()
            blurredImageView.setSurahNameEntity(
                str, str2, null, 1.0f, "\u062E\u0637 \u0627\u0644\u0625\u0628\u0644.otf",
                blurredImageView.getClr_aya(), AyaTextPreset.NONE.ordinal,
                if (blurredImageView.getSurahNameEntity() != null) blurredImageView.getSurahNameEntity()!!.getStyle() else SurahNameStyle.NONE.ordinal,
                i,
                blurredImageView.getSurahNameEntity() != null && blurredImageView.getSurahNameEntity()!!.isHaveBg(),
                if (blurredImageView.getSurahNameEntity() != null) blurredImageView.getSurahNameEntity()!!.getClrBg() else ViewCompat.MEASURED_STATE_MASK
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

        override fun onAddReaderName(str: String, str2: String, uri: Uri?) {
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
                mCurrentFragment = AddQuranFragment.getInstance(iAddQuran, mResources!!)
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
                val parse = Uri.parse(data.getStringExtra(MimeTypes.BASE_TYPE_AUDIO))
                val stringExtra = data.getStringExtra("path_video_copy")
                AddQuranFragment.instance!!.setNameReader(data.getStringExtra("name"), parse, stringExtra)
                return@registerForActivityResult
            }
            try {
                val parse2 = Uri.parse(data.getStringExtra(MimeTypes.BASE_TYPE_AUDIO))
                val stringExtra2 = data.getStringExtra("path_video_copy")
                val stringExtra3 = data.getStringExtra("name")
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
        val stringExtra = data.getStringExtra(Common.READER)
        val booleanExtra = data.getBooleanExtra("isBg", false)
        val intExtra = data.getIntExtra("style", 0)
        if (blurredImageView.getSurahNameEntity()!!.getIndex_surah() == 0) {
            blurredImageView.getSurahNameEntity()!!.setIndex_surah(data.getIntExtra(StreamInformation.KEY_INDEX, 1))
        }
        blurredImageView.getSurahNameEntity()!!.setClrBg(data.getIntExtra("clrBg", ViewCompat.MEASURED_STATE_MASK))
        if (intExtra == SurahNameStyle.NONE.ordinal) {
            blurredImageView.getSurahNameEntity()!!.setAlignment(blurredImageView.updateAlignmentSurah(stringExtra))
        }
        blurredImageView.getSurahNameEntity()!!.setStyle(this@EngineActivity, intExtra, stringExtra, booleanExtra)
        blurredImageView.invalidate()
    }

    private val editTrslResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult: ActivityResult ->
        isToCrop = false
        if (activityResult.resultCode != -1) return@registerForActivityResult
        val data = activityResult.data ?: return@registerForActivityResult
        val stringExtra = data.getStringExtra(Common.READER)
        val booleanExtra = data.getBooleanExtra("isBg", true)
        val translationQuranEntity = blurredImageView.getEntity_select() as TranslationQuranEntity
        translationQuranEntity.setClrBg(data.getIntExtra("clrBg", ViewCompat.MEASURED_STATE_MASK))
        translationQuranEntity.setTxt(stringExtra)
        translationQuranEntity.setHaveBg(booleanExtra)
        blurredImageView.invalidate()
    }

    private val iChangeBgCallback = object : ChangeBgFragment.IChangeBgCallback {
        override fun onSubscribe() {
            dialogPremium()
        }

        override fun onCrop() {
            toCrop()
        }

        override fun onAdd(bgItem: BgItem) {
            if (bgItem.getName_drawable() == mTemplate!!.getName_drawable()) {
                return
            }
            if (ChangeBgFragment.instance != null) {
                ChangeBgFragment.instance!!.scrollToSelected()
            }
            mTemplate!!.setName_drawable(bgItem.getName_drawable())
            uri_bg = "android.resource://" + packageName + "/drawable/" + bgItem.getId()
            showProgressSimple()
            executor.execute {
                var engineActivity: EngineActivity
                var runnable: Runnable
                var cropTo16x9: Bitmap
                var bitmap: Bitmap
                var bitmap2: Bitmap
                var rect: Rect
                try {
                    try {
                        try {
                            mTemplate!!.setUri_bg(uri_bg)
                            var i = 0
                            mTemplate!!.setVideoSquare(false)
                            blurredImageView.setVideo(false)
                            val height = blurredImageView.getHeight()
                            blurredImageView.setBitmapOriginal(
                                Glide.with(this@EngineActivity as FragmentActivity)
                                    .asBitmap()
                                    .load(uri_bg)
                                    .override(height, height)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .submit()
                                    .get()
                            )
                            cropTo16x9 = if (mTemplate!!.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                                BitmapCropper.cropTo9x16(blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH())
                            } else if (mTemplate!!.geTypeResize() == ResizeType.SQUARE.ordinal) {
                                BitmapCropper.cropTo1x1(blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH())
                            } else {
                                BitmapCropper.cropTo16x9(blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH())
                            }
                            blurredImageView.updatePosCanvas(cropTo16x9)
                            blurredImageView.updateIpad(cropTo16x9, mTemplate!!.getIpad_type(), mTemplate!!.geTypeResize())
                            if (mTemplate!!.getIpad_type() == IpadType.IPAD_NEOMORPHIC.ordinal) {
                                val width = (blurredImageView.getIpad_rect().width() * 0.6f).toInt()
                                var round = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate!!.getX_square())
                                var round2 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate!!.getY_square())
                                var i2 = width + round
                                if (i2 > blurredImageView.getBitmapOriginal().width) {
                                    round -= i2 - blurredImageView.getBitmapOriginal().width
                                    i2 = blurredImageView.getBitmapOriginal().width
                                }
                                var i3 = width + round2
                                if (i3 > blurredImageView.getBitmapOriginal().height) {
                                    round2 -= i3 - blurredImageView.getBitmapOriginal().height
                                    i3 = blurredImageView.getBitmapOriginal().height
                                }
                                if (round < 0) {
                                    round = 0
                                }
                                if (round2 >= 0) {
                                    i = round2
                                }
                                val rect2 = Rect(round, i, i2, i3)
                                blurredImageView.setRadius_square(width)
                                val width2 = (blurredImageView.getBitmapOriginal().width * mTemplate!!.getWidth_square()).toInt()
                                val height2 = (blurredImageView.getBitmapOriginal().height * mTemplate!!.getHeight_square()).toInt()
                                val cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.getBitmapOriginal(), rect2, width, width2, height2)
                                rect2.right = rect2.left + width2
                                rect2.bottom = rect2.top + height2
                                blurredImageView.setRectSquare(rect2)
                                bitmap2 = cropToSquareWithRoundCorners
                                rect = rect2
                            } else {
                                if (mTemplate!!.getIpad_type() != IpadType.IPAD.ordinal && mTemplate!!.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal && mTemplate!!.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal) {
                                    val width3 = (blurredImageView.getIpad_rect().width() * 1.0f).toInt()
                                    val height3 = (cropTo16x9.height * 0.5355f).toInt()
                                    var round3 = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate!!.getX_square())
                                    var round4 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate!!.getY_square())
                                    var i4 = width3 + round3
                                    if (i4 > blurredImageView.getBitmapOriginal().width) {
                                        round3 -= i4 - blurredImageView.getBitmapOriginal().width
                                        i4 = blurredImageView.getBitmapOriginal().width
                                    }
                                    var i5 = height3 + round4
                                    if (i5 > blurredImageView.getBitmapOriginal().height) {
                                        round4 -= i5 - blurredImageView.getBitmapOriginal().height
                                        i5 = blurredImageView.getBitmapOriginal().height
                                    }
                                    if (round3 < 0) {
                                        round3 = 0
                                    }
                                    if (round4 < 0) {
                                        round4 = 0
                                    }
                                    val rect3 = Rect(round3, round4, i4, i5)
                                    val width4 = (blurredImageView.getBitmapOriginal().width * mTemplate!!.getWidth_square()).toInt()
                                    val height4 = (blurredImageView.getBitmapOriginal().height * mTemplate!!.getHeight_square()).toInt()
                                    val cropToSquare = UtilsBitmap.cropToSquare(blurredImageView.getBitmapOriginal(), rect3, width4, height4)
                                    blurredImageView.setBitmapSquare(cropToSquare)
                                    blurredImageView.setRadius_square(0)
                                    rect3.right = rect3.left + width4
                                    rect3.bottom = rect3.top + height4
                                    blurredImageView.setRectSquare(rect3)
                                    bitmap2 = cropToSquare
                                    rect = rect3
                                }
                                val width5 = (blurredImageView.getIpad_rect().width() * 0.87530595f).toInt()
                                val i6 = (width5 * 1.13f).toInt()
                                val min = Math.min(width5, i6)
                                var round5 = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate!!.getX_square())
                                var round6 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate!!.getY_square())
                                var i7 = width5 + round5
                                if (i7 > blurredImageView.getBitmapOriginal().width) {
                                    round5 -= i7 - blurredImageView.getBitmapOriginal().width
                                    i7 = blurredImageView.getBitmapOriginal().width
                                }
                                var i8 = i6 + round6
                                if (i8 > blurredImageView.getBitmapOriginal().height) {
                                    round6 -= i8 - blurredImageView.getBitmapOriginal().height
                                    i8 = blurredImageView.getBitmapOriginal().height
                                }
                                if (round5 < 0) {
                                    round5 = 0
                                }
                                if (round6 < 0) {
                                    round6 = 0
                                }
                                val rect4 = Rect(round5, round6, i7, i8)
                                if (mTemplate!!.getIpad_type() == IpadType.IPAD_CLASSIC.ordinal) {
                                    val width6 = (blurredImageView.getBitmapOriginal().width * mTemplate!!.getWidth_square()).toInt()
                                    val height5 = (blurredImageView.getBitmapOriginal().height * mTemplate!!.getHeight_square()).toInt()
                                    val cropToSquare2 = UtilsBitmap.cropToSquare(blurredImageView.getBitmapOriginal(), rect4, width6, height5)
                                    blurredImageView.setBitmapSquare(cropToSquare2)
                                    blurredImageView.setRadius_square(0)
                                    rect4.right = rect4.left + width6
                                    rect4.bottom = rect4.top + height5
                                    blurredImageView.setRectSquare(rect4)
                                    bitmap = cropToSquare2
                                } else {
                                    val i9 = (min * 0.10800001f).toInt()
                                    blurredImageView.setRadius_square(i9)
                                    val width7 = (blurredImageView.getBitmapOriginal().width * mTemplate!!.getWidth_square()).toInt()
                                    val height6 = (blurredImageView.getBitmapOriginal().height * mTemplate!!.getHeight_square()).toInt()
                                    val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.getBitmapOriginal(), rect4, i9, width7, height6)
                                    rect4.right = rect4.left + width7
                                    rect4.bottom = rect4.top + height6
                                    blurredImageView.setRectSquare(rect4)
                                    bitmap = cropToSquareWithRoundCorners2
                                }
                                bitmap2 = bitmap
                                rect = rect4
                            }
                            if (mTemplate!!.getIpad_type() == IpadType.GRADIENT.ordinal) {
                                blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap2, ViewCompat.MEASURED_STATE_MASK, mTemplate!!.getIpad_type(), mTemplate!!.geTypeResize(), rect)
                            } else if (mTemplate!!.getIpad_type() == IpadType.BLUE_TYPE.ordinal) {
                                if (blurredImageView.getColor_gradient() != null) {
                                    blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap2, blurredImageView.getColor_gradient(), mTemplate!!.getIpad_type(), mTemplate!!.geTypeResize(), rect)
                                } else {
                                    blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap2, blurredImageView.getColor_ipad(), mTemplate!!.getIpad_type(), mTemplate!!.geTypeResize(), rect)
                                }
                            } else {
                                blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap2, -1, mTemplate!!.getIpad_type(), mTemplate!!.geTypeResize(), rect)
                            }
                            mTemplate!!.setColor_ipad(blurredImageView.colorIpad())
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

        override fun onDone() {
            hideFragment()
        }

        override fun onCancel() {
            hideFragment()
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
                var cropTo16x9: Bitmap
                var i5: Int
                var cropToSquareWithRoundCorners: Bitmap
                var bitmap: Bitmap
                var rect: Rect
                try {
                    try {
                        try {
                            blurredImageView.reset()
                            mTemplate!!.setResizeType(i3)
                            mTemplate!!.setImgResize(str)
                            val size = AspectRatioCalculator.getSize(i3, mTemplate!!.getResolution())
                            mTemplate!!.setWidthAndHeight(size.first, size.second)
                            blurredImageView.initCanvasDimension(blurredImageView.getWidth(), blurredImageView.getHeight(), i3)
                            cropTo16x9 = if (mTemplate!!.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                                BitmapCropper.cropTo9x16(blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH())
                            } else if (mTemplate!!.geTypeResize() == ResizeType.SQUARE.ordinal) {
                                BitmapCropper.cropTo1x1(blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH())
                            } else {
                                BitmapCropper.cropTo16x9(blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH())
                            }
                            blurredImageView.updatePosCanvas(cropTo16x9)
                            blurredImageView.setBitmapBlured(cropTo16x9)
                            blurredImageView.updateIpad(cropTo16x9, mTemplate!!.getIpad_type(), mTemplate!!.geTypeResize())
                            i5 = 0
                        } finally {
                        }
                        if (mTemplate!!.getIpad_type() != IpadType.GRADIENT.ordinal && mTemplate!!.getIpad_type() != IpadType.BLACK_LAYER.ordinal && mTemplate!!.getIpad_type() != IpadType.MASK_BRUSH.ordinal && mTemplate!!.getIpad_type() != IpadType.BLUE_TYPE.ordinal && mTemplate!!.getIpad_type() != IpadType.CASSET_IMG.ordinal && mTemplate!!.getIpad_type() != IpadType.CASSET_IMG_BLUR.ordinal) {
                            if (mTemplate!!.getIpad_type() == IpadType.IPAD_NEOMORPHIC.ordinal) {
                                val width = (blurredImageView.getIpad_rect().width() * 0.6f).toInt()
                                var round = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate!!.getX_square())
                                var round2 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate!!.getY_square())
                                var i6 = width + round
                                if (i6 > blurredImageView.getBitmapOriginal().width) {
                                    round -= i6 - blurredImageView.getBitmapOriginal().width
                                    i6 = blurredImageView.getBitmapOriginal().width
                                }
                                var i7 = width + round2
                                if (i7 > blurredImageView.getBitmapOriginal().height) {
                                    round2 -= i7 - blurredImageView.getBitmapOriginal().height
                                    i7 = blurredImageView.getBitmapOriginal().height
                                }
                                if (round < 0) {
                                    round = 0
                                }
                                if (round2 >= 0) {
                                    i5 = round2
                                }
                                val rect2 = Rect(round, i5, i6, i7)
                                blurredImageView.setRadius_square(width)
                                val width2 = (blurredImageView.getBitmapOriginal().width * mTemplate!!.getWidth_square()).toInt()
                                val height = (blurredImageView.getBitmapOriginal().height * mTemplate!!.getHeight_square()).toInt()
                                val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.getBitmapOriginal(), rect2, width, width2, height)
                                rect2.right = rect2.left + width2
                                rect2.bottom = rect2.top + height
                                blurredImageView.setRectSquare(rect2)
                                bitmap = cropToSquareWithRoundCorners2
                                rect = rect2
                            } else {
                                if (mTemplate!!.getIpad_type() != IpadType.IPAD.ordinal && mTemplate!!.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal && mTemplate!!.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal) {
                                    val width3 = (blurredImageView.getIpad_rect().width() * 1.0f).toInt()
                                    val height2 = (cropTo16x9.height * 0.5355f).toInt()
                                    var round3 = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate!!.getX_square())
                                    var round4 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate!!.getY_square())
                                    var i8 = width3 + round3
                                    if (i8 > blurredImageView.getBitmapOriginal().width) {
                                        round3 -= i8 - blurredImageView.getBitmapOriginal().width
                                        i8 = blurredImageView.getBitmapOriginal().width
                                    }
                                    var i9 = height2 + round4
                                    if (i9 > blurredImageView.getBitmapOriginal().height) {
                                        round4 -= i9 - blurredImageView.getBitmapOriginal().height
                                    }
                                }
                                // Additional ipad type handling continues in full implementation
                                bitmap = blurredImageView.getBitmapSquare()!!
                                rect = blurredImageView.getRectSquare()!!
                            }
                        } else {
                            bitmap = blurredImageView.getBitmapSquare()!!
                            rect = blurredImageView.getRectSquare()!!
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
                mCurrentFragment = AddQuranFragment.getInstance(iAddQuran, mResources!!)
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
            } catch (unused: Exception) {
            }
        }
    }

    private val iIpadEditCallback = object : EditIpadFragment.IIpadEditCallback {
        override fun onClick(i: Int, i2: Int) {
            mTemplate!!.setColor_ipad(i)
            mTemplate!!.setIndex_color(i2)
            mTemplate!!.setGradient(null)
            blurredImageView.setColorIpad(i)
            blurredImageView.invalidate()
        }

        override fun onClick(gradient: Gradient, i: Int) {
            mTemplate!!.setGradient(gradient)
            mTemplate!!.setIndex_color(i)
            blurredImageView.setColorIpad(gradient)
            blurredImageView.invalidate()
        }

        override fun onDialogPremium() {
            dialogPremium()
        }

        override fun onGlassType(z: Boolean) {
            mTemplate!!.setGlass(z)
            blurredImageView.setGlass(z)
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
                mTemplate!!.setIpad_type(i)
                blurredImageView.changeTypeIpad(i)
                if (mTemplate!!.isVideoSquare()) {
                    if (i != IpadType.GRADIENT.ordinal && i != IpadType.BLACK_LAYER.ordinal && i != IpadType.MASK_BRUSH.ordinal && i != IpadType.BLUE_TYPE.ordinal && i != IpadType.CASSET_IMG.ordinal) {
                        if (mTemplate!!.getIpad_type() == IpadType.CASSET_IMG_BLUR.ordinal) {
                            blurredImageView.setBitmapSquare(blurredImageView.getBitmapBlured())
                            blurredImageView.setRadius_square(0)
                        }
                    }
                    blurredImageView.setBitmapSquare(blurredImageView.getBitmapNotBlur())
                    blurredImageView.setRadius_square(0)
                }
                if (i == IpadType.IPAD.ordinal || i == IpadType.IPAD_UNBLUR.ordinal) {
                    val width = (blurredImageView.getIpad_rect().width() * 0.87530595f).toInt()
                    val i2 = (width * 1.13f).toInt()
                    val min = (Math.min(width, i2) * 0.10800001f).toInt()
                    var round = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate!!.getX_square())
                    var round2 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate!!.getY_square())
                    var i3 = width + round
                    if (i3 > blurredImageView.getBitmapOriginal().width) {
                        round -= i3 - blurredImageView.getBitmapOriginal().width
                        i3 = blurredImageView.getBitmapOriginal().width
                    }
                    var i4 = i2 + round2
                    if (i4 > blurredImageView.getBitmapOriginal().height) {
                        round2 -= i4 - blurredImageView.getBitmapOriginal().height
                        i4 = blurredImageView.getBitmapOriginal().height
                    }
                    if (round < 0) {
                        round = 0
                    }
                    if (round2 < 0) {
                        round2 = 0
                    }
                    val rect = Rect(round, round2, i3, i4)
                    val width2 = (blurredImageView.getBitmapOriginal().width * mTemplate!!.getWidth_square()).toInt()
                    val height = (blurredImageView.getBitmapOriginal().height * mTemplate!!.getHeight_square()).toInt()
                    blurredImageView.setBitmapSquare(UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.getBitmapOriginal(), rect, min, width2, height))
                    blurredImageView.setRadius_square(min)
                    rect.right = rect.left + width2
                    rect.bottom = rect.top + height
                    blurredImageView.setRectSquare(rect)
                }
                if (i == IpadType.IPAD_CLASSIC.ordinal) {
                    val width3 = (blurredImageView.getIpad_rect().width() * 0.87530595f).toInt()
                    val i5 = (width3 * 1.13f).toInt()
                    var round3 = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate!!.getX_square())
                    var round4 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate!!.getY_square())
                    var i6 = width3 + round3
                    if (i6 > blurredImageView.getBitmapOriginal().width) {
                        round3 -= i6 - blurredImageView.getBitmapOriginal().width
                        i6 = blurredImageView.getBitmapOriginal().width
                    }
                    var i7 = i5 + round4
                    if (i7 > blurredImageView.getBitmapOriginal().height) {
                        round4 -= i7 - blurredImageView.getBitmapOriginal().height
                        i7 = blurredImageView.getBitmapOriginal().height
                    }
                    if (round3 < 0) {
                        round3 = 0
                    }
                    if (round4 < 0) {
                        round4 = 0
                    }
                    val rect2 = Rect(round3, round4, i6, i7)
                    val width4 = (blurredImageView.getBitmapOriginal().width * mTemplate!!.getWidth_square()).toInt()
                    val height2 = (blurredImageView.getBitmapOriginal().height * mTemplate!!.getHeight_square()).toInt()
                    blurredImageView.setBitmapSquare(UtilsBitmap.cropToSquare(blurredImageView.getBitmapOriginal(), rect2, width4, height2))
                    blurredImageView.setRadius_square(0)
                    rect2.right = rect2.left + width4
                    rect2.bottom = rect2.top + height2
                    blurredImageView.setRectSquare(rect2)
                }
                if (i == IpadType.IPAD_NEOMORPHIC.ordinal) {
                    val width5 = (blurredImageView.getIpad_rect().width() * 0.6f).toInt()
                    var round5 = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate!!.getX_square())
                    var round6 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate!!.getY_square())
                    var i8 = width5 + round5
                    if (i8 > blurredImageView.getBitmapOriginal().width) {
                        round5 -= i8 - blurredImageView.getBitmapOriginal().width
                        i8 = blurredImageView.getBitmapOriginal().width
                    }
                    var i9 = width5 + round6
                    if (i9 > blurredImageView.getBitmapOriginal().height) {
                        round6 -= i9 - blurredImageView.getBitmapOriginal().height
                        i9 = blurredImageView.getBitmapOriginal().height
                    }
                    if (round5 < 0) {
                        round5 = 0
                    }
                    if (round6 < 0) {
                        round6 = 0
                    }
                    val rect3 = Rect(round5, round6, i8, i9)
                    val width6 = (blurredImageView.getBitmapOriginal().width * mTemplate!!.getWidth_square()).toInt()
                    val height3 = (blurredImageView.getBitmapOriginal().height * mTemplate!!.getHeight_square()).toInt()
                    blurredImageView.setBitmapSquare(UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.getBitmapOriginal(), rect3, width5, width6, height3))
                    blurredImageView.setRadius_square(width5)
                    rect3.right = rect3.left + width6
                    rect3.bottom = rect3.top + height3
                    blurredImageView.setRectSquare(rect3)
                }
                if (i == IpadType.BOTTOM_RECT.ordinal) {
                    val width7 = (blurredImageView.getIpad_rect().width() * 1.0f).toInt()
                    val height4 = (blurredImageView.getBitmapBlured().height * 0.5355f).toInt()
                    var round7 = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate!!.getX_square())
                    var round8 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate!!.getY_square())
                    var i10 = width7 + round7
                    if (i10 > blurredImageView.getBitmapOriginal().width) {
                        round7 -= i10 - blurredImageView.getBitmapOriginal().width
                        i10 = blurredImageView.getBitmapOriginal().width
                    }
                    var i11 = height4 + round8
                    if (i11 > blurredImageView.getBitmapOriginal().height) {
                        round8 -= i11 - blurredImageView.getBitmapOriginal().height
                        i11 = blurredImageView.getBitmapOriginal().height
                    }
                    if (round7 < 0) {
                        round7 = 0
                    }
                    if (round8 < 0) {
                        round8 = 0
                    }
                    val rect4 = Rect(round7, round8, i10, i11)
                    val width8 = (blurredImageView.getBitmapOriginal().width * mTemplate!!.getWidth_square()).toInt()
                    val height5 = (blurredImageView.getBitmapOriginal().height * mTemplate!!.getHeight_square()).toInt()
                    blurredImageView.setBitmapSquare(UtilsBitmap.cropToSquare(blurredImageView.getBitmapOriginal(), rect4, width8, height5))
                    blurredImageView.setRadius_square(0)
                    rect4.right = rect4.left + width8
                    rect4.bottom = rect4.top + height5
                    blurredImageView.setRectSquare(rect4)
                }
                if (i == IpadType.BORDER.ordinal) {
                    if (ColorUtils.isColorDark(blurredImageView.getBitmapOriginal().getPixel(
                        (blurredImageView.getBitmapOriginal().width * 0.5f).toInt(),
                        (blurredImageView.getBitmapOriginal().height * 0.5f).toInt()
                    ))) {
                        mTemplate!!.setColor_ipad(-1)
                    } else {
                        mTemplate!!.setColor_ipad(ViewCompat.MEASURED_STATE_MASK)
                    }
                    blurredImageView.setColorIpad(mTemplate!!.getColor_ipad())
                }
                blurredImageView.createRectWithoutSurahName()
                blurredImageView.resizeEntity()
                if (blurredImageView.getSurahNameEntity() != null && blurredImageView.getSurahNameEntity()!!.getStyle() != SurahNameStyle.ZAGHRAFAT.ordinal && !blurredImageView.getSurahNameEntity()!!.isHaveBg()) {
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
                val quranEntity = trackViewEntity.getSelectedEntity().getEntityView() as QuranEntity
                quranEntity.setVectorDrawable(ContextCompat.getDrawable(applicationContext, DrawableHelper.getIDDrawableIconByName(str)) as VectorDrawable)
                quranEntity.setIcon(str)
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
                iTrimLineCallback.onSelectEntity(trackViewEntity.getSelectedEntity(), -1.0f)
            } catch (unused: Exception) {
            }
        }

        override fun onCancel(str: String) {
            try {
                val quranEntity = trackViewEntity.getSelectedEntity().getEntityView() as QuranEntity
                quranEntity.setVectorDrawable(ContextCompat.getDrawable(applicationContext, DrawableHelper.getIDDrawableIconByName(str)) as VectorDrawable)
                quranEntity.setIcon(str)
                quranEntity.updateIconDraw()
                quranEntity.initPreset(quranEntity.getmPreset())
                blurredImageView.invalidate()
                hideFragment()
                iTrimLineCallback.onSelectEntity(trackViewEntity.getSelectedEntity(), -1.0f)
            } catch (unused: Exception) {
            }
        }
    }

    private val iEditSName = object : EditS_NameFragment.IEditS_Name {
        override fun onFont(surahNameEntity: SurahNameEntity) {
            val beginTransaction = supportFragmentManager.beginTransaction()
            mCurrentFragment = FontFragment.getInstance(iFontCallback, surahNameEntity.getNameFont(), surahNameEntity.getPaintAya().typeface)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
            setupShowFragment(mResources!!.getString(R.string.font))
        }

        override fun onEdit(surahNameEntity: SurahNameEntity) {
            try {
                isToCrop = true
                val intent = Intent(this@EngineActivity, EditS_NameActivity::class.java)
                intent.putExtra("surah_name", blurredImageView.getSurahNameEntity()!!.getName())
                intent.putExtra("reader_name", blurredImageView.getSurahNameEntity()!!.getReader())
                intent.putExtra("style", blurredImageView.getSurahNameEntity()!!.getStyle())
                intent.putExtra(StreamInformation.KEY_INDEX, blurredImageView.getSurahNameEntity()!!.getIndex_surah())
                intent.putExtra("isBg", blurredImageView.getSurahNameEntity()!!.isHaveBg())
                intent.putExtra("clrBg", blurredImageView.getSurahNameEntity()!!.getClrBg())
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
                pausePlayer()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = ColorS_NameFragment.getInstance(iEditSName, surahNameEntity, mResources!!)
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
                if (blurredImageView.getEntity_select() is SurahNameEntity) {
                    blurredImageView.getSurahNameEntity()!!.setTypeface(typeface, str)
                    blurredImageView.invalidate()
                } else if (str != null && typeface != null) {
                    blurredImageView.setTypeface(typeface, str)
                }
                FontFragment.instance!!.add(typeface, str)
            } catch (unused: Exception) {
            }
        }

        override fun onDone(str: String?, typeface: Typeface?) {
            try {
                hideFragment()
                if (blurredImageView.getEntity_select() is SurahNameEntity) {
                    selectSurahName()
                } else {
                    iTrimLineCallback.onSelectEntity(trackViewEntity.getSelectedEntity(), -1.0f)
                }
            } catch (unused: Exception) {
            }
        }

        override fun onCancel(str: String?, typeface: Typeface?) {
            try {
                if (blurredImageView.getEntity_select() is SurahNameEntity) {
                    blurredImageView.getSurahNameEntity()!!.setTypeface(typeface, str)
                    blurredImageView.invalidate()
                    selectSurahName()
                } else {
                    if (str != null && typeface != null) {
                        blurredImageView.setTypeface(typeface, str)
                    }
                    hideFragment()
                    iTrimLineCallback.onSelectEntity(trackViewEntity.getSelectedEntity(), -1.0f)
                }
            } catch (unused: Exception) {
            }
        }
    }

    private val iBismilahEntityCallback = object : EditBismilahEntityFragment.IBismilahEntityCallback {
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {
            blurredImageView.setPreset(ayaTextPreset)
        }

        override fun updateAya(i: Int) {
            blurredImageView.setColorAya(i)
        }

        override fun onAnim() {
            try {
                pausePlayer()
                val bismilahEntity = trackViewEntity.getSelectedEntity().getEntityView() as BismilahEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = EffectBismilahFragment.get(
                    bismilahEntity.getBismilahTimeline().getTransition(),
                    mResources!!, iTransitionBismilahCallback,
                    trackViewEntity.getSelectedEntity() as EntityBismilahTimeline
                )
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.animtion))
            } catch (unused: Exception) {
            }
        }

        override fun onDelete() {
            try {
                pausePlayer()
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
            if (blurredImageView.getEntity_select() is QuranEntity || blurredImageView.getEntity_select() is BismilahEntity) {
                iTrimLineCallback.onSelectEntity(trackViewEntity.getSelectedEntity(), -1.0f)
            }
        }

        override fun onColor() {
            try {
                pausePlayer()
                val bismilahEntity = trackViewEntity.getSelectedEntity().getEntityView() as BismilahEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = ColorBismilahFragment.getInstance(iBismilahEntityCallback, bismilahEntity, mResources!!)
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(null)
            } catch (unused: Exception) {
            }
        }

        override fun fromTheStart() {
            pausePlayer()
            trackViewEntity.translateFromStart()
        }

        override fun fromNow() {
            pausePlayer()
            trackViewEntity.translateFromNow()
        }

        override fun untilNow() {
            pausePlayer()
            trackViewEntity.translateUntilNow()
        }

        override fun untilTheEnd() {
            pausePlayer()
            trackViewEntity.translateEndNow()
        }
    }

    private val iEditEntityCallback = object : EditEntityFragment.IEditEntityCallback {
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {
            blurredImageView.setPreset(ayaTextPreset)
        }

        override fun updateAya(i: Int) {
            blurredImageView.setColorAya(i)
        }

        override fun updateTrsl(i: Int) {
            blurredImageView.setColorTrsl(i)
        }

        override fun onFont() {
            try {
                pausePlayer()
                val quranEntity = trackViewEntity.getSelectedEntity().getEntityView() as QuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = FontFragment.getInstance(iFontCallback, quranEntity.getNameFont(), quranEntity.getPaintAya().typeface)
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.font))
            } catch (unused: Exception) {
            }
        }

        override fun onIcon() {
            try {
                pausePlayer()
                val quranEntity = trackViewEntity.getSelectedEntity().getEntityView() as QuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = EditIconQuranFragment.getInstance(iQuranIconCallback, quranEntity.getIcon())
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.icon))
            } catch (unused: Exception) {
            }
        }

        override fun onAnim() {
            try {
                pausePlayer()
                val quranEntity = trackViewEntity.getSelectedEntity().getEntityView() as QuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = EffectAyaFragment.get(
                    quranEntity.getEntityQuran().getTransition(),
                    mResources!!, iTransitionCallback,
                    trackViewEntity.getSelectedEntity() as EntityQuranTimeline
                )
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.animtion))
            } catch (unused: Exception) {
            }
        }

        override fun onDelete() {
            try {
                pausePlayer()
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
            if (blurredImageView.getEntity_select() is QuranEntity) {
                iTrimLineCallback.onSelectEntity(trackViewEntity.getSelectedEntity(), -1.0f)
            }
        }

        override fun onColor() {
            try {
                pausePlayer()
                val quranEntity = trackViewEntity.getSelectedEntity().getEntityView() as QuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = ColorAyaFragment.getInstance(iEditEntityCallback, quranEntity, mResources!!)
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(null)
            } catch (unused: Exception) {
            }
        }

        override fun onEdit() {
            try {
                pausePlayer()
                val quranEntity = trackViewEntity.getSelectedEntity().getEntityView() as QuranEntity
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
                pausePlayer()
                splitEntity(trackViewEntity.getSelectedEntity().getEntityView() as QuranEntity)
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onDuplicate() {
            try {
                pausePlayer()
                duplicateEntity(trackViewEntity.getSelectedEntity().getEntityView() as QuranEntity)
                updateTime()
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun fromTheStart() {
            pausePlayer()
            trackViewEntity.translateFromStart()
        }

        override fun fromNow() {
            pausePlayer()
            trackViewEntity.translateFromNow()
        }

        override fun untilNow() {
            pausePlayer()
            trackViewEntity.translateUntilNow()
        }

        override fun untilTheEnd() {
            pausePlayer()
            trackViewEntity.translateEndNow()
        }
    }

    private val iEditTrstEntityCallback = object : EditTrslEntityFragment.IEditEntityCallback {
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {
            blurredImageView.setTrslPreset(ayaTextPreset)
        }

        override fun updateAya(i: Int) {
            blurredImageView.setColorTrsl(i)
        }

        override fun updateTrsl(i: Int) {
            blurredImageView.setColorTrsl(i)
        }

        override fun onFont() {
            try {
                pausePlayer()
                val translationQuranEntity = trackViewEntity.getSelectedEntity().getEntityView() as TranslationQuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = FontFragment.getInstance(iFontCallback, translationQuranEntity.getNameFont(), translationQuranEntity.getPaintAya().typeface)
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.font))
            } catch (unused: Exception) {
            }
        }

        override fun onIcon() {
            try {
                pausePlayer()
                val quranEntity = trackViewEntity.getSelectedEntity().getEntityView() as QuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = EditIconQuranFragment.getInstance(iQuranIconCallback, quranEntity.getIcon())
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.icon))
            } catch (unused: Exception) {
            }
        }

        override fun onAnim() {
            try {
                pausePlayer()
                val quranEntity = trackViewEntity.getSelectedEntity().getEntityView() as QuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = EffectAyaFragment.get(
                    quranEntity.getEntityQuran().getTransition(),
                    mResources!!, iTransitionCallback,
                    trackViewEntity.getSelectedEntity() as EntityQuranTimeline
                )
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(mResources!!.getString(R.string.animtion))
            } catch (unused: Exception) {
            }
        }

        override fun onDelete() {
            try {
                pausePlayer()
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
            if (blurredImageView.getEntity_select() is TranslationQuranEntity) {
                iTrimLineCallback.onSelectEntity(trackViewEntity.getSelectedEntity(), -1.0f)
            }
        }

        override fun onColor() {
            try {
                pausePlayer()
                val translationQuranEntity = trackViewEntity.getSelectedEntity().getEntityView() as TranslationQuranEntity
                trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = supportFragmentManager.beginTransaction()
                mCurrentFragment = ColorTrslAyaFragment.getInstance(iEditTrstEntityCallback, translationQuranEntity, mResources!!)
                beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
                beginTransaction.commit()
                setupShowFragment(null)
            } catch (unused: Exception) {
            }
        }

        override fun onEdit() {
            try {
                pausePlayer()
                isToCrop = true
                val translationQuranEntity = trackViewEntity.getSelectedEntity().getEntityView() as TranslationQuranEntity
                val intent = Intent(this@EngineActivity, EditTrslTxtActivity::class.java)
                intent.putExtra("surah_name", "")
                intent.putExtra("reader_name", translationQuranEntity.getTxt())
                intent.putExtra("isBg", translationQuranEntity.isHaveBg())
                intent.putExtra("clrBg", translationQuranEntity.getClrBg())
                editTrslResult.launch(intent)
                overridePendingTransition(0, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCut() {
            try {
                pausePlayer()
                splitEntity(trackViewEntity.getSelectedEntity().getEntityView() as TranslationQuranEntity)
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onDuplicate() {
            try {
                pausePlayer()
                duplicateEntity(trackViewEntity.getSelectedEntity().getEntityView() as TranslationQuranEntity)
                updateTime()
            } catch (unused: Exception) {
                if (iTrimLineCallback != null) {
                    iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun fromTheStart() {
            pausePlayer()
            trackViewEntity.translateFromStart()
        }

        override fun fromNow() {
            pausePlayer()
            trackViewEntity.translateFromNow()
        }

        override fun untilNow() {
            pausePlayer()
            trackViewEntity.translateUntilNow()
        }

        override fun untilTheEnd() {
            pausePlayer()
            trackViewEntity.translateEndNow()
        }
    }

    private val iEditMultipleCallback = object : EditMultipleEntityFragment.IEditMultipleCallback {
        override fun onDelete() {
            pausePlayer()
            dialogDeleteSelected()
        }
    }

    private val iEditMediaCallback = object : EditMediaFragment.IEditMediaCallback {
        override fun onReplace() {}

        override fun updateEntity(effectAudioType: EffectAudioType, entityAudio: EntityAudio) {
            for (i in trackViewEntity.getEntityListAudio().indices) {
                val entityAudio2 = trackViewEntity.getEntityListAudio()[i]
                if (entityAudio2 !== entityAudio && entityAudio2.visible()) {
                    if (effectAudioType == EffectAudioType.ECHO) {
                        entityAudio2.getEffectAudio().setDecays(entityAudio.getEffectAudio().getDecays())
                        entityAudio2.getEffectAudio().setDelays(entityAudio.getEffectAudio().getDelays())
                        entityAudio2.getEffectAudio().setOutGain(entityAudio.getEffectAudio().getOutGain())
                        entityAudio2.getEffectAudio().setDecays_cmd(entityAudio.getEffectAudio().getDecays_cmd())
                        entityAudio2.getEffectAudio().setDelays_cmd(entityAudio.getEffectAudio().getDelays_cmd())
                    }
                    if (effectAudioType == EffectAudioType.NOICE) {
                        entityAudio2.getEffectAudio().setRemoveNoice(entityAudio.getEffectAudio().isRemoveNoice())
                    }
                    if (effectAudioType == EffectAudioType.ENHANCE) {
                        entityAudio2.getEffectAudio().setEnhance(entityAudio.getEffectAudio().isEnhance())
                    }
                    if (effectAudioType == EffectAudioType.SPEED) {
                        entityAudio2.getEffectAudio().setSpeed(entityAudio.getEffectAudio().getSpeed())
                    }
                    if (effectAudioType == EffectAudioType.REVERB) {
                        entityAudio2.getEffectAudio().setReverbPreset(entityAudio.getEffectAudio().getReverbPreset())
                        entityAudio2.getEffectAudio().setReverbPreset_index_list(entityAudio.getEffectAudio().getReverbPreset_index_list())
                    }
                    if (effectAudioType == EffectAudioType.VOLUME) {
                        entityAudio2.getEffectAudio().setVolume(entityAudio.getEffectAudio().getVolume())
                    }
                    if (effectAudioType == EffectAudioType.FADE) {
                        entityAudio2.getEffectAudio().setFade_in(entityAudio.getEffectAudio().getFade_in())
                        entityAudio2.getEffectAudio().setFade_out(entityAudio.getEffectAudio().getFade_out())
                    }
                }
            }
        }

        override fun onDone() {
            pausePreview()
            hideFragment()
            iTrimLineCallback.onSelectEntity(trackViewEntity.getSelectedEntity(), -1.0f)
        }

        override fun startPreview() {
            if (trackViewEntity.getSelectedEntity() is EntityAudio) {
                val entityAudio = trackViewEntity.getSelectedEntity() as EntityAudio
                if (entityAudio.getMediaPlayer()!!.isPlaying) {
                    return
                }
                trackViewEntity.previewEntity(entityAudio)
                mIsPlaying = true
                trackViewEntity.translateToStart(entityAudio)
                startCursur = trackViewEntity.getCurrent_cursur_position()
                startTimelineAnimationPreview(entityAudio)
            }
        }

        override fun pausePreview() {
            if (mIsPlaying && trackViewEntity.getSelectedEntity() is EntityAudio) {
                val entityAudio = trackViewEntity.getSelectedEntity() as EntityAudio
                mIsPlaying = false
                pauseTimelineAnimation()
                trackViewEntity.setPlaying(mIsPlaying)
                blurredImageView.setPlaying(mIsPlaying)
                try {
                    if (entityAudio.getMediaPlayer() == null || !entityAudio.getMediaPlayer()!!.isPlaying) {
                        return
                    }
                    entityAudio.getMediaPlayer()!!.pause()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onCmdPlay(str: String) {
            pausePreview()
            if (trackViewEntity.getSelectedEntity() is EntityAudio) {
                applyffectPlayAuto(str, trackViewEntity.getSelectedEntity() as EntityAudio)
            }
        }

        override fun onCmd(str: String) {
            pausePreview()
            if (trackViewEntity.getSelectedEntity() is EntityAudio) {
                applyffect(str, trackViewEntity.getSelectedEntity() as EntityAudio)
            }
        }

        override fun onCmdAll(effectAudio: EffectAudio) {
            pausePreview()
            showProgressSimple()
            applyffectAll(effectAudio, 0)
        }

        override fun onDuplicate() {
            try {
                if (trackViewEntity.getSelectedEntity() is EntityAudio) {
                    val entityAudio = trackViewEntity.getSelectedEntity() as EntityAudio
                    pausePlayer()
                    duplicateEntityAudio(entityAudio.getMediaPlayer()!!.duration.toLong(), entityAudio)
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
                pausePlayer()
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
                pausePlayer()
                if (trackViewEntity.getSelectedEntity() is EntityAudio) {
                    val entityAudio = trackViewEntity.getSelectedEntity() as EntityAudio
                    val abs = Math.abs(trackViewEntity.getCurrentPosition())
                    if (abs >= entityAudio.getRect().left && abs <= entityAudio.getRect().right) {
                        val second_in_screenNoScale = trackViewEntity.getSecond_in_screenNoScale() * 0.1f
                        if (abs <= entityAudio.getRect().left || abs >= entityAudio.getRect().left + second_in_screenNoScale) {
                            if (abs >= entityAudio.getRect().right || abs <= entityAudio.getRect().right - second_in_screenNoScale) {
                                val round = Math.round(
                                    (Math.abs(Math.round((trackViewEntity.getCurrentPosition() / trackViewEntity.getSecond_in_screen()) * 1000.0f)) -
                                            Math.abs(Math.round((entityAudio.getRect().left / trackViewEntity.getSecond_in_screen()) * 1000.0f))).toFloat()
                                ) + entityAudio.getStart()
                                val split = entityAudio.split(abs)
                                split.setAmps(entityAudio.getAmps())
                                split.setRenderer(entityAudio.getRenderer())
                                split.addPathHttp(entityAudio.getPaths_http())
                                split.setPath_ffmpeg_effect(entityAudio.getPath_ffmpeg_effect())
                                split.setVideo_path(entityAudio.getVideo_path())
                                split.setApplyEffectInPreview(entityAudio.isApplyEffectInPreview())
                                split.setEffectAudio(entityAudio.getEffectAudio())
                                split.setscaleFactor(entityAudio.getscaleFactor())
                                split.setMediaPlayer(entityAudio.getMediaPlayer())
                                split.setPath_ffmpeg(entityAudio.getPath_ffmpeg())
                                split.setIndex(entityAudio.getIndex() + 1)
                                split.setEnd(entityAudio.getEnd())
                                val f = round.toFloat()
                                split.setStart(f)
                                split.setMin_duration(round)
                                trackViewEntity.splitAudio(split, split.getIndex())
                                trackViewEntity.stackSplit(entityAudio)
                                entityAudio.setCurrentRect()
                                entityAudio.setRight(abs)
                                entityAudio.setMax((entityAudio.getRect().right / entityAudio.getscaleFactor()) - ((entityAudio.getRect().left / entityAudio.getscaleFactor()) - entityAudio.getOffset_left()))
                                entityAudio.setEnd(f)
                                split.setOffset_right(entityAudio.getOffset_right())
                                entityAudio.setOffset_right(0.0f)
                                split.setOffset(entityAudio.getOffset() + entityAudio.getOffset_left() + (entityAudio.getRect().width() / entityAudio.getscaleFactor()))
                                entityAudio.onChange()
                                split.setSecond_in_screen(trackViewEntity.getSecond_in_screenNoScale())
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
            pausePlayer()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.getSelectedEntity() as EntityAudio
            mCurrentFragment = ReverbePresetFragment.getInstance(iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun echoEffect() {
            pausePlayer()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.getSelectedEntity() as EntityAudio
            mCurrentFragment = EchoEffectFragment.getInstance(iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun noice() {
            pausePlayer()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.getSelectedEntity() as EntityAudio
            mCurrentFragment = RemoveNoiceFragment.getInstance(iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun enhanceVoice() {
            pausePlayer()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.getSelectedEntity() as EntityAudio
            mCurrentFragment = EnhanceVoiceFragment.getInstance(iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun speedffect() {
            pausePlayer()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.getSelectedEntity() as EntityAudio
            mCurrentFragment = SpeedFragment.getInstance(iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun volumeEffect() {
            pausePlayer()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.getSelectedEntity() as EntityAudio
            mCurrentFragment = VolumeFragment.getInstance(iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun pitchffect() {
            pausePlayer()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.getSelectedEntity() as EntityAudio
            mCurrentFragment = PitchFragment.getInstance(iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
        }

        override fun fadeffect() {
            pausePlayer()
            findViewById<View>(R.id.layout_menu).visibility = View.INVISIBLE
            val beginTransaction = supportFragmentManager.beginTransaction()
            val entityAudio = trackViewEntity.getSelectedEntity() as EntityAudio
            mCurrentFragment = FadeInOutFragment.getInstance(iEditMediaCallback, entityAudio)
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

        override fun onUpdate(quranEntity: QuranEntity) {
            blurredImageView.postInvalidate()
            trackViewEntity.postInvalidate()
        }
    }

    private val iTransitionCallback = object : EffectAyaFragment.ITransition {
        override fun toSubscribe() {}

        override fun destroy(entityQuranTimeline: EntityQuranTimeline?) {
            if (entityQuranTimeline == null) {
                return
            }
            entityQuranTimeline.getQuranEntity().setAnimTest(false)
            entityQuranTimeline.getQuranEntity().endAnimator()
            blurredImageView.invalidate()
        }

        override fun playing(entityQuranTimeline: EntityQuranTimeline?) {
            if (entityQuranTimeline == null) {
                return
            }
            entityQuranTimeline.getQuranEntity().setAnimTest(true)
        }

        override fun onHideFragment(entityQuranTimeline: EntityQuranTimeline) {
            hideFragment()
            try {
                iTrimLineCallback.onSelectEntity(entityQuranTimeline, -1.0f)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun `in`(str: String, entityQuranTimeline: EntityQuranTimeline?) {
            if (entityQuranTimeline == null) {
                return
            }
            if (entityQuranTimeline.getTransition() == null) {
                entityQuranTimeline.setTransition(Transition())
            }
            entityQuranTimeline.getTransition()!!.setIn(true)
            entityQuranTimeline.getTransition()!!.setType_in(str)
            EffectAyaFragment.instance!!.updateView(entityQuranTimeline.getTransition()!!.getDuration_in(), entityQuranTimeline.getTransition()!!)
            entityQuranTimeline.getQuranEntity().endAnimator()
            entityQuranTimeline.getQuranEntity().runIn((entityQuranTimeline.getTransition()!!.getDuration_in() * 1000.0f).toInt(), true, entityQuranTimeline.getTransition()!!.getType_in())
        }

        override fun out(str: String, entityQuranTimeline: EntityQuranTimeline?) {
            if (entityQuranTimeline == null) {
                return
            }
            if (entityQuranTimeline.getTransition() == null) {
                entityQuranTimeline.setTransition(Transition())
            }
            entityQuranTimeline.getTransition()!!.setOut(true)
            entityQuranTimeline.getTransition()!!.setType_out(str)
            EffectAyaFragment.instance!!.updateView(entityQuranTimeline.getTransition()!!.getDuration_out(), entityQuranTimeline.getTransition()!!)
            entityQuranTimeline.getQuranEntity().endAnimator()
            entityQuranTimeline.getQuranEntity().runOut((entityQuranTimeline.getTransition()!!.getDuration_out() * 1000.0f).toInt(), true, entityQuranTimeline.getTransition()!!.getType_out())
        }

        override fun remove(i: Int, entityQuranTimeline: EntityQuranTimeline) {
            if (i == 0) {
                entityQuranTimeline.getTransition()!!.setIn(false)
                entityQuranTimeline.getQuranEntity().endAnimator()
            }
            if (i == 1) {
                entityQuranTimeline.getTransition()!!.setOut(false)
                entityQuranTimeline.getQuranEntity().endAnimator()
            }
        }

        override fun updateDurationIn(f: Float, entityQuranTimeline: EntityQuranTimeline?) {
            if (entityQuranTimeline == null) {
                return
            }
            entityQuranTimeline.getTransition()!!.setDuration_in(f)
            entityQuranTimeline.getQuranEntity().endAnimator()
            entityQuranTimeline.getQuranEntity().runIn((entityQuranTimeline.getTransition()!!.getDuration_in() * 1000.0f).toInt(), true, entityQuranTimeline.getTransition()!!.getType_in())
        }

        override fun updateDurationOut(f: Float, entityQuranTimeline: EntityQuranTimeline?) {
            if (entityQuranTimeline == null) {
                return
            }
            entityQuranTimeline.getTransition()!!.setDuration_out(f)
            entityQuranTimeline.getQuranEntity().endAnimator()
            entityQuranTimeline.getQuranEntity().runOut((entityQuranTimeline.getTransition()!!.getDuration_out() * 1000.0f).toInt(), true, entityQuranTimeline.getTransition()!!.getType_out())
        }

        override fun applyAll(i: Int, entityQuranTimeline: EntityQuranTimeline) {
            showProgress()
            addUpdateAnim(trackViewEntity.getmIsi3adaTimeline(), entityQuranTimeline)
            addUpdateAnim(trackViewEntity.getBismilahTimeline(), entityQuranTimeline)
            for (entityQuranTimeline2 in trackViewEntity.getEntityListQuran()) {
                if (entityQuranTimeline2 !== entityQuranTimeline) {
                    if (entityQuranTimeline.getTransition() == null) {
                        entityQuranTimeline2.setTransition(null)
                        return
                    }
                    if (entityQuranTimeline2.getTransition() == null) {
                        entityQuranTimeline2.setTransition(Transition())
                    }
                    entityQuranTimeline2.getTransition()!!.setOut(entityQuranTimeline.getTransition()!!.isOut()
                    entityQuranTimeline2.getTransition()!!.setType_out(entityQuranTimeline.getTransition()!!.getType_out())
                    entityQuranTimeline2.getTransition()!!.setDuration_out(entityQuranTimeline.getTransition()!!.getDuration_out())
                    entityQuranTimeline2.getTransition()!!.setIn(entityQuranTimeline.getTransition()!!.isIn()
                    entityQuranTimeline2.getTransition()!!.setType_in(entityQuranTimeline.getTransition()!!.getType_in())
                    entityQuranTimeline2.getTransition()!!.setDuration_in(entityQuranTimeline.getTransition()!!.getDuration_in())
                }
            }
            hideProgressFragment()
        }
    }

    private val iTransitionBismilahCallback = object : EffectBismilahFragment.ITransition {
        override fun destroy(entityBismilahTimeline: EntityBismilahTimeline?) {
            if (entityBismilahTimeline == null) {
                return
            }
            entityBismilahTimeline.getQuranEntity().setAnimTest(false)
            entityBismilahTimeline.getQuranEntity().endAnimator()
            blurredImageView.invalidate()
        }

        override fun playing(entityBismilahTimeline: EntityBismilahTimeline?) {
            if (entityBismilahTimeline == null) {
                return
            }
            entityBismilahTimeline.getQuranEntity().setAnimTest(true)
        }

        override fun onHideFragment(entityBismilahTimeline: EntityBismilahTimeline) {
            hideFragment()
            try {
                iTrimLineCallback.onSelectEntity(entityBismilahTimeline, -1.0f)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun `in`(str: String, entityBismilahTimeline: EntityBismilahTimeline?) {
            if (entityBismilahTimeline == null) {
                return
            }
            if (entityBismilahTimeline.getTransition() == null) {
                entityBismilahTimeline.setTransition(Transition())
            }
            entityBismilahTimeline.getTransition()!!.setIn(true)
            entityBismilahTimeline.getTransition()!!.setType_in(str)
            EffectBismilahFragment.instance!!.updateView(entityBismilahTimeline.getTransition()!!.getDuration_in(), entityBismilahTimeline.getTransition()!!)
            entityBismilahTimeline.getQuranEntity().endAnimator()
            entityBismilahTimeline.getQuranEntity().runIn((entityBismilahTimeline.getTransition()!!.getDuration_in() * 1000.0f).toInt(), true, entityBismilahTimeline.getTransition()!!.getType_in())
        }

        override fun out(str: String, entityBismilahTimeline: EntityBismilahTimeline?) {
            if (entityBismilahTimeline == null) {
                return
            }
            if (entityBismilahTimeline.getTransition() == null) {
                entityBismilahTimeline.setTransition(Transition())
            }
            entityBismilahTimeline.getTransition()!!.setOut(true)
            entityBismilahTimeline.getTransition()!!.setType_out(str)
            EffectBismilahFragment.instance!!.updateView(entityBismilahTimeline.getTransition()!!.getDuration_out(), entityBismilahTimeline.getTransition()!!)
            entityBismilahTimeline.getQuranEntity().endAnimator()
            entityBismilahTimeline.getQuranEntity().runOut((entityBismilahTimeline.getTransition()!!.getDuration_out() * 1000.0f).toInt(), true, entityBismilahTimeline.getTransition()!!.getType_out())
        }

        override fun remove(i: Int, entityBismilahTimeline: EntityBismilahTimeline) {
            if (i == 0) {
                entityBismilahTimeline.getTransition()!!.setIn(false)
                entityBismilahTimeline.getQuranEntity().endAnimator()
            }
            if (i == 1) {
                entityBismilahTimeline.getTransition()!!.setOut(false)
                entityBismilahTimeline.getQuranEntity().endAnimator()
            }
        }

        override fun updateDurationIn(f: Float, entityBismilahTimeline: EntityBismilahTimeline?) {
            if (entityBismilahTimeline == null) {
                return
            }
            entityBismilahTimeline.getTransition()!!.setDuration_in(f)
            entityBismilahTimeline.getQuranEntity().endAnimator()
            entityBismilahTimeline.getQuranEntity().runIn((entityBismilahTimeline.getTransition()!!.getDuration_in() * 1000.0f).toInt(), true, entityBismilahTimeline.getTransition()!!.getType_in())
        }

        override fun updateDurationOut(f: Float, entityBismilahTimeline: EntityBismilahTimeline?) {
            if (entityBismilahTimeline == null) {
                return
            }
            entityBismilahTimeline.getTransition()!!.setDuration_out(f)
            entityBismilahTimeline.getQuranEntity().endAnimator()
            entityBismilahTimeline.getQuranEntity().runOut((entityBismilahTimeline.getTransition()!!.getDuration_out() * 1000.0f).toInt(), true, entityBismilahTimeline.getTransition()!!.getType_out())
        }

        override fun applyAll(entityBismilahTimeline: EntityBismilahTimeline) {
            showProgress()
            addUpdateAnim(
                if (trackViewEntity.getmIsi3adaTimeline() !== entityBismilahTimeline) trackViewEntity.getmIsi3adaTimeline() else trackViewEntity.getBismilahTimeline(),
                entityBismilahTimeline
            )
            for (entityQuranTimeline in trackViewEntity.getEntityListQuran()) {
                if (entityBismilahTimeline.getTransition() == null) {
                    entityQuranTimeline.setTransition(null)
                    return
                }
                if (entityQuranTimeline.getTransition() == null) {
                    entityQuranTimeline.setTransition(Transition())
                }
                entityQuranTimeline.getTransition()!!.setOut(entityBismilahTimeline.getTransition()!!.isOut()
                entityQuranTimeline.getTransition()!!.setType_out(entityBismilahTimeline.getTransition()!!.getType_out())
                entityQuranTimeline.getTransition()!!.setDuration_out(entityBismilahTimeline.getTransition()!!.getDuration_out())
                entityQuranTimeline.getTransition()!!.setIn(entityBismilahTimeline.getTransition()!!.isIn()
                entityQuranTimeline.getTransition()!!.setType_in(entityBismilahTimeline.getTransition()!!.getType_in())
                entityQuranTimeline.getTransition()!!.setDuration_in(entityBismilahTimeline.getTransition()!!.getDuration_in())
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
        var cropToSquareWithRoundCorners: Bitmap
        var bitmap2: Bitmap
        var rect: Rect
        var clrTrsl: Int
        try {
            blurredImageView.initCanvasDimension(blurredImageView.width, blurredImageView.height, mTemplate.geTypeResize())
            val height = blurredImageView.height
            try {
                bitmap = Glide.with(this@EngineActivity as FragmentActivity).asBitmap()
                    .load(mTemplate.uri_bg)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(height, height)
                    .submit().get() as Bitmap
            } catch (unused: Exception) {
                mTemplate.color_ipad = -1
                bitmap = Glide.with(this@EngineActivity as FragmentActivity).asBitmap()
                    .load(R.drawable.bg_19)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(height, height)
                    .submit().get() as Bitmap
            }
            blurredImageView.bitmapOriginal = setupOriginalBitmap(bitmap, height)
            cropTo16x9 = when (mTemplate.geTypeResize()) {
                ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(blurredImageView.bitmapOriginal, blurredImageView.w, blurredImageView.h)
                ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(blurredImageView.bitmapOriginal, blurredImageView.w, blurredImageView.h)
                else -> BitmapCropper.cropTo16x9(blurredImageView.bitmapOriginal, blurredImageView.w, blurredImageView.h)
            }
            blurredImageView.isGlass = mTemplate.isGlass
            blurredImageView.isVideo = false
            blurredImageView.updatePosCanvas(cropTo16x9)
            blurredImageView.updateIpad(cropTo16x9, mTemplate.ipad_type, mTemplate.geTypeResize())

            if (mTemplate.ipad_type == IpadType.IPAD_NEOMORPHIC.ordinal) {
                val width = (blurredImageView.ipad_rect.width() * 0.6f).toInt()
                var round = Math.round(blurredImageView.bitmapOriginal.width * mTemplate.x_square)
                var round2 = Math.round(blurredImageView.bitmapOriginal.height * mTemplate.y_square)
                var i3 = width + round
                if (i3 > blurredImageView.bitmapOriginal.width) {
                    round -= i3 - blurredImageView.bitmapOriginal.width
                    i3 = blurredImageView.bitmapOriginal.width
                }
                var i4 = width + round2
                if (i4 > blurredImageView.bitmapOriginal.height) {
                    round2 -= i4 - blurredImageView.bitmapOriginal.height
                    i4 = blurredImageView.bitmapOriginal.height
                }
                if (round < 0) round = 0
                if (round2 >= 0) {
                    // i2 = round2 (in original decompiled code)
                }
                val i2 = if (round2 >= 0) round2 else 0
                val rect2 = Rect(round, i2, i3, i4)
                blurredImageView.radius_square = width
                val width2 = (blurredImageView.bitmapOriginal.width * mTemplate.width_square).toInt()
                val height2 = (blurredImageView.bitmapOriginal.height * mTemplate.height_square).toInt()
                val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.bitmapOriginal, rect2, width, width2, height2)
                rect2.right = rect2.left + width2
                rect2.bottom = rect2.top + height2
                blurredImageView.rectSquare = rect2
                bitmap2 = cropToSquareWithRoundCorners2
                rect = rect2
            } else {
                if (mTemplate.ipad_type != IpadType.IPAD.ordinal && mTemplate.ipad_type != IpadType.IPAD_UNBLUR.ordinal && mTemplate.ipad_type != IpadType.IPAD_CLASSIC.ordinal) {
                    val width3 = (blurredImageView.ipad_rect.width() * 1.0f).toInt()
                    val height3 = (cropTo16x9.height * 0.5355f).toInt()
                    var round3 = Math.round(blurredImageView.bitmapOriginal.width * mTemplate.x_square)
                    var round4 = Math.round(blurredImageView.bitmapOriginal.height * mTemplate.y_square)
                    var i5 = width3 + round3
                    if (i5 > blurredImageView.bitmapOriginal.width) {
                        round3 -= i5 - blurredImageView.bitmapOriginal.width
                        i5 = blurredImageView.bitmapOriginal.width
                    }
                    var i6 = height3 + round4
                    if (i6 > blurredImageView.bitmapOriginal.height) {
                        round4 -= i6 - blurredImageView.bitmapOriginal.height
                        i6 = blurredImageView.bitmapOriginal.height
                    }
                    if (round3 < 0) round3 = 0
                    if (round4 < 0) round4 = 0
                    val rect3 = Rect(round3, round4, i5, i6)
                    val width4 = (blurredImageView.bitmapOriginal.width * mTemplate.width_square).toInt()
                    val height4 = (blurredImageView.bitmapOriginal.height * mTemplate.height_square).toInt()
                    val cropToSquare = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal, rect3, width4, height4)
                    blurredImageView.bitmapSquare = cropToSquare
                    blurredImageView.radius_square = 0
                    rect3.right = rect3.left + width4
                    rect3.bottom = rect3.top + height4
                    blurredImageView.rectSquare = rect3
                    bitmap2 = cropToSquare
                    rect = rect3
                }
                val width5 = (blurredImageView.ipad_rect.width() * 0.87530595f).toInt()
                val i7 = (width5 * 1.13f).toInt()
                val min = Math.min(width5, i7)
                var round5 = Math.round(blurredImageView.bitmapOriginal.width * mTemplate.x_square)
                var round6 = Math.round(blurredImageView.bitmapOriginal.height * mTemplate.y_square)
                var i8 = width5 + round5
                if (i8 > blurredImageView.bitmapOriginal.width) {
                    round5 -= i8 - blurredImageView.bitmapOriginal.width
                    i8 = blurredImageView.bitmapOriginal.width
                }
                var i9 = i7 + round6
                if (i9 > blurredImageView.bitmapOriginal.height) {
                    round6 -= i9 - blurredImageView.bitmapOriginal.height
                    i9 = blurredImageView.bitmapOriginal.height
                }
                if (round5 < 0) round5 = 0
                if (round6 < 0) round6 = 0
                val rect4 = Rect(round5, round6, i8, i9)
                if (mTemplate.ipad_type == IpadType.IPAD_CLASSIC.ordinal) {
                    val width6 = (blurredImageView.bitmapOriginal.width * mTemplate.width_square).toInt()
                    val height5 = (blurredImageView.bitmapOriginal.height * mTemplate.height_square).toInt()
                    val cropToSquare2 = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal, rect4, width6, height5)
                    blurredImageView.bitmapSquare = cropToSquare2
                    blurredImageView.radius_square = 0
                    rect4.right = rect4.left + width6
                    rect4.bottom = rect4.top + height5
                    blurredImageView.rectSquare = rect4
                    cropToSquareWithRoundCorners = cropToSquare2
                } else {
                    val i10 = (min * 0.10800001f).toInt()
                    blurredImageView.radius_square = i10
                    val width7 = (blurredImageView.bitmapOriginal.width * mTemplate.width_square).toInt()
                    val height6 = (blurredImageView.bitmapOriginal.height * mTemplate.height_square).toInt()
                    cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.bitmapOriginal, rect4, i10, width7, height6)
                    rect4.right = rect4.left + width7
                    rect4.bottom = rect4.top + height6
                    blurredImageView.rectSquare = rect4
                }
                bitmap2 = cropToSquareWithRoundCorners
                rect = rect4
            }
            if (mTemplate.gradient != null) {
                blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap2, mTemplate.gradient, mTemplate.ipad_type, mTemplate.geTypeResize(), rect)
            } else {
                blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap2, mTemplate.color_ipad, mTemplate.ipad_type, mTemplate.geTypeResize(), rect)
            }
            clrTrsl = if (mTemplate.ipad_type == IpadType.BLUE_TYPE.ordinal) {
                blurredImageView.paintLecture.color
            } else {
                if (blurredImageView.paintLecture.color == -1) InputDeviceCompat.SOURCE_ANY else Common.COLOR_TRANSLATION
            }
            blurredImageView.clr_trsl = clrTrsl
            blurredImageView.clr_aya = blurredImageView.paintLecture.color
            addEntityFromTemplate()
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
    seekBar_fps = findViewById<CustomDiscreteSeekBar>(R.id.seekbar_fps)
    when (mTemplate.fps) {
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
                mTemplate.fps = seekBar_fps.getCurrentLabel().toInt()
            }
        }
    })
    tv_resolution.text = mTemplate.resolution
    seekBar_res = findViewById<CustomDiscreteSeekBar>(R.id.seekbar_resolution)
    when (mTemplate.resolution) {
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
                mTemplate.resolution = seekBar_res.getCurrentLabel()
                val size = AspectRatioCalculator.getSize(mTemplate.geTypeResize(), mTemplate.resolution)
                tv_resolution.text = mTemplate.resolution
                mTemplate.setWidthAndHeight(size.first, size.second)
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
            trackViewEntity.setPlaying(mIsPlaying)
            blurredImageView.setPlaying(mIsPlaying)
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
        updateBtnToEndAndStart()
        current_position_time = System.currentTimeMillis().toInt()
        mIsPlaying = true
        trackViewEntity.setPlaying(true)
        blurredImageView.setPlaying(true)
        startTimelineAnimation()
    }
    val imageButton2 = findViewById<ImageButton>(R.id.btn_to_end)
    btnToEnd = imageButton2
    imageButton2.setOnClickListener {
        if (trackViewEntity.current_cursur_position == trackViewEntity.maxTime) return@setOnClickListener
        blurredImageView.setProgress(1.0f)
        pausePlayer()
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
        blurredImageView.setProgress(0.0f)
        pausePlayer()
        startCursur = 0
        trackViewEntity.translateToStart()
        updateViewTime(trackViewEntity.maxTime, trackViewEntity.current_cursur_position)
        updateBtnToStart()
        updateBtnToEnd()
    }
    updateBtnToStart(mTemplate.currentCursur)
    btnRedo = findViewById(R.id.btn_redo)
    btnUndo = findViewById(R.id.btn_undo)
    disableUndoBtn()
    disableRedoBtn()
    btnRedo.setOnClickListener(object : View.OnClickListener {
        override fun onClick(view: View) {
            pausePlayer()
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
            pausePlayer()
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
    blurredImgView.setPro(BillingPreferences.isSubscribed(this))
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
                    pausePlayer()
                    selectSurahName()
                    return
                } catch (unused: Exception) {
                    return
                }
            }
            if (entityView is QuranEntity) {
                trackViewEntity.selectEntity(entityView.entityQuran, true)
                iTrimLineCallback.onSelectEntity(entityView.entityQuran, 0.0f)
            } else if (entityView is BismilahEntity) {
                val bismilahTimeline = (entityView as BismilahEntity).bismilahTimeline
                trackViewEntity.selectEntity(bismilahTimeline, true)
                iTrimLineCallback.onSelectEntity(bismilahTimeline, 0.0f)
            } else if (entityView is TranslationQuranEntity) {
                trackViewEntity.selectEntity(entityView.entityTrslTimeline, true)
                iTrimLineCallback.onSelectEntity(entityView.entityTrslTimeline, 0.0f)
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
        if (mTemplate.isVideoSquare) {
            initTypeVideo()
        } else {
            iniTypeImg()
        }
    }
    val buttonCustumFont = findViewById<ButtonCustumFont>(R.id.btn_export)
    btn_export = buttonCustumFont
    buttonCustumFont.text = mResources.getString(R.string.export)
    btn_export.setOnClickListener {
        isSaveTmpTemplate = false
        pausePlayer()
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
    imageButton4.setOnClickListener { dialog() }
    tv_tittle_fragment = findViewById(R.id.tv_tittle_fragment)
    (findViewById<TextCustumFont>(R.id.tv_quran)).text = mResources.getString(R.string.quran)
    (findViewById<TextCustumFont>(R.id.tv_bg)).text = mResources.getString(R.string.bg)
    val textCustumFont = findViewById<TextCustumFont>(R.id.tv_ipad)
    textCustumFont.text = mResources.getString(R.string.ipad)
    findViewById<View>(R.id.btn_add_quran).setOnClickListener {
        pausePlayer()
        try {
            val beginTransaction = supportFragmentManager.beginTransaction()
            mCurrentFragment = AddQuranFragment.getInstance(iAddQuran, mResources)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
            setupShowFragment(mResources.getString(R.string.quran))
        } catch (unused: Exception) {
        }
    }
    findViewById<View>(R.id.btn_bg).setOnClickListener {
        pausePlayer()
        try {
            val beginTransaction = supportFragmentManager.beginTransaction()
            mCurrentFragment = ChangeBgFragment.getInstance(iChangeBgCallback, mResources, mTemplate.name_drawable)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commitNow()
            setupShowFragment(mResources.getString(R.string.bg))
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
            pausePlayer()
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
            pausePlayer()
            dialogPremium(R.drawable.iv_layout_ipod)
        }
        textCustumFont.setTextColor(-8355712)
        ivIpod?.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
        btnIpod?.setBackgroundColor(0)
    }
    btnIpod?.setOnClickListener {
        pausePlayer()
        try {
            val beginTransaction = supportFragmentManager.beginTransaction()
            mCurrentFragment = EditIpadFragment.getInstance(mResources, mTemplate.ipad_type, iIpadEditCallback, mTemplate.index_color, mTemplate.gradient != null, mTemplate.isGlass)
            beginTransaction.replace(R.id.m_container, mCurrentFragment!!)
            beginTransaction.commit()
            setupShowFragment(mResources.getString(R.string.ipad))
        } catch (unused: Exception) {
        }
    }
    updateHitRatio(mTemplate.geTypeResize(), mTemplate.imgResize)
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
        blurredImageView.setRemoveWattermark(false)
    }
    stop()
    showProgress()
    executor.execute(Runnable {
        try {
            trackViewEntity.calculMaxTime()
            blurredImageView.reset()
            blurredImageView.initCanvasDimension(mTemplate.width, mTemplate.height, mTemplate.geTypeResize())
            val max = Math.max(mTemplate.width, mTemplate.height)

            if (mTemplate.ipad_type != IpadType.HEART.ordinal && mTemplate.ipad_type != IpadType.BATTERY.ordinal) {
                if (mTemplate.isVideoSquare && (mTemplate.ipad_type == IpadType.GRADIENT.ordinal || mTemplate.ipad_type == IpadType.BLACK_LAYER.ordinal || mTemplate.ipad_type == IpadType.MASK_BRUSH.ordinal || mTemplate.ipad_type == IpadType.BLUE_TYPE.ordinal || mTemplate.ipad_type == IpadType.CASSET_IMG.ordinal)) {
                    blurredImageView.bitmapOriginal = Bitmap.createBitmap(max, max, Bitmap.Config.ARGB_8888)
                    val cropTo16x92 = when (mTemplate.geTypeResize()) {
                        ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(blurredImageView.bitmapOriginal)
                        ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(blurredImageView.bitmapOriginal)
                        else -> BitmapCropper.cropTo16x9(blurredImageView.bitmapOriginal)
                    }
                    blurredImageView.updatePosCanvas(mTemplate.width, mTemplate.height, cropTo16x92)
                    blurredImageView.updateIpad(cropTo16x92, mTemplate.ipad_type, mTemplate.geTypeResize())
                    val width = (blurredImageView.ipad_rect.width() * 1.0f).toInt()
                    val height = (cropTo16x92.height * 0.5355f).toInt()
                    mTemplate.setDrawingTranslation(blurredImageView.btmX, blurredImageView.btmY)
                    var round = Math.round(blurredImageView.bitmapOriginal.width * mTemplate.x_square)
                    var round2 = Math.round(blurredImageView.bitmapOriginal.height * mTemplate.y_square)
                    var i4 = width + round
                    if (i4 > blurredImageView.bitmapOriginal.width) {
                        round -= i4 - blurredImageView.bitmapOriginal.width
                        i4 = blurredImageView.bitmapOriginal.width
                    }
                    var i5 = height + round2
                    if (i5 > blurredImageView.bitmapOriginal.height) {
                        round2 -= i5 - blurredImageView.bitmapOriginal.height
                        i5 = blurredImageView.bitmapOriginal.height
                    }
                    if (round < 0) round = 0
                    if (round2 < 0) round2 = 0
                    val rect2 = Rect(round, round2, i4, i5)
                    val width2 = (blurredImageView.bitmapOriginal.width * mTemplate.width_square).toInt()
                    val height2 = (blurredImageView.bitmapOriginal.height * mTemplate.height_square).toInt()
                    val cropToSquare = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal, rect2, width2, height2)
                    blurredImageView.bitmapSquare = cropToSquare
                    blurredImageView.radius_square = 0
                    rect2.right = rect2.left + width2
                    rect2.bottom = rect2.top + height2
                    blurredImageView.rectSquare = rect2
                    mTemplate.uri_bg_ffmpeg = blurredImageView.setupBitmapDraw(cropTo16x92, cropToSquare, mTemplate)
                    mTemplate.squareBitmapModel.set(blurredImageView.left_square, blurredImageView.top_square, round, round2, rect2.width(), rect2.height(), cropToSquare.width, cropToSquare.height, 0)
                } else {
                    blurredImageView.bitmapOriginal = setupOriginalBitmap(
                        Glide.with(this@EngineActivity as FragmentActivity).asBitmap()
                            .load(if (mTemplate.isVideoSquare) mTemplate.frame_bg else mTemplate.uri_bg)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .override(max, max)
                            .submit().get() as Bitmap,
                        max
                    )
                    val cropTo16x9 = when (mTemplate.geTypeResize()) {
                        ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(blurredImageView.bitmapOriginal, mTemplate.width, mTemplate.height)
                        ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(blurredImageView.bitmapOriginal, mTemplate.width, mTemplate.height)
                        else -> BitmapCropper.cropTo16x9(blurredImageView.bitmapOriginal, mTemplate.width, mTemplate.height)
                    }
                    val bitmap = cropTo16x9
                    blurredImageView.updatePosCanvas(mTemplate.width, mTemplate.height, bitmap)
                    blurredImageView.updateIpad(bitmap, mTemplate.ipad_type, mTemplate.geTypeResize())

                    var rect: Rect
                    var cropToSquareWithRoundCorners: Bitmap
                    var i2: Int
                    var i3: Int

                    if (mTemplate.ipad_type == IpadType.IPAD_NEOMORPHIC.ordinal) {
                        val radius = (blurredImageView.ipad_rect.width() * 0.6f).toInt()
                        mTemplate.setDrawingTranslation(blurredImageView.btmX, blurredImageView.btmY)
                        i2 = Math.round(blurredImageView.bitmapOriginal.width * mTemplate.x_square)
                        i3 = Math.round(blurredImageView.bitmapOriginal.height * mTemplate.y_square)
                        var i6 = radius + i2
                        if (i6 > blurredImageView.bitmapOriginal.width) {
                            i2 -= i6 - blurredImageView.bitmapOriginal.width
                            i6 = blurredImageView.bitmapOriginal.width
                        }
                        var i7 = radius + i3
                        if (i7 > blurredImageView.bitmapOriginal.height) {
                            i3 -= i7 - blurredImageView.bitmapOriginal.height
                            i7 = blurredImageView.bitmapOriginal.height
                        }
                        if (i2 < 0) i2 = 0
                        if (i3 < 0) i3 = 0
                        rect = Rect(i2, i3, i6, i7)
                        val width3 = (blurredImageView.bitmapOriginal.width * mTemplate.width_square).toInt()
                        val height3 = (blurredImageView.bitmapOriginal.height * mTemplate.height_square).toInt()
                        cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.bitmapOriginal, rect, radius, width3, height3)
                        rect.right = rect.left + width3
                        rect.bottom = rect.top + height3
                        blurredImageView.rectSquare = rect
                    } else {
                        if (mTemplate.ipad_type != IpadType.IPAD.ordinal && mTemplate.ipad_type != IpadType.IPAD_UNBLUR.ordinal && mTemplate.ipad_type != IpadType.IPAD_CLASSIC.ordinal) {
                            val width4 = (blurredImageView.ipad_rect.width() * 1.0f).toInt()
                            val height4 = (bitmap.height * 0.5355f).toInt()
                            mTemplate.setDrawingTranslation(blurredImageView.btmX, blurredImageView.btmY)
                            var round3 = Math.round(blurredImageView.bitmapOriginal.width * mTemplate.x_square)
                            var round4 = Math.round(blurredImageView.bitmapOriginal.height * mTemplate.y_square)
                            var i8 = width4 + round3
                            if (i8 > blurredImageView.bitmapOriginal.width) {
                                round3 -= i8 - blurredImageView.bitmapOriginal.width
                                i8 = blurredImageView.bitmapOriginal.width
                            }
                            var i9 = height4 + round4
                            if (i9 > blurredImageView.bitmapOriginal.height) {
                                round4 -= i9 - blurredImageView.bitmapOriginal.height
                                i9 = blurredImageView.bitmapOriginal.height
                            }
                            if (round3 < 0) round3 = 0
                            if (round4 < 0) round4 = 0
                            rect = Rect(round3, round4, i8, i9)
                            val width5 = (blurredImageView.bitmapOriginal.width * mTemplate.width_square).toInt()
                            val height5 = (blurredImageView.bitmapOriginal.height * mTemplate.height_square).toInt()
                            cropToSquareWithRoundCorners = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal, rect, width5, height5)
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

                        if (mTemplate.ipad_type == IpadType.IPAD.ordinal || mTemplate.ipad_type == IpadType.IPAD_UNBLUR.ordinal || mTemplate.ipad_type == IpadType.IPAD_CLASSIC.ordinal) {
                            val width6 = (blurredImageView.ipad_rect.width() * 0.87530595f).toInt()
                            val i10 = (width6 * 1.13f).toInt()
                            val min = Math.min(width6, i10)
                            mTemplate.setDrawingTranslation(blurredImageView.btmX, blurredImageView.btmY)
                            var round5 = Math.round(blurredImageView.bitmapOriginal.width * mTemplate.x_square)
                            var round6 = Math.round(blurredImageView.bitmapOriginal.height * mTemplate.y_square)
                            var i11 = width6 + round5
                            if (i11 > blurredImageView.bitmapOriginal.width) {
                                round5 -= i11 - blurredImageView.bitmapOriginal.width
                                i11 = blurredImageView.bitmapOriginal.width
                            }
                            var i12 = i10 + round6
                            if (i12 > blurredImageView.bitmapOriginal.height) {
                                round6 -= i12 - blurredImageView.bitmapOriginal.height
                                i12 = blurredImageView.bitmapOriginal.height
                            }
                            if (round5 < 0) round5 = 0
                            if (round6 < 0) round6 = 0
                            rect = Rect(round5, round6, i11, i12)
                            var radiusVal = 0
                            if (mTemplate.ipad_type == IpadType.IPAD_CLASSIC.ordinal) {
                                val width7 = (blurredImageView.bitmapOriginal.width * mTemplate.width_square).toInt()
                                val height6 = (blurredImageView.bitmapOriginal.height * mTemplate.height_square).toInt()
                                val cropToSquare2 = UtilsBitmap.cropToSquare(blurredImageView.bitmapOriginal, rect, width7, height6)
                                blurredImageView.bitmapSquare = cropToSquare2
                                blurredImageView.radius_square = 0
                                rect.right = rect.left + width7
                                rect.bottom = rect.top + height6
                                blurredImageView.rectSquare = rect
                                cropToSquareWithRoundCorners = cropToSquare2
                                radiusVal = 0
                            } else {
                                radiusVal = (min * 0.10800001f).toInt()
                                val width8 = (blurredImageView.bitmapOriginal.width * mTemplate.width_square).toInt()
                                val height7 = (blurredImageView.bitmapOriginal.height * mTemplate.height_square).toInt()
                                cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(blurredImageView.bitmapOriginal, rect, radiusVal, width8, height7)
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
                        mTemplate.uri_bg_ffmpeg = blurredImageView.setupBitmapDraw(UtilsBitmap.blurInSave(engineActivity, bitmap, 20, 1, engineActivity.mTemplate.width, mTemplate.height), bitmap2, mTemplate)
                        mTemplate.squareBitmapModel.set(blurredImageView.left_square, blurredImageView.top_square, i2, i3, rect3.width(), rect3.height(), bitmap2.width, bitmap2.height, 0)
                    }

                    // Heart/Battery fallback
                    mTemplate.uri_bg_ffmpeg = blurredImageView.setupBitmapDraw(cropTo16x9, null, mTemplate)
                }
                saveTemplate()
                val intent = Intent(this@EngineActivity, ProgressViewActivity::class.java)
                intent.putExtra(Common.TEMPLATE, mTemplate.idTemplate)
                intent.addFlags(65536)
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
            // HEART/BATTERY type handling
            val createBitmap = Bitmap.createBitmap(mTemplate.width, mTemplate.height, Bitmap.Config.RGB_565)
            createBitmap.eraseColor(ViewCompat.MEASURED_STATE_MASK)
            blurredImageView.updatePosCanvas(mTemplate.width, mTemplate.height, createBitmap)
            blurredImageView.updateIpad(createBitmap, mTemplate.ipad_type, mTemplate.geTypeResize())
            mTemplate.uri_bg_ffmpeg = blurredImageView.setupBitmapDraw(createBitmap, null, mTemplate)
            saveTemplate()
            val intent2 = Intent(this@EngineActivity, ProgressViewActivity::class.java)
            intent2.putExtra(Common.TEMPLATE, mTemplate.idTemplate)
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
        mTemplate.setNewCode()
        mTemplate.isGlass = blurredImageView.isGlass
        mTemplate.currentCursur = trackViewEntity.current_cursur_position
        mTemplate.scale_timeline = trackViewEntity.scaleFactor
        mTemplate.gradient = blurredImageView.color_gradient
        mTemplate.duration = trackViewEntity.maxTime
        mTemplate.color_ipad = blurredImageView.colorIpad()
        mTemplate.quranEntityList.clear()
        mTemplate.translationTemplateList.clear()
        mTemplate.uri_bg = uri_bg

        try {
            for (entityQuranTimeline in trackViewEntity.entityListQuran) {
                if (entityQuranTimeline.visible()) {
                    val f2 = Utils.f2(Math.abs(entityQuranTimeline.rect.left / trackViewEntity.second_in_screen))
                    val f22 = Utils.f2(Math.abs(entityQuranTimeline.rect.right / trackViewEntity.second_in_screen))
                    if (entityQuranTimeline.quranEntity.copyRect == null) {
                        entityQuranTimeline.quranEntity.setCopyRect()
                        if (entityQuranTimeline.quranEntity.copyRect == null) {
                            // skip
                        }
                    }
                    val entityQuranTemplate = EntityQuranTemplate(
                        entityQuranTimeline.transition, f2, f22,
                        entityQuranTimeline.quranEntity.copyRect!!.left * mTemplate.width,
                        mTemplate.height * entityQuranTimeline.quranEntity.copyRect!!.top,
                        entityQuranTimeline.rect.left / entityQuranTimeline.scaleFactor,
                        entityQuranTimeline.rect.right / entityQuranTimeline.scaleFactor,
                        entityQuranTimeline.quranEntity.txt,
                        entityQuranTimeline.quranEntity.complete_aya,
                        entityQuranTimeline.quranEntity.nameFont,
                        entityQuranTimeline.quranEntity.indexNumber,
                        entityQuranTimeline.quranEntity.number,
                        entityQuranTimeline.quranEntity.clrAya,
                        if (entityQuranTimeline.quranEntity.paintTranslationAya != null) entityQuranTimeline.quranEntity.clrTrsl else InputDeviceCompat.SOURCE_ANY,
                        entityQuranTimeline.quranEntity.mPreset
                    )
                    entityQuranTemplate.height = (entityQuranTimeline.quranEntity.copyRect!!.bottom * mTemplate.height) - (entityQuranTimeline.quranEntity.copyRect!!.top * mTemplate.height)
                    entityQuranTemplate.factor_size = entityQuranTimeline.quranEntity.factorSize
                    entityQuranTemplate.factor_sizeTrl = entityQuranTimeline.quranEntity.factorSizeTrl
                    entityQuranTemplate.scale = entityQuranTimeline.quranEntity.scaleFactor
                    entityQuranTemplate.translation = entityQuranTimeline.quranEntity.translation
                    entityQuranTemplate.translation_complete = entityQuranTimeline.quranEntity.translation_complete
                    entityQuranTemplate.startWord_index = entityQuranTimeline.quranEntity.startWord_index
                    entityQuranTemplate.endWord_index = entityQuranTimeline.quranEntity.endWord_index
                    entityQuranTemplate.icon = entityQuranTimeline.quranEntity.icon
                    entityQuranTemplate.file = entityQuranTimeline.file
                    entityQuranTemplate.file_in = entityQuranTimeline.file_in
                    entityQuranTemplate.file_out = entityQuranTimeline.file_out
                    entityQuranTemplate.rectF = MRectF(entityQuranTimeline.quranEntity.copyRect!!.left, entityQuranTimeline.quranEntity.copyRect!!.top, entityQuranTimeline.quranEntity.copyRect!!.right, entityQuranTimeline.quranEntity.copyRect!!.bottom)
                    mTemplate.addQuranEntityList(entityQuranTemplate)
                }
            }
        } catch (e: Exception) {
            Log.e("save templete quran", "" + e.message)
        }

        try {
            for (entityTrslTimeline in trackViewEntity.entityListTrslQuran) {
                if (entityTrslTimeline.visible()) {
                    val f23 = Utils.f2(Math.abs(entityTrslTimeline.rect.left / trackViewEntity.second_in_screen))
                    val f24 = Utils.f2(Math.abs(entityTrslTimeline.rect.right / trackViewEntity.second_in_screen))
                    if (entityTrslTimeline.quranEntity.copyRect == null) {
                        entityTrslTimeline.quranEntity.setCopyRect()
                        if (entityTrslTimeline.quranEntity.copyRect == null) {
                            // skip
                        }
                    }
                    val entityTranslationTemplate = EntityTranslationTemplate(
                        entityTrslTimeline.transition, f23, f24,
                        entityTrslTimeline.quranEntity.copyRect!!.left * mTemplate.width,
                        mTemplate.height * entityTrslTimeline.quranEntity.copyRect!!.top,
                        entityTrslTimeline.rect.left / entityTrslTimeline.scaleFactor,
                        entityTrslTimeline.rect.right / entityTrslTimeline.scaleFactor,
                        entityTrslTimeline.quranEntity.txt,
                        entityTrslTimeline.quranEntity.nameFont,
                        entityTrslTimeline.quranEntity.number,
                        entityTrslTimeline.quranEntity.clrAya,
                        entityTrslTimeline.quranEntity.mPreset
                    )
                    entityTranslationTemplate.height = (entityTrslTimeline.quranEntity.copyRect!!.bottom * mTemplate.height) - (entityTrslTimeline.quranEntity.copyRect!!.top * mTemplate.height)
                    entityTranslationTemplate.factor_size = entityTrslTimeline.quranEntity.factorSize
                    entityTranslationTemplate.factor_sizeTrl = entityTrslTimeline.quranEntity.factorSizeTrl
                    entityTranslationTemplate.scale = entityTrslTimeline.quranEntity.scaleFactor
                    entityTranslationTemplate.file = entityTrslTimeline.file
                    entityTranslationTemplate.file_in = entityTrslTimeline.file_in
                    entityTranslationTemplate.file_out = entityTrslTimeline.file_out
                    entityTranslationTemplate.clr_bg = entityTrslTimeline.quranEntity.clrBg
                    entityTranslationTemplate.isHaveBg = entityTrslTimeline.quranEntity.isHaveBg
                    entityTranslationTemplate.rectF = MRectF(entityTrslTimeline.quranEntity.copyRect!!.left, entityTrslTimeline.quranEntity.copyRect!!.top, entityTrslTimeline.quranEntity.copyRect!!.right, entityTrslTimeline.quranEntity.copyRect!!.bottom)
                    mTemplate.addTrslEntityList(entityTranslationTemplate)
                }
            }
        } catch (e2: Exception) {
            Log.e("save templete trsl quran", "" + e2.message)
        }

        mTemplate.entityIsti3adaTemplate = null
        if (blurredImageView.mIsti3adhaEntity != null && blurredImageView.mIsti3adhaEntity!!.bismilahTimeline.visible()) {
            val bismilahTimeline = blurredImageView.mIsti3adhaEntity!!.bismilahTimeline
            val f25 = Utils.f2(Math.abs(bismilahTimeline.rect.left / trackViewEntity.second_in_screen))
            val f26 = Utils.f2(Math.abs(bismilahTimeline.rect.right / trackViewEntity.second_in_screen))
            if (bismilahTimeline.quranEntity.copyRect == null) {
                bismilahTimeline.quranEntity.setCopyRect()
            }
            val entityBismilahTemplate = EntityBismilahTemplate(
                bismilahTimeline.transition, f25, f26,
                bismilahTimeline.quranEntity.copyRect!!.left * mTemplate.width,
                mTemplate.height * bismilahTimeline.quranEntity.copyRect!!.top,
                bismilahTimeline.rect.left / bismilahTimeline.scaleFactor,
                bismilahTimeline.rect.right / bismilahTimeline.scaleFactor,
                bismilahTimeline.quranEntity.txt,
                bismilahTimeline.quranEntity.clrAya,
                bismilahTimeline.quranEntity.mPreset
            )
            entityBismilahTemplate.height = (bismilahTimeline.quranEntity.copyRect!!.bottom * mTemplate.height) - (bismilahTimeline.quranEntity.copyRect!!.top * mTemplate.height)
            entityBismilahTemplate.factor_size = bismilahTimeline.quranEntity.factorSize
            entityBismilahTemplate.scale = bismilahTimeline.quranEntity.scaleFactor
            entityBismilahTemplate.file = bismilahTimeline.file
            entityBismilahTemplate.file_in = bismilahTimeline.file_in
            entityBismilahTemplate.file_out = bismilahTimeline.file_out
            entityBismilahTemplate.rectF = MRectF(bismilahTimeline.quranEntity.copyRect!!.left, bismilahTimeline.quranEntity.copyRect!!.top, bismilahTimeline.quranEntity.copyRect!!.right, bismilahTimeline.quranEntity.copyRect!!.bottom)
            mTemplate.entityIsti3adaTemplate = entityBismilahTemplate
        }

        mTemplate.entityBismilahTemplate = null
        if (blurredImageView.bismilahEntity != null && blurredImageView.bismilahEntity!!.bismilahTimeline.visible()) {
            val bismilahTimeline2 = blurredImageView.bismilahEntity!!.bismilahTimeline
            val f27 = Utils.f2(Math.abs(bismilahTimeline2.rect.left / trackViewEntity.second_in_screen))
            val f28 = Utils.f2(Math.abs(bismilahTimeline2.rect.right / trackViewEntity.second_in_screen))
            if (bismilahTimeline2.quranEntity.copyRect == null) {
                bismilahTimeline2.quranEntity.setCopyRect()
            }
            val entityBismilahTemplate2 = EntityBismilahTemplate(
                bismilahTimeline2.transition, f27, f28,
                bismilahTimeline2.quranEntity.copyRect!!.left * mTemplate.width,
                mTemplate.height * bismilahTimeline2.quranEntity.copyRect!!.top,
                bismilahTimeline2.rect.left / bismilahTimeline2.scaleFactor,
                bismilahTimeline2.rect.right / bismilahTimeline2.scaleFactor,
                bismilahTimeline2.quranEntity.txt,
                bismilahTimeline2.quranEntity.clrAya,
                bismilahTimeline2.quranEntity.mPreset
            )
            entityBismilahTemplate2.height = (bismilahTimeline2.quranEntity.copyRect!!.bottom * mTemplate.height) - (bismilahTimeline2.quranEntity.copyRect!!.top * mTemplate.height)
            entityBismilahTemplate2.factor_size = bismilahTimeline2.quranEntity.factorSize
            entityBismilahTemplate2.scale = bismilahTimeline2.quranEntity.scaleFactor
            entityBismilahTemplate2.file = bismilahTimeline2.file
            entityBismilahTemplate2.file_in = bismilahTimeline2.file_in
            entityBismilahTemplate2.file_out = bismilahTimeline2.file_out
            entityBismilahTemplate2.rectF = MRectF(bismilahTimeline2.quranEntity.copyRect!!.left, bismilahTimeline2.quranEntity.copyRect!!.top, bismilahTimeline2.quranEntity.copyRect!!.right, bismilahTimeline2.quranEntity.copyRect!!.bottom)
            mTemplate.entityBismilahTemplate = entityBismilahTemplate2
        }

        str = if (blurredImageView.surahNameEntity == null) {
            ""
        } else if (mTemplate.entitySurahTemplate == null) {
            ""
        } else {
            ""
        }

        if (blurredImageView.surahNameEntity != null) {
            if (mTemplate.entitySurahTemplate == null) {
                mTemplate.entitySurahTemplate = EntitySurahTemplate(
                    blurredImageView.surahNameEntity!!.name,
                    blurredImageView.surahNameEntity!!.reader,
                    mTemplate.mDrawingTranslationX + blurredImageView.rectFSurahName.left,
                    mTemplate.mDrawingTranslationY + blurredImageView.rectFSurahName.top,
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
                mTemplate.entitySurahTemplate!!.clrBg = blurredImageView.surahNameEntity!!.clrBg
                mTemplate.entitySurahTemplate!!.isHaveBg = blurredImageView.surahNameEntity!!.isHaveBg
                mTemplate.entitySurahTemplate!!.index_surah = blurredImageView.surahNameEntity!!.index_surah
                mTemplate.entitySurahTemplate!!.style = blurredImageView.surahNameEntity!!.style
                mTemplate.entitySurahTemplate!!.clr = blurredImageView.surahNameEntity!!.clrS_name
                mTemplate.entitySurahTemplate!!.preset = blurredImageView.surahNameEntity!!.mPreset
                mTemplate.entitySurahTemplate!!.name_font = blurredImageView.surahNameEntity!!.nameFont
                mTemplate.entitySurahTemplate!!.scaleFactor = blurredImageView.surahNameEntity!!.scaleFactor
                mTemplate.entitySurahTemplate!!.rectF = MRectF(blurredImageView.surahNameEntity!!.copyRect!!.left, blurredImageView.surahNameEntity!!.copyRect!!.top, blurredImageView.surahNameEntity!!.copyRect!!.right, blurredImageView.surahNameEntity!!.copyRect!!.bottom)
                mTemplate.entitySurahTemplate!!.name = blurredImageView.surahNameEntity!!.name
                mTemplate.entitySurahTemplate!!.reader = blurredImageView.surahNameEntity!!.reader
                mTemplate.entitySurahTemplate!!.setPos(blurredImageView.rectFSurahName.left + mTemplate.mDrawingTranslationX, blurredImageView.rectFSurahName.top + mTemplate.mDrawingTranslationY)
            }
        }

        if (mTemplate.entityProgressTemplate == null) {
            mTemplate.entityProgressTemplate = EntityProgressTemplate(Utils.f2(blurredImageView.rectFProgress.left + mTemplate.mDrawingTranslationX), Utils.f2(blurredImageView.rectFProgress.top + mTemplate.mDrawingTranslationY))
        } else {
            mTemplate.entityProgressTemplate!!.left = Utils.f2(blurredImageView.rectFProgress.left + mTemplate.mDrawingTranslationX)
            mTemplate.entityProgressTemplate!!.top = Utils.f2(blurredImageView.rectFProgress.top + mTemplate.mDrawingTranslationY)
        }

        mTemplate.entityMediaList.clear()
        for (entityAudio in trackViewEntity.entityListAudio) {
            if (entityAudio.visible() && entityAudio.end > entityAudio.start) {
                val entityMedia = EntityMedia(
                    entityAudio.uri.toString(), entityAudio.min_duration, entityAudio.start, entityAudio.end,
                    entityAudio.rect.left / trackViewEntity.scaleFactor, entityAudio.rect.right / trackViewEntity.scaleFactor,
                    Math.round(entityAudio.end - entityAudio.start), entityAudio.offset, entityAudio.offset_right, entityAudio.offset_left,
                    entityAudio.max, entityAudio.fade_in, entityAudio.fade_out,
                    (entityAudio.rect.left / trackViewEntity.second_in_screen) * 1000.0f
                )
                entityMedia.paths_https = entityAudio.paths_http
                entityMedia.effectAudio = entityAudio.effectAudio
                entityMedia.path_ffmpeg = entityAudio.path_ffmpeg
                entityMedia.path_ffmpeg_effect = entityAudio.path_ffmpeg_effect
                entityMedia.video_path = entityAudio.video_path
                entityMedia.isApplyEffectInPreview = entityAudio.isApplyEffectInPreview
                mTemplate.addMedia(entityMedia)
            }
        }
        mTemplate.uri_video = FileHelper(this).createPublicVideoFolder(mResources.getString(R.string.app_name)).absolutePath + "/" + System.currentTimeMillis() + "_NurMontage.mp4"
        LocalPersistence.writeTemplate(this, mTemplate, str, Common.TEMPLATE_TMP)
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
        engineActivity.mTemplate.setNewCode()
        engineActivity.mTemplate.isGlass = engineActivity.blurredImageView.isGlass
        engineActivity.mTemplate.currentCursur = engineActivity.trackViewEntity.current_cursur_position
        engineActivity.mTemplate.scale_timeline = engineActivity.trackViewEntity.scaleFactor
        engineActivity.mTemplate.duration = engineActivity.trackViewEntity.maxTime
        engineActivity.mTemplate.gradient = engineActivity.blurredImageView.color_gradient
        engineActivity.mTemplate.color_ipad = engineActivity.blurredImageView.colorIpad()
        engineActivity.mTemplate.quranEntityList.clear()
        engineActivity.mTemplate.translationTemplateList.clear()
        engineActivity.mTemplate.uri_bg = engineActivity.uri_bg

        try {
            for (entityQuranTimeline in engineActivity.trackViewEntity.entityListQuran) {
                if (entityQuranTimeline.visible()) {
                    val f2 = Utils.f2(Math.abs(entityQuranTimeline.rect.left / engineActivity.trackViewEntity.second_in_screen))
                    val f22 = Utils.f2(Math.abs(entityQuranTimeline.rect.right / engineActivity.trackViewEntity.second_in_screen))
                    if (entityQuranTimeline.quranEntity.copyRect == null) {
                        entityQuranTimeline.quranEntity.setCopyRect()
                    }
                    val entityQuranTemplate = EntityQuranTemplate(
                        entityQuranTimeline.transition, f2, f22,
                        entityQuranTimeline.quranEntity.copyRect!!.left * engineActivity.mTemplate.width,
                        engineActivity.mTemplate.height * entityQuranTimeline.quranEntity.copyRect!!.top,
                        entityQuranTimeline.rect.left / entityQuranTimeline.scaleFactor,
                        entityQuranTimeline.rect.right / entityQuranTimeline.scaleFactor,
                        entityQuranTimeline.quranEntity.txt, entityQuranTimeline.quranEntity.complete_aya,
                        entityQuranTimeline.quranEntity.nameFont, entityQuranTimeline.quranEntity.indexNumber,
                        entityQuranTimeline.quranEntity.number, entityQuranTimeline.quranEntity.clrAya,
                        if (entityQuranTimeline.quranEntity.paintTranslationAya != null) entityQuranTimeline.quranEntity.clrTrsl else InputDeviceCompat.SOURCE_ANY,
                        entityQuranTimeline.quranEntity.mPreset
                    )
                    entityQuranTemplate.height = (entityQuranTimeline.quranEntity.copyRect!!.bottom * engineActivity.mTemplate.height) - (entityQuranTimeline.quranEntity.copyRect!!.top * engineActivity.mTemplate.height)
                    entityQuranTemplate.factor_size = entityQuranTimeline.quranEntity.factorSize
                    entityQuranTemplate.factor_sizeTrl = entityQuranTimeline.quranEntity.factorSizeTrl
                    entityQuranTemplate.scale = entityQuranTimeline.quranEntity.scaleFactor
                    entityQuranTemplate.translation = entityQuranTimeline.quranEntity.translation
                    entityQuranTemplate.translation_complete = entityQuranTimeline.quranEntity.translation_complete
                    entityQuranTemplate.startWord_index = entityQuranTimeline.quranEntity.startWord_index
                    entityQuranTemplate.endWord_index = entityQuranTimeline.quranEntity.endWord_index
                    entityQuranTemplate.icon = entityQuranTimeline.quranEntity.icon
                    entityQuranTemplate.file = entityQuranTimeline.file
                    entityQuranTemplate.file_in = entityQuranTimeline.file_in
                    entityQuranTemplate.file_out = entityQuranTimeline.file_out
                    entityQuranTemplate.rectF = MRectF(entityQuranTimeline.quranEntity.copyRect!!.left, entityQuranTimeline.quranEntity.copyRect!!.top, entityQuranTimeline.quranEntity.copyRect!!.right, entityQuranTimeline.quranEntity.copyRect!!.bottom)
                    engineActivity.mTemplate.addQuranEntityList(entityQuranTemplate)
                }
            }
        } catch (e: Exception) {
            Log.e("save templete quran", "" + e.message)
        }

        try {
            for (entityTrslTimeline in engineActivity.trackViewEntity.entityListTrslQuran) {
                if (entityTrslTimeline.visible()) {
                    val f23 = Utils.f2(Math.abs(entityTrslTimeline.rect.left / engineActivity.trackViewEntity.second_in_screen))
                    val f24 = Utils.f2(Math.abs(entityTrslTimeline.rect.right / engineActivity.trackViewEntity.second_in_screen))
                    if (entityTrslTimeline.quranEntity.copyRect == null) {
                        entityTrslTimeline.quranEntity.setCopyRect()
                    }
                    val entityTranslationTemplate = EntityTranslationTemplate(
                        entityTrslTimeline.transition, f23, f24,
                        entityTrslTimeline.quranEntity.copyRect!!.left * engineActivity.mTemplate.width,
                        engineActivity.mTemplate.height * entityTrslTimeline.quranEntity.copyRect!!.top,
                        entityTrslTimeline.rect.left / entityTrslTimeline.scaleFactor,
                        entityTrslTimeline.rect.right / entityTrslTimeline.scaleFactor,
                        entityTrslTimeline.quranEntity.txt, entityTrslTimeline.quranEntity.nameFont,
                        entityTrslTimeline.quranEntity.number, entityTrslTimeline.quranEntity.clrAya,
                        entityTrslTimeline.quranEntity.mPreset
                    )
                    entityTranslationTemplate.height = (entityTrslTimeline.quranEntity.copyRect!!.bottom * engineActivity.mTemplate.height) - (entityTrslTimeline.quranEntity.copyRect!!.top * engineActivity.mTemplate.height)
                    entityTranslationTemplate.factor_size = entityTrslTimeline.quranEntity.factorSize
                    entityTranslationTemplate.factor_sizeTrl = entityTrslTimeline.quranEntity.factorSizeTrl
                    entityTranslationTemplate.scale = entityTrslTimeline.quranEntity.scaleFactor
                    entityTranslationTemplate.file = entityTrslTimeline.file
                    entityTranslationTemplate.file_in = entityTrslTimeline.file_in
                    entityTranslationTemplate.file_out = entityTrslTimeline.file_out
                    entityTranslationTemplate.clr_bg = entityTrslTimeline.quranEntity.clrBg
                    entityTranslationTemplate.isHaveBg = entityTrslTimeline.quranEntity.isHaveBg
                    entityTranslationTemplate.rectF = MRectF(entityTrslTimeline.quranEntity.copyRect!!.left, entityTrslTimeline.quranEntity.copyRect!!.top, entityTrslTimeline.quranEntity.copyRect!!.right, entityTrslTimeline.quranEntity.copyRect!!.bottom)
                    engineActivity.mTemplate.addTrslEntityList(entityTranslationTemplate)
                }
            }
        } catch (e2: Exception) {
            Log.e("save templete trsl quran", "" + e2.message)
        }

        engineActivity.mTemplate.entityIsti3adaTemplate = null
        if (engineActivity.blurredImageView.mIsti3adhaEntity != null && engineActivity.blurredImageView.mIsti3adhaEntity!!.bismilahTimeline.visible()) {
            val bismilahTimeline = engineActivity.blurredImageView.mIsti3adhaEntity!!.bismilahTimeline
            val f25 = Utils.f2(Math.abs(bismilahTimeline.rect.left / engineActivity.trackViewEntity.second_in_screen))
            val f26 = Utils.f2(Math.abs(bismilahTimeline.rect.right / engineActivity.trackViewEntity.second_in_screen))
            if (bismilahTimeline.quranEntity.copyRect == null) {
                bismilahTimeline.quranEntity.setCopyRect()
            }
            val entityBismilahTemplate = EntityBismilahTemplate(
                bismilahTimeline.transition, f25, f26,
                bismilahTimeline.quranEntity.copyRect!!.left * engineActivity.mTemplate.width,
                engineActivity.mTemplate.height * bismilahTimeline.quranEntity.copyRect!!.top,
                bismilahTimeline.rect.left / bismilahTimeline.scaleFactor,
                bismilahTimeline.rect.right / bismilahTimeline.scaleFactor,
                bismilahTimeline.quranEntity.txt, bismilahTimeline.quranEntity.clrAya,
                bismilahTimeline.quranEntity.mPreset
            )
            entityBismilahTemplate.height = (bismilahTimeline.quranEntity.copyRect!!.bottom * engineActivity.mTemplate.height) - (bismilahTimeline.quranEntity.copyRect!!.top * engineActivity.mTemplate.height)
            entityBismilahTemplate.factor_size = bismilahTimeline.quranEntity.factorSize
            entityBismilahTemplate.scale = bismilahTimeline.quranEntity.scaleFactor
            entityBismilahTemplate.file = bismilahTimeline.file
            entityBismilahTemplate.file_in = bismilahTimeline.file_in
            entityBismilahTemplate.file_out = bismilahTimeline.file_out
            entityBismilahTemplate.rectF = MRectF(bismilahTimeline.quranEntity.copyRect!!.left, bismilahTimeline.quranEntity.copyRect!!.top, bismilahTimeline.quranEntity.copyRect!!.right, bismilahTimeline.quranEntity.copyRect!!.bottom)
            engineActivity.mTemplate.entityIsti3adaTemplate = entityBismilahTemplate
        }

        engineActivity.mTemplate.entityBismilahTemplate = null
        if (engineActivity.blurredImageView.bismilahEntity != null && engineActivity.blurredImageView.bismilahEntity!!.bismilahTimeline.visible()) {
            val bismilahTimeline2 = engineActivity.blurredImageView.bismilahEntity!!.bismilahTimeline
            val f27 = Utils.f2(Math.abs(bismilahTimeline2.rect.left / engineActivity.trackViewEntity.second_in_screen))
            val f28 = Utils.f2(Math.abs(bismilahTimeline2.rect.right / engineActivity.trackViewEntity.second_in_screen))
            if (bismilahTimeline2.quranEntity.copyRect == null) {
                bismilahTimeline2.quranEntity.setCopyRect()
            }
            val entityBismilahTemplate2 = EntityBismilahTemplate(
                bismilahTimeline2.transition, f27, f28,
                bismilahTimeline2.quranEntity.copyRect!!.left * engineActivity.mTemplate.width,
                engineActivity.mTemplate.height * bismilahTimeline2.quranEntity.copyRect!!.top,
                bismilahTimeline2.rect.left / bismilahTimeline2.scaleFactor,
                bismilahTimeline2.rect.right / bismilahTimeline2.scaleFactor,
                bismilahTimeline2.quranEntity.txt, bismilahTimeline2.quranEntity.clrAya,
                bismilahTimeline2.quranEntity.mPreset
            )
            entityBismilahTemplate2.height = (bismilahTimeline2.quranEntity.copyRect!!.bottom * engineActivity.mTemplate.height) - (bismilahTimeline2.quranEntity.copyRect!!.top * engineActivity.mTemplate.height)
            entityBismilahTemplate2.factor_size = bismilahTimeline2.quranEntity.factorSize
            entityBismilahTemplate2.scale = bismilahTimeline2.quranEntity.scaleFactor
            entityBismilahTemplate2.file = bismilahTimeline2.file
            entityBismilahTemplate2.file_in = bismilahTimeline2.file_in
            entityBismilahTemplate2.file_out = bismilahTimeline2.file_out
            entityBismilahTemplate2.rectF = MRectF(bismilahTimeline2.quranEntity.copyRect!!.left, bismilahTimeline2.quranEntity.copyRect!!.top, bismilahTimeline2.quranEntity.copyRect!!.right, bismilahTimeline2.quranEntity.copyRect!!.bottom)
            engineActivity.mTemplate.entityBismilahTemplate = entityBismilahTemplate2
        }

        if (engineActivity.blurredImageView.surahNameEntity != null) {
            if (engineActivity.mTemplate.entitySurahTemplate == null) {
                try {
                    if (engineActivity.blurredImageView.surahNameEntity!!.copyRect == null) {
                        engineActivity.blurredImageView.surahNameEntity!!.setCopyRect()
                    }
                    engineActivity.mTemplate.entitySurahTemplate = EntitySurahTemplate(
                        engineActivity.blurredImageView.surahNameEntity!!.name,
                        engineActivity.blurredImageView.surahNameEntity!!.reader,
                        engineActivity.mTemplate.mDrawingTranslationX + engineActivity.blurredImageView.rectFSurahName.left,
                        engineActivity.mTemplate.mDrawingTranslationY + engineActivity.blurredImageView.rectFSurahName.top,
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
                engineActivity.mTemplate.entitySurahTemplate!!.clrBg = engineActivity.blurredImageView.surahNameEntity!!.clrBg
                engineActivity.mTemplate.entitySurahTemplate!!.isHaveBg = engineActivity.blurredImageView.surahNameEntity!!.isHaveBg
                engineActivity.mTemplate.entitySurahTemplate!!.index_surah = engineActivity.blurredImageView.surahNameEntity!!.index_surah
                engineActivity.mTemplate.entitySurahTemplate!!.style = engineActivity.blurredImageView.surahNameEntity!!.style
                engineActivity.mTemplate.entitySurahTemplate!!.clr = engineActivity.blurredImageView.surahNameEntity!!.clrS_name
                engineActivity.mTemplate.entitySurahTemplate!!.preset = engineActivity.blurredImageView.surahNameEntity!!.mPreset
                engineActivity.mTemplate.entitySurahTemplate!!.name_font = engineActivity.blurredImageView.surahNameEntity!!.nameFont
                engineActivity.mTemplate.entitySurahTemplate!!.scaleFactor = engineActivity.blurredImageView.surahNameEntity!!.scaleFactor
                engineActivity.mTemplate.entitySurahTemplate!!.rectF = MRectF(engineActivity.blurredImageView.surahNameEntity!!.copyRect!!.left, engineActivity.blurredImageView.surahNameEntity!!.copyRect!!.top, engineActivity.blurredImageView.surahNameEntity!!.copyRect!!.right, engineActivity.blurredImageView.surahNameEntity!!.copyRect!!.bottom)
                engineActivity.mTemplate.entitySurahTemplate!!.name = engineActivity.blurredImageView.surahNameEntity!!.name
                engineActivity.mTemplate.entitySurahTemplate!!.reader = engineActivity.blurredImageView.surahNameEntity!!.reader
                engineActivity.mTemplate.entitySurahTemplate!!.setPos(engineActivity.blurredImageView.rectFSurahName.left + engineActivity.mTemplate.mDrawingTranslationX, engineActivity.blurredImageView.rectFSurahName.top + engineActivity.mTemplate.mDrawingTranslationY)
            }
        }

        if (engineActivity.mTemplate.entityProgressTemplate == null) {
            engineActivity.mTemplate.entityProgressTemplate = EntityProgressTemplate(Utils.f2(engineActivity.blurredImageView.rectFProgress.left + engineActivity.mTemplate.mDrawingTranslationX), Utils.f2(engineActivity.blurredImageView.rectFProgress.top + engineActivity.mTemplate.mDrawingTranslationY))
        } else {
            engineActivity.mTemplate.entityProgressTemplate!!.left = Utils.f2(engineActivity.blurredImageView.rectFProgress.left + engineActivity.mTemplate.mDrawingTranslationX)
            engineActivity.mTemplate.entityProgressTemplate!!.top = Utils.f2(engineActivity.blurredImageView.rectFProgress.top + engineActivity.mTemplate.mDrawingTranslationY)
        }

        engineActivity.mTemplate.entityMediaList.clear()
        val it2 = engineActivity.trackViewEntity.entityListAudio.iterator()
        while (it2.hasNext()) {
            val next = it2.next()
            if (next.visible() && next.end > next.start) {
                val entityMedia = EntityMedia(
                    next.uri.toString(), next.min_duration, next.start, next.end,
                    next.rect.left / engineActivity.trackViewEntity.scaleFactor,
                    next.rect.right / engineActivity.trackViewEntity.scaleFactor,
                    Math.round(next.end - next.start), next.offset, next.offset_right, next.offset_left,
                    next.max, next.fade_in, next.fade_out,
                    (next.rect.left / engineActivity.trackViewEntity.second_in_screen) * 1000.0f
                )
                entityMedia.paths_https = next.paths_http
                entityMedia.effectAudio = next.effectAudio
                entityMedia.path_ffmpeg = next.path_ffmpeg
                entityMedia.video_path = next.video_path
                entityMedia.path_ffmpeg_effect = next.path_ffmpeg_effect
                entityMedia.isApplyEffectInPreview = next.isApplyEffectInPreview
                engineActivity.mTemplate.addMedia(entityMedia)
                next.release()
            }
        }
        val idStr = "Template_" + System.currentTimeMillis()
        val idTemplate = engineActivity.mTemplate.idTemplate
        engineActivity.mTemplate.idTemplate = idStr
        engineActivity.mTemplate.uri_video = FileHelper(engineActivity).createPublicVideoFolder(engineActivity.mResources.getString(R.string.app_name)).absolutePath + "/" + System.currentTimeMillis() + "_NurMontage.mp4"
        val template = engineActivity.mTemplate
        LocalPersistence.writeTemplate(engineActivity, template, idTemplate, template.idTemplate)
        LocalPersistence.deleteTemplate(engineActivity, Common.TEMPLATE_TMP)
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
        activityLauncher.launch(intent)
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
            Toast.makeText(this, mResources.getString(R.string.permission_img), Toast.LENGTH_SHORT).show()
        }
    }
    if (i == 2) {
        if (iArr.isNotEmpty() && iArr[0] == 0) {
            pickAudio()
        } else {
            Toast.makeText(this, mResources.getString(R.string.permission_audio), Toast.LENGTH_SHORT).show()
        }
    }
    if (i == 10) {
        if ((Build.VERSION.SDK_INT >= 34 && ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == 0) || (iArr.isNotEmpty() && iArr[0] == 0)) {
            imageChooser()
        } else {
            Toast.makeText(this, mResources.getString(R.string.permission_img), Toast.LENGTH_SHORT).show()
        }
    }
    if (i == 11) {
        if ((Build.VERSION.SDK_INT >= 34 && ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == 0) || (iArr.isNotEmpty() && iArr[0] == 0)) {
            videoChooser()
        } else {
            Toast.makeText(this, mResources.getString(R.string.permission_video), Toast.LENGTH_SHORT).show()
        }
    }
    if (i == 12) {
        if ((Build.VERSION.SDK_INT >= 34 && ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == 0) || (iArr.isNotEmpty() && iArr[0] == 0)) {
            videoChooserForAudio()
        } else {
            Toast.makeText(this, mResources.getString(R.string.permission_video), Toast.LENGTH_SHORT).show()
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
    timeFormatter = TimeFormatter(maxTime)
    val smoothTimelineAnimator = SmoothTimelineAnimator(startCursur, maxTime, object : SmoothTimelineAnimator.AnimatorListener {
        override fun onUpdate(i: Int) {
            if (!mIsPlaying || i == 0) return
            val f = i.toFloat() / maxTime
            if (blurredImageView != null) {
                updateTime(i.toLong())
                blurredImageView.setProgress(f)
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
                trackViewEntity.setPlaying(mIsPlaying)
                blurredImageView.setPlaying(mIsPlaying)
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
    if (mTemplate.isVideoSquare) {
        start()
    }
}

fun startTimelineAnimationPreview(entityAudio: EntityAudio) {
    val maxTime = trackViewEntity.maxTime
    val timeLineW = trackViewEntity.timeLineW
    timeFormatter = TimeFormatter(maxTime)
    val smoothTimelineAnimator = SmoothTimelineAnimator(startCursur, maxTime, object : SmoothTimelineAnimator.AnimatorListener {
        override fun onUpdate(i: Int) {
            if (!mIsPlaying || i == 0) return
            val f = i.toFloat() / maxTime
            if (blurredImageView != null) {
                updateTime(i.toLong())
                blurredImageView.setProgress(f)
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
                trackViewEntity.setPlaying(mIsPlaying)
                blurredImageView.setPlaying(mIsPlaying)
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
    if (mTemplate.isVideoSquare) {
        start()
    }
}

// =====================================================================
// updateTime(long) / initTimeLineView()
// =====================================================================

private fun updateTime(j: Long) {
    val tf = timeFormatter
    if (tf == null) {
        timeFormatter = TimeFormatter(trackViewEntity.maxTime)
    } else {
        tf.setTotalDurationMs(trackViewEntity.maxTime)
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
    trackViewEntity.setScaleFactor(mTemplate.scale_timeline)
    trackViewEntity.post {
        val screenWidth = ScreenUtils.getScreenWidth(this@EngineActivity)
        val f = screenWidth * 0.12f
        trackViewEntity.setSecond_in_screen(f)
        trackViewEntity.setSecond_in_screen(f, 0, screenWidth)
        trackViewEntity.setMaxTime(0)
        trackViewEntity.init(screenWidth, trackViewEntity.height)
        trackViewEntity.setPosCursur(mTemplate.currentCursur)
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
        if (mTemplate.entityMediaList != null) {
            updateProgress(i + 1, mTemplate.entityMediaList.size)
        }
        val uri2 = if (str != null) {
            uri.path
        } else if (!uri.toString().contains("share_with_me")) {
            AudioUtils.copyFromUri(this, uri, mTemplate.folder_template)
        } else {
            uri.toString()
        }
        val str2 = uri2 ?: return
        val entityMedia = mTemplate.entityMediaList[i]

        if (entityMedia.isApplyEffectInPreview) {
            val file = File(mTemplate.folder_template, System.currentTimeMillis().toString() + "_audio_echo.mp3")
            val effectAudio = entityMedia.effectAudio
            val start = effectAudio.start / 1000.0f
            val end = effectAudio.end / 1000.0f
            val arrayList = ArrayList<String>()
            arrayList.add("atrim=start=$start:end=$end")
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
                arrayList.add("afade=t=out:st=${(end - start) - fade_out}:d=$fade_out")
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
                                addEntitMediaHttp(entityMedia, effectAudio.duration, uri, mediaPlayer, entityMedia.paths_https, i, str2, str)
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
                addEntitMediaHttp(entityMedia, mediaPlayer2.duration, uri, mediaPlayer2, entityMedia.paths_https, i, str2, str)
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
        if (ProgressViewFragment.instance != null) {
            ProgressViewFragment.instance!!.update(i, i2)
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
        val posX = if (mTemplate.isNewCode) entityMedia.posX else (entityMedia.posX / 1000.0f) * trackViewEntity.second_in_screen
        val posY = if (mTemplate.isNewCode) entityMedia.posY else (entityMedia.posY / 1000.0f) * trackViewEntity.second_in_screen
        EntityAudio(null, uri, posX, 0.0f, round, posY, entityMedia.max, trackViewEntity.second_in_screenNoScale, i, entityMedia.offset, entityMedia.offset_right, entityMedia.offset_left).also { ea ->
            ea.setPathHttp(list)
            ea.mediaPlayer = mediaPlayer
            ea.video_path = str2
            ea.start = entityMedia.start
            ea.min_duration = entityMedia.start_original
            if (entityMedia.end != 0.0f) {
                ea.end = entityMedia.end
            }
            ea.effectAudio = entityMedia.effectAudio
            ea.fade_in = entityMedia.duration_fade_in
            ea.fade_out = entityMedia.duration_fade_out
            trackViewEntity.addAudio(ea)
        }
    } else null

    if (round2 <= 0 || round <= 0) {
        trackViewEntity.invalidate()
        hideProgressFragment()
        return
    }
    try {
        val file = File(mTemplate.folder_template, System.currentTimeMillis().toString() + "_output.pcm")
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
                if (ReturnCode.isSuccess(fFmpegSession.returnCode)) {
                    try {
                        entityAudio?.setAmps(PCMWaveformExtractor.extractWaveform(file.absolutePath, round2 / ((round * 0.1f).toInt() + (round * 0.07f).toInt()), round2, round))
                        entityAudio?.path_ffmpeg = str
                        val i4 = i2 + 1
                        if (i4 >= mTemplate.entityMediaList.size) {
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
                            val entityMedia2 = mTemplate.entityMediaList[i4]
                            if (entityMedia2.video_path != null) {
                                entityMedia.video_path = AudioUtils.copyFromUri(this@EngineActivity, Uri.parse(mTemplate.uri_upload_extract_audio_video), mTemplate.folder_template)
                                if (mTemplate.extension != null) {
                                    addAudioFromVideoWithExtention(mTemplate.extension!!, entityMedia.video_path, i4)
                                } else {
                                    start_extenstion = 0
                                    extractAudioFromVideoRecursive(entityMedia.video_path, 0, true, i4)
                                }
                            } else if (entityMedia2.paths_https != null) {
                                addAudioRecitersTemplate(entityMedia2.paths_https!!, i4, null)
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
        val audio = trackViewEntity.audio
        val scaleFactor = if (trackViewEntity.entityListAudio.isEmpty() || audio == null) 0.0f else audio.rect.right / trackViewEntity.scaleFactor
        val round = Math.round(trackViewEntity.width * 0.077f)
        val round2 = Math.round(trackViewEntity.second_in_screenNoScale * (i / 1000.0f))
        val f = round2.toFloat()
        val entityAudio = EntityAudio(null, uri, scaleFactor, 0.0f, round, f + scaleFactor, f, trackViewEntity.second_in_screenNoScale, i)
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
        val copyFromUri = AudioUtils.copyFromUri(this, uri, mTemplate.folder_template)
        val f = i.toFloat()
        entityAudio.setAmps(PCMWaveformExtractor.extractWaveform(str, i2 / ((0.1f * f).toInt() + (f * 0.07f).toInt()), i2, i))
        entityAudio.path_ffmpeg = copyFromUri
        if (i3 != -1) {
            val i4 = i3 + 1
            if (i4 >= mTemplate.entityMediaList.size) {
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
            val entityMedia = mTemplate.entityMediaList[i3]
            val entityMedia2 = mTemplate.entityMediaList[i4]
            if (entityMedia2.video_path != null) {
                entityMedia.video_path = AudioUtils.copyFromUri(this, Uri.parse(mTemplate.uri_upload_extract_audio_video), mTemplate.folder_template)
                if (mTemplate.extension != null) {
                    addAudioFromVideoWithExtention(mTemplate.extension!!, entityMedia.video_path, i4)
                    return
                } else {
                    start_extenstion = 0
                    extractAudioFromVideoRecursive(entityMedia.video_path, 0, true, i4)
                    return
                }
            }
            if (entityMedia2.paths_https != null) {
                addAudioRecitersTemplate(entityMedia2.paths_https!!, i4, null)
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
        val posX = if (mTemplate.isNewCode) entityMedia.posX else (entityMedia.posX / 1000.0f) * trackViewEntity.second_in_screen
        val posY = if (mTemplate.isNewCode) entityMedia.posY else (entityMedia.posY / 1000.0f) * trackViewEntity.second_in_screen
        EntityAudio(null, uri, posX, 0.0f, round, posY, entityMedia.max, trackViewEntity.second_in_screenNoScale, i, entityMedia.offset, entityMedia.offset_right, entityMedia.offset_left).also { ea ->
            ea.setPathHttp(list)
            ea.mediaPlayer = mediaPlayer
            ea.video_path = str3
            ea.start = entityMedia.start
            ea.min_duration = entityMedia.start_original
            if (entityMedia.end != 0.0f) {
                ea.end = entityMedia.end
            }
            ea.effectAudio = entityMedia.effectAudio
            ea.fade_in = entityMedia.duration_fade_in
            ea.fade_out = entityMedia.duration_fade_out
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
                entityAudio2?.setAmps(PCMWaveformExtractor.extractWaveform(str2, round2 / ((round * 0.1f).toInt() + (round * 0.07f).toInt()), round2, round))
                entityAudio2?.path_ffmpeg = str
                val i4 = i2 + 1
                if (i4 >= mTemplate.entityMediaList.size) {
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
                    val entityMedia2 = mTemplate.entityMediaList[i4]
                    if (entityMedia2.video_path != null) {
                        entityMedia.video_path = AudioUtils.copyFromUri(this@EngineActivity, Uri.parse(mTemplate.uri_upload_extract_audio_video), mTemplate.folder_template)
                        if (mTemplate.extension != null) {
                            addAudioFromVideoWithExtention(mTemplate.extension!!, entityMedia.video_path, i4)
                        } else {
                            start_extenstion = 0
                            extractAudioFromVideoRecursive(entityMedia.video_path, 0, true, i4)
                        }
                    } else if (entityMedia2.paths_https != null) {
                        addAudioRecitersTemplate(entityMedia2.paths_https!!, i4, null)
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
            if (mediaPlayer2 != null && i < mTemplate.entityMediaList.size) {
                addEntitMediaHttp(mTemplate.entityMediaList[i], mediaPlayer2.duration, uri, mPlayer!!, list, i, str, str2, str3)
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
        val audio = trackViewEntity.audio
        val scaleFactor = if (trackViewEntity.entityListAudio.isEmpty() || audio == null) 0.0f else audio.rect.right / trackViewEntity.scaleFactor
        val round = Math.round(trackViewEntity.width * 0.077f)
        val round2 = Math.round(trackViewEntity.second_in_screenNoScale * (i / 1000.0f))
        val f = round2.toFloat()
        val entityAudio = EntityAudio(null, uri, scaleFactor, 0.0f, round, f + scaleFactor, f, trackViewEntity.second_in_screenNoScale, i)
        entityAudio.mediaPlayer = mPlayer
        entityAudio.effectAudio.end = entityAudio.end
        entityAudio.effectAudio.start = entityAudio.start
        entityAudio.effectAudio.duration = (entityAudio.end - entityAudio.start).toInt()
        trackViewEntity.addAudio(entityAudio)
        if (round2 > 0 && round > 0) {
            val copyFromUri = AudioUtils.copyFromUri(this, uri, mTemplate.folder_template)
            val file = File(mTemplate.folder_template, System.currentTimeMillis().toString() + "_output.pcm")
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
                    if (ReturnCode.isSuccess(fFmpegSession.returnCode)) {
                        try {
                            entityAudio.setAmps(PCMWaveformExtractor.extractWaveform(file.absolutePath, round2 / ((round * 0.1f).toInt() + (round * 0.07f).toInt()), round2, round))
                            entityAudio.path_ffmpeg = uri.path
                            entityAudio.video_path = str
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
        val audio = trackViewEntity.audio
        val scaleFactor = if (trackViewEntity.entityListAudio.isEmpty() || audio == null) 0.0f else audio.rect.right / trackViewEntity.scaleFactor
        val round = Math.round(trackViewEntity.width * 0.077f)
        val round2 = Math.round(trackViewEntity.second_in_screenNoScale * (i / 1000.0f))
        val f = round2.toFloat()
        val entityAudio = EntityAudio(null, uri, scaleFactor, 0.0f, round, f + scaleFactor, f, trackViewEntity.second_in_screenNoScale, i)
        entityAudio.effectAudio.end = entityAudio.end
        entityAudio.effectAudio.start = entityAudio.start
        entityAudio.effectAudio.duration = (entityAudio.end - entityAudio.start).toInt()
        entityAudio.mediaPlayer = mediaPlayer
        trackViewEntity.addAudio(entityAudio)
        if (round2 > 0 && round > 0) {
            AudioUtils.copyToLocalAsync(this, uri.toString(), mTemplate.folder_template, object : AudioUtils.Callback {
                override fun onSuccess(str: String) {
                    try {
                        val file = File(mTemplate.folder_template, System.currentTimeMillis().toString() + "_audio_wave.png")
                        id_ffmpeg.add(FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", str, "-filter_complex", "aformat=channel_layouts=mono,showwavespic=s=${round}x${round2}:colors=#522123", "-frames:v", "1", "-y", file.absolutePath), object : FFmpegSessionCompleteCallback {
                            override fun apply(fFmpegSession: FFmpegSession) {
                                if (ReturnCode.isSuccess(fFmpegSession.returnCode)) {
                                    try {
                                        Glide.with(this@EngineActivity as FragmentActivity).asBitmap().load(Uri.fromFile(file)).submit().get()
                                        entityAudio.path_ffmpeg = str
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
        val entityAudio2 = EntityAudio(null, entityAudio.uri, f, entityAudio.rect.top, entityAudio.h, f + entityAudio.rect.width(), entityAudio.max, entityAudio.second_in_screen, (i / 1000.0f).toInt(), 0.0f, 0.0f, 0.0f)
        entityAudio2.setAmps(entityAudio.amps)
        entityAudio2.setRenderer(entityAudio.getRenderer())
        entityAudio2.addPathHttp(entityAudio.paths_http)
        entityAudio2.mediaPlayer = entityAudio.mediaPlayer
        entityAudio2.rect.bottom = entityAudio.rect.bottom
        entityAudio2.path_ffmpeg = entityAudio.path_ffmpeg
        entityAudio2.effectAudio = entityAudio.effectAudio
        entityAudio2.video_path = entityAudio.video_path
        entityAudio2.isApplyEffectInPreview = entityAudio.isApplyEffectInPreview
        entityAudio2.scaleFactor = entityAudio.scaleFactor
        entityAudio2.setIndex(entityAudio.index + 1)
        entityAudio2.offset_right = entityAudio.offset_right
        entityAudio2.offset_left = entityAudio.offset_left
        entityAudio2.offset = entityAudio.offset
        entityAudio2.end = Math.round((Math.abs(Math.round((entityAudio.rect.right / trackViewEntity.second_in_screen) * 1000.0f)) - Math.abs(Math.round((entityAudio.rect.left / trackViewEntity.second_in_screen) * 1000.0f))) + entityAudio.start)
        entityAudio2.start = entityAudio.start
        entityAudio2.min_duration = entityAudio.min_duration
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
        val audio = trackViewEntity.audio
        val scaleFactor = if (trackViewEntity.entityListAudio.isEmpty() || audio == null) 0.0f else audio.rect.right / trackViewEntity.scaleFactor
        val round = Math.round(trackViewEntity.width * 0.077f)
        val round2 = Math.round(trackViewEntity.second_in_screenNoScale * (i / 1000.0f))
        val f = round2.toFloat()
        val entityAudio = EntityAudio(null, uri, scaleFactor, 0.0f, round, f + scaleFactor, f, trackViewEntity.second_in_screenNoScale, i)
        entityAudio.mediaPlayer = mPlayer
        entityAudio.effectAudio.end = entityAudio.end
        entityAudio.effectAudio.start = entityAudio.start
        entityAudio.effectAudio.duration = (entityAudio.end - entityAudio.start).toInt()
        trackViewEntity.addAudio(entityAudio)
        if (round2 > 0 && round > 0) {
            val uri2 = if (!uri.toString().contains("share_with_me")) {
                AudioUtils.copyFromUri(this, uri, mTemplate.folder_template)
            } else {
                uri.toString()
            }
            val file = File(mTemplate.folder_template, System.currentTimeMillis().toString() + "_output.pcm")
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
                    if (ReturnCode.isSuccess(fFmpegSession.returnCode)) {
                        try {
                            entityAudio.setAmps(PCMWaveformExtractor.extractWaveform(file.absolutePath, round2 / ((round * 0.1f).toInt() + (round * 0.07f).toInt()), round2, round))
                            entityAudio.path_ffmpeg = str
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
    val file = File(mTemplate.folder_template, System.currentTimeMillis().toString() + "_audio_echo.mp3")
    val fromFile = Uri.fromFile(file)
    id_ffmpeg.add(FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", entityAudio.path_ffmpeg, "-af", createCmd, "-y", file.absolutePath), object : FFmpegSessionCompleteCallback {
        override fun apply(fFmpegSession: FFmpegSession) {
            if (ReturnCode.isSuccess(fFmpegSession.returnCode)) {
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
                            entityAudio.setRight(entityAudio.rect.left + Math.round(trackViewEntity.second_in_screen * (mediaPlayer.duration / 1000.0f)))
                            entityAudio.duration = mediaPlayer.duration * 1000
                            entityAudio.end = mediaPlayer.duration.toFloat()
                            entityAudio.start = 0.0f
                            entityAudio.max = (entityAudio.rect.right / entityAudio.scaleFactor) - ((entityAudio.rect.left / entityAudio.scaleFactor) - entityAudio.offset_left)
                            trackViewEntity.updateWhenEffect(entityAudio)
                        }
                        entityAudio.mediaPlayer = this@EngineActivity.mPlayer
                        applyffectAll(effectAudio, intValue + 1)
                    }
                    entityAudio.path_ffmpeg_effect = file.absolutePath
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
    val file = File(mTemplate.folder_template, System.currentTimeMillis().toString() + "_audio_echo.mp3")
    val uri = Uri.fromFile(file)
    id_ffmpeg.add(FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", entityAudio.path_ffmpeg, "-af", str, "-y", file.absolutePath), object : FFmpegSessionCompleteCallback {
        override fun apply(fFmpegSession: FFmpegSession) {
            if (ReturnCode.isSuccess(fFmpegSession.returnCode)) {
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
                            entityAudio.setRight(entityAudio.rect.left + Math.round(trackViewEntity.second_in_screen * (mediaPlayer.duration / 1000.0f)))
                            entityAudio.duration = mediaPlayer.duration * 1000
                            entityAudio.max = (entityAudio.rect.right / entityAudio.scaleFactor) - ((entityAudio.rect.left / entityAudio.scaleFactor) - entityAudio.offset_left)
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
                    entityAudio.path_ffmpeg_effect = file.absolutePath
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
// addTimeLineBismilah / addTimeLineIsti3ada / addTimeLineQuran overloads
// =====================================================================

fun addTimeLineBismilah(bismilahEntity: BismilahEntity, f: Float, f2: Float): EntityBismilahTimeline {
    val entityBismilahTimeline = EntityBismilahTimeline(bismilahEntity, f, 0.0f, trackViewEntity.width * 0.077f, f2, trackViewEntity.second_in_screen)
    trackViewEntity.setBismilahTimeline(entityBismilahTimeline)
    return entityBismilahTimeline
}

fun addTimeLineIsti3ada(bismilahEntity: BismilahEntity, f: Float, f2: Float): EntityBismilahTimeline {
    val entityBismilahTimeline = EntityBismilahTimeline(bismilahEntity, f, 0.0f, trackViewEntity.width * 0.077f, f2, trackViewEntity.second_in_screen)
    trackViewEntity.setmIsi3adaTimeline(entityBismilahTimeline)
    return entityBismilahTimeline
}

fun addTimeLineQuran(quranEntity: QuranEntity, f: Float, f2: Float): EntityQuranTimeline {
    val entityQuranTimeline = EntityQuranTimeline(quranEntity, f, 0.0f, trackViewEntity.width * 0.077f, f2, trackViewEntity.second_in_screen)
    trackViewEntity.addQuran(entityQuranTimeline)
    return entityQuranTimeline
}

fun addTimeLineQuran(translationQuranEntity: TranslationQuranEntity, f: Float, f2: Float): EntityTrslTimeline {
    val entityTrslTimeline = EntityTrslTimeline(translationQuranEntity, f, 0.0f, trackViewEntity.width * 0.077f, f2, trackViewEntity.second_in_screen)
    trackViewEntity.addTrslQuran(entityTrslTimeline)
    return entityTrslTimeline
}

fun addTimeLineQuran(i: Int, quranEntity: QuranEntity, f: Float, f2: Float): EntityQuranTimeline {
    val entityQuranTimeline = EntityQuranTimeline(quranEntity, f, 0.0f, trackViewEntity.width * 0.077f, f2, trackViewEntity.second_in_screen)
    trackViewEntity.addQuran(entityQuranTimeline, i)
    return entityQuranTimeline
}

fun addTimeLineQuran(i: Int, translationQuranEntity: TranslationQuranEntity, f: Float, f2: Float): EntityTrslTimeline {
    val entityTrslTimeline = EntityTrslTimeline(translationQuranEntity, f, 0.0f, trackViewEntity.width * 0.077f, f2, trackViewEntity.second_in_screen)
    trackViewEntity.addTrslQuran(entityTrslTimeline, i)
    return entityTrslTimeline
}

fun splitTimeLineQuran(i: Int, quranEntity: QuranEntity, f: Float, f2: Float, f3: Float): EntityQuranTimeline {
    val entityQuranTimeline = EntityQuranTimeline(quranEntity, f, 0.0f, trackViewEntity.width * 0.077f, f2, trackViewEntity.second_in_screen)
    entityQuranTimeline.scaleFactor = f3
    trackViewEntity.addQuran_split(entityQuranTimeline, i)
    return entityQuranTimeline
}

fun splitTimeLineQuran(i: Int, translationQuranEntity: TranslationQuranEntity, f: Float, f2: Float, f3: Float): EntityTrslTimeline {
    val entityTrslTimeline = EntityTrslTimeline(translationQuranEntity, f, 0.0f, trackViewEntity.width * 0.077f, f2, trackViewEntity.second_in_screen)
    entityTrslTimeline.scaleFactor = f3
    trackViewEntity.addQuran_split(entityTrslTimeline, i)
    return entityTrslTimeline
}

fun addTimeLineQuran(quranEntity: QuranEntity): EntityQuranTimeline {
    var xCursur = trackViewEntity.xCursur
    val quran = trackViewEntity.quran
    if (quran != null) {
        xCursur = quran.rect.right
    }
    val trackEntityView = trackViewEntity
    if (trackEntityView.isExist(trackEntityView.bismilahTimeline)) {
        xCursur = Math.max(xCursur, trackViewEntity.bismilahTimeline!!.rect.right)
    }
    val f = xCursur
    val entityQuranTimeline = EntityQuranTimeline(quranEntity, f, 0.0f, trackViewEntity.width * 0.077f, f + trackViewEntity.second_in_screen * 4.0f, trackViewEntity.second_in_screen)
    trackViewEntity.addQuran(entityQuranTimeline)
    return entityQuranTimeline
}

fun addTimeLineTrslQuran(translationQuranEntity: TranslationQuranEntity): EntityTrslTimeline {
    var xCursur = trackViewEntity.xCursur
    val trslQuran = trackViewEntity.trslQuran
    if (trslQuran != null) {
        xCursur = trslQuran.rect.right
    }
    val entityTrslTimeline = EntityTrslTimeline(translationQuranEntity, xCursur, 0.0f, trackViewEntity.width * 0.077f, trackViewEntity.quran!!.rect.right, trackViewEntity.second_in_screen)
    trackViewEntity.addTrslQuran(entityTrslTimeline)
    return entityTrslTimeline
}

fun addTimeLineBismilah(bismilahEntity: BismilahEntity): EntityBismilahTimeline {
    val f = trackViewEntity.mIsi3adaTimeline?.rect?.right ?: 0.0f
    val entityBismilahTimeline = EntityBismilahTimeline(bismilahEntity, f, 0.0f, trackViewEntity.width * 0.077f, f + trackViewEntity.second_in_screen * 4.0f, trackViewEntity.second_in_screen)
    trackViewEntity.setBismilahTimeline(entityBismilahTimeline)
    return entityBismilahTimeline
}

fun addTimeLineIsti3ada(bismilahEntity: BismilahEntity): EntityBismilahTimeline {
    val entityBismilahTimeline = EntityBismilahTimeline(bismilahEntity, 0.0f, 0.0f, trackViewEntity.width * 0.077f, trackViewEntity.second_in_screen * 4.0f + 0.0f, trackViewEntity.second_in_screen)
    trackViewEntity.setmIsi3adaTimeline(entityBismilahTimeline)
    return entityBismilahTimeline
}

// =====================================================================
// enableUndoBtn / enableRedoBtn / disableRedoBtn / disableUndoBtn
// =====================================================================

private fun enableUndoBtn() {
    try {
        val imageButton = btnUndo
        if (imageButton == null || imageButton.isEnabled) return
        runOnUiThread {
            btnUndo?.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
            btnUndo?.isEnabled = true
            btnUndo?.isClickable = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun enableRedoBtn() {
    try {
        val imageButton = btnRedo
        if (imageButton == null || imageButton.isEnabled) return
        runOnUiThread {
            btnRedo?.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
            btnRedo?.isEnabled = true
            btnRedo?.isClickable = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun disableRedoBtn() {
    try {
        val imageButton = btnRedo
        if (imageButton == null || !imageButton.isEnabled) return
        runOnUiThread {
            btnRedo?.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
            btnRedo?.isEnabled = false
            btnRedo?.isClickable = false
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun disableUndoBtn() {
    try {
        val imageButton = btnUndo
        if (imageButton == null || !imageButton.isEnabled) return
        runOnUiThread {
            btnUndo?.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
            btnUndo?.isEnabled = false
            btnUndo?.isClickable = false
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    }

    fun addTimeLineTrslQuran(translationQuranEntity: TranslationQuranEntity): EntityTrslTimeline {
        var xCursur = trackViewEntity.getXCursur()
        val trslQuran = trackViewEntity.getTrslQuran()
        if (trslQuran != null) {
            xCursur = trslQuran.rect.right
        }
        val entityTrslTimeline = EntityTrslTimeline(
            translationQuranEntity, xCursur, 0.0f,
            trackViewEntity.getWidth() * 0.077f,
            trackViewEntity.getQuran().rect.right,
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
        trackViewEntity.setBismilahTimeline(entityBismilahTimeline)
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
        val z = mTemplate.getIpad_type() == IpadType.GRADIENT.ordinal ||
                mTemplate.getIpad_type() == IpadType.MASK_BRUSH.ordinal ||
                mTemplate.getIpad_type() == IpadType.BLACK_LAYER.ordinal
        val nameFont = if (blurredImageView.getQuranEntities().isEmpty()) {
            Common.FONT_QURAN
        } else {
            blurredImageView.getQuranEntities()[0].getNameFont()
        }
        val str6 = nameFont
        val quranEntity = QuranEntity(
            this, DrawableHelper.getIDDrawableIconByName(str5),
            str, str2, str3, str4,
            blurredImageView.getRectFAya(),
            UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/$str6"),
            Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf"),
            i, i2,
            UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/خط فارس الكوفي.otf"),
            blurredImageView.getClr_aya(),
            blurredImageView.getClr_trsl(),
            str6, z
        )
        quranEntity.setIpad_type(mTemplate.getIpad_type())
        quranEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        quranEntity.setStartWord_index(i3)
        quranEntity.setEndWord_index(i4)
        quranEntity.setIcon(str5)
        quranEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        val addTimeLineQuran = addTimeLineQuran(quranEntity)
        addTimeLineQuran.setscaleFactor(trackViewEntity.getScaleFactor())
        quranEntity.setEntityQuran(addTimeLineQuran)
        addTimeLineQuran.setEntityView(quranEntity)
        blurredImageView.addEntity(quranEntity)
    }

    private fun addTranslationEntity(str: String, i: Int, z: Boolean) {
        val translationQuranEntity = TranslationQuranEntity(
            str, blurredImageView.getRectFAya(),
            Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf"),
            i, InputDeviceCompat.SOURCE_ANY, "ReadexPro_Medium.ttf",
            blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height()
        )
        translationQuranEntity.setIpad_type(mTemplate.getIpad_type())
        translationQuranEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        translationQuranEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        val addTimeLineTrslQuran = addTimeLineTrslQuran(translationQuranEntity)
        addTimeLineTrslQuran.setscaleFactor(trackViewEntity.getScaleFactor())
        translationQuranEntity.setEntityTrslTimeline(addTimeLineTrslQuran)
        addTimeLineTrslQuran.setEntityView(translationQuranEntity)
        blurredImageView.addEntity(translationQuranEntity)
    }

    private fun addEntityBissmilah(
        str: String, f: Float, f2: Float, i: Int,
        transition: Transition, f3: Float, f4: Float,
        rectF: RectF?, i2: Int
    ) {
        val loadFontFromAsset = UtilsFileLast.loadFontFromAsset(this, "fonts/خط البسملة.ttf")
        val rectF2 = if (rectF == null) {
            blurredImageView.getRectFAya()
        } else {
            RectF(
                rectF.left * blurredImageView.getmCanvas_width(),
                rectF.top * blurredImageView.getmCanvas_height(),
                rectF.right * blurredImageView.getmCanvas_width(),
                rectF.bottom * blurredImageView.getmCanvas_height()
            )
        }
        val bismilahEntity = BismilahEntity(str, rectF2, loadFontFromAsset, i, i2)
        bismilahEntity.setFcSize(f4)
        bismilahEntity.setFactor_scale(f3)
        bismilahEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        if (bismilahEntity.getFactorSize() == 1.0f) {
            bismilahEntity.createStaticLayout()
        } else {
            bismilahEntity.setupScaleSave(bismilahEntity.getFactorSize(), blurredImageView.getmCanvas_width())
        }
        bismilahEntity.initPreset(i2)
        bismilahEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        val addTimeLineBismilah = addTimeLineBismilah(bismilahEntity, f, f2)
        bismilahEntity.setBismilahTimeline(addTimeLineBismilah)
        addTimeLineBismilah.setTransition(transition)
        addTimeLineBismilah.setEntityView(bismilahEntity)
        blurredImageView.addBismilahEntity(bismilahEntity)
    }

    private fun addEntityIsti3ada(
        str: String, f: Float, f2: Float, i: Int,
        transition: Transition, f3: Float, f4: Float,
        rectF: RectF?, i2: Int
    ) {
        val loadFontFromAsset = UtilsFileLast.loadFontFromAsset(this, "fonts/خط الاستعاذه.ttf")
        val rectF2 = if (rectF == null) {
            blurredImageView.getRectFAya()
        } else {
            RectF(
                rectF.left * blurredImageView.getmCanvas_width(),
                rectF.top * blurredImageView.getmCanvas_height(),
                rectF.right * blurredImageView.getmCanvas_width(),
                rectF.bottom * blurredImageView.getmCanvas_height()
            )
        }
        val bismilahEntity = BismilahEntity(str, rectF2, loadFontFromAsset, i, i2)
        bismilahEntity.setFcSize(f4)
        bismilahEntity.setFactor_scale(f3)
        bismilahEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        if (bismilahEntity.getFactorSize() == 1.0f) {
            bismilahEntity.createStaticLayout()
        } else {
            bismilahEntity.setupScaleSave(bismilahEntity.getFactorSize(), blurredImageView.getmCanvas_width())
        }
        bismilahEntity.initPreset(i2)
        bismilahEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        val addTimeLineIsti3ada = addTimeLineIsti3ada(bismilahEntity, f, f2)
        bismilahEntity.setBismilahTimeline(addTimeLineIsti3ada)
        addTimeLineIsti3ada.setTransition(transition)
        addTimeLineIsti3ada.setEntityView(bismilahEntity)
        blurredImageView.addIsti3adhaEntity(bismilahEntity)
    }

    private fun addEntityBissmilah(): Boolean {
        if (blurredImageView.getBismilahEntity() != null) {
            if (blurredImageView.getBismilahEntity()!!.getBismilahTimeline().visible()) {
                return false
            }
            blurredImageView.getBismilahEntity()!!.getBismilahTimeline().visible(true)
            return false
        }
        val bismilahEntity = BismilahEntity(
            "1", blurredImageView.getRectFAya(),
            UtilsFileLast.loadFontFromAsset(this, "fonts/خط البسملة.ttf"),
            blurredImageView.getClr_aya()
        )
        bismilahEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        bismilahEntity.setFcSize(bismilahEntity.getPaintAya().textSize / blurredImageView.getmCanvas_width())
        bismilahEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        val addTimeLineBismilah = addTimeLineBismilah(bismilahEntity)
        addTimeLineBismilah.setscaleFactor(trackViewEntity.getScaleFactor())
        bismilahEntity.setBismilahTimeline(addTimeLineBismilah)
        addTimeLineBismilah.setEntityView(bismilahEntity)
        blurredImageView.addBismilahEntity(bismilahEntity)
        if (trackViewEntity.getQuran() != null) {
            trackViewEntity.translateToRightBismilah(addTimeLineBismilah)
        }
        return true
    }

    private fun addEntityIste3adha(): Boolean {
        if (blurredImageView.getmIsti3adhaEntity() != null) {
            if (blurredImageView.getmIsti3adhaEntity()!!.getBismilahTimeline().visible()) {
                return false
            }
            blurredImageView.getmIsti3adhaEntity()!!.getBismilahTimeline().visible(true)
            return false
        }
        val bismilahEntity = BismilahEntity(
            "4", blurredImageView.getRectFAya(),
            UtilsFileLast.loadFontFromAsset(this, "fonts/خط الاستعاذه.ttf"),
            blurredImageView.getClr_aya()
        )
        bismilahEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        bismilahEntity.setFcSize(bismilahEntity.getPaintAya().textSize / blurredImageView.getmCanvas_width())
        bismilahEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        val addTimeLineIsti3ada = addTimeLineIsti3ada(bismilahEntity)
        addTimeLineIsti3ada.setscaleFactor(trackViewEntity.getScaleFactor())
        bismilahEntity.setBismilahTimeline(addTimeLineIsti3ada)
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
            typefaceNumber = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/خط فارس الكوفي.otf")
        }
        val typeface = typefaceNumber
        var typeface2 = quranEntity.getPaintAya().typeface
        if (typeface2 == null) {
            typeface2 = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/${quranEntity.getNameFont()}")
        }
        val typeface3 = typeface2
        var typeface4: Typeface? = quranEntity.getPaintTranslationAya()?.typeface
        if (typeface4 == null) {
            typeface4 = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")
        }
        val quranEntity2 = QuranEntity(
            quranEntity.getTxt(), quranEntity.getComplete_aya(),
            quranEntity.getTranslation(), quranEntity.getTranslation_complete(),
            blurredImageView.getRectFAya(), typeface3, typeface4,
            quranEntity.getIndexNumber(), quranEntity.getNumber(), typeface,
            quranEntity.getClrAya(), quranEntity.getClrTrsl(), quranEntity.getNameFont(),
            quranEntity.getPaintAya().textSize,
            quranEntity.getPaintTranslationAya()?.textSize ?: 0f,
            quranEntity.getPaintAya().isUnderlineText,
            quranEntity.getVectorDrawable()
        )
        quranEntity2.setFcSize(quranEntity.getFactorSize())
        quranEntity2.setFactorSizeTrl(quranEntity.getFactorSizeTrl())
        quranEntity2.setFactor_scale(quranEntity.getFactor_scale())
        quranEntity2.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        quranEntity2.setIpad_type(mTemplate.getIpad_type())
        quranEntity2.setStartWord_index(quranEntity.getStartWord_index())
        quranEntity2.setEndWord_index(quranEntity.getEndWord_index())
        quranEntity2.setIcon(quranEntity.getIcon())
        quranEntity2.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        quranEntity2.setVisible(false)
        quranEntity2.setupScaleSave(quranEntity2.getFactorSize(), blurredImageView.getmCanvas_width())
        quranEntity2.setColor(quranEntity.getClrAya())
        quranEntity2.setColorTranslation(
            if (quranEntity.getPaintTranslationAya() != null) quranEntity.getClrTrsl() else InputDeviceCompat.SOURCE_ANY
        )
        quranEntity2.initPreset(quranEntity.getmPreset())
        val addTimeLineQuran = addTimeLineQuran(
            quranEntity.getEntityQuran().index + 1, quranEntity2,
            quranEntity.getEntityQuran().rect.right,
            quranEntity.getEntityQuran().rect.right + quranEntity.getEntityQuran().rect.width()
        )
        addTimeLineQuran.setscaleFactor(quranEntity.getEntityQuran().getscaleFactor())
        quranEntity2.setEntityQuran(addTimeLineQuran)
        addTimeLineQuran.setEntityView(quranEntity2)
        if (quranEntity.getEntityQuran().getTransition() != null) {
            addTimeLineQuran.setTransition(quranEntity.getEntityQuran().getTransition()!!.duplicate())
        }
        blurredImageView.addEntity(quranEntity2, quranEntity.getIndex() + 1)
        trackViewEntity.selectEntity(quranEntity2.getEntityQuran(), false)
        iTrimLineCallback!!.onSelectEntity(quranEntity2.getEntityQuran(), -1.0f)
        trackViewEntity.updateCursurToSelectEntity()
    }

    private fun duplicateEntity(translationQuranEntity: TranslationQuranEntity) {
        var typeface = translationQuranEntity.getPaintAya().typeface
        if (typeface == null) {
            typeface = UtilsFileLast.loadFontFromAsset(this, "fonts/${translationQuranEntity.getNameFont()}")
        }
        val translationQuranEntity2 = TranslationQuranEntity(
            translationQuranEntity.getTxt(), translationQuranEntity.getRect(),
            typeface, translationQuranEntity.getNumber(),
            translationQuranEntity.getClrAya(), translationQuranEntity.getNameFont(),
            translationQuranEntity.getPaintAya().textSize
        )
        translationQuranEntity2.setFcSize(translationQuranEntity.getFactorSize())
        translationQuranEntity2.setFactorSizeTrl(translationQuranEntity.getFactorSizeTrl())
        translationQuranEntity2.setFactor_scale(translationQuranEntity.getFactor_scale())
        translationQuranEntity2.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        translationQuranEntity2.setIpad_type(mTemplate.getIpad_type())
        translationQuranEntity2.setVisible(false)
        translationQuranEntity2.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        translationQuranEntity2.updatePaint(
            translationQuranEntity.getPaintAya().textSize,
            translationQuranEntity.getStaticLayout().width
        )
        translationQuranEntity2.setColor(translationQuranEntity.getClrAya())
        translationQuranEntity2.initPreset(translationQuranEntity.getmPreset())
        val addTimeLineQuran = addTimeLineQuran(
            translationQuranEntity.getEntityTrslTimeline().index + 1,
            translationQuranEntity2,
            translationQuranEntity.getEntityTrslTimeline().rect.right,
            translationQuranEntity.getEntityTrslTimeline().rect.right + translationQuranEntity.getEntityTrslTimeline().rect.width()
        )
        val transition = translationQuranEntity.getEntityTrslTimeline().getTransition()
        if (transition != null) {
            addTimeLineQuran.setTransition(transition.duplicate())
            if (transition.isIn() && transition.isOut() {
                addTimeLineQuran.getTransition()!!.setIn(false)
                transition.setOut(false)
            } else if (transition.isIn() {
                addTimeLineQuran.getTransition()!!.setIn(false)
            } else if (transition.isOut() {
                transition.setOut(false)
            }
        }
        addTimeLineQuran.setscaleFactor(translationQuranEntity.getEntityTrslTimeline().getscaleFactor())
        translationQuranEntity2.setEntityTrslTimeline(addTimeLineQuran)
        addTimeLineQuran.setEntityView(translationQuranEntity2)
        if (translationQuranEntity.getEntityTrslTimeline().getTransition() != null) {
            addTimeLineQuran.setTransition(translationQuranEntity.getEntityTrslTimeline().getTransition()!!.duplicate())
        }
        blurredImageView.addEntity(translationQuranEntity2, translationQuranEntity.getIndex() + 1)
        trackViewEntity.selectEntity(translationQuranEntity2.getEntityTrslTimeline(), false)
        iTrimLineCallback!!.onSelectEntity(translationQuranEntity2.getEntityTrslTimeline(), -1.0f)
        trackViewEntity.updateCursurToSelectEntity()
    }

    private fun splitEntity(translationQuranEntity: TranslationQuranEntity) {
        val abs = abs(trackViewEntity.getXCursur())
        if (abs <= translationQuranEntity.getEntityTrslTimeline().rect.left ||
            abs >= translationQuranEntity.getEntityTrslTimeline().rect.right
        ) {
            return
        }
        val secondInScreen = trackViewEntity.getSecond_in_screen() * 0.2f
        if (abs <= translationQuranEntity.getEntityTrslTimeline().rect.left ||
            abs >= translationQuranEntity.getEntityTrslTimeline().rect.left + secondInScreen
        ) {
            if (abs >= translationQuranEntity.getEntityTrslTimeline().rect.right ||
                abs <= translationQuranEntity.getEntityTrslTimeline().rect.right - secondInScreen
            ) {
                var typeface = translationQuranEntity.getPaintAya().typeface
                if (typeface == null) {
                    typeface = UtilsFileLast.loadFontFromAsset(this, "fonts/${translationQuranEntity.getNameFont()}")
                }
                val translationQuranEntity2 = TranslationQuranEntity(
                    translationQuranEntity.getTxt(), translationQuranEntity.getRect(),
                    typeface, translationQuranEntity.getNumber(),
                    translationQuranEntity.getClrAya(), translationQuranEntity.getNameFont(),
                    translationQuranEntity.getPaintAya().textSize
                )
                translationQuranEntity2.setFcSize(translationQuranEntity.getFactorSize())
                translationQuranEntity2.setFactorSizeTrl(translationQuranEntity.getFactorSizeTrl())
                translationQuranEntity2.setFactor_scale(translationQuranEntity.getFactor_scale())
                translationQuranEntity2.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
                translationQuranEntity2.setIpad_type(mTemplate.getIpad_type())
                translationQuranEntity2.setViewWeakReference(
                    WeakReference(trackViewEntity), WeakReference(blurredImageView)
                )
                translationQuranEntity2.updatePaint(
                    translationQuranEntity.getPaintAya().textSize,
                    translationQuranEntity.getStaticLayout().width
                )
                translationQuranEntity2.setColor(translationQuranEntity.getClrAya())
                translationQuranEntity2.initPreset(translationQuranEntity.getmPreset())
                trackViewEntity.stackSplit(translationQuranEntity.getEntityTrslTimeline())
                val splitTimeLineQuran = splitTimeLineQuran(
                    translationQuranEntity.getEntityTrslTimeline().index + 1,
                    translationQuranEntity2,
                    abs(trackViewEntity.getCurrentPosition()),
                    translationQuranEntity.getEntityTrslTimeline().rect.right,
                    translationQuranEntity.getEntityTrslTimeline().getscaleFactor()
                )
                val transition = translationQuranEntity.getEntityTrslTimeline().getTransition()
                if (transition != null) {
                    splitTimeLineQuran.setTransition(transition.duplicate())
                    if (transition.isIn() && transition.isOut() {
                        splitTimeLineQuran.getTransition()!!.setIn(false)
                        transition.setOut(false)
                    } else if (transition.isIn() {
                        splitTimeLineQuran.getTransition()!!.setIn(false)
                    } else if (transition.isOut() {
                        transition.setOut(false)
                    }
                }
                translationQuranEntity.getEntityTrslTimeline().setCurrentRect()
                translationQuranEntity.getEntityTrslTimeline().setRight(abs(trackViewEntity.getCurrentPosition()))
                translationQuranEntity.getEntityTrslTimeline().onChange()
                translationQuranEntity2.setEntityTrslTimeline(splitTimeLineQuran)
                splitTimeLineQuran.setEntityView(translationQuranEntity2)
                if (translationQuranEntity.getEntityTrslTimeline().getTransition() != null) {
                    splitTimeLineQuran.setTransition(
                        translationQuranEntity.getEntityTrslTimeline().getTransition()!!.duplicate()
                    )
                }
                blurredImageView.addEntity(translationQuranEntity2, translationQuranEntity.getIndex() + 1)
                trackViewEntity.invalidate()
            }
        }
    }

    private fun splitEntity(quranEntity: QuranEntity) {
        val abs = abs(trackViewEntity.getXCursur())
        if (abs <= quranEntity.getEntityQuran().rect.left ||
            abs >= quranEntity.getEntityQuran().rect.right
        ) {
            return
        }
        val secondInScreen = trackViewEntity.getSecond_in_screen() * 0.2f
        if (abs <= quranEntity.getEntityQuran().rect.left ||
            abs >= quranEntity.getEntityQuran().rect.left + secondInScreen
        ) {
            if (abs >= quranEntity.getEntityQuran().rect.right ||
                abs <= quranEntity.getEntityQuran().rect.right - secondInScreen
            ) {
                var typefaceNumber = quranEntity.getTypefaceNumber()
                if (typefaceNumber == null) {
                    typefaceNumber = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/خط فارس الكوفي.otf")
                }
                val typeface = typefaceNumber
                var typeface2 = quranEntity.getPaintAya().typeface
                if (typeface2 == null) {
                    typeface2 = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/${quranEntity.getNameFont()}")
                }
                val typeface3 = typeface2
                var typeface4: Typeface? = quranEntity.getPaintTranslationAya()?.typeface
                if (typeface4 == null) {
                    typeface4 = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")
                }
                val quranEntity2 = QuranEntity(
                    quranEntity.getTxt(), quranEntity.getComplete_aya(),
                    quranEntity.getTranslation(), quranEntity.getTranslation_complete(),
                    blurredImageView.getRectFAya(), typeface3, typeface4,
                    quranEntity.getIndexNumber(), quranEntity.getNumber(), typeface,
                    quranEntity.getClrAya(), quranEntity.getClrTrsl(), quranEntity.getNameFont(),
                    quranEntity.getPaintAya().textSize,
                    quranEntity.getPaintTranslationAya()?.textSize ?: 0f,
                    quranEntity.getPaintAya().isUnderlineText,
                    quranEntity.getVectorDrawable()
                )
                quranEntity2.setFcSize(quranEntity.getFactorSize())
                quranEntity2.setFactorSizeTrl(quranEntity.getFactorSizeTrl())
                quranEntity2.setFactor_scale(quranEntity.getFactor_scale())
                quranEntity2.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
                quranEntity2.setIpad_type(mTemplate.getIpad_type())
                quranEntity2.setStartWord_index(quranEntity.getStartWord_index())
                quranEntity2.setEndWord_index(quranEntity.getEndWord_index())
                quranEntity2.setIcon(quranEntity.getIcon())
                quranEntity2.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
                quranEntity2.setupScaleSave(quranEntity2.getFactorSize(), blurredImageView.getmCanvas_width())
                quranEntity2.setColor(quranEntity.getClrAya())
                quranEntity2.setColorTranslation(
                    if (quranEntity.getPaintTranslationAya() != null) quranEntity.getClrTrsl() else InputDeviceCompat.SOURCE_ANY
                )
                quranEntity2.initPreset(quranEntity.getmPreset())
                trackViewEntity.stackSplit(quranEntity.getEntityQuran())
                val splitTimeLineQuran = splitTimeLineQuran(
                    quranEntity.getEntityQuran().index + 1, quranEntity2,
                    abs(trackViewEntity.getCurrentPosition()),
                    quranEntity.getEntityQuran().rect.right,
                    quranEntity.getEntityQuran().getscaleFactor()
                )
                val transition = quranEntity.getEntityQuran().getTransition()
                if (transition != null) {
                    splitTimeLineQuran.setTransition(transition.duplicate())
                    if (transition.isIn() && transition.isOut() {
                        splitTimeLineQuran.getTransition()!!.setIn(false)
                        transition.setOut(false)
                    } else if (transition.isIn() {
                        splitTimeLineQuran.getTransition()!!.setIn(false)
                    } else if (transition.isOut() {
                        transition.setOut(false)
                    }
                }
                quranEntity.getEntityQuran().setCurrentRect()
                quranEntity.getEntityQuran().setRight(abs(trackViewEntity.getCurrentPosition()))
                quranEntity.getEntityQuran().onChange()
                quranEntity2.setEntityQuran(splitTimeLineQuran)
                splitTimeLineQuran.setEntityView(quranEntity2)
                if (quranEntity.getEntityQuran().getTransition() != null) {
                    splitTimeLineQuran.setTransition(quranEntity.getEntityQuran().getTransition()!!.duplicate())
                }
                blurredImageView.addEntity(quranEntity2, quranEntity.getIndex() + 1)
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
            blurredImageView.getRectFAya()
        } else {
            RectF(
                rectF.left * blurredImageView.getmCanvas_width(),
                rectF.top * blurredImageView.getmCanvas_height(),
                rectF.right * blurredImageView.getmCanvas_width(),
                rectF.bottom * blurredImageView.getmCanvas_height()
            )
        }
        val quranEntity = QuranEntity(
            this, str, str2, str3, str4, rectF2, loadFontFromAsset,
            typeface2, i, i2, typeface, i3, i6, str5, z,
            DrawableHelper.getIDDrawableIconByName(str7)
        )
        quranEntity.setFcSize(f4)
        quranEntity.setFactorSizeTrl(f5)
        quranEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        quranEntity.setFactor_scale(f3)
        quranEntity.setIpad_type(mTemplate.getIpad_type())
        quranEntity.setStartWord_index(i4)
        quranEntity.setEndWord_index(i5)
        quranEntity.setIcon(str7)
        quranEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        if (quranEntity.getFactorSize() == 1.0f) {
            quranEntity.setTextSize(quranEntity.calculateTextSize())
        } else {
            quranEntity.setupScaleSave(quranEntity.getFactorSize(), blurredImageView.getmCanvas_width())
        }
        quranEntity.initPreset(i7)
        val addTimeLineQuran = addTimeLineQuran(quranEntity, f, f2)
        quranEntity.setEntityQuran(addTimeLineQuran)
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
            blurredImageView.getRectFAya()
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
            str, rectF2, loadFontFromAsset, i, i2, str2
        )
        translationQuranEntity.setHaveBg(z)
        translationQuranEntity.setClrBg(i4)
        translationQuranEntity.setFcSize(f4)
        translationQuranEntity.setCanvasWH(blurredImageView.getmCanvas_width(), blurredImageView.getmCanvas_height())
        translationQuranEntity.setFactor_scale(f3)
        translationQuranEntity.setIpad_type(mTemplate.getIpad_type())
        translationQuranEntity.setViewWeakReference(WeakReference(trackViewEntity), WeakReference(blurredImageView))
        if (translationQuranEntity.getFactorSize() == 1.0f) {
            translationQuranEntity.setTextSize(translationQuranEntity.calculateTextSize())
        } else {
            translationQuranEntity.setupScaleSave(translationQuranEntity.getFactorSize(), blurredImageView.getmCanvas_width())
        }
        translationQuranEntity.initPreset(i3)
        val addTimeLineQuran = addTimeLineQuran(translationQuranEntity, f, f2)
        translationQuranEntity.setEntityTrslTimeline(addTimeLineQuran)
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
                    val str = if (recitersModel.isTarteel()) {
                        "https://audio-cdn.tarteel.ai/quran/${recitersModel.getIdentifer()}/${recitersModel.getSurah_index()}${recitersModel.getNumber_aya()}.mp3"
                    } else {
                        "https://everyayah.com/data/${recitersModel.getIdentifer()}/${recitersModel.getSurah_index()}${recitersModel.getNumber_aya()}.mp3"
                    }
                    val downloadFile = AudioUtils.downloadFile(this, str, mTemplate.getFolder_template())
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
            val file = File(mTemplate.getFolder_template(), "concat_${System.currentTimeMillis()}.txt")
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(sb.toString().toByteArray())
            fileOutputStream.close()
            val file2 = File(mTemplate.getFolder_template(), "${System.currentTimeMillis()}_output.mp3")
            val file3 = File(mTemplate.getFolder_template(), "${System.currentTimeMillis()}_output.pcm")
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
                if (ReturnCode.isSuccess(fFmpegSession.returnCode)) {
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
                    downloadFile = AudioUtils.copyFromUri(this@EngineActivity, parse, mTemplate.getFolder_template())
                    if (downloadFile == null) {
                        sb.append("file '").append(downloadFile!!.replace("'", "\\'")).append("'\n")
                        i++
                        updateProgress(i, pathes.size)
                    }
                }
                downloadFile = AudioUtils.downloadFile(this@EngineActivity, uri, mTemplate.getFolder_template())
                if (downloadFile == null) {
                    sb.append("file '").append(downloadFile!!.replace("'", "\\'")).append("'\n")
                    i++
                    updateProgress(i, pathes.size)
                }
            }
            val file = File(mTemplate.getFolder_template(), "concat.txt")
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(sb.toString().toByteArray())
            fileOutputStream.close()
            val file2 = File(mTemplate.getFolder_template(), "${System.currentTimeMillis()}_output.mp3")
            val file3 = File(mTemplate.getFolder_template(), "${System.currentTimeMillis()}_output.pcm")
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
                    if (ReturnCode.isSuccess(fFmpegSession.returnCode)) {
                        if (valIndex >= 0 && valIndex < mTemplate.getEntityMediaList().size) {
                            val entityMedia = mTemplate.getEntityMediaList()[valIndex]
                            if (entityMedia.isApplyEffectInPreview()) {
                                val file4 = File(
                                    mTemplate.getFolder_template(),
                                    "${System.currentTimeMillis()}_audio_echo.mp3"
                                )
                                val effectAudio = entityMedia.getEffectAudio()
                                val start = effectAudio.getStart() / 1000.0f
                                val end = effectAudio.getEnd() / 1000.0f
                                val arrayList2Inner = ArrayList<String>()
                                arrayList2Inner.add("atrim=start=$start:end=$end")
                                arrayList2Inner.add("asetpts=N/SR/TB")
                                if (effectAudio.isRemoveNoice()) {
                                    arrayList2Inner.add("afftdn=nf=-25")
                                }
                                arrayList2Inner.add(
                                    String.format(Locale.US, "volume=%.2f", effectAudio.getVolume())
                                )
                                if (effectAudio.getFade_in() > 0) {
                                    arrayList2Inner.add("afade=t=in:st=0:d=${effectAudio.getFade_in()}")
                                }
                                if (effectAudio.getFade_out() > 0) {
                                    val fadeOut = effectAudio.getFade_out()
                                    arrayList2Inner.add(
                                        "afade=t=out:st=${(end - start) - fadeOut}:d=$fadeOut"
                                    )
                                }
                                if (effectAudio.isEnhance()) {
                                    arrayList2Inner.add(Common.ENHANCE_CMD)
                                }
                                if (effectAudio.getReverbPreset() != null) {
                                    arrayList2Inner.add(effectAudio.getReverbPreset()!!)
                                }
                                if (effectAudio.getDecays() > 0) {
                                    arrayList2Inner.add(
                                        String.format(
                                            Locale.US, "aecho=%.2f:%.2f:%s:%s",
                                            1.0f, effectAudio.getOutGain(),
                                            effectAudio.getDelays_cmd(), effectAudio.getDecays_cmd()
                                        )
                                    )
                                }
                                if (effectAudio.getSpeed() != 1.0f) {
                                    arrayList2Inner.addAll(buildSpeedFilters(effectAudio.getSpeed()))
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
            MyPrefereces.putVuCopyRight(this)
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
        Common.bitmap = blurredImageView.getBitmapOriginal()
        Common.rect = blurredImageView.getRectSquare()
        if (blurredImageView.getBitmapSquare() != null) {
            Common.MIN_SQUARE_W = blurredImageView.getBitmapSquare()!!.width
            Common.MIN_SQUARE_H = blurredImageView.getBitmapSquare()!!.height
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

    private val iChangeBgCallback = object : ChangeBgFragment.IChangeBgCallback {
        override fun onSubscribe() {
            dialogPremium(0)
        }

        override fun onCrop() {
            toCrop()
        }

        override fun onAdd(bgItem: BgItem) {
            if (bgItem.getName_drawable() == mTemplate.getName_drawable()) {
                return
            }
            if (ChangeBgFragment.instance != null) {
                ChangeBgFragment.instance!!.scrollToSelected()
            }
            mTemplate.setName_drawable(bgItem.getName_drawable())
            uri_bg = "android.resource://$packageName/drawable/${bgItem.getId()}"
            showProgressSimple()
            executor.execute {
                try {
                    try {
                        try {
                            mTemplate.setUri_bg(uri_bg)
                            var i = 0
                            mTemplate.setVideoSquare(false)
                            blurredImageView.setVideo(false)
                            val height = blurredImageView.height
                            blurredImageView.setBitmapOriginal(
                                Glide.with(this@EngineActivity as FragmentActivity)
                                    .asBitmap()
                                    .load(uri_bg)
                                    .override(height, height)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .submit().get()
                            )
                            val cropTo16x9 = when (mTemplate.geTypeResize()) {
                                ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(
                                    blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
                                )
                                ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(
                                    blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
                                )
                                else -> BitmapCropper.cropTo16x9(
                                    blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
                                )
                            }
                            blurredImageView.updatePosCanvas(cropTo16x9)
                            blurredImageView.updateIpad(cropTo16x9, mTemplate.getIpad_type(), mTemplate.geTypeResize())
                            var bitmap2: Bitmap?
                            var rect: Rect?
                            if (mTemplate.getIpad_type() == IpadType.IPAD_NEOMORPHIC.ordinal) {
                                val width = (blurredImageView.getIpad_rect().width() * 0.6f).toInt()
                                var round = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate.getX_square())
                                var round2 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate.getY_square())
                                var i2 = width + round
                                if (i2 > blurredImageView.getBitmapOriginal().width) {
                                    round -= i2 - blurredImageView.getBitmapOriginal().width
                                    i2 = blurredImageView.getBitmapOriginal().width
                                }
                                var i3 = width + round2
                                if (i3 > blurredImageView.getBitmapOriginal().height) {
                                    round2 -= i3 - blurredImageView.getBitmapOriginal().height
                                    i3 = blurredImageView.getBitmapOriginal().height
                                }
                                if (round < 0) {
                                    round = 0
                                }
                                if (round2 >= 0) {
                                    i = round2
                                }
                                val rect2 = Rect(round, i, i2, i3)
                                blurredImageView.setRadius_square(width)
                                val width2 = (blurredImageView.getBitmapOriginal().width * mTemplate.getWidth_square()).toInt()
                                val height2 = (blurredImageView.getBitmapOriginal().height * mTemplate.getHeight_square()).toInt()
                                val cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(
                                    blurredImageView.getBitmapOriginal(), rect2, width, width2, height2
                                )
                                rect2.right = rect2.left + width2
                                rect2.bottom = rect2.top + height2
                                blurredImageView.setRectSquare(rect2)
                                bitmap2 = cropToSquareWithRoundCorners
                                rect = rect2
                            } else {
                                if (mTemplate.getIpad_type() != IpadType.IPAD.ordinal &&
                                    mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal &&
                                    mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal
                                ) {
                                    val width3 = (blurredImageView.getIpad_rect().width() * 1.0f).toInt()
                                    val height3 = (cropTo16x9.height * 0.5355f).toInt()
                                    var round3 = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate.getX_square())
                                    var round4 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate.getY_square())
                                    var i4 = width3 + round3
                                    if (i4 > blurredImageView.getBitmapOriginal().width) {
                                        round3 -= i4 - blurredImageView.getBitmapOriginal().width
                                        i4 = blurredImageView.getBitmapOriginal().width
                                    }
                                    var i5 = height3 + round4
                                    if (i5 > blurredImageView.getBitmapOriginal().height) {
                                        round4 -= i5 - blurredImageView.getBitmapOriginal().height
                                        i5 = blurredImageView.getBitmapOriginal().height
                                    }
                                    if (round3 < 0) {
                                        round3 = 0
                                    }
                                    if (round4 < 0) {
                                        round4 = 0
                                    }
                                    val rect3 = Rect(round3, round4, i4, i5)
                                    val width4 = (blurredImageView.getBitmapOriginal().width * mTemplate.getWidth_square()).toInt()
                                    val height4 = (blurredImageView.getBitmapOriginal().height * mTemplate.getHeight_square()).toInt()
                                    val cropToSquare = UtilsBitmap.cropToSquare(
                                        blurredImageView.getBitmapOriginal(), rect3, width4, height4
                                    )
                                    blurredImageView.setBitmapSquare(cropToSquare)
                                    blurredImageView.setRadius_square(0)
                                    rect3.right = rect3.left + width4
                                    rect3.bottom = rect3.top + height4
                                    blurredImageView.setRectSquare(rect3)
                                    bitmap2 = cropToSquare
                                    rect = rect3
                                } else {
                                    val width5 = (blurredImageView.getIpad_rect().width() * 0.87530595f).toInt()
                                    val i6 = (width5 * 1.13f).toInt()
                                    val min = Math.min(width5, i6)
                                    var round5 = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate.getX_square())
                                    var round6 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate.getY_square())
                                    var i7 = width5 + round5
                                    if (i7 > blurredImageView.getBitmapOriginal().width) {
                                        round5 -= i7 - blurredImageView.getBitmapOriginal().width
                                        i7 = blurredImageView.getBitmapOriginal().width
                                    }
                                    var i8 = i6 + round6
                                    if (i8 > blurredImageView.getBitmapOriginal().height) {
                                        round6 -= i8 - blurredImageView.getBitmapOriginal().height
                                        i8 = blurredImageView.getBitmapOriginal().height
                                    }
                                    if (round5 < 0) {
                                        round5 = 0
                                    }
                                    if (round6 < 0) {
                                        round6 = 0
                                    }
                                    val rect4 = Rect(round5, round6, i7, i8)
                                    val bitmap: Bitmap? = if (mTemplate.getIpad_type() == IpadType.IPAD_CLASSIC.ordinal) {
                                        val width6 = (blurredImageView.getBitmapOriginal().width * mTemplate.getWidth_square()).toInt()
                                        val height5 = (blurredImageView.getBitmapOriginal().height * mTemplate.getHeight_square()).toInt()
                                        val cropToSquare2 = UtilsBitmap.cropToSquare(
                                            blurredImageView.getBitmapOriginal(), rect4, width6, height5
                                        )
                                        blurredImageView.setBitmapSquare(cropToSquare2)
                                        blurredImageView.setRadius_square(0)
                                        rect4.right = rect4.left + width6
                                        rect4.bottom = rect4.top + height5
                                        blurredImageView.setRectSquare(rect4)
                                        cropToSquare2
                                    } else {
                                        val i9 = (min * 0.10800001f).toInt()
                                        blurredImageView.setRadius_square(i9)
                                        val width7 = (blurredImageView.getBitmapOriginal().width * mTemplate.getWidth_square()).toInt()
                                        val height6 = (blurredImageView.getBitmapOriginal().height * mTemplate.getHeight_square()).toInt()
                                        val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(
                                            blurredImageView.getBitmapOriginal(), rect4, i9, width7, height6
                                        )
                                        rect4.right = rect4.left + width7
                                        rect4.bottom = rect4.top + height6
                                        blurredImageView.setRectSquare(rect4)
                                        cropToSquareWithRoundCorners2
                                    }
                                    bitmap2 = bitmap
                                    rect = rect4
                                }
                            }
                            when (mTemplate.getIpad_type()) {
                                IpadType.GRADIENT.ordinal -> blurredImageView.updateBitmap(
                                    UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1),
                                    bitmap2, ViewCompat.MEASURED_STATE_MASK,
                                    mTemplate.getIpad_type(), mTemplate.geTypeResize(), rect
                                )
                                IpadType.BLUE_TYPE.ordinal -> {
                                    if (blurredImageView.getColor_gradient() != null) {
                                        blurredImageView.updateBitmap(
                                            UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1),
                                            bitmap2, blurredImageView.getColor_gradient(),
                                            mTemplate.getIpad_type(), mTemplate.geTypeResize(), rect
                                        )
                                    } else {
                                        blurredImageView.updateBitmap(
                                            UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1),
                                            bitmap2, blurredImageView.getColor_ipad(),
                                            mTemplate.getIpad_type(), mTemplate.geTypeResize(), rect
                                        )
                                    }
                                }
                                else -> blurredImageView.updateBitmap(
                                    UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1),
                                    bitmap2, -1, mTemplate.getIpad_type(), mTemplate.geTypeResize(), rect
                                )
                            }
                            mTemplate.setColor_ipad(blurredImageView.colorIpad())
                            runOnUiThread {
                                blurredImageView.invalidate()
                            }
                            runOnUiThread {
                                hideProgressFragment()
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

        override fun onUploadVideo() {
            pickVideoFromGallery()
        }

        override fun onUploadImg() {
            pickImageFromGallery()
        }

        override fun onDone() {
            hideFragment()
        }

        override fun onCancel() {
            hideFragment()
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

    private val iDimensionCallback = object : DimensionAdabters.IDimensionCallback {
        override fun isCustomSize(z: Boolean, resizeType: ResizeType) {}

        override fun done() {
            hideFragment()
        }

        override fun onCustumSize(i: Int, i2: Int, i3: Int, str: String, i4: Int) {
            updateHitRatio(i3, str)
            if (i3 == mTemplate.geTypeResize()) {
                return
            }
            if (ResizeFragment.instance != null) {
                ResizeFragment.instance!!.scrollToSelectedPosition()
            }
            showProgressSimple()
            executor.execute {
                try {
                    try {
                        try {
                            blurredImageView.reset()
                            mTemplate.setResizeType(i3)
                            mTemplate.setImgResize(str)
                            val size = AspectRatioCalculator.getSize(i3, mTemplate.getResolution())
                            mTemplate.setWidthAndHeight(size.first, size.second)
                            blurredImageView.initCanvasDimension(
                                blurredImageView.width, blurredImageView.height, i3
                            )
                            val cropTo16x9 = when (mTemplate.geTypeResize()) {
                                ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(
                                    blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
                                )
                                ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(
                                    blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
                                )
                                else -> BitmapCropper.cropTo16x9(
                                    blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
                                )
                            }
                            blurredImageView.updatePosCanvas(cropTo16x9)
                            blurredImageView.setBitmapBlured(cropTo16x9)
                            blurredImageView.updateIpad(cropTo16x9, mTemplate.getIpad_type(), mTemplate.geTypeResize())
                        } finally {
                        }
                    } catch (e: Exception) {
                        Log.e("Tag resize : ", "init ${e.message}")
                        runOnUiThread {
                            hideProgressFragment()
                        }
                    }
                    var i5 = 0
                    if (mTemplate.getIpad_type() != IpadType.GRADIENT.ordinal &&
                        mTemplate.getIpad_type() != IpadType.BLACK_LAYER.ordinal &&
                        mTemplate.getIpad_type() != IpadType.MASK_BRUSH.ordinal &&
                        mTemplate.getIpad_type() != IpadType.BLUE_TYPE.ordinal &&
                        mTemplate.getIpad_type() != IpadType.CASSET_IMG.ordinal &&
                        mTemplate.getIpad_type() != IpadType.CASSET_IMG_BLUR.ordinal
                    ) {
                        var bitmap: Bitmap?
                        var rect: Rect?
                        if (mTemplate.getIpad_type() == IpadType.IPAD_NEOMORPHIC.ordinal) {
                            val width = (blurredImageView.getIpad_rect().width() * 0.6f).toInt()
                            var round = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate.getX_square())
                            var round2 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate.getY_square())
                            var i6 = width + round
                            if (i6 > blurredImageView.getBitmapOriginal().width) {
                                round -= i6 - blurredImageView.getBitmapOriginal().width
                                i6 = blurredImageView.getBitmapOriginal().width
                            }
                            var i7 = width + round2
                            if (i7 > blurredImageView.getBitmapOriginal().height) {
                                round2 -= i7 - blurredImageView.getBitmapOriginal().height
                                i7 = blurredImageView.getBitmapOriginal().height
                            }
                            if (round < 0) {
                                round = 0
                            }
                            if (round2 >= 0) {
                                i5 = round2
                            }
                            val rect2 = Rect(round, i5, i6, i7)
                            blurredImageView.setRadius_square(width)
                            val width2 = (blurredImageView.getBitmapOriginal().width * mTemplate.getWidth_square()).toInt()
                            val height = (blurredImageView.getBitmapOriginal().height * mTemplate.getHeight_square()).toInt()
                            val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(
                                blurredImageView.getBitmapOriginal(), rect2, width, width2, height
                            )
                            rect2.right = rect2.left + width2
                            rect2.bottom = rect2.top + height
                            blurredImageView.setRectSquare(rect2)
                            bitmap = cropToSquareWithRoundCorners2
                            rect = rect2
                        } else {
                            if (mTemplate.getIpad_type() != IpadType.IPAD.ordinal &&
                                mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal &&
                                mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal
                            ) {
                                val width3 = (blurredImageView.getIpad_rect().width() * 1.0f).toInt()
                                val height2 = (cropTo16x9!!.height * 0.5355f).toInt()
                                var round3 = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate.getX_square())
                                var round4 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate.getY_square())
                                var i8 = width3 + round3
                                if (i8 > blurredImageView.getBitmapOriginal().width) {
                                    round3 -= i8 - blurredImageView.getBitmapOriginal().width
                                    i8 = blurredImageView.getBitmapOriginal().width
                                }
                                var i9 = height2 + round4
                                if (i9 > blurredImageView.getBitmapOriginal().height) {
                                    round4 -= i9 - blurredImageView.getBitmapOriginal().height
                                    i9 = blurredImageView.getBitmapOriginal().height
                                }
                                if (round3 < 0) {
                                    round3 = 0
                                }
                                if (round4 < 0) {
                                    round4 = 0
                                }
                                val rect3 = Rect(round3, round4, i8, i9)
                                val width4 = (blurredImageView.getBitmapOriginal().width * mTemplate.getWidth_square()).toInt()
                                val height3 = (blurredImageView.getBitmapOriginal().height * mTemplate.getHeight_square()).toInt()
                                val cropToSquare = UtilsBitmap.cropToSquare(
                                    blurredImageView.getBitmapOriginal(), rect3, width4, height3
                                )
                                blurredImageView.setBitmapSquare(cropToSquare)
                                blurredImageView.setRadius_square(0)
                                rect3.right = rect3.left + width4
                                rect3.bottom = rect3.top + height3
                                blurredImageView.setRectSquare(rect3)
                                bitmap = cropToSquare
                                rect = rect3
                            } else {
                                val width5 = (blurredImageView.getIpad_rect().width() * 0.87530595f).toInt()
                                val i10 = (width5 * 1.13f).toInt()
                                val min = Math.min(width5, i10)
                                var round5 = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate.getX_square())
                                var round6 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate.getY_square())
                                var i11 = width5 + round5
                                if (i11 > blurredImageView.getBitmapOriginal().width) {
                                    round5 -= i11 - blurredImageView.getBitmapOriginal().width
                                    i11 = blurredImageView.getBitmapOriginal().width
                                }
                                var i12 = i10 + round6
                                if (i12 > blurredImageView.getBitmapOriginal().height) {
                                    round6 -= i12 - blurredImageView.getBitmapOriginal().height
                                    i12 = blurredImageView.getBitmapOriginal().height
                                }
                                if (round5 < 0) {
                                    round5 = 0
                                }
                                if (round6 < 0) {
                                    round6 = 0
                                }
                                val rect4 = Rect(round5, round6, i11, i12)
                                val cropToSquareWithRoundCorners: Bitmap? = if (mTemplate.getIpad_type() == IpadType.IPAD_CLASSIC.ordinal) {
                                    val width6 = (blurredImageView.getBitmapOriginal().width * mTemplate.getWidth_square()).toInt()
                                    val height4 = (blurredImageView.getBitmapOriginal().height * mTemplate.getHeight_square()).toInt()
                                    val cropToSquare2 = UtilsBitmap.cropToSquare(
                                        blurredImageView.getBitmapOriginal(), rect4, width6, height4
                                    )
                                    blurredImageView.setBitmapSquare(cropToSquare2)
                                    blurredImageView.setRadius_square(0)
                                    rect4.right = rect4.left + width6
                                    rect4.bottom = rect4.top + height4
                                    blurredImageView.setRectSquare(rect4)
                                    cropToSquare2
                                } else {
                                    val i13 = (min * 0.10800001f).toInt()
                                    blurredImageView.setRadius_square(i13)
                                    val width7 = (blurredImageView.getBitmapOriginal().width * mTemplate.getWidth_square()).toInt()
                                    val height5 = (blurredImageView.getBitmapOriginal().height * mTemplate.getHeight_square()).toInt()
                                    val result = UtilsBitmap.cropToSquareWithRoundCorners(
                                        blurredImageView.getBitmapOriginal(), rect4, i13, width7, height5
                                    )
                                    rect4.right = rect4.left + width7
                                    rect4.bottom = rect4.top + height5
                                    blurredImageView.setRectSquare(rect4)
                                    result
                                }
                                bitmap = cropToSquareWithRoundCorners
                                rect = rect4
                            }
                            if (blurredImageView.getColor_gradient() != null) {
                                blurredImageView.setBitmap(
                                    UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1),
                                    bitmap, blurredImageView.getColor_gradient(),
                                    mTemplate.getIpad_type(), i3, rect
                                )
                            } else {
                                blurredImageView.setBitmap(
                                    UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1),
                                    bitmap, blurredImageView.getColor_ipad(),
                                    mTemplate.getIpad_type(), i3, rect
                                )
                            }
                            blurredImageView.resizeEntity()
                            blurredImageView.updatePosSurahName()
                            runOnUiThread {
                                if (trackViewEntity.getCurrent_cursur_position() > trackViewEntity.getMaxTime()) {
                                    blurredImageView.invalidate()
                                }
                                trackViewEntity.invalidate()
                                updateTime()
                            }
                            runOnUiThread {
                                hideProgressFragment()
                            }
                        }
                        blurredImageView.setBitmapNotBlur(cropTo16x9!!)
                        val copy = cropTo16x9.copy(
                            cropTo16x9.config ?: Bitmap.Config.ARGB_8888, true
                        )
                        if (blurredImageView.getColor_gradient() != null) {
                            blurredImageView.setBitmap(
                                UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1),
                                copy, blurredImageView.getColor_gradient(),
                                mTemplate.getIpad_type(), i3, blurredImageView.getRectSquare()
                            )
                        } else {
                            blurredImageView.setBitmap(
                                UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1),
                                copy, blurredImageView.getColor_ipad(),
                                mTemplate.getIpad_type(), i3, blurredImageView.getRectSquare()
                            )
                        }
                        if (mTemplate.getIpad_type() == IpadType.CASSET_IMG_BLUR.ordinal) {
                            blurredImageView.setBitmapSquare(blurredImageView.getBitmapBlured())
                            blurredImageView.setRadius_square(0)
                        }
                        blurredImageView.resizeEntity()
                        blurredImageView.updatePosSurahName()
                        runOnUiThread {
                            if (trackViewEntity.getCurrent_cursur_position() > trackViewEntity.getMaxTime()) {
                                blurredImageView.invalidate()
                            }
                            trackViewEntity.invalidate()
                            updateTime()
                        }
                        runOnUiThread {
                            hideProgressFragment()
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
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
            Common.bitmap!!, blurredImageView.height, blurredImageView.height, false
        )
        blurredImageView.setBitmapOriginal(Common.bitmap)
        cropTo16x9 = when (mTemplate.geTypeResize()) {
            ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(
                blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
            )
            ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(
                blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
            )
            else -> BitmapCropper.cropTo16x9(
                blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
            )
        }
        blurredImageView.setBitmapBlured(UtilsBitmap.blur(this, cropTo16x9, 20, 1))
        blurredImageView.invalidate()
    }

    @Suppress("unused")
    fun onCropDataActivityResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == -1) {
            val data = activityResult.data ?: return
            mTemplate.setX_square(data.getFloatExtra("x", 0.3f))
            mTemplate.setY_square(data.getFloatExtra("y", 0.4f))
            mTemplate.setWidth_square(data.getFloatExtra("w", 1.0f))
            mTemplate.setHeight_square(data.getFloatExtra("h", 0.5f))
            blurredImageView.setBitmapSquare(Common.bitmap)
            blurredImageView.setRectSquare(Common.rect)
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
            mTemplate.setUri_upload_extract_audio_video(data2.toString())
            val copyFromUri = AudioUtils.copyFromUri(this, data2, mTemplate.getFolder_template())
            start_extenstion = 0
            extractAudioFromVideoRecursive(copyFromUri, 0, false, 0)
        } catch (e2: Exception) {
            e2.printStackTrace()
        }
    }

    private fun addAudioFromVideoWithExtention(str: String, str2: String, i: Int) {
        try {
            val file = File(File(mTemplate.getFolder_template()), "${System.currentTimeMillis()}_audio$str")
            FFmpegKit.executeWithArgumentsAsync(
                arrayOf("-i", str2, "-vn", "-acodec", "copy", "-y", file.absolutePath)
            ) { fFmpegSession ->
                if (ReturnCode.isSuccess(fFmpegSession.returnCode)) {
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
                val file = File(File(mTemplate.getFolder_template()), "${System.currentTimeMillis()}_audio${extentions[i]}")
                FFmpegKit.executeWithArgumentsAsync(
                    arrayOf("-i", str, "-vn", "-acodec", "copy", "-y", file.absolutePath)
                ) { fFmpegSession ->
                    if (ReturnCode.isSuccess(fFmpegSession.returnCode)) {
                        mTemplate.setExtension(extentions[i])
                        val fromFile = Uri.fromFile(file)
                        if (!z) {
                            runOnUiThread {
                                hideFragment()
                                hideProgressFragment()
                            }
                            addUriAudioToQuranFragment(fromFile, str)
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
            val file = File(File(mTemplate.getFolder_template()), "${System.currentTimeMillis()}_audio.mp3")
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
                    mTemplate.setExtension(".mp3")
                    if (!z) {
                        addUriAudioToQuranFragment(fromFile, str)
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
        clearFFmpeg()
        executor.execute(handleVideoRunnable(uri))
    }

    private fun handleVideoRunnable(uri: Uri): Runnable = Runnable {
        try {
            val copyFromUri = AudioUtils.copyFromUri(this@EngineActivity, uri, mTemplate.getFolder_template())
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(this@EngineActivity, uri)
            mediaPlayer.setOnPreparedListener { mp ->
                if (mp == null) {
                    return@setOnPreparedListener
                }
                val height = blurredImageView.height
                mTemplate.setVideoSquare(true)
                blurredImageView.setVideo(true)
                mTemplate.setName_drawable(null)
                mTemplate.setUri_original_upload_video(uri.toString())
                mTemplate.setUri_media_video(copyFromUri)
                mTemplate.setDuration_video_media(mp.duration / 1000)
                val fileVideo = FileUtils.getFileVideo(mTemplate.getFolder_template())
                val file = File(fileVideo, "frame_%04d.jpg")
                val file2 = File(fileVideo, "frame_0001.jpg")
                mTemplate.setFrame_bg(file2.absolutePath)
                endFrame = Math.min(Math.round(trackViewEntity.getMaxTime() / 1000.0f), 3)
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
        executor.execute {
            var cropTo16x9: Bitmap?
            var cropToSquareWithRoundCorners: Bitmap?
            var bitmap: Bitmap?
            var rect: Rect?
            try {
                val height = blurredImageView.height
                val bitmap2 = Glide.with(this@EngineActivity as FragmentActivity)
                    .asBitmap()
                    .load(str)
                    .override(height, height)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .submit().get()
                if (bitmap2 == null) {
                    return@execute
                }
                blurredImageView.setBitmapOriginal(bitmap2)
                if (mTemplate.getIpad_type() == IpadType.RECT.ordinal ||
                    mTemplate.getIpad_type() == IpadType.ROUND_RECT.ordinal
                ) {
                    mTemplate.setIpad_type(IpadType.IPAD.ordinal)
                }
                cropTo16x9 = when (mTemplate.geTypeResize()) {
                    ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(
                        blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
                    )
                    ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(
                        blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
                    )
                    else -> BitmapCropper.cropTo16x9(
                        blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
                    )
                }
                blurredImageView.updatePosCanvas(cropTo16x9)
                blurredImageView.updateIpad(cropTo16x9, mTemplate.getIpad_type(), mTemplate.geTypeResize())
                if (mTemplate.getIpad_type() != IpadType.BLACK_LAYER.ordinal &&
                    mTemplate.getIpad_type() != IpadType.GRADIENT.ordinal &&
                    mTemplate.getIpad_type() != IpadType.MASK_BRUSH.ordinal &&
                    mTemplate.getIpad_type() != IpadType.BLUE_TYPE.ordinal &&
                    mTemplate.getIpad_type() != IpadType.CASSET_IMG.ordinal
                ) {
                    if (mTemplate.getIpad_type() == IpadType.CASSET_IMG_BLUR.ordinal) {
                        blurredImageView.setBitmapBlured(UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1))
                        blurredImageView.setBitmapSquare(blurredImageView.getBitmapBlured())
                    } else {
                        val min = Math.min(
                            blurredImageView.getBitmapOriginal().width,
                            blurredImageView.getBitmapOriginal().height
                        )
                        var i = 0
                        if (mTemplate.getIpad_type() == IpadType.IPAD_NEOMORPHIC.ordinal) {
                            val width = (blurredImageView.getIpad_rect().width() * 0.6f).toInt()
                            val f = min.toFloat()
                            var round = Math.round(mTemplate.getX_square() * f)
                            var round2 = Math.round(mTemplate.getY_square() * f)
                            var i2 = width + round
                            if (i2 > blurredImageView.getBitmapOriginal().width) {
                                round -= i2 - blurredImageView.getBitmapOriginal().width
                                i2 = blurredImageView.getBitmapOriginal().width
                            }
                            var i3 = width + round2
                            if (i3 > blurredImageView.getBitmapOriginal().height) {
                                round2 -= i3 - blurredImageView.getBitmapOriginal().height
                                i3 = blurredImageView.getBitmapOriginal().height
                            }
                            if (round < 0) {
                                round = 0
                            }
                            if (round2 >= 0) {
                                i = round2
                            }
                            val rect2 = Rect(round, i, i2, i3)
                            blurredImageView.setRadius_square(width)
                            val widthSquare = (mTemplate.getWidth_square() * f).toInt()
                            val heightSquare = (f * mTemplate.getHeight_square()).toInt()
                            val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(
                                blurredImageView.getBitmapOriginal(), rect2, width, widthSquare, heightSquare
                            )
                            rect2.right = rect2.left + widthSquare
                            rect2.bottom = rect2.top + heightSquare
                            blurredImageView.setRectSquare(rect2)
                            bitmap = cropToSquareWithRoundCorners2
                            rect = rect2
                        } else {
                            if (mTemplate.getIpad_type() != IpadType.IPAD.ordinal &&
                                mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal &&
                                mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal
                            ) {
                                val width2 = (blurredImageView.getIpad_rect().width() * 1.0f).toInt()
                                val height2 = (cropTo16x9!!.height * 0.5355f).toInt()
                                val f2 = min.toFloat()
                                var round3 = Math.round(mTemplate.getX_square() * f2)
                                var round4 = Math.round(mTemplate.getY_square() * f2)
                                var i4 = width2 + round3
                                if (i4 > blurredImageView.getBitmapOriginal().width) {
                                    round3 -= i4 - blurredImageView.getBitmapOriginal().width
                                    i4 = blurredImageView.getBitmapOriginal().width
                                }
                                var i5 = height2 + round4
                                if (i5 > blurredImageView.getBitmapOriginal().height) {
                                    round4 -= i5 - blurredImageView.getBitmapOriginal().height
                                    i5 = blurredImageView.getBitmapOriginal().height
                                }
                                if (round3 < 0) {
                                    round3 = 0
                                }
                                if (round4 < 0) {
                                    round4 = 0
                                }
                                val rect3 = Rect(round3, round4, i4, i5)
                                val widthSquare2 = (mTemplate.getWidth_square() * f2).toInt()
                                val heightSquare2 = (f2 * mTemplate.getHeight_square()).toInt()
                                val cropToSquare = UtilsBitmap.cropToSquare(
                                    blurredImageView.getBitmapOriginal(), rect3, widthSquare2, heightSquare2
                                )
                                blurredImageView.setBitmapSquare(cropToSquare)
                                blurredImageView.setRadius_square(0)
                                rect3.right = rect3.left + widthSquare2
                                rect3.bottom = rect3.top + heightSquare2
                                blurredImageView.setRectSquare(rect3)
                                bitmap = cropToSquare
                                rect = rect3
                            } else {
                                val width3 = (blurredImageView.getIpad_rect().width() * 0.87530595f).toInt()
                                val i6 = (width3 * 1.13f).toInt()
                                val min2 = Math.min(width3, i6)
                                val f3 = min.toFloat()
                                var round5 = Math.round(mTemplate.getX_square() * f3)
                                var round6 = Math.round(mTemplate.getY_square() * f3)
                                var i7 = width3 + round5
                                if (i7 > blurredImageView.getBitmapOriginal().width) {
                                    round5 -= i7 - blurredImageView.getBitmapOriginal().width
                                    i7 = blurredImageView.getBitmapOriginal().width
                                }
                                var i8 = i6 + round6
                                if (i8 > blurredImageView.getBitmapOriginal().height) {
                                    round6 -= i8 - blurredImageView.getBitmapOriginal().height
                                    i8 = blurredImageView.getBitmapOriginal().height
                                }
                                if (round5 < 0) {
                                    round5 = 0
                                }
                                if (round6 < 0) {
                                    round6 = 0
                                }
                                val rect4 = Rect(round5, round6, i7, i8)
                                cropToSquareWithRoundCorners = if (mTemplate.getIpad_type() == IpadType.IPAD_CLASSIC.ordinal) {
                                    val widthSquare3 = (mTemplate.getWidth_square() * f3).toInt()
                                    val heightSquare3 = (f3 * mTemplate.getHeight_square()).toInt()
                                    val cropToSquare2 = UtilsBitmap.cropToSquare(
                                        blurredImageView.getBitmapOriginal(), rect4, widthSquare3, heightSquare3
                                    )
                                    blurredImageView.setBitmapSquare(cropToSquare2)
                                    blurredImageView.setRadius_square(0)
                                    rect4.right = rect4.left + widthSquare3
                                    rect4.bottom = rect4.top + heightSquare3
                                    blurredImageView.setRectSquare(rect4)
                                    cropToSquare2
                                } else {
                                    val i9 = (min2 * 0.10800001f).toInt()
                                    blurredImageView.setRadius_square(i9)
                                    val widthSquare4 = (mTemplate.getWidth_square() * f3).toInt()
                                    val heightSquare4 = (f3 * mTemplate.getHeight_square()).toInt()
                                    val result = UtilsBitmap.cropToSquareWithRoundCorners(
                                        blurredImageView.getBitmapOriginal(), rect4, i9, widthSquare4, heightSquare4
                                    )
                                    rect4.right = rect4.left + widthSquare4
                                    rect4.bottom = rect4.top + heightSquare4
                                    blurredImageView.setRectSquare(rect4)
                                    result
                                }
                                bitmap = cropToSquareWithRoundCorners
                                rect = rect4
                            }
                            blurredImageView.setBitmap(
                                UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1),
                                bitmap, -1, mTemplate.getIpad_type(), mTemplate.geTypeResize(), rect
                            )
                        }
                        mTemplate.setColor_ipad(blurredImageView.colorIpad())
                        runOnUiThread {
                            blurredImageView.invalidate()
                        }
                    }
                    if (mTemplate.getIpad_type() == IpadType.GRADIENT.ordinal) {
                        blurredImageView.setColorIpad(ViewCompat.MEASURED_STATE_MASK)
                    }
                    blurredImageView.setBitmapSquare(cropTo16x9)
                    blurredImageView.setBitmapBlured(UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1))
                    mTemplate.setColor_ipad(blurredImageView.colorIpad())
                    runOnUiThread {
                        blurredImageView.invalidate()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
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
                    val height = blurredImageView.height
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
                            blurredImageView.setDrawingSquareVideo(true)
                        }
                        blurredImageView.invalidate()
                    }
                    return@execute
                }
                if (bitmap == null) {
                    return@execute
                }
                if (mTemplate.getIpad_type() != IpadType.BLACK_LAYER.ordinal &&
                    mTemplate.getIpad_type() != IpadType.GRADIENT.ordinal &&
                    mTemplate.getIpad_type() != IpadType.MASK_BRUSH.ordinal &&
                    mTemplate.getIpad_type() != IpadType.BLUE_TYPE.ordinal &&
                    mTemplate.getIpad_type() != IpadType.CASSET_IMG.ordinal &&
                    mTemplate.getIpad_type() != IpadType.CASSET_IMG_BLUR.ordinal
                ) {
                    if (mTemplate.getIpad_type() != IpadType.IPAD.ordinal &&
                        mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal &&
                        mTemplate.getIpad_type() != IpadType.BOTTOM_RECT.ordinal &&
                        mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal &&
                        mTemplate.getIpad_type() != IpadType.IPAD_NEOMORPHIC.ordinal
                    ) {
                        val width = (blurredImageView.getIpad_rect().width() * 0.87530595f).toInt()
                        val i = (width * 1.13f).toInt()
                        val min = (Math.min(width, i) * 0.10800001f).toInt()
                        var round = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate.getX_square())
                        var round2 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate.getY_square())
                        var i2 = width + round
                        if (i2 > blurredImageView.getBitmapOriginal().width) {
                            round -= i2 - blurredImageView.getBitmapOriginal().width
                            i2 = blurredImageView.getBitmapOriginal().width
                        }
                        var i3 = i + round2
                        if (i3 > blurredImageView.getBitmapOriginal().height) {
                            round2 -= i3 - blurredImageView.getBitmapOriginal().height
                            i3 = blurredImageView.getBitmapOriginal().height
                        }
                        if (round < 0) {
                            round = 0
                        }
                        if (round2 < 0) {
                            round2 = 0
                        }
                        val rect = Rect(round, round2, i2, i3)
                        val width2 = (blurredImageView.getBitmapOriginal().width * mTemplate.getWidth_square()).toInt()
                        val height2 = (blurredImageView.getBitmapOriginal().height * mTemplate.getHeight_square()).toInt()
                        blurredImageView.setBitmapSquare(
                            UtilsBitmap.cropToSquareWithRoundCorners(bitmap, rect, min, width2, height2)
                        )
                        rect.right = rect.left + width2
                        rect.bottom = rect.top + height2
                        blurredImageView.setRectSquare(rect)
                    } else {
                        blurredImageView.setBitmapSquare(
                            UtilsBitmap.cropToSquareWithRoundCornersPlusScale(
                                bitmap, blurredImageView.getRectSquare(), blurredImageView.getRadius_square(),
                                blurredImageView.getBitmapSquare()!!.width, blurredImageView.getBitmapSquare()!!.height
                            )
                        )
                    }
                    runOnUiThread {
                        if (!isOnScroll) {
                            blurredImageView.setDrawingSquareVideo(true)
                        }
                        blurredImageView.invalidate()
                    }
                }
                cropTo16x9 = when (mTemplate.geTypeResize()) {
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
                blurredImageView.setBitmapSquare(cropTo16x9)
                runOnUiThread {
                    if (!isOnScroll) {
                        blurredImageView.setDrawingSquareVideo(true)
                    }
                    blurredImageView.invalidate()
                }
            } finally {
                runOnUiThread {
                    if (!isOnScroll) {
                        blurredImageView.setDrawingSquareVideo(true)
                    }
                    blurredImageView.invalidate()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun setupOriginalBitmap(uri: Uri): Bitmap {
        val height = blurredImageView.height
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
            var cropToSquareWithRoundCorners: Bitmap?
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
                        mTemplate.setName_drawable(null)
                        mTemplate.setUri_bg(uri_bg)
                        var i = 0
                        mTemplate.setVideoSquare(false)
                        blurredImageView.setVideo(false)
                        blurredImageView.setBitmapOriginal(setupOriginalBitmap(uri))
                        cropTo16x9 = when (mTemplate.geTypeResize()) {
                            ResizeType.SOCIAL_STORY.ordinal -> BitmapCropper.cropTo9x16(
                                blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
                            )
                            ResizeType.SQUARE.ordinal -> BitmapCropper.cropTo1x1(
                                blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
                            )
                            else -> BitmapCropper.cropTo16x9(
                                blurredImageView.getBitmapOriginal(), blurredImageView.getW(), blurredImageView.getH()
                            )
                        }
                        blurredImageView.updatePosCanvas(cropTo16x9)
                        blurredImageView.updateIpad(cropTo16x9, mTemplate.getIpad_type(), mTemplate.geTypeResize())
                        val min = Math.min(
                            blurredImageView.getBitmapOriginal().width,
                            blurredImageView.getBitmapOriginal().height
                        )
                        if (mTemplate.getIpad_type() == IpadType.IPAD_NEOMORPHIC.ordinal) {
                            val width = (blurredImageView.getIpad_rect().width() * 0.6f).toInt()
                            val f = min.toFloat()
                            var round = Math.round(mTemplate.getX_square() * f)
                            var round2 = Math.round(mTemplate.getY_square() * f)
                            var i2 = width + round
                            if (i2 > blurredImageView.getBitmapOriginal().width) {
                                round -= i2 - blurredImageView.getBitmapOriginal().width
                                i2 = blurredImageView.getBitmapOriginal().width
                            }
                            var i3 = width + round2
                            if (i3 > blurredImageView.getBitmapOriginal().height) {
                                round2 -= i3 - blurredImageView.getBitmapOriginal().height
                                i3 = blurredImageView.getBitmapOriginal().height
                            }
                            if (round < 0) {
                                round = 0
                            }
                            if (round2 >= 0) {
                                i = round2
                            }
                            val rect2 = Rect(round, i, i2, i3)
                            blurredImageView.setRadius_square(width)
                            val widthSquare = (mTemplate.getWidth_square() * f).toInt()
                            val heightSquare = (f * mTemplate.getHeight_square()).toInt()
                            val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(
                                blurredImageView.getBitmapOriginal(), rect2, width, widthSquare, heightSquare
                            )
                            blurredImageView.setBitmapSquare(cropToSquareWithRoundCorners2)
                            rect2.right = rect2.left + widthSquare
                            rect2.bottom = rect2.top + heightSquare
                            blurredImageView.setRectSquare(rect2)
                            bitmap = cropToSquareWithRoundCorners2
                            rect = rect2
                        } else {
                            if (mTemplate.getIpad_type() != IpadType.IPAD.ordinal &&
                                mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal &&
                                mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal
                            ) {
                                if (mTemplate.getIpad_type() == IpadType.BOTTOM_RECT.ordinal) {
                                    val width2 = (blurredImageView.getIpad_rect().width() * 1.0f).toInt()
                                    val height = (cropTo16x9!!.height * 0.5355f).toInt()
                                    val f2 = min.toFloat()
                                    var round3 = Math.round(mTemplate.getX_square() * f2)
                                    var round4 = Math.round(mTemplate.getY_square() * f2)
                                    var i4 = width2 + round3
                                    if (i4 > blurredImageView.getBitmapOriginal().width) {
                                        round3 -= i4 - blurredImageView.getBitmapOriginal().width
                                        i4 = blurredImageView.getBitmapOriginal().width
                                    }
                                    var i5 = height + round4
                                    if (i5 > blurredImageView.getBitmapOriginal().height) {
                                        round4 -= i5 - blurredImageView.getBitmapOriginal().height
                                        i5 = blurredImageView.getBitmapOriginal().height
                                    }
                                    if (round3 < 0) {
                                        round3 = 0
                                    }
                                    if (round4 < 0) {
                                        round4 = 0
                                    }
                                    val rect3 = Rect(round3, round4, i4, i5)
                                    val widthSquare2 = (mTemplate.getWidth_square() * f2).toInt()
                                    val heightSquare2 = (f2 * mTemplate.getHeight_square()).toInt()
                                    val cropToSquare = UtilsBitmap.cropToSquare(
                                        blurredImageView.getBitmapOriginal(), rect3, widthSquare2, heightSquare2
                                    )
                                    blurredImageView.setBitmapSquare(cropToSquare)
                                    blurredImageView.setRadius_square(0)
                                    rect3.right = rect3.left + widthSquare2
                                    rect3.bottom = rect3.top + heightSquare2
                                    blurredImageView.setRectSquare(rect3)
                                    bitmap = cropToSquare
                                    rect = rect3
                                } else {
                                    bitmap = null
                                    rect = null
                                }
                            } else {
                                val width3 = (blurredImageView.getIpad_rect().width() * 0.87530595f).toInt()
                                val i6 = (width3 * 1.13f).toInt()
                                val min2 = Math.min(width3, i6)
                                val f3 = min.toFloat()
                                var round5 = Math.round(mTemplate.getX_square() * f3)
                                var round6 = Math.round(mTemplate.getY_square() * f3)
                                var i7 = width3 + round5
                                if (i7 > blurredImageView.getBitmapOriginal().width) {
                                    round5 -= i7 - blurredImageView.getBitmapOriginal().width
                                    i7 = blurredImageView.getBitmapOriginal().width
                                }
                                var i8 = i6 + round6
                                if (i8 > blurredImageView.getBitmapOriginal().height) {
                                    round6 -= i8 - blurredImageView.getBitmapOriginal().height
                                    i8 = blurredImageView.getBitmapOriginal().height
                                }
                                if (round5 < 0) {
                                    round5 = 0
                                }
                                if (round6 < 0) {
                                    round6 = 0
                                }
                                val rect4 = Rect(round5, round6, i7, i8)
                                cropToSquareWithRoundCorners = if (mTemplate.getIpad_type() == IpadType.IPAD_CLASSIC.ordinal) {
                                    val widthSquare3 = (mTemplate.getWidth_square() * f3).toInt()
                                    val heightSquare3 = (f3 * mTemplate.getHeight_square()).toInt()
                                    val cropToSquare2 = UtilsBitmap.cropToSquare(
                                        blurredImageView.getBitmapOriginal(), rect4, widthSquare3, heightSquare3
                                    )
                                    blurredImageView.setBitmapSquare(cropToSquare2)
                                    blurredImageView.setRadius_square(0)
                                    rect4.right = rect4.left + widthSquare3
                                    rect4.bottom = rect4.top + heightSquare3
                                    blurredImageView.setRectSquare(rect4)
                                    cropToSquare2
                                } else {
                                    val i9 = (min2 * 0.10800001f).toInt()
                                    blurredImageView.setRadius_square(i9)
                                    val widthSquare4 = (mTemplate.getWidth_square() * f3).toInt()
                                    val heightSquare4 = (f3 * mTemplate.getHeight_square()).toInt()
                                    val result = UtilsBitmap.cropToSquareWithRoundCorners(
                                        blurredImageView.getBitmapOriginal(), rect4, i9, widthSquare4, heightSquare4
                                    )
                                    blurredImageView.setBitmapSquare(result)
                                    rect4.right = rect4.left + widthSquare4
                                    rect4.bottom = rect4.top + heightSquare4
                                    blurredImageView.setRectSquare(rect4)
                                    result
                                }
                                bitmap = cropToSquareWithRoundCorners
                                rect = rect4
                            }
                            when (mTemplate.getIpad_type()) {
                                IpadType.GRADIENT.ordinal -> blurredImageView.setBitmap(
                                    UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1),
                                    bitmap, ViewCompat.MEASURED_STATE_MASK,
                                    mTemplate.getIpad_type(), mTemplate.geTypeResize(), rect
                                )
                                IpadType.BLUE_TYPE.ordinal -> {
                                    if (blurredImageView.getColor_gradient() != null) {
                                        blurredImageView.setBitmap(
                                            UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1),
                                            bitmap, blurredImageView.getColor_gradient(),
                                            mTemplate.getIpad_type(), mTemplate.geTypeResize(), rect
                                        )
                                    } else {
                                        blurredImageView.setBitmap(
                                            UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1),
                                            bitmap, blurredImageView.getColor_ipad(),
                                            mTemplate.getIpad_type(), mTemplate.geTypeResize(), rect
                                        )
                                    }
                                }
                                else -> blurredImageView.setBitmap(
                                    UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1),
                                    bitmap, -1, mTemplate.getIpad_type(), mTemplate.geTypeResize(), rect
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
        updateViewTime(trackViewEntity.getMaxTime(), trackViewEntity.getCurrent_cursur_position())
        if (trackViewEntity.getCurrent_cursur_position() <= trackViewEntity.getMaxTime()) {
            updateTime(trackViewEntity.getCurrent_cursur_position())
            val trackEntityView = trackViewEntity
            trackEntityView.setCurrent_cursur_position(trackEntityView.getCurrent_cursur_position())
            blurredImageView.setProgress(trackViewEntity.getCurrent_cursur_position() / trackViewEntity.getMaxTime())
        }
    }

    private fun updateTimeToEndAya() {
        trackViewEntity.calculMaxTime()
        trackViewEntity.translateToEnd()
        updateViewTime(trackViewEntity.getMaxTime(), trackViewEntity.getCurrent_cursur_position())
        if (trackViewEntity.getCurrent_cursur_position() <= trackViewEntity.getMaxTime()) {
            updateTime(trackViewEntity.getCurrent_cursur_position())
            val trackEntityView = trackViewEntity
            trackEntityView.setCurrent_cursur_position(trackEntityView.getCurrent_cursur_position())
            blurredImageView.setProgress(trackViewEntity.getCurrent_cursur_position() / trackViewEntity.getMaxTime())
        }
    }

    private fun selectSurahName() {
        findViewById<View>(R.id.layout_menu).visibility = 4
        val surahNameEntity = blurredImageView.getSurahNameEntity()
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
            buttonCustumFont.setBackgroundResource(R.drawable.btn_dialog_delete)
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
        val file = File(mTemplate.getFolder_template(), "${System.currentTimeMillis()}_audio_echo.mp3")
        id_ffmpeg.add(
            FFmpegKit.executeWithArgumentsAsync(
                arrayOf("-i", entityAudio.getPath_ffmpeg(), "-af", str, "-y", file.absolutePath)
            ) { fFmpegSession ->
                if (ReturnCode.isSuccess(fFmpegSession.returnCode)) {
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
                            if (entityAudio.getMediaPlayer() != null &&
                                mediaPlayer.duration != entityAudio.getMediaPlayer()!!.duration
                            ) {
                                entityAudio.setRight(
                                    entityAudio.getRect().left + Math.round(
                                        trackViewEntity.getSecond_in_screen() * (mediaPlayer.duration / 1000.0f)
                                    )
                                )
                                entityAudio.setEnd(mediaPlayer.duration.toLong())
                                entityAudio.setStart(0.0f)
                                entityAudio.setMax(
                                    (entityAudio.getRect().right / entityAudio.getscaleFactor()) -
                                        ((entityAudio.getRect().left / entityAudio.getscaleFactor()) - entityAudio.getOffset_left())
                                )
                                trackViewEntity.updateWhenEffect(entityAudio)
                                runOnUiThread {
                                    trackViewEntity.invalidate()
                                    entityAudio.setMediaPlayer(mediaPlayer)
                                    iEditMediaCallback!!.startPreview()
                                    hideProgressFragment()
                                }
                            } else {
                                runOnUiThread {
                                    entityAudio.setMediaPlayer(mediaPlayer)
                                    iEditMediaCallback!!.startPreview()
                                    hideProgressFragment()
                                }
                            }
                        }
                        entityAudio.setApplyEffectInPreview(true)
                        entityAudio.setPath_ffmpeg_effect(file.absolutePath)
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
        if (EditEntityFragment.instance == null || trackViewEntity.getSelectedEntity() == null) {
            return
        }
        EditEntityFragment.instance!!.checkSplitEntity(
            trackViewEntity.getSelectedEntity()!!, -trackViewEntity.getCurrentPosition()
        )
    }

    fun checkSplitTrslEntity() {
        if (EditTrslEntityFragment.instance == null || trackViewEntity.getSelectedEntity() == null) {
            return
        }
        EditTrslEntityFragment.instance!!.checkSplitEntity(
            trackViewEntity.getSelectedEntity()!!, -trackViewEntity.getCurrentPosition()
        )
    }

    fun checkSplitAudio() {
        if (EditMediaFragment.instance == null || trackViewEntity.getSelectedEntity() !is EntityAudio) {
            return
        }
        val f = -trackViewEntity.getCurrentPosition()
        EditMediaFragment.instance!!.checkSplit(trackViewEntity.getSelectedEntity() as EntityAudio, f)
    }

    private fun clearCallback() {
        iBismilahEntityCallback = null
        iEditSName = null
        iEditMultipleCallback = null
        iEditMediaCallback = null
        iEditTrstEntityCallback = null
        iEditEntityCallback = null
        iChangeBgCallback = null
        iTrimLineCallback = null
        iIpadEditCallback = null
        iDimensionCallback = null
        searchAyaResult = null
        iFontCallback = null
        launchVideoExtract = null
        launchChoiceBgActivity = null
        launchVideo = null
        launchImg = null
        activityLauncher = null
        onBackPressedCallback = null
        iAddQuran = null
        iAudioCallback = null
        iTransitionCallback = null
        iTransitionBismilahCallback = null
        nameReaderResult = null
        iQuranIconCallback = null
        launchCropActivity = null
        editSurahNameResult = null
        iEdiTextCallback = null
        editTrslResult = null
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
        entityBismilahTimeline.getTransition()!!.setOut(entityBismilahTimeline2.getTransition().isOut()
        entityBismilahTimeline.getTransition()!!.setType_out(entityBismilahTimeline2.getTransition().getType_out())
        entityBismilahTimeline.getTransition()!!.setDuration_out(entityBismilahTimeline2.getTransition().getDuration_out())
        entityBismilahTimeline.getTransition()!!.setIn(entityBismilahTimeline2.getTransition().isIn()
        entityBismilahTimeline.getTransition()!!.setType_in(entityBismilahTimeline2.getTransition().getType_in())
        entityBismilahTimeline.getTransition()!!.setDuration_in(entityBismilahTimeline2.getTransition().getDuration_in())
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
        entityBismilahTimeline.getTransition()!!.setOut(entityQuranTimeline.getTransition().isOut()
        entityBismilahTimeline.getTransition()!!.setType_out(entityQuranTimeline.getTransition().getType_out())
        entityBismilahTimeline.getTransition()!!.setDuration_out(entityQuranTimeline.getTransition().getDuration_out())
        entityBismilahTimeline.getTransition()!!.setIn(entityQuranTimeline.getTransition().isIn()
        entityBismilahTimeline.getTransition()!!.setType_in(entityQuranTimeline.getTransition().getType_in())
        entityBismilahTimeline.getTransition()!!.setDuration_in(entityQuranTimeline.getTransition().getDuration_in())
    }

    fun start() {
        if (mTemplate.getIpad_type() == IpadType.RECT.ordinal ||
            mTemplate.getIpad_type() == IpadType.ROUND_RECT.ordinal ||
            mTemplate.getIpad_type() == IpadType.CASSET_IMG_BLUR.ordinal ||
            mTemplate.getIpad_type() == IpadType.CASSET.ordinal
        ) {
            return
        }
        isOnScroll = false
        val smoothVideoAnimator = SmoothVideoAnimator(
            trackViewEntity, mTemplate, 25,
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
        blurredImageView.setDrawingSquareVideo(false)
        val smoothVideoAnimator = animator_frame_video
        if (smoothVideoAnimator != null) {
            smoothVideoAnimator.stop()
        }
    }

    private fun updateFrame() {
        val template = mTemplate
        if (template == null || !template.isVideoSquare() ||
            mTemplate.getIpad_type() == IpadType.RECT.ordinal ||
            mTemplate.getIpad_type() == IpadType.ROUND_RECT.ordinal ||
            mTemplate.getIpad_type() == IpadType.CASSET_IMG_BLUR.ordinal ||
            mTemplate.getIpad_type() == IpadType.CASSET.ordinal ||
            mIsPlaying
        ) {
            return
        }
        var max = Math.max(1, Math.round((trackViewEntity.getCurrent_cursur_position() / 1000.0f) * 25.0f))
        val min = Math.min(
            mTemplate.getDuration_video_media() * 25,
            trackViewEntity.getDuration() * 25
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
            File(mTemplate.getFolder_template() + "/VideoFrame", str).absolutePath
        )
    }

    private fun processFrame(str: String) {
        var cropTo16x9: Bitmap?
        try {
            if (!(isOnScroll && mIsPlaying) && mIsPlaying) {
                val height = blurredImageView.height
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
                if (mTemplate.getIpad_type() != IpadType.BLACK_LAYER.ordinal &&
                    mTemplate.getIpad_type() != IpadType.GRADIENT.ordinal &&
                    mTemplate.getIpad_type() != IpadType.MASK_BRUSH.ordinal &&
                    mTemplate.getIpad_type() != IpadType.BLUE_TYPE.ordinal &&
                    mTemplate.getIpad_type() != IpadType.CASSET_IMG.ordinal
                ) {
                    if (mTemplate.getIpad_type() != IpadType.IPAD.ordinal &&
                        mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal &&
                        mTemplate.getIpad_type() != IpadType.BOTTOM_RECT.ordinal &&
                        mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal &&
                        mTemplate.getIpad_type() != IpadType.IPAD_NEOMORPHIC.ordinal
                    ) {
                        val width = (blurredImageView.getIpad_rect().width() * 0.87530595f).toInt()
                        val i = (width * 1.13f).toInt()
                        val min = (Math.min(width, i) * 0.10800001f).toInt()
                        var round = Math.round(blurredImageView.getBitmapOriginal().width * mTemplate.getX_square())
                        var round2 = Math.round(blurredImageView.getBitmapOriginal().height * mTemplate.getY_square())
                        var i2 = width + round
                        if (i2 > blurredImageView.getBitmapOriginal().width) {
                            round -= i2 - blurredImageView.getBitmapOriginal().width
                            i2 = blurredImageView.getBitmapOriginal().width
                        }
                        var i3 = i + round2
                        if (i3 > blurredImageView.getBitmapOriginal().height) {
                            round2 -= i3 - blurredImageView.getBitmapOriginal().height
                            i3 = blurredImageView.getBitmapOriginal().height
                        }
                        if (round < 0) {
                            round = 0
                        }
                        if (round2 < 0) {
                            round2 = 0
                        }
                        val rect = Rect(round, round2, i2, i3)
                        val width2 = (blurredImageView.getBitmapOriginal().width * mTemplate.getWidth_square()).toInt()
                        val height2 = (blurredImageView.getBitmapOriginal().height * mTemplate.getHeight_square()).toInt()
                        cropTo16x9 = UtilsBitmap.cropToSquareWithRoundCorners(
                            bitmap, rect, min, width2, height2
                        )
                        rect.right = rect.left + width2
                        rect.bottom = rect.top + height2
                        blurredImageView.setRectSquare(rect)
                    } else {
                        cropTo16x9 = UtilsBitmap.cropToSquareWithRoundCornersPlusScale(
                            bitmap, blurredImageView.getRectSquare(), blurredImageView.getRadius_square(),
                            blurredImageView.getBitmapSquare()!!.width, blurredImageView.getBitmapSquare()!!.height
                        )
                    }
                    runOnUiThread {
                        blurredImageView.setBitmapSquare(cropTo16x9)
                        if (!isOnScroll) {
                            blurredImageView.setDrawingSquareVideo(true)
                        }
                        blurredImageView.invalidate()
                    }
                }
                cropTo16x9 = when (mTemplate.geTypeResize()) {
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
                    blurredImageView.setBitmapSquare(cropTo16x9)
                    if (!isOnScroll) {
                        blurredImageView.setDrawingSquareVideo(true)
                    }
                    blurredImageView.invalidate()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
