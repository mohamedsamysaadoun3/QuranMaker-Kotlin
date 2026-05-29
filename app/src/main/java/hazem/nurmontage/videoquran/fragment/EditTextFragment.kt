package hazem.nurmontage.videoquran.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.WordAyaAdabter
import hazem.nurmontage.videoquran.databinding.FragmentEditTextBinding
import hazem.nurmontage.videoquran.entity_timeline.EntityQuranTimeline
import hazem.nurmontage.videoquran.model.WordModel
import hazem.nurmontage.videoquran.model.data.QuranEntity
import hazem.nurmontage.videoquran.views.ArrowOverlayDecoration

/**
 * Fragment for editing Quran aya text at the word-by-word level.
 *
 * Displays the complete aya as a horizontally-scrollable list of selectable
 * word chips. The user can toggle individual words on/off to control which
 * words appear in the final video output. When the user is done editing,
 * the selected words are reassembled into the [QuranEntity.txt] field and
 * the entity's word-index boundaries are updated.
 *
 * Key behaviours:
 * - Words that are part of the original selection appear highlighted;
 *   other words appear dimmed.
 * - When start/end word indices are equal (single-selection mode), the
 *   fragment performs a position-based search to match the entity's text
 *   against the complete aya.
 * - An [ArrowOverlayDecoration] is added to the RecyclerView for visual
 *   guidance.
 * - The close button signals the host via [IEdiTextCallback.onDone].
 *
 * Converted from EditTextFragment.java (287 lines).
 */
class EditTextFragment : Fragment {

    companion object {
        @Volatile
        @JvmStatic var instance: EditTextFragment? = null

        fun getInstance(
            callback: IEdiTextCallback?,
            quranEntity: QuranEntity?
        ): EditTextFragment {
            if (instance == null) {
                instance = EditTextFragment(callback, quranEntity)
            }
            return instance!!
        }

        /**
         * Finds the index of the first digit character in [str].
         * Returns -1 if no digit is found or the string is null/empty.
         */
        fun findFirstDigitIndex(str: String?): Int {
            if (str.isNullOrEmpty()) return -1
            for (i in str.indices) {
                if (Character.isDigit(str[i])) return i
            }
            return -1
        }
    }

    /**
     * Callback interface for text editing events.
     * - [onUpdate]: called when the user toggles a word; provides the
     *   updated [QuranEntity] with its text, number, and indices refreshed.
     * - [onDone]: called when the close button is pressed; provides the
     *   owning [EntityQuranTimeline] so the host can finalise changes.
     */
    interface IEdiTextCallback {
        fun onDone(entityQuranTimeline: EntityQuranTimeline?)
        fun onUpdate(quranEntity: QuranEntity?)
    }

    private var fragmentBinding: FragmentEditTextBinding? = null
    private var iEditEntityCallback: IEdiTextCallback? = null
    private var iWordAya: WordAyaAdabter.IWordAya? = null
    private var quranEntity: QuranEntity? = null
    private var recyclerView: RecyclerView? = null
    private var wordAyaAdabter: WordAyaAdabter? = null

    constructor()

