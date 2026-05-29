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
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.utils.AspectRatioCalculator
import hazem.nurmontage.videoquran.utils.AudioUtils
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
import hazem.nurmontage.videoquran.databinding.ActivityTimeLineBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.Iterator
import java.util.Locale
import java.util.Map
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.Pair

class EngineActivity : BaseActivity() {
    companion object {
        const val EXTRACT_AUDIO_VIDEO_PERMISSION_REQUEST_CODE = 12
        const val FPS = 25
        const val IMAGE_PERMISSION_REQUEST_CODE = 10
        const val REQUEST_CODE_AUDIO = 2
        const val REQUEST_WRITE_EXTERNAL_STORAGE = 1
        const val VIDEO_PERMISSION_REQUEST_CODE = 11
    }

    // ViewBinding
    private lateinit var binding: ActivityTimeLineBinding

    private lateinit var activityLauncher: ActivityResultLauncher<Intent>
    private var animator_frame_video: SmoothVideoAnimator? = null
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
    private lateinit var timeFormatter: TimeFormatter
    private lateinit var trackViewEntity: TrackEntityView
    private lateinit var tv_currentTime: TextView
    private lateinit var tv_endTime: TextView
    private lateinit var tv_resolution: TextCustumFont
    private lateinit var tv_tittle_fragment: TextCustumFont
    private var uri_bg: String? = null
    private var valueAnimator: SmoothTimelineAnimator? = null
    private var vibrationHelper: MyVibrationHelper? = null
    private var isSaveTmpTemplate: Boolean = true
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val id_ffmpeg: MutableList<Long> = mutableListOf()
    private var current_position_time: Int = 0
    private var startCursur: Int = 0
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (this@EngineActivity.mCurrentFragment != null) {
                this@EngineActivity.hideFragment()
            } else {
                this@EngineActivity.dialog()
            }
        }
    }
    private val iTrimLineCallback = object : TrackEntityView.ITrimLineCallback {
        override fun fadeInAudio(f: Float) {
        }

        override fun fadeOutAudio(f: Float) {
        }

        override fun onMove() {
        }

        override fun onUpdatePlayerAudio(entityAudio: EntityAudio) {
        }

        override fun onSelectMultiple(i: Int) {
            this@EngineActivity.showEditMultipleEntity(i)
        }

        override fun onDelete(entityView: EntityView) {
            try {
                this@EngineActivity.blurredImageView.setEntity_select(null)
            this@EngineActivity.blurredImageView.postInvalidate()
                this@EngineActivity.hideFragment()
            } catch (Exception e) {
                e.printStackTrace()
            }
        }

        override fun onEmptySelect() {
            this@EngineActivity.blurredImageView.setEntity_select(null)
            this@EngineActivity.blurredImageView.postInvalidate()
            this@EngineActivity.pausePlayer()
            this@EngineActivity.hideFragment()
        }

        override fun onUpdate() {
            if (this@EngineActivity.blurredImageView != null) {
                this@EngineActivity.blurredImageView.postInvalidate()
            }
        }

        override fun onUp() {
            this@EngineActivity.isOnScroll = false
            this@EngineActivity.updateBtnCutState()
        }

        override fun onAddStack(entityAction: EntityAction) {
            this@EngineActivity.enableUndoBtn()
        }

        override fun onSeekPlayer(f: Float) {
            try {
                this@EngineActivity.isOnScroll = true
                for (entityAudio in this@EngineActivity.trackViewEntity.getEntityListAudio()) {
                    try {
                        if (entityAudio.getMediaPlayer() != null && entityAudio.getMediaPlayer().isPlaying()) {
                            entityAudio.getMediaPlayer().pause()
                        }
                    } catch (Exception e) {
                        e.printStackTrace()
                    }
                }
                if (this@EngineActivity.mIsPlaying) {
                    if (this@EngineActivity.btnPlayPause != null) {
                        this@EngineActivity.btnPlayPause.setImageResource(R.drawable.play_btn)
                    }
                    this@EngineActivity.mIsPlaying = false
                    this@EngineActivity.trackViewEntity.setPlaying(false)
            this@EngineActivity.blurredImageView.setPlaying(false)
                }
                this@EngineActivity.pauseTimelineAnimation()
                this@EngineActivity.stop()
                var round: Int = Math.round(Math.abs((f / this@EngineActivity.trackViewEntity.getSecond_in_screen()) * (-1000.0f)))
                if (this@EngineActivity.blurredImageView != null && (round <= this@EngineActivity.trackViewEntity.getMaxTime() || this@EngineActivity.blurredImageView.getProgress() < 1.0f)) {
                    var min: Float = min(1.0f, round / this@EngineActivity.trackViewEntity.getMaxTime())
                    this@EngineActivity.updateTime(round)
            this@EngineActivity.blurredImageView.setProgress(min)
                }
                this@EngineActivity.trackViewEntity.update_current_cursur_position(round)
            this@EngineActivity.current_position_time = System.currentTimeMillis().toInt()
                val engineActivity = this@EngineActivity
                engineActivity.startCursur = engineActivity.trackViewEntity.getCurrent_cursur_position()
                val engineActivity2 = this@EngineActivity
                engineActivity2.updateViewTime(engineActivity2.trackViewEntity.getMaxTime(), this@EngineActivity.trackViewEntity.getCurrent_cursur_position())
                this@EngineActivity.updateBtnCutState()
                this@EngineActivity.updateBtnToStart()
                this@EngineActivity.updateBtnToEnd()
                this@EngineActivity.updateFrame()
            } catch (Exception unused) {
            }
        }

        override fun pause() {
            this@EngineActivity.pausePlayer()
        }

        override fun onPlayVibration() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.runOnUiThread(object : Runnable {
                override fun run() {
                    if (this@EngineActivity.vibrationHelper != null) {
                        this@EngineActivity.vibrationHelper.vibrate()
                    }
                }
            })
        }

        override fun onSelectEntity(entity: Entity, f: Float) {
            this@EngineActivity.pausePlayer()
            if (entity is EntityQuranTimeline) {
                this@EngineActivity.blurredImageView.setEntity_select(entity.getEntityView())
                this@EngineActivity.blurredImageView.invalidate()
                if (EditEntityFragment.instance != null) {
                    EditEntityFragment.instance.checkSplitEntity(entity, -this@EngineActivity.trackViewEntity.getCurrentPosition())
                    EditEntityFragment.instance.checkIcon(entity)
                    return
                } else if (EditTextFragment.instance != null) {
                    EditTextFragment.instance.update(((EntityQuranTimeline) entity).getQuranEntity())
                    return
                } else {
                    this@EngineActivity.showEditEntity(entity)
                    return
                }
            }
            if (entity is EntityTrslTimeline) {
                this@EngineActivity.blurredImageView.setEntity_select(entity.getEntityView())
                this@EngineActivity.blurredImageView.invalidate()
                if (EditTrslEntityFragment.instance != null) {
                    EditTrslEntityFragment.instance.checkSplitEntity(entity, -this@EngineActivity.trackViewEntity.getCurrentPosition())
                    return
                } else {
                    this@EngineActivity.showEditTrslEntity(entity)
                    return
                }
            }
            if (entity is EntityBismilahTimeline) {
                this@EngineActivity.blurredImageView.setEntity_select(entity.getEntityView())
                this@EngineActivity.blurredImageView.invalidate()
                this@EngineActivity.showEditBismilahEntity(entity)
            } else if (entity is EntityAudio) {
                var entityAudio: EntityAudio? = entity as EntityAudio
                if (EditMediaFragment.instance != null) {
                    EditMediaFragment.instance.checkSplit(entityAudio, -this@EngineActivity.trackViewEntity.getCurrentPosition())
                } else {
                    this@EngineActivity.showEditAudioEntity(entityAudio)
                }
            }
        }

        override fun enableRedo(z: Boolean) {
            if (z) {
                this@EngineActivity.enableRedoBtn()
            } else {
                this@EngineActivity.disableRedoBtn()
            }
        }

        override fun enableUndo(z: Boolean) {
            if (z) {
                this@EngineActivity.enableUndoBtn()
            } else {
                this@EngineActivity.disableUndoBtn()
            }
        }

        override fun progress(z: Boolean) {
            this@EngineActivity.runOnUiThread(object : Runnable {
                override fun run() {
                    if (z) {
                        this@EngineActivity.showProgress()
                    } else {
                        this@EngineActivity.hideProgressFragment()
                    }
                }
            })
        }

        override fun onUpdateTime() {
            val engineActivity = this@EngineActivity
            engineActivity.startCursur = engineActivity.trackViewEntity.getCurrent_cursur_position()
            this@EngineActivity.updateTime()
        }
    }
    private val iAddQuran = object : AddQuranFragment.IAddQuran {
        override fun onVuCopyRight() {
            this@EngineActivity.dialogCopyRight()
        }

        override fun progress() {
            this@EngineActivity.runOnUiThread(object : Runnable {
                override fun run() {
                    this@EngineActivity.showProgress()
                }
            })
        }

        override fun onSearch() {
            this@EngineActivity.isToCrop = true
            this@EngineActivity.searchAyaResult.launch(Intent(this@EngineActivity, QuranSearchActivity::class.java))
        }

        override fun uploadRecitation() {
            try {
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                val engineActivity = this@EngineActivity
                engineActivity.mCurrentFragment = AddAudioFragment.getInstance(engineActivity.iAudioCallback, this@EngineActivity.mResources)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
                val engineActivity2 = this@EngineActivity
                engineActivity2.setupShowFragment(engineActivity2.mResources.getString(R.string.audio))
            } catch (Exception unused) {
            }
        }

        override fun onAddTranslation(str: String, i: Int, z: Boolean) {
            this@EngineActivity.addTranslationEntity(str, i, z)
        }

        override fun onAdd(str: String, str2: String, str3: String, str4: String, i: Int, i2: Int, str5: String, i3: Int, i4: Int) {
            this@EngineActivity.addEntity(str, str2 + " " + i2, str3, str4, i, i2, str5, i3, i4)
        }

        override fun onDone(str: String, i: Int, str2: String, uri: Uri, str3: String) {
            this@EngineActivity.runOnUiThread(object : Runnable {
                override fun run() {
                    this@EngineActivity.hideFragment()
                }
            })
            this@EngineActivity.blurredImageView.updateSizeAya()
            this@EngineActivity.blurredImageView.updateSizeAyaTrsl()
            this@EngineActivity.blurredImageView.setSurahNameEntity(str, str2, null, 1.0f, "خط الإبل.otf", this@EngineActivity.blurredImageView.getClr_aya(), AyaTextPreset.NONE.ordinal, this@EngineActivity.blurredImageView.getSurahNameEntity() != null ? this@EngineActivity.blurredImageView.getSurahNameEntity().getStyle() : SurahNameStyle.NONE.ordinal, i, this@EngineActivity.blurredImageView.getSurahNameEntity() != null && this@EngineActivity.blurredImageView.getSurahNameEntity().isHaveBg(), this@EngineActivity.blurredImageView.getSurahNameEntity() != null ? this@EngineActivity.blurredImageView.getSurahNameEntity().getClrBg() : ViewCompat.MEASURED_STATE_MASK)
            if (str3 == null) {
                this@EngineActivity.addAudio(uri)
            } else {
                this@EngineActivity.addAudioFromVideo(uri, str3)
            }
        }

        override fun onBismilah() {
            var addEntityIste3adha: Boolean = this@EngineActivity.addEntityIste3adha()
            var addEntityBissmilah: Boolean = this@EngineActivity.addEntityBissmilah()
            if (!addEntityIste3adha || !addEntityBissmilah) {
                this@EngineActivity.trackViewEntity.translateToRight(addEntityIste3adha)
            } else {
                this@EngineActivity.trackViewEntity.translateToRight()
            }
        }

        override fun onDone(str: String, i: Int, str2: String, list: List<RecitersModel>) {
            this@EngineActivity.runOnUiThread(object : Runnable {
                override fun run() {
                    this@EngineActivity.hideFragment()
                }
            })
            this@EngineActivity.blurredImageView.updateSizeAya()
            this@EngineActivity.blurredImageView.updateSizeAyaTrsl()
            this@EngineActivity.blurredImageView.setSurahNameEntity(str, str2, null, 1.0f, "خط الإبل.otf", this@EngineActivity.blurredImageView.getClr_aya(), AyaTextPreset.NONE.ordinal, this@EngineActivity.blurredImageView.getSurahNameEntity() != null ? this@EngineActivity.blurredImageView.getSurahNameEntity().getStyle() : SurahNameStyle.NONE.ordinal, i, this@EngineActivity.blurredImageView.getSurahNameEntity() != null && this@EngineActivity.blurredImageView.getSurahNameEntity().isHaveBg(), this@EngineActivity.blurredImageView.getSurahNameEntity() != null ? this@EngineActivity.blurredImageView.getSurahNameEntity().getClrBg() : ViewCompat.MEASURED_STATE_MASK)
            if (NetworkUtils.isNetworkAvailable(this@EngineActivity) && list != null && !list.isEmpty()) {
                this@EngineActivity.addAudioReciters(list)
            } else {
                this@EngineActivity.runOnUiThread(object : Runnable {
                    override fun run() {
                        this@EngineActivity.updateTimeToEndAya()
                        this@EngineActivity.updateBtnToEnd()
                        this@EngineActivity.updateBtnToStart()
                        this@EngineActivity.hideProgressFragment()
                    }
                })
            }
        }

        override fun onCancel() {
            this@EngineActivity.hideFragment()
        }

        override fun onErrorLimitation() {
            this@EngineActivity.runOnUiThread(object : Runnable {
                override fun run() {
                    Toast.makeText(this@EngineActivity, this@EngineActivity.mResources.getString(R.string.error_limit), 0).show()
                }
            })
        }

        override fun onAddReaderName(str: String, str2: String, uri: Uri) {
            this@EngineActivity.isToCrop = true
            var intent = Intent(this@EngineActivity, AddReaderNameActivity::class.java)
            intent.putExtra("name", str)
            if (uri != null) {
                intent.putExtra(MimeTypes.BASE_TYPE_AUDIO, uri.toString())
            }
            intent.putExtra("path_video_copy", str2)
            this@EngineActivity.nameReaderResult.launch(intent)
        }
    }
    private val searchAyaResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult> {
        override fun onActivityResult(activityResult: ActivityResult) {
            this@EngineActivity.isToCrop = false
            try {
                if (AddQuranFragment.instance != null) {
                    AddQuranFragment.instance.addAyaIndex()
                } else {
                    val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                    val engineActivity = this@EngineActivity
                    engineActivity.mCurrentFragment = AddQuranFragment.getInstance(engineActivity.iAddQuran, this@EngineActivity.mResources)
                    beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                    beginTransaction.commit()
                    this@EngineActivity.runOnUiThread(object : Runnable {
                        override fun run() {
                            this@EngineActivity.setupShowFragment(this@EngineActivity.mResources.getString(R.string.quran))
                        }
                    })
                }
            } catch (Exception unused) {
            }
        }
    })
    private val nameReaderResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult> {
        override fun onActivityResult(activityResult: ActivityResult) {
            this@EngineActivity.isToCrop = false
            var data = activityResult.getData()
            if (data != null) {
                if (AddQuranFragment.instance != null) {
                    var parse: Uri? = Uri.parse(data.getStringExtra(MimeTypes.BASE_TYPE_AUDIO))
                    var stringExtra: String? = data.getStringExtra("path_video_copy")
                    AddQuranFragment.instance.setNameReader(data.getStringExtra("name"), parse, stringExtra)
                    return
                }
                try {
                    var parse2: Uri? = Uri.parse(data.getStringExtra(MimeTypes.BASE_TYPE_AUDIO))
                    var stringExtra2: String? = data.getStringExtra("path_video_copy")
                    var stringExtra3: String? = data.getStringExtra("name")
                    val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                    val engineActivity = this@EngineActivity
                    engineActivity.mCurrentFragment = AddQuranFragment.getInstance(engineActivity.iAddQuran, this@EngineActivity.mResources, parse2, stringExtra2, stringExtra3)
                    beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                    beginTransaction.commit()
                    this@EngineActivity.runOnUiThread(object : Runnable {
                        override fun run() {
                            this@EngineActivity.setupShowFragment(this@EngineActivity.mResources.getString(R.string.quran))
                        }
                    })
                } catch (Exception unused) {
                }
            }
        }
    })
    private val editSurahNameResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult> {
        override fun onActivityResult(activityResult: ActivityResult) {
                        this@EngineActivity.isToCrop = false
            if (activityResult.getResultCode() != -1 || (data = activityResult.getData()) == null) {
                return
            }
            var stringExtra: String? = data.getStringExtra(Common.READER)
            var booleanExtra: Boolean = data.getBooleanExtra("isBg", false)
            var intExtra: Int = data.getIntExtra("style", 0)
            if (this@EngineActivity.blurredImageView.getSurahNameEntity().getIndex_surah() == 0) {
                this@EngineActivity.blurredImageView.getSurahNameEntity().setIndex_surah(data.getIntExtra(StreamInformation.KEY_INDEX, 1))
            }
            this@EngineActivity.blurredImageView.getSurahNameEntity().setClrBg(data.getIntExtra("clrBg", ViewCompat.MEASURED_STATE_MASK))
            if (intExtra == SurahNameStyle.NONE.ordinal) {
                this@EngineActivity.blurredImageView.getSurahNameEntity().setAlignment(this@EngineActivity.blurredImageView.updateAlignmentSurah(stringExtra))
            }
            this@EngineActivity.blurredImageView.getSurahNameEntity().setStyle(this@EngineActivity, intExtra, stringExtra, booleanExtra)
            this@EngineActivity.blurredImageView.invalidate()
        }
    })
    private val editTrslResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult> {
        override fun onActivityResult(activityResult: ActivityResult) {
                        this@EngineActivity.isToCrop = false
            if (activityResult.getResultCode() != -1 || (data = activityResult.getData()) == null) {
                return
            }
            var stringExtra: String? = data.getStringExtra(Common.READER)
            var booleanExtra: Boolean = data.getBooleanExtra("isBg", true)
            var translationQuranEntity: TranslationQuranEntity? = .blurredImageView.getEntity_select()
            translationQuranEntity.setClrBg(data.getIntExtra("clrBg", ViewCompat.MEASURED_STATE_MASK))
            translationQuranEntity.setTxt(stringExtra)
            translationQuranEntity.setHaveBg(booleanExtra)
            this@EngineActivity.blurredImageView.invalidate()
        }
    })
    private ChangeBgFragment.IChangeBgCallback iChangeBgCallback = object : ChangeBgFragment.IChangeBgCallback {}
    private DimensionAdabters.IDimensionCallback iDimensionCallback = object : DimensionAdabters.IDimensionCallback {}
    private val iAudioCallback = object : AddAudioFragment.IAudioCallback {
        override fun upload() {
            if (this@EngineActivity.checkPermissionAudio()) {
                this@EngineActivity.pickAudio()
            }
        }

        override fun extract() {
            this@EngineActivity.pickVideoForAudio()
        }

        override fun cancel() {
            this@EngineActivity.hideFragment()
            try {
                val engineActivity = this@EngineActivity
                engineActivity.setupShowFragment(engineActivity.mResources.getString(R.string.quran))
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                val engineActivity2 = this@EngineActivity
                engineActivity2.mCurrentFragment = AddQuranFragment.getInstance(engineActivity2.iAddQuran, this@EngineActivity.mResources)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
            } catch (Exception unused) {
            }
        }
    }
    private val iIpadEditCallback = object : EditIpadFragment.IIpadEditCallback {
        override fun onClick(i: Int, i2: Int) {
            this@EngineActivity.mTemplate.setColor_ipad(i)
            this@EngineActivity.mTemplate.setIndex_color(i2)
            this@EngineActivity.mTemplate.setGradient(null)
            this@EngineActivity.blurredImageView.setColorIpad(i)
            this@EngineActivity.blurredImageView.invalidate()
        }

        override fun onClick(gradient: Gradient, i: Int) {
            this@EngineActivity.mTemplate.setGradient(null)
            this@EngineActivity.mTemplate.setIndex_color(i2)
            this@EngineActivity.blurredImageView.setColorIpad(i)
            this@EngineActivity.blurredImageView.invalidate()
        }

        override fun onDialogPremium() {
            /* billing removed */
        }

        override fun onGlassType(z: Boolean) {
            this@EngineActivity.mTemplate.setGlass(z)
            this@EngineActivity.blurredImageView.setGlass(z)
            this@EngineActivity.blurredImageView.invalidate()
        }

        override fun onChangeType(i: Int) {
            if (this@EngineActivity.blurredImageView.getmIpadType() == i) {
                return
            }
            if (EditIpadFragment.instance != null) {
                EditIpadFragment.instance.scrollToSelectedPosition()
            }
            try {
                this@EngineActivity.mTemplate.setIpad_type(i)
            this@EngineActivity.blurredImageView.changeTypeIpad(i)
                if (this@EngineActivity.mTemplate.isVideoSquare()) {
                    if (i != IpadType.GRADIENT.ordinal && i != IpadType.BLACK_LAYER.ordinal && i != IpadType.MASK_BRUSH.ordinal && i != IpadType.BLUE_TYPE.ordinal && i != IpadType.CASSET_IMG.ordinal) {
                        if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.CASSET_IMG_BLUR.ordinal) {
                            this@EngineActivity.blurredImageView.setBitmapSquare(this@EngineActivity.blurredImageView.getBitmapBlured())
                            this@EngineActivity.blurredImageView.setRadius_square(0)
                        }
                    }
                    this@EngineActivity.blurredImageView.setBitmapSquare(this@EngineActivity.blurredImageView.getBitmapNotBlur())
                    this@EngineActivity.blurredImageView.setRadius_square(0)
                }
                if (i == IpadType.IPAD.ordinal || i == IpadType.IPAD_UNBLUR.ordinal) {
                    var width: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.87530595f)
                    var i2: Int = (width * 1.13f)
                    var min: Int = (min(width, i2) * 0.10800001f)
                    var round: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                    var round2: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                    var i3: Int = width + round
                    if (i3 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                        round -= i3 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                        i3 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                    }
                    var i4: Int = i2 + round2
                    if (i4 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                        round2 -= i4 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                        i4 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                    }
                    if (round < 0) {
                        round = 0
                    }
                    if (round2 < 0) {
                        round2 = 0
                    }
                    var rect = Rect(round, round2, i3, i4)
                    var width2: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                    var height: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                    this@EngineActivity.blurredImageView.setBitmapSquare(UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect, min, width2, height))
                    this@EngineActivity.blurredImageView.setRadius_square(min)
                    rect.right = rect.left + width2
                    rect.bottom = rect.top + height
                    this@EngineActivity.blurredImageView.setRectSquare(rect)
                }
                if (i == IpadType.IPAD_CLASSIC.ordinal) {
                    var width3: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.87530595f)
                    var i5: Int = (width3 * 1.13f)
                    var round3: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                    var round4: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                    var i6: Int = width3 + round3
                    if (i6 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                        round3 -= i6 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                        i6 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                    }
                    var i7: Int = i5 + round4
                    if (i7 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                        round4 -= i7 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                        i7 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                    }
                    if (round3 < 0) {
                        round3 = 0
                    }
                    if (round4 < 0) {
                        round4 = 0
                    }
                    var rect2 = Rect(round3, round4, i6, i7)
                    var width4: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                    var height2: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                    this@EngineActivity.blurredImageView.setBitmapSquare(UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect2, width4, height2))
                    this@EngineActivity.blurredImageView.setRadius_square(0)
                    rect2.right = rect2.left + width4
                    rect2.bottom = rect2.top + height2
                    this@EngineActivity.blurredImageView.setRectSquare(rect2)
                }
                if (i == IpadType.IPAD_NEOMORPHIC.ordinal) {
                    var width5: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.6f)
                    var round5: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                    var round6: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                    var i8: Int = width5 + round5
                    if (i8 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                        round5 -= i8 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                        i8 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                    }
                    var i9: Int = width5 + round6
                    if (i9 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                        round6 -= i9 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                        i9 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                    }
                    if (round5 < 0) {
                        round5 = 0
                    }
                    if (round6 < 0) {
                        round6 = 0
                    }
                    var rect3 = Rect(round5, round6, i8, i9)
                    var width6: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                    var height3: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                    this@EngineActivity.blurredImageView.setBitmapSquare(UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect3, width5, width6, height3))
                    this@EngineActivity.blurredImageView.setRadius_square(width5)
                    rect3.right = rect3.left + width6
                    rect3.bottom = rect3.top + height3
                    this@EngineActivity.blurredImageView.setRectSquare(rect3)
                }
                if (i == IpadType.BOTTOM_RECT.ordinal) {
                    var width7: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 1.0f)
                    var height4: Int = (this@EngineActivity.blurredImageView.getBitmapBlured().getHeight() * 0.5355f)
                    var round7: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                    var round8: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                    var i10: Int = width7 + round7
                    if (i10 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                        round7 -= i10 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                        i10 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                    }
                    var i11: Int = height4 + round8
                    if (i11 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                        round8 -= i11 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                        i11 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                    }
                    if (round7 < 0) {
                        round7 = 0
                    }
                    if (round8 < 0) {
                        round8 = 0
                    }
                    var rect4 = Rect(round7, round8, i10, i11)
                    var width8: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                    var height5: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                    this@EngineActivity.blurredImageView.setBitmapSquare(UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect4, width8, height5))
                    this@EngineActivity.blurredImageView.setRadius_square(0)
                    rect4.right = rect4.left + width8
                    rect4.bottom = rect4.top + height5
                    this@EngineActivity.blurredImageView.setRectSquare(rect4)
                }
                if (i == IpadType.BORDER.ordinal) {
                    if (ColorUtils.isColorDark(this@EngineActivity.blurredImageView.getBitmapOriginal().getPixel((this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * 0.5f), (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * 0.5f)))) {
                        this@EngineActivity.mTemplate.setColor_ipad(-1)
                    } else {
                        this@EngineActivity.mTemplate.setColor_ipad(ViewCompat.MEASURED_STATE_MASK)
                    }
                    this@EngineActivity.blurredImageView.setColorIpad(this@EngineActivity.mTemplate.getColor_ipad())
                }
                this@EngineActivity.blurredImageView.createRectWithoutSurahName()
                this@EngineActivity.blurredImageView.resizeEntity()
                if (this@EngineActivity.blurredImageView.getSurahNameEntity() != null && this@EngineActivity.blurredImageView.getSurahNameEntity().getStyle() != SurahNameStyle.ZAGHRAFAT.ordinal && !this@EngineActivity.blurredImageView.getSurahNameEntity().isHaveBg()) {
                    this@EngineActivity.blurredImageView.updatePosSurahName()
                }
                this@EngineActivity.blurredImageView.changeColorIpad()
                this@EngineActivity.blurredImageView.invalidate()
            } catch (Exception e) {
                Log.e("execption", "onChangeType" + e.getMessage())
            }
        }

        override fun onDone() {
            this@EngineActivity.hideFragment()
        }

        override fun onCancel() {
            this@EngineActivity.hideFragment()
        }
    }
    val launchChoiceBgActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback() { // from class: Runnable
        override fun onActivityResult(result: ActivityResult) {
            this@EngineActivity.(ActivityResult) obj)
        }
    })
    val launchCropActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback() { // from class: Runnable
        override fun onActivityResult(result: ActivityResult) {
            this@EngineActivity.(ActivityResult) obj)
        }
    })
    private val launchImg = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback() { // from class: Runnable
        override fun onActivityResult(result: ActivityResult) {
            this@EngineActivity.(ActivityResult) obj)
        }
    })
    private val launchVideo = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback() { // from class: Runnable
        override fun onActivityResult(result: ActivityResult) {
            this@EngineActivity.(ActivityResult) obj)
        }
    })
    private val launchVideoExtract = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback() { // from class: Runnable
        override fun onActivityResult(result: ActivityResult) {
            this@EngineActivity.(ActivityResult) obj)
        }
    })
    private val extentions: Array<String> = {".mp3", ".ogg", ".acc", ".m4a", ".wav", ".mpeg"}
    private var start_extenstion: Int = 0
    private val iQuranIconCallback = object : EditIconQuranFragment.IQuranIconCallback {
        override fun add(str: String) {
            try {
                var quranEntity: QuranEntity? = this as QuranEntity@EngineActivity.trackViewEntity.getSelectedEntity().getEntityView()
                quranEntity.setVectorDrawable((VectorDrawable) ContextCompat.getDrawable(this@EngineActivity.getApplicationContext(), DrawableHelper.getIDDrawableIconByName(str)))
                quranEntity.setIcon(str)
                quranEntity.updateIconDraw()
                quranEntity.initPreset(quranEntity.getmPreset())
                this@EngineActivity.blurredImageView.invalidate()
            } catch (Exception unused) {
                Log.e("icon  e ", "" + str)
            }
        }

        override fun onDone(str: String) {
            try {
                this@EngineActivity.blurredImageView.setIcon(str, (VectorDrawable) ContextCompat.getDrawable(this@EngineActivity.getApplicationContext(), DrawableHelper.getIDDrawableIconByName(str)))
                this@EngineActivity.hideFragment()
                this@EngineActivity.iTrimLineCallback.onSelectEntity(this@EngineActivity.trackViewEntity.getSelectedEntity(), -1.0f)
            } catch (Exception unused) {
            }
        }

        override fun onCancel(str: String) {
            try {
                var quranEntity: QuranEntity? = this as QuranEntity@EngineActivity.trackViewEntity.getSelectedEntity().getEntityView()
                quranEntity.setVectorDrawable((VectorDrawable) ContextCompat.getDrawable(this@EngineActivity.getApplicationContext(), DrawableHelper.getIDDrawableIconByName(str)))
                quranEntity.setIcon(str)
                quranEntity.updateIconDraw()
                quranEntity.initPreset(quranEntity.getmPreset())
                this@EngineActivity.blurredImageView.invalidate()
                this@EngineActivity.hideFragment()
                this@EngineActivity.iTrimLineCallback.onSelectEntity(this@EngineActivity.trackViewEntity.getSelectedEntity(), -1.0f)
            } catch (Exception unused) {
            }
        }
    }
    private val iEditSName = object : EditS_NameFragment.IEditS_Name {
        override fun onFont(surahNameEntity: SurahNameEntity) {
            val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
            val engineActivity = this@EngineActivity
            engineActivity.mCurrentFragment = FontFragment.getInstance(engineActivity.iFontCallback, surahNameEntity.getNameFont(), surahNameEntity.getPaintAya().getTypeface())
            beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
            beginTransaction.commit()
            val engineActivity2 = this@EngineActivity
            engineActivity2.setupShowFragment(engineActivity2.mResources.getString(R.string.font))
        }

        override fun onEdit(surahNameEntity: SurahNameEntity) {
            try {
                this@EngineActivity.isToCrop = true
                var intent = Intent(this@EngineActivity, EditS_NameActivity::class.java)
                intent.putExtra("surah_name", this@EngineActivity.blurredImageView.getSurahNameEntity().getName())
                intent.putExtra("reader_name", this@EngineActivity.blurredImageView.getSurahNameEntity().getReader())
                intent.putExtra("style", this@EngineActivity.blurredImageView.getSurahNameEntity().getStyle())
                intent.putExtra(StreamInformation.KEY_INDEX, this@EngineActivity.blurredImageView.getSurahNameEntity().getIndex_surah())
                intent.putExtra("isBg", this@EngineActivity.blurredImageView.getSurahNameEntity().isHaveBg())
                intent.putExtra("clrBg", this@EngineActivity.blurredImageView.getSurahNameEntity().getClrBg())
                this@EngineActivity.editSurahNameResult.launch
            this@EngineActivity.overridePendingTransition(0, 0)
            } catch (Exception unused) {
            }
        }

        override fun update() {
            this@EngineActivity.blurredImageView.postInvalidate()
        }

        override fun onDone() {
            this@EngineActivity.selectSurahName()
        }

        override fun onColor(surahNameEntity: SurahNameEntity) {
            try {
                this@EngineActivity.pausePlayer()
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                val engineActivity = this@EngineActivity
                engineActivity.mCurrentFragment = ColorS_NameFragment.getInstance(engineActivity.iEditSName, surahNameEntity, this@EngineActivity.mResources)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(null)
            } catch (Exception unused) {
            }
        }
    }
    private val iFontCallback = object : FontFragment.IFontCallback {
        override fun onAdd(str: String, typeface: Typeface) {
            try {
                if (this@EngineActivity.blurredImageView.getEntity_select() is SurahNameEntity) {
                    this@EngineActivity.blurredImageView.getSurahNameEntity().setTypeface(typeface, str)
                    this@EngineActivity.blurredImageView.invalidate()
                } else if (str != null && typeface != null) {
                    this@EngineActivity.blurredImageView.setTypeface(typeface, str)
                }
                FontFragment.instance.add(typeface, str)
            } catch (Exception unused) {
            }
        }

        override fun onDone(str: String, typeface: Typeface) {
            try {
                this@EngineActivity.hideFragment()
                if (this@EngineActivity.blurredImageView.getEntity_select() is SurahNameEntity) {
                    this@EngineActivity.selectSurahName()
                } else {
                    this@EngineActivity.iTrimLineCallback.onSelectEntity(this@EngineActivity.trackViewEntity.getSelectedEntity(), -1.0f)
                }
            } catch (Exception unused) {
            }
        }

        override fun onCancel(str: String, typeface: Typeface) {
            try {
                if (this@EngineActivity.blurredImageView.getEntity_select() is SurahNameEntity) {
                    this@EngineActivity.blurredImageView.getSurahNameEntity().setTypeface(typeface, str)
                    this@EngineActivity.blurredImageView.invalidate()
                    this@EngineActivity.selectSurahName()
                } else {
                    if (str != null && typeface != null) {
                        this@EngineActivity.blurredImageView.setTypeface(typeface, str)
                    }
                    this@EngineActivity.hideFragment()
                    this@EngineActivity.iTrimLineCallback.onSelectEntity(this@EngineActivity.trackViewEntity.getSelectedEntity(), -1.0f)
                }
            } catch (Exception unused) {
            }
        }
    }
    private val iBismilahEntityCallback = object : EditBismilahEntityFragment.IBismilahEntityCallback {
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {
            this@EngineActivity.blurredImageView.setPreset(ayaTextPreset)
        }

        override fun updateAya(i: Int) {
            this@EngineActivity.blurredImageView.setColorAya(i)
        }

        override fun onAnim() {
            try {
                this@EngineActivity.pausePlayer()
                val bismilahEntity = this@EngineActivity.trackViewEntity.getSelectedEntity().getEntityView()
                this@EngineActivity.trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                this@EngineActivity.mCurrentFragment = EffectBismilahFragment.get(bismilahEntity.getBismilahTimeline().getTransition(), this@EngineActivity.mResources, this@EngineActivity.iTransitionBismilahCallback, this@EngineActivity.trackViewEntity.getSelectedEntity())
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
                val engineActivity = this@EngineActivity
                engineActivity.setupShowFragment(engineActivity.mResources.getString(R.string.animtion))
            } catch (Exception unused) {
            }
        }

        override fun onDelete() {
            try {
                this@EngineActivity.pausePlayer()
                this@EngineActivity.trackViewEntity.deleteEntity
            this@EngineActivity.updateTime()
                this@EngineActivity.iTrimLineCallback.onEmptySelect()
            } catch (Exception unused) {
                if (this@EngineActivity.iTrimLineCallback != null) {
                    this@EngineActivity.iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun update() {
            this@EngineActivity.blurredImageView.postInvalidate()
        }

        override fun onDone() {
            this@EngineActivity.hideFragment()
            if ((this@EngineActivity.blurredImageView.getEntity_select() is QuranEntity) || (this@EngineActivity.blurredImageView.getEntity_select() is BismilahEntity)) {
                this@EngineActivity.iTrimLineCallback.onSelectEntity(this@EngineActivity.trackViewEntity.getSelectedEntity(), -1.0f)
            }
        }

        override fun onColor() {
            try {
                this@EngineActivity.pausePlayer()
                val bismilahEntity = this@EngineActivity.trackViewEntity.getSelectedEntity().getEntityView()
                this@EngineActivity.trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                val engineActivity = this@EngineActivity
                engineActivity.mCurrentFragment = ColorBismilahFragment.getInstance(engineActivity.iBismilahEntityCallback, bismilahEntity, this@EngineActivity.mResources)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(null)
            } catch (Exception unused) {
            }
        }

        override fun fromTheStart() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.trackViewEntity.translateFromStart()
        }

        override fun fromNow() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.trackViewEntity.translateFromNow()
        }

        override fun untilNow() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.trackViewEntity.translateUntilNow()
        }

        override fun untilTheEnd() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.trackViewEntity.translateEndNow()
        }
    }
    private val iEditEntityCallback = object : EditEntityFragment.IEditEntityCallback {
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {
            this@EngineActivity.blurredImageView.setPreset(ayaTextPreset)
        }

        override fun updateAya(i: Int) {
            this@EngineActivity.blurredImageView.setColorAya(i)
        }

        override fun updateTrsl(i: Int) {
            this@EngineActivity.blurredImageView.setColorTrsl(i)
        }

        override fun onFont() {
            try {
                this@EngineActivity.pausePlayer()
                var quranEntity: QuranEntity? = this as QuranEntity@EngineActivity.trackViewEntity.getSelectedEntity().getEntityView()
                this@EngineActivity.trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                val engineActivity = this@EngineActivity
                engineActivity.mCurrentFragment = FontFragment.getInstance(engineActivity.iFontCallback, quranEntity.getNameFont(), quranEntity.getPaintAya().getTypeface())
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
                val engineActivity2 = this@EngineActivity
                engineActivity2.setupShowFragment(engineActivity2.mResources.getString(R.string.font))
            } catch (Exception unused) {
            }
        }

        override fun onIcon() {
            try {
                this@EngineActivity.pausePlayer()
                var quranEntity: QuranEntity? = this as QuranEntity@EngineActivity.trackViewEntity.getSelectedEntity().getEntityView()
                this@EngineActivity.trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                val engineActivity = this@EngineActivity
                engineActivity.mCurrentFragment = EditIconQuranFragment.getInstance(engineActivity.iQuranIconCallback, quranEntity.getIcon())
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
                val engineActivity2 = this@EngineActivity
                engineActivity2.setupShowFragment(engineActivity2.mResources.getString(R.string.icon))
            } catch (Exception unused) {
            }
        }

        override fun onAnim() {
            try {
                this@EngineActivity.pausePlayer()
                var quranEntity: QuranEntity? = this as QuranEntity@EngineActivity.trackViewEntity.getSelectedEntity().getEntityView()
                this@EngineActivity.trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                this@EngineActivity.mCurrentFragment = EffectAyaFragment.get(quranEntity.getEntityQuran().getTransition(), this@EngineActivity.mResources, this@EngineActivity.iTransitionCallback, this@EngineActivity.trackViewEntity.getSelectedEntity())
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
                val engineActivity = this@EngineActivity
                engineActivity.setupShowFragment(engineActivity.mResources.getString(R.string.animtion))
            } catch (Exception unused) {
            }
        }

        override fun onDelete() {
            try {
                this@EngineActivity.pausePlayer()
                this@EngineActivity.trackViewEntity.deleteEntity
            this@EngineActivity.updateTime()
                this@EngineActivity.iTrimLineCallback.onEmptySelect()
            } catch (Exception unused) {
                if (this@EngineActivity.iTrimLineCallback != null) {
                    this@EngineActivity.iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onDone() {
            this@EngineActivity.hideFragment()
            if (this@EngineActivity.blurredImageView.getEntity_select() is QuranEntity) {
                this@EngineActivity.iTrimLineCallback.onSelectEntity(this@EngineActivity.trackViewEntity.getSelectedEntity(), -1.0f)
            }
        }

        override fun onColor() {
            try {
                this@EngineActivity.pausePlayer()
                var quranEntity: QuranEntity? = this as QuranEntity@EngineActivity.trackViewEntity.getSelectedEntity().getEntityView()
                this@EngineActivity.trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                val engineActivity = this@EngineActivity
                engineActivity.mCurrentFragment = ColorAyaFragment.getInstance(engineActivity.iEditEntityCallback, quranEntity, this@EngineActivity.mResources)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(null)
            } catch (Exception unused) {
            }
        }

        override fun onEdit() {
            try {
                this@EngineActivity.pausePlayer()
                var quranEntity: QuranEntity? = this as QuranEntity@EngineActivity.trackViewEntity.getSelectedEntity().getEntityView()
                this@EngineActivity.trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                val engineActivity = this@EngineActivity
                engineActivity.mCurrentFragment = EditTextFragment.getInstance(engineActivity.iEdiTextCallback, quranEntity)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(null)
            } catch (Exception unused) {
            }
        }

        override fun onCut() {
            try {
                this@EngineActivity.pausePlayer()
                val engineActivity = this@EngineActivity
                engineActivity.splitEntity(engineActivity as QuranEntity.trackViewEntity.getSelectedEntity().getEntityView())
            } catch (Exception unused) {
                if (this@EngineActivity.iTrimLineCallback != null) {
                    this@EngineActivity.iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onDuplicate() {
            try {
                this@EngineActivity.pausePlayer()
                val engineActivity = this@EngineActivity
                engineActivity.duplicateEntity(engineActivity as QuranEntity.trackViewEntity.getSelectedEntity().getEntityView())
                this@EngineActivity.updateTime()
            } catch (Exception unused) {
                if (this@EngineActivity.iTrimLineCallback != null) {
                    this@EngineActivity.iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun fromTheStart() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.trackViewEntity.translateFromStart()
        }

        override fun fromNow() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.trackViewEntity.translateFromNow()
        }

        override fun untilNow() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.trackViewEntity.translateUntilNow()
        }

        override fun untilTheEnd() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.trackViewEntity.translateEndNow()
        }
    }
    private val iEditTrstEntityCallback = object : EditTrslEntityFragment.IEditEntityCallback {
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {
            this@EngineActivity.blurredImageView.setTrslPreset(ayaTextPreset)
        }

        override fun updateAya(i: Int) {
            this@EngineActivity.blurredImageView.setColorTrsl(i)
        }

        override fun updateTrsl(i: Int) {
            this@EngineActivity.blurredImageView.setColorTrsl(i)
        }

        override fun onFont() {
            try {
                this@EngineActivity.pausePlayer()
                var translationQuranEntity: TranslationQuranEntity? = .trackViewEntity.getSelectedEntity().getEntityView()
                this@EngineActivity.trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                val engineActivity = this@EngineActivity
                engineActivity.mCurrentFragment = FontFragment.getInstance(engineActivity.iFontCallback, translationQuranEntity.getNameFont(), translationQuranEntity.getPaintAya().getTypeface())
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
                val engineActivity2 = this@EngineActivity
                engineActivity2.setupShowFragment(engineActivity2.mResources.getString(R.string.font))
            } catch (Exception unused) {
            }
        }

        override fun onIcon() {
            try {
                this@EngineActivity.pausePlayer()
                var quranEntity: QuranEntity? = this as QuranEntity@EngineActivity.trackViewEntity.getSelectedEntity().getEntityView()
                this@EngineActivity.trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                val engineActivity = this@EngineActivity
                engineActivity.mCurrentFragment = EditIconQuranFragment.getInstance(engineActivity.iQuranIconCallback, quranEntity.getIcon())
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
                val engineActivity2 = this@EngineActivity
                engineActivity2.setupShowFragment(engineActivity2.mResources.getString(R.string.icon))
            } catch (Exception unused) {
            }
        }

        override fun onAnim() {
            try {
                this@EngineActivity.pausePlayer()
                var quranEntity: QuranEntity? = this as QuranEntity@EngineActivity.trackViewEntity.getSelectedEntity().getEntityView()
                this@EngineActivity.trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                this@EngineActivity.mCurrentFragment = EffectAyaFragment.get(quranEntity.getEntityQuran().getTransition(), this@EngineActivity.mResources, this@EngineActivity.iTransitionCallback, this@EngineActivity.trackViewEntity.getSelectedEntity())
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
                val engineActivity = this@EngineActivity
                engineActivity.setupShowFragment(engineActivity.mResources.getString(R.string.animtion))
            } catch (Exception unused) {
            }
        }

        override fun onDelete() {
            try {
                this@EngineActivity.pausePlayer()
                this@EngineActivity.trackViewEntity.deleteEntity
            this@EngineActivity.updateTime()
                this@EngineActivity.iTrimLineCallback.onEmptySelect()
            } catch (Exception unused) {
                if (this@EngineActivity.iTrimLineCallback != null) {
                    this@EngineActivity.iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onDone() {
            this@EngineActivity.hideFragment()
            if (this@EngineActivity.blurredImageView.getEntity_select() is TranslationQuranEntity) {
                this@EngineActivity.iTrimLineCallback.onSelectEntity(this@EngineActivity.trackViewEntity.getSelectedEntity(), -1.0f)
            }
        }

        override fun onColor() {
            try {
                this@EngineActivity.pausePlayer()
                var translationQuranEntity: TranslationQuranEntity? = .trackViewEntity.getSelectedEntity().getEntityView()
                this@EngineActivity.trackViewEntity.updateCursurToSelectEntity()
                val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                val engineActivity = this@EngineActivity
                engineActivity.mCurrentFragment = ColorTrslAyaFragment.getInstance(engineActivity.iEditTrstEntityCallback, translationQuranEntity, this@EngineActivity.mResources)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(null)
            } catch (Exception unused) {
            }
        }

        override fun onEdit() {
            try {
                this@EngineActivity.pausePlayer()
                this@EngineActivity.isToCrop = true
                var translationQuranEntity: TranslationQuranEntity? = .trackViewEntity.getSelectedEntity().getEntityView()
                var intent = Intent(this@EngineActivity, EditTrslTxtActivity::class.java)
                intent.putExtra("surah_name", "")
                intent.putExtra("reader_name", translationQuranEntity.getTxt())
                intent.putExtra("isBg", translationQuranEntity.isHaveBg())
                intent.putExtra("clrBg", translationQuranEntity.getClrBg())
                this@EngineActivity.editTrslResult.launch
            this@EngineActivity.overridePendingTransition(0, 0)
            } catch (Exception e) {
                e.printStackTrace()
            }
        }

        override fun onCut() {
            try {
                this@EngineActivity.pausePlayer()
                val engineActivity = this@EngineActivity
                engineActivity.splitEntity(engineActivity as TranslationQuranEntity.trackViewEntity.getSelectedEntity().getEntityView())
            } catch (Exception unused) {
                if (this@EngineActivity.iTrimLineCallback != null) {
                    this@EngineActivity.iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onDuplicate() {
            try {
                this@EngineActivity.pausePlayer()
                val engineActivity = this@EngineActivity
                engineActivity.duplicateEntity(engineActivity as TranslationQuranEntity.trackViewEntity.getSelectedEntity().getEntityView())
                this@EngineActivity.updateTime()
            } catch (Exception unused) {
                if (this@EngineActivity.iTrimLineCallback != null) {
                    this@EngineActivity.iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun fromTheStart() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.trackViewEntity.translateFromStart()
        }

        override fun fromNow() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.trackViewEntity.translateFromNow()
        }

        override fun untilNow() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.trackViewEntity.translateUntilNow()
        }

        override fun untilTheEnd() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.trackViewEntity.translateEndNow()
        }
    }
    private val iEditMultipleCallback = object : EditMultipleEntityFragment.IEditMultipleCallback {
        override fun onDelete() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.dialogDeleteSelected()
        }
    }
    private val iEditMediaCallback = object : EditMediaFragment.IEditMediaCallback {
        override fun onReplace() {
        }

        override fun updateEntity(effectAudioType: EffectAudioType, entityAudio: EntityAudio) {
            for (int i = 0; i < this@EngineActivity.trackViewEntity.getEntityListAudio().size; i++) {
                var entityAudio2: EntityAudio? = this@EngineActivity.trackViewEntity.getEntityListAudio()[]
                if (entityAudio2 != entityAudio && entityAudio2.visible()) {
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
            this@EngineActivity.hideFragment()
            this@EngineActivity.iTrimLineCallback.onSelectEntity(this@EngineActivity.trackViewEntity.getSelectedEntity(), -1.0f)
        }

        override fun startPreview() {
            if (this@EngineActivity.trackViewEntity.getSelectedEntity() is EntityAudio) {
                var entityAudio: EntityAudio? = this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity()
                if (entityAudio.getMediaPlayer().isPlaying()) {
                    return
                }
                this@EngineActivity.trackViewEntity.previewEntity
            this@EngineActivity.mIsPlaying = true
                this@EngineActivity.trackViewEntity.translateToStart(entityAudio)
                val engineActivity = this@EngineActivity
                engineActivity.startCursur = engineActivity.trackViewEntity.getCurrent_cursur_position()
                this@EngineActivity.startTimelineAnimationPreview(entityAudio)
            }
        }

        override fun pausePreview() {
            if (this@EngineActivity.mIsPlaying && (this@EngineActivity.trackViewEntity.getSelectedEntity() is EntityAudio)) {
                var entityAudio: EntityAudio? = this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity()
                this@EngineActivity.mIsPlaying = false
                this@EngineActivity.pauseTimelineAnimation()
                this@EngineActivity.trackViewEntity.setPlaying(this@EngineActivity.mIsPlaying)
                this@EngineActivity.blurredImageView.setPlaying(this@EngineActivity.mIsPlaying)
                try {
                    if (entityAudio.getMediaPlayer() == null || !entityAudio.getMediaPlayer().isPlaying()) {
                        return
                    }
                    entityAudio.getMediaPlayer().pause()
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }

        override fun onCmdPlay(str: String) {
            pausePreview()
            if (this@EngineActivity.trackViewEntity.getSelectedEntity() is EntityAudio) {
                this@EngineActivity.applyffectPlayAuto(str, this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity())
            }
        }

        override fun onCmd(str: String) {
            pausePreview()
            if (this@EngineActivity.trackViewEntity.getSelectedEntity() is EntityAudio) {
                this@EngineActivity.applyffect(str, this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity())
            }
        }

        override fun onCmdAll(effectAudio: EffectAudio) {
            pausePreview()
            this@EngineActivity.showProgressSimple()
            this@EngineActivity.applyffectAll(effectAudio, 0)
        }

        override fun onDuplicate() {
            try {
                if (this@EngineActivity.trackViewEntity.getSelectedEntity() is EntityAudio) {
                    var entityAudio: EntityAudio? = this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity()
                    this@EngineActivity.pausePlayer()
                    this@EngineActivity.duplicateEntityAudio(entityAudio.getMediaPlayer().getDuration(), entityAudio)
                    this@EngineActivity.updateTime()
                }
            } catch (Exception unused) {
                if (this@EngineActivity.iTrimLineCallback != null) {
                    this@EngineActivity.iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onDelete() {
            try {
                this@EngineActivity.pausePlayer()
                this@EngineActivity.trackViewEntity.deleteMediaEntity()
                this@EngineActivity.updateTime()
                this@EngineActivity.iTrimLineCallback.onEmptySelect()
            } catch (Exception unused) {
                if (this@EngineActivity.iTrimLineCallback != null) {
                    this@EngineActivity.iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun onCut() {
            try {
                this@EngineActivity.pausePlayer()
                if (this@EngineActivity.trackViewEntity.getSelectedEntity() is EntityAudio) {
                    var entityAudio: EntityAudio? = this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity()
                    var abs: Float = Math.abs(this@EngineActivity.trackViewEntity.getCurrentPosition())
                    if (abs >= entityAudio.getRect().left && abs <= entityAudio.getRect().right) {
                        var second_in_screenNoScale: Float = this@EngineActivity.trackViewEntity.getSecond_in_screenNoScale() * 0.1f
                        if (abs <= entityAudio.getRect().left || abs >= entityAudio.getRect().left + second_in_screenNoScale) {
                            if (abs >= entityAudio.getRect().right || abs <= entityAudio.getRect().right - second_in_screenNoScale) {
                                var round: Int = Math.round((Math.abs(Math.round((this@EngineActivity.trackViewEntity.getCurrentPosition() / this@EngineActivity.trackViewEntity.getSecond_in_screen()) * 1000.0f)) - Math.abs(Math.round((entityAudio.getRect().left / this@EngineActivity.trackViewEntity.getSecond_in_screen()) * 1000.0f))) + entityAudio.getStart())
                                var split: EntityAudio? = entityAudio.split(abs)
                                split.setAmps(entityAudio.getAmps())
                                split.setRenderer(entityAudio.getRenderer())
                                split.addPathHttp(entityAudio.getPaths_http())
                                split.setPath_ffmpeg_effect(entityAudio.getPath_ffmpeg_effect())
                                split.setVideo_path(entityAudio.getVideo_path())
                                split.setApplyEffectInPreview(entityAudio.isApplyEffectInPreview())
                                split.setEffectAudio(entityAudio.getEffectAudio())
                                split.setmScaleFactor(entityAudio.getmScaleFactor())
                                split.setMediaPlayer(entityAudio.getMediaPlayer())
                                split.setPath_ffmpeg(entityAudio.getPath_ffmpeg())
                                split.setIndex(entityAudio.getIndex() + 1)
                                split.setEnd(entityAudio.getEnd())
                                var f: Float = round
                                split.setStart(f)
                                split.setMin_duration
            this@EngineActivity.trackViewEntity.splitAudio(split, split.getIndex())
                                this@EngineActivity.trackViewEntity.stackSplit(entityAudio)
                                entityAudio.setCurrentRect()
                                entityAudio.setRight(abs)
                                entityAudio.setMax((entityAudio.getRect().right / entityAudio.getmScaleFactor()) - ((entityAudio.getRect().left / entityAudio.getmScaleFactor()) - entityAudio.getOffset_left()))
                                entityAudio.setEnd(f)
                                split.setOffset_right(entityAudio.getOffset_right())
                                entityAudio.setOffset_right(0.0f)
                                split.setOffset(entityAudio.getOffset() + entityAudio.getOffset_left() + (entityAudio.getRect().width() / entityAudio.getmScaleFactor()))
                                entityAudio.onChange()
                                split.setSecond_in_screen(this@EngineActivity.trackViewEntity.getSecond_in_screenNoScale())
                                split.updateEffect()
                                entityAudio.updateEffect()
                                this@EngineActivity.trackViewEntity.stackSplit
            this@EngineActivity.trackViewEntity.invalidate()
                            }
                        }
                    }
                }
            } catch (Exception unused) {
                if (this@EngineActivity.iTrimLineCallback != null) {
                    this@EngineActivity.iTrimLineCallback.onEmptySelect()
                }
            }
        }

        override fun reverbEffect() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.findViewById(R.id.layout_menu).setVisibility(4)
            val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
            var entityAudio: EntityAudio? = this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity()
            val engineActivity = this@EngineActivity
            engineActivity.mCurrentFragment = ReverbePresetFragment.getInstance(engineActivity.iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
            beginTransaction.commit()
        }

        override fun echoEffect() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.findViewById(R.id.layout_menu).setVisibility(4)
            val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
            var entityAudio: EntityAudio? = this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity()
            val engineActivity = this@EngineActivity
            engineActivity.mCurrentFragment = EchoEffectFragment.getInstance(engineActivity.iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
            beginTransaction.commit()
        }

        override fun noice() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.findViewById(R.id.layout_menu).setVisibility(4)
            val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
            var entityAudio: EntityAudio? = this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity()
            val engineActivity = this@EngineActivity
            engineActivity.mCurrentFragment = RemoveNoiceFragment.getInstance(engineActivity.iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
            beginTransaction.commit()
        }

        override fun enhanceVoice() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.findViewById(R.id.layout_menu).setVisibility(4)
            val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
            var entityAudio: EntityAudio? = this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity()
            val engineActivity = this@EngineActivity
            engineActivity.mCurrentFragment = EnhanceVoiceFragment.getInstance(engineActivity.iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
            beginTransaction.commit()
        }

        override fun speedffect() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.findViewById(R.id.layout_menu).setVisibility(4)
            val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
            var entityAudio: EntityAudio? = this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity()
            val engineActivity = this@EngineActivity
            engineActivity.mCurrentFragment = SpeedFragment.getInstance(engineActivity.iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
            beginTransaction.commit()
        }

        override fun volumeEffect() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.findViewById(R.id.layout_menu).setVisibility(4)
            val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
            var entityAudio: EntityAudio? = this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity()
            val engineActivity = this@EngineActivity
            engineActivity.mCurrentFragment = VolumeFragment.getInstance(engineActivity.iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
            beginTransaction.commit()
        }

        override fun pitchffect() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.findViewById(R.id.layout_menu).setVisibility(4)
            val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
            var entityAudio: EntityAudio? = this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity()
            val engineActivity = this@EngineActivity
            engineActivity.mCurrentFragment = PitchFragment.getInstance(engineActivity.iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
            beginTransaction.commit()
        }

        override fun fadeffect() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.findViewById(R.id.layout_menu).setVisibility(4)
            val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
            var entityAudio: EntityAudio? = this as EntityAudio@EngineActivity.trackViewEntity.getSelectedEntity()
            val engineActivity = this@EngineActivity
            engineActivity.mCurrentFragment = FadeInOutFragment.getInstance(engineActivity.iEditMediaCallback, entityAudio)
            beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
            beginTransaction.commit()
        }
    }
    private val iEdiTextCallback = object : EditTextFragment.IEdiTextCallback {
        override fun onDone(entityQuranTimeline: EntityQuranTimeline) {
            this@EngineActivity.setupHideFragment()
            if (entityQuranTimeline != null) {
                this@EngineActivity.showEditEntity(entityQuranTimeline)
            }
        }

        override fun onUpdate(quranEntity: QuranEntity) {
            this@EngineActivity.blurredImageView.postInvalidate()
            this@EngineActivity.trackViewEntity.postInvalidate()
        }
    }
    private val iTransitionCallback = object : EffectAyaFragment.ITransition {
        override fun toSubscribe() {
        }

        override fun destroy(entityQuranTimeline: EntityQuranTimeline) {
            if (entityQuranTimeline == null) {
                return
            }
            entityQuranTimeline.getQuranEntity().setAnimTest(false)
            entityQuranTimeline.getQuranEntity().endAnimator()
            this@EngineActivity.blurredImageView.invalidate()
        }

        override fun playing(entityQuranTimeline: EntityQuranTimeline) {
            if (entityQuranTimeline == null) {
                return
            }
            entityQuranTimeline.getQuranEntity().setAnimTest(true)
        }

        override fun onHideFragment(entityQuranTimeline: EntityQuranTimeline) {
            this@EngineActivity.hideFragment()
            try {
                this@EngineActivity.iTrimLineCallback.onSelectEntity(entityQuranTimeline, -1.0f)
            } catch (Exception e) {
                e.printStackTrace()
            }
        }

        override fun in(str: String, entityQuranTimeline: EntityQuranTimeline) {
            if (entityQuranTimeline == null) {
                return
            }
            if (entityQuranTimeline.getTransition() == null) {
                entityQuranTimeline.setTransition(Transition())
            }
            entityQuranTimeline.getTransition().setIn(true)
            entityQuranTimeline.getTransition().setType_in(str)
            EffectAyaFragment.instance.updateView(entityQuranTimeline.getTransition().getDuration_in(), entityQuranTimeline.getTransition())
            entityQuranTimeline.getQuranEntity().endAnimator()
            entityQuranTimeline.getQuranEntity().runIn((entityQuranTimeline.getTransition().getDuration_in() * 1000.0f), true, entityQuranTimeline.getTransition().getType_in())
        }

        override fun out(str: String, entityQuranTimeline: EntityQuranTimeline) {
            if (entityQuranTimeline == null) {
                return
            }
            if (entityQuranTimeline.getTransition() == null) {
                entityQuranTimeline.setTransition(Transition())
            }
            entityQuranTimeline.getTransition().setOut(true)
            entityQuranTimeline.getTransition().setType_out(str)
            EffectAyaFragment.instance.updateView(entityQuranTimeline.getTransition().getDuration_out(), entityQuranTimeline.getTransition())
            entityQuranTimeline.getQuranEntity().endAnimator()
            entityQuranTimeline.getQuranEntity().runOut((entityQuranTimeline.getTransition().getDuration_out() * 1000.0f), true, entityQuranTimeline.getTransition().getType_out())
        }

        override fun remove(i: Int, entityQuranTimeline: EntityQuranTimeline) {
            if (i == 0) {
                entityQuranTimeline.getTransition().setIn(false)
                entityQuranTimeline.getQuranEntity().endAnimator()
            }
            if (i == 1) {
                entityQuranTimeline.getTransition().setOut(false)
                entityQuranTimeline.getQuranEntity().endAnimator()
            }
        }

        override fun updateDurationIn(f: Float, entityQuranTimeline: EntityQuranTimeline) {
            if (entityQuranTimeline == null) {
                return
            }
            entityQuranTimeline.getTransition().setDuration_in(f)
            entityQuranTimeline.getQuranEntity().endAnimator()
            entityQuranTimeline.getQuranEntity().runIn((entityQuranTimeline.getTransition().getDuration_in() * 1000.0f), true, entityQuranTimeline.getTransition().getType_in())
        }

        override fun updateDurationOut(f: Float, entityQuranTimeline: EntityQuranTimeline) {
            if (entityQuranTimeline == null) {
                return
            }
            entityQuranTimeline.getTransition().setDuration_out(f)
            entityQuranTimeline.getQuranEntity().endAnimator()
            entityQuranTimeline.getQuranEntity().runOut((entityQuranTimeline.getTransition().getDuration_out() * 1000.0f), true, entityQuranTimeline.getTransition().getType_out())
        }

        override fun applyAll(i: Int, entityQuranTimeline: EntityQuranTimeline) {
            this@EngineActivity.showProgress()
            val engineActivity = this@EngineActivity
            engineActivity.addUpdateAnim(engineActivity.trackViewEntity.getmIsi3adaTimeline(), entityQuranTimeline)
            val engineActivity2 = this@EngineActivity
            engineActivity2.addUpdateAnim(engineActivity2.trackViewEntity.getBismilahTimeline(), entityQuranTimeline)
            for (entityQuranTimeline2 in this@EngineActivity.trackViewEntity.getEntityListQuran()) {
                if (entityQuranTimeline2 != entityQuranTimeline) {
                    if (entityQuranTimeline.getTransition() == null) {
                        entityQuranTimeline2.setTransition(null)
                        return
                    }
                    if (entityQuranTimeline2.getTransition() == null) {
                        entityQuranTimeline2.setTransition(Transition())
                    }
                    entityQuranTimeline2.getTransition().setOut(entityQuranTimeline.getTransition().isOut())
                    entityQuranTimeline2.getTransition().setType_out(entityQuranTimeline.getTransition().getType_out())
                    entityQuranTimeline2.getTransition().setDuration_out(entityQuranTimeline.getTransition().getDuration_out())
                    entityQuranTimeline2.getTransition().setIn(entityQuranTimeline.getTransition().isIn())
                    entityQuranTimeline2.getTransition().setType_in(entityQuranTimeline.getTransition().getType_in())
                    entityQuranTimeline2.getTransition().setDuration_in(entityQuranTimeline.getTransition().getDuration_in())
                }
            }
            this@EngineActivity.hideProgressFragment()
        }
    }
    private val iTransitionBismilahCallback = object : EffectBismilahFragment.ITransition {
        override fun destroy(entityBismilahTimeline: EntityBismilahTimeline) {
            if (entityBismilahTimeline == null) {
                return
            }
            entityBismilahTimeline.getQuranEntity().setAnimTest(false)
            entityBismilahTimeline.getQuranEntity().endAnimator()
            this@EngineActivity.blurredImageView.invalidate()
        }

        override fun playing(entityBismilahTimeline: EntityBismilahTimeline) {
            if (entityBismilahTimeline == null) {
                return
            }
            entityBismilahTimeline.getQuranEntity().setAnimTest(true)
        }

        override fun onHideFragment(entityBismilahTimeline: EntityBismilahTimeline) {
            this@EngineActivity.hideFragment()
            try {
                this@EngineActivity.iTrimLineCallback.onSelectEntity(entityBismilahTimeline, -1.0f)
            } catch (Exception e) {
                e.printStackTrace()
            }
        }

        override fun in(str: String, entityBismilahTimeline: EntityBismilahTimeline) {
            if (entityBismilahTimeline == null) {
                return
            }
            if (entityBismilahTimeline.getTransition() == null) {
                entityBismilahTimeline.setTransition(Transition())
            }
            entityBismilahTimeline.getTransition().setIn(true)
            entityBismilahTimeline.getTransition().setType_in(str)
            EffectBismilahFragment.instance.updateView(entityBismilahTimeline.getTransition().getDuration_in(), entityBismilahTimeline.getTransition())
            entityBismilahTimeline.getQuranEntity().endAnimator()
            entityBismilahTimeline.getQuranEntity().runIn((entityBismilahTimeline.getTransition().getDuration_in() * 1000.0f), true, entityBismilahTimeline.getTransition().getType_in())
        }

        override fun out(str: String, entityBismilahTimeline: EntityBismilahTimeline) {
            if (entityBismilahTimeline == null) {
                return
            }
            if (entityBismilahTimeline.getTransition() == null) {
                entityBismilahTimeline.setTransition(Transition())
            }
            entityBismilahTimeline.getTransition().setOut(true)
            entityBismilahTimeline.getTransition().setType_out(str)
            EffectBismilahFragment.instance.updateView(entityBismilahTimeline.getTransition().getDuration_out(), entityBismilahTimeline.getTransition())
            entityBismilahTimeline.getQuranEntity().endAnimator()
            entityBismilahTimeline.getQuranEntity().runOut((entityBismilahTimeline.getTransition().getDuration_out() * 1000.0f), true, entityBismilahTimeline.getTransition().getType_out())
        }

        override fun remove(i: Int, entityBismilahTimeline: EntityBismilahTimeline) {
            if (i == 0) {
                entityBismilahTimeline.getTransition().setIn(false)
                entityBismilahTimeline.getQuranEntity().endAnimator()
            }
            if (i == 1) {
                entityBismilahTimeline.getTransition().setOut(false)
                entityBismilahTimeline.getQuranEntity().endAnimator()
            }
        }

        override fun updateDurationIn(f: Float, entityBismilahTimeline: EntityBismilahTimeline) {
            if (entityBismilahTimeline == null) {
                return
            }
            entityBismilahTimeline.getTransition().setDuration_in(f)
            entityBismilahTimeline.getQuranEntity().endAnimator()
            entityBismilahTimeline.getQuranEntity().runIn((entityBismilahTimeline.getTransition().getDuration_in() * 1000.0f), true, entityBismilahTimeline.getTransition().getType_in())
        }

        override fun updateDurationOut(f: Float, entityBismilahTimeline: EntityBismilahTimeline) {
            if (entityBismilahTimeline == null) {
                return
            }
            entityBismilahTimeline.getTransition().setDuration_out(f)
            entityBismilahTimeline.getQuranEntity().endAnimator()
            entityBismilahTimeline.getQuranEntity().runOut((entityBismilahTimeline.getTransition().getDuration_out() * 1000.0f), true, entityBismilahTimeline.getTransition().getType_out())
        }

        override fun applyAll(entityBismilahTimeline: EntityBismilahTimeline) {
            this@EngineActivity.showProgress()
            this@EngineActivity.addUpdateAnim(this@EngineActivity.trackViewEntity.getmIsi3adaTimeline() != entityBismilahTimeline ? this@EngineActivity.trackViewEntity.getmIsi3adaTimeline() : this@EngineActivity.trackViewEntity.getBismilahTimeline(), entityBismilahTimeline)
            for (entityQuranTimeline in this@EngineActivity.trackViewEntity.getEntityListQuran()) {
                if (entityBismilahTimeline.getTransition() == null) {
                    entityQuranTimeline.setTransition(null)
                    return
                }
                if (entityQuranTimeline.getTransition() == null) {
                    entityQuranTimeline.setTransition(Transition())
                }
                entityQuranTimeline.getTransition().setOut(entityBismilahTimeline.getTransition().isOut())
                entityQuranTimeline.getTransition().setType_out(entityBismilahTimeline.getTransition().getType_out())
                entityQuranTimeline.getTransition().setDuration_out(entityBismilahTimeline.getTransition().getDuration_out())
                entityQuranTimeline.getTransition().setIn(entityBismilahTimeline.getTransition().isIn())
                entityQuranTimeline.getTransition().setType_in(entityBismilahTimeline.getTransition().getType_in())
                entityQuranTimeline.getTransition().setDuration_in(entityBismilahTimeline.getTransition().getDuration_in())
            }
            this@EngineActivity.hideProgressFragment()
        }
    }
    private val frameLock = Any()
    private var pendingFramePath: String? = null
    private var isProcessingFrame: Boolean = false
    private val frameProcessorRunnable: Runnable = object : Runnable() {
        override fun run() {
            String str
            while (true) {
                synchronized(frameLock) {
                    if (this@EngineActivity.pendingFramePath == null) {
                        this@EngineActivity.isProcessingFrame = false
                        return
                    } else {
                        str = this@EngineActivity.pendingFramePath
                        this@EngineActivity.pendingFramePath = null
                    }
                }
                this@EngineActivity.processFrame(str)
            }
        }
    }

    fun toProVersion() {
    }

    override protected fun onPause() {
        super.onPause()
        try {
            if (this.isSaveTmpTemplate) {
                saveTemplateTmp()
            }
            if (this.isToCrop) {
                return
            }
            TrackEntityView.ITrimLineCallback iTrimLineCallback = this.iTrimLineCallback
            if (iTrimLineCallback != null) {
                iTrimLineCallback.onEmptySelect()
            }
            cancelDialog()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    override protected fun onResume() {
        Template template
        super.onResume()
        this.isToCrop = false
        this.isSaveTmpTemplate = true
        var str: String? = Common.pixabayBgFilePath
        if (str != null) {
            var fromFile: Uri? = Uri.fromFile(File(str))
            var str2: String? = Common.pixabayBgType
            if (str2 == null || !MimeTypes.BASE_TYPE_VIDEO.equals(str2)) {
                handleImg(fromFile)
            } else {
                handleVideo(fromFile)
            }
            Common.pixabayBgFilePath = null
            Common.pixabayBgType = null
        }
        val list = Common.freeElements
        if (list == null || (template = this.mTemplate) == null) {
            return
        }
        template.getFreeElementList().clear()
        val it = list.iterator()
        while (it.hasNext()) {
            this.mTemplate.addFreeElement(it.next())
        }
    }

    fun cancelDialog() {
        var dialog = this.dialog
        if (dialog != null && dialog.isShowing()) {
            this.dialog.dismiss()
        }
        this.dialog = null
    }

    fun cancelDialogInternet() {
        var dialog = this.dialogInternet
        if (dialog != null && dialog.isShowing()) {
            this.dialogInternet.dismiss()
        }
        this.dialogInternet = null
    }

    fun dialog() {
        try {
            this.isSaveTmpTemplate = false
            pausePlayer()
            var dialog = Dialog(this)
            this.dialog = dialog
            dialog.setCancelable(true)
            this.dialog.requestWindowFeature(1)
            this.dialog.getWindow().setLayout(-1, -2)
            this.dialog.getWindow().setBackgroundDrawable(ColorDrawable(0))
            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog, (ViewGroup) null)
            this.dialog.setContentView(inflate)
            ((TextCustumFont) inflate.findViewById(R.id.dialog_title)).setText(this.mResources.getString(R.string.exit))
            ((TextCustumFont) inflate.findViewById(R.id.dialog_message)).setText(this.mResources.getString(R.string.are_you_sure_want_to_leave_this_work))
            var buttonCustumFont = (ButtonCustumFont) inflate.findViewById(R.id.dialog_no)
            buttonCustumFont.setText(this.mResources.getString(R.string.leave))
            buttonCustumFont.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    LocalPersistence.deleteTemplate(this@EngineActivity, Common.TEMPLATE_TMP)
                    this@EngineActivity.cancelDialog()
                    this@EngineActivity.startActivity(Intent(this@EngineActivity, WorkUserActivity::class.java))
                    this@EngineActivity.finish()
                }
            })
            var buttonCustumFont2 = (ButtonCustumFont) inflate.findViewById(R.id.dialog_yes)
            buttonCustumFont2.setText(this.mResources.getString(R.string.Continue))
            buttonCustumFont2.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    this@EngineActivity.cancelDialog()
                }
            })
            this.dialog.show()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    fun dialogNoInternet(uri: Uri) {
        try {
            var dialog = Dialog(this)
            this.dialogInternet = dialog
            dialog.setCancelable(false)
            this.dialogInternet.requestWindowFeature(1)
            this.dialogInternet.getWindow().setLayout(-1, -2)
            this.dialogInternet.getWindow().setBackgroundDrawable(ColorDrawable(0))
            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog, (ViewGroup) null)
            this.dialogInternet.setContentView(inflate)
            ((TextCustumFont) inflate.findViewById(R.id.dialog_title)).setText(this.mResources.getString(R.string.no_connection))
            ((TextCustumFont) inflate.findViewById(R.id.dialog_message)).setText(this.mResources.getString(R.string.msj_connection_on))
            var buttonCustumFont = (ButtonCustumFont) inflate.findViewById(R.id.dialog_no)
            buttonCustumFont.setText(this.mResources.getString(R.string.ignore))
            buttonCustumFont.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    this@EngineActivity.cancelDialogInternet()
                    this@EngineActivity.hideProgressFragment()
                }
            })
            var buttonCustumFont2 = (ButtonCustumFont) inflate.findViewById(R.id.dialog_yes)
            buttonCustumFont2.setText(this.mResources.getString(R.string.retry))
            buttonCustumFont2.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    if (NetworkUtils.isNetworkAvailable(this@EngineActivity)) {
                        this@EngineActivity.cancelDialogInternet()
                        this@EngineActivity.addAudioTemplateHttp(uri, 0, null)
                    }
                }
            })
            this.dialogInternet.show()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    fun dialogNoInternetList(list: List<String>) {
        var dialog = Dialog(this)
        this.dialogInternet = dialog
        dialog.setCancelable(false)
        this.dialogInternet.requestWindowFeature(1)
        this.dialogInternet.getWindow().setLayout(-1, -2)
        this.dialogInternet.getWindow().setBackgroundDrawable(ColorDrawable(0))
        val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog, (ViewGroup) null)
        this.dialogInternet.setContentView(inflate)
        ((TextCustumFont) inflate.findViewById(R.id.dialog_title)).setText(this.mResources.getString(R.string.no_connection))
        ((TextCustumFont) inflate.findViewById(R.id.dialog_message)).setText(this.mResources.getString(R.string.msj_connection_on))
        var buttonCustumFont = (ButtonCustumFont) inflate.findViewById(R.id.dialog_no)
        buttonCustumFont.setText(this.mResources.getString(R.string.ignore))
        buttonCustumFont.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                this@EngineActivity.runOnUiThread(object : Runnable {
                    override fun run() {
                        this@EngineActivity.trackViewEntity.invalidate()
                        this@EngineActivity.updateTime()
                        if (this@EngineActivity.mTemplate.getQuranEntityList().isEmpty()) {
                            this@EngineActivity.blurredImageView.invalidate()
                        }
                        this@EngineActivity.cancelDialogInternet()
                        this@EngineActivity.hideProgressFragment()
                    }
                })
            }
        })
        var buttonCustumFont2 = (ButtonCustumFont) inflate.findViewById(R.id.dialog_yes)
        buttonCustumFont2.setText(this.mResources.getString(R.string.retry))
        buttonCustumFont2.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (NetworkUtils.isNetworkAvailable(this@EngineActivity)) {
                    this@EngineActivity.cancelDialogInternet()
                    this@EngineActivity.addAudioRecitersTemplate(list, 0, null)
                }
            }
        })
        this.dialogInternet.show()
    }

    private fun releaseWakeLock() {
        try {
            getWindow().clearFlags(128)
        } catch (Exception unused) {
        }
    }

    private fun clearFFmpeg() {
        val it = this.id_ffmpeg.iterator()
        while (it.hasNext()) {
            FFmpegKit.cancel(it.next().toLong())
        }
    }

    override protected fun onDestroy() {
        super.onDestroy()
        try {
            Glide[].clearMemory()
        } catch (Exception unused) {
        }
        clearFFmpeg()
        releaseWakeLock()
        clearCallback()
        pausePlayer()
    }

    override protected fun attachBaseContext(context: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(context))
    }

    override protected fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_time_line)
        getOnBackPressedDispatcher().addCallback(this, this.onBackPressedCallback)
        val insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
        insetsController.setAppearanceLightStatusBars(false)
        insetsController.setAppearanceLightNavigationBars(false)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), object : OnApplyWindowInsetsListener() { // from class: Runnable
            override
            WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
                return EngineActivity.lambda$onCreate$0(view, windowInsetsCompat)
            }
        })
        this.mResources = getResources()
        setStatusBarColor(-15658735)
        setNavigationBarColor(-14935010)
        wakeLockAquire()
        showProgress()
        loadTemplate()
        initLauncher()
        this.vibrationHelper = MyVibrationHelper(this)
        initTimeLineView()
        initViews()
        checkUriShared()
    }

    static WindowInsetsCompat lambda$onCreate$0(View view, WindowInsetsCompat windowInsetsCompat) {
        val insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
        return windowInsetsCompat
    }

    private fun checkUriShared() {
        var stringExtra: String? = getIntent().getStringExtra("muri")
        if (stringExtra != null) {
            addUriAudioToQuranFragment(Uri.parse(stringExtra), null)
        }
    }

    private fun loadTemplate() {
        String stringExtra
        val template = (Template) LocalPersistence.readObjectFromFile(this, Common.TEMPLATE_TMP)
        this.mTemplate = template
        if (template == null && getIntent() != null && (stringExtra = getIntent().getStringExtra(Common.TEMPLATE)) != null) {
            val template2 = (Template) LocalPersistence.readObjectFromFile(this, stringExtra)
            this.mTemplate = template2
            if (template2 != null) {
                if (template2.getName_drawable() != null) {
                    this.uri_bg = "android.resource://" + getPackageName() + "/drawable/" + DrawableHelper.getIDDrawableByName(this.mTemplate.getName_drawable())
                } else {
                    this.uri_bg = this.mTemplate.getUri_bg()
                }
                if (this.mTemplate.getWidth() < 1 || this.mTemplate.getHeight() < 1) {
                    this.mTemplate.setWidthAndHeight(720, 1280)
                }
            }
        }
        val template3 = this.mTemplate
        if (template3 == null) {
            this.mTemplate = Template()
            var stringExtra2: String? = getIntent().getStringExtra("img_bg")
            this.uri_bg = stringExtra2
            if (stringExtra2 != null) {
                this.mTemplate.setUri_bg(stringExtra2)
            } else {
                Map.Entry<String, Integer> randomDrawableEntry = DrawableHelper.getRandomDrawableEntry()
                var str: String? = "android.resource://" + getPackageName() + "/drawable/" + randomDrawableEntry.getValue()
                this.uri_bg = str
                this.mTemplate.setUri_bg(str)
                this.mTemplate.setName_drawable(randomDrawableEntry.getKey())
            }
            this.mTemplate.setWidthAndHeight(720, 1280)
        } else {
            if (template3.getName_drawable() != null) {
                this.uri_bg = "android.resource://" + getPackageName() + "/drawable/" + DrawableHelper.getIDDrawableByName(this.mTemplate.getName_drawable())
            } else {
                this.uri_bg = this.mTemplate.getUri_bg()
            }
            if (this.mTemplate.getWidth() < 1 || this.mTemplate.getHeight() < 1) {
                this.mTemplate.setWidthAndHeight(720, 1280)
            }
        }
        var file = FileUtils.getFile(getApplicationContext())
        if (file != null) {
            this.mTemplate.setFolder_template(file.getAbsolutePath())
        }
    }

    fun addEntityFromTemplate() {
        RectF rectF
        val engineActivity = this
        val template = engineActivity.mTemplate
        if (template != null) {
            var z: Boolean = template.getIpad_type() == IpadType.GRADIENT.ordinal || engineActivity.mTemplate.getIpad_type() == IpadType.MASK_BRUSH.ordinal || engineActivity.mTemplate.getIpad_type() == IpadType.BLACK_LAYER.ordinal
            val loadFontFromAsset = UtilsFileLast.loadFontFromAsset(engineActivity, "fonts/arabic/خط فارس الكوفي.otf")
            val createFromAsset = Typeface.createFromAsset(getResources().getAssets(), "fonts/ReadexPro_Medium.ttf")
            for (entityQuranTemplate in engineActivity.mTemplate.getQuranEntityList()) {
                addEntity(entityQuranTemplate.getAya(), entityQuranTemplate.getComplete_aya(), entityQuranTemplate.getTranslation(), entityQuranTemplate.getTranslation_complete(), entityQuranTemplate.getLeft(), entityQuranTemplate.getRight(), entityQuranTemplate.getIndexNumber(), entityQuranTemplate.getNumber(), entityQuranTemplate.getColor(), entityQuranTemplate.getName_font(), entityQuranTemplate.getTransition(), z, entityQuranTemplate.getIcon(), entityQuranTemplate.getStartWord_index(), entityQuranTemplate.getEndWord_index(), entityQuranTemplate.getScale(), entityQuranTemplate.getFactor_size(), entityQuranTemplate.getFactor_sizeTrl(), RectF(entityQuranTemplate.getRectF().getL(), entityQuranTemplate.getRectF().getT(), entityQuranTemplate.getRectF().getR(), entityQuranTemplate.getRectF().getB()), loadFontFromAsset, createFromAsset, entityQuranTemplate.getColorTrsl(), entityQuranTemplate.getPreset())
                engineActivity = this
            }
            val engineActivity2 = engineActivity
            for (val it = engineActivity2.mTemplate.getTranslationTemplateList().iterator(); it.hasNext(); it = it) {
                val next = it.next()
                addEntityTrsl(next.getAya(), next.getLeft(), next.getRight(), next.getNumber(), next.getColor(), next.getName_font(), next.getTransition(), next.getScale(), next.getFactor_size(), RectF(next.getRectF().getL(), next.getRectF().getT(), next.getRectF().getR(), next.getRectF().getB()), next.getPreset(), next.getClr_bg(), next.isHaveBg())
            }
            if (engineActivity2.mTemplate.getEntityIsti3adaTemplate() != null) {
                addEntityIsti3ada(engineActivity2.mTemplate.getEntityIsti3adaTemplate().getAya(), engineActivity2.mTemplate.getEntityIsti3adaTemplate().getLeft(), engineActivity2.mTemplate.getEntityIsti3adaTemplate().getRight(), engineActivity2.mTemplate.getEntityIsti3adaTemplate().getColor(), engineActivity2.mTemplate.getEntityIsti3adaTemplate().getTransition(), engineActivity2.mTemplate.getEntityIsti3adaTemplate().getScale(), engineActivity2.mTemplate.getEntityIsti3adaTemplate().getFactor_size(), RectF(engineActivity2.mTemplate.getEntityIsti3adaTemplate().getRectF().getL(), engineActivity2.mTemplate.getEntityIsti3adaTemplate().getRectF().getT(), engineActivity2.mTemplate.getEntityIsti3adaTemplate().getRectF().getR(), engineActivity2.mTemplate.getEntityIsti3adaTemplate().getRectF().getB()), engineActivity2.mTemplate.getEntityIsti3adaTemplate().getPreset())
            }
            if (engineActivity2.mTemplate.getEntityBismilahTemplate() != null) {
                addEntityBissmilah(engineActivity2.mTemplate.getEntityBismilahTemplate().getAya(), engineActivity2.mTemplate.getEntityBismilahTemplate().getLeft(), engineActivity2.mTemplate.getEntityBismilahTemplate().getRight(), engineActivity2.mTemplate.getEntityBismilahTemplate().getColor(), engineActivity2.mTemplate.getEntityBismilahTemplate().getTransition(), engineActivity2.mTemplate.getEntityBismilahTemplate().getScale(), engineActivity2.mTemplate.getEntityBismilahTemplate().getFactor_size(), RectF(engineActivity2.mTemplate.getEntityBismilahTemplate().getRectF().getL(), engineActivity2.mTemplate.getEntityBismilahTemplate().getRectF().getT(), engineActivity2.mTemplate.getEntityBismilahTemplate().getRectF().getR(), engineActivity2.mTemplate.getEntityBismilahTemplate().getRectF().getB()), engineActivity2.mTemplate.getEntityBismilahTemplate().getPreset())
            }
            if (engineActivity2.mTemplate.getEntitySurahTemplate() != null) {
                if (engineActivity2.mTemplate.getEntitySurahTemplate().getRectF() == null) {
                    rectF = engineActivity2.blurredImageView.getRectFSurahName()
                } else {
                    rectF = RectF(engineActivity2.mTemplate.getEntitySurahTemplate().getRectF().getL() * engineActivity2.blurredImageView.getmCanvas_width(), engineActivity2.mTemplate.getEntitySurahTemplate().getRectF().getT() * engineActivity2.blurredImageView.getmCanvas_height(), engineActivity2.mTemplate.getEntitySurahTemplate().getRectF().getR() * engineActivity2.blurredImageView.getmCanvas_width(), engineActivity2.mTemplate.getEntitySurahTemplate().getRectF().getB() * engineActivity2.blurredImageView.getmCanvas_height())
                }
                engineActivity2.blurredImageView.setSurahNameEntity(engineActivity2.mTemplate.getEntitySurahTemplate().getName(), engineActivity2.mTemplate.getEntitySurahTemplate().getReader(), rectF, engineActivity2.mTemplate.getEntitySurahTemplate().getFactor_scale(), engineActivity2.mTemplate.getEntitySurahTemplate().getName_font() == null ? "خط الإبل.otf" : engineActivity2.mTemplate.getEntitySurahTemplate().getName_font(), engineActivity2.mTemplate.getEntitySurahTemplate().getClr(), engineActivity2.mTemplate.getEntitySurahTemplate().getPreset(), engineActivity2.mTemplate.getEntitySurahTemplate().getStyle(), engineActivity2.mTemplate.getEntitySurahTemplate().getIndex_surah(), engineActivity2.mTemplate.getEntitySurahTemplate().isHaveBg(), engineActivity2.mTemplate.getEntitySurahTemplate().getClrBg() == 0 ? ViewCompat.MEASURED_STATE_MASK : engineActivity2.mTemplate.getEntitySurahTemplate().getClrBg())
            }
            if (!engineActivity2.mTemplate.getEntityMediaList().isEmpty()) {
                try {
                    val entityMedia = engineActivity2.mTemplate.getEntityMediaList()[]
                    if (entityMedia.getVideo_path() != null) {
                        if (engineActivity2.mTemplate.getUri_upload_extract_audio_video() == null) {
                            engineActivity2.runOnUiThread(object : Runnable() {
                                override fun run() {
                                    this@EngineActivity.hideProgressFragment()
                                }
                            })
                        } else {
                            AudioUtils.copyToLocalAsync(engineActivity2, Uri.parse(engineActivity2.mTemplate.getUri_upload_extract_audio_video()).toString(), engineActivity2.mTemplate.getFolder_template(), object : AudioUtils.Callback {
                                override fun onSuccess(str: String) {
                                    entityMedia.setVideo_path(str)
                                    if (this@EngineActivity.mTemplate.getExtension() != null) {
                                        val engineActivity3 = this@EngineActivity
                                        engineActivity3.addAudioFromVideoWithExtention(engineActivity3.mTemplate.getExtension(), entityMedia.getVideo_path(), 0)
                                    } else {
                                        this@EngineActivity.start_extenstion = 0
                                        this@EngineActivity.extractAudioFromVideoRecursive(entityMedia.getVideo_path(), 0, true, 0)
                                    }
                                }

                                override fun onError(exc: Exception) {
                                    exc.printStackTrace()
                                }
                            })
                        }
                    } else if (entityMedia.getUri() != null) {
                        if (entityMedia.getPaths_https() != null) {
                            if (NetworkUtils.isNetworkAvailable(this)) {
                                engineActivity2.addAudioRecitersTemplate(entityMedia.getPaths_https(), 0, null)
                            } else {
                                engineActivity2.runOnUiThread(object : Runnable() {
                                    override fun run() {
                                        this@EngineActivity.dialogNoInternetList(entityMedia.getPaths_https())
                                    }
                                })
                            }
                        } else if (entityMedia.getUri().contains("http")) {
                            var parse: Uri? = Uri.parse(entityMedia.getUri())
                            if (NetworkUtils.isNetworkAvailable(this)) {
                                engineActivity2.addAudioTemplateHttp(parse, 0, null)
                            } else {
                                engineActivity2.runOnUiThread(object : Runnable() {
                                    override fun run() {
                                        this@EngineActivity.dialogNoInternet(parse)
                                    }
                                })
                            }
                        } else {
                            engineActivity2.addAudioTemplateHttp(Uri.parse(entityMedia.getUri()), 0, null)
                        }
                    }
                    return
                } catch (Exception e) {
                    e.printStackTrace()
                    engineActivity2.runOnUiThread(object : Runnable() {
                        override fun run() {
                            this@EngineActivity.hideProgressFragment()
                        }
                    })
                    return
                }
            }
            engineActivity2.runOnUiThread(object : Runnable() {
                override fun run() {
                    this@EngineActivity.trackViewEntity.invalidate()
                    this@EngineActivity.updateTime()
                    if (this@EngineActivity.mTemplate.getQuranEntityList().isEmpty()) {
                        this@EngineActivity.blurredImageView.invalidate()
                    }
                    this@EngineActivity.hideProgressFragment()
                }
            })
        }
    }

    private fun initLauncher() {
        this.activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback() { // from class: Runnable
            override fun onActivityResult(result: ActivityResult) {
                this@EngineActivity.(ActivityResult) obj)
            }
        })
    }

 void m640lambda$initLauncher$1$hazemnurmontagevideoquranEngineActivity(ActivityResult activityResult) {
        if (activityResult.getResultCode() == -1) {
            var data = activityResult.getData()
            if (data != null && data.getData() != null) {
                var data2: Uri? = data.getData()
                try {
                    getContentResolver().takePersistableUriPermission(data2, 1)
                } catch (Exception e) {
                    e.printStackTrace()
                }
                addUriAudioToQuranFragment(data2, null)
                return
            }
            Toast.makeText(this, this.mResources.getString(R.string.no_audio_select), 0).show()
            return
        }
        Toast.makeText(this, this.mResources.getString(R.string.audio_cancel), 0).show()
    }

    fun addUriAudioToQuranFragment(uri: Uri, str: String) {
        try {
            val beginTransaction = getSupportFragmentManager().beginTransaction()
            this.mCurrentFragment = AddQuranFragment.getInstance(this.iAddQuran, this.mResources, uri, str, "-")
            beginTransaction.replace(R.id.m_container, this.mCurrentFragment)
            beginTransaction.commit()
            runOnUiThread(object : Runnable() {
                override fun run() {
                    val engineActivity = this@EngineActivity
                    engineActivity.setupShowFragment(engineActivity.mResources.getString(R.string.quran))
                }
            })
        } catch (Exception unused) {
        }
    }

    fun pauseTimelineAnimation() {
        stop()
        val smoothTimelineAnimator = this.valueAnimator
        if (smoothTimelineAnimator == null || !smoothTimelineAnimator.isRunning()) {
            return
        }
        this.startCursur = this.valueAnimator.getCurrentTimeMs()
        this.valueAnimator.stop()
        this.valueAnimator = null
    }

    fun pausePlayer() {
        try {
            hideLayoutResolution()
            if (this.mIsPlaying) {
                this.mIsPlaying = false
                pauseTimelineAnimation()
                this.trackViewEntity.setPlaying(this.mIsPlaying)
                this.blurredImageView.setPlaying(this.mIsPlaying)
                this.trackViewEntity.invalidate()
                for (entityAudio in this.trackViewEntity.getEntityListAudio()) {
                    try {
                        if (entityAudio.getMediaPlayer() != null && entityAudio.getMediaPlayer().isPlaying()) {
                            entityAudio.getMediaPlayer().pause()
                        }
                    } catch (Exception e) {
                        e.printStackTrace()
                    }
                }
                this.btnPlayPause.setImageResource(R.drawable.play_btn)
                stop()
            }
            this.trackViewEntity.pauseScroll()
        } catch (Exception unused) {
        }
    }

    private fun updateBtnToStart(i: Int) {
        if (i == 0) {
            this.btnToStart.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
            this.btnToStart.setClickable(false)
        } else {
            this.btnToStart.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
            this.btnToStart.setClickable(true)
        }
    }

    fun updateBtnToStart() {
        if (this.trackViewEntity.getCurrent_cursur_position() == 0) {
            this.btnToStart.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
            this.btnToStart.setClickable(false)
        } else {
            this.btnToStart.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
            this.btnToStart.setClickable(true)
        }
    }

    fun updateBtnToEnd() {
        if (this.trackViewEntity.getCurrent_cursur_position() == this.trackViewEntity.getMaxTime()) {
            this.btnToEnd.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
            this.btnToEnd.setClickable(false)
        } else {
            this.btnToEnd.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
            this.btnToEnd.setClickable(true)
        }
    }

    fun updateBtnToEndAndStart() {
        this.btnToStart.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
        this.btnToStart.setClickable(true)
        this.btnToEnd.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
        this.btnToEnd.setClickable(true)
    }

    fun initTypeVideo() {
        try {
            val blurredImageView = this.blurredImageView
            blurredImageView.initCanvasDimension(blurredImageView.getWidth(), this.blurredImageView.getHeight(), this.mTemplate.geTypeResize())
            AudioUtils.copyToLocalAsync(this, Uri.parse(this.mTemplate.getUri_original_upload_video()).toString(), this.mTemplate.getFolder_template(), object : AudioUtils.Callback {
                override fun onError(exc: Exception) {
                }

                override fun onSuccess(str: String) {
                    this@EngineActivity.mTemplate.setUri_media_video(str)
                    var fileVideo = FileUtils.getFileVideo(this@EngineActivity.mTemplate.getFolder_template())
                    var file = File(fileVideo, "frame_%04d.jpg")
                    var file2 = File(fileVideo, "frame_0001.jpg")
                    var height: Int = this@EngineActivity.blurredImageView.getHeight()
                    this@EngineActivity.endFrame = min(4, Math.round(r0.trackViewEntity.getMaxTime() / 1000.0f))
                    if (this@EngineActivity.endFrame == 0) {
                        this@EngineActivity.endFrame = 4
                    }
                    this@EngineActivity.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", str, "-ss", "0", "-t", "" + this@EngineActivity.endFrame, "-r", "25", "-vf", "scale=" + height + ":" + height + ":force_original_aspect_ratio=increase", "-q:v", "0", "-threads", "4", "-an", "-y", file.getAbsolutePath()), object : FFmpegSessionCompleteCallback() {.1
                        /* JADX WARN: Multi-variable type inference failed */
                        /* JADX WARN: Removed duplicated region for block: B:36:0x069e A[Catch: Exception -> 0x0919, TryCatch #0 {Exception -> 0x0919, blocks: (B:3:0x0004, B:5:0x0092, B:6:0x0120, B:8:0x0169, B:10:0x017d, B:12:0x0191, B:14:0x01a5, B:16:0x01b9, B:19:0x01cf, B:21:0x01e3, B:23:0x0250, B:24:0x0272, B:26:0x0286, B:31:0x02ae, B:34:0x0690, B:36:0x069e, B:37:0x08ae, B:39:0x08c2, B:40:0x08ec, B:45:0x08d3, B:49:0x06d7, B:50:0x0326, B:52:0x033a, B:54:0x034e, B:57:0x0364, B:59:0x03d4, B:60:0x03f6, B:62:0x0409, B:67:0x0431, B:68:0x04b2, B:70:0x0528, B:71:0x054a, B:73:0x055d, B:78:0x0585, B:80:0x059e, B:81:0x061a, B:82:0x0710, B:84:0x0720, B:85:0x078f, B:87:0x07ff, B:88:0x0821, B:90:0x0834, B:95:0x085c, B:97:0x0875, B:98:0x0898, B:99:0x088d, B:100:0x0758, B:101:0x00bb, B:103:0x00cf, B:104:0x00f8), top: B:2:0x0004 }] */
                        /* JADX WARN: Removed duplicated region for block: B:39:0x08c2 A[Catch: Exception -> 0x0919, TryCatch #0 {Exception -> 0x0919, blocks: (B:3:0x0004, B:5:0x0092, B:6:0x0120, B:8:0x0169, B:10:0x017d, B:12:0x0191, B:14:0x01a5, B:16:0x01b9, B:19:0x01cf, B:21:0x01e3, B:23:0x0250, B:24:0x0272, B:26:0x0286, B:31:0x02ae, B:34:0x0690, B:36:0x069e, B:37:0x08ae, B:39:0x08c2, B:40:0x08ec, B:45:0x08d3, B:49:0x06d7, B:50:0x0326, B:52:0x033a, B:54:0x034e, B:57:0x0364, B:59:0x03d4, B:60:0x03f6, B:62:0x0409, B:67:0x0431, B:68:0x04b2, B:70:0x0528, B:71:0x054a, B:73:0x055d, B:78:0x0585, B:80:0x059e, B:81:0x061a, B:82:0x0710, B:84:0x0720, B:85:0x078f, B:87:0x07ff, B:88:0x0821, B:90:0x0834, B:95:0x085c, B:97:0x0875, B:98:0x0898, B:99:0x088d, B:100:0x0758, B:101:0x00bb, B:103:0x00cf, B:104:0x00f8), top: B:2:0x0004 }] */
                        /* JADX WARN: Removed duplicated region for block: B:45:0x08d3 A[Catch: Exception -> 0x0919, TryCatch #0 {Exception -> 0x0919, blocks: (B:3:0x0004, B:5:0x0092, B:6:0x0120, B:8:0x0169, B:10:0x017d, B:12:0x0191, B:14:0x01a5, B:16:0x01b9, B:19:0x01cf, B:21:0x01e3, B:23:0x0250, B:24:0x0272, B:26:0x0286, B:31:0x02ae, B:34:0x0690, B:36:0x069e, B:37:0x08ae, B:39:0x08c2, B:40:0x08ec, B:45:0x08d3, B:49:0x06d7, B:50:0x0326, B:52:0x033a, B:54:0x034e, B:57:0x0364, B:59:0x03d4, B:60:0x03f6, B:62:0x0409, B:67:0x0431, B:68:0x04b2, B:70:0x0528, B:71:0x054a, B:73:0x055d, B:78:0x0585, B:80:0x059e, B:81:0x061a, B:82:0x0710, B:84:0x0720, B:85:0x078f, B:87:0x07ff, B:88:0x0821, B:90:0x0834, B:95:0x085c, B:97:0x0875, B:98:0x0898, B:99:0x088d, B:100:0x0758, B:101:0x00bb, B:103:0x00cf, B:104:0x00f8), top: B:2:0x0004 }] */
                        /* JADX WARN: Removed duplicated region for block: B:49:0x06d7 A[Catch: Exception -> 0x0919, TryCatch #0 {Exception -> 0x0919, blocks: (B:3:0x0004, B:5:0x0092, B:6:0x0120, B:8:0x0169, B:10:0x017d, B:12:0x0191, B:14:0x01a5, B:16:0x01b9, B:19:0x01cf, B:21:0x01e3, B:23:0x0250, B:24:0x0272, B:26:0x0286, B:31:0x02ae, B:34:0x0690, B:36:0x069e, B:37:0x08ae, B:39:0x08c2, B:40:0x08ec, B:45:0x08d3, B:49:0x06d7, B:50:0x0326, B:52:0x033a, B:54:0x034e, B:57:0x0364, B:59:0x03d4, B:60:0x03f6, B:62:0x0409, B:67:0x0431, B:68:0x04b2, B:70:0x0528, B:71:0x054a, B:73:0x055d, B:78:0x0585, B:80:0x059e, B:81:0x061a, B:82:0x0710, B:84:0x0720, B:85:0x078f, B:87:0x07ff, B:88:0x0821, B:90:0x0834, B:95:0x085c, B:97:0x0875, B:98:0x0898, B:99:0x088d, B:100:0x0758, B:101:0x00bb, B:103:0x00cf, B:104:0x00f8), top: B:2:0x0004 }] */
                        override
                        /*
                            Code decompiled incorrectly, please refer to instructions dump.
                        */
                        fun apply(fFmpegSession: FFmpegSession) {
                            Bitmap cropTo16x9
                            var i: Int = 0
                            Rect rect
                            Bitmap cropToSquareWithRoundCorners
                            Bitmap bitmap
                            try {
                                this@EngineActivity.mTemplate.setFrame_bg(file2.getAbsolutePath())
                                val skipMemoryCache = Glide.with(this@EngineActivity).asBitmap().load(this@EngineActivity.mTemplate.getFrame_bg()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                                var i2: Int = height
                                var bitmap2: Bitmap? = skipMemoryCache as Bitmap.override(i2, i2).submit().get()
                                this@EngineActivity.blurredImageView.setGlass(this@EngineActivity.mTemplate.isGlass())
                                this@EngineActivity.blurredImageView.setVideo
            this@EngineActivity.blurredImageView.setBitmapOriginal(bitmap2)
                                if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                                    cropTo16x9 = BitmapCropper.cropTo9x16(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                                } else if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SQUARE.ordinal) {
                                    cropTo16x9 = BitmapCropper.cropTo1x1(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                                } else {
                                    cropTo16x9 = BitmapCropper.cropTo16x9(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                                }
                                this@EngineActivity.blurredImageView.updatePosCanvas
            this@EngineActivity.blurredImageView.updateIpad(cropTo16x9, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize())
                            } catch (Exception unused) {
                            }
                            if (this@EngineActivity.mTemplate.getIpad_type() != IpadType.BLACK_LAYER.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.GRADIENT.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.MASK_BRUSH.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.BLUE_TYPE.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.CASSET_IMG.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.CASSET_IMG_BLUR.ordinal) {
                                if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_NEOMORPHIC.ordinal) {
                                    var width: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.6f)
                                    var round: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                                    var round2: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                                    var i3: Int = width + round
                                    if (i3 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                        round -= i3 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                        i3 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    }
                                    var i4: Int = width + round2
                                    if (i4 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                        round2 -= i4 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                        i4 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    }
                                    if (round < 0) {
                                        round = 0
                                    }
                                    if (round2 < 0) {
                                        round2 = 0
                                    }
                                    rect = Rect(round, round2, i3, i4)
                                    this@EngineActivity.blurredImageView.setRadius_square(width)
                                    var width2: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                    var height2: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                    cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect, width, width2, height2)
                                    rect.right = rect.left + width2
                                    rect.bottom = rect.top + height2
                                    this@EngineActivity.blurredImageView.setRectSquare(rect)
                                } else {
                                    if (this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal) {
                                        var width3: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 1.0f)
                                        var height3: Int = (cropTo16x9.getHeight() * 0.5355f)
                                        var round3: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                                        var round4: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                                        var i5: Int = width3 + round3
                                        if (i5 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                            round3 -= i5 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                            i5 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                        }
                                        var i6: Int = height3 + round4
                                        if (i6 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                            round4 -= i6 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                            i6 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                        }
                                        if (round3 < 0) {
                                            round3 = 0
                                        }
                                        if (round4 < 0) {
                                            round4 = 0
                                        }
                                        rect = Rect(round3, round4, i5, i6)
                                        var width4: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                        var height4: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                        var cropToSquare: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect, width4, height4)
                                        this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                                        rect.right = rect.left + width4
                                        rect.bottom = rect.top + height4
                                        this@EngineActivity.blurredImageView.setRectSquare(rect)
                                        bitmap = cropToSquare
                                        var rect2 = Rect(rect
                                        if (this@EngineActivity.mTemplate.getGradient() == null) {
                                            this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap, this@EngineActivity.mTemplate.getGradient(), this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), rect2)
                                        } else {
                                            this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap, this@EngineActivity.mTemplate.getColor_ipad(), this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), rect2)
                                        }
                                        if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.BLUE_TYPE.ordinal) {
                                            i = this@EngineActivity.blurredImageView.getPaintLecture().getColor()
                                        } else {
                                            i = this@EngineActivity.blurredImageView.getPaintLecture().getColor() == -1 ? InputDeviceCompat.SOURCE_ANY : Common.COLOR_TRANSLATION
                                        }
                                        this@EngineActivity.blurredImageView.setClr_trsl
            this@EngineActivity.blurredImageView.setClr_aya(this@EngineActivity.blurredImageView.getPaintLecture().getColor())
                                        this@EngineActivity.addEntityFromTemplate()
                                        this@EngineActivity.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", str, "-ss", "" + this@EngineActivity.endFrame, "-r", "25", "-vf", "scale=" + height + ":" + height + ":force_original_aspect_ratio=increase", "-start_number", "" + (this@EngineActivity.endFrame * 25), "-q:v", "0", "-threads", "4", "-an", "-y", file.getAbsolutePath()), object : FFmpegSessionCompleteCallback() {.1.1
                                            override fun apply(fFmpegSession2: FFmpegSession) {
                                            }
                                        }).getSessionId()))
                                    }
                                    var width5: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.87530595f)
                                    var i7: Int = (width5 * 1.13f)
                                    var min: Int = min(width5, i7)
                                    var round5: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                                    var round6: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                                    var i8: Int = width5 + round5
                                    if (i8 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                        round5 -= i8 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                        i8 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    }
                                    var i9: Int = i7 + round6
                                    if (i9 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                        round6 -= i9 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                        i9 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    }
                                    if (round5 < 0) {
                                        round5 = 0
                                    }
                                    if (round6 < 0) {
                                        round6 = 0
                                    }
                                    rect = Rect(round5, round6, i8, i9)
                                    if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_CLASSIC.ordinal) {
                                        var width6: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                        var height5: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                        var cropToSquare2: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect, width6, height5)
                                        this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                                        rect.right = rect.left + width6
                                        rect.bottom = rect.top + height5
                                        this@EngineActivity.blurredImageView.setRectSquare(rect)
                                        cropToSquareWithRoundCorners = cropToSquare2
                                    } else {
                                        var i10: Int = (min * 0.10800001f)
                                        this@EngineActivity.blurredImageView.setRadius_square(i10)
                                        var width7: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                        var height6: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                        cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect, i10, width7, height6)
                                        rect.right = rect.left + width7
                                        rect.bottom = rect.top + height6
                                        this@EngineActivity.blurredImageView.setRectSquare(rect)
                                    }
                                }
                                bitmap = cropToSquareWithRoundCorners
                                var rect22 = Rect(rect
                                if (this@EngineActivity.mTemplate.getGradient() == null) {
                                }
                                if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.BLUE_TYPE.ordinal) {
                                }
                                this@EngineActivity.blurredImageView.setClr_trsl
            this@EngineActivity.blurredImageView.setClr_aya(this@EngineActivity.blurredImageView.getPaintLecture().getColor())
                                this@EngineActivity.addEntityFromTemplate()
                                this@EngineActivity.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", str, "-ss", "" + this@EngineActivity.endFrame, "-r", "25", "-vf", "scale=" + height + ":" + height + ":force_original_aspect_ratio=increase", "-start_number", "" + (this@EngineActivity.endFrame * 25), "-q:v", "0", "-threads", "4", "-an", "-y", file.getAbsolutePath()), object : FFmpegSessionCompleteCallback() {.1.1
                                    override fun apply(fFmpegSession2: FFmpegSession) {
                                    }
                                }).getSessionId()))
                            }
                            if (this@EngineActivity.mTemplate.getGradient() != null) {
                                this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), null as Bitmap, this@EngineActivity.mTemplate.getGradient(), this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), (Rect) null)
                            } else {
                                this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), null as Bitmap, this@EngineActivity.mTemplate.getColor_ipad(), this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), (Rect) null)
                            }
                            var width8: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 1.0f)
                            var height7: Int = (cropTo16x9.getHeight() * 0.5355f)
                            var round7: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                            var round8: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                            var i11: Int = width8 + round7
                            if (i11 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                round7 -= i11 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                i11 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                            }
                            var i12: Int = height7 + round8
                            if (i12 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                round8 -= i12 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                i12 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                            }
                            if (round7 < 0) {
                                round7 = 0
                            }
                            if (round8 < 0) {
                                round8 = 0
                            }
                            var rect3 = Rect(round7, round8, i11, i12)
                            if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.CASSET_IMG_BLUR.ordinal) {
                                this@EngineActivity.blurredImageView.setBitmapSquare(this@EngineActivity.blurredImageView.getBitmapBlured())
                            } else {
                                this@EngineActivity.blurredImageView.setBitmapSquare(cropTo16x9)
                            }
                            this@EngineActivity.blurredImageView.setRadius_square
            this@EngineActivity.blurredImageView.setRectSquare(rect3)
                            if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.BLUE_TYPE.ordinal) {
                            }
                            this@EngineActivity.blurredImageView.setClr_trsl
            this@EngineActivity.blurredImageView.setClr_aya(this@EngineActivity.blurredImageView.getPaintLecture().getColor())
                            this@EngineActivity.addEntityFromTemplate()
                            this@EngineActivity.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", str, "-ss", "" + this@EngineActivity.endFrame, "-r", "25", "-vf", "scale=" + height + ":" + height + ":force_original_aspect_ratio=increase", "-start_number", "" + (this@EngineActivity.endFrame * 25), "-q:v", "0", "-threads", "4", "-an", "-y", file.getAbsolutePath()), object : FFmpegSessionCompleteCallback() {.1.1
                                override fun apply(fFmpegSession2: FFmpegSession) {
                                }
                            }).getSessionId()))
                        }
                    }).getSessionId()))
                }
            })
        } catch (Exception e) {
            this.uri_bg = "android.resource://" + getPackageName() + "/drawable/" + R.drawable.bg_1
            this.mTemplate.setName_drawable("bg_1")
            this.mTemplate.setColor_ipad(-1)
            this.mTemplate.setVideoSquare(false)
            iniTypeImg()
            Log.e("Tag : ", "init " + e.getMessage())
        }
    }

    fun iniTypeImg() {
        this.executor.execute(object : Runnable() {
            /* JADX WARN: Multi-variable type inference failed */
            override fun run() {
                Bitmap bitmap
                Bitmap cropTo16x9
                Bitmap cropToSquareWithRoundCorners
                Bitmap bitmap2
                Rect rect
                var i: Int = 0
                try {
                    this@EngineActivity.blurredImageView.initCanvasDimension(this@EngineActivity.blurredImageView.getWidth(), this@EngineActivity.blurredImageView.getHeight(), this@EngineActivity.mTemplate.geTypeResize())
                    var height: Int = this@EngineActivity.blurredImageView.getHeight()
                    try {
                        bitmap = Glide as Bitmap.with(this@EngineActivity).asBitmap().load(this@EngineActivity.mTemplate.getUri_bg()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(height, height).submit().get()
                    } catch (Exception unused) {
                        this@EngineActivity.mTemplate.setColor_ipad(-1)
                        bitmap = Glide as Bitmap.with(this@EngineActivity).asBitmap().load(Integer.valueOf(R.drawable.bg_19)).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(height, height).submit().get()
                    }
                    this@EngineActivity.blurredImageView.setBitmapOriginal(this@EngineActivity.setupOriginalBitmap(bitmap, height))
                    if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                        cropTo16x9 = BitmapCropper.cropTo9x16(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                    } else if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SQUARE.ordinal) {
                        cropTo16x9 = BitmapCropper.cropTo1x1(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                    } else {
                        cropTo16x9 = BitmapCropper.cropTo16x9(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                    }
                    this@EngineActivity.blurredImageView.setGlass(this@EngineActivity.mTemplate.isGlass())
                    var i2: Int = 0
                    this@EngineActivity.blurredImageView.setVideo
            this@EngineActivity.blurredImageView.updatePosCanvas
            this@EngineActivity.blurredImageView.updateIpad(cropTo16x9, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize())
                    if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_NEOMORPHIC.ordinal) {
                        var width: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.6f)
                        var round: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                        var round2: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                        var i3: Int = width + round
                        if (i3 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                            round -= i3 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                            i3 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                        }
                        var i4: Int = width + round2
                        if (i4 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                            round2 -= i4 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                            i4 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                        }
                        if (round < 0) {
                            round = 0
                        }
                        if (round2 >= 0) {
                            i2 = round2
                        }
                        var rect2 = Rect(round, i2, i3, i4)
                        this@EngineActivity.blurredImageView.setRadius_square(width)
                        var width2: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                        var height2: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                        var cropToSquareWithRoundCorners2: Bitmap? = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect2, width, width2, height2)
                        rect2.right = rect2.left + width2
                        rect2.bottom = rect2.top + height2
                        this@EngineActivity.blurredImageView.setRectSquare(rect2)
                        bitmap2 = cropToSquareWithRoundCorners2
                        rect = rect2
                    } else {
                        if (this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal) {
                            var width3: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 1.0f)
                            var height3: Int = (cropTo16x9.getHeight() * 0.5355f)
                            var round3: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                            var round4: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                            var i5: Int = width3 + round3
                            if (i5 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                round3 -= i5 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                i5 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                            }
                            var i6: Int = height3 + round4
                            if (i6 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                round4 -= i6 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                i6 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                            }
                            if (round3 < 0) {
                                round3 = 0
                            }
                            if (round4 < 0) {
                                round4 = 0
                            }
                            var rect3 = Rect(round3, round4, i5, i6)
                            var width4: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                            var height4: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                            var cropToSquare: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect3, width4, height4)
                            this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                            rect3.right = rect3.left + width4
                            rect3.bottom = rect3.top + height4
                            this@EngineActivity.blurredImageView.setRectSquare(rect3)
                            bitmap2 = cropToSquare
                            rect = rect3
                        }
                        var width5: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.87530595f)
                        var i7: Int = (width5 * 1.13f)
                        var min: Int = min(width5, i7)
                        var round5: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                        var round6: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                        var i8: Int = width5 + round5
                        if (i8 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                            round5 -= i8 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                            i8 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                        }
                        var i9: Int = i7 + round6
                        if (i9 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                            round6 -= i9 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                            i9 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                        }
                        if (round5 < 0) {
                            round5 = 0
                        }
                        if (round6 < 0) {
                            round6 = 0
                        }
                        var rect4 = Rect(round5, round6, i8, i9)
                        if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_CLASSIC.ordinal) {
                            var width6: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                            var height5: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                            var cropToSquare2: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect4, width6, height5)
                            this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                            rect4.right = rect4.left + width6
                            rect4.bottom = rect4.top + height5
                            this@EngineActivity.blurredImageView.setRectSquare(rect4)
                            cropToSquareWithRoundCorners = cropToSquare2
                        } else {
                            var i10: Int = (min * 0.10800001f)
                            this@EngineActivity.blurredImageView.setRadius_square(i10)
                            var width7: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                            var height6: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                            cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect4, i10, width7, height6)
                            rect4.right = rect4.left + width7
                            rect4.bottom = rect4.top + height6
                            this@EngineActivity.blurredImageView.setRectSquare(rect4)
                        }
                        bitmap2 = cropToSquareWithRoundCorners
                        rect = rect4
                    }
                    if (this@EngineActivity.mTemplate.getGradient() != null) {
                        this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap2, this@EngineActivity.mTemplate.getGradient(), this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), rect)
                    } else {
                        this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap2, this@EngineActivity.mTemplate.getColor_ipad(), this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), rect)
                    }
                    if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.BLUE_TYPE.ordinal) {
                        i = this@EngineActivity.blurredImageView.getPaintLecture().getColor()
                    } else {
                        i = this@EngineActivity.blurredImageView.getPaintLecture().getColor() == -1 ? InputDeviceCompat.SOURCE_ANY : Common.COLOR_TRANSLATION
                    }
                    this@EngineActivity.blurredImageView.setClr_trsl
            this@EngineActivity.blurredImageView.setClr_aya(this@EngineActivity.blurredImageView.getPaintLecture().getColor())
                    this@EngineActivity.addEntityFromTemplate()
                } catch (Exception e) {
                    Log.e("Tag : ", "init " + e.getMessage())
                }
            }
        })
    }

    private fun initResolution() {
        this.tv_resolution = (TextCustumFont) findViewById(R.id.tv_resolution)
        this.layout_resolution = (LinearLayout) findViewById(R.id.layout_resolution)
        val linearLayout = (LinearLayout) findViewById(R.id.btn_setup_fps)
        this.btn_setup_fps = linearLayout
        linearLayout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (this@EngineActivity.layout_resolution == null) {
                    return
                }
                if (this@EngineActivity.layout_resolution.getVisibility() != 0) {
                    this@EngineActivity.layout_resolution.setVisibility(0)
                } else {
                    this@EngineActivity.layout_resolution.setVisibility(8)
                }
            }
        })
        this.seekBar_fps = (CustomDiscreteSeekBar) findViewById(R.id.seekbar_fps)
        if (this.mTemplate.getFps() == 15) {
            this.seekBar_fps.setProgress(0)
        } else if (this.mTemplate.getFps() == 25) {
            this.seekBar_fps.setProgress(1)
        } else if (this.mTemplate.getFps() == 30) {
            this.seekBar_fps.setProgress(2)
        } else if (this.mTemplate.getFps() == 50) {
            this.seekBar_fps.setProgress(3)
        } else {
            this.seekBar_fps.setProgress(4)
        }
        this.seekBar_fps.setOnProgressChangeListener(object : CustomDiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(customDiscreteSeekBar: CustomDiscreteSeekBar, i: Int, str: String, z: Boolean) {
            }

            override fun onStartTrackingTouch(customDiscreteSeekBar: CustomDiscreteSeekBar) {
            }

            override fun onStopTrackingTouch(customDiscreteSeekBar: CustomDiscreteSeekBar) {
                if (this@EngineActivity.mTemplate != null) {
                    this@EngineActivity.mTemplate.setFps(Integer.parseInt(this@EngineActivity.seekBar_fps.getCurrentLabel()))
                }
            }
        })
        this.tv_resolution.setText(this.mTemplate.getResolution())
        this.seekBar_res = (CustomDiscreteSeekBar) findViewById(R.id.seekbar_resolution)
        if (this.mTemplate.getResolution().equals("480p")) {
            this.seekBar_res.setProgress(0)
        } else if (this.mTemplate.getResolution().equals("720p")) {
            this.seekBar_res.setProgress(1)
        } else if (this.mTemplate.getResolution().equals("1080p")) {
            this.seekBar_res.setProgress(2)
        } else {
            this.seekBar_res.setProgress(3)
        }
        this.seekBar_res.setOnProgressChangeListener(object : CustomDiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(customDiscreteSeekBar: CustomDiscreteSeekBar, i: Int, str: String, z: Boolean) {
            }

            override fun onStartTrackingTouch(customDiscreteSeekBar: CustomDiscreteSeekBar) {
            }

            override fun onStopTrackingTouch(customDiscreteSeekBar: CustomDiscreteSeekBar) {
                if (this@EngineActivity.mTemplate != null) {
                    this@EngineActivity.mTemplate.setResolution(this@EngineActivity.seekBar_res.getCurrentLabel())
                    val size = AspectRatioCalculator.getSize(this@EngineActivity.mTemplate.geTypeResize(), this@EngineActivity.mTemplate.getResolution())
                    this@EngineActivity.tv_resolution.setText(this@EngineActivity.mTemplate.getResolution())
                    this@EngineActivity.mTemplate.setWidthAndHeight(size.getFirst().toInt(), size.getSecond().toInt())
                }
            }
        })
    }

    private fun initViews() {
        initResolution()
        val imageButton = (ImageButton) findViewById(R.id.btn_play_pause)
        this.btnPlayPause = imageButton
        imageButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                this@EngineActivity.hideLayoutResolution()
                if (this@EngineActivity.mIsPlaying) {
                    this@EngineActivity.mIsPlaying = false
                    this@EngineActivity.pauseTimelineAnimation()
                    this@EngineActivity.trackViewEntity.setPlaying(this@EngineActivity.mIsPlaying)
                    this@EngineActivity.blurredImageView.setPlaying(this@EngineActivity.mIsPlaying)
                    this@EngineActivity.trackViewEntity.invalidate()
                    for (entityAudio in this@EngineActivity.trackViewEntity.getEntityListAudio()) {
                        try {
                            if (entityAudio.visible() && entityAudio.getMediaPlayer() != null && entityAudio.getMediaPlayer().isPlaying()) {
                                entityAudio.getMediaPlayer().pause()
                            }
                        } catch (Exception e) {
                            e.printStackTrace()
                        }
                    }
                    this@EngineActivity.btnPlayPause.setImageResource(R.drawable.play_btn)
                    return
                }
                if (this@EngineActivity.current_position_time == 0) {
                    this@EngineActivity.trackViewEntity.updateCursur(0)
                }
                this@EngineActivity.trackViewEntity.calculMaxTime()
                this@EngineActivity.btnPlayPause.setImageResource(R.drawable.pause_24px)
                this@EngineActivity.updateBtnToEndAndStart()
                this@EngineActivity.current_position_time = System.currentTimeMillis().toInt()
                this@EngineActivity.mIsPlaying = true
                this@EngineActivity.trackViewEntity.setPlaying(false)
            this@EngineActivity.blurredImageView.setPlaying(false)
            this@EngineActivity.startTimelineAnimation()
            }
        })
        val imageButton2 = (ImageButton) findViewById(R.id.btn_to_end)
        this.btnToEnd = imageButton2
        imageButton2.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (this@EngineActivity.trackViewEntity.getCurrent_cursur_position() == this@EngineActivity.trackViewEntity.getMaxTime()) {
                    return
                }
                this@EngineActivity.blurredImageView.setProgress(1.0f)
                this@EngineActivity.pausePlayer()
                this@EngineActivity.startCursur = 0
                this@EngineActivity.trackViewEntity.translateToEnd()
                val engineActivity = this@EngineActivity
                engineActivity.updateViewTime(engineActivity.trackViewEntity.getMaxTime(), this@EngineActivity.trackViewEntity.getCurrent_cursur_position())
                this@EngineActivity.updateBtnToEnd()
                this@EngineActivity.updateBtnToStart()
            }
        })
        val imageButton3 = (ImageButton) findViewById(R.id.btn_to_start)
        this.btnToStart = imageButton3
        imageButton3.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (this@EngineActivity.trackViewEntity.getCurrent_cursur_position() == 0) {
                    return
                }
                this@EngineActivity.blurredImageView.setProgress(0.0f)
                this@EngineActivity.pausePlayer()
                this@EngineActivity.startCursur = 0
                this@EngineActivity.trackViewEntity.translateToStart()
                val engineActivity = this@EngineActivity
                engineActivity.updateViewTime(engineActivity.trackViewEntity.getMaxTime(), this@EngineActivity.trackViewEntity.getCurrent_cursur_position())
                this@EngineActivity.updateBtnToStart()
                this@EngineActivity.updateBtnToEnd()
            }
        })
        updateBtnToStart(this.mTemplate.getCurrentCursur())
        this.btnRedo = (ImageButton) findViewById(R.id.btn_redo)
        this.btnUndo = (ImageButton) findViewById(R.id.btn_undo)
        disableUndoBtn()
        disableRedoBtn()
        this.btnRedo.setOnClickListener(AnonymousClass23())
        this.btnUndo.setOnClickListener(AnonymousClass24())
        this.trackViewEntity.setRedoUndo(this.btnRedo, this.btnUndo)
        val blurredImageView = (BlurredImageView) findViewById(R.id.view)
        this.blurredImageView = blurredImageView
        blurredImageView.setPro(BillingPreferences.isSubscribed(this))
        this.blurredImageView.setiViewCallback(object : BlurredImageView.IViewCallback {
            override fun onDrawFinish() {
            }

            override fun onSquare() {
            }

            override fun onEndMove() {
                if (this@EngineActivity.blurredImageView.getEntity_select() != null) {
                    this@EngineActivity.blurredImageView.applyAll(this@EngineActivity.blurredImageView.getEntity_select().getFactor_scale(), this@EngineActivity.blurredImageView.getEntity_select().getRect(), this@EngineActivity.blurredImageView.getEntity_select().getMax_w(), this@EngineActivity.blurredImageView.getEntity_select().getMax_h())
                }
            }

            override fun onEndScale() {
                if (this@EngineActivity.blurredImageView.getEntity_select() != null) {
                    this@EngineActivity.blurredImageView.applyAll(this@EngineActivity.blurredImageView.getEntity_select().getFactor_scale(), this@EngineActivity.blurredImageView.getEntity_select().getRect(), this@EngineActivity.blurredImageView.getEntity_select().getMax_w(), this@EngineActivity.blurredImageView.getEntity_select().getMax_h())
                }
            }

            override fun onSelect(entityView: EntityView) {
                if (entityView is SurahNameEntity) {
                    try {
                        if (EditS_NameFragment.instance != null) {
                            return
                        }
                        this@EngineActivity.pausePlayer()
                        this@EngineActivity.selectSurahName()
                        return
                    } catch (Exception unused) {
                        return
                    }
                }
                if (entityView is QuranEntity) {
                    this@EngineActivity.trackViewEntity.selectEntity(entityView.getEntityQuran(), true)
                    this@EngineActivity.iTrimLineCallback.onSelectEntity(entityView.getEntityQuran(), 0.0f)
                } else if (entityView is BismilahEntity) {
                    val bismilahTimeline = ((BismilahEntity) entityView).getBismilahTimeline()
                    this@EngineActivity.trackViewEntity.selectEntity(bismilahTimeline, true)
                    this@EngineActivity.iTrimLineCallback.onSelectEntity(bismilahTimeline, 0.0f)
                } else if (entityView is TranslationQuranEntity) {
                    this@EngineActivity.trackViewEntity.selectEntity(entityView.getEntityTrslTimeline(), true)
                    this@EngineActivity.iTrimLineCallback.onSelectEntity(entityView.getEntityTrslTimeline(), 0.0f)
                }
            }

            override fun onEmtyClick() {
                this@EngineActivity.iTrimLineCallback.onEmptySelect()
            }

            override fun onWattermark() {
                this@EngineActivity.dialogWatermark()
            }
        })
        if (this.blurredImageView.true) {
            findViewById(R.id.to_pro).setVisibility(8)
        } else {
            findViewById(R.id.to_pro).setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    this@EngineActivity.toProVersion()
                }
            })
        }
        this.blurredImageView.post(object : Runnable() {
            override fun run() {
                if (this@EngineActivity.mTemplate.isVideoSquare()) {
                    this@EngineActivity.initTypeVideo()
                } else {
                    this@EngineActivity.iniTypeImg()
                }
            }
        })
        var buttonCustumFont = (ButtonCustumFont) findViewById(R.id.btn_export)
        this.btn_export = buttonCustumFont
        buttonCustumFont.setText(this.mResources.getString(R.string.export))
        this.btn_export.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                this@EngineActivity.isSaveTmpTemplate = false
                this@EngineActivity.pausePlayer()
                if (Build.VERSION.SDK_INT >= 33) {
                    this@EngineActivity.save()
                } else if (ContextCompat.checkSelfPermission(this@EngineActivity, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                    this@EngineActivity.save()
                } else {
                    ActivityCompat.requestPermissions(this@EngineActivity, arrayOf("android.permission.WRITE_EXTERNAL_STORAGE"), 1)
                }
            }
        })
        val imageButton4 = (ImageButton) findViewById(R.id.btn_cancel)
        this.btn_cancel = imageButton4
        imageButton4.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                this@EngineActivity.dialog()
            }
        })
        this.tv_tittle_fragment = (TextCustumFont) findViewById(R.id.tv_tittle_fragment)
        ((TextCustumFont) findViewById(R.id.tv_quran)).setText(this.mResources.getString(R.string.quran))
        ((TextCustumFont) findViewById(R.id.tv_bg)).setText(this.mResources.getString(R.string.bg))
        val textCustumFont = (TextCustumFont) findViewById(R.id.tv_ipad)
        textCustumFont.setText(this.mResources.getString(R.string.ipad))
        findViewById(R.id.btn_add_quran).setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                this@EngineActivity.pausePlayer()
                try {
                    val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                    val engineActivity = this@EngineActivity
                    engineActivity.mCurrentFragment = AddQuranFragment.getInstance(engineActivity.iAddQuran, this@EngineActivity.mResources)
                    beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                    beginTransaction.commit()
                    val engineActivity2 = this@EngineActivity
                    engineActivity2.setupShowFragment(engineActivity2.mResources.getString(R.string.quran))
                } catch (Exception unused) {
                }
            }
        })
        findViewById(R.id.btn_bg).setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                this@EngineActivity.pausePlayer()
                try {
                    val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                    val engineActivity = this@EngineActivity
                    engineActivity.mCurrentFragment = ChangeBgFragment.getInstance(engineActivity.iChangeBgCallback, this@EngineActivity.mResources, this@EngineActivity.mTemplate.getName_drawable())
                    beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                    beginTransaction.commitNow()
                    val engineActivity2 = this@EngineActivity
                    engineActivity2.setupShowFragment(engineActivity2.mResources.getString(R.string.bg))
                } catch (Exception unused) {
                }
            }
        })
        this.btnIpod = (LinearLayout) findViewById(R.id.btn_ipad)
        this.textChangeResize = (TextCustumFont) findViewById(R.id.tv_ratio)
        this.ivResize = (ImageView) findViewById(R.id.iv_ratio)
        this.ivIpod = (ImageView) findViewById(R.id.iv_ipod)
        this.btnChangeResize = (LinearLayout) findViewById(R.id.btn_change_aspect)
        if (this.blurredImageView.true) {
            this.btnChangeResize.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    this@EngineActivity.pausePlayer()
                    try {
                        val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                        val engineActivity = this@EngineActivity
                        engineActivity.mCurrentFragment = ResizeFragment.getInstance(engineActivity.iDimensionCallback, this@EngineActivity.mResources, "16")
                        beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                        beginTransaction.commit()
                        this@EngineActivity.setupShowFragment(null)
                    } catch (Exception unused) {
                    }
                }
            })
        } else {
            this.textChangeResize.setTextColor(-8355712)
            this.ivResize.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
            this.btnChangeResize.setBackgroundColor(0)
            this.btnChangeResize.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    this@EngineActivity.pausePlayer()
                    /* billing removed */
                }
            })
            textCustumFont.setTextColor(-8355712)
            this.ivIpod.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
            this.btnIpod.setBackgroundColor(0)
        }
        this.btnIpod.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                this@EngineActivity.pausePlayer()
                try {
                    val beginTransaction = this@EngineActivity.getSupportFragmentManager().beginTransaction()
                    val engineActivity = this@EngineActivity
                    engineActivity.mCurrentFragment = EditIpadFragment.getInstance(engineActivity.mResources, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.iIpadEditCallback, this@EngineActivity.mTemplate.getIndex_color(), this@EngineActivity.mTemplate.getGradient() != null, this@EngineActivity.mTemplate.isGlass())
                    beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment)
                    beginTransaction.commit()
                    val engineActivity2 = this@EngineActivity
                    engineActivity2.setupShowFragment(engineActivity2.mResources.getString(R.string.ipad))
                } catch (Exception unused) {
                }
            }
        })
        updateHitRatio(this.mTemplate.geTypeResize(), this.mTemplate.getImgResize())
    }

    private inner class AnonymousClass23 : View.OnClickListener {
        AnonymousClass23() {
        }

        override fun onClick(view: View) {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.showProgressSimple()
            object : Thread(object : Runnable {
                override fun run() {
                    this@EngineActivity.runOnUiThread(object : Runnable {
                        override fun run() {
                            this@EngineActivity.trackViewEntity.redo()
                            this@EngineActivity.hideProgressFragment()
                        }
                    })
                }
            }).start()
        }
    }

    private inner class AnonymousClass24 : View.OnClickListener {
        AnonymousClass24() {
        }

        override fun onClick(view: View) {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.showProgressSimple()
            object : Thread(object : Runnable {
                override fun run() {
                    this@EngineActivity.runOnUiThread(object : Runnable {
                        override fun run() {
                            this@EngineActivity.trackViewEntity.undo()
                            this@EngineActivity.hideProgressFragment()
                        }
                    })
                }
            }).start()
        }
    }

    fun save() {
        if (this.oneExport) {
            return
        }
        this.oneExport = true
        this.trackViewEntity.finishScroll()
        this.trackViewEntity.setOnProgress(true)
        this.blurredImageView.setNotDraw(true)
        if (!this.blurredImageView.true) {
            this.blurredImageView.setRemoveWattermark(false)
        }
        stop()
        showProgress()
        this.executor.execute(object : Runnable() {
            /* JADX WARN: Multi-variable type inference failed */
            override fun run() {
                Bitmap cropTo16x9
                Rect rect
                var i: Int = 0
                Bitmap cropToSquareWithRoundCorners
                var i2: Int = 0
                var i3: Int = 0
                Bitmap cropTo16x92
                try {
                    this@EngineActivity.trackViewEntity.calculMaxTime()
                    this@EngineActivity.blurredImageView.reset()
                    this@EngineActivity.blurredImageView.initCanvasDimension(this@EngineActivity.mTemplate.getWidth(), this@EngineActivity.mTemplate.getHeight(), this@EngineActivity.mTemplate.geTypeResize())
                    var max: Int = max(this@EngineActivity.mTemplate.getWidth(), this@EngineActivity.mTemplate.getHeight())
                    if (this@EngineActivity.mTemplate.getIpad_type() != IpadType.HEART.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.BATTERY.ordinal) {
                        if (this@EngineActivity.mTemplate.isVideoSquare() && (this@EngineActivity.mTemplate.getIpad_type() == IpadType.GRADIENT.ordinal || this@EngineActivity.mTemplate.getIpad_type() == IpadType.BLACK_LAYER.ordinal || this@EngineActivity.mTemplate.getIpad_type() == IpadType.MASK_BRUSH.ordinal || this@EngineActivity.mTemplate.getIpad_type() == IpadType.BLUE_TYPE.ordinal || this@EngineActivity.mTemplate.getIpad_type() == IpadType.CASSET_IMG.ordinal)) {
                            this@EngineActivity.blurredImageView.setBitmapOriginal(Bitmap.createBitmap(max, max, Bitmap.Config.ARGB_8888))
                            if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                                cropTo16x92 = BitmapCropper.cropTo9x16(this@EngineActivity.blurredImageView.getBitmapOriginal())
                            } else if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SQUARE.ordinal) {
                                cropTo16x92 = BitmapCropper.cropTo1x1(this@EngineActivity.blurredImageView.getBitmapOriginal())
                            } else {
                                cropTo16x92 = BitmapCropper.cropTo16x9(this@EngineActivity.blurredImageView.getBitmapOriginal())
                            }
                            this@EngineActivity.blurredImageView.updatePosCanvas(this@EngineActivity.mTemplate.getWidth(), this@EngineActivity.mTemplate.getHeight(), cropTo16x92)
                            this@EngineActivity.blurredImageView.updateIpad(cropTo16x92, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize())
                            var width: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 1.0f)
                            var height: Int = (cropTo16x92.getHeight() * 0.5355f)
                            this@EngineActivity.mTemplate.setDrawingTranslation(this@EngineActivity.blurredImageView.getBtmX(), this@EngineActivity.blurredImageView.getBtmY())
                            var round: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                            var round2: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                            var i4: Int = width + round
                            if (i4 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                round -= i4 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                i4 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                            }
                            var i5: Int = height + round2
                            if (i5 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                round2 -= i5 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                i5 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                            }
                            if (round < 0) {
                                round = 0
                            }
                            if (round2 < 0) {
                                round2 = 0
                            }
                            var rect2 = Rect(round, round2, i4, i5)
                            var width2: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                            var height2: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                            var cropToSquare: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect2, width2, height2)
                            this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                            rect2.right = rect2.left + width2
                            rect2.bottom = rect2.top + height2
                            this@EngineActivity.blurredImageView.setRectSquare
            this@EngineActivity.mTemplate.setUri_bg_ffmpeg(this@EngineActivity.blurredImageView.setupBitmapDraw(cropTo16x92, cropToSquare, this@EngineActivity.mTemplate))
                            this@EngineActivity.mTemplate.getSquareBitmapModel().set(this@EngineActivity.blurredImageView.getLeft_square(), this@EngineActivity.blurredImageView.getTop_square(), round, round2, rect2.width(), rect2.height(), cropToSquare.getWidth(), cropToSquare.getHeight(), 0)
                        } else {
                            this@EngineActivity.blurredImageView.setBitmapOriginal(this@EngineActivity.setupOriginalBitmap(Glide as Bitmap.with(this@EngineActivity).asBitmap().load(this@EngineActivity.mTemplate.isVideoSquare() ? this@EngineActivity.mTemplate.getFrame_bg() : this@EngineActivity.mTemplate.getUri_bg()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(max, max).submit().get(), max))
                            if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                                cropTo16x9 = BitmapCropper.cropTo9x16(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.mTemplate.getWidth(), this@EngineActivity.mTemplate.getHeight())
                            } else if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SQUARE.ordinal) {
                                cropTo16x9 = BitmapCropper.cropTo1x1(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.mTemplate.getWidth(), this@EngineActivity.mTemplate.getHeight())
                            } else {
                                cropTo16x9 = BitmapCropper.cropTo16x9(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.mTemplate.getWidth(), this@EngineActivity.mTemplate.getHeight())
                            }
                            var bitmap: Bitmap? = cropTo16x9
                            this@EngineActivity.blurredImageView.updatePosCanvas(this@EngineActivity.mTemplate.getWidth(), this@EngineActivity.mTemplate.getHeight(), bitmap)
                            this@EngineActivity.blurredImageView.updateIpad(bitmap, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize())
                            if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_NEOMORPHIC.ordinal) {
                                i = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.6f)
                                this@EngineActivity.mTemplate.setDrawingTranslation(this@EngineActivity.blurredImageView.getBtmX(), this@EngineActivity.blurredImageView.getBtmY())
                                i2 = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                                i3 = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                                var i6: Int = i + i2
                                if (i6 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                    i2 -= i6 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    i6 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                }
                                var i7: Int = i + i3
                                if (i7 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                    i3 -= i7 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    i7 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                }
                                if (i2 < 0) {
                                    i2 = 0
                                }
                                if (i3 < 0) {
                                    i3 = 0
                                }
                                rect = Rect(i2, i3, i6, i7)
                                var width3: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                var height3: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect, i, width3, height3)
                                rect.right = rect.left + width3
                                rect.bottom = rect.top + height3
                                this@EngineActivity.blurredImageView.setRectSquare(rect)
                            } else {
                                if (this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal) {
                                    var width4: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 1.0f)
                                    var height4: Int = (bitmap.getHeight() * 0.5355f)
                                    this@EngineActivity.mTemplate.setDrawingTranslation(this@EngineActivity.blurredImageView.getBtmX(), this@EngineActivity.blurredImageView.getBtmY())
                                    var round3: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                                    var round4: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                                    var i8: Int = width4 + round3
                                    if (i8 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                        round3 -= i8 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                        i8 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    }
                                    var i9: Int = height4 + round4
                                    if (i9 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                        round4 -= i9 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                        i9 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    }
                                    if (round3 < 0) {
                                        round3 = 0
                                    }
                                    if (round4 < 0) {
                                        round4 = 0
                                    }
                                    rect = Rect(round3, round4, i8, i9)
                                    var width5: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                    var height5: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                    cropToSquareWithRoundCorners = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect, width5, height5)
                                    this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                                    rect.right = rect.left + width5
                                    rect.bottom = rect.top + height5
                                    this@EngineActivity.blurredImageView.setRectSquare(rect)
                                    i2 = round3
                                    i = 0
                                    i3 = round4
                                }
                                var width6: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.87530595f)
                                var i10: Int = (width6 * 1.13f)
                                var min: Int = min(width6, i10)
                                this@EngineActivity.mTemplate.setDrawingTranslation(this@EngineActivity.blurredImageView.getBtmX(), this@EngineActivity.blurredImageView.getBtmY())
                                var round5: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                                var round6: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                                var i11: Int = width6 + round5
                                if (i11 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                    round5 -= i11 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    i11 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                }
                                var i12: Int = i10 + round6
                                if (i12 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                    round6 -= i12 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    i12 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                }
                                if (round5 < 0) {
                                    round5 = 0
                                }
                                if (round6 < 0) {
                                    round6 = 0
                                }
                                rect = Rect(round5, round6, i11, i12)
                                if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_CLASSIC.ordinal) {
                                    var width7: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                    var height6: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                    var cropToSquare2: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect, width7, height6)
                                    this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                                    rect.right = rect.left + width7
                                    rect.bottom = rect.top + height6
                                    this@EngineActivity.blurredImageView.setRectSquare(rect)
                                    cropToSquareWithRoundCorners = cropToSquare2
                                    i = 0
                                } else {
                                    i = (min * 0.10800001f)
                                    var width8: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                    var height7: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                    cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect, i, width8, height7)
                                    rect.right = rect.left + width8
                                    rect.bottom = rect.top + height7
                                    this@EngineActivity.blurredImageView.setRectSquare(rect)
                                }
                                i2 = round5
                                i3 = round6
                            }
                            var rect3 = Rect(rect
                            var bitmap2: Bitmap? = cropToSquareWithRoundCorners
                            val engineActivity = this@EngineActivity
                            this@EngineActivity.mTemplate.setUri_bg_ffmpeg(this@EngineActivity.blurredImageView.setupBitmapDraw(UtilsBitmap.blurInSave(engineActivity, bitmap, 20, 1, engineActivity.mTemplate.getWidth(), this@EngineActivity.mTemplate.getHeight()), bitmap2, this@EngineActivity.mTemplate))
                            this@EngineActivity.mTemplate.getSquareBitmapModel().set(this@EngineActivity.blurredImageView.getLeft_square(), this@EngineActivity.blurredImageView.getTop_square(), i2, i3, rect3.width(), rect3.height(), bitmap2.getWidth(), bitmap2.getHeight(), i)
                        }
                        this@EngineActivity.saveTemplate()
                        var intent = Intent(this@EngineActivity, ProgressViewActivity::class.java)
                        intent.putExtra(Common.TEMPLATE, this@EngineActivity.mTemplate.getIdTemplate())
                        intent.addFlags
            this@EngineActivity.startActivity
            this@EngineActivity.overridePendingTransition(0, 0)
                        this@EngineActivity.finish()
                    }
                    var createBitmap: Bitmap? = Bitmap.createBitmap(this@EngineActivity.mTemplate.getWidth(), this@EngineActivity.mTemplate.getHeight(), Bitmap.Config.RGB_565)
                    createBitmap.eraseColor(ViewCompat.MEASURED_STATE_MASK)
                    this@EngineActivity.blurredImageView.updatePosCanvas(this@EngineActivity.mTemplate.getWidth(), this@EngineActivity.mTemplate.getHeight(), createBitmap)
                    this@EngineActivity.blurredImageView.updateIpad(createBitmap, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize())
                    this@EngineActivity.mTemplate.setUri_bg_ffmpeg(this@EngineActivity.blurredImageView.setupBitmapDraw(createBitmap, null, this@EngineActivity.mTemplate))
                    this@EngineActivity.saveTemplate()
                    var intent2 = Intent(this@EngineActivity, ProgressViewActivity::class.java)
                    intent2.putExtra(Common.TEMPLATE, this@EngineActivity.mTemplate.getIdTemplate())
                    intent2.addFlags
            this@EngineActivity.startActivity
            this@EngineActivity.overridePendingTransition(0, 0)
                    this@EngineActivity.finish()
                } catch (Exception e) {
                    Log.e("Tag : ", "init " + e.getMessage())
                }
            }
        })
    }

    fun setupShowFragment(str: String) {
        findViewById(R.id.layout_time).setVisibility(4)
        findViewById(R.id.layout_menu).setVisibility(4)
        if (str != null) {
            this.tv_tittle_fragment.setText(str)
            this.tv_tittle_fragment.setVisibility(0)
            val linearLayout = this.btnChangeResize
            if (linearLayout != null) {
                linearLayout.setVisibility(4)
            }
        }
        this.btn_cancel.setVisibility(4)
        this.btn_export.setVisibility(4)
        this.btn_setup_fps.setVisibility(4)
    }

    fun setupHideFragment() {
        findViewById(R.id.layout_time).setVisibility(0)
        findViewById(R.id.layout_menu).setVisibility(0)
        this.tv_tittle_fragment.setVisibility(8)
        val linearLayout = this.btnChangeResize
        if (linearLayout != null) {
            linearLayout.setVisibility(0)
        }
        this.btn_cancel.setVisibility(0)
        this.btn_export.setVisibility(0)
        this.btn_setup_fps.setVisibility(0)
    }

    fun showEditAudioEntity(entityAudio: EntityAudio) {
        findViewById(R.id.layout_menu).setVisibility(4)
        val beginTransaction = getSupportFragmentManager().beginTransaction()
        this.mCurrentFragment = EditMediaFragment.getInstance(this.iEditMediaCallback, this.mResources, entityAudio, -this.trackViewEntity.getCurrentPosition())
        beginTransaction.replace(R.id.m_container, this.mCurrentFragment)
        beginTransaction.commit()
    }

    fun showEditMultipleEntity(i: Int) {
        if (EditMultipleEntityFragment.instance != null) {
            EditMultipleEntityFragment.instance.setCount_select(i)
            return
        }
        findViewById(R.id.layout_menu).setVisibility(4)
        val beginTransaction = getSupportFragmentManager().beginTransaction()
        this.mCurrentFragment = EditMultipleEntityFragment.getInstance(this.iEditMultipleCallback, this.mResources, i)
        beginTransaction.replace(R.id.m_container, this.mCurrentFragment)
        beginTransaction.commit()
    }

    fun showEditEntity(entity: Entity) {
        val beginTransaction = getSupportFragmentManager().beginTransaction()
        this.mCurrentFragment = EditEntityFragment.getInstance(this.iEditEntityCallback, this.mResources, entity, -this.trackViewEntity.getCurrentPosition())
        beginTransaction.replace(R.id.m_container, this.mCurrentFragment)
        beginTransaction.commit()
    }

    fun showEditTrslEntity(entity: Entity) {
        val beginTransaction = getSupportFragmentManager().beginTransaction()
        this.mCurrentFragment = EditTrslEntityFragment.getInstance(this.iEditTrstEntityCallback, this.mResources, entity, -this.trackViewEntity.getCurrentPosition())
        beginTransaction.replace(R.id.m_container, this.mCurrentFragment)
        beginTransaction.commit()
    }

    fun showEditBismilahEntity(entity: Entity) {
        val beginTransaction = getSupportFragmentManager().beginTransaction()
        this.mCurrentFragment = EditBismilahEntityFragment.getInstance(this.iBismilahEntityCallback, this.mResources, entity, -this.trackViewEntity.getCurrentPosition())
        beginTransaction.replace(R.id.m_container, this.mCurrentFragment)
        beginTransaction.commit()
    }

    private fun saveTemplateTmp() {
        String str
        try {
            if (this.mTemplate == null) {
                this.mTemplate = Template()
            }
            this.mTemplate.setNewCode()
            this.mTemplate.setGlass(this.blurredImageView.isGlass())
            this.mTemplate.setCurrentCursur(this.trackViewEntity.getCurrent_cursur_position())
            this.mTemplate.setScale_timeline(this.trackViewEntity.getScaleFactor())
            this.mTemplate.setGradient(this.blurredImageView.getColor_gradient())
            this.mTemplate.setDuration(this.trackViewEntity.getMaxTime())
            this.mTemplate.setColor_ipad(this.blurredImageView.colorIpad())
            this.mTemplate.getQuranEntityList().clear()
            this.mTemplate.getTranslationTemplateList().clear()
            this.mTemplate.setUri_bg(this.uri_bg)
            try {
                for (entityQuranTimeline in this.trackViewEntity.getEntityListQuran()) {
                    if (entityQuranTimeline.visible()) {
                        var f2: Float = utils.f2(Math.abs(entityQuranTimeline.getRect().left / this.trackViewEntity.getSecond_in_screen()))
                        var f22: Float = utils.f2(Math.abs(entityQuranTimeline.getRect().right / this.trackViewEntity.getSecond_in_screen()))
                        if (entityQuranTimeline.getQuranEntity().getCopyRect() == null) {
                            entityQuranTimeline.getQuranEntity().setCopyRect()
                            if (entityQuranTimeline.getQuranEntity().getCopyRect() == null) {
                            }
                        }
                        val entityQuranTemplate = EntityQuranTemplate(entityQuranTimeline.getTransition(), f2, f22, entityQuranTimeline.getQuranEntity().getCopyRect().left * this.mTemplate.getWidth(), this.mTemplate.getHeight() * entityQuranTimeline.getQuranEntity().getCopyRect().top, entityQuranTimeline.getRect().left / entityQuranTimeline.getmScaleFactor(), entityQuranTimeline.getRect().right / entityQuranTimeline.getmScaleFactor(), entityQuranTimeline.getQuranEntity().getTxt(), entityQuranTimeline.getQuranEntity().getComplete_aya(), entityQuranTimeline.getQuranEntity().getNameFont(), entityQuranTimeline.getQuranEntity().getIndexNumber(), entityQuranTimeline.getQuranEntity().getNumber(), entityQuranTimeline.getQuranEntity().getClrAya(), entityQuranTimeline.getQuranEntity().getPaintTranslationAya() != null ? entityQuranTimeline.getQuranEntity().getClrTrsl() : InputDeviceCompat.SOURCE_ANY, entityQuranTimeline.getQuranEntity().getmPreset())
                        entityQuranTemplate.setHeight((entityQuranTimeline.getQuranEntity().getCopyRect().bottom * this.mTemplate.getHeight()) - (entityQuranTimeline.getQuranEntity().getCopyRect().top * this.mTemplate.getHeight()))
                        entityQuranTemplate.setFactor_size(entityQuranTimeline.getQuranEntity().getFactorSize())
                        entityQuranTemplate.setFactor_sizeTrl(entityQuranTimeline.getQuranEntity().getFactorSizeTrl())
                        entityQuranTemplate.setScale(entityQuranTimeline.getQuranEntity().getFactor_scale())
                        entityQuranTemplate.setTranslation(entityQuranTimeline.getQuranEntity().getTranslation())
                        entityQuranTemplate.setTranslation_complete(entityQuranTimeline.getQuranEntity().getTranslation_complete())
                        entityQuranTemplate.setStartWord_index(entityQuranTimeline.getQuranEntity().getStartWord_index())
                        entityQuranTemplate.setEndWord_index(entityQuranTimeline.getQuranEntity().getEndWord_index())
                        entityQuranTemplate.setIcon(entityQuranTimeline.getQuranEntity().getIcon())
                        entityQuranTemplate.setFile(entityQuranTimeline.getFile())
                        entityQuranTemplate.setFile_in(entityQuranTimeline.getFile_in())
                        entityQuranTemplate.setFile_out(entityQuranTimeline.getFile_out())
                        entityQuranTemplate.setRectF(MRectF(entityQuranTimeline.getQuranEntity().getCopyRect().left, entityQuranTimeline.getQuranEntity().getCopyRect().top, entityQuranTimeline.getQuranEntity().getCopyRect().right, entityQuranTimeline.getQuranEntity().getCopyRect().bottom))
                        this.mTemplate.addQuranEntityList(entityQuranTemplate)
                    }
                }
            } catch (Exception e) {
                Log.e("save templete quran", "" + e.getMessage())
            }
            try {
                for (entityTrslTimeline in this.trackViewEntity.getEntityListTrslQuran()) {
                    if (entityTrslTimeline.visible()) {
                        var f23: Float = utils.f2(Math.abs(entityTrslTimeline.getRect().left / this.trackViewEntity.getSecond_in_screen()))
                        var f24: Float = utils.f2(Math.abs(entityTrslTimeline.getRect().right / this.trackViewEntity.getSecond_in_screen()))
                        if (entityTrslTimeline.getQuranEntity().getCopyRect() == null) {
                            entityTrslTimeline.getQuranEntity().setCopyRect()
                            if (entityTrslTimeline.getQuranEntity().getCopyRect() == null) {
                            }
                        }
                        val entityTranslationTemplate = EntityTranslationTemplate(entityTrslTimeline.getTransition(), f23, f24, entityTrslTimeline.getQuranEntity().getCopyRect().left * this.mTemplate.getWidth(), this.mTemplate.getHeight() * entityTrslTimeline.getQuranEntity().getCopyRect().top, entityTrslTimeline.getRect().left / entityTrslTimeline.getmScaleFactor(), entityTrslTimeline.getRect().right / entityTrslTimeline.getmScaleFactor(), entityTrslTimeline.getQuranEntity().getTxt(), entityTrslTimeline.getQuranEntity().getNameFont(), entityTrslTimeline.getQuranEntity().getNumber(), entityTrslTimeline.getQuranEntity().getClrAya(), entityTrslTimeline.getQuranEntity().getmPreset())
                        entityTranslationTemplate.setHeight((entityTrslTimeline.getQuranEntity().getCopyRect().bottom * this.mTemplate.getHeight()) - (entityTrslTimeline.getQuranEntity().getCopyRect().top * this.mTemplate.getHeight()))
                        entityTranslationTemplate.setFactor_size(entityTrslTimeline.getQuranEntity().getFactorSize())
                        entityTranslationTemplate.setFactor_sizeTrl(entityTrslTimeline.getQuranEntity().getFactorSizeTrl())
                        entityTranslationTemplate.setScale(entityTrslTimeline.getQuranEntity().getFactor_scale())
                        entityTranslationTemplate.setFile(entityTrslTimeline.getFile())
                        entityTranslationTemplate.setFile_in(entityTrslTimeline.getFile_in())
                        entityTranslationTemplate.setFile_out(entityTrslTimeline.getFile_out())
                        entityTranslationTemplate.setClr_bg(entityTrslTimeline.getQuranEntity().getClrBg())
                        entityTranslationTemplate.setHaveBg(entityTrslTimeline.getQuranEntity().isHaveBg())
                        entityTranslationTemplate.setRectF(MRectF(entityTrslTimeline.getQuranEntity().getCopyRect().left, entityTrslTimeline.getQuranEntity().getCopyRect().top, entityTrslTimeline.getQuranEntity().getCopyRect().right, entityTrslTimeline.getQuranEntity().getCopyRect().bottom))
                        this.mTemplate.addTrslEntityList(entityTranslationTemplate)
                    }
                }
            } catch (Exception e2) {
                Log.e("save templete trsl quran", "" + e2.getMessage())
            }
            this.mTemplate.setEntityIsti3adaTemplate(null)
            if (this.blurredImageView.getmIsti3adhaEntity() != null && this.blurredImageView.getmIsti3adhaEntity().getBismilahTimeline().visible()) {
                val bismilahTimeline = this.blurredImageView.getmIsti3adhaEntity().getBismilahTimeline()
                var f25: Float = utils.f2(Math.abs(bismilahTimeline.getRect().left / this.trackViewEntity.getSecond_in_screen()))
                var f26: Float = utils.f2(Math.abs(bismilahTimeline.getRect().right / this.trackViewEntity.getSecond_in_screen()))
                if (bismilahTimeline.getQuranEntity().getCopyRect() == null) {
                    bismilahTimeline.getQuranEntity().setCopyRect()
                }
                val entityBismilahTemplate = EntityBismilahTemplate(bismilahTimeline.getTransition(), f25, f26, bismilahTimeline.getQuranEntity().getCopyRect().left * this.mTemplate.getWidth(), this.mTemplate.getHeight() * bismilahTimeline.getQuranEntity().getCopyRect().top, bismilahTimeline.getRect().left / bismilahTimeline.getmScaleFactor(), bismilahTimeline.getRect().right / bismilahTimeline.getmScaleFactor(), bismilahTimeline.getQuranEntity().getTxt(), bismilahTimeline.getQuranEntity().getClrAya(), bismilahTimeline.getQuranEntity().getmPreset())
                entityBismilahTemplate.setHeight((bismilahTimeline.getQuranEntity().getCopyRect().bottom * this.mTemplate.getHeight()) - (bismilahTimeline.getQuranEntity().getCopyRect().top * this.mTemplate.getHeight()))
                entityBismilahTemplate.setFactor_size(bismilahTimeline.getQuranEntity().getFactorSize())
                entityBismilahTemplate.setScale(bismilahTimeline.getQuranEntity().getFactor_scale())
                entityBismilahTemplate.setFile(bismilahTimeline.getFile())
                entityBismilahTemplate.setFile_in(bismilahTimeline.getFile_in())
                entityBismilahTemplate.setFile_out(bismilahTimeline.getFile_out())
                entityBismilahTemplate.setRectF(MRectF(bismilahTimeline.getQuranEntity().getCopyRect().left, bismilahTimeline.getQuranEntity().getCopyRect().top, bismilahTimeline.getQuranEntity().getCopyRect().right, bismilahTimeline.getQuranEntity().getCopyRect().bottom))
                this.mTemplate.setEntityIsti3adaTemplate(entityBismilahTemplate)
            }
            this.mTemplate.setEntityBismilahTemplate(null)
            if (this.blurredImageView.getBismilahEntity() != null && this.blurredImageView.getBismilahEntity().getBismilahTimeline().visible()) {
                val bismilahTimeline2 = this.blurredImageView.getBismilahEntity().getBismilahTimeline()
                var f27: Float = utils.f2(Math.abs(bismilahTimeline2.getRect().left / this.trackViewEntity.getSecond_in_screen()))
                var f28: Float = utils.f2(Math.abs(bismilahTimeline2.getRect().right / this.trackViewEntity.getSecond_in_screen()))
                if (bismilahTimeline2.getQuranEntity().getCopyRect() == null) {
                    bismilahTimeline2.getQuranEntity().setCopyRect()
                }
                val entityBismilahTemplate2 = EntityBismilahTemplate(bismilahTimeline2.getTransition(), f27, f28, bismilahTimeline2.getQuranEntity().getCopyRect().left * this.mTemplate.getWidth(), this.mTemplate.getHeight() * bismilahTimeline2.getQuranEntity().getCopyRect().top, bismilahTimeline2.getRect().left / bismilahTimeline2.getmScaleFactor(), bismilahTimeline2.getRect().right / bismilahTimeline2.getmScaleFactor(), bismilahTimeline2.getQuranEntity().getTxt(), bismilahTimeline2.getQuranEntity().getClrAya(), bismilahTimeline2.getQuranEntity().getmPreset())
                entityBismilahTemplate2.setHeight((bismilahTimeline2.getQuranEntity().getCopyRect().bottom * this.mTemplate.getHeight()) - (bismilahTimeline2.getQuranEntity().getCopyRect().top * this.mTemplate.getHeight()))
                entityBismilahTemplate2.setFactor_size(bismilahTimeline2.getQuranEntity().getFactorSize())
                entityBismilahTemplate2.setScale(bismilahTimeline2.getQuranEntity().getFactor_scale())
                entityBismilahTemplate2.setFile(bismilahTimeline2.getFile())
                entityBismilahTemplate2.setFile_in(bismilahTimeline2.getFile_in())
                entityBismilahTemplate2.setFile_out(bismilahTimeline2.getFile_out())
                entityBismilahTemplate2.setRectF(MRectF(bismilahTimeline2.getQuranEntity().getCopyRect().left, bismilahTimeline2.getQuranEntity().getCopyRect().top, bismilahTimeline2.getQuranEntity().getCopyRect().right, bismilahTimeline2.getQuranEntity().getCopyRect().bottom))
                this.mTemplate.setEntityBismilahTemplate(entityBismilahTemplate2)
            }
            if (this.blurredImageView.getSurahNameEntity() == null) {
                str = ""
            } else if (this.mTemplate.getEntitySurahTemplate() == null) {
                str = ""
                this.mTemplate.setEntitySurahTemplate(EntitySurahTemplate(this.blurredImageView.getSurahNameEntity().getName(), this.blurredImageView.getSurahNameEntity().getReader(), this.mTemplate.getmDrawingTranslationX() + this.blurredImageView.getRectFSurahName().left, this.mTemplate.getmDrawingTranslationY() + this.blurredImageView.getRectFSurahName().top, MRectF(this.blurredImageView.getSurahNameEntity().getCopyRect().left, this.blurredImageView.getSurahNameEntity().getCopyRect().top, this.blurredImageView.getSurahNameEntity().getCopyRect().right, this.blurredImageView.getSurahNameEntity().getCopyRect().bottom), this.blurredImageView.getSurahNameEntity().getFactor_scale(), this.blurredImageView.getSurahNameEntity().getNameFont(), this.blurredImageView.getSurahNameEntity().getClrS_name(), this.blurredImageView.getSurahNameEntity().getmPreset(), this.blurredImageView.getSurahNameEntity().getStyle(), this.blurredImageView.getSurahNameEntity().getIndex_surah(), this.blurredImageView.getSurahNameEntity().isHaveBg(), this.blurredImageView.getSurahNameEntity().getClrBg()))
            } else {
                str = ""
                this.mTemplate.getEntitySurahTemplate().setClrBg(this.blurredImageView.getSurahNameEntity().getClrBg())
                this.mTemplate.getEntitySurahTemplate().setHaveBg(this.blurredImageView.getSurahNameEntity().isHaveBg())
                this.mTemplate.getEntitySurahTemplate().setIndex_surah(this.blurredImageView.getSurahNameEntity().getIndex_surah())
                this.mTemplate.getEntitySurahTemplate().setStyle(this.blurredImageView.getSurahNameEntity().getStyle())
                this.mTemplate.getEntitySurahTemplate().setClr(this.blurredImageView.getSurahNameEntity().getClrS_name())
                this.mTemplate.getEntitySurahTemplate().setPreset(this.blurredImageView.getSurahNameEntity().getmPreset())
                this.mTemplate.getEntitySurahTemplate().setName_font(this.blurredImageView.getSurahNameEntity().getNameFont())
                this.mTemplate.getEntitySurahTemplate().setFactor_scale(this.blurredImageView.getSurahNameEntity().getFactor_scale())
                this.mTemplate.getEntitySurahTemplate().setRectF(MRectF(this.blurredImageView.getSurahNameEntity().getCopyRect().left, this.blurredImageView.getSurahNameEntity().getCopyRect().top, this.blurredImageView.getSurahNameEntity().getCopyRect().right, this.blurredImageView.getSurahNameEntity().getCopyRect().bottom))
                this.mTemplate.getEntitySurahTemplate().setName(this.blurredImageView.getSurahNameEntity().getName())
                this.mTemplate.getEntitySurahTemplate().setReader(this.blurredImageView.getSurahNameEntity().getReader())
                this.mTemplate.getEntitySurahTemplate().setPos(this.blurredImageView.getRectFSurahName().left + this.mTemplate.getmDrawingTranslationX(), this.blurredImageView.getRectFSurahName().top + this.mTemplate.getmDrawingTranslationY())
            }
            if (this.mTemplate.getEntityProgressTemplate() == null) {
                this.mTemplate.setEntityProgressTemplate(EntityProgressTemplate(utils.f2(this.blurredImageView.getRectFProgress().left + this.mTemplate.getmDrawingTranslationX()), utils.f2(this.blurredImageView.getRectFProgress().top + this.mTemplate.getmDrawingTranslationY())))
            } else {
                this.mTemplate.getEntityProgressTemplate().setLeft(utils.f2(this.blurredImageView.getRectFProgress().left + this.mTemplate.getmDrawingTranslationX()))
                this.mTemplate.getEntityProgressTemplate().setTop(utils.f2(this.blurredImageView.getRectFProgress().top + this.mTemplate.getmDrawingTranslationY()))
            }
            this.mTemplate.getEntityMediaList().clear()
            for (entityAudio in this.trackViewEntity.getEntityListAudio()) {
                if (entityAudio.visible() && entityAudio.getEnd() > entityAudio.getStart()) {
                    val entityMedia = EntityMedia(entityAudio.getUri().toString(), entityAudio.getMin_duration(), entityAudio.getStart(), entityAudio.getEnd(), entityAudio.getRect().left / this.trackViewEntity.getScaleFactor(), entityAudio.getRect().right / this.trackViewEntity.getScaleFactor(), Math.round(entityAudio.getEnd() - entityAudio.getStart()), entityAudio.getOffset(), entityAudio.getOffset_right(), entityAudio.getOffset_left(), entityAudio.getMax(), entityAudio.getFade_in(), entityAudio.getFade_out(), (entityAudio.getRect().left / this.trackViewEntity.getSecond_in_screen()) * 1000.0f)
                    entityMedia.setPaths_https(entityAudio.getPaths_http())
                    entityMedia.setEffectAudio(entityAudio.getEffectAudio())
                    entityMedia.setPath_ffmpeg(entityAudio.getPath_ffmpeg())
                    entityMedia.setPath_ffmpeg_effect(entityAudio.getPath_ffmpeg_effect())
                    entityMedia.setVideo_path(entityAudio.getVideo_path())
                    entityMedia.setApplyEffectInPreview(entityAudio.isApplyEffectInPreview())
                    this.mTemplate.addMedia(entityMedia)
                }
            }
            this.mTemplate.setUri_video(FileHelper(this).createPublicVideoFolder(this.mResources.getString(R.string.app_name)).getAbsolutePath() + "/" + System.currentTimeMillis() + "_NurMontage.mp4")
            LocalPersistence.writeTemplate(this, this.mTemplate, str, Common.TEMPLATE_TMP)
        } catch (Exception unused) {
        }
    }

    fun saveTemplate() {
        Iterator<EntityAudio> it
        val engineActivity = this
        try {
            if (engineActivity.mTemplate == null) {
                engineActivity.mTemplate = Template()
            }
            engineActivity.mTemplate.setNewCode()
            engineActivity.mTemplate.setGlass(engineActivity.blurredImageView.isGlass())
            engineActivity.mTemplate.setCurrentCursur(engineActivity.trackViewEntity.getCurrent_cursur_position())
            engineActivity.mTemplate.setScale_timeline(engineActivity.trackViewEntity.getScaleFactor())
            engineActivity.mTemplate.setDuration(engineActivity.trackViewEntity.getMaxTime())
            engineActivity.mTemplate.setGradient(engineActivity.blurredImageView.getColor_gradient())
            engineActivity.mTemplate.setColor_ipad(engineActivity.blurredImageView.colorIpad())
            engineActivity.mTemplate.getQuranEntityList().clear()
            engineActivity.mTemplate.getTranslationTemplateList().clear()
            engineActivity.mTemplate.setUri_bg(engineActivity.uri_bg)
            try {
                for (entityQuranTimeline in engineActivity.trackViewEntity.getEntityListQuran()) {
                    if (entityQuranTimeline.visible()) {
                        var f2: Float = utils.f2(Math.abs(entityQuranTimeline.getRect().left / engineActivity.trackViewEntity.getSecond_in_screen()))
                        var f22: Float = utils.f2(Math.abs(entityQuranTimeline.getRect().right / engineActivity.trackViewEntity.getSecond_in_screen()))
                        if (entityQuranTimeline.getQuranEntity().getCopyRect() == null) {
                            entityQuranTimeline.getQuranEntity().setCopyRect()
                            if (entityQuranTimeline.getQuranEntity().getCopyRect() == null) {
                            }
                        }
                        val entityQuranTemplate = EntityQuranTemplate(entityQuranTimeline.getTransition(), f2, f22, entityQuranTimeline.getQuranEntity().getCopyRect().left * engineActivity.mTemplate.getWidth(), engineActivity.mTemplate.getHeight() * entityQuranTimeline.getQuranEntity().getCopyRect().top, entityQuranTimeline.getRect().left / entityQuranTimeline.getmScaleFactor(), entityQuranTimeline.getRect().right / entityQuranTimeline.getmScaleFactor(), entityQuranTimeline.getQuranEntity().getTxt(), entityQuranTimeline.getQuranEntity().getComplete_aya(), entityQuranTimeline.getQuranEntity().getNameFont(), entityQuranTimeline.getQuranEntity().getIndexNumber(), entityQuranTimeline.getQuranEntity().getNumber(), entityQuranTimeline.getQuranEntity().getClrAya(), entityQuranTimeline.getQuranEntity().getPaintTranslationAya() != null ? entityQuranTimeline.getQuranEntity().getClrTrsl() : InputDeviceCompat.SOURCE_ANY, entityQuranTimeline.getQuranEntity().getmPreset())
                        entityQuranTemplate.setHeight((entityQuranTimeline.getQuranEntity().getCopyRect().bottom * engineActivity.mTemplate.getHeight()) - (entityQuranTimeline.getQuranEntity().getCopyRect().top * engineActivity.mTemplate.getHeight()))
                        entityQuranTemplate.setFactor_size(entityQuranTimeline.getQuranEntity().getFactorSize())
                        entityQuranTemplate.setFactor_sizeTrl(entityQuranTimeline.getQuranEntity().getFactorSizeTrl())
                        entityQuranTemplate.setScale(entityQuranTimeline.getQuranEntity().getFactor_scale())
                        entityQuranTemplate.setTranslation(entityQuranTimeline.getQuranEntity().getTranslation())
                        entityQuranTemplate.setTranslation_complete(entityQuranTimeline.getQuranEntity().getTranslation_complete())
                        entityQuranTemplate.setStartWord_index(entityQuranTimeline.getQuranEntity().getStartWord_index())
                        entityQuranTemplate.setEndWord_index(entityQuranTimeline.getQuranEntity().getEndWord_index())
                        entityQuranTemplate.setIcon(entityQuranTimeline.getQuranEntity().getIcon())
                        entityQuranTemplate.setFile(entityQuranTimeline.getFile())
                        entityQuranTemplate.setFile_in(entityQuranTimeline.getFile_in())
                        entityQuranTemplate.setFile_out(entityQuranTimeline.getFile_out())
                        entityQuranTemplate.setRectF(MRectF(entityQuranTimeline.getQuranEntity().getCopyRect().left, entityQuranTimeline.getQuranEntity().getCopyRect().top, entityQuranTimeline.getQuranEntity().getCopyRect().right, entityQuranTimeline.getQuranEntity().getCopyRect().bottom))
                        engineActivity.mTemplate.addQuranEntityList(entityQuranTemplate)
                    }
                }
            } catch (Exception e) {
                Log.e("save templete quran", "" + e.getMessage())
            }
            try {
                for (entityTrslTimeline in engineActivity.trackViewEntity.getEntityListTrslQuran()) {
                    if (entityTrslTimeline.visible()) {
                        var f23: Float = utils.f2(Math.abs(entityTrslTimeline.getRect().left / engineActivity.trackViewEntity.getSecond_in_screen()))
                        var f24: Float = utils.f2(Math.abs(entityTrslTimeline.getRect().right / engineActivity.trackViewEntity.getSecond_in_screen()))
                        if (entityTrslTimeline.getQuranEntity().getCopyRect() == null) {
                            entityTrslTimeline.getQuranEntity().setCopyRect()
                            if (entityTrslTimeline.getQuranEntity().getCopyRect() == null) {
                            }
                        }
                        val entityTranslationTemplate = EntityTranslationTemplate(entityTrslTimeline.getTransition(), f23, f24, entityTrslTimeline.getQuranEntity().getCopyRect().left * engineActivity.mTemplate.getWidth(), engineActivity.mTemplate.getHeight() * entityTrslTimeline.getQuranEntity().getCopyRect().top, entityTrslTimeline.getRect().left / entityTrslTimeline.getmScaleFactor(), entityTrslTimeline.getRect().right / entityTrslTimeline.getmScaleFactor(), entityTrslTimeline.getQuranEntity().getTxt(), entityTrslTimeline.getQuranEntity().getNameFont(), entityTrslTimeline.getQuranEntity().getNumber(), entityTrslTimeline.getQuranEntity().getClrAya(), entityTrslTimeline.getQuranEntity().getmPreset())
                        entityTranslationTemplate.setHeight((entityTrslTimeline.getQuranEntity().getCopyRect().bottom * engineActivity.mTemplate.getHeight()) - (entityTrslTimeline.getQuranEntity().getCopyRect().top * engineActivity.mTemplate.getHeight()))
                        entityTranslationTemplate.setFactor_size(entityTrslTimeline.getQuranEntity().getFactorSize())
                        entityTranslationTemplate.setFactor_sizeTrl(entityTrslTimeline.getQuranEntity().getFactorSizeTrl())
                        entityTranslationTemplate.setScale(entityTrslTimeline.getQuranEntity().getFactor_scale())
                        entityTranslationTemplate.setFile(entityTrslTimeline.getFile())
                        entityTranslationTemplate.setFile_in(entityTrslTimeline.getFile_in())
                        entityTranslationTemplate.setFile_out(entityTrslTimeline.getFile_out())
                        entityTranslationTemplate.setClr_bg(entityTrslTimeline.getQuranEntity().getClrBg())
                        entityTranslationTemplate.setHaveBg(entityTrslTimeline.getQuranEntity().isHaveBg())
                        entityTranslationTemplate.setRectF(MRectF(entityTrslTimeline.getQuranEntity().getCopyRect().left, entityTrslTimeline.getQuranEntity().getCopyRect().top, entityTrslTimeline.getQuranEntity().getCopyRect().right, entityTrslTimeline.getQuranEntity().getCopyRect().bottom))
                        engineActivity.mTemplate.addTrslEntityList(entityTranslationTemplate)
                    }
                }
            } catch (Exception e2) {
                Log.e("save templete trsl quran", "" + e2.getMessage())
            }
            engineActivity.mTemplate.setEntityIsti3adaTemplate(null)
            if (engineActivity.blurredImageView.getmIsti3adhaEntity() != null && engineActivity.blurredImageView.getmIsti3adhaEntity().getBismilahTimeline().visible()) {
                val bismilahTimeline = engineActivity.blurredImageView.getmIsti3adhaEntity().getBismilahTimeline()
                var f25: Float = utils.f2(Math.abs(bismilahTimeline.getRect().left / engineActivity.trackViewEntity.getSecond_in_screen()))
                var f26: Float = utils.f2(Math.abs(bismilahTimeline.getRect().right / engineActivity.trackViewEntity.getSecond_in_screen()))
                if (bismilahTimeline.getQuranEntity().getCopyRect() == null) {
                    bismilahTimeline.getQuranEntity().setCopyRect()
                }
                val entityBismilahTemplate = EntityBismilahTemplate(bismilahTimeline.getTransition(), f25, f26, bismilahTimeline.getQuranEntity().getCopyRect().left * engineActivity.mTemplate.getWidth(), engineActivity.mTemplate.getHeight() * bismilahTimeline.getQuranEntity().getCopyRect().top, bismilahTimeline.getRect().left / bismilahTimeline.getmScaleFactor(), bismilahTimeline.getRect().right / bismilahTimeline.getmScaleFactor(), bismilahTimeline.getQuranEntity().getTxt(), bismilahTimeline.getQuranEntity().getClrAya(), bismilahTimeline.getQuranEntity().getmPreset())
                entityBismilahTemplate.setHeight((bismilahTimeline.getQuranEntity().getCopyRect().bottom * engineActivity.mTemplate.getHeight()) - (bismilahTimeline.getQuranEntity().getCopyRect().top * engineActivity.mTemplate.getHeight()))
                entityBismilahTemplate.setFactor_size(bismilahTimeline.getQuranEntity().getFactorSize())
                entityBismilahTemplate.setScale(bismilahTimeline.getQuranEntity().getFactor_scale())
                entityBismilahTemplate.setFile(bismilahTimeline.getFile())
                entityBismilahTemplate.setFile_in(bismilahTimeline.getFile_in())
                entityBismilahTemplate.setFile_out(bismilahTimeline.getFile_out())
                entityBismilahTemplate.setRectF(MRectF(bismilahTimeline.getQuranEntity().getCopyRect().left, bismilahTimeline.getQuranEntity().getCopyRect().top, bismilahTimeline.getQuranEntity().getCopyRect().right, bismilahTimeline.getQuranEntity().getCopyRect().bottom))
                engineActivity.mTemplate.setEntityIsti3adaTemplate(entityBismilahTemplate)
            }
            engineActivity.mTemplate.setEntityBismilahTemplate(null)
            if (engineActivity.blurredImageView.getBismilahEntity() != null && engineActivity.blurredImageView.getBismilahEntity().getBismilahTimeline().visible()) {
                val bismilahTimeline2 = engineActivity.blurredImageView.getBismilahEntity().getBismilahTimeline()
                var f27: Float = utils.f2(Math.abs(bismilahTimeline2.getRect().left / engineActivity.trackViewEntity.getSecond_in_screen()))
                var f28: Float = utils.f2(Math.abs(bismilahTimeline2.getRect().right / engineActivity.trackViewEntity.getSecond_in_screen()))
                if (bismilahTimeline2.getQuranEntity().getCopyRect() == null) {
                    bismilahTimeline2.getQuranEntity().setCopyRect()
                }
                val entityBismilahTemplate2 = EntityBismilahTemplate(bismilahTimeline2.getTransition(), f27, f28, bismilahTimeline2.getQuranEntity().getCopyRect().left * engineActivity.mTemplate.getWidth(), engineActivity.mTemplate.getHeight() * bismilahTimeline2.getQuranEntity().getCopyRect().top, bismilahTimeline2.getRect().left / bismilahTimeline2.getmScaleFactor(), bismilahTimeline2.getRect().right / bismilahTimeline2.getmScaleFactor(), bismilahTimeline2.getQuranEntity().getTxt(), bismilahTimeline2.getQuranEntity().getClrAya(), bismilahTimeline2.getQuranEntity().getmPreset())
                entityBismilahTemplate2.setHeight((bismilahTimeline2.getQuranEntity().getCopyRect().bottom * engineActivity.mTemplate.getHeight()) - (bismilahTimeline2.getQuranEntity().getCopyRect().top * engineActivity.mTemplate.getHeight()))
                entityBismilahTemplate2.setFactor_size(bismilahTimeline2.getQuranEntity().getFactorSize())
                entityBismilahTemplate2.setScale(bismilahTimeline2.getQuranEntity().getFactor_scale())
                entityBismilahTemplate2.setFile(bismilahTimeline2.getFile())
                entityBismilahTemplate2.setFile_in(bismilahTimeline2.getFile_in())
                entityBismilahTemplate2.setFile_out(bismilahTimeline2.getFile_out())
                entityBismilahTemplate2.setRectF(MRectF(bismilahTimeline2.getQuranEntity().getCopyRect().left, bismilahTimeline2.getQuranEntity().getCopyRect().top, bismilahTimeline2.getQuranEntity().getCopyRect().right, bismilahTimeline2.getQuranEntity().getCopyRect().bottom))
                engineActivity.mTemplate.setEntityBismilahTemplate(entityBismilahTemplate2)
            }
            if (engineActivity.blurredImageView.getSurahNameEntity() != null) {
                if (engineActivity.mTemplate.getEntitySurahTemplate() == null) {
                    try {
                        if (engineActivity.blurredImageView.getSurahNameEntity().getCopyRect() == null) {
                            engineActivity.blurredImageView.getSurahNameEntity().setCopyRect()
                        }
                        engineActivity.mTemplate.setEntitySurahTemplate(EntitySurahTemplate(engineActivity.blurredImageView.getSurahNameEntity().getName(), engineActivity.blurredImageView.getSurahNameEntity().getReader(), engineActivity.mTemplate.getmDrawingTranslationX() + engineActivity.blurredImageView.getRectFSurahName().left, engineActivity.mTemplate.getmDrawingTranslationY() + engineActivity.blurredImageView.getRectFSurahName().top, MRectF(engineActivity.blurredImageView.getSurahNameEntity().getCopyRect().left, engineActivity.blurredImageView.getSurahNameEntity().getCopyRect().top, engineActivity.blurredImageView.getSurahNameEntity().getCopyRect().right, engineActivity.blurredImageView.getSurahNameEntity().getCopyRect().bottom), engineActivity.blurredImageView.getSurahNameEntity().getFactor_scale(), engineActivity.blurredImageView.getSurahNameEntity().getNameFont(), engineActivity.blurredImageView.getSurahNameEntity().getClrS_name(), engineActivity.blurredImageView.getSurahNameEntity().getmPreset(), engineActivity.blurredImageView.getSurahNameEntity().getStyle(), engineActivity.blurredImageView.getSurahNameEntity().getIndex_surah(), engineActivity.blurredImageView.getSurahNameEntity().isHaveBg(), engineActivity.blurredImageView.getSurahNameEntity().getClrBg()))
                        engineActivity = this
                    } catch (Exception e3) {
                        e = e3
                        e.printStackTrace()
                    }
                } else {
                    engineActivity.mTemplate.getEntitySurahTemplate().setClrBg(engineActivity.blurredImageView.getSurahNameEntity().getClrBg())
                    engineActivity.mTemplate.getEntitySurahTemplate().setHaveBg(engineActivity.blurredImageView.getSurahNameEntity().isHaveBg())
                    engineActivity.mTemplate.getEntitySurahTemplate().setIndex_surah(engineActivity.blurredImageView.getSurahNameEntity().getIndex_surah())
                    engineActivity.mTemplate.getEntitySurahTemplate().setStyle(engineActivity.blurredImageView.getSurahNameEntity().getStyle())
                    engineActivity.mTemplate.getEntitySurahTemplate().setClr(engineActivity.blurredImageView.getSurahNameEntity().getClrS_name())
                    engineActivity.mTemplate.getEntitySurahTemplate().setPreset(engineActivity.blurredImageView.getSurahNameEntity().getmPreset())
                    engineActivity.mTemplate.getEntitySurahTemplate().setName_font(engineActivity.blurredImageView.getSurahNameEntity().getNameFont())
                    engineActivity.mTemplate.getEntitySurahTemplate().setFactor_scale(engineActivity.blurredImageView.getSurahNameEntity().getFactor_scale())
                    engineActivity.mTemplate.getEntitySurahTemplate().setRectF(MRectF(engineActivity.blurredImageView.getSurahNameEntity().getCopyRect().left, engineActivity.blurredImageView.getSurahNameEntity().getCopyRect().top, engineActivity.blurredImageView.getSurahNameEntity().getCopyRect().right, engineActivity.blurredImageView.getSurahNameEntity().getCopyRect().bottom))
                    engineActivity.mTemplate.getEntitySurahTemplate().setName(engineActivity.blurredImageView.getSurahNameEntity().getName())
                    engineActivity.mTemplate.getEntitySurahTemplate().setReader(engineActivity.blurredImageView.getSurahNameEntity().getReader())
                    engineActivity.mTemplate.getEntitySurahTemplate().setPos(engineActivity.blurredImageView.getRectFSurahName().left + engineActivity.mTemplate.getmDrawingTranslationX(), engineActivity.blurredImageView.getRectFSurahName().top + engineActivity.mTemplate.getmDrawingTranslationY())
                }
            }
            if (engineActivity.mTemplate.getEntityProgressTemplate() == null) {
                engineActivity.mTemplate.setEntityProgressTemplate(EntityProgressTemplate(utils.f2(engineActivity.blurredImageView.getRectFProgress().left + engineActivity.mTemplate.getmDrawingTranslationX()), utils.f2(engineActivity.blurredImageView.getRectFProgress().top + engineActivity.mTemplate.getmDrawingTranslationY())))
            } else {
                engineActivity.mTemplate.getEntityProgressTemplate().setLeft(utils.f2(engineActivity.blurredImageView.getRectFProgress().left + engineActivity.mTemplate.getmDrawingTranslationX()))
                engineActivity.mTemplate.getEntityProgressTemplate().setTop(utils.f2(engineActivity.blurredImageView.getRectFProgress().top + engineActivity.mTemplate.getmDrawingTranslationY()))
            }
            engineActivity.mTemplate.getEntityMediaList().clear()
            val it2 = engineActivity.trackViewEntity.getEntityListAudio().iterator()
            while (it2.hasNext()) {
                var next: EntityAudio? = it2.next()
                if (!next.visible()) {
                    it = it2
                } else if (next.getEnd() > next.getStart()) {
                    it = it2
                    val entityMedia = EntityMedia(next.getUri().toString(), next.getMin_duration(), next.getStart(), next.getEnd(), next.getRect().left / engineActivity.trackViewEntity.getScaleFactor(), next.getRect().right / engineActivity.trackViewEntity.getScaleFactor(), Math.round(next.getEnd() - next.getStart()), next.getOffset(), next.getOffset_right(), next.getOffset_left(), next.getMax(), next.getFade_in(), next.getFade_out(), (next.getRect().left / engineActivity.trackViewEntity.getSecond_in_screen()) * 1000.0f)
                    entityMedia.setPaths_https(next.getPaths_http())
                    entityMedia.setEffectAudio(next.getEffectAudio())
                    entityMedia.setPath_ffmpeg(next.getPath_ffmpeg())
                    entityMedia.setVideo_path(next.getVideo_path())
                    entityMedia.setPath_ffmpeg_effect(next.getPath_ffmpeg_effect())
                    entityMedia.setApplyEffectInPreview(next.isApplyEffectInPreview())
                    engineActivity.mTemplate.addMedia(entityMedia)
                    next.release()
                }
                it2 = it
            }
            var str: String? = "Template_" + System.currentTimeMillis()
            var idTemplate: String? = engineActivity.mTemplate.getIdTemplate()
            engineActivity.mTemplate.setIdTemplate(str)
            engineActivity.mTemplate.setUri_video(FileHelper(engineActivity).createPublicVideoFolder(engineActivity.mResources.getString(R.string.app_name)).getAbsolutePath() + "/" + System.currentTimeMillis() + "_NurMontage.mp4")
            val template = engineActivity.mTemplate
            LocalPersistence.writeTemplate(engineActivity, template, idTemplate, template.getIdTemplate())
            LocalPersistence.deleteTemplate(engineActivity, Common.TEMPLATE_TMP)
        } catch (Exception e4) {
            e = e4
            e.printStackTrace()
        }
    }

    fun checkPermissionAudio(): Boolean() {
        if (Build.VERSION.SDK_INT < 33 || ActivityCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_AUDIO") == 0) {
            return true
        }
        ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_MEDIA_AUDIO"), 2)
        return false
    }

    fun pickAudio() {
        try {
            var intent = Intent("android.intent.action.OPEN_DOCUMENT")
            intent.addCategory("android.intent.category.OPENABLE")
            intent.setType("audio/*")
            this.activityLauncher.launch(intent)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(i: Int, strArr: Array<String>, iArr: IntArray) {
        super.onRequestPermissionsResult(i, strArr, iArr)
        if (i == 1) {
            if (iArr.length > 0 && iArr[0] == 0) {
                save()
            } else {
                Toast.makeText(this, this.mResources.getString(R.string.permission_img), 0).show()
            }
        }
        if (i == 2) {
            if (iArr.length > 0 && iArr[0] == 0) {
                pickAudio()
            } else {
                Toast.makeText(this, this.mResources.getString(R.string.permission_audio), 0).show()
            }
        }
        if (i == 10) {
            if ((Build.VERSION.SDK_INT >= 34 && ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == 0) || (iArr.length > 0 && iArr[0] == 0)) {
                imageChooser()
            } else {
                Toast.makeText(this, this.mResources.getString(R.string.permission_img), 0).show()
            }
        }
        if (i == 11) {
            if ((Build.VERSION.SDK_INT >= 34 && ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == 0) || (iArr.length > 0 && iArr[0] == 0)) {
                videoChooser()
            } else {
                Toast.makeText(this, this.mResources.getString(R.string.permission_video), 0).show()
            }
        }
        if (i == 12) {
            if ((Build.VERSION.SDK_INT >= 34 && ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == 0) || (iArr.length > 0 && iArr[0] == 0)) {
                videoChooserForAudio()
            } else {
                Toast.makeText(this, this.mResources.getString(R.string.permission_video), 0).show()
            }
        }
    }

    fun startTimelineAnimation() {
        this.entityAudio_visible = null
        this.entityAudio_player = null
        this.lastIndexVisible = 0
        var maxTime: Int = this.trackViewEntity.getMaxTime()
        var timeLineW: Float = this.trackViewEntity.getTimeLineW()
        this.timeFormatter = TimeFormatter(maxTime)
        val smoothTimelineAnimator = object : SmoothTimelineAnimator(this.startCursur, maxTime, object : SmoothTimelineAnimator.AnimatorListener {
            override fun onUpdate(i: Int) {
                if (!this@EngineActivity.mIsPlaying || i == 0) {
                    return
                }
                var f: Float = i / maxTime
                if (this@EngineActivity.blurredImageView != null) {
                    this@EngineActivity.updateTime(round)
            this@EngineActivity.blurredImageView.setProgress(f)
                }
                this@EngineActivity.trackViewEntity.updateCursur(f * timeLineW)
                this@EngineActivity.trackViewEntity.setCurrent_cursur_position(i)
                var abs: Float = Math.abs(Math.round((this@EngineActivity.trackViewEntity.getCurrentPosition() / this@EngineActivity.trackViewEntity.getSecond_in_screen()) * 1000.0f))
                if (abs > this@EngineActivity.endTimeAudioVisible) {
                    this@EngineActivity.entityAudio_visible = null
                }
                if (this@EngineActivity.entityAudio_visible == null) {
                    for (int i2 = this@EngineActivity.lastIndexVisible; i2 < this@EngineActivity.trackViewEntity.getEntityListAudio().size; i2++) {
                        var entityAudio: EntityAudio? = this@EngineActivity.trackViewEntity.getEntityListAudio()[]
                        if (entityAudio.visible() && entityAudio.isVisible()) {
                            this@EngineActivity.entityAudio_visible = entityAudio
                            val engineActivity = this@EngineActivity
                            engineActivity.endTimeAudioVisible = Math.round((engineActivity.entityAudio_visible.getRect().right / this@EngineActivity.trackViewEntity.getSecond_in_screen()) * 1000.0f)
                            this@EngineActivity.lastIndexVisible = i2
                            break
                        }
                    }
                }
                try {
                    if (this@EngineActivity.entityAudio_visible != null) {
                        if (this@EngineActivity.entityAudio_player != this@EngineActivity.entityAudio_visible && this@EngineActivity.mPlayer != null && this@EngineActivity.mPlayer.isPlaying()) {
                            this@EngineActivity.mPlayer.pause()
                        }
                        val engineActivity2 = this@EngineActivity
                        engineActivity2.mPlayer = engineActivity2.entityAudio_visible.getMediaPlayer()
                        if (this@EngineActivity.mPlayer != null && !this@EngineActivity.mPlayer.isPlaying()) {
                            val engineActivity3 = this@EngineActivity
                            engineActivity3.entityAudio_player = engineActivity3.entityAudio_visible
                            var abs2: Int = ((abs - Math.abs(Math.round((this@EngineActivity.entityAudio_visible.getRect().left / this@EngineActivity.trackViewEntity.getSecond_in_screen()) * 1000.0f))) + this@EngineActivity.entityAudio_visible.getStart())
                            if (abs2 <= this@EngineActivity.mPlayer.getDuration()) {
                                this@EngineActivity.mPlayer.seekTo(abs2)
                            }
                            Log.e("data", "" + this@EngineActivity.mPlayer.getCurrentPosition())
                            this@EngineActivity.mPlayer.start()
                            Log.e("mPlayer c ", "" + this@EngineActivity.mPlayer.isPlaying())
                        }
                    } else if (this@EngineActivity.mPlayer != null && this@EngineActivity.mPlayer.isPlaying()) {
                        this@EngineActivity.mPlayer.pause()
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
                val engineActivity4 = this@EngineActivity
                engineActivity4.updateStartViewTime(engineActivity4.trackViewEntity.getCurrent_cursur_position())
                this@EngineActivity.updateBtnCutState()
            }

            override fun onEnd() {
                if (this@EngineActivity.mIsPlaying) {
                    this@EngineActivity.mIsPlaying = false
                    this@EngineActivity.trackViewEntity.setPlaying(this@EngineActivity.mIsPlaying)
                    this@EngineActivity.blurredImageView.setPlaying(this@EngineActivity.mIsPlaying)
                    this@EngineActivity.stop()
                    this@EngineActivity.trackViewEntity.setCurrent_cursur_position(this@EngineActivity.trackViewEntity.getMaxTime())
                    this@EngineActivity.trackViewEntity.updateCursur(this@EngineActivity.trackViewEntity.getMaxTime())
                    try {
                        if (this@EngineActivity.entityAudio_visible != null && this@EngineActivity.entityAudio_visible.getMediaPlayer() != null && this@EngineActivity.entityAudio_visible.getMediaPlayer().isPlaying()) {
                            this@EngineActivity.entityAudio_visible.getMediaPlayer().pause()
                        }
                        if (this@EngineActivity.mPlayer != null && this@EngineActivity.mPlayer.isPlaying()) {
                            this@EngineActivity.mPlayer.pause()
                        }
                    } catch (Exception e) {
                        e.printStackTrace()
                    }
                    this@EngineActivity.startCursur = 0
                    this@EngineActivity.current_position_time = 0
                    if (this@EngineActivity.btnPlayPause != null) {
                        this@EngineActivity.btnPlayPause.setImageResource(R.drawable.play_btn)
                    }
                    this@EngineActivity.updateBtnToEnd()
                    this@EngineActivity.updateBtnToStart()
                }
            }
        })
        this.valueAnimator = smoothTimelineAnimator
        smoothTimelineAnimator.start()
        if (this.mTemplate.isVideoSquare()) {
            start()
        }
    }

    fun startTimelineAnimationPreview(entityAudio: EntityAudio) {
        var maxTime: Int = this.trackViewEntity.getMaxTime()
        var timeLineW: Float = this.trackViewEntity.getTimeLineW()
        this.timeFormatter = TimeFormatter(maxTime)
        val smoothTimelineAnimator = object : SmoothTimelineAnimator(this.startCursur, maxTime, object : SmoothTimelineAnimator.AnimatorListener {
            override fun onUpdate(i: Int) {
                if (!this@EngineActivity.mIsPlaying || i == 0) {
                    return
                }
                var f: Float = i / maxTime
                if (this@EngineActivity.blurredImageView != null) {
                    this@EngineActivity.updateTime(round)
            this@EngineActivity.blurredImageView.setProgress(f)
                }
                this@EngineActivity.trackViewEntity.updateCursur(f * timeLineW)
                this@EngineActivity.trackViewEntity.setCurrent_cursur_position(i)
                try {
                    if (entityAudio.getMediaPlayer() != null && !entityAudio.getMediaPlayer().isPlaying()) {
                        var abs: Int = ((Math.abs(Math.round((this@EngineActivity.trackViewEntity.getCurrentPosition() / this@EngineActivity.trackViewEntity.getSecond_in_screen()) * 1000.0f)) - Math.abs(Math.round((entityAudio.getRect().left / this@EngineActivity.trackViewEntity.getSecond_in_screen()) * 1000.0f))) + entityAudio.getStart())
                        if (abs <= entityAudio.getMediaPlayer().getDuration()) {
                            entityAudio.getMediaPlayer().seekTo(abs)
                        }
                        entityAudio.getMediaPlayer().start()
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
                val engineActivity = this@EngineActivity
                engineActivity.updateStartViewTime(engineActivity.trackViewEntity.getCurrent_cursur_position())
            }

            override fun onEnd() {
                if (this@EngineActivity.mIsPlaying) {
                    this@EngineActivity.mIsPlaying = false
                    this@EngineActivity.trackViewEntity.setPlaying(this@EngineActivity.mIsPlaying)
                    this@EngineActivity.blurredImageView.setPlaying(this@EngineActivity.mIsPlaying)
                    this@EngineActivity.stop()
                    try {
                        if (entityAudio.getMediaPlayer() != null && entityAudio.getMediaPlayer().isPlaying()) {
                            entityAudio.getMediaPlayer().pause()
                        }
                    } catch (Exception e) {
                        e.printStackTrace()
                    }
                    val engineActivity = this@EngineActivity
                    engineActivity.startCursur = engineActivity.trackViewEntity.getCurrent_cursur_position()
                }
                if (VolumeFragment.instance != null) {
                    VolumeFragment.instance.updateButton()
                }
                if (SpeedFragment.instance != null) {
                    SpeedFragment.instance.updateButton()
                }
                if (FadeInOutFragment.instance != null) {
                    FadeInOutFragment.instance.updateButton()
                }
                if (EchoEffectFragment.instance != null) {
                    EchoEffectFragment.instance.updateButton()
                }
                if (EnhanceVoiceFragment.instance != null) {
                    EnhanceVoiceFragment.instance.updateButton()
                }
                if (RemoveNoiceFragment.instance != null) {
                    RemoveNoiceFragment.instance.updateButton()
                }
            }
        })
        this.valueAnimator = smoothTimelineAnimator
        smoothTimelineAnimator.start()
        if (this.mTemplate.isVideoSquare()) {
            start()
        }
    }

    fun updateTime(j: Long) {
        val timeFormatter = this.timeFormatter
        if (timeFormatter == null) {
            this.timeFormatter = TimeFormatter(this.trackViewEntity.getMaxTime())
        } else {
            timeFormatter.setTotalDurationMs(this.trackViewEntity.getMaxTime())
        }
        android.util.Pair<String, String> formatTime = this.timeFormatter.formatTime(j)
        this.blurredImageView.setCurrentTime((String) formatTime.first, (String) formatTime.second)
    }

    private fun initTimeLineView() {
        this.tv_currentTime = (TextView) findViewById(R.id.tv_current_time)
        this.tv_endTime = (TextView) findViewById(R.id.tv_end_time)
        val trackEntityView = (TrackEntityView) findViewById(R.id.time_line_view)
        this.trackViewEntity = trackEntityView
        trackEntityView.setiTrimLineCallback(this.iTrimLineCallback)
        this.trackViewEntity.setScaleFactor(this.mTemplate.getScale_timeline())
        this.trackViewEntity.post(object : Runnable() {
            override fun run() {
                var screenWidth: Int = ScreenUtils.getScreenWidth(this@EngineActivity)
                var f: Float = screenWidth * 0.12f
                this@EngineActivity.trackViewEntity.setSecond_in_screen
            this@EngineActivity.trackViewEntity.setSecond_in_screen(f, 0, screenWidth)
                this@EngineActivity.trackViewEntity.setMaxTime
            this@EngineActivity.trackViewEntity.init(screenWidth, this@EngineActivity.trackViewEntity.getHeight())
                this@EngineActivity.trackViewEntity.setPosCursur(this@EngineActivity.mTemplate.getCurrentCursur())
                val engineActivity = this@EngineActivity
                engineActivity.startCursur = engineActivity.trackViewEntity.getCurrent_cursur_position()
                val engineActivity2 = this@EngineActivity
                engineActivity2.updateViewTime(engineActivity2.trackViewEntity.getMaxTime(), this@EngineActivity.trackViewEntity.getCurrent_cursur_position())
            }
        })
    }

    fun addAudioTemplateHttp(uri: Uri, i: Int, str: String) {
        String uri2
        try {
            if (isDestroyed()) {
                return
            }
            if (uri == null) {
                hideProgressFragment()
                return
            }
            if (this.mTemplate.getEntityMediaList() != null) {
                updateProgress(i + 1, this.mTemplate.getEntityMediaList().size)
            }
            if (str != null) {
                uri2 = uri.getPath()
            } else if (!uri.toString().contains("share_with_me")) {
                uri2 = AudioUtils.copyFromUri(this, uri, this.mTemplate.getFolder_template())
            } else {
                uri2 = uri.toString()
            }
            var str2: String? = uri2
            val entityMedia = this.mTemplate.getEntityMediaList()[]
            if (entityMedia.isApplyEffectInPreview()) {
                var file = File(this.mTemplate.getFolder_template(), System.currentTimeMillis() + "_audio_echo.mp3")
                val effectAudio = entityMedia.getEffectAudio()
                var start: Float = effectAudio.getStart() / 1000.0f
                var end: Float = effectAudio.getEnd() / 1000.0f
                val arrayList = mutableListOf()
                arrayList.add("atrim=start=" + start + ":end=" + end)
                arrayList.add("asetpts=N/SR/TB")
                if (effectAudio.isRemoveNoice()) {
                    arrayList.add("afftdn=nf=-25")
                }
                arrayList.add(String.format(Locale.US, "volume=%.2f", Float.valueOf(effectAudio.getVolume())))
                if (effectAudio.getFade_in() > 0) {
                    arrayList.add("afade=t=in:st=0:d=" + effectAudio.getFade_in())
                }
                if (effectAudio.getFade_out() > 0) {
                    var fade_out: Float = effectAudio.getFade_out()
                    arrayList.add("afade=t=out:st=" + ((end - start) - fade_out) + ":d=" + fade_out)
                }
                if (effectAudio.isEnhance()) {
                    arrayList.add(Common.ENHANCE_CMD)
                }
                if (effectAudio.getReverbPreset() != null) {
                    arrayList.add(effectAudio.getReverbPreset())
                }
                if (effectAudio.getDecays() > 0) {
                    arrayList.add(String.format(Locale.US, "aecho=%.2f:%.2f:%s:%s", Float.valueOf(1.0f), Float.valueOf(effectAudio.getOutGain()), effectAudio.getDelays_cmd(), effectAudio.getDecays_cmd()))
                }
                if (effectAudio.getSpeed() != 1.0f) {
                    arrayList.addAll(buildSpeedFilters(effectAudio.getSpeed()))
                }
                this.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", str2, "-af", TextUtils.join(",", arrayList), "-y", file.getAbsolutePath()), object : FFmpegSessionCompleteCallback() {
                    override fun apply(fFmpegSession: FFmpegSession) {
                        try {
                            this@EngineActivity.mPlayer = MediaPlayer()
                            this@EngineActivity.mPlayer.setAudioStreamType(3)
                            var fromFile: Uri? = Uri.fromFile(file)
                            if (fromFile.getScheme() != null && fromFile.getScheme().startsWith("http")) {
                                this@EngineActivity.mPlayer.setDataSource(fromFile.toString())
                            } else {
                                this@EngineActivity.mPlayer.setDataSource(this@EngineActivity, fromFile)
                            }
                            this@EngineActivity.mPlayer.prepareAsync()
                            this@EngineActivity.mPlayer.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {.1
                                override fun onPrepared(mediaPlayer: MediaPlayer) {
                                    if (mediaPlayer == null) {
                                        return
                                    }
                                    try {
                                        this@EngineActivity.addEntitMediaHttp(entityMedia, effectAudio.getDuration(), uri, mediaPlayer, entityMedia.getPaths_https(), i, str2, str)
                                    } catch (Exception unused) {
                                        this@EngineActivity.hideProgressFragment()
                                    }
                                }
                            })
                        } catch (Exception e) {
                            e.printStackTrace()
                        }
                    }
                }).getSessionId()))
                return
            }
            val mediaPlayer = MediaPlayer()
            this.mPlayer = mediaPlayer
            mediaPlayer.setAudioStreamType(3)
            if (uri.getScheme() != null && uri.getScheme().startsWith("http")) {
                this.mPlayer.setDataSource(uri.toString())
            } else {
                this.mPlayer.setDataSource(this, uri)
            }
            this.mPlayer.prepareAsync()
            this.mPlayer.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
                override fun onPrepared(mediaPlayer2: MediaPlayer) {
                    if (mediaPlayer2 == null) {
                        return
                    }
                    try {
                        this@EngineActivity.addEntitMediaHttp(entityMedia, mediaPlayer2.getDuration(), uri, mediaPlayer2, entityMedia.getPaths_https(), i, str2, str)
                    } catch (Exception unused) {
                        this@EngineActivity.hideProgressFragment()
                    }
                }
            })
        } catch (Exception e) {
            e.printStackTrace()
            hideProgressFragment()
        }
    }

    fun buildSpeedFilters(): List<String>(float f) {
        val arrayList = mutableListOf()
        if (f < 0.5f) {
            while (f < 0.5f) {
                arrayList.add("atempo=0.5")
                f /= 0.5f
            }
            arrayList.add(String.format(Locale.US, "atempo=%.2f", Float.valueOf(f)))
        } else if (f > 2.0f) {
            while (f > 2.0f) {
                arrayList.add("atempo=2.0")
                f /= 2.0f
            }
            arrayList.add(String.format(Locale.US, "atempo=%.2f", Float.valueOf(f)))
        } else {
            arrayList.add(String.format(Locale.US, "atempo=%.2f", Float.valueOf(f)))
        }
        return arrayList
    }

    fun addAudioFromVideo(uri: Uri, str: String) {
        try {
            val mediaPlayer = MediaPlayer()
            this.mPlayer = mediaPlayer
            mediaPlayer.setAudioStreamType(3)
            if (uri.getScheme() != null && uri.getScheme().startsWith("http")) {
                this.mPlayer.setDataSource(uri.toString())
            } else {
                this.mPlayer.setDataSource(this, uri)
            }
            this.mPlayer.prepareAsync()
            this.mPlayer.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
                override fun onPrepared(mediaPlayer2: MediaPlayer) {
                    if (mediaPlayer2 == null) {
                        return
                    }
                    this@EngineActivity.changeEntityAudioFromVideo(mediaPlayer2.getDuration(), uri, str)
                    try {
                        this@EngineActivity.runOnUiThread(object : Runnable {
                            override fun run() {
                                this@EngineActivity.updateTimeToEndAya()
                            }
                        })
                    } catch (Exception e) {
                        e.printStackTrace()
                        this@EngineActivity.runOnUiThread(object : Runnable {
                            override fun run() {
                                this@EngineActivity.hideProgressFragment()
                                this@EngineActivity.hideFragment()
                            }
                        })
                    }
                }
            })
        } catch (Exception e) {
            e.printStackTrace()
            hideFragment()
            hideProgressFragment()
        }
    }

    fun updateProgress(i: Int, i2: Int) {
        runOnUiThread(object : Runnable() {
            override fun run() {
                if (ProgressViewFragment.instance != null) {
                    ProgressViewFragment.instance.update(i, i2)
                }
            }
        })
    }

    fun addAudioReciters(list: List<RecitersModel>, i: Int) {
        Uri parse
        try {
            if (isDestroyed()) {
                return
            }
            updateProgress(i + 1, list.size)
            if (i >= list.size) {
                runOnUiThread(object : Runnable() {
                    override fun run() {
                        this@EngineActivity.updateTime()
                        this@EngineActivity.trackViewEntity.translateToEnd()
                        this@EngineActivity.updateBtnToEnd()
                        this@EngineActivity.updateBtnToStart()
                        this@EngineActivity.hideProgressFragment()
                        this@EngineActivity.hideFragment()
                    }
                })
                return
            }
            val recitersModel = list[]
            if (recitersModel.isTarteel()) {
                parse = Uri.parse("https://audio-cdn.tarteel.ai/quran/" + recitersModel.getIdentifer() + "/" + recitersModel.getSurah_index() + recitersModel.getNumber_aya() + ".mp3")
            } else {
                parse = Uri.parse("https://everyayah.com/data/" + recitersModel.getIdentifer() + "/" + recitersModel.getSurah_index() + recitersModel.getNumber_aya() + ".mp3")
            }
            val mediaPlayer = MediaPlayer()
            this.mPlayer = mediaPlayer
            mediaPlayer.setAudioStreamType(3)
            if (parse.getScheme() != null && parse.getScheme().startsWith("http")) {
                this.mPlayer.setDataSource(parse.toString())
            } else {
                this.mPlayer.setDataSource(this, parse)
            }
            this.mPlayer.prepareAsync()
            this.mPlayer.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
                override fun onPrepared(mediaPlayer2: MediaPlayer) {
                    if (mediaPlayer2 == null) {
                        this@EngineActivity.hideProgressFragment()
                    } else {
                        this@EngineActivity.changeEntityAudioReciters(mediaPlayer2.getDuration(), parse, mediaPlayer2, list, i)
                    }
                }
            })
        } catch (Exception e) {
            e.printStackTrace()
            hideProgressFragment()
        }
    }

    fun addEntitMediaHttp(entityMedia: EntityMedia, i: Int, uri: Uri, mediaPlayer: MediaPlayer, list: List<String>, i2: Int, str: String, str2: String) {
        EntityAudio entityAudio
        var posX: Float = 0f
        var posY: Float = 0f
        var round: Int = Math.round(this.trackViewEntity.getWidth() * 0.077f)
        var round2: Int = Math.round(this.trackViewEntity.getSecond_in_screenNoScale() * (i / 1000.0f))
        if (entityMedia.getStart() != entityMedia.getEnd()) {
            if (this.mTemplate.isNewCode()) {
                posX = entityMedia.getPosX()
                posY = entityMedia.getPosY()
            } else {
                posX = (entityMedia.getPosX() / 1000.0f) * this.trackViewEntity.getSecond_in_screen()
                posY = (entityMedia.getPosY() / 1000.0f) * this.trackViewEntity.getSecond_in_screen()
            }
            var entityAudio2: EntityAudio? = EntityAudio(null, uri, posX, 0.0f, round, posY, entityMedia.getMax(), this.trackViewEntity.getSecond_in_screenNoScale(), i, entityMedia.getOffset(), entityMedia.getOffset_right(), entityMedia.getOffset_left())
            entityAudio2.setPathHttp(list)
            entityAudio2.setMediaPlayer(mediaPlayer)
            entityAudio2.setVideo_path(str2)
            entityAudio2.setStart(entityMedia.getStart())
            entityAudio2.setMin_duration(entityMedia.getStart_original())
            if (entityMedia.getEnd() != 0.0f) {
                entityAudio2.setEnd(entityMedia.getEnd())
            }
            entityAudio2.setEffectAudio(entityMedia.getEffectAudio())
            entityAudio2.setFade_in(entityMedia.getDuration_fade_in())
            entityAudio2.setFade_out(entityMedia.getDuration_fade_out())
            this.trackViewEntity.addAudio(entityAudio2)
            entityAudio = entityAudio2
        } else {
            entityAudio = null
        }
        if (round2 <= 0 || round <= 0) {
            this.trackViewEntity.invalidate()
            hideProgressFragment()
            return
        }
        try {
            var file = File(this.mTemplate.getFolder_template(), System.currentTimeMillis() + "_output.pcm")
            val arrayList = mutableListOf()
            arrayList.add("-i")
            arrayList.add(str)
            arrayList.add("-map")
            arrayList.add("0:a")
            arrayList.add("-ac")
            arrayList.add(IcyHeaders.REQUEST_HEADER_ENABLE_METADATA_VALUE)
            arrayList.add("-ar")
            arrayList.add("44100")
            arrayList.add("-f")
            arrayList.add("s16le")
            arrayList.add(file.getAbsolutePath())
            arrayList.add("-y")
            this.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayList.toTypedArray(), object : FFmpegSessionCompleteCallback() {
                override fun apply(fFmpegSession: FFmpegSession) {
                    if (ReturnCode.isSuccess(fFmpegSession.getReturnCode())) {
                        try {
                            var i3: Int = round
                            entityAudio.setAmps(PCMWaveformExtractor.extractWaveform(file.getAbsolutePath(), round2 / (((i3 * 0.1f)) + ((i3 * 0.07f)))), round2, round)
                            entityAudio.setPath_ffmpeg(str)
                            var i4: Int = i2 + 1
                            if (i4 >= this@EngineActivity.mTemplate.getEntityMediaList().size) {
                                try {
                                    this@EngineActivity.runOnUiThread(object : Runnable {
                                        override fun run() {
                                            this@EngineActivity.updateTime()
                                            this@EngineActivity.trackViewEntity.invalidate()
                                            this@EngineActivity.hideProgressFragment()
                                        }
                                    })
                                } catch (Exception e) {
                                    e.printStackTrace()
                                }
                            } else {
                                val entityMedia2 = this@EngineActivity.mTemplate.getEntityMediaList()[]
                                if (entityMedia2.getVideo_path() != null) {
                                    val engineActivity = this@EngineActivity
                                    entityMedia.setVideo_path(AudioUtils.copyFromUri(engineActivity, Uri.parse(engineActivity.mTemplate.getUri_upload_extract_audio_video()), this@EngineActivity.mTemplate.getFolder_template()))
                                    if (this@EngineActivity.mTemplate.getExtension() != null) {
                                        val engineActivity2 = this@EngineActivity
                                        engineActivity2.addAudioFromVideoWithExtention(engineActivity2.mTemplate.getExtension(), entityMedia.getVideo_path(), i4)
                                    } else {
                                        this@EngineActivity.start_extenstion = 0
                                        this@EngineActivity.extractAudioFromVideoRecursive(entityMedia.getVideo_path(), 0, true, i4)
                                    }
                                } else if (entityMedia2.getPaths_https() != null) {
                                    this@EngineActivity.addAudioRecitersTemplate(entityMedia2.getPaths_https(), i4, null)
                                } else {
                                    this@EngineActivity.addAudioTemplateHttp(Uri.parse(entityMedia2.getUri()), i4, null)
                                }
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace()
                            this@EngineActivity.runOnUiThread(object : Runnable {
                                override fun run() {
                                    this@EngineActivity.hideProgressFragment()
                                    this@EngineActivity.hideFragment()
                                }
                            })
                        }
                    }
                }
            }).getSessionId()))
        } catch (Exception e) {
            e.printStackTrace()
            hideProgressFragment()
        }
        this.trackViewEntity.invalidate()
    }

    fun changeEntityAudio(i: Int, uri: Uri, list: List<String>, i2: Int, str: String) {
        EntityAudio audio
        try {
            var scaleFactor: Float = (this.trackViewEntity.getEntityListAudio().isEmpty() || (audio = this.trackViewEntity.getAudio()) == null) ? 0.0f : audio.getRect().right / this.trackViewEntity.getScaleFactor()
            var round: Int = Math.round(this.trackViewEntity.getWidth() * 0.077f)
            var round2: Int = Math.round(this.trackViewEntity.getSecond_in_screenNoScale() * (i / 1000.0f))
            var f: Float = round2
            var entityAudio: EntityAudio? = EntityAudio(null, uri, scaleFactor, 0.0f, round, f + scaleFactor, f, this.trackViewEntity.getSecond_in_screenNoScale(), i)
            entityAudio.setMediaPlayer(this.mPlayer)
            entityAudio.setPathHttp(list)
            entityAudio.getEffectAudio().setEnd(entityAudio.getEnd())
            entityAudio.getEffectAudio().setStart(entityAudio.getStart())
            entityAudio.getEffectAudio().setDuration((entityAudio.getEnd() - entityAudio.getStart()))
            this.trackViewEntity.addAudio(entityAudio)
            if (round2 > 0 && round > 0) {
                this.executor.execute(object : Runnable() { // from class: Runnable
                    override
                    void run() {
                        this@EngineActivity.uri, round, round2, str, entityAudio, i2)
                    }
                })
                this.trackViewEntity.invalidate()
            }
        } catch (Exception e) {
            e.printStackTrace()
            hideProgressFragment()
            hideFragment()
        }
    }

 fun m639xda6d3951(uri: Uri, i: Int, i2: Int, str: String, entityAudio: EntityAudio, i3: Int) {
        try {
            var copyFromUri: String? = AudioUtils.copyFromUri(this, uri, this.mTemplate.getFolder_template())
            var f: Float = i
            entityAudio.setAmps(PCMWaveformExtractor.extractWaveform(str, i2 / (((0.1f * f)) + ((f * 0.07f)))), i2, i)
            entityAudio.setPath_ffmpeg(copyFromUri)
            if (i3 != -1) {
                var i4: Int = i3 + 1
                if (i4 >= this.mTemplate.getEntityMediaList().size) {
                    try {
                        runOnUiThread(object : Runnable() {
                            override fun run() {
                                this@EngineActivity.updateTimeToEndAya()
                                this@EngineActivity.updateBtnToEnd()
                                this@EngineActivity.updateBtnToStart()
                                this@EngineActivity.hideProgressFragment()
                                this@EngineActivity.hideFragment()
                            }
                        })
                        return
                    } catch (Exception e) {
                        e.printStackTrace()
                        hideProgressFragment()
                        hideFragment()
                        return
                    }
                }
                val entityMedia = this.mTemplate.getEntityMediaList()[]
                val entityMedia2 = this.mTemplate.getEntityMediaList()[]
                if (entityMedia2.getVideo_path() != null) {
                    entityMedia.setVideo_path(AudioUtils.copyFromUri(this, Uri.parse(this.mTemplate.getUri_upload_extract_audio_video()), this.mTemplate.getFolder_template()))
                    if (this.mTemplate.getExtension() != null) {
                        addAudioFromVideoWithExtention(this.mTemplate.getExtension(), entityMedia.getVideo_path(), i4)
                        return
                    } else {
                        this.start_extenstion = 0
                        extractAudioFromVideoRecursive(entityMedia.getVideo_path(), 0, true, i4)
                        return
                    }
                }
                if (entityMedia2.getPaths_https() != null) {
                    addAudioRecitersTemplate(entityMedia2.getPaths_https(), i4, null)
                    return
                } else {
                    addAudioTemplateHttp(Uri.parse(entityMedia2.getUri()), i4, null)
                    return
                }
            }
            try {
                runOnUiThread(object : Runnable() {
                    override fun run() {
                        this@EngineActivity.trackViewEntity.calculMaxTime()
                        val engineActivity = this@EngineActivity
                        engineActivity.updateViewTime(engineActivity.trackViewEntity.getMaxTime(), this@EngineActivity.trackViewEntity.getCurrent_cursur_position())
                        this@EngineActivity.trackViewEntity.translateToEnd()
                        this@EngineActivity.updateTimeToEndAya()
                        this@EngineActivity.updateBtnToEnd()
                        this@EngineActivity.updateBtnToStart()
                        this@EngineActivity.trackViewEntity.invalidate()
                        this@EngineActivity.hideProgressFragment()
                        this@EngineActivity.hideFragment()
                    }
                })
                return
            } catch (Exception e2) {
                e2.printStackTrace()
                hideProgressFragment()
                hideFragment()
                return
            }
        } catch (Exception e3) {
            e3.printStackTrace()
            hideProgressFragment()
            hideFragment()
        }
        e3.printStackTrace()
        hideProgressFragment()
        hideFragment()
    }

    fun addEntitMediaHttp(entityMedia: EntityMedia, i: Int, uri: Uri, mediaPlayer: MediaPlayer, list: List<String>, i2: Int, str: String, str2: String, str3: String) {
        EntityAudio entityAudio
        var posX: Float = 0f
        var posY: Float = 0f
        var round: Int = Math.round(this.trackViewEntity.getWidth() * 0.077f)
        var round2: Int = Math.round(this.trackViewEntity.getSecond_in_screenNoScale() * (i / 1000.0f))
        if (entityMedia.getStart() != entityMedia.getEnd()) {
            if (this.mTemplate.isNewCode()) {
                posX = entityMedia.getPosX()
                posY = entityMedia.getPosY()
            } else {
                posX = (entityMedia.getPosX() / 1000.0f) * this.trackViewEntity.getSecond_in_screen()
                posY = (entityMedia.getPosY() / 1000.0f) * this.trackViewEntity.getSecond_in_screen()
            }
            entityAudio = EntityAudio(null, uri, posX, 0.0f, round, posY, entityMedia.getMax(), this.trackViewEntity.getSecond_in_screenNoScale(), i, entityMedia.getOffset(), entityMedia.getOffset_right(), entityMedia.getOffset_left())
            entityAudio.setPathHttp(list)
            entityAudio.setMediaPlayer(mediaPlayer)
            entityAudio.setVideo_path(str3)
            entityAudio.setStart(entityMedia.getStart())
            entityAudio.setMin_duration(entityMedia.getStart_original())
            if (entityMedia.getEnd() != 0.0f) {
                entityAudio.setEnd(entityMedia.getEnd())
            }
            entityAudio.setEffectAudio(entityMedia.getEffectAudio())
            entityAudio.setFade_in(entityMedia.getDuration_fade_in())
            entityAudio.setFade_out(entityMedia.getDuration_fade_out())
            this.trackViewEntity.addAudio(entityAudio)
        } else {
            entityAudio = null
        }
        var entityAudio2: EntityAudio? = entityAudio
        if (round2 <= 0 || round <= 0) {
            this.trackViewEntity.invalidate()
            hideProgressFragment()
        } else {
            this.executor.execute(object : Runnable() {
                override fun run() {
                    try {
                        var i3: Int = round
                        entityAudio2.setAmps(PCMWaveformExtractor.extractWaveform(str2, round2 / (((i3 * 0.1f)) + ((i3 * 0.07f)))), round2, round)
                        entityAudio2.setPath_ffmpeg(str)
                        var i4: Int = i2 + 1
                        if (i4 >= this@EngineActivity.mTemplate.getEntityMediaList().size) {
                            try {
                                this@EngineActivity.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        this@EngineActivity.updateTime()
                                        this@EngineActivity.trackViewEntity.invalidate()
                                        this@EngineActivity.hideProgressFragment()
                                    }
                                })
                            } catch (Exception e) {
                                e.printStackTrace()
                            }
                        } else {
                            val entityMedia2 = this@EngineActivity.mTemplate.getEntityMediaList()[]
                            if (entityMedia2.getVideo_path() != null) {
                                val engineActivity = this@EngineActivity
                                entityMedia.setVideo_path(AudioUtils.copyFromUri(engineActivity, Uri.parse(engineActivity.mTemplate.getUri_upload_extract_audio_video()), this@EngineActivity.mTemplate.getFolder_template()))
                                if (this@EngineActivity.mTemplate.getExtension() != null) {
                                    val engineActivity2 = this@EngineActivity
                                    engineActivity2.addAudioFromVideoWithExtention(engineActivity2.mTemplate.getExtension(), entityMedia.getVideo_path(), i4)
                                } else {
                                    this@EngineActivity.start_extenstion = 0
                                    this@EngineActivity.extractAudioFromVideoRecursive(entityMedia.getVideo_path(), 0, true, i4)
                                }
                            } else if (entityMedia2.getPaths_https() != null) {
                                this@EngineActivity.addAudioRecitersTemplate(entityMedia2.getPaths_https(), i4, null)
                            } else {
                                this@EngineActivity.addAudioTemplateHttp(Uri.parse(entityMedia2.getUri()), i4, null)
                            }
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace()
                        this@EngineActivity.runOnUiThread(object : Runnable {
                            override fun run() {
                                this@EngineActivity.hideProgressFragment()
                                this@EngineActivity.hideFragment()
                            }
                        })
                    }
                }
            })
        }
    }

    fun addAudio(uri: Uri) {
        try {
            val mediaPlayer = MediaPlayer()
            this.mPlayer = mediaPlayer
            mediaPlayer.setAudioStreamType(3)
            if (uri.getScheme() != null && uri.getScheme().startsWith("http")) {
                this.mPlayer.setDataSource(uri.toString())
            } else {
                this.mPlayer.setDataSource(this, uri)
            }
            this.mPlayer.prepareAsync()
            this.mPlayer.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
                override fun onPrepared(mediaPlayer2: MediaPlayer) {
                    if (mediaPlayer2 == null) {
                        return
                    }
                    this@EngineActivity.changeEntityAudio(mediaPlayer2.getDuration(), uri)
                }
            })
        } catch (Exception e) {
            hideProgressFragment()
            hideFragment()
            e.printStackTrace()
        }
    }

    private fun addAudio(uri: Uri, list: List<String>, i: Int, str: String) {
        try {
            val mediaPlayer = MediaPlayer()
            this.mPlayer = mediaPlayer
            mediaPlayer.setAudioStreamType(3)
            if (uri.getScheme() != null && uri.getScheme().startsWith("http")) {
                this.mPlayer.setDataSource(uri.toString())
            } else {
                this.mPlayer.setDataSource(this, uri)
            }
            this.mPlayer.prepareAsync()
            this.mPlayer.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
                override fun onPrepared(mediaPlayer2: MediaPlayer) {
                    if (mediaPlayer2 == null) {
                        return
                    }
                    this@EngineActivity.changeEntityAudio(mediaPlayer2.getDuration(), uri, list, i, str)
                }
            })
        } catch (Exception e) {
            hideProgressFragment()
            hideFragment()
            e.printStackTrace()
        }
    }

    fun addAudioTemplate(uri: Uri, list: List<String>, i: Int, str: String, str2: String, str3: String) {
        try {
            val mediaPlayer = MediaPlayer()
            this.mPlayer = mediaPlayer
            mediaPlayer.setAudioStreamType(3)
            if (uri.getScheme() != null && uri.getScheme().startsWith("http")) {
                this.mPlayer.setDataSource(uri.toString())
            } else {
                this.mPlayer.setDataSource(this, uri)
            }
            this.mPlayer.prepareAsync()
            this.mPlayer.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
                override fun onPrepared(mediaPlayer2: MediaPlayer) {
                    if (mediaPlayer2 != null && i < this@EngineActivity.mTemplate.getEntityMediaList().size) {
                        this@EngineActivity.addEntitMediaHttp(this@EngineActivity.mTemplate.getEntityMediaList()[], mediaPlayer2.getDuration(), uri, this@EngineActivity.mPlayer, list, i, str, str2, str3)
                    }
                }
            })
        } catch (Exception e) {
            hideProgressFragment()
            hideFragment()
            e.printStackTrace()
        }
    }

    fun changeEntityAudioFromVideo(i: Int, uri: Uri, str: String) {
        EntityAudio audio
        try {
            var scaleFactor: Float = (this.trackViewEntity.getEntityListAudio().isEmpty() || (audio = this.trackViewEntity.getAudio()) == null) ? 0.0f : audio.getRect().right / this.trackViewEntity.getScaleFactor()
            var round: Int = Math.round(this.trackViewEntity.getWidth() * 0.077f)
            var round2: Int = Math.round(this.trackViewEntity.getSecond_in_screenNoScale() * (i / 1000.0f))
            var f: Float = round2
            var entityAudio: EntityAudio? = EntityAudio(null, uri, scaleFactor, 0.0f, round, f + scaleFactor, f, this.trackViewEntity.getSecond_in_screenNoScale(), i)
            entityAudio.setMediaPlayer(this.mPlayer)
            entityAudio.getEffectAudio().setEnd(entityAudio.getEnd())
            entityAudio.getEffectAudio().setStart(entityAudio.getStart())
            entityAudio.getEffectAudio().setDuration((entityAudio.getEnd() - entityAudio.getStart()))
            this.trackViewEntity.addAudio(entityAudio)
            if (round2 > 0 && round > 0) {
                var copyFromUri: String? = AudioUtils.copyFromUri(this, uri, this.mTemplate.getFolder_template())
                var file = File(this.mTemplate.getFolder_template(), System.currentTimeMillis() + "_output.pcm")
                val arrayList = mutableListOf()
                arrayList.add("-i")
                arrayList.add(copyFromUri)
                arrayList.add("-map")
                arrayList.add("0:a")
                arrayList.add("-ac")
                arrayList.add(IcyHeaders.REQUEST_HEADER_ENABLE_METADATA_VALUE)
                arrayList.add("-ar")
                arrayList.add("44100")
                arrayList.add("-f")
                arrayList.add("s16le")
                arrayList.add(file.getAbsolutePath())
                arrayList.add("-y")
                this.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayList.toTypedArray(), object : FFmpegSessionCompleteCallback() {
                    override fun apply(fFmpegSession: FFmpegSession) {
                        if (ReturnCode.isSuccess(fFmpegSession.getReturnCode())) {
                            try {
                                var i2: Int = round
                                entityAudio.setAmps(PCMWaveformExtractor.extractWaveform(file.getAbsolutePath(), round2 / (((i2 * 0.1f)) + ((i2 * 0.07f)))), round2, round)
                                entityAudio.setPath_ffmpeg(uri.getPath())
                                entityAudio.setVideo_path
            this@EngineActivity.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        this@EngineActivity.trackViewEntity.invalidate()
                                        this@EngineActivity.hideProgressFragment()
                                        this@EngineActivity.hideFragment()
                                    }
                                })
                            } catch (Exception e) {
                                e.printStackTrace()
                                this@EngineActivity.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        this@EngineActivity.hideProgressFragment()
                                        this@EngineActivity.hideFragment()
                                    }
                                })
                            }
                        }
                    }
                }).getSessionId()))
                this.trackViewEntity.invalidate()
            }
        } catch (Exception e) {
            e.printStackTrace()
            runOnUiThread(object : Runnable() {
                override fun run() {
                    this@EngineActivity.hideProgressFragment()
                    this@EngineActivity.hideFragment()
                }
            })
        }
    }

    fun changeEntityAudioReciters(i: Int, uri: Uri, mediaPlayer: MediaPlayer, list: List<RecitersModel>, i2: Int) {
        EntityAudio audio
        try {
            var scaleFactor: Float = (this.trackViewEntity.getEntityListAudio().isEmpty() || (audio = this.trackViewEntity.getAudio()) == null) ? 0.0f : audio.getRect().right / this.trackViewEntity.getScaleFactor()
            var round: Int = Math.round(this.trackViewEntity.getWidth() * 0.077f)
            var round2: Int = Math.round(this.trackViewEntity.getSecond_in_screenNoScale() * (i / 1000.0f))
            var f: Float = round2
            var entityAudio: EntityAudio? = EntityAudio(null, uri, scaleFactor, 0.0f, round, f + scaleFactor, f, this.trackViewEntity.getSecond_in_screenNoScale(), i)
            entityAudio.getEffectAudio().setEnd(entityAudio.getEnd())
            entityAudio.getEffectAudio().setStart(entityAudio.getStart())
            entityAudio.getEffectAudio().setDuration((entityAudio.getEnd() - entityAudio.getStart()))
            entityAudio.setMediaPlayer(mediaPlayer)
            this.trackViewEntity.addAudio(entityAudio)
            if (round2 > 0 && round > 0) {
                AudioUtils.copyToLocalAsync(this, uri.toString(), this.mTemplate.getFolder_template(), AnonymousClass54(round2, round, entityAudio, i2, list))
            }
        } catch (Exception e) {
            e.printStackTrace()
            hideProgressFragment()
        }
    }

    private inner class AnonymousClass54 : AudioUtils.Callback {
        EntityAudio val$entityAudio
        int val$h
        int val$index
        List val$recitersModels
        int val$w

        AnonymousClass54(int i, int i2, EntityAudio entityAudio, int i3, List list) {
            this.val$w = i
            this.val$h = i2
            this.val$entityAudio = entityAudio
            this.val$index = i3
            this.val$recitersModels = list
        }

        override fun onSuccess(str: String) {
            try {
                var file = File(this@EngineActivity.mTemplate.getFolder_template(), System.currentTimeMillis() + "_audio_wave.png")
                this@EngineActivity.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", str, "-filter_complex", "aformat=channel_layouts=mono,showwavespic=s=" + this.val$w + "x" + this.val$h + ":colors=#522123", "-frames:v", IcyHeaders.REQUEST_HEADER_ENABLE_METADATA_VALUE, "-y", file.getAbsolutePath()), object : FFmpegSessionCompleteCallback() {.1
                    override fun apply(fFmpegSession: FFmpegSession) {
                        if (ReturnCode.isSuccess(fFmpegSession.getReturnCode())) {
                            try {
                                Glide.with(this@EngineActivity).asBitmap().load(Uri.fromFile(file)).submit().get()
                                AnonymousClass54.this.val$entityAudio.setPath_ffmpeg
            this@EngineActivity.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        this@EngineActivity.trackViewEntity.invalidate()
                                    }
                                })
                            } catch (Exception e) {
                                e.printStackTrace()
                                this@EngineActivity.hideProgressFragment()
                            }
                        }
                        this@EngineActivity.addAudioReciters(AnonymousClass54.this.val$recitersModels, AnonymousClass54.this.val$index + 1)
                    }
                }).getSessionId()))
            } catch (Exception e) {
                e.printStackTrace()
                this@EngineActivity.hideProgressFragment()
            }
        }

        override fun onError(exc: Exception) {
            exc.printStackTrace()
            this@EngineActivity.hideProgressFragment()
        }
    }

    fun duplicateEntityAudio(i: Int, entityAudio: EntityAudio) {
        try {
            var f: Float = entityAudio.getRect().right
            var entityAudio2: EntityAudio? = EntityAudio(null, entityAudio.getUri(), f, entityAudio.getRect().top, entityAudio.getH(), f + entityAudio.getRect().width(), entityAudio.getMax(), entityAudio.getSecond_in_screen(), (i / 1000.0f), 0.0f, 0.0f, 0.0f)
            entityAudio2.setAmps(entityAudio.getAmps())
            entityAudio2.setRenderer(entityAudio.getRenderer())
            entityAudio2.addPathHttp(entityAudio.getPaths_http())
            entityAudio2.setMediaPlayer(entityAudio.getMediaPlayer())
            entityAudio2.getRect().bottom = entityAudio.getRect().bottom
            entityAudio2.setPath_ffmpeg(entityAudio.getPath_ffmpeg())
            entityAudio2.setEffectAudio(entityAudio.getEffectAudio())
            entityAudio2.setVideo_path(entityAudio.getVideo_path())
            entityAudio2.setApplyEffectInPreview(entityAudio.isApplyEffectInPreview())
            entityAudio2.setmScaleFactor(entityAudio.getmScaleFactor())
            entityAudio2.setIndex(entityAudio.getIndex() + 1)
            entityAudio2.setOffset_right(entityAudio.getOffset_right())
            entityAudio2.setOffset_left(entityAudio.getOffset_left())
            entityAudio2.setOffset(entityAudio.getOffset())
            entityAudio2.setEnd(Math.round((Math.abs(Math.round((entityAudio.getRect().right / this.trackViewEntity.getSecond_in_screen()) * 1000.0f)) - Math.abs(Math.round((entityAudio.getRect().left / this.trackViewEntity.getSecond_in_screen()) * 1000.0f))) + entityAudio.getStart()))
            entityAudio2.setStart(entityAudio.getStart())
            entityAudio2.setMin_duration(entityAudio.getMin_duration())
            this.trackViewEntity.addAudio(entityAudio2, entityAudio.getIndex() + 1)
            this.trackViewEntity.invalidate()
        } catch (Exception e) {
            e.printStackTrace()
            hideProgressFragment()
            hideFragment()
        }
    }

    fun changeEntityAudio(i: Int, uri: Uri) {
        String uri2
        EntityAudio audio
        try {
            var scaleFactor: Float = (this.trackViewEntity.getEntityListAudio().isEmpty() || (audio = this.trackViewEntity.getAudio()) == null) ? 0.0f : audio.getRect().right / this.trackViewEntity.getScaleFactor()
            var round: Int = Math.round(this.trackViewEntity.getWidth() * 0.077f)
            var round2: Int = Math.round(this.trackViewEntity.getSecond_in_screenNoScale() * (i / 1000.0f))
            var f: Float = round2
            var entityAudio: EntityAudio? = EntityAudio(null, uri, scaleFactor, 0.0f, round, f + scaleFactor, f, this.trackViewEntity.getSecond_in_screenNoScale(), i)
            entityAudio.setMediaPlayer(this.mPlayer)
            entityAudio.getEffectAudio().setEnd(entityAudio.getEnd())
            entityAudio.getEffectAudio().setStart(entityAudio.getStart())
            entityAudio.getEffectAudio().setDuration((entityAudio.getEnd() - entityAudio.getStart()))
            this.trackViewEntity.addAudio(entityAudio)
            if (round2 > 0 && round > 0) {
                if (!uri.toString().contains("share_with_me")) {
                    uri2 = AudioUtils.copyFromUri(this, uri, this.mTemplate.getFolder_template())
                } else {
                    uri2 = uri.toString()
                }
                var file = File(this.mTemplate.getFolder_template(), System.currentTimeMillis() + "_output.pcm")
                val arrayList = mutableListOf()
                arrayList.add("-i")
                arrayList.add(uri2)
                arrayList.add("-map")
                arrayList.add("0:a")
                arrayList.add("-ac")
                arrayList.add(IcyHeaders.REQUEST_HEADER_ENABLE_METADATA_VALUE)
                arrayList.add("-ar")
                arrayList.add("44100")
                arrayList.add("-f")
                arrayList.add("s16le")
                arrayList.add(file.getAbsolutePath())
                arrayList.add("-y")
                var str: String? = uri2
                this.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayList.toTypedArray(), object : FFmpegSessionCompleteCallback() {
                    override fun apply(fFmpegSession: FFmpegSession) {
                        if (ReturnCode.isSuccess(fFmpegSession.getReturnCode())) {
                            try {
                                var i2: Int = round
                                entityAudio.setAmps(PCMWaveformExtractor.extractWaveform(file.getAbsolutePath(), round2 / (((i2 * 0.1f)) + ((i2 * 0.07f)))), round2, round)
                                entityAudio.setPath_ffmpeg
            this@EngineActivity.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        this@EngineActivity.updateTimeToEndAya()
                                        this@EngineActivity.updateBtnToEnd()
                                        this@EngineActivity.updateBtnToStart()
                                        this@EngineActivity.hideProgressFragment()
                                        this@EngineActivity.hideFragment()
                                    }
                                })
                            } catch (Exception e) {
                                e.printStackTrace()
                                this@EngineActivity.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        this@EngineActivity.hideProgressFragment()
                                        this@EngineActivity.hideFragment()
                                    }
                                })
                            }
                        }
                    }
                }).getSessionId()))
                this.trackViewEntity.invalidate()
            }
        } catch (Exception e) {
            e.printStackTrace()
            hideProgressFragment()
            hideFragment()
        }
    }

    private fun createCmd(): String(EffectAudio effectAudio, float f, float f2) {
        val arrayList = mutableListOf()
        arrayList.add(String.format(Locale.US, "atrim=start=%.2f:end=%.2f", Float.valueOf(f), Float.valueOf(f2)))
        arrayList.add("asetpts=N/SR/TB")
        if (effectAudio.isRemoveNoice()) {
            arrayList.add("afftdn=nf=-25")
        }
        arrayList.add(String.format(Locale.US, "volume=%.2f", Float.valueOf(effectAudio.getVolume())))
        if (effectAudio.getFade_in() > 0) {
            arrayList.add("afade=t=in:st=0:d=" + effectAudio.getFade_in())
        }
        if (effectAudio.getFade_out() > 0) {
            var fade_out: Float = effectAudio.getFade_out()
            arrayList.add("afade=t=out:st=" + ((f2 - f) - fade_out) + ":d=" + fade_out)
        }
        if (effectAudio.isEnhance()) {
            arrayList.add(Common.ENHANCE_CMD)
        }
        if (effectAudio.getReverbPreset() != null) {
            arrayList.add(effectAudio.getReverbPreset())
        }
        if (effectAudio.getDecays() > 0) {
            arrayList.add(String.format(Locale.US, "aecho=%.2f:%.2f:%s:%s", Float.valueOf(1.0f), Float.valueOf(effectAudio.getOutGain()), effectAudio.getDelays_cmd(), effectAudio.getDecays_cmd()))
        }
        if (effectAudio.getSpeed() != 1.0f) {
            arrayList.addAll(buildSpeedFilters(effectAudio.getSpeed()))
        }
        return TextUtils.join(",", arrayList)
    }

    fun applyffectAll(effectAudio: EffectAudio, i: Int) {
        if (i >= this.trackViewEntity.getEntityListAudio().size) {
            runOnUiThread(object : Runnable() {
                override fun run() {
                    this@EngineActivity.trackViewEntity.invalidate()
                    this@EngineActivity.hideProgressFragment()
                    if (this@EngineActivity.iEditMediaCallback != null) {
                        this@EngineActivity.iEditMediaCallback.onDone()
                    }
                }
            })
            return
        }
        android.util.Pair<Integer, EntityAudio> entityAudioNotDeleted = this.trackViewEntity.getEntityAudioNotDeleted(i)
        if (entityAudioNotDeleted == null) {
            runOnUiThread(object : Runnable() {
                override fun run() {
                    this@EngineActivity.trackViewEntity.invalidate()
                    this@EngineActivity.hideProgressFragment()
                    if (this@EngineActivity.iEditMediaCallback != null) {
                        this@EngineActivity.iEditMediaCallback.onDone()
                    }
                }
            })
            return
        }
        var entityAudio: EntityAudio? = entityAudioNotDeleted as EntityAudio.second
        var intValue: Int = ((Integer) entityAudioNotDeleted.first).toInt()
        var createCmd: String? = createCmd(effectAudio, entityAudio.getEffectAudio().getStart() / 1000.0f, entityAudio.getEffectAudio().getEnd() / 1000.0f)
        var file = File(this.mTemplate.getFolder_template(), System.currentTimeMillis() + "_audio_echo.mp3")
        var fromFile: Uri? = Uri.fromFile(file)
        this.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", entityAudio.getPath_ffmpeg(), "-af", createCmd, "-y", file.getAbsolutePath()), object : FFmpegSessionCompleteCallback() {
            override fun apply(fFmpegSession: FFmpegSession) {
                if (ReturnCode.isSuccess(fFmpegSession.getReturnCode())) {
                    try {
                        this@EngineActivity.mPlayer = MediaPlayer()
                        this@EngineActivity.mPlayer.setAudioStreamType(3)
                        if (fromFile.getScheme() != null && fromFile.getScheme().startsWith("http")) {
                            this@EngineActivity.mPlayer.setDataSource(fromFile.toString())
                        } else {
                            this@EngineActivity.mPlayer.setDataSource(this@EngineActivity, fromFile)
                        }
                        this@EngineActivity.mPlayer.prepareAsync()
                        this@EngineActivity.mPlayer.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {.1
                            override fun onPrepared(mediaPlayer: MediaPlayer) {
                                if (entityAudio.getMediaPlayer() != null && mediaPlayer.getDuration() != entityAudio.getMediaPlayer().getDuration()) {
                                    entityAudio.setRight(entityAudio.getRect().left + Math.round(this@EngineActivity.trackViewEntity.getSecond_in_screen() * (mediaPlayer.getDuration() / 1000.0f)))
                                    entityAudio.setDuration(mediaPlayer.getDuration() * 1000)
                                    entityAudio.setEnd(mediaPlayer.getDuration())
                                    entityAudio.setStart(0.0f)
                                    entityAudio.setMax((entityAudio.getRect().right / entityAudio.getmScaleFactor()) - ((entityAudio.getRect().left / entityAudio.getmScaleFactor()) - entityAudio.getOffset_left()))
                                    this@EngineActivity.trackViewEntity.updateWhenEffect(entityAudio)
                                }
                                entityAudio.setMediaPlayer(this@EngineActivity.mPlayer)
                                this@EngineActivity.applyffectAll(effectAudio, intValue + 1)
                            }
                        })
                        entityAudio.setPath_ffmpeg_effect(file.getAbsolutePath())
                        entityAudio.setApplyEffectInPreview(true)
                    } catch (Exception e) {
                        e.printStackTrace()
                        this@EngineActivity.runOnUiThread(object : Runnable {
                            override fun run() {
                                this@EngineActivity.hideProgressFragment()
                                if (this@EngineActivity.iEditMediaCallback != null) {
                                    this@EngineActivity.iEditMediaCallback.onDone()
                                }
                            }
                        })
                    }
                }
            }
        }).getSessionId()))
    }

    fun applyffect(str: String, entityAudio: EntityAudio) {
        showProgressSimple()
        var file = File(this.mTemplate.getFolder_template(), System.currentTimeMillis() + "_audio_echo.mp3")
        this.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", entityAudio.getPath_ffmpeg(), "-af", str, "-y", file.getAbsolutePath()), AnonymousClass59(Uri.fromFile(file), entityAudio, file)).getSessionId()))
    }

    private inner class AnonymousClass59 : FFmpegSessionCompleteCallback {
        EntityAudio val$entityAudio
        File val$file
        Uri val$uri

        AnonymousClass59(Uri uri, EntityAudio entityAudio, File file) {
            this.val$uri = uri
            this.val$entityAudio = entityAudio
            this.val$file = file
        }

        override fun apply(fFmpegSession: FFmpegSession) {
            if (ReturnCode.isSuccess(fFmpegSession.getReturnCode())) {
                try {
                    this@EngineActivity.mPlayer = MediaPlayer()
                    this@EngineActivity.mPlayer.setAudioStreamType(3)
                    if (this.val$uri.getScheme() != null && this.val$uri.getScheme().startsWith("http")) {
                        this@EngineActivity.mPlayer.setDataSource(this.val$uri.toString())
                    } else {
                        this@EngineActivity.mPlayer.setDataSource(this@EngineActivity, this.val$uri)
                    }
                    this@EngineActivity.mPlayer.prepareAsync()
                    this@EngineActivity.mPlayer.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {.1
                        override fun onPrepared(mediaPlayer: MediaPlayer) {
                            if (AnonymousClass59.this.val$entityAudio.getMediaPlayer() != null && mediaPlayer.getDuration() != AnonymousClass59.this.val$entityAudio.getMediaPlayer().getDuration()) {
                                AnonymousClass59.this.val$entityAudio.setRight(AnonymousClass59.this.val$entityAudio.getRect().left + Math.round(this@EngineActivity.trackViewEntity.getSecond_in_screen() * (mediaPlayer.getDuration() / 1000.0f)))
                                AnonymousClass59.this.val$entityAudio.setDuration(mediaPlayer.getDuration() * 1000)
                                AnonymousClass59.this.val$entityAudio.setMax((AnonymousClass59.this.val$entityAudio.getRect().right / AnonymousClass59.this.val$entityAudio.getmScaleFactor()) - ((AnonymousClass59.this.val$entityAudio.getRect().left / AnonymousClass59.this.val$entityAudio.getmScaleFactor()) - AnonymousClass59.this.val$entityAudio.getOffset_left()))
                                this@EngineActivity.trackViewEntity.updateWhenEffect(AnonymousClass59.this.val$entityAudio)
                                this@EngineActivity.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        this@EngineActivity.trackViewEntity.invalidate()
                                        this@EngineActivity.hideProgressFragment()
                                    }
                                })
                            } else {
                                this@EngineActivity.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        this@EngineActivity.hideProgressFragment()
                                    }
                                })
                            }
                            AnonymousClass59.this.val$entityAudio.setMediaPlayer(mediaPlayer)
                        }
                    })
                    this.val$entityAudio.setPath_ffmpeg_effect(this.val$file.getAbsolutePath())
                    this.val$entityAudio.setApplyEffectInPreview(true)
                } catch (Exception e) {
                    e.printStackTrace()
                    this@EngineActivity.runOnUiThread(object : Runnable {
                        override fun run() {
                            this@EngineActivity.hideProgressFragment()
                        }
                    })
                }
            }
        }
    }

    fun addTimeLineBismilah(): EntityBismilahTimeline(BismilahEntity bismilahEntity, float f, float f2) {
        val entityBismilahTimeline = EntityBismilahTimeline(bismilahEntity, f, 0.0f, this.trackViewEntity.getWidth() * 0.077f, f2, this.trackViewEntity.getSecond_in_screen())
        this.trackViewEntity.setBismilahTimeline(entityBismilahTimeline)
        return entityBismilahTimeline
    }

    fun addTimeLineIsti3ada(): EntityBismilahTimeline(BismilahEntity bismilahEntity, float f, float f2) {
        val entityBismilahTimeline = EntityBismilahTimeline(bismilahEntity, f, 0.0f, this.trackViewEntity.getWidth() * 0.077f, f2, this.trackViewEntity.getSecond_in_screen())
        this.trackViewEntity.setmIsi3adaTimeline(entityBismilahTimeline)
        return entityBismilahTimeline
    }

    fun addTimeLineQuran(): EntityQuranTimeline(QuranEntity quranEntity, float f, float f2) {
        val entityQuranTimeline = EntityQuranTimeline(quranEntity, f, 0.0f, this.trackViewEntity.getWidth() * 0.077f, f2, this.trackViewEntity.getSecond_in_screen())
        this.trackViewEntity.addQuran(entityQuranTimeline)
        return entityQuranTimeline
    }

    fun addTimeLineQuran(): EntityTrslTimeline(TranslationQuranEntity translationQuranEntity, float f, float f2) {
        val entityTrslTimeline = EntityTrslTimeline(translationQuranEntity, f, 0.0f, this.trackViewEntity.getWidth() * 0.077f, f2, this.trackViewEntity.getSecond_in_screen())
        this.trackViewEntity.addTrslQuran(entityTrslTimeline)
        return entityTrslTimeline
    }

    fun addTimeLineQuran(): EntityQuranTimeline(int i, QuranEntity quranEntity, float f, float f2) {
        val entityQuranTimeline = EntityQuranTimeline(quranEntity, f, 0.0f, this.trackViewEntity.getWidth() * 0.077f, f2, this.trackViewEntity.getSecond_in_screen())
        this.trackViewEntity.addQuran(entityQuranTimeline, i)
        return entityQuranTimeline
    }

    fun addTimeLineQuran(): EntityTrslTimeline(int i, TranslationQuranEntity translationQuranEntity, float f, float f2) {
        val entityTrslTimeline = EntityTrslTimeline(translationQuranEntity, f, 0.0f, this.trackViewEntity.getWidth() * 0.077f, f2, this.trackViewEntity.getSecond_in_screen())
        this.trackViewEntity.addTrslQuran(entityTrslTimeline, i)
        return entityTrslTimeline
    }

    fun splitTimeLineQuran(): EntityQuranTimeline(int i, QuranEntity quranEntity, float f, float f2, float f3) {
        val entityQuranTimeline = EntityQuranTimeline(quranEntity, f, 0.0f, this.trackViewEntity.getWidth() * 0.077f, f2, this.trackViewEntity.getSecond_in_screen())
        entityQuranTimeline.setmScaleFactor(f3)
        this.trackViewEntity.addQuran_split(entityQuranTimeline, i)
        return entityQuranTimeline
    }

    fun splitTimeLineQuran(): EntityTrslTimeline(int i, TranslationQuranEntity translationQuranEntity, float f, float f2, float f3) {
        val entityTrslTimeline = EntityTrslTimeline(translationQuranEntity, f, 0.0f, this.trackViewEntity.getWidth() * 0.077f, f2, this.trackViewEntity.getSecond_in_screen())
        entityTrslTimeline.setmScaleFactor(f3)
        this.trackViewEntity.addQuran_split(entityTrslTimeline, i)
        return entityTrslTimeline
    }

    fun addTimeLineQuran(): EntityQuranTimeline(QuranEntity quranEntity) {
        var xCursur: Float = this.trackViewEntity.getXCursur()
        val quran = this.trackViewEntity.getQuran()
        if (quran != null) {
            xCursur = quran.getRect().right
        }
        val trackEntityView = this.trackViewEntity
        if (trackEntityView.isExist(trackEntityView.getBismilahTimeline())) {
            xCursur = max(xCursur, this.trackViewEntity.getBismilahTimeline().getRect().right)
        }
        var f: Float = xCursur
        val entityQuranTimeline = EntityQuranTimeline(quranEntity, f, 0.0f, this.trackViewEntity.getWidth() * 0.077f, f + (this.trackViewEntity.getSecond_in_screen() * 4.0f), this.trackViewEntity.getSecond_in_screen())
        this.trackViewEntity.addQuran(entityQuranTimeline)
        return entityQuranTimeline
    }

    fun addTimeLineTrslQuran(): EntityTrslTimeline(TranslationQuranEntity translationQuranEntity) {
        var xCursur: Float = this.trackViewEntity.getXCursur()
        val trslQuran = this.trackViewEntity.getTrslQuran()
        if (trslQuran != null) {
            xCursur = trslQuran.getRect().right
        }
        val entityTrslTimeline = EntityTrslTimeline(translationQuranEntity, xCursur, 0.0f, this.trackViewEntity.getWidth() * 0.077f, this.trackViewEntity.getQuran().getRect().right, this.trackViewEntity.getSecond_in_screen())
        this.trackViewEntity.addTrslQuran(entityTrslTimeline)
        return entityTrslTimeline
    }

    fun addTimeLineBismilah(): EntityBismilahTimeline(BismilahEntity bismilahEntity) {
        var f: Float = this.trackViewEntity.getmIsi3adaTimeline() != null ? this.trackViewEntity.getmIsi3adaTimeline().getRect().right : 0.0f
        val entityBismilahTimeline = EntityBismilahTimeline(bismilahEntity, f, 0.0f, this.trackViewEntity.getWidth() * 0.077f, f + (this.trackViewEntity.getSecond_in_screen() * 4.0f), this.trackViewEntity.getSecond_in_screen())
        this.trackViewEntity.setBismilahTimeline(entityBismilahTimeline)
        return entityBismilahTimeline
    }

    fun addTimeLineIsti3ada(): EntityBismilahTimeline(BismilahEntity bismilahEntity) {
        val entityBismilahTimeline = EntityBismilahTimeline(bismilahEntity, 0.0f, 0.0f, this.trackViewEntity.getWidth() * 0.077f, (this.trackViewEntity.getSecond_in_screen() * 4.0f) + 0.0f, this.trackViewEntity.getSecond_in_screen())
        this.trackViewEntity.setmIsi3adaTimeline(entityBismilahTimeline)
        return entityBismilahTimeline
    }

    fun enableUndoBtn() {
        try {
            val imageButton = this.btnUndo
            if (imageButton == null || imageButton.isEnabled()) {
                return
            }
            runOnUiThread(object : Runnable() {
                override fun run() {
                    this@EngineActivity.btnUndo.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
                    this@EngineActivity.btnUndo.setEnabled
            this@EngineActivity.btnUndo.setClickable(true)
                }
            })
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    fun enableRedoBtn() {
        try {
            val imageButton = this.btnRedo
            if (imageButton == null || imageButton.isEnabled()) {
                return
            }
            runOnUiThread(object : Runnable() {
                override fun run() {
                    this@EngineActivity.btnRedo.setColorFilter(-1, PorterDuff.Mode.SRC_IN)
                    this@EngineActivity.btnRedo.setEnabled
            this@EngineActivity.btnRedo.setClickable(true)
                }
            })
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    fun disableRedoBtn() {
        try {
            val imageButton = this.btnRedo
            if (imageButton == null || !imageButton.isEnabled()) {
                return
            }
            runOnUiThread(object : Runnable() {
                override fun run() {
                    this@EngineActivity.btnRedo.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
                    this@EngineActivity.btnRedo.setEnabled
            this@EngineActivity.btnRedo.setClickable(false)
                }
            })
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    fun disableUndoBtn() {
        try {
            val imageButton = this.btnUndo
            if (imageButton == null || !imageButton.isEnabled()) {
                return
            }
            runOnUiThread(object : Runnable() {
                override fun run() {
                    this@EngineActivity.btnUndo.setColorFilter(-8355712, PorterDuff.Mode.SRC_IN)
                    this@EngineActivity.btnUndo.setEnabled
            this@EngineActivity.btnUndo.setClickable(false)
                }
            })
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    fun updateBtnCutState() {
        try {
            checkSplitEntity()
            checkSplitTrslEntity()
            checkSplitAudio()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    fun addEntity(str: String, str2: String, str3: String, str4: String, i: Int, i2: Int, str5: String, i3: Int, i4: Int) {
        String nameFont
        var z: Boolean = this.mTemplate.getIpad_type() == IpadType.GRADIENT.ordinal || this.mTemplate.getIpad_type() == IpadType.MASK_BRUSH.ordinal || this.mTemplate.getIpad_type() == IpadType.BLACK_LAYER.ordinal
        if (this.blurredImageView.getQuranEntities().isEmpty()) {
            nameFont = Common.FONT_QURAN
        } else {
            nameFont = this.blurredImageView.getQuranEntities()[].getNameFont()
        }
        var str6: String? = nameFont
        var quranEntity: QuranEntity? = QuranEntity(this, DrawableHelper.getIDDrawableIconByName(str5), str, str2, str3, str4, this.blurredImageView.getRectFAya(), UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/" + str6), Typeface.createFromAsset(getResources().getAssets(), "fonts/ReadexPro_Medium.ttf"), i, i2, UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/خط فارس الكوفي.otf"), this.blurredImageView.getClr_aya(), this.blurredImageView.getClr_trsl(), str6, z)
        quranEntity.setIpad_type(this.mTemplate.getIpad_type())
        quranEntity.setCanvasWH(this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height())
        quranEntity.setStartWord_index(i3)
        quranEntity.setEndWord_index(i4)
        quranEntity.setIcon(str5)
        quranEntity.setViewWeakReference(WeakReference(this.trackViewEntity), WeakReference(this.blurredImageView))
        val addTimeLineQuran = addTimeLineQuran(quranEntity)
        addTimeLineQuran.setmScaleFactor(this.trackViewEntity.getScaleFactor())
        quranEntity.setEntityQuran(addTimeLineQuran)
        addTimeLineQuran.setEntityView(quranEntity)
        this.blurredImageView.addEntity(quranEntity)
    }

    fun addTranslationEntity(str: String, i: Int, z: Boolean) {
        var translationQuranEntity: TranslationQuranEntity? = TranslationQuranEntity(str, this.blurredImageView.getRectFAya(), Typeface.createFromAsset(getResources().getAssets(), "fonts/ReadexPro_Medium.ttf"), i, InputDeviceCompat.SOURCE_ANY, "ReadexPro_Medium.ttf", this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height())
        translationQuranEntity.setIpad_type(this.mTemplate.getIpad_type())
        translationQuranEntity.setCanvasWH(this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height())
        translationQuranEntity.setViewWeakReference(WeakReference(this.trackViewEntity), WeakReference(this.blurredImageView))
        val addTimeLineTrslQuran = addTimeLineTrslQuran(translationQuranEntity)
        addTimeLineTrslQuran.setmScaleFactor(this.trackViewEntity.getScaleFactor())
        translationQuranEntity.setEntityTrslTimeline(addTimeLineTrslQuran)
        addTimeLineTrslQuran.setEntityView(translationQuranEntity)
        this.blurredImageView.addEntity(translationQuranEntity)
    }

    private fun addEntityBissmilah(str: String, f: Float, f2: Float, i: Int, transition: Transition, f3: Float, f4: Float, rectF: RectF, i2: Int) {
        RectF rectF2
        val loadFontFromAsset = UtilsFileLast.loadFontFromAsset(this, "fonts/خط البسملة.ttf")
        if (rectF == null) {
            rectF2 = this.blurredImageView.getRectFAya()
        } else {
            rectF2 = RectF(rectF.left * this.blurredImageView.getmCanvas_width(), rectF.top * this.blurredImageView.getmCanvas_height(), rectF.right * this.blurredImageView.getmCanvas_width(), rectF.bottom * this.blurredImageView.getmCanvas_height())
        }
        val bismilahEntity = BismilahEntity(str, rectF2, loadFontFromAsset, i, i2)
        bismilahEntity.setFcSize(f4)
        bismilahEntity.setFactor_scale(f3)
        bismilahEntity.setCanvasWH(this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height())
        if (bismilahEntity.getFactorSize() == 1.0f) {
            bismilahEntity.createStaticLayout()
        } else {
            bismilahEntity.setupScaleSave(bismilahEntity.getFactorSize(), this.blurredImageView.getmCanvas_width())
        }
        bismilahEntity.initPreset(i2)
        bismilahEntity.setViewWeakReference(WeakReference(this.trackViewEntity), WeakReference(this.blurredImageView))
        val addTimeLineBismilah = addTimeLineBismilah(bismilahEntity, f, f2)
        bismilahEntity.setBismilahTimeline(addTimeLineBismilah)
        addTimeLineBismilah.setTransition(transition)
        addTimeLineBismilah.setEntityView(bismilahEntity)
        this.blurredImageView.addBismilahEntity(bismilahEntity)
    }

    private fun addEntityIsti3ada(str: String, f: Float, f2: Float, i: Int, transition: Transition, f3: Float, f4: Float, rectF: RectF, i2: Int) {
        RectF rectF2
        val loadFontFromAsset = UtilsFileLast.loadFontFromAsset(this, "fonts/خط الاستعاذه.ttf")
        if (rectF == null) {
            rectF2 = this.blurredImageView.getRectFAya()
        } else {
            rectF2 = RectF(rectF.left * this.blurredImageView.getmCanvas_width(), rectF.top * this.blurredImageView.getmCanvas_height(), rectF.right * this.blurredImageView.getmCanvas_width(), rectF.bottom * this.blurredImageView.getmCanvas_height())
        }
        val bismilahEntity = BismilahEntity(str, rectF2, loadFontFromAsset, i, i2)
        bismilahEntity.setFcSize(f4)
        bismilahEntity.setFactor_scale(f3)
        bismilahEntity.setCanvasWH(this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height())
        if (bismilahEntity.getFactorSize() == 1.0f) {
            bismilahEntity.createStaticLayout()
        } else {
            bismilahEntity.setupScaleSave(bismilahEntity.getFactorSize(), this.blurredImageView.getmCanvas_width())
        }
        bismilahEntity.initPreset(i2)
        bismilahEntity.setViewWeakReference(WeakReference(this.trackViewEntity), WeakReference(this.blurredImageView))
        val addTimeLineIsti3ada = addTimeLineIsti3ada(bismilahEntity, f, f2)
        bismilahEntity.setBismilahTimeline(addTimeLineIsti3ada)
        addTimeLineIsti3ada.setTransition(transition)
        addTimeLineIsti3ada.setEntityView(bismilahEntity)
        this.blurredImageView.addIsti3adhaEntity(bismilahEntity)
    }

    fun addEntityBissmilah(): Boolean() {
        if (this.blurredImageView.getBismilahEntity() != null) {
            if (this.blurredImageView.getBismilahEntity().getBismilahTimeline().visible()) {
                return false
            }
            this.blurredImageView.getBismilahEntity().getBismilahTimeline().visible(true)
            return false
        }
        val bismilahEntity = BismilahEntity(IcyHeaders.REQUEST_HEADER_ENABLE_METADATA_VALUE, this.blurredImageView.getRectFAya(), UtilsFileLast.loadFontFromAsset(this, "fonts/خط البسملة.ttf"), this.blurredImageView.getClr_aya())
        bismilahEntity.setCanvasWH(this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height())
        bismilahEntity.setFcSize(bismilahEntity.getPaintAya().getTextSize() / this.blurredImageView.getmCanvas_width())
        bismilahEntity.setViewWeakReference(WeakReference(this.trackViewEntity), WeakReference(this.blurredImageView))
        val addTimeLineBismilah = addTimeLineBismilah(bismilahEntity)
        addTimeLineBismilah.setmScaleFactor(this.trackViewEntity.getScaleFactor())
        bismilahEntity.setBismilahTimeline(addTimeLineBismilah)
        addTimeLineBismilah.setEntityView(bismilahEntity)
        this.blurredImageView.addBismilahEntity(bismilahEntity)
        if (this.trackViewEntity.getQuran() != null) {
            this.trackViewEntity.translateToRightBismilah(addTimeLineBismilah)
        }
        return true
    }

    fun addEntityIste3adha(): Boolean() {
        if (this.blurredImageView.getmIsti3adhaEntity() != null) {
            if (this.blurredImageView.getmIsti3adhaEntity().getBismilahTimeline().visible()) {
                return false
            }
            this.blurredImageView.getmIsti3adhaEntity().getBismilahTimeline().visible(true)
            return false
        }
        val bismilahEntity = BismilahEntity("4", this.blurredImageView.getRectFAya(), UtilsFileLast.loadFontFromAsset(this, "fonts/خط الاستعاذه.ttf"), this.blurredImageView.getClr_aya())
        bismilahEntity.setCanvasWH(this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height())
        bismilahEntity.setFcSize(bismilahEntity.getPaintAya().getTextSize() / this.blurredImageView.getmCanvas_width())
        bismilahEntity.setViewWeakReference(WeakReference(this.trackViewEntity), WeakReference(this.blurredImageView))
        val addTimeLineIsti3ada = addTimeLineIsti3ada(bismilahEntity)
        addTimeLineIsti3ada.setmScaleFactor(this.trackViewEntity.getScaleFactor())
        bismilahEntity.setBismilahTimeline(addTimeLineIsti3ada)
        addTimeLineIsti3ada.setEntityView(bismilahEntity)
        this.blurredImageView.addIsti3adhaEntity(bismilahEntity)
        if (this.trackViewEntity.getQuran() != null) {
            this.trackViewEntity.translateToRightBismilah(addTimeLineIsti3ada)
        }
        return true
    }

    fun duplicateEntity(quranEntity: QuranEntity) {
        val typefaceNumber = quranEntity.getTypefaceNumber()
        if (typefaceNumber == null) {
            typefaceNumber = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/خط فارس الكوفي.otf")
        }
        val typeface = typefaceNumber
        val typeface2 = quranEntity.getPaintAya().getTypeface()
        if (typeface2 == null) {
            typeface2 = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/" + quranEntity.getNameFont())
        }
        val typeface3 = typeface2
        val typeface4 = quranEntity.getPaintTranslationAya() != null ? quranEntity.getPaintTranslationAya().getTypeface() : null
        if (typeface4 == null) {
            typeface4 = Typeface.createFromAsset(getResources().getAssets(), "fonts/ReadexPro_Medium.ttf")
        }
        var quranEntity2: QuranEntity? = QuranEntity(quranEntity.getTxt(), quranEntity.getComplete_aya(), quranEntity.getTranslation(), quranEntity.getTranslation_complete(), this.blurredImageView.getRectFAya(), typeface3, typeface4, quranEntity.getIndexNumber(), quranEntity.getNumber(), typeface, quranEntity.getClrAya(), quranEntity.getClrTrsl(), quranEntity.getNameFont(), quranEntity.getPaintAya().getTextSize(), quranEntity.getPaintTranslationAya().getTextSize(), quranEntity.getPaintAya().isUnderlineText(), quranEntity.getVectorDrawable())
        quranEntity2.setFcSize(quranEntity.getFactorSize())
        quranEntity2.setFactorSizeTrl(quranEntity.getFactorSizeTrl())
        quranEntity2.setFactor_scale(quranEntity.getFactor_scale())
        quranEntity2.setCanvasWH(this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height())
        quranEntity2.setIpad_type(this.mTemplate.getIpad_type())
        quranEntity2.setStartWord_index(quranEntity.getStartWord_index())
        quranEntity2.setEndWord_index(quranEntity.getEndWord_index())
        quranEntity2.setIcon(quranEntity.getIcon())
        quranEntity2.setViewWeakReference(WeakReference(this.trackViewEntity), WeakReference(this.blurredImageView))
        quranEntity2.setVisible(false)
        quranEntity2.setupScaleSave(quranEntity2.getFactorSize(), this.blurredImageView.getmCanvas_width())
        quranEntity2.setColor(quranEntity.getClrAya())
        quranEntity2.setColorTranslation(quranEntity.getPaintTranslationAya() != null ? quranEntity.getClrTrsl() : InputDeviceCompat.SOURCE_ANY)
        quranEntity2.initPreset(quranEntity.getmPreset())
        val addTimeLineQuran = addTimeLineQuran(quranEntity.getEntityQuran().getIndex() + 1, quranEntity2, quranEntity.getEntityQuran().getRect().right, quranEntity.getEntityQuran().getRect().right + quranEntity.getEntityQuran().getRect().width())
        addTimeLineQuran.setmScaleFactor(quranEntity.getEntityQuran().getmScaleFactor())
        quranEntity2.setEntityQuran(addTimeLineQuran)
        addTimeLineQuran.setEntityView(quranEntity2)
        if (quranEntity.getEntityQuran().getTransition() != null) {
            addTimeLineQuran.setTransition(quranEntity.getEntityQuran().getTransition().duplicate())
        }
        this.blurredImageView.addEntity(quranEntity2, quranEntity.getIndex() + 1)
        this.trackViewEntity.selectEntity(quranEntity2.getEntityQuran(), false)
        this.iTrimLineCallback.onSelectEntity(quranEntity2.getEntityQuran(), -1.0f)
        this.trackViewEntity.updateCursurToSelectEntity()
    }

    fun duplicateEntity(translationQuranEntity: TranslationQuranEntity) {
        val typeface = translationQuranEntity.getPaintAya().getTypeface()
        if (typeface == null) {
            typeface = UtilsFileLast.loadFontFromAsset(this, "fonts/" + translationQuranEntity.getNameFont())
        }
        var translationQuranEntity2: TranslationQuranEntity? = TranslationQuranEntity(translationQuranEntity.getTxt(), translationQuranEntity.getRect(), typeface, translationQuranEntity.getNumber(), translationQuranEntity.getClrAya(), translationQuranEntity.getNameFont(), translationQuranEntity.getPaintAya().getTextSize())
        translationQuranEntity2.setFcSize(translationQuranEntity.getFactorSize())
        translationQuranEntity2.setFactorSizeTrl(translationQuranEntity.getFactorSizeTrl())
        translationQuranEntity2.setFactor_scale(translationQuranEntity.getFactor_scale())
        translationQuranEntity2.setCanvasWH(this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height())
        translationQuranEntity2.setIpad_type(this.mTemplate.getIpad_type())
        translationQuranEntity2.setVisible(false)
        translationQuranEntity2.setViewWeakReference(WeakReference(this.trackViewEntity), WeakReference(this.blurredImageView))
        translationQuranEntity2.updatePaint(translationQuranEntity.getPaintAya().getTextSize(), translationQuranEntity.getStaticLayout().getWidth())
        translationQuranEntity2.setColor(translationQuranEntity.getClrAya())
        translationQuranEntity2.initPreset(translationQuranEntity.getmPreset())
        val addTimeLineQuran = addTimeLineQuran(translationQuranEntity.getEntityTrslTimeline().getIndex() + 1, translationQuranEntity2, translationQuranEntity.getEntityTrslTimeline().getRect().right, translationQuranEntity.getEntityTrslTimeline().getRect().right + translationQuranEntity.getEntityTrslTimeline().getRect().width())
        val transition = translationQuranEntity.getEntityTrslTimeline().getTransition()
        if (transition != null) {
            addTimeLineQuran.setTransition(transition.duplicate())
            if (transition.isIn() && transition.isOut()) {
                addTimeLineQuran.getTransition().setIn(false)
                transition.setOut(false)
            } else if (transition.isIn()) {
                addTimeLineQuran.getTransition().setIn(false)
            } else if (transition.isOut()) {
                transition.setOut(false)
            }
        }
        addTimeLineQuran.setmScaleFactor(translationQuranEntity.getEntityTrslTimeline().getmScaleFactor())
        translationQuranEntity2.setEntityTrslTimeline(addTimeLineQuran)
        addTimeLineQuran.setEntityView(translationQuranEntity2)
        if (translationQuranEntity.getEntityTrslTimeline().getTransition() != null) {
            addTimeLineQuran.setTransition(translationQuranEntity.getEntityTrslTimeline().getTransition().duplicate())
        }
        this.blurredImageView.addEntity(translationQuranEntity2, translationQuranEntity.getIndex() + 1)
        this.trackViewEntity.selectEntity(translationQuranEntity2.getEntityTrslTimeline(), false)
        this.iTrimLineCallback.onSelectEntity(translationQuranEntity2.getEntityTrslTimeline(), -1.0f)
        this.trackViewEntity.updateCursurToSelectEntity()
    }

    fun splitEntity(translationQuranEntity: TranslationQuranEntity) {
        var abs: Float = Math.abs(this.trackViewEntity.getXCursur())
        if (abs <= translationQuranEntity.getEntityTrslTimeline().getRect().left || abs >= translationQuranEntity.getEntityTrslTimeline().getRect().right) {
            return
        }
        var second_in_screen: Float = this.trackViewEntity.getSecond_in_screen() * 0.2f
        if (abs <= translationQuranEntity.getEntityTrslTimeline().getRect().left || abs >= translationQuranEntity.getEntityTrslTimeline().getRect().left + second_in_screen) {
            if (abs >= translationQuranEntity.getEntityTrslTimeline().getRect().right || abs <= translationQuranEntity.getEntityTrslTimeline().getRect().right - second_in_screen) {
                val typeface = translationQuranEntity.getPaintAya().getTypeface()
                if (typeface == null) {
                    typeface = UtilsFileLast.loadFontFromAsset(this, "fonts/" + translationQuranEntity.getNameFont())
                }
                var translationQuranEntity2: TranslationQuranEntity? = TranslationQuranEntity(translationQuranEntity.getTxt(), translationQuranEntity.getRect(), typeface, translationQuranEntity.getNumber(), translationQuranEntity.getClrAya(), translationQuranEntity.getNameFont(), translationQuranEntity.getPaintAya().getTextSize())
                translationQuranEntity2.setFcSize(translationQuranEntity.getFactorSize())
                translationQuranEntity2.setFactorSizeTrl(translationQuranEntity.getFactorSizeTrl())
                translationQuranEntity2.setFactor_scale(translationQuranEntity.getFactor_scale())
                translationQuranEntity2.setCanvasWH(this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height())
                translationQuranEntity2.setIpad_type(this.mTemplate.getIpad_type())
                translationQuranEntity2.setViewWeakReference(WeakReference(this.trackViewEntity), WeakReference(this.blurredImageView))
                translationQuranEntity2.updatePaint(translationQuranEntity.getPaintAya().getTextSize(), translationQuranEntity.getStaticLayout().getWidth())
                translationQuranEntity2.setColor(translationQuranEntity.getClrAya())
                translationQuranEntity2.initPreset(translationQuranEntity.getmPreset())
                this.trackViewEntity.stackSplit(translationQuranEntity.getEntityTrslTimeline())
                val splitTimeLineQuran = splitTimeLineQuran(translationQuranEntity.getEntityTrslTimeline().getIndex() + 1, translationQuranEntity2, Math.abs(this.trackViewEntity.getCurrentPosition()), translationQuranEntity.getEntityTrslTimeline().getRect().right, translationQuranEntity.getEntityTrslTimeline().getmScaleFactor())
                val transition = translationQuranEntity.getEntityTrslTimeline().getTransition()
                if (transition != null) {
                    splitTimeLineQuran.setTransition(transition.duplicate())
                    if (transition.isIn() && transition.isOut()) {
                        splitTimeLineQuran.getTransition().setIn(false)
                        transition.setOut(false)
                    } else if (transition.isIn()) {
                        splitTimeLineQuran.getTransition().setIn(false)
                    } else if (transition.isOut()) {
                        transition.setOut(false)
                    }
                }
                translationQuranEntity.getEntityTrslTimeline().setCurrentRect()
                translationQuranEntity.getEntityTrslTimeline().setRight(Math.abs(this.trackViewEntity.getCurrentPosition()))
                translationQuranEntity.getEntityTrslTimeline().onChange()
                translationQuranEntity2.setEntityTrslTimeline(splitTimeLineQuran)
                splitTimeLineQuran.setEntityView(translationQuranEntity2)
                if (translationQuranEntity.getEntityTrslTimeline().getTransition() != null) {
                    splitTimeLineQuran.setTransition(translationQuranEntity.getEntityTrslTimeline().getTransition().duplicate())
                }
                this.blurredImageView.addEntity(translationQuranEntity2, translationQuranEntity.getIndex() + 1)
                this.trackViewEntity.invalidate()
            }
        }
    }

    fun splitEntity(quranEntity: QuranEntity) {
        var abs: Float = Math.abs(this.trackViewEntity.getXCursur())
        if (abs <= quranEntity.getEntityQuran().getRect().left || abs >= quranEntity.getEntityQuran().getRect().right) {
            return
        }
        var second_in_screen: Float = this.trackViewEntity.getSecond_in_screen() * 0.2f
        if (abs <= quranEntity.getEntityQuran().getRect().left || abs >= quranEntity.getEntityQuran().getRect().left + second_in_screen) {
            if (abs >= quranEntity.getEntityQuran().getRect().right || abs <= quranEntity.getEntityQuran().getRect().right - second_in_screen) {
                val typefaceNumber = quranEntity.getTypefaceNumber()
                if (typefaceNumber == null) {
                    typefaceNumber = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/خط فارس الكوفي.otf")
                }
                val typeface = typefaceNumber
                val typeface2 = quranEntity.getPaintAya().getTypeface()
                if (typeface2 == null) {
                    typeface2 = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/" + quranEntity.getNameFont())
                }
                val typeface3 = typeface2
                val typeface4 = quranEntity.getPaintTranslationAya() != null ? quranEntity.getPaintTranslationAya().getTypeface() : null
                if (typeface4 == null) {
                    typeface4 = Typeface.createFromAsset(getResources().getAssets(), "fonts/ReadexPro_Medium.ttf")
                }
                var quranEntity2: QuranEntity? = QuranEntity(quranEntity.getTxt(), quranEntity.getComplete_aya(), quranEntity.getTranslation(), quranEntity.getTranslation_complete(), this.blurredImageView.getRectFAya(), typeface3, typeface4, quranEntity.getIndexNumber(), quranEntity.getNumber(), typeface, quranEntity.getClrAya(), quranEntity.getClrTrsl(), quranEntity.getNameFont(), quranEntity.getPaintAya().getTextSize(), quranEntity.getPaintTranslationAya().getTextSize(), quranEntity.getPaintAya().isUnderlineText(), quranEntity.getVectorDrawable())
                quranEntity2.setFcSize(quranEntity.getFactorSize())
                quranEntity2.setFactorSizeTrl(quranEntity.getFactorSizeTrl())
                quranEntity2.setFactor_scale(quranEntity.getFactor_scale())
                quranEntity2.setCanvasWH(this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height())
                quranEntity2.setIpad_type(this.mTemplate.getIpad_type())
                quranEntity2.setStartWord_index(quranEntity.getStartWord_index())
                quranEntity2.setEndWord_index(quranEntity.getEndWord_index())
                quranEntity2.setIcon(quranEntity.getIcon())
                quranEntity2.setViewWeakReference(WeakReference(this.trackViewEntity), WeakReference(this.blurredImageView))
                quranEntity2.setupScaleSave(quranEntity2.getFactorSize(), this.blurredImageView.getmCanvas_width())
                quranEntity2.setColor(quranEntity.getClrAya())
                quranEntity2.setColorTranslation(quranEntity.getPaintTranslationAya() != null ? quranEntity.getClrTrsl() : InputDeviceCompat.SOURCE_ANY)
                quranEntity2.initPreset(quranEntity.getmPreset())
                this.trackViewEntity.stackSplit(quranEntity.getEntityQuran())
                val splitTimeLineQuran = splitTimeLineQuran(quranEntity.getEntityQuran().getIndex() + 1, quranEntity2, Math.abs(this.trackViewEntity.getCurrentPosition()), quranEntity.getEntityQuran().getRect().right, quranEntity.getEntityQuran().getmScaleFactor())
                val transition = quranEntity.getEntityQuran().getTransition()
                if (transition != null) {
                    splitTimeLineQuran.setTransition(transition.duplicate())
                    if (transition.isIn() && transition.isOut()) {
                        splitTimeLineQuran.getTransition().setIn(false)
                        transition.setOut(false)
                    } else if (transition.isIn()) {
                        splitTimeLineQuran.getTransition().setIn(false)
                    } else if (transition.isOut()) {
                        transition.setOut(false)
                    }
                }
                quranEntity.getEntityQuran().setCurrentRect()
                quranEntity.getEntityQuran().setRight(Math.abs(this.trackViewEntity.getCurrentPosition()))
                quranEntity.getEntityQuran().onChange()
                quranEntity2.setEntityQuran(splitTimeLineQuran)
                splitTimeLineQuran.setEntityView(quranEntity2)
                if (quranEntity.getEntityQuran().getTransition() != null) {
                    splitTimeLineQuran.setTransition(quranEntity.getEntityQuran().getTransition().duplicate())
                }
                this.blurredImageView.addEntity(quranEntity2, quranEntity.getIndex() + 1)
                this.trackViewEntity.invalidate()
            }
        }
    }

    private fun addEntity(str: String, str2: String, str3: String, str4: String, f: Float, f2: Float, i: Int, i2: Int, i3: Int, str5: String, transition: Transition, z: Boolean, str6: String, i4: Int, i5: Int, f3: Float, f4: Float, f5: Float, rectF: RectF, typeface: Typeface, typeface2: Typeface, i6: Int, i7: Int) {
        RectF rectF2
        var str7: String? = str6 == null ? "hafes" : str6
        val loadFontFromAsset = UtilsFileLast.loadFontFromAsset(this, "fonts/arabic/" + str5)
        if (rectF == null) {
            rectF2 = this.blurredImageView.getRectFAya()
        } else {
            rectF2 = RectF(rectF.left * this.blurredImageView.getmCanvas_width(), rectF.top * this.blurredImageView.getmCanvas_height(), rectF.right * this.blurredImageView.getmCanvas_width(), rectF.bottom * this.blurredImageView.getmCanvas_height())
        }
        var quranEntity: QuranEntity? = QuranEntity(this, str, str2, str3, str4, rectF2, loadFontFromAsset, typeface2, i, i2, typeface, i3, i6, str5, z, DrawableHelper.getIDDrawableIconByName(str7))
        quranEntity.setFcSize(f4)
        quranEntity.setFactorSizeTrl(f5)
        quranEntity.setCanvasWH(this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height())
        quranEntity.setFactor_scale(f3)
        quranEntity.setIpad_type(this.mTemplate.getIpad_type())
        quranEntity.setStartWord_index(i4)
        quranEntity.setEndWord_index(i5)
        quranEntity.setIcon(str7)
        quranEntity.setViewWeakReference(WeakReference(this.trackViewEntity), WeakReference(this.blurredImageView))
        if (quranEntity.getFactorSize() == 1.0f) {
            quranEntity.setTextSize(quranEntity.calculateTextSize())
        } else {
            quranEntity.setupScaleSave(quranEntity.getFactorSize(), this.blurredImageView.getmCanvas_width())
        }
        quranEntity.initPreset(i7)
        val addTimeLineQuran = addTimeLineQuran(quranEntity, f, f2)
        quranEntity.setEntityQuran(addTimeLineQuran)
        addTimeLineQuran.setTransition(transition)
        addTimeLineQuran.setEntityView(quranEntity)
        this.blurredImageView.addEntity(quranEntity)
    }

    private fun addEntityTrsl(str: String, f: Float, f2: Float, i: Int, i2: Int, str2: String, transition: Transition, f3: Float, f4: Float, rectF: RectF, i3: Int, i4: Int, z: Boolean) {
        RectF rectF2
        val loadFontFromAsset = UtilsFileLast.loadFontFromAsset(this, "fonts/" + str2)
        if (rectF == null) {
            rectF2 = this.blurredImageView.getRectFAya()
        } else {
            rectF2 = RectF(rectF.left * this.blurredImageView.getmCanvas_width(), rectF.top * this.blurredImageView.getmCanvas_height(), rectF.right * this.blurredImageView.getmCanvas_width(), rectF.bottom * this.blurredImageView.getmCanvas_height())
        }
        var translationQuranEntity: TranslationQuranEntity? = TranslationQuranEntity(this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height(), str, rectF2, loadFontFromAsset, i, i2, str2)
        translationQuranEntity.setHaveBg(z)
        translationQuranEntity.setClrBg(i4)
        translationQuranEntity.setFcSize(f4)
        translationQuranEntity.setCanvasWH(this.blurredImageView.getmCanvas_width(), this.blurredImageView.getmCanvas_height())
        translationQuranEntity.setFactor_scale(f3)
        translationQuranEntity.setIpad_type(this.mTemplate.getIpad_type())
        translationQuranEntity.setViewWeakReference(WeakReference(this.trackViewEntity), WeakReference(this.blurredImageView))
        if (translationQuranEntity.getFactorSize() == 1.0f) {
            translationQuranEntity.setTextSize(translationQuranEntity.calculateTextSize())
        } else {
            translationQuranEntity.setupScaleSave(translationQuranEntity.getFactorSize(), this.blurredImageView.getmCanvas_width())
        }
        translationQuranEntity.initPreset(i3)
        val addTimeLineQuran = addTimeLineQuran(translationQuranEntity, f, f2)
        translationQuranEntity.setEntityTrslTimeline(addTimeLineQuran)
        addTimeLineQuran.setTransition(transition)
        addTimeLineQuran.setEntityView(translationQuranEntity)
        this.blurredImageView.addEntity(translationQuranEntity)
    }

    fun addAudioReciters(list: List<RecitersModel>) {
        val newSingleThreadExecutor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        newSingleThreadExecutor.execute(object : Runnable() { // from class: Runnable
            override
            void run() {
                this@EngineActivity.list, handler)
            }
        })
    }

 fun m638xadfdcdf5(list: List, handler: Handler) {
        val arrayList = mutableListOf()
        val arrayList2 = mutableListOf()
        val sb = StringBuilder()
        try {
            val it = list.iterator()
            var i: Int = 0
            while (it.hasNext()) {
                val recitersModel = (RecitersModel) it.next()
                try {
                    var str: String? = recitersModel.isTarteel() ? "https://audio-cdn.tarteel.ai/quran/" + recitersModel.getIdentifer() + "/" + recitersModel.getSurah_index() + recitersModel.getNumber_aya() + ".mp3" : "https://everyayah.com/data/" + recitersModel.getIdentifer() + "/" + recitersModel.getSurah_index() + recitersModel.getNumber_aya() + ".mp3"
                    var downloadFile: String? = AudioUtils.downloadFile(this, str, this.mTemplate.getFolder_template())
                    if (downloadFile != null) {
                        arrayList.add(downloadFile)
                        arrayList2.add(str)
                        sb.append("file '").append(downloadFile.replace("'", "\\'")).append("'\n")
                        i++
                        try {
                            handler.post(object : Runnable() { // from class: Runnable
                                override
                                void run() {
                                    this@EngineActivity.i, list)
                                }
                            })
                        } catch (Exception e) {
                            e = e
                            e.printStackTrace()
                        }
                    }
                } catch (Exception e2) {
                    e = e2
                }
            }
            var file = File(this.mTemplate.getFolder_template(), "concat_" + System.currentTimeMillis() + ".txt")
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(sb.toString().getBytes())
            fileOutputStream.close()
            var file2 = File(this.mTemplate.getFolder_template(), System.currentTimeMillis() + "_output.mp3")
            var file3 = File(this.mTemplate.getFolder_template(), System.currentTimeMillis() + "_output.pcm")
            val arrayList3 = mutableListOf()
            arrayList3.add("-f")
            arrayList3.add("concat")
            arrayList3.add("-safe")
            arrayList3.add("0")
            arrayList3.add("-i")
            arrayList3.add(file.getAbsolutePath())
            arrayList3.add("-map")
            arrayList3.add("0:a")
            arrayList3.add("-c")
            arrayList3.add("copy")
            arrayList3.add(file2.getAbsolutePath())
            arrayList3.add("-map")
            arrayList3.add("0:a")
            arrayList3.add("-ac")
            arrayList3.add(IcyHeaders.REQUEST_HEADER_ENABLE_METADATA_VALUE)
            arrayList3.add("-ar")
            arrayList3.add("44100")
            arrayList3.add("-f")
            arrayList3.add("s16le")
            arrayList3.add(file3.getAbsolutePath())
            arrayList3.add("-y")
            val strArr = arrayList3.toTypedArray()
            handler.post(object : Runnable() { // from class: Runnable
                override
                void run() {
                    this@EngineActivity.strArr, file2, arrayList2, file3)
                }
            })
        } catch (Exception e3) {
            e3.printStackTrace()
            handler.post(object : Runnable() { // from class: Runnable
                override
                void run() {
                    this@EngineActivity.)
                }
            })
        }
    }

 fun m634xa24c2879(i: Int, list: List) {
        updateProgress(i, list.size)
    }

 fun m636x2824fb37(strArr: Array<String>, file: File, list: List, file2: File) {
        this.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(strArr, object : FFmpegSessionCompleteCallback() { // from class: Runnable
            override
            void apply(FFmpegSession fFmpegSession) {
                this@EngineActivity.file, list, file2, fFmpegSession)
            }
        }).getSessionId()))
    }

 fun m635x653891d8(file: File, list: List, file2: File, fFmpegSession: FFmpegSession) {
        if (ReturnCode.isSuccess(fFmpegSession.getReturnCode())) {
            addAudio(Uri.fromFile(file), list, -1, file2.getAbsolutePath())
        } else {
            Log.e("FFMPEG", "Failed: " + fFmpegSession.getFailStackTrace())
        }
    }

 fun m637xeb116496() {
        hideProgressFragment()
        hideFragment()
    }

    fun addAudioRecitersTemplate(list: List<String>, i: Int, str: String) {
        Executors.newSingleThreadExecutor().execute(AnonymousClass66(list, i, str))
    }

    private inner class AnonymousClass66 : Runnable {
        int val$index
        String val$path_video
        List val$pathes

        AnonymousClass66(List list, int i, String str) {
            this.val$pathes = list
            this.val$index = i
            this.val$path_video = str
        }

        /* JADX WARN: Removed duplicated region for block: B:13:0x0061 A[SYNTHETIC] */
        /* JADX WARN: Removed duplicated region for block: B:17:0x001d A[SYNTHETIC] */
        override
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        fun run() {
            String downloadFile
            try {
                mutableListOf()
                mutableListOf()
                val sb = StringBuilder()
                val it = this.val$pathes.iterator()
                var i: Int = 0
                while (it.hasNext()) {
                    var parse: Uri? = Uri.parse((String) it.next())
                    var uri: String? = parse.toString()
                    if (!uri.startsWith("http://") && !uri.startsWith("https://")) {
                        val engineActivity = this@EngineActivity
                        downloadFile = AudioUtils.copyFromUri(engineActivity, parse, engineActivity.mTemplate.getFolder_template())
                        if (downloadFile == null) {
                            sb.append("file '").append(downloadFile.replace("'", "\\'")).append("'\n")
                            i++
                            this@EngineActivity.updateProgress(i, this.val$pathes.size)
                        }
                    }
                    val engineActivity2 = this@EngineActivity
                    downloadFile = AudioUtils.downloadFile(engineActivity2, uri, engineActivity2.mTemplate.getFolder_template())
                    if (downloadFile == null) {
                    }
                }
                var file = File(this@EngineActivity.mTemplate.getFolder_template(), "concat.txt")
                val fileOutputStream = FileOutputStream(file)
                fileOutputStream.write(sb.toString().getBytes())
                fileOutputStream.close()
                var file2 = File(this@EngineActivity.mTemplate.getFolder_template(), System.currentTimeMillis() + "_output.mp3")
                var file3 = File(this@EngineActivity.mTemplate.getFolder_template(), System.currentTimeMillis() + "_output.pcm")
                val arrayList = mutableListOf()
                arrayList.add("-f")
                arrayList.add("concat")
                arrayList.add("-safe")
                arrayList.add("0")
                arrayList.add("-i")
                arrayList.add(file.getAbsolutePath())
                arrayList.add("-map")
                arrayList.add("0:a")
                arrayList.add("-c")
                arrayList.add("copy")
                arrayList.add(file2.getAbsolutePath())
                arrayList.add("-map")
                arrayList.add("0:a")
                arrayList.add("-ac")
                arrayList.add(IcyHeaders.REQUEST_HEADER_ENABLE_METADATA_VALUE)
                arrayList.add("-ar")
                arrayList.add("44100")
                arrayList.add("-f")
                arrayList.add("s16le")
                arrayList.add(file3.getAbsolutePath())
                arrayList.add("-y")
                this@EngineActivity.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayList.toTypedArray(), object : FFmpegSessionCompleteCallback() {.1
                    override fun apply(fFmpegSession: FFmpegSession) {
                        if (ReturnCode.isSuccess(fFmpegSession.getReturnCode())) {
                            if (AnonymousClass66.this.val$index >= 0 && AnonymousClass66.this.val$index < this@EngineActivity.mTemplate.getEntityMediaList().size) {
                                val entityMedia = this@EngineActivity.mTemplate.getEntityMediaList().get(AnonymousClass66.this.val$index)
                                if (entityMedia.isApplyEffectInPreview()) {
                                    var file4 = File(this@EngineActivity.mTemplate.getFolder_template(), System.currentTimeMillis() + "_audio_echo.mp3")
                                    val effectAudio = entityMedia.getEffectAudio()
                                    var start: Float = effectAudio.getStart() / 1000.0f
                                    var end: Float = effectAudio.getEnd() / 1000.0f
                                    val arrayList2 = mutableListOf()
                                    arrayList2.add("atrim=start=" + start + ":end=" + end)
                                    arrayList2.add("asetpts=N/SR/TB")
                                    if (effectAudio.isRemoveNoice()) {
                                        arrayList2.add("afftdn=nf=-25")
                                    }
                                    arrayList2.add(String.format(Locale.US, "volume=%.2f", Float.valueOf(effectAudio.getVolume())))
                                    if (effectAudio.getFade_in() > 0) {
                                        arrayList2.add("afade=t=in:st=0:d=" + effectAudio.getFade_in())
                                    }
                                    if (effectAudio.getFade_out() > 0) {
                                        var fade_out: Float = effectAudio.getFade_out()
                                        arrayList2.add("afade=t=out:st=" + ((end - start) - fade_out) + ":d=" + fade_out)
                                    }
                                    if (effectAudio.isEnhance()) {
                                        arrayList2.add(Common.ENHANCE_CMD)
                                    }
                                    if (effectAudio.getReverbPreset() != null) {
                                        arrayList2.add(effectAudio.getReverbPreset())
                                    }
                                    if (effectAudio.getDecays() > 0) {
                                        arrayList2.add(String.format(Locale.US, "aecho=%.2f:%.2f:%s:%s", Float.valueOf(1.0f), Float.valueOf(effectAudio.getOutGain()), effectAudio.getDelays_cmd(), effectAudio.getDecays_cmd()))
                                    }
                                    if (effectAudio.getSpeed() != 1.0f) {
                                        arrayList2.addAll(this@EngineActivity.buildSpeedFilters(effectAudio.getSpeed()))
                                    }
                                    this@EngineActivity.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", file2.getAbsolutePath(), "-af", TextUtils.join(",", arrayList2), "-y", file4.getAbsolutePath()), object : FFmpegSessionCompleteCallback() {.1.1
                                        override fun apply(fFmpegSession2: FFmpegSession) {
                                            this@EngineActivity.addAudioTemplate(Uri.fromFile(file4), AnonymousClass66.this.val$pathes, AnonymousClass66.this.val$index, file2.getAbsolutePath(), file3.getAbsolutePath(), AnonymousClass66.this.val$path_video)
                                        }
                                    }).getSessionId()))
                                    return
                                }
                            }
                            this@EngineActivity.addAudioTemplate(Uri.fromFile(file2), AnonymousClass66.this.val$pathes, AnonymousClass66.this.val$index, file2.getAbsolutePath(), file3.getAbsolutePath(), AnonymousClass66.this.val$path_video)
                        }
                    }
                }).getSessionId()))
            } catch (Exception e) {
                this@EngineActivity.hideProgressFragment()
                this@EngineActivity.hideFragment()
                e.printStackTrace()
            }
        }
    }

    fun dialogCopyRight() {
        try {
            var dialog = Dialog(this)
            this.dialog = dialog
            dialog.setCancelable(false)
            this.dialog.requestWindowFeature(1)
            this.dialog.getWindow().setLayout(-1, -2)
            this.dialog.getWindow().setBackgroundDrawable(ColorDrawable(0))
            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog_copyright, (ViewGroup) null)
            this.dialog.setContentView(inflate)
            val textCustumFontBold = (TextCustumFontBold) inflate.findViewById(R.id.dialog_title)
            val textCustumFont = (TextCustumFont) inflate.findViewById(R.id.tv_msj)
            inflate.findViewById(R.id.dialog_no).setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    this@EngineActivity.cancelDialog()
                }
            })
            if (LocaleHelper.getLanguage(this).equals("ar")) {
                textCustumFontBold.setText("تنبيه حقوق الاستخدام ⚠️")
                textCustumFont.setText("بعض تسجيلات تلاوات القرّاء محمية بحقوق النشر، وهي مخصّصة للاستخدام الشخصي فقط.\n\nقد تسمح بعض المنصات باستخدام هذه الأصوات دون مشاكل، لكن ذلك لا يُعدّ تصريحًا بالنشر أو الاستخدام التجاري.\n\nللنشر الآمن، يُرجى اختيار قارئ مذكور على أنه مسموح بالنشر أو استخدام صوتك الخاص.\n\nالمستخدم مسؤول بالكامل عن الالتزام بسياسات حقوق النشر الخاصة بكل منصة.")
            } else {
                textCustumFontBold.setText("⚠️ Copyright Notice")
                textCustumFont.setText("Some reciters’ audio recordings are protected by copyright and are intended for personal use only.\n\nCertain platforms may allow these sounds without issues, but this does not constitute permission to publish or use them commercially.\n\nFor safe publishing, please select a reciter marked as allowed for publishing or use your own audio.\n\nThe user is solely responsible for complying with the copyright policies of each platform.")
            }
            this.dialog.show()
            MyPrefereces.putVuCopyRight(this)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    fun updateEndViewTime(i: Int) {
        String str
        var j: Long = i
        var seconds: Long = TimeUnit.MILLISECONDS.toSeconds(j) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(j))
        if (seconds < 10) {
            str = TimeUnit.MILLISECONDS.toMinutes(j) + ":0" + seconds
        } else {
            str = TimeUnit.MILLISECONDS.toMinutes(j) + ":" + seconds
        }
        this.tv_endTime.setText("/" + str)
    }

    fun updateStartViewTime(i: Int) {
        String str
        var j: Long = i
        var seconds: Long = TimeUnit.MILLISECONDS.toSeconds(j) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(j))
        if (seconds < 10) {
            str = TimeUnit.MILLISECONDS.toMinutes(j) + ":0" + seconds
        } else {
            str = TimeUnit.MILLISECONDS.toMinutes(j) + ":" + seconds
        }
        this.tv_currentTime.setText(str)
    }

    fun updateViewTime(i: Int, i2: Int) {
        String str
        String str2
        var j: Long = i2
        var seconds: Long = TimeUnit.MILLISECONDS.toSeconds(j) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(j))
        if (seconds < 10) {
            str = TimeUnit.MILLISECONDS.toMinutes(j) + ":0" + seconds
        } else {
            str = TimeUnit.MILLISECONDS.toMinutes(j) + ":" + seconds
        }
        var j2: Long = i
        var seconds2: Long = TimeUnit.MILLISECONDS.toSeconds(j2) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(j2))
        if (seconds2 < 10) {
            str2 = TimeUnit.MILLISECONDS.toMinutes(j2) + ":0" + seconds2
        } else {
            str2 = TimeUnit.MILLISECONDS.toMinutes(j2) + ":" + seconds2
        }
        this.tv_currentTime.setText(str)
        this.tv_endTime.setText("/" + str2)
    }

    fun hideLayoutResolution() {
        val linearLayout = this.layout_resolution
        if (linearLayout == null || linearLayout.getVisibility() != 0) {
            return
        }
        this.layout_resolution.post(object : Runnable() {
            override fun run() {
                this@EngineActivity.layout_resolution.setVisibility(8)
            }
        })
    }

    fun hideFragment() {
        try {
            if (!isFinishing() && !getSupportFragmentManager().isDestroyed()) {
                val supportFragmentManager = getSupportFragmentManager()
                val beginTransaction = supportFragmentManager.beginTransaction()
                val findFragmentById = supportFragmentManager.findFragmentById(R.id.m_container)
                if (findFragmentById != null) {
                    beginTransaction.remove(findFragmentById)
                }
                beginTransaction.commit()
                setupHideFragment()
            }
        } catch (Exception unused) {
        }
        this.mCurrentFragment = null
    }

    fun showProgress() {
        try {
            setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
            setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)
            findViewById(R.id.container_progress).setVisibility(0)
            if (isFinishing() || getSupportFragmentManager().isDestroyed()) {
                return
            }
            val beginTransaction = getSupportFragmentManager().beginTransaction()
            beginTransaction.replace(R.id.container_progress, ProgressViewFragment.getInstance())
            beginTransaction.commit()
        } catch (Exception unused) {
        }
    }

    fun showProgressSimple() {
        try {
            findViewById(R.id.container_progress).setVisibility(0)
            if (isFinishing() || getSupportFragmentManager().isDestroyed()) {
                return
            }
            val beginTransaction = getSupportFragmentManager().beginTransaction()
            beginTransaction.replace(R.id.container_progress, SimpleProgressViewFragment.getInstance())
            beginTransaction.commit()
        } catch (Exception unused) {
        }
    }

    fun hideProgressFragment() {
        try {
            setStatusBarColor(-15658735)
            setNavigationBarColor(-14803426)
            if (!isFinishing() && !getSupportFragmentManager().isDestroyed()) {
                val supportFragmentManager = getSupportFragmentManager()
                val beginTransaction = supportFragmentManager.beginTransaction()
                val findFragmentById = supportFragmentManager.findFragmentById(R.id.container_progress)
                if (findFragmentById != null) {
                    beginTransaction.remove(findFragmentById)
                }
                beginTransaction.commit()
            }
            findViewById(R.id.container_progress).setVisibility(8)
        } catch (Exception unused) {
        }
    }

    fun toCrop() {
        this.isSaveTmpTemplate = false
        this.isToCrop = true
        Common.bitmap = this.blurredImageView.getBitmapOriginal()
        Common.rect = this.blurredImageView.getRectSquare()
        if (this.blurredImageView.getBitmapSquare() != null) {
            Common.MIN_SQUARE_W = this.blurredImageView.getBitmapSquare().getWidth()
            Common.MIN_SQUARE_H = this.blurredImageView.getBitmapSquare().getHeight()
        }
        Common.radius = this.blurredImageView.getRadius_square()
        this.launchCropActivity.launch(Intent(this, CropBitmapActivity::class.java))
    }

    fun dialogWatermark() {
        try {
            if (this.dialog != null) {
                cancelDialog()
            }
            this.isSaveTmpTemplate = false
            this.isToCrop = true
            var dialog = Dialog(this)
            this.dialog = dialog
            dialog.setCancelable(true)
            this.dialog.requestWindowFeature(1)
            this.dialog.getWindow().setLayout(-1, -2)
            this.dialog.getWindow().setBackgroundDrawable(ColorDrawable(0))
            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog, (ViewGroup) null)
            this.dialog.setContentView(inflate)
            inflate.findViewById(R.id.dialog_title).setVisibility(8)
            inflate.findViewById(R.id.img_pro).setVisibility(0)
            val textCustumFont = (TextCustumFont) inflate.findViewById(R.id.dialog_message)
            textCustumFont.setText(this.mResources.getString(R.string.do_want_delete_watermark))
            textCustumFont.setGravity(17)
            var buttonCustumFont = (ButtonCustumFont) inflate.findViewById(R.id.dialog_no)
            buttonCustumFont.setText(this.mResources.getString(R.string.no))
            buttonCustumFont.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    this@EngineActivity.cancelDialog()
                }
            })
            var buttonCustumFont2 = (ButtonCustumFont) inflate.findViewById(R.id.dialog_yes)
            buttonCustumFont2.setText(this.mResources.getString(R.string.yes))
            buttonCustumFont2.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    this@EngineActivity.toProVersion()
                    this@EngineActivity.cancelDialog()
                }
            })
            this.dialog.show()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    void /* billing removed */ {
        try {
            if (this.dialog != null) {
                cancelDialog()
            }
            this.isSaveTmpTemplate = false
            var dialog = Dialog(this)
            this.dialog = dialog
            dialog.setCancelable(true)
            this.dialog.requestWindowFeature(1)
            this.dialog.getWindow().setLayout(-1, -2)
            this.dialog.getWindow().setBackgroundDrawable(ColorDrawable(0))
            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog, (ViewGroup) null)
            this.dialog.setContentView(inflate)
            inflate.findViewById(R.id.dialog_title).setVisibility(8)
            inflate.findViewById(R.id.img_pro).setVisibility(0)
            val textCustumFont = (TextCustumFont) inflate.findViewById(R.id.dialog_message)
            textCustumFont.setText(this.mResources.getString(R.string.unlock_premium))
            textCustumFont.setGravity(17)
            var buttonCustumFont = (ButtonCustumFont) inflate.findViewById(R.id.dialog_no)
            buttonCustumFont.setText(this.mResources.getString(R.string.no))
            buttonCustumFont.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    this@EngineActivity.cancelDialog()
                }
            })
            var buttonCustumFont2 = (ButtonCustumFont) inflate.findViewById(R.id.dialog_yes)
            buttonCustumFont2.setText(this.mResources.getString(R.string.yes))
            buttonCustumFont2.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    this@EngineActivity.toProVersion()
                    this@EngineActivity.cancelDialog()
                }
            })
            this.dialog.show()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    private inner class AnonymousClass77 : ChangeBgFragment.IChangeBgCallback {
        object : ChangeBgFragment.IChangeBgCallback {} {
        }

        override fun onSubscribe() {
            /* billing removed */
        }

        override fun onCrop() {
            this@EngineActivity.toCrop()
        }

        override fun onAdd(bgItem: BgItem) {
            if (bgItem.getName_drawable().equals(this@EngineActivity.mTemplate.getName_drawable())) {
                return
            }
            if (ChangeBgFragment.instance != null) {
                ChangeBgFragment.instance.scrollToSelected()
            }
            this@EngineActivity.mTemplate.setName_drawable(bgItem.getName_drawable())
            this@EngineActivity.uri_bg = "android.resource://" + this@EngineActivity.getPackageName() + "/drawable/" + bgItem.getId()
            this@EngineActivity.showProgressSimple()
            this@EngineActivity.executor.execute(object : Runnable {
                /* JADX WARN: Multi-variable type inference failed */
                override fun run() {
                    EngineActivity engineActivity
                    Runnable runnable
                    Bitmap cropTo16x9
                    Bitmap bitmap
                    Bitmap bitmap2
                    Rect rect
                    try {
                        try {
                            try {
                                this@EngineActivity.mTemplate.setUri_bg(this@EngineActivity.uri_bg)
                                var i: Int = 0
                                this@EngineActivity.mTemplate.setVideoSquare
            this@EngineActivity.blurredImageView.setVideo(false)
                                var height: Int = this@EngineActivity.blurredImageView.getHeight()
                                this@EngineActivity.blurredImageView.setBitmapOriginal(Glide as Bitmap.with(this@EngineActivity).asBitmap().load(this@EngineActivity.uri_bg).override(height, height).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).submit().get())
                                if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                                    cropTo16x9 = BitmapCropper.cropTo9x16(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                                } else if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SQUARE.ordinal) {
                                    cropTo16x9 = BitmapCropper.cropTo1x1(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                                } else {
                                    cropTo16x9 = BitmapCropper.cropTo16x9(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                                }
                                this@EngineActivity.blurredImageView.updatePosCanvas
            this@EngineActivity.blurredImageView.updateIpad(cropTo16x9, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize())
                                if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_NEOMORPHIC.ordinal) {
                                    var width: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.6f)
                                    var round: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                                    var round2: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                                    var i2: Int = width + round
                                    if (i2 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                        round -= i2 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                        i2 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    }
                                    var i3: Int = width + round2
                                    if (i3 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                        round2 -= i3 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                        i3 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    }
                                    if (round < 0) {
                                        round = 0
                                    }
                                    if (round2 >= 0) {
                                        i = round2
                                    }
                                    var rect2 = Rect(round, i, i2, i3)
                                    this@EngineActivity.blurredImageView.setRadius_square(width)
                                    var width2: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                    var height2: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                    var cropToSquareWithRoundCorners: Bitmap? = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect2, width, width2, height2)
                                    rect2.right = rect2.left + width2
                                    rect2.bottom = rect2.top + height2
                                    this@EngineActivity.blurredImageView.setRectSquare(rect2)
                                    bitmap2 = cropToSquareWithRoundCorners
                                    rect = rect2
                                } else {
                                    if (this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal) {
                                        var width3: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 1.0f)
                                        var height3: Int = (cropTo16x9.getHeight() * 0.5355f)
                                        var round3: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                                        var round4: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                                        var i4: Int = width3 + round3
                                        if (i4 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                            round3 -= i4 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                            i4 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                        }
                                        var i5: Int = height3 + round4
                                        if (i5 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                            round4 -= i5 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                            i5 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                        }
                                        if (round3 < 0) {
                                            round3 = 0
                                        }
                                        if (round4 < 0) {
                                            round4 = 0
                                        }
                                        var rect3 = Rect(round3, round4, i4, i5)
                                        var width4: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                        var height4: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                        var cropToSquare: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect3, width4, height4)
                                        this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                                        rect3.right = rect3.left + width4
                                        rect3.bottom = rect3.top + height4
                                        this@EngineActivity.blurredImageView.setRectSquare(rect3)
                                        bitmap2 = cropToSquare
                                        rect = rect3
                                    }
                                    var width5: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.87530595f)
                                    var i6: Int = (width5 * 1.13f)
                                    var min: Int = min(width5, i6)
                                    var round5: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                                    var round6: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                                    var i7: Int = width5 + round5
                                    if (i7 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                        round5 -= i7 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                        i7 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    }
                                    var i8: Int = i6 + round6
                                    if (i8 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                        round6 -= i8 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                        i8 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    }
                                    if (round5 < 0) {
                                        round5 = 0
                                    }
                                    if (round6 < 0) {
                                        round6 = 0
                                    }
                                    var rect4 = Rect(round5, round6, i7, i8)
                                    if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_CLASSIC.ordinal) {
                                        var width6: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                        var height5: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                        var cropToSquare2: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect4, width6, height5)
                                        this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                                        rect4.right = rect4.left + width6
                                        rect4.bottom = rect4.top + height5
                                        this@EngineActivity.blurredImageView.setRectSquare(rect4)
                                        bitmap = cropToSquare2
                                    } else {
                                        var i9: Int = (min * 0.10800001f)
                                        this@EngineActivity.blurredImageView.setRadius_square(i9)
                                        var width7: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                        var height6: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                        var cropToSquareWithRoundCorners2: Bitmap? = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect4, i9, width7, height6)
                                        rect4.right = rect4.left + width7
                                        rect4.bottom = rect4.top + height6
                                        this@EngineActivity.blurredImageView.setRectSquare(rect4)
                                        bitmap = cropToSquareWithRoundCorners2
                                    }
                                    bitmap2 = bitmap
                                    rect = rect4
                                }
                                if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.GRADIENT.ordinal) {
                                    this@EngineActivity.blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap2, ViewCompat.MEASURED_STATE_MASK, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), rect)
                                } else if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.BLUE_TYPE.ordinal) {
                                    if (this@EngineActivity.blurredImageView.getColor_gradient() != null) {
                                        this@EngineActivity.blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap2, this@EngineActivity.blurredImageView.getColor_gradient(), this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), rect)
                                    } else {
                                        this@EngineActivity.blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap2, this@EngineActivity.blurredImageView.getColor_ipad(), this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), rect)
                                    }
                                } else {
                                    this@EngineActivity.blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap2, -1, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), rect)
                                }
                                this@EngineActivity.mTemplate.setColor_ipad(this@EngineActivity.blurredImageView.colorIpad())
                                this@EngineActivity.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        this@EngineActivity.blurredImageView.invalidate()
                                    }
                                })
                                engineActivity = this@EngineActivity
                                runnable = object : Runnable {
                                    override fun run() {
                                        this@EngineActivity.hideProgressFragment()
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace()
                                engineActivity = this@EngineActivity
                                runnable = object : Runnable {
                                    override fun run() {
                                        this@EngineActivity.hideProgressFragment()
                                    }
                                }
                            }
                            engineActivity.runOnUiThread(runnable)
                        } catch (Exception unused) {
                        }
                    } finally {
                    }
                }
            })
        }

        override fun onUploadVideo() {
            this@EngineActivity.pickVideoFromGallery()
        }

        override fun onUploadImg() {
            this@EngineActivity.pickImageFromGallery()
        }

        override fun onDone() {
            this@EngineActivity.hideFragment()
        }

        override fun onCancel() {
            this@EngineActivity.hideFragment()
        }
    }

    fun updateHitRatio(i: Int, str: String) {
        if (i == ResizeType.SOCIAL_STORY.ordinal) {
            this.textChangeResize.setText("9:16")
        } else if (i == ResizeType.SQUARE.ordinal) {
            this.textChangeResize.setText("1:1")
        } else {
            this.textChangeResize.setText("16:9")
        }
        this.ivResize.setImageResource(DrawableHelper.getIdResource(str))
    }

    private inner class AnonymousClass78 : DimensionAdabters.IDimensionCallback {
        override fun isCustomSize(z: Boolean, resizeType: ResizeType) {
        }

        object : DimensionAdabters.IDimensionCallback {} {
        }

        override fun done() {
            this@EngineActivity.hideFragment()
        }

        override fun onCustumSize(i: Int, i2: Int, i3: Int, str: String, i4: Int) {
            this@EngineActivity.updateHitRatio(i3, str)
            if (i3 == this@EngineActivity.mTemplate.geTypeResize()) {
                return
            }
            if (ResizeFragment.instance != null) {
                ResizeFragment.instance.scrollToSelectedPosition()
            }
            this@EngineActivity.showProgressSimple()
            this@EngineActivity.executor.execute(object : Runnable {
                override fun run() {
                    EngineActivity engineActivity
                    Runnable runnable
                    Bitmap cropTo16x9
                    var i5: Int = 0
                    Bitmap cropToSquareWithRoundCorners
                    Bitmap bitmap
                    Rect rect
                    try {
                        try {
                            try {
                                this@EngineActivity.blurredImageView.reset()
                                this@EngineActivity.mTemplate.setResizeType
            this@EngineActivity.mTemplate.setImgResize(str)
                                val size = AspectRatioCalculator.getSize(i3, this@EngineActivity.mTemplate.getResolution())
                                this@EngineActivity.mTemplate.setWidthAndHeight(size.getFirst().toInt(), size.getSecond().toInt())
                                this@EngineActivity.blurredImageView.initCanvasDimension(this@EngineActivity.blurredImageView.getWidth(), this@EngineActivity.blurredImageView.getHeight(), i3)
                                if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                                    cropTo16x9 = BitmapCropper.cropTo9x16(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                                } else if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SQUARE.ordinal) {
                                    cropTo16x9 = BitmapCropper.cropTo1x1(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                                } else {
                                    cropTo16x9 = BitmapCropper.cropTo16x9(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                                }
                                this@EngineActivity.blurredImageView.updatePosCanvas
            this@EngineActivity.blurredImageView.setBitmapBlured
            this@EngineActivity.blurredImageView.updateIpad(cropTo16x9, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize())
                                i5 = 0
                            } finally {
                            }
                        } catch (Exception e) {
                            Log.e("Tag resize : ", "init " + e.getMessage())
                            engineActivity = this@EngineActivity
                            runnable = object : Runnable {
                                override fun run() {
                                    this@EngineActivity.hideProgressFragment()
                                }
                            }
                        }
                        if (this@EngineActivity.mTemplate.getIpad_type() != IpadType.GRADIENT.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.BLACK_LAYER.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.MASK_BRUSH.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.BLUE_TYPE.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.CASSET_IMG.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.CASSET_IMG_BLUR.ordinal) {
                            if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_NEOMORPHIC.ordinal) {
                                var width: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.6f)
                                var round: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                                var round2: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                                var i6: Int = width + round
                                if (i6 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                    round -= i6 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    i6 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                }
                                var i7: Int = width + round2
                                if (i7 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                    round2 -= i7 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    i7 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                }
                                if (round < 0) {
                                    round = 0
                                }
                                if (round2 >= 0) {
                                    i5 = round2
                                }
                                var rect2 = Rect(round, i5, i6, i7)
                                this@EngineActivity.blurredImageView.setRadius_square(width)
                                var width2: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                var height: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                var cropToSquareWithRoundCorners2: Bitmap? = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect2, width, width2, height)
                                rect2.right = rect2.left + width2
                                rect2.bottom = rect2.top + height
                                this@EngineActivity.blurredImageView.setRectSquare(rect2)
                                bitmap = cropToSquareWithRoundCorners2
                                rect = rect2
                            } else {
                                if (this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal) {
                                    var width3: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 1.0f)
                                    var height2: Int = (cropTo16x9.getHeight() * 0.5355f)
                                    var round3: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                                    var round4: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                                    var i8: Int = width3 + round3
                                    if (i8 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                        round3 -= i8 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                        i8 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    }
                                    var i9: Int = height2 + round4
                                    if (i9 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                        round4 -= i9 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                        i9 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    }
                                    if (round3 < 0) {
                                        round3 = 0
                                    }
                                    if (round4 < 0) {
                                        round4 = 0
                                    }
                                    var rect3 = Rect(round3, round4, i8, i9)
                                    var width4: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                    var height3: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                    var cropToSquare: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect3, width4, height3)
                                    this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                                    rect3.right = rect3.left + width4
                                    rect3.bottom = rect3.top + height3
                                    this@EngineActivity.blurredImageView.setRectSquare(rect3)
                                    bitmap = cropToSquare
                                    rect = rect3
                                }
                                var width5: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.87530595f)
                                var i10: Int = (width5 * 1.13f)
                                var min: Int = min(width5, i10)
                                var round5: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                                var round6: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                                var i11: Int = width5 + round5
                                if (i11 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                    round5 -= i11 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    i11 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                }
                                var i12: Int = i10 + round6
                                if (i12 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                    round6 -= i12 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    i12 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                }
                                if (round5 < 0) {
                                    round5 = 0
                                }
                                if (round6 < 0) {
                                    round6 = 0
                                }
                                var rect4 = Rect(round5, round6, i11, i12)
                                if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_CLASSIC.ordinal) {
                                    var width6: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                    var height4: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                    var cropToSquare2: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect4, width6, height4)
                                    this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                                    rect4.right = rect4.left + width6
                                    rect4.bottom = rect4.top + height4
                                    this@EngineActivity.blurredImageView.setRectSquare(rect4)
                                    cropToSquareWithRoundCorners = cropToSquare2
                                } else {
                                    var i13: Int = (min * 0.10800001f)
                                    this@EngineActivity.blurredImageView.setRadius_square(i13)
                                    var width7: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                                    var height5: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                                    cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect4, i13, width7, height5)
                                    rect4.right = rect4.left + width7
                                    rect4.bottom = rect4.top + height5
                                    this@EngineActivity.blurredImageView.setRectSquare(rect4)
                                }
                                bitmap = cropToSquareWithRoundCorners
                                rect = rect4
                            }
                            if (this@EngineActivity.blurredImageView.getColor_gradient() != null) {
                                this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap, this@EngineActivity.blurredImageView.getColor_gradient(), this@EngineActivity.mTemplate.getIpad_type(), i3, rect)
                            } else {
                                this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap, this@EngineActivity.blurredImageView.getColor_ipad(), this@EngineActivity.mTemplate.getIpad_type(), i3, rect)
                            }
                            this@EngineActivity.blurredImageView.resizeEntity()
                            this@EngineActivity.blurredImageView.updatePosSurahName()
                            this@EngineActivity.runOnUiThread(object : Runnable {
                                override fun run() {
                                    if (this@EngineActivity.trackViewEntity.getCurrent_cursur_position() > this@EngineActivity.trackViewEntity.getMaxTime()) {
                                        this@EngineActivity.blurredImageView.invalidate()
                                    }
                                    this@EngineActivity.trackViewEntity.invalidate()
                                    this@EngineActivity.updateTime()
                                }
                            })
                            engineActivity = this@EngineActivity
                            runnable = object : Runnable {
                                override fun run() {
                                    this@EngineActivity.hideProgressFragment()
                                }
                            }
                            engineActivity.runOnUiThread(runnable)
                        }
                        this@EngineActivity.blurredImageView.setBitmapNotBlur(cropTo16x9)
                        var copy: Bitmap? = cropTo16x9.copy(cropTo16x9.getConfig() != null ? cropTo16x9.getConfig() : Bitmap.Config.ARGB_8888, true)
                        if (this@EngineActivity.blurredImageView.getColor_gradient() != null) {
                            this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), copy, this@EngineActivity.blurredImageView.getColor_gradient(), this@EngineActivity.mTemplate.getIpad_type(), i3, this@EngineActivity.blurredImageView.getRectSquare())
                        } else {
                            this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), copy, this@EngineActivity.blurredImageView.getColor_ipad(), this@EngineActivity.mTemplate.getIpad_type(), i3, this@EngineActivity.blurredImageView.getRectSquare())
                        }
                        if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.CASSET_IMG_BLUR.ordinal) {
                            this@EngineActivity.blurredImageView.setBitmapSquare(this@EngineActivity.blurredImageView.getBitmapBlured())
                            this@EngineActivity.blurredImageView.setRadius_square(0)
                        }
                        this@EngineActivity.blurredImageView.resizeEntity()
                        this@EngineActivity.blurredImageView.updatePosSurahName()
                        this@EngineActivity.runOnUiThread(object : Runnable {
                            override fun run() {
                                if (this@EngineActivity.trackViewEntity.getCurrent_cursur_position() > this@EngineActivity.trackViewEntity.getMaxTime()) {
                                    this@EngineActivity.blurredImageView.invalidate()
                                }
                                this@EngineActivity.trackViewEntity.invalidate()
                                this@EngineActivity.updateTime()
                            }
                        })
                        engineActivity = this@EngineActivity
                        runnable = object : Runnable {
                            override fun run() {
                                this@EngineActivity.hideProgressFragment()
                            }
                        }
                        engineActivity.runOnUiThread(runnable)
                    } catch (Exception unused) {
                    }
                }
            })
        }
    }

    fun dialogPremiumIpad() { /* billing removed */ }
            })
            val relativeLayout = (RelativeLayout) inflate.findViewById(R.id.dialog_yes)
            relativeLayout.setBackgroundResource(R.drawable.btn_dialog_premium_state)
            relativeLayout.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    this@EngineActivity.toProVersion()
                    this@EngineActivity.cancelDialog()
                }
            })
            if (LocaleHelper.getLanguage(this).equals("ar")) {
                textCustumFont.setText("🎁 هذه الميزة فقط للمشتركين في التطبيق.")
                textCustumFont2.setText("النسخة المدفوعة")
            } else {
                textCustumFont.setText("🎁 This feature is only for app subscribers.")
                textCustumFont2.setText("Upgrade premium")
            }
            this.dialog.show()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    fun pickVideoForAudio() {
        if (Build.VERSION.SDK_INT >= 34) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") != 0 && ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") != 0) {
                ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_MEDIA_VIDEO", "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"), 12)
                return
            }
        } else if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") != 0) {
                ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_MEDIA_VIDEO"), 12)
                return
            }
        } else if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_EXTERNAL_STORAGE"), 12)
            return
        }
        videoChooserForAudio()
    }

    fun pickVideoFromGallery() {
        if (Build.VERSION.SDK_INT >= 34) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") != 0 && ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") != 0) {
                ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO", "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"), 11)
                return
            }
        } else if (Build.VERSION.SDK_INT == 33) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_IMAGES") != 0 || ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") != 0) {
                ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO"), 11)
                return
            }
        } else if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_EXTERNAL_STORAGE"), 11)
            return
        }
        videoChooser()
    }

    fun pickImageFromGallery() {
        if (Build.VERSION.SDK_INT >= 34) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") != 0 || ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_IMAGES") != 0) {
                ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO", "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"), 10)
                return
            }
        } else if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_IMAGES") != 0 || ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") != 0) {
                ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO"), 10)
                return
            }
        } else if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_EXTERNAL_STORAGE"), 10)
            return
        }
        imageChooser()
    }

    private fun videoChooserForAudio() {
        this.isToCrop = true
        this.launchVideoExtract.launch(Intent(this, GalleryPickerVideo::class.java))
    }

    private fun videoChooser() {
        this.launchVideo.launch(Intent(this, GalleryPickerVideo::class.java))
    }

    private fun imageChooser() {
        this.launchImg.launch(Intent(this, GalleryPickerOneImage::class.java))
    }

 void m644lambda$new$8$hazemnurmontagevideoquranEngineActivity(ActivityResult activityResult) {
        Bitmap cropTo16x9
        if (activityResult.getResultCode() != -1 || activityResult.getData() == null || Common.bitmap == null || Common.bitmap.isRecycled()) {
            return
        }
        Common.bitmap = Bitmap.createScaledBitmap(Common.bitmap, this.blurredImageView.getHeight(), this.blurredImageView.getHeight(), false)
        this.blurredImageView.setBitmapOriginal(Common.bitmap)
        if (this.mTemplate.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
            cropTo16x9 = BitmapCropper.cropTo9x16(this.blurredImageView.getBitmapOriginal(), this.blurredImageView.getW(), this.blurredImageView.getH())
        } else if (this.mTemplate.geTypeResize() == ResizeType.SQUARE.ordinal) {
            cropTo16x9 = BitmapCropper.cropTo1x1(this.blurredImageView.getBitmapOriginal(), this.blurredImageView.getW(), this.blurredImageView.getH())
        } else {
            cropTo16x9 = BitmapCropper.cropTo16x9(this.blurredImageView.getBitmapOriginal(), this.blurredImageView.getW(), this.blurredImageView.getH())
        }
        this.blurredImageView.setBitmapBlured(UtilsBitmap.blur(this, cropTo16x9, 20, 1))
        this.blurredImageView.invalidate()
    }

 void m645lambda$new$9$hazemnurmontagevideoquranEngineActivity(ActivityResult activityResult) {
        if (activityResult.getResultCode() == -1) {
            var data = activityResult.getData()
            if (data == null) {
                return
            }
            this.mTemplate.setX_square(data.getFloatExtra("x", 0.3f))
            this.mTemplate.setY_square(data.getFloatExtra("y", 0.4f))
            this.mTemplate.setWidth_square(data.getFloatExtra("w", 1.0f))
            this.mTemplate.setHeight_square(data.getFloatExtra(CmcdData.STREAMING_FORMAT_HLS, 0.5f))
            this.blurredImageView.setBitmapSquare(Common.bitmap)
            this.blurredImageView.setRectSquare(Common.rect)
            this.blurredImageView.invalidate()
        }
        this.isToCrop = false
    }

 void m641lambda$new$10$hazemnurmontagevideoquranEngineActivity(ActivityResult activityResult) {
                if (activityResult.getResultCode() != -1 || (data = activityResult.getData()) == null || data.getData() == null) {
            return
        }
        handleImg(data.getData())
    }

 void m642lambda$new$11$hazemnurmontagevideoquranEngineActivity(ActivityResult activityResult) {
                if (activityResult.getResultCode() != -1 || (data = activityResult.getData()) == null || data.getData() == null) {
            return
        }
        var data2: Uri? = data.getData()
        try {
            getContentResolver().takePersistableUriPermission(data2, 1)
        } catch (Exception e) {
            e.printStackTrace()
        }
        handleVideo(data2)
    }

 void m643lambda$new$12$hazemnurmontagevideoquranEngineActivity(ActivityResult activityResult) {
                this.isToCrop = false
        if (activityResult.getResultCode() != -1 || (data = activityResult.getData()) == null || data.getData() == null) {
            return
        }
        try {
            var data2: Uri? = data.getData()
            try {
                getContentResolver().takePersistableUriPermission(data2, 1)
            } catch (Exception e) {
                e.printStackTrace()
            }
            runOnUiThread(object : Runnable() {
                override fun run() {
                    this@EngineActivity.showProgress()
                }
            })
            this.mTemplate.setUri_upload_extract_audio_video(data2.toString())
            var copyFromUri: String? = AudioUtils.copyFromUri(this, data2, this.mTemplate.getFolder_template())
            this.start_extenstion = 0
            extractAudioFromVideoRecursive(copyFromUri, 0, false, 0)
        } catch (Exception e2) {
            e2.printStackTrace()
        }
    }

    fun addAudioFromVideoWithExtention(str: String, str2: String, i: Int) {
        try {
            var file = File(File(this.mTemplate.getFolder_template()), System.currentTimeMillis() + "_audio" + str)
            FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", str2, "-vn", "-acodec", "copy", "-y", file.getAbsolutePath()), object : FFmpegSessionCompleteCallback() {
                override fun apply(fFmpegSession: FFmpegSession) {
                    if (ReturnCode.isSuccess(fFmpegSession.getReturnCode())) {
                        this@EngineActivity.addAudioTemplateHttp(Uri.fromFile(file), i, str2)
                    }
                }
            })
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    fun extractAudioFromVideoRecursive(str: String, i: Int, z: Boolean, i2: Int) {
        if (isDestroyed()) {
            return
        }
        if (i < this.extentions.length) {
            try {
                var file = File(File(this.mTemplate.getFolder_template()), System.currentTimeMillis() + "_audio" + this.extentions[i])
                FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", str, "-vn", "-acodec", "copy", "-y", file.getAbsolutePath()), object : FFmpegSessionCompleteCallback() {
                    override fun apply(fFmpegSession: FFmpegSession) {
                        if (ReturnCode.isSuccess(fFmpegSession.getReturnCode())) {
                            this@EngineActivity.mTemplate.setExtension(this@EngineActivity.extentions[i])
                            var fromFile: Uri? = Uri.fromFile(file)
                            if (!z) {
                                this@EngineActivity.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        this@EngineActivity.hideFragment()
                                        this@EngineActivity.hideProgressFragment()
                                    }
                                })
                                this@EngineActivity.addUriAudioToQuranFragment(fromFile, str)
                                return
                            } else {
                                this@EngineActivity.addAudioTemplateHttp(fromFile, i2, str)
                                return
                            }
                        }
                        this@EngineActivity.start_extenstion++
                        val engineActivity = this@EngineActivity
                        engineActivity.extractAudioFromVideoRecursive(str, engineActivity.start_extenstion, z, i)
                    }
                })
                return
            } catch (Exception e) {
                e.printStackTrace()
                extractAudioFromVideo(str, z)
                return
            }
        }
        extractAudioFromVideo(str, z)
    }

    private fun extractAudioFromVideo(str: String, z: Boolean) {
        try {
            var file = File(File(this.mTemplate.getFolder_template()), System.currentTimeMillis() + "_audio.mp3")
            FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", str, "-vn", "-acodec", "copy", "-y", file.getAbsolutePath()), object : FFmpegSessionCompleteCallback() {
                override fun apply(fFmpegSession: FFmpegSession) {
                    if (fFmpegSession == null) {
                        this@EngineActivity.runOnUiThread(object : Runnable {
                            override fun run() {
                                this@EngineActivity.hideFragment()
                                this@EngineActivity.hideProgressFragment()
                            }
                        })
                        return
                    }
                    if (fFmpegSession.getReturnCode().isValueSuccess()) {
                        var fromFile: Uri? = Uri.fromFile
            this@EngineActivity.mTemplate.setExtension(".mp3")
                        if (!z) {
                            this@EngineActivity.addUriAudioToQuranFragment(fromFile, str)
                            return
                        } else {
                            this@EngineActivity.addAudioTemplateHttp(fromFile, 0, str)
                            return
                        }
                    }
                    this@EngineActivity.runOnUiThread(object : Runnable {
                        override fun run() {
                            this@EngineActivity.hideProgressFragment()
                            this@EngineActivity.hideFragment()
                            Toast.makeText(this@EngineActivity, this@EngineActivity.mResources.getString(R.string.video_not_have_sound), 0).show()
                        }
                    })
                }
            })
        } catch (Exception e) {
            e.printStackTrace()
            runOnUiThread(object : Runnable() {
                override fun run() {
                    this@EngineActivity.hideFragment()
                    this@EngineActivity.hideProgressFragment()
                }
            })
        }
    }

    private fun toChoiceBgFromVideo(uri: Uri) {
        var intent = Intent(this, ChoiceBgFromVideoActivity::class.java)
        intent.setData(uri)
        this.launchChoiceBgActivity.launch(intent)
    }

    private fun handleVideo(uri: Uri) {
        showProgress()
        clearFFmpeg()
        this.executor.execute(AnonymousClass88(uri))
    }

    private inner class AnonymousClass88 : Runnable {
        Uri val$uri

        AnonymousClass88(Uri uri) {
            this.val$uri = uri
        }

        override fun run() {
            try {
                val engineActivity = this@EngineActivity
                var copyFromUri: String? = AudioUtils.copyFromUri(engineActivity, this.val$uri, engineActivity.mTemplate.getFolder_template())
                val mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(this@EngineActivity, this.val$uri)
                mediaPlayer.setOnPreparedListener(AnonymousClass1(copyFromUri))
                mediaPlayer.prepare()
            } catch (Exception e) {
                e.printStackTrace()
                this@EngineActivity.runOnUiThread(object : Runnable {
                    override fun run() {
                        this@EngineActivity.hideProgressFragment()
                    }
                })
            }
        }

        private inner class AnonymousClass1 : MediaPlayer.OnPreparedListener {
            String val$path

            AnonymousClass1(String str) {
                this.val$path = str
            }

            override fun onPrepared(mediaPlayer: MediaPlayer) {
                if (mediaPlayer == null) {
                    return
                }
                var height: Int = this@EngineActivity.blurredImageView.getHeight()
                this@EngineActivity.mTemplate.setVideoSquare
            this@EngineActivity.blurredImageView.setVideo
            this@EngineActivity.mTemplate.setName_drawable
            this@EngineActivity.mTemplate.setUri_original_upload_video(AnonymousClass88.this.val$uri.toString())
                this@EngineActivity.mTemplate.setUri_media_video(this.val$path)
                this@EngineActivity.mTemplate.setDuration_video_media(mediaPlayer.getDuration() / 1000)
                var fileVideo = FileUtils.getFileVideo(this@EngineActivity.mTemplate.getFolder_template())
                var file = File(fileVideo, "frame_%04d.jpg")
                var file2 = File(fileVideo, "frame_0001.jpg")
                this@EngineActivity.mTemplate.setFrame_bg(file2.getAbsolutePath())
                this@EngineActivity.endFrame = min(Math.round(this@EngineActivity.trackViewEntity.getMaxTime() / 1000.0f), 3)
                if (this@EngineActivity.endFrame == 0) {
                    this@EngineActivity.endFrame = 3
                }
                this@EngineActivity.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", this.val$path, "-ss", "0", "-t", "" + this@EngineActivity.endFrame, "-r", "25", "-vf", "scale=" + height + ":" + height + ":force_original_aspect_ratio=increase", "-q:v", "0", "-threads", "4", "-an", "-y", file.getAbsolutePath()), object : FFmpegSessionCompleteCallback() {.1.1
                    override fun apply(fFmpegSession: FFmpegSession) {
                        this@EngineActivity.changeBitmap(file2.getAbsolutePath())
                        this@EngineActivity.runOnUiThread(object : Runnable {
                            override fun run() {
                                this@EngineActivity.hideProgressFragment()
                            }
                        })
                        this@EngineActivity.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", AnonymousClass1.this.val$path, "-ss", "" + this@EngineActivity.endFrame, "-r", "25", "-vf", "scale=" + height + ":" + height + ":force_original_aspect_ratio=increase", "-start_number", "" + (this@EngineActivity.endFrame * 25), "-q:v", "0", "-threads", "4", "-an", "-y", file.getAbsolutePath()), object : FFmpegSessionCompleteCallback() {.1.1.2
                            override fun apply(fFmpegSession2: FFmpegSession) {
                            }
                        }).getSessionId()))
                    }
                }).getSessionId()))
            }
        }
    }

    fun changeBitmap(str: String) {
        this.executor.execute(object : Runnable() {
            /* JADX WARN: Multi-variable type inference failed */
            override fun run() {
                Bitmap cropTo16x9
                Bitmap cropToSquareWithRoundCorners
                Bitmap bitmap
                Rect rect
                try {
                    var height: Int = this@EngineActivity.blurredImageView.getHeight()
                    var bitmap2: Bitmap? = Glide as Bitmap.with(this@EngineActivity).asBitmap().load(str).override(height, height).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).submit().get()
                    if (bitmap2 == null) {
                        return
                    }
                    this@EngineActivity.blurredImageView.setBitmapOriginal(bitmap2)
                    if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.RECT.ordinal || this@EngineActivity.mTemplate.getIpad_type() == IpadType.ROUND_RECT.ordinal) {
                        this@EngineActivity.mTemplate.setIpad_type(IpadType.IPAD.ordinal)
                    }
                    if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                        cropTo16x9 = BitmapCropper.cropTo9x16(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                    } else if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SQUARE.ordinal) {
                        cropTo16x9 = BitmapCropper.cropTo1x1(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                    } else {
                        cropTo16x9 = BitmapCropper.cropTo16x9(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                    }
                    this@EngineActivity.blurredImageView.updatePosCanvas
            this@EngineActivity.blurredImageView.updateIpad(cropTo16x9, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize())
                    if (this@EngineActivity.mTemplate.getIpad_type() != IpadType.BLACK_LAYER.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.GRADIENT.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.MASK_BRUSH.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.BLUE_TYPE.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.CASSET_IMG.ordinal) {
                        if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.CASSET_IMG_BLUR.ordinal) {
                            this@EngineActivity.blurredImageView.setBitmapBlured(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1))
                            this@EngineActivity.blurredImageView.setBitmapSquare(this@EngineActivity.blurredImageView.getBitmapBlured())
                        } else {
                            var min: Int = min(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth(), this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight())
                            var i: Int = 0
                            if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_NEOMORPHIC.ordinal) {
                                var width: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.6f)
                                var f: Float = min
                                var round: Int = Math.round(this@EngineActivity.mTemplate.getX_square() * f)
                                var round2: Int = Math.round(this@EngineActivity.mTemplate.getY_square() * f)
                                var i2: Int = width + round
                                if (i2 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                    round -= i2 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    i2 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                }
                                var i3: Int = width + round2
                                if (i3 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                    round2 -= i3 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    i3 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                }
                                if (round < 0) {
                                    round = 0
                                }
                                if (round2 >= 0) {
                                    i = round2
                                }
                                var rect2 = Rect(round, i, i2, i3)
                                this@EngineActivity.blurredImageView.setRadius_square(width)
                                var width_square: Int = (this@EngineActivity.mTemplate.getWidth_square() * f)
                                var height_square: Int = (f * this@EngineActivity.mTemplate.getHeight_square())
                                var cropToSquareWithRoundCorners2: Bitmap? = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect2, width, width_square, height_square)
                                rect2.right = rect2.left + width_square
                                rect2.bottom = rect2.top + height_square
                                this@EngineActivity.blurredImageView.setRectSquare(rect2)
                                bitmap = cropToSquareWithRoundCorners2
                                rect = rect2
                            } else {
                                if (this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal) {
                                    var width2: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 1.0f)
                                    var height2: Int = (cropTo16x9.getHeight() * 0.5355f)
                                    var f2: Float = min
                                    var round3: Int = Math.round(this@EngineActivity.mTemplate.getX_square() * f2)
                                    var round4: Int = Math.round(this@EngineActivity.mTemplate.getY_square() * f2)
                                    var i4: Int = width2 + round3
                                    if (i4 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                        round3 -= i4 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                        i4 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    }
                                    var i5: Int = height2 + round4
                                    if (i5 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                        round4 -= i5 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                        i5 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    }
                                    if (round3 < 0) {
                                        round3 = 0
                                    }
                                    if (round4 < 0) {
                                        round4 = 0
                                    }
                                    var rect3 = Rect(round3, round4, i4, i5)
                                    var width_square2: Int = (this@EngineActivity.mTemplate.getWidth_square() * f2)
                                    var height_square2: Int = (f2 * this@EngineActivity.mTemplate.getHeight_square())
                                    var cropToSquare: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect3, width_square2, height_square2)
                                    this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                                    rect3.right = rect3.left + width_square2
                                    rect3.bottom = rect3.top + height_square2
                                    this@EngineActivity.blurredImageView.setRectSquare(rect3)
                                    bitmap = cropToSquare
                                    rect = rect3
                                }
                                var width3: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.87530595f)
                                var i6: Int = (width3 * 1.13f)
                                var min2: Int = min(width3, i6)
                                var f3: Float = min
                                var round5: Int = Math.round(this@EngineActivity.mTemplate.getX_square() * f3)
                                var round6: Int = Math.round(this@EngineActivity.mTemplate.getY_square() * f3)
                                var i7: Int = width3 + round5
                                if (i7 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                    round5 -= i7 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    i7 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                }
                                var i8: Int = i6 + round6
                                if (i8 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                    round6 -= i8 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    i8 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                }
                                if (round5 < 0) {
                                    round5 = 0
                                }
                                if (round6 < 0) {
                                    round6 = 0
                                }
                                var rect4 = Rect(round5, round6, i7, i8)
                                if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_CLASSIC.ordinal) {
                                    var width_square3: Int = (this@EngineActivity.mTemplate.getWidth_square() * f3)
                                    var height_square3: Int = (f3 * this@EngineActivity.mTemplate.getHeight_square())
                                    var cropToSquare2: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect4, width_square3, height_square3)
                                    this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                                    rect4.right = rect4.left + width_square3
                                    rect4.bottom = rect4.top + height_square3
                                    this@EngineActivity.blurredImageView.setRectSquare(rect4)
                                    cropToSquareWithRoundCorners = cropToSquare2
                                } else {
                                    var i9: Int = (min2 * 0.10800001f)
                                    this@EngineActivity.blurredImageView.setRadius_square(i9)
                                    var width_square4: Int = (this@EngineActivity.mTemplate.getWidth_square() * f3)
                                    var height_square4: Int = (f3 * this@EngineActivity.mTemplate.getHeight_square())
                                    cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect4, i9, width_square4, height_square4)
                                    rect4.right = rect4.left + width_square4
                                    rect4.bottom = rect4.top + height_square4
                                    this@EngineActivity.blurredImageView.setRectSquare(rect4)
                                }
                                bitmap = cropToSquareWithRoundCorners
                                rect = rect4
                            }
                            this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap, -1, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), rect)
                        }
                        this@EngineActivity.mTemplate.setColor_ipad(this@EngineActivity.blurredImageView.colorIpad())
                        this@EngineActivity.runOnUiThread(object : Runnable {
                            override fun run() {
                                this@EngineActivity.blurredImageView.invalidate()
                            }
                        })
                    }
                    if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.GRADIENT.ordinal) {
                        this@EngineActivity.blurredImageView.setColorIpad(ViewCompat.MEASURED_STATE_MASK)
                    }
                    this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setBitmapBlured(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1))
                    this@EngineActivity.mTemplate.setColor_ipad(this@EngineActivity.blurredImageView.colorIpad())
                    this@EngineActivity.runOnUiThread(object : Runnable {
                        override fun run() {
                            this@EngineActivity.blurredImageView.invalidate()
                        }
                    })
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun updateSquareBitmap(str: String) {
        if (this.isOnScroll) {
            if (this.mIsPlaying) {
                return
            }
        } else if (!this.mIsPlaying) {
            return
        }
        this.executor.execute(object : Runnable() {
            /* JADX WARN: Multi-variable type inference failed */
            override fun run() {
                EngineActivity engineActivity
                Runnable runnable
                Bitmap bitmap
                Bitmap cropTo16x9
                try {
                    try {
                        var height: Int = this@EngineActivity.blurredImageView.getHeight()
                        bitmap = Glide as Bitmap.with(this@EngineActivity).asBitmap().load(str).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(height, height).submit().get()
                    } catch (Exception e) {
                        e.printStackTrace()
                        engineActivity = this@EngineActivity
                        runnable = object : Runnable {
                            override fun run() {
                                if (!this@EngineActivity.isOnScroll) {
                                    this@EngineActivity.blurredImageView.setDrawingSquareVideo(true)
                                }
                                this@EngineActivity.blurredImageView.invalidate()
                            }
                        }
                    }
                    if (bitmap == null) {
                        return
                    }
                    if (this@EngineActivity.mTemplate.getIpad_type() != IpadType.BLACK_LAYER.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.GRADIENT.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.MASK_BRUSH.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.BLUE_TYPE.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.CASSET_IMG.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.CASSET_IMG_BLUR.ordinal) {
                        if (this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.BOTTOM_RECT.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_NEOMORPHIC.ordinal) {
                            var width: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.87530595f)
                            var i: Int = (width * 1.13f)
                            var min: Int = (min(width, i) * 0.10800001f)
                            var round: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getX_square())
                            var round2: Int = Math.round(this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getY_square())
                            var i2: Int = width + round
                            if (i2 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                round -= i2 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                i2 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                            }
                            var i3: Int = i + round2
                            if (i3 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                round2 -= i3 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                i3 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                            }
                            if (round < 0) {
                                round = 0
                            }
                            if (round2 < 0) {
                                round2 = 0
                            }
                            var rect = Rect(round, round2, i2, i3)
                            var width2: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth() * this@EngineActivity.mTemplate.getWidth_square())
                            var height2: Int = (this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight() * this@EngineActivity.mTemplate.getHeight_square())
                            this@EngineActivity.blurredImageView.setBitmapSquare(UtilsBitmap.cropToSquareWithRoundCorners(bitmap, rect, min, width2, height2))
                            rect.right = rect.left + width2
                            rect.bottom = rect.top + height2
                            this@EngineActivity.blurredImageView.setRectSquare(rect)
                        } else {
                            this@EngineActivity.blurredImageView.setBitmapSquare(UtilsBitmap.cropToSquareWithRoundCornersPlusScale(bitmap, this@EngineActivity.blurredImageView.getRectSquare(), this@EngineActivity.blurredImageView.getRadius_square(), this@EngineActivity.blurredImageView.getBitmapSquare().getWidth(), this@EngineActivity.blurredImageView.getBitmapSquare().getHeight()))
                        }
                        engineActivity = this@EngineActivity
                        runnable = object : Runnable {
                            override fun run() {
                                if (!this@EngineActivity.isOnScroll) {
                                    this@EngineActivity.blurredImageView.setDrawingSquareVideo(true)
                                }
                                this@EngineActivity.blurredImageView.invalidate()
                            }
                        }
                        engineActivity.runOnUiThread(runnable)
                    }
                    if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                        cropTo16x9 = BitmapCropper.cropTo9x16(bitmap, this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                    } else if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SQUARE.ordinal) {
                        cropTo16x9 = BitmapCropper.cropTo1x1(bitmap, this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                    } else {
                        cropTo16x9 = BitmapCropper.cropTo16x9(bitmap, this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                    }
                    this@EngineActivity.blurredImageView.setBitmapSquare(cropTo16x9)
                    engineActivity = this@EngineActivity
                    runnable = object : Runnable {
                        override fun run() {
                            if (!this@EngineActivity.isOnScroll) {
                                this@EngineActivity.blurredImageView.setDrawingSquareVideo(true)
                            }
                            this@EngineActivity.blurredImageView.invalidate()
                        }
                    }
                    engineActivity.runOnUiThread(runnable)
                } finally {
                    this@EngineActivity.runOnUiThread(object : Runnable {
                        override fun run() {
                            if (!this@EngineActivity.isOnScroll) {
                                this@EngineActivity.blurredImageView.setDrawingSquareVideo(true)
                            }
                            this@EngineActivity.blurredImageView.invalidate()
                        }
                    })
                }
            }
        })
    }

    fun setupOriginalBitmap(): Bitmap(Uri uri) throws IOException {
        var height: Int = this.blurredImageView.getHeight()
        var bitmap: Bitmap? = MediaStore.Images.Media.getBitmap(getContentResolver(), uri)
        var min: Float = height / min(r1, r2)
        return Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth() * min), Math.round(bitmap.getHeight() * min), true)
    }

    fun setupOriginalBitmap(): Bitmap(Bitmap bitmap, int i) {
        var min: Float = i / min(r0, r1)
        return Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth() * min), Math.round(bitmap.getHeight() * min), true)
    }

    private fun handleImg(uri: Uri) {
        showProgress()
        this.executor.execute(object : Runnable() {
            override fun run() {
                EngineActivity engineActivity
                Runnable runnable
                Bitmap cropTo16x9
                Bitmap cropToSquareWithRoundCorners
                Bitmap bitmap
                Rect rect
                try {
                    try {
                        this@EngineActivity.getContentResolver().takePersistableUriPermission(uri, 1)
                    } catch (Exception unused) {
                    }
                    try {
                        try {
                            this@EngineActivity.uri_bg = uri.toString()
                            this@EngineActivity.mTemplate.setName_drawable
            this@EngineActivity.mTemplate.setUri_bg(this@EngineActivity.uri_bg)
                            var i: Int = 0
                            this@EngineActivity.mTemplate.setVideoSquare
            this@EngineActivity.blurredImageView.setVideo
            this@EngineActivity.blurredImageView.setBitmapOriginal(this@EngineActivity.setupOriginalBitmap(uri))
                            if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                                cropTo16x9 = BitmapCropper.cropTo9x16(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                            } else if (this@EngineActivity.mTemplate.geTypeResize() == ResizeType.SQUARE.ordinal) {
                                cropTo16x9 = BitmapCropper.cropTo1x1(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                            } else {
                                cropTo16x9 = BitmapCropper.cropTo16x9(this@EngineActivity.blurredImageView.getBitmapOriginal(), this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                            }
                            this@EngineActivity.blurredImageView.updatePosCanvas
            this@EngineActivity.blurredImageView.updateIpad(cropTo16x9, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize())
                            var min: Int = min(this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth(), this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight())
                            if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_NEOMORPHIC.ordinal) {
                                var width: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.6f)
                                var f: Float = min
                                var round: Int = Math.round(this@EngineActivity.mTemplate.getX_square() * f)
                                var round2: Int = Math.round(this@EngineActivity.mTemplate.getY_square() * f)
                                var i2: Int = width + round
                                if (i2 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                    round -= i2 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    i2 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                }
                                var i3: Int = width + round2
                                if (i3 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                    round2 -= i3 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    i3 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                }
                                if (round < 0) {
                                    round = 0
                                }
                                if (round2 >= 0) {
                                    i = round2
                                }
                                var rect2 = Rect(round, i, i2, i3)
                                this@EngineActivity.blurredImageView.setRadius_square(width)
                                var width_square: Int = (this@EngineActivity.mTemplate.getWidth_square() * f)
                                var height_square: Int = (f * this@EngineActivity.mTemplate.getHeight_square())
                                var cropToSquareWithRoundCorners2: Bitmap? = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect2, width, width_square, height_square)
                                this@EngineActivity.blurredImageView.setBitmapSquare(cropToSquareWithRoundCorners2)
                                rect2.right = rect2.left + width_square
                                rect2.bottom = rect2.top + height_square
                                this@EngineActivity.blurredImageView.setRectSquare(rect2)
                                bitmap = cropToSquareWithRoundCorners2
                                rect = rect2
                            } else {
                                if (this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal && this@EngineActivity.mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal) {
                                    if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.BOTTOM_RECT.ordinal) {
                                        var width2: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 1.0f)
                                        var height: Int = (cropTo16x9.getHeight() * 0.5355f)
                                        var f2: Float = min
                                        var round3: Int = Math.round(this@EngineActivity.mTemplate.getX_square() * f2)
                                        var round4: Int = Math.round(this@EngineActivity.mTemplate.getY_square() * f2)
                                        var i4: Int = width2 + round3
                                        if (i4 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                            round3 -= i4 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                            i4 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                        }
                                        var i5: Int = height + round4
                                        if (i5 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                            round4 -= i5 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                            i5 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                        }
                                        if (round3 < 0) {
                                            round3 = 0
                                        }
                                        if (round4 < 0) {
                                            round4 = 0
                                        }
                                        var rect3 = Rect(round3, round4, i4, i5)
                                        var width_square2: Int = (this@EngineActivity.mTemplate.getWidth_square() * f2)
                                        var height_square2: Int = (f2 * this@EngineActivity.mTemplate.getHeight_square())
                                        var cropToSquare: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect3, width_square2, height_square2)
                                        this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                                        rect3.right = rect3.left + width_square2
                                        rect3.bottom = rect3.top + height_square2
                                        this@EngineActivity.blurredImageView.setRectSquare(rect3)
                                        bitmap = cropToSquare
                                        rect = rect3
                                    } else {
                                        bitmap = null
                                        rect = null
                                    }
                                }
                                var width3: Int = (this@EngineActivity.blurredImageView.getIpad_rect().width() * 0.87530595f)
                                var i6: Int = (width3 * 1.13f)
                                var min2: Int = min(width3, i6)
                                var f3: Float = min
                                var round5: Int = Math.round(this@EngineActivity.mTemplate.getX_square() * f3)
                                var round6: Int = Math.round(this@EngineActivity.mTemplate.getY_square() * f3)
                                var i7: Int = width3 + round5
                                if (i7 > this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()) {
                                    round5 -= i7 - this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                    i7 = this@EngineActivity.blurredImageView.getBitmapOriginal().getWidth()
                                }
                                var i8: Int = i6 + round6
                                if (i8 > this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()) {
                                    round6 -= i8 - this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                    i8 = this@EngineActivity.blurredImageView.getBitmapOriginal().getHeight()
                                }
                                if (round5 < 0) {
                                    round5 = 0
                                }
                                if (round6 < 0) {
                                    round6 = 0
                                }
                                var rect4 = Rect(round5, round6, i7, i8)
                                if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.IPAD_CLASSIC.ordinal) {
                                    var width_square3: Int = (this@EngineActivity.mTemplate.getWidth_square() * f3)
                                    var height_square3: Int = (f3 * this@EngineActivity.mTemplate.getHeight_square())
                                    var cropToSquare2: Bitmap? = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect4, width_square3, height_square3)
                                    this@EngineActivity.blurredImageView.setBitmapSquare
            this@EngineActivity.blurredImageView.setRadius_square(0)
                                    rect4.right = rect4.left + width_square3
                                    rect4.bottom = rect4.top + height_square3
                                    this@EngineActivity.blurredImageView.setRectSquare(rect4)
                                    cropToSquareWithRoundCorners = cropToSquare2
                                } else {
                                    var i9: Int = (min2 * 0.10800001f)
                                    this@EngineActivity.blurredImageView.setRadius_square(i9)
                                    var width_square4: Int = (this@EngineActivity.mTemplate.getWidth_square() * f3)
                                    var height_square4: Int = (f3 * this@EngineActivity.mTemplate.getHeight_square())
                                    cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.getBitmapOriginal(), rect4, i9, width_square4, height_square4)
                                    this@EngineActivity.blurredImageView.setBitmapSquare(cropToSquareWithRoundCorners)
                                    rect4.right = rect4.left + width_square4
                                    rect4.bottom = rect4.top + height_square4
                                    this@EngineActivity.blurredImageView.setRectSquare(rect4)
                                }
                                bitmap = cropToSquareWithRoundCorners
                                rect = rect4
                            }
                            if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.GRADIENT.ordinal) {
                                this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap, ViewCompat.MEASURED_STATE_MASK, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), rect)
                            } else if (this@EngineActivity.mTemplate.getIpad_type() == IpadType.BLUE_TYPE.ordinal) {
                                if (this@EngineActivity.blurredImageView.getColor_gradient() != null) {
                                    this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap, this@EngineActivity.blurredImageView.getColor_gradient(), this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), rect)
                                } else {
                                    this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap, this@EngineActivity.blurredImageView.getColor_ipad(), this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), rect)
                                }
                            } else {
                                this@EngineActivity.blurredImageView.setBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9, 20, 1), bitmap, -1, this@EngineActivity.mTemplate.getIpad_type(), this@EngineActivity.mTemplate.geTypeResize(), rect)
                            }
                            this@EngineActivity.blurredImageView.invalidate()
                            engineActivity = this@EngineActivity
                            runnable = object : Runnable {
                                override fun run() {
                                    this@EngineActivity.hideProgressFragment()
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace()
                            engineActivity = this@EngineActivity
                            runnable = object : Runnable {
                                override fun run() {
                                    this@EngineActivity.hideProgressFragment()
                                }
                            }
                        }
                        engineActivity.runOnUiThread(runnable)
                    } catch (Exception unused2) {
                    }
                } finally {
                }
            }
        })
    }

    fun updateTime() {
        this.trackViewEntity.calculMaxTime()
        updateViewTime(this.trackViewEntity.getMaxTime(), this.trackViewEntity.getCurrent_cursur_position())
        if (this.trackViewEntity.getCurrent_cursur_position() <= this.trackViewEntity.getMaxTime()) {
            updateTime(this.trackViewEntity.getCurrent_cursur_position())
            val trackEntityView = this.trackViewEntity
            trackEntityView.setCurrent_cursur_position(trackEntityView.getCurrent_cursur_position())
            this.blurredImageView.setProgress(this.trackViewEntity.getCurrent_cursur_position() / this.trackViewEntity.getMaxTime())
        }
    }

    fun updateTimeToEndAya() {
        this.trackViewEntity.calculMaxTime()
        this.trackViewEntity.translateToEnd()
        updateViewTime(this.trackViewEntity.getMaxTime(), this.trackViewEntity.getCurrent_cursur_position())
        if (this.trackViewEntity.getCurrent_cursur_position() <= this.trackViewEntity.getMaxTime()) {
            updateTime(this.trackViewEntity.getCurrent_cursur_position())
            val trackEntityView = this.trackViewEntity
            trackEntityView.setCurrent_cursur_position(trackEntityView.getCurrent_cursur_position())
            this.blurredImageView.setProgress(this.trackViewEntity.getCurrent_cursur_position() / this.trackViewEntity.getMaxTime())
        }
    }

    fun selectSurahName() {
        findViewById(R.id.layout_menu).setVisibility(4)
        val surahNameEntity = this.blurredImageView.getSurahNameEntity()
        val beginTransaction = getSupportFragmentManager().beginTransaction()
        this.mCurrentFragment = EditS_NameFragment.getInstance(this.iEditSName, this.mResources, surahNameEntity)
        beginTransaction.replace(R.id.m_container, this.mCurrentFragment)
        beginTransaction.commit()
    }

    fun dialogDeleteSelected() {
        try {
            var dialog = Dialog(this)
            this.dialog = dialog
            dialog.setCancelable(true)
            this.dialog.requestWindowFeature(1)
            this.dialog.getWindow().setLayout(-1, -2)
            this.dialog.getWindow().setBackgroundDrawable(ColorDrawable(0))
            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_dialog, (ViewGroup) null)
            this.dialog.setContentView(inflate)
            inflate.findViewById(R.id.dialog_title).setVisibility(8)
            ((TextCustumFont) inflate.findViewById(R.id.dialog_message)).setText(this.mResources.getString(R.string.are_you_sure_to_delete_this_work))
            var buttonCustumFont = (ButtonCustumFont) inflate.findViewById(R.id.dialog_no)
            buttonCustumFont.setText(this.mResources.getString(R.string.delete))
            buttonCustumFont.setTextColor(-1499549)
            buttonCustumFont.setBackgroundResource(R.drawable.btn_dialog_delete)
            buttonCustumFont.setOnClickListener(AnonymousClass98(buttonCustumFont))
            var buttonCustumFont2 = (ButtonCustumFont) inflate.findViewById(R.id.dialog_yes)
            buttonCustumFont2.setText(this.mResources.getString(R.string.no))
            buttonCustumFont2.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    this@EngineActivity.dialog.dismiss()
                }
            })
            this.dialog.show()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    private inner class AnonymousClass98 : View.OnClickListener {
        ButtonCustumFont val$dialog_no

        AnonymousClass98(ButtonCustumFont buttonCustumFont) {
            this.val$dialog_no = buttonCustumFont
        }

        override fun onClick(view: View) {
            this.val$dialog_no.setClickable
            this@EngineActivity.showProgress()
            object : Thread(object : Runnable {
                override fun run() {
                    this@EngineActivity.trackViewEntity.deleteEntityAllSelect()
                    this@EngineActivity.runOnUiThread(object : Runnable {
                        override fun run() {
                            this@EngineActivity.trackViewEntity.invalidate()
                            this@EngineActivity.updateTime()
                            this@EngineActivity.hideProgressFragment()
                            this@EngineActivity.iTrimLineCallback.onEmptySelect()
                        }
                    })
                }
            }).start()
            if (this@EngineActivity.dialog != null) {
                this@EngineActivity.dialog.dismiss()
            }
        }
    }

    fun applyffectPlayAuto(str: String, entityAudio: EntityAudio) {
        showProgressSimple()
        var file = File(this.mTemplate.getFolder_template(), System.currentTimeMillis() + "_audio_echo.mp3")
        this.id_ffmpeg.add(/* Long.valueOf */ FFmpegKit.executeWithArgumentsAsync(arrayOf("-i", entityAudio.getPath_ffmpeg(), "-af", str, "-y", file.getAbsolutePath()), AnonymousClass101(Uri.fromFile(file), entityAudio, file)).getSessionId()))
    }

    private inner class AnonymousClass101 : FFmpegSessionCompleteCallback {
        EntityAudio val$entityAudio
        File val$file
        Uri val$uri

        AnonymousClass101(Uri uri, EntityAudio entityAudio, File file) {
            this.val$uri = uri
            this.val$entityAudio = entityAudio
            this.val$file = file
        }

        override fun apply(fFmpegSession: FFmpegSession) {
            if (ReturnCode.isSuccess(fFmpegSession.getReturnCode())) {
                try {
                    this@EngineActivity.mPlayer = MediaPlayer()
                    this@EngineActivity.mPlayer.setAudioStreamType(3)
                    if (this.val$uri.getScheme() != null && this.val$uri.getScheme().startsWith("http")) {
                        this@EngineActivity.mPlayer.setDataSource(this.val$uri.toString())
                    } else {
                        this@EngineActivity.mPlayer.setDataSource(this@EngineActivity, this.val$uri)
                    }
                    this@EngineActivity.mPlayer.prepareAsync()
                    this@EngineActivity.mPlayer.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {.1
                        override fun onPrepared(mediaPlayer: MediaPlayer) {
                            if (AnonymousClass101.this.val$entityAudio.getMediaPlayer() != null && mediaPlayer.getDuration() != AnonymousClass101.this.val$entityAudio.getMediaPlayer().getDuration()) {
                                AnonymousClass101.this.val$entityAudio.setRight(AnonymousClass101.this.val$entityAudio.getRect().left + Math.round(this@EngineActivity.trackViewEntity.getSecond_in_screen() * (mediaPlayer.getDuration() / 1000.0f)))
                                AnonymousClass101.this.val$entityAudio.setEnd(mediaPlayer.getDuration())
                                AnonymousClass101.this.val$entityAudio.setStart(0.0f)
                                AnonymousClass101.this.val$entityAudio.setMax((AnonymousClass101.this.val$entityAudio.getRect().right / AnonymousClass101.this.val$entityAudio.getmScaleFactor()) - ((AnonymousClass101.this.val$entityAudio.getRect().left / AnonymousClass101.this.val$entityAudio.getmScaleFactor()) - AnonymousClass101.this.val$entityAudio.getOffset_left()))
                                this@EngineActivity.trackViewEntity.updateWhenEffect(AnonymousClass101.this.val$entityAudio)
                                this@EngineActivity.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        this@EngineActivity.trackViewEntity.invalidate()
                                        AnonymousClass101.this.val$entityAudio.setMediaPlayer
            this@EngineActivity.iEditMediaCallback.startPreview()
                                        this@EngineActivity.hideProgressFragment()
                                    }
                                })
                                return
                            }
                            this@EngineActivity.runOnUiThread(object : Runnable {
                                override fun run() {
                                    AnonymousClass101.this.val$entityAudio.setMediaPlayer
            this@EngineActivity.iEditMediaCallback.startPreview()
                                    this@EngineActivity.hideProgressFragment()
                                }
                            })
                        }
                    })
                    this.val$entityAudio.setApplyEffectInPreview(true)
                    this.val$entityAudio.setPath_ffmpeg_effect(this.val$file.getAbsolutePath())
                } catch (Exception e) {
                    e.printStackTrace()
                    this@EngineActivity.runOnUiThread(object : Runnable {
                        override fun run() {
                            this@EngineActivity.hideProgressFragment()
                        }
                    })
                }
            }
        }
    }

    fun checkSplitEntity() {
        if (EditEntityFragment.instance == null || this.trackViewEntity.getSelectedEntity() == null) {
            return
        }
        EditEntityFragment.instance.checkSplitEntity(this.trackViewEntity.getSelectedEntity(), -this.trackViewEntity.getCurrentPosition())
    }

    fun checkSplitTrslEntity() {
        if (EditTrslEntityFragment.instance == null || this.trackViewEntity.getSelectedEntity() == null) {
            return
        }
        EditTrslEntityFragment.instance.checkSplitEntity(this.trackViewEntity.getSelectedEntity(), -this.trackViewEntity.getCurrentPosition())
    }

    fun checkSplitAudio() {
        if (EditMediaFragment.instance == null || !(this.trackViewEntity.getSelectedEntity() is EntityAudio)) {
            return
        }
        var f: Float = -this.trackViewEntity.getCurrentPosition()
        EditMediaFragment.instance.checkSplit(this as EntityAudio.trackViewEntity.getSelectedEntity(), f)
    }

    private fun clearCallback() {
        this.iBismilahEntityCallback = null
        this.iEditSName = null
        this.iEditMultipleCallback = null
        this.iEditMediaCallback = null
        this.iEditTrstEntityCallback = null
        this.iEditEntityCallback = null
        this.iChangeBgCallback = null
        this.iTrimLineCallback = null
        this.iIpadEditCallback = null
        this.iDimensionCallback = null
        this.searchAyaResult = null
        this.iFontCallback = null
        this.launchVideoExtract = null
        this.launchChoiceBgActivity = null
        this.launchVideo = null
        this.launchImg = null
        this.activityLauncher = null
        this.onBackPressedCallback = null
        this.iAddQuran = null
        this.iAudioCallback = null
        this.iTransitionCallback = null
        this.iTransitionBismilahCallback = null
        this.nameReaderResult = null
        this.iQuranIconCallback = null
        this.launchCropActivity = null
        this.editSurahNameResult = null
        this.iEdiTextCallback = null
        this.editTrslResult = null
    }

    fun addUpdateAnim(entityBismilahTimeline: EntityBismilahTimeline, entityBismilahTimeline2: EntityBismilahTimeline) {
        if (entityBismilahTimeline == null) {
            return
        }
        if (entityBismilahTimeline.getTransition() == null) {
            entityBismilahTimeline.setTransition(Transition())
        }
        entityBismilahTimeline.getTransition().setOut(entityBismilahTimeline2.getTransition().isOut())
        entityBismilahTimeline.getTransition().setType_out(entityBismilahTimeline2.getTransition().getType_out())
        entityBismilahTimeline.getTransition().setDuration_out(entityBismilahTimeline2.getTransition().getDuration_out())
        entityBismilahTimeline.getTransition().setIn(entityBismilahTimeline2.getTransition().isIn())
        entityBismilahTimeline.getTransition().setType_in(entityBismilahTimeline2.getTransition().getType_in())
        entityBismilahTimeline.getTransition().setDuration_in(entityBismilahTimeline2.getTransition().getDuration_in())
    }

    fun addUpdateAnim(entityBismilahTimeline: EntityBismilahTimeline, entityQuranTimeline: EntityQuranTimeline) {
        if (entityBismilahTimeline == null) {
            return
        }
        if (entityBismilahTimeline.getTransition() == null) {
            entityBismilahTimeline.setTransition(Transition())
        }
        entityBismilahTimeline.getTransition().setOut(entityQuranTimeline.getTransition().isOut())
        entityBismilahTimeline.getTransition().setType_out(entityQuranTimeline.getTransition().getType_out())
        entityBismilahTimeline.getTransition().setDuration_out(entityQuranTimeline.getTransition().getDuration_out())
        entityBismilahTimeline.getTransition().setIn(entityQuranTimeline.getTransition().isIn())
        entityBismilahTimeline.getTransition().setType_in(entityQuranTimeline.getTransition().getType_in())
        entityBismilahTimeline.getTransition().setDuration_in(entityQuranTimeline.getTransition().getDuration_in())
    }

    fun start() {
        if (this.mTemplate.getIpad_type() == IpadType.RECT.ordinal || this.mTemplate.getIpad_type() == IpadType.ROUND_RECT.ordinal || this.mTemplate.getIpad_type() == IpadType.CASSET_IMG_BLUR.ordinal || this.mTemplate.getIpad_type() == IpadType.CASSET.ordinal) {
            return
        }
        this.isOnScroll = false
        val smoothVideoAnimator = object : SmoothVideoAnimator(this.trackViewEntity, this.mTemplate, 25, object : SmoothVideoAnimator.FrameUpdateListener {
            override fun onAnimationEnd() {
            }

            override fun onFrameUpdate(str: String) {
                synchronized(frameLock) {
                    this@EngineActivity.pendingFramePath = str
                    if (!this@EngineActivity.isProcessingFrame) {
                        this@EngineActivity.isProcessingFrame = true
                        this@EngineActivity.executor.execute(this@EngineActivity.frameProcessorRunnable)
                    }
                }
            }
        })
        this.animator_frame_video = smoothVideoAnimator
        smoothVideoAnimator.start()
    }

    fun stop() {
        this.blurredImageView.setDrawingSquareVideo(false)
        val smoothVideoAnimator = this.animator_frame_video
        if (smoothVideoAnimator != null) {
            smoothVideoAnimator.stop()
        }
    }

    fun updateFrame() {
        val template = this.mTemplate
        if (template == null || !template.isVideoSquare() || this.mTemplate.getIpad_type() == IpadType.RECT.ordinal || this.mTemplate.getIpad_type() == IpadType.ROUND_RECT.ordinal || this.mTemplate.getIpad_type() == IpadType.CASSET_IMG_BLUR.ordinal || this.mTemplate.getIpad_type() == IpadType.CASSET.ordinal || this.mIsPlaying) {
            return
        }
        var max: Int = max(1, Math.round((this.trackViewEntity.getCurrent_cursur_position() / 1000.0f) * 25.0f))
        var min: Int = min(this.mTemplate.getDuration_video_media() * 25, this.trackViewEntity.getDuration() * 25)
        if (max > min) {
            max = ((max - 1) % min) + 1
        }
        var str: String? = max < 10 ? "frame_000" + max + ".jpg" : max < 100 ? "frame_00" + max + ".jpg" : max < 1000 ? "frame_0" + max + ".jpg" : "frame_" + max + ".jpg"
        this.isOnScroll = true
        updateSquareBitmap(File(this.mTemplate.getFolder_template() + "/VideoFrame", str).getAbsolutePath())
    }

    /* JADX WARN: Multi-variable type inference failed */
    fun processFrame(str: String) {
        Bitmap cropTo16x9
        try {
            if (!(this.isOnScroll && this.mIsPlaying) && this.mIsPlaying) {
                var height: Int = this.blurredImageView.getHeight()
                var bitmap: Bitmap? = Glide as Bitmap.with((FragmentActivity) this).asBitmap().load(str).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(height, height).submit().get()
                if (bitmap == null) {
                    return
                }
                if (this.mTemplate.getIpad_type() != IpadType.BLACK_LAYER.ordinal && this.mTemplate.getIpad_type() != IpadType.GRADIENT.ordinal && this.mTemplate.getIpad_type() != IpadType.MASK_BRUSH.ordinal && this.mTemplate.getIpad_type() != IpadType.BLUE_TYPE.ordinal && this.mTemplate.getIpad_type() != IpadType.CASSET_IMG.ordinal) {
                    if (this.mTemplate.getIpad_type() != IpadType.IPAD.ordinal && this.mTemplate.getIpad_type() != IpadType.IPAD_UNBLUR.ordinal && this.mTemplate.getIpad_type() != IpadType.BOTTOM_RECT.ordinal && this.mTemplate.getIpad_type() != IpadType.IPAD_CLASSIC.ordinal && this.mTemplate.getIpad_type() != IpadType.IPAD_NEOMORPHIC.ordinal) {
                        var width: Int = (this.blurredImageView.getIpad_rect().width() * 0.87530595f)
                        var i: Int = (width * 1.13f)
                        var min: Int = (min(width, i) * 0.10800001f)
                        var round: Int = Math.round(this.blurredImageView.getBitmapOriginal().getWidth() * this.mTemplate.getX_square())
                        var round2: Int = Math.round(this.blurredImageView.getBitmapOriginal().getHeight() * this.mTemplate.getY_square())
                        var i2: Int = width + round
                        if (i2 > this.blurredImageView.getBitmapOriginal().getWidth()) {
                            round -= i2 - this.blurredImageView.getBitmapOriginal().getWidth()
                            i2 = this.blurredImageView.getBitmapOriginal().getWidth()
                        }
                        var i3: Int = i + round2
                        if (i3 > this.blurredImageView.getBitmapOriginal().getHeight()) {
                            round2 -= i3 - this.blurredImageView.getBitmapOriginal().getHeight()
                            i3 = this.blurredImageView.getBitmapOriginal().getHeight()
                        }
                        if (round < 0) {
                            round = 0
                        }
                        if (round2 < 0) {
                            round2 = 0
                        }
                        var rect = Rect(round, round2, i2, i3)
                        var width2: Int = (this.blurredImageView.getBitmapOriginal().getWidth() * this.mTemplate.getWidth_square())
                        var height2: Int = (this.blurredImageView.getBitmapOriginal().getHeight() * this.mTemplate.getHeight_square())
                        cropTo16x9 = UtilsBitmap.cropToSquareWithRoundCorners(bitmap, rect, min, width2, height2)
                        rect.right = rect.left + width2
                        rect.bottom = rect.top + height2
                        this.blurredImageView.setRectSquare(rect)
                    } else {
                        cropTo16x9 = UtilsBitmap.cropToSquareWithRoundCornersPlusScale(bitmap, this.blurredImageView.getRectSquare(), this.blurredImageView.getRadius_square(), this.blurredImageView.getBitmapSquare().getWidth(), this.blurredImageView.getBitmapSquare().getHeight())
                    }
                    runOnUiThread(object : Runnable() { // from class: Runnable
                        override
                        void run() {
                            this@EngineActivity.cropTo16x9)
                        }
                    })
                }
                if (this.mTemplate.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                    cropTo16x9 = BitmapCropper.cropTo9x16(bitmap, this.blurredImageView.getW(), this.blurredImageView.getH())
                } else if (this.mTemplate.geTypeResize() == ResizeType.SQUARE.ordinal) {
                    cropTo16x9 = BitmapCropper.cropTo1x1(bitmap, this.blurredImageView.getW(), this.blurredImageView.getH())
                } else {
                    cropTo16x9 = BitmapCropper.cropTo16x9(bitmap, this.blurredImageView.getW(), this.blurredImageView.getH())
                }
                runOnUiThread(object : Runnable() { // from class: Runnable
                    override
                    void run() {
                        this@EngineActivity.cropTo16x9)
                    }
                })
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

 fun m646x854d6eda(bitmap: Bitmap) {
        this.blurredImageView.setBitmapSquare(bitmap)
        if (!this.isOnScroll) {
            this.blurredImageView.setDrawingSquareVideo(true)
        }
        this.blurredImageView.invalidate()
    }
}