    constructor(iEditEntityCallback: IEdiTextCallback?, quranEntity: QuranEntity?) {
        this.iEditEntityCallback = iEditEntityCallback
        this.quranEntity = quranEntity
        this.iWordAya = object : WordAyaAdabter.IWordAya {
            override fun onClick() {
                if (this@EditTextFragment.iEditEntityCallback != null) {
                    var selectedAya = getSelectedAya()
                    val entity = this@EditTextFragment.quranEntity ?: return
                    val findFirstDigitIndex = if (entity.getNumber() != -1)
                        findFirstDigitIndex(selectedAya) else -1
                    if (findFirstDigitIndex != -1) {
                        val substring = selectedAya.substring(0, findFirstDigitIndex)
                        try {
                            var parseInt = selectedAya.substring(findFirstDigitIndex).toInt()
                            if (parseInt > 286) {
                                parseInt = 286
                            }
                            entity.setNumber(parseInt)
                            entity.setIndexNumber(findFirstDigitIndex)
                            selectedAya = "$substring نص"
                        } catch (_: Exception) {
                            selectedAya = substring
                        }
                    }
                    entity.setTxt(selectedAya)
                    entity.initPreset(entity.getmPreset())
                    this@EditTextFragment.iEditEntityCallback?.onUpdate(entity)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bind = FragmentEditTextBinding.inflate(inflater, container, false)
        fragmentBinding = bind
        val root: LinearLayout = bind.root

        if (quranEntity != null && iEditEntityCallback != null) {
            init(root)
            root.findViewById<View>(R.id.btn_close).setOnClickListener {
                iEditEntityCallback?.onDone(quranEntity?.getEntityQuran())
            }
        }
        return root
    }

    /**
     * Initialises the RecyclerView with the word list derived from
     * the entity's complete aya text. Two rendering modes exist:
     *
     * 1. **Single-selection mode** (startWord_index == endWord_index):
     *    The fragment matches the entity's current text against the
     *    complete aya to determine which words are selected, performing
     *    a position-based search through the aya character offsets.
     *
     * 2. **Range-selection mode** (startWord_index != endWord_index):
     *    Words within the [startWord_index, endWord_index) range are
     *    marked as selected; all others are unselected.
     *
     * Words equal to "-1" are filtered out (placeholder markers from
     * the ayah parser).
     */
    private fun init(view: View) {
        val entity = quranEntity ?: return
        val txt = buildDisplayText(entity)

        val completeAya = entity.getComplete_aya() ?: ""
        val startWordIndex = entity.getStartWord_index()
        val endWordIndex = entity.getEndWord_index()
        val split = completeAya
            .trim()
            .replace("\\s*([\u06D6-\u06ED])".toRegex(), "$1")
            .trim()
            .split("\\s+".toRegex())

        val arrayList = mutableListOf<WordModel>()

        if (startWordIndex == endWordIndex) {
            // Single-selection mode: match entity text against complete aya
            val split2 = txt
                .trim()
                .replace("\\s*([\u06D6-\u06ED])".toRegex(), "$1")
                .split("\\s+".toRegex())
            val indexOf = completeAya.indexOf(txt)
            var i2 = if (indexOf == 0) 1 else 0
            var i4 = 0
            var i5 = 0

            for (str in split) {
                if (str != "-1") {
                    if (i2 == 0) {
                        if (i4 == indexOf) {
                            i2 = 1
                        }
                        i4 += str.length + 1
                    }
                    if (i2 != 0 && i5 < split2.size) {
                        val equals = str == split2[i5]
                        arrayList.add(WordModel(str, equals))
                        if (equals) i5++
                    } else {
                        arrayList.add(WordModel(str, false))
                    }
                }
            }
        } else {
            // Range-selection mode
            for (i6 in split.indices) {
                if (split[i6] != "-1") {
                    arrayList.add(WordModel(split[i6], i6 >= startWordIndex && i6 < endWordIndex))
                }
            }
        }

        wordAyaAdabter = WordAyaAdabter(arrayList, iWordAya)
        val rv = view.findViewById<RecyclerView>(R.id.rv)
        recyclerView = rv
        rv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, true)
        rv.setHasFixedSize(true)
        rv.itemAnimator = null
        rv.adapter = wordAyaAdabter
        try {
            rv.scrollToPosition(startWordIndex)
        } catch (_: Exception) {
        }
        rv.addItemDecoration(ArrowOverlayDecoration(requireContext(), R.drawable.btn_on_back, 18))
    }

    /**
     * Updates the word list when the entity changes externally.
     * Rebuilds the display text and word selection state, then
     * refreshes the adapter.
     */
    fun update(quranEntity: QuranEntity?) {
        if (quranEntity == null) return
        this.quranEntity = quranEntity
        val txt = buildDisplayText(quranEntity)

        val completeAya = this.quranEntity?.getComplete_aya() ?: ""
        val startWordIndex = this.quranEntity?.getStartWord_index() ?: 0
        val endWordIndex = this.quranEntity?.getEndWord_index() ?: 0
        val split = completeAya
            .trim()
            .replace("\\s*([\u06D6-\u06ED])".toRegex(), "$1")
            .trim()
            .split("\\s+".toRegex())

        val arrayList = mutableListOf<WordModel>()

        if (startWordIndex == endWordIndex) {
            val split2 = txt
                .trim()
                .replace("\\s*([\u06D6-\u06ED])".toRegex(), "$1")
                .split("\\s+".toRegex())
            val indexOf = completeAya.indexOf(txt)
            var i2 = if (indexOf == 0) 1 else 0
            var i4 = 0
            var i5 = 0

            for (str in split) {
                if (str != "-1") {
                    if (i2 == 0) {
                        if (i4 == indexOf) {
                            i2 = 1
                        }
                        i4 += str.length + 1
                    }
                    if (i2 != 0 && i5 < split2.size) {
                        val equals = str == split2[i5]
                        arrayList.add(WordModel(str, equals))
                        if (equals) i5++
                    } else {
                        arrayList.add(WordModel(str, false))
                    }
                }
            }
        } else {
            for (i6 in split.indices) {
                if (split[i6] != "-1") {
                    arrayList.add(WordModel(split[i6], i6 >= startWordIndex && i6 < endWordIndex))
                }
            }
        }

        wordAyaAdabter?.setList(arrayList)
        try {
            recyclerView?.scrollToPosition(startWordIndex)
        } catch (_: Exception) {
        }
    }

    /**
     * Builds the display text for the entity, appending the verse number
     * if the entity has a valid index number.
     */
    private fun buildDisplayText(entity: QuranEntity): String {
        val indexNumber = entity.getIndexNumber()
        return if (indexNumber >= 0) {
            entity.getTxt()?.substring(0, minOf(indexNumber, entity.getTxt()?.length ?: 0))
                .orEmpty() + " " + entity.getNumber()
        } else {
            entity.getTxt().orEmpty()
        }
    }

    /**
     * Reconstructs the selected aya text from the word adapter's current
     * selection state. Also updates the entity's translation, start/end
     * word indices based on which words are currently selected.
     *
     * @return The selected aya text (trimmed), or an empty string if
     *   no words are selected.
     */
    private fun getSelectedAya(): String {
        val sb = StringBuilder()
        val sb2 = StringBuilder()
        val list = wordAyaAdabter?.getList() ?: return ""
        val split = quranEntity?.getTranslation_complete()?.split(",")

        var startIndex = -1
        var selectedCount = 0
        for (i3 in list.indices) {
            val wordModel = list[i3]
            if (wordModel.isSelected) {
                if (startIndex == -1) {
                    startIndex = i3
                }
                selectedCount++
                sb.append(wordModel.w).append(" ")
                if (split != null && i3 < split.size) {
                    sb2.append(split[i3]).append(" ")
                }
            }
        }

        if (sb2.isNotEmpty()) {
            quranEntity?.setTranslation(sb2.toString())
        } else {
            quranEntity?.setTranslation(null)
        }

        var endWordIndex = selectedCount + startIndex
        if (quranEntity?.getNumber() != -1) {
            endWordIndex++
        }
        quranEntity?.setEndWord_index(endWordIndex)
        quranEntity?.setStartWord_index(startIndex)

        return sb.toString().trim()
    }

    override fun onDestroyView() {
        fragmentBinding?.let {
            it.root.removeAllViews()
        }
        fragmentBinding = null
        iWordAya = null
        instance = null
        super.onDestroyView()
    }
}
