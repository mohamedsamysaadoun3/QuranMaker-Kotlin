package hazem.nurmontage.videoquran.fragment

import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.SpinnerAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.IconQuranAdabters
import hazem.nurmontage.videoquran.databinding.FragmentAddQuranBinding
import hazem.nurmontage.videoquran.model.RecitersModel
import hazem.nurmontage.videoquran.utils.LocaleHelper
import hazem.nurmontage.videoquran.utils.MyPreferences
import hazem.nurmontage.videoquran.utils.NetworkUtils
import hazem.nurmontage.videoquran.utils.QuranPreference
import hazem.nurmontage.videoquran.utils.QuranReader
import hazem.nurmontage.videoquran.views.CheckboxCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Fragment for adding Quran ayahs to the video project.
 *
 * Provides a complete UI for selecting surah, ayah range, reciter, and
 * translation. When the user confirms, the fragment reads the Quran text
 * from assets and splits long ayahs into manageable chunks for the
 * timeline, invoking the [IAddQuran] callback for each entity.
 *
 * Key features:
 * - Surah spinner with ayah count auto-population
 * - From/To ayah spinners that sync in certain selection modes
 * - Reciter selection with network availability check
 * - Translation language selection (8 supported languages)
 * - Quran icon picker (Hafs, Shamerli, Nour Hode, Amiri)
 * - Bismillah inclusion checkbox
 * - Upload recitation support (custom audio + reader name)
 * - Recursive ayah processing with automatic chunk splitting
 * - State persistence via QuranPreference and MyPreferences
 *
 * Originally: AddQuranFragment.java (616 lines)
 */
class AddQuranFragment : Fragment {

    companion object {
        @Volatile
        @JvmStatic var instance: AddQuranFragment? = null

        fun getInstance(
            iAddQuran: IAddQuran?,
            resources: Resources?,
            uri: Uri?,
            pathVideoCopy: String?,
            readerName: String?
        ): AddQuranFragment {
            if (instance == null) {
                instance = AddQuranFragment(iAddQuran, resources, uri, pathVideoCopy, readerName)
            }
            return instance!!
        }

        fun getInstance(
            iAddQuran: IAddQuran?,
            resources: Resources?
        ): AddQuranFragment {
            if (instance == null) {
                instance = AddQuranFragment(iAddQuran, resources)
            }
            return instance!!
        }
    }

    /**
     * Callback interface for Quran ayah addition events.
     *
     * The host Activity/Fragment implements this to receive ayah data,
     * translation text, reciter information, and lifecycle events.
     */
    interface IAddQuran {
        /** Called for each ayah chunk with full text and index data. */
        fun onAdd(
            str: String, str2: String, str3: String?, str4: String?,
            i: Int, i2: Int, str5: String, i3: Int, i4: Int
        )

        /** Called when a custom recitation reader name is selected. */
        fun onAddReaderName(str: String?, str2: String?, uri: Uri?)

        /** Called with the translation text for an ayah. */
        fun onAddTranslation(str: String, i: Int, z: Boolean)

        /** Called when bismillah should be included. */
        fun onBismilah()

        /** Called when the user cancels. */
        fun onCancel()

        /** Called when all ayahs are done (with custom recitation upload). */
        fun onDone(str: String, i: Int, str2: String?, uri: Uri?, str3: String?)

        /** Called when all ayahs are done (with online reciter). */
        fun onDone(str: String, i: Int, str2: String?, list: List<RecitersModel>?)

        /** Called when the ayah limit is exceeded. */
        fun onErrorLimitation()

        /** Called when the search button is pressed. */
        fun onSearch()

        /** Called for video copyright check. */
        fun onVuCopyRight()

        /** Called to show progress indicator. */
        fun progress()

        /** Called when the upload recitation button is pressed. */
        fun uploadRecitation()
    }

    private var adapterFromAyah: ArrayAdapter<String>? = null
    private var adapterToAyah: ArrayAdapter<String>? = null
    private var arrayCount: IntArray? = null
    private var arrayIdentifier: Array<String>? = null
    private var arrayReciters: Array<String>? = null
    private var arraySurah: Array<String>? = null
    private var arrayTranslation: Array<String>? = null
    private var fragmentBinding: FragmentAddQuranBinding? = null
    private var iAddQuran: IAddQuran? = null
    private var iconQuranAdabters: IconQuranAdabters? = null
    private var includeBismilah: CheckboxCustumFont? = null
    private var isFromSearch: Boolean = false
    private var isFromSelectReciters: Boolean = false
    private var iv_done_upload: ImageView? = null
    private var layoutConnection: LinearLayout? = null
    private var path_video_copy: String? = null
    private var quranPreference: QuranPreference? = null
    private var quranReader: QuranReader? = null
    private var reader_name: String? = null
    private var resources: Resources? = null
    private var spinnerFrom: Spinner? = null
    private var spinnerReciters: Spinner? = null
    private var spinnerSurah: Spinner? = null
    private var spinnerTo: Spinner? = null
    private var spinnerTranslation: Spinner? = null
    private var surah_hint: String? = null
    private var tv_reader_name: TextCustumFont? = null
    private var uri_recitation: Uri? = null
    private var icon: String = "hafes"
    private var recitersModels: MutableList<RecitersModel> = mutableListOf()
    private var current_pos: Int = -1
    private val translation_name: Array<String> = arrayOf(
        "en.hilali.txt", "fr.hamidullah.txt", "ur.maududi.txt",
        "tr.ozturk.txt", "de.bubenheim.txt", "id.indonesian.txt",
        "fa.fooladvand.txt", "bn.bengali.txt"
    )
    private var isInit: Boolean = true
    private var isFromSelect: Boolean = true

    private var iconQuranCallback: IconQuranAdabters.IIconQuranCallback? =
        object : IconQuranAdabters.IIconQuranCallback {
            override fun onIcon(str: String) {
                icon = str
            }
        }

    private var onFromAyaSelectedListener: AdapterView.OnItemSelectedListener? =
        object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, pos: Int, id: Long
            ) {
                if (isFromSearch) {
                    spinnerTo?.setSelection(quranPreference?.getTo() ?: 0)
                    isFromSearch = false
                } else {
                    if (!isFromSelect) {
                        if (spinnerTo?.selectedItemPosition != pos) {
                            spinnerTo?.setSelection(pos)
                        }
                    } else {
                        isFromSelect = false
                    }
                }
            }
        }

    private var onSurahSelectedListener: AdapterView.OnItemSelectedListener? =
        object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, pos: Int, id: Long
            ) {
                if (pos == current_pos) return

                val ayahCount = if (isInit) {
                    arrayCount?.get(quranPreference?.getSurah() ?: 0) ?: 0
                } else {
                    arrayCount?.get(pos) ?: 0
                }

                val ayahList = mutableListOf<String>()
                for (i in 1..ayahCount) {
                    ayahList.add(i.toString())
                }
                adapterFromAyah?.clear()
                adapterFromAyah?.addAll(ayahList)
                adapterToAyah?.clear()
                adapterToAyah?.addAll(ayahList)

                if (isInit) {
                    try {
                        spinnerSurah?.setSelection(quranPreference?.getSurah() ?: 0, true)
                        spinnerFrom?.setSelection(quranPreference?.getFrom() ?: 0, false)
                        spinnerTo?.setSelection(quranPreference?.getTo() ?: 0, false)
                        spinnerReciters?.setSelection(quranPreference?.getNameReader() ?: 0, false)
                        spinnerTranslation?.setSelection(quranPreference?.getTranslation() ?: 0, false)
                    } catch (_: Exception) {}
                    isInit = false
                } else {
                    spinnerTo?.setSelection(0, false)
                    spinnerFrom?.setSelection(0, false)
                }
                current_pos = spinnerSurah?.selectedItemPosition ?: -1
            }
        }

    constructor()

    constructor(iAddQuran: IAddQuran?, resources: Resources?) {
        this.iAddQuran = iAddQuran
        this.resources = resources
    }

    constructor(
        iAddQuran: IAddQuran?,
        resources: Resources?,
        uri: Uri?,
        pathVideoCopy: String?,
        readerName: String?
    ) {
        this.iAddQuran = iAddQuran
        this.resources = resources
        this.uri_recitation = uri
        this.path_video_copy = pathVideoCopy
        this.reader_name = readerName
    }

    private fun setSystemBarsColorBlack() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        setSystemBarsColorBlack()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bind = FragmentAddQuranBinding.inflate(inflater, container, false)
        fragmentBinding = bind
        val root: RelativeLayout = bind.root

        if (resources != null && iAddQuran != null) {
            quranPreference = QuranPreference(requireContext())
            quranReader = QuranReader(requireContext())
            surah_hint = if (LocaleHelper.getLanguage(requireContext()) == "ar") "سورة " else "Surah "

            val ivDone = root.findViewById<ImageView>(R.id.iv_done)
            iv_done_upload = ivDone
            if (uri_recitation != null) {
                ivDone.visibility = View.VISIBLE
            }

            root.findViewById<TextCustumFont>(R.id.tv_surah)
                .setText(resources?.getString(R.string.tv_surah))
            root.findViewById<TextCustumFont>(R.id.tv_icon)
                .setText(resources?.getString(R.string.quran_icon))
            root.findViewById<TextCustumFont>(R.id.tv_add_bismilah)
                .setText(resources?.getString(R.string.add_bismilah))
            root.findViewById<TextCustumFont>(R.id.tv_end_ayah)
                .setText(resources?.getString(R.string.to))
            root.findViewById<TextCustumFont>(R.id.tv_hint_reader)
                .setText(resources?.getString(R.string.tv_hint_reader))
            root.findViewById<TextCustumFont>(R.id.tv_translation)
                .setText(resources?.getString(R.string.translation))

            arraySurah = resources?.getStringArray(R.array.surah_names_merged)
            arrayCount = resources?.getIntArray(R.array.surah_count)
            arrayIdentifier = resources?.getStringArray(R.array.identifier)
            arrayReciters = resources?.getStringArray(R.array.reciters)
            arrayTranslation = resources?.getStringArray(R.array.translation_name)

            val checkbox = root.findViewById<CheckboxCustumFont>(R.id.checkbox)
            includeBismilah = checkbox
            checkbox.isChecked = MyPreferences.isIncludeBismilah(requireContext())

            root.findViewById<View>(R.id.add_bismilah).setOnClickListener {
                includeBismilah?.isChecked = !(includeBismilah?.isChecked ?: false)
            }

            // Surah spinner
            spinnerSurah = root.findViewById(R.id.sura_name)
            val surahAdapter = ArrayAdapter(
                requireContext(), R.layout.row_spinner_aya, arraySurah!!
            )
            surahAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSurah?.onItemSelectedListener = onSurahSelectedListener
            spinnerSurah?.adapter = surahAdapter as SpinnerAdapter
            spinnerSurah?.let {
                it.dropDownVerticalOffset = it.height * (-10)
            }

            // From ayah spinner
            spinnerFrom = root.findViewById(R.id.aya_from)
            val fromAdapter = ArrayAdapter<String>(
                requireContext(), R.layout.row_spinner_aya
            )
            adapterFromAyah = fromAdapter
            fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFrom?.onItemSelectedListener = onFromAyaSelectedListener
            spinnerFrom?.adapter = adapterFromAyah as SpinnerAdapter

            // To ayah spinner
            spinnerTo = root.findViewById(R.id.aya_to)
            val toAdapter = ArrayAdapter<String>(
                requireContext(), R.layout.row_spinner_aya
            )
            adapterToAyah = toAdapter
            toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTo?.adapter = adapterToAyah as SpinnerAdapter

            // Reciters spinner
            spinnerReciters = root.findViewById(R.id.spinner_reciters)
            val recitersAdapter = ArrayAdapter(
                requireContext(), R.layout.row_spinner_aya, arrayReciters!!
            )
            recitersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerReciters?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, pos: Int, id: Long
                ) {
                    if (isFromSelectReciters) {
                        goneReaderNameUpload()
                    }
                    isFromSelectReciters = true
                }
            }
            spinnerReciters?.adapter = recitersAdapter as SpinnerAdapter

            // Translation spinner
            spinnerTranslation = root.findViewById(R.id.spinner_translation)
            val translationAdapter = ArrayAdapter(
                requireContext(), R.layout.row_spinner_aya, arrayTranslation!!
            )
            translationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTranslation?.adapter = translationAdapter as SpinnerAdapter

            // No internet layout
            layoutConnection = root.findViewById(R.id.hint_no_internet)

            // Done button — starts ayah processing on background thread
            root.findViewById<View>(R.id.btn_done).setOnClickListener {
                if (iAddQuran != null) {
                    val fromPos = spinnerFrom?.selectedItemPosition ?: 0
                    val toPos = spinnerTo?.selectedItemPosition ?: 0
                    val surahPos = spinnerSurah?.selectedItemPosition ?: 0
                    val selectedItemPosition = fromPos + 1
                    val selectedItemPosition2 = toPos + 1
                    val selectedItemPosition3 = surahPos + 1
                    Thread {
                        iAddQuran?.progress()
                        if (includeBismilah != null && includeBismilah?.isChecked == true) {
                            iAddQuran?.onBismilah()
                        }
                        addAyaEntityRecursive(
                            selectedItemPosition, selectedItemPosition2, selectedItemPosition3
                        )
                    }.start()
                }
            }

            // Cancel button
            root.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                if (iAddQuran != null) {
                    iAddQuran?.onCancel()
                }
            }

            // Search button
            root.findViewById<View>(R.id.btn_search).setOnClickListener {
                savePreference()
                if (iAddQuran != null) {
                    iAddQuran?.onSearch()
                }
            }

            // Upload recitation button
            root.findViewById<View>(R.id.btn_upload).setOnClickListener {
                if (iAddQuran != null) {
                    iAddQuran?.uploadRecitation()
                }
                iAddQuran = null
            }

            // Reader name text view
            val tvReader = root.findViewById<TextCustumFont>(R.id.tv_reader)
            tv_reader_name = tvReader
            tvReader.setOnClickListener {
                if (iAddQuran == null || uri_recitation == null) return@setOnClickListener
                iAddQuran?.onAddReaderName(reader_name, path_video_copy, uri_recitation)
            }

            if (reader_name.isNullOrEmpty()) {
                reader_name = "-"
                tv_reader_name?.setTextColor(-1)
            } else {
                tv_reader_name?.paint?.isUnderlineText = true
                tv_reader_name?.text = reader_name
            }

            initIconRv(root)
        }
        return root
    }

    /**
     * Initialises the horizontal RecyclerView for Quran icon selection.
     * Displays four icon options (hafes, shamerli, nour_hode, amiri)
     * and restores the previously selected icon index.
     */
    private fun initIconRv(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recyclerView.itemAnimator = null
        recyclerView.setHasFixedSize(true)

        val iconList = arrayListOf("hafes", "shamerli", "nour_hode", "amiri")
        val adapter = IconQuranAdabters(
            iconQuranCallback, iconList, MyPreferences.getLastIconIndex(requireContext())
        )
        iconQuranAdabters = adapter
        icon = iconList[adapter.getSelect()]
        recyclerView.adapter = adapter
    }

    /**
     * Hides the reader name upload UI elements and resets the recitation state.
     * Called when the user selects a different reciter from the spinner.
     */
    private fun goneReaderNameUpload() {
        uri_recitation = null
        iv_done_upload?.visibility = View.GONE
        tv_reader_name?.text = "-"
        tv_reader_name?.paint?.isUnderlineText = false
        tv_reader_name?.setOnClickListener(null)
    }

    override fun onResume() {
        super.onResume()
        try {
            if (NetworkUtils.isNetworkAvailable(requireContext())) {
                spinnerReciters?.visibility = View.VISIBLE
                spinnerReciters?.isEnabled = true
                layoutConnection?.visibility = View.GONE
            } else {
                spinnerReciters?.isEnabled = false
                spinnerReciters?.visibility = View.INVISIBLE
                layoutConnection?.visibility = View.VISIBLE
            }
        } catch (_: Exception) {}
    }

    /**
     * Restores spinner positions from saved preferences when returning
     * from a search. Sets the isFromSearch flag so the from-ayah
     * listener can properly sync the to-ayah spinner.
     */
    fun addAyaIndex() {
        try {
            isFromSearch = true
            val surah = quranPreference?.getSurah() ?: 0
            current_pos = surah
            spinnerSurah?.setSelection(surah, false)
            val ayahCount = arrayCount?.get(quranPreference?.getSurah() ?: 0) ?: 0
            val ayahList = mutableListOf<String>()
            for (i in 1..ayahCount) {
                ayahList.add(i.toString())
            }
            adapterFromAyah?.clear()
            adapterFromAyah?.addAll(ayahList)
            adapterToAyah?.clear()
            adapterToAyah?.addAll(ayahList)
            spinnerFrom?.setSelection(quranPreference?.getFrom() ?: 0, false)
            spinnerReciters?.setSelection(quranPreference?.getNameReader() ?: 0, false)
        } catch (_: Exception) {}
    }

    /**
     * Updates the reader name and recitation URI after an upload.
     * Shows the done indicator if a valid URI is provided, and
     * underlines the reader name if it's non-empty.
     */
    fun setNameReader(str: String?, uri: Uri?, str2: String?) {
        uri_recitation = uri
        path_video_copy = str2
        if (uri != null) {
            iv_done_upload?.visibility = View.VISIBLE
        }
        var name = str
        if (name.isNullOrEmpty()) {
            tv_reader_name?.paint?.isUnderlineText = false
            name = "-"
        } else {
            tv_reader_name?.paint?.isUnderlineText = true
        }
        reader_name = name
        tv_reader_name?.text = name
    }

    /**
     * Splits an ayah text into chunks of approximately 5 "long" words
     * (words with length > 1 character) for display on the timeline.
     *
     * Short ayahs (4 or fewer words) are added as a single entity
     * with the " نص" suffix. Long ayahs are split so each chunk
     * contains roughly 5 long words, with intermediate chunks having
     * text-length and aya-number set to -1 (not final) and the last
     * chunk carrying the verse number marker.
     *
     * @param str The ayah text to split
     * @param str2 Optional comma-separated word-level translation
     * @param ayaNum The ayah number (1-based within the surah)
     */
    fun splitAya(str: String, str2: String?, ayaNum: Int) {
        val trim = str.trim()
        val split = trim.replace("\\s*([\u06D6-\u06ED])".toRegex(), "$1")
            .trim().split("\\s+".toRegex()).toTypedArray()
        val split2 = str2?.split(",")?.toTypedArray()

        val space = " "
        val nasMark = " نص"

        if (split.size <= 4) {
            iAddQuran?.onAdd(
                str + nasMark, trim,
                str2?.replace(",", " "),
                str2, str.length, ayaNum, icon, 0, split.size
            )
            return
        }

        val sb = StringBuilder()
        var longWordThreshold = 1
        var lastIndex = split.size - 1
        var longWordCount = 0
        var totalWordCount = 0
        var loopIndex = 0
        var chunkStart = 0

        while (loopIndex < split.size) {
            val word = split[loopIndex]
            sb.append(word).append(space)
            if (word.length > longWordThreshold) {
                longWordCount++
            }
            val newTotalCount = totalWordCount + 1

            if (longWordCount == 5) {
                val endIndex = (chunkStart + longWordCount) - (newTotalCount - longWordCount)

                if (loopIndex == lastIndex) {
                    // Last word in the aya — this chunk carries the verse number
                    val chunkText = sb.toString().trim()
                    iAddQuran?.onAdd(
                        chunkText + nasMark, trim,
                        if (split2 != null) getWords(split2, chunkStart, endIndex) else null,
                        str2, chunkText.length, ayaNum, icon, chunkStart, endIndex
                    )
                } else {
                    // Middle chunk — not final, so text-length and aya-number are -1
                    val chunkText = sb.toString().trim()
                    val transWords = if (split2 != null)
                        getWords(split2, chunkStart, endIndex) else null
                    iAddQuran?.onAdd(
                        chunkText, trim, transWords, str2,
                        -1, -1, icon, chunkStart, chunkStart + newTotalCount
                    )
                }

                chunkStart += newTotalCount
                sb.setLength(0)
                longWordCount = 0
                totalWordCount = 0
            } else {
                totalWordCount = newTotalCount
            }
            loopIndex++
        }

        // Remaining words after the last 5-long-word boundary
        if (sb.isNotEmpty()) {
            val chunkText = sb.toString().trim()
            val transWords = if (split2 != null)
                getWords(split2, split2.size - totalWordCount, split2.size) else null
            iAddQuran?.onAdd(
                chunkText + nasMark, trim, transWords, str2,
                chunkText.length, ayaNum, icon, chunkStart, chunkStart + totalWordCount
            )
        }
    }

    /**
     * Extracts a space-joined substring from a word array, with bounds clamping.
     * Used to get the translation words that correspond to a specific chunk
     * of the Arabic ayah text.
     *
     * @param strArr The full array of translation words
     * @param start The start index (clamped to 0 if negative)
     * @param end The end index (clamped to array length if over)
     * @return Space-joined words from the sub-range, or empty string if invalid
     */
    fun getWords(strArr: Array<String>?, start: Int, end: Int): String {
        if (strArr == null || strArr.isEmpty()) return ""
        var s = start
        var e = end
        if (s < 0) s = 0
        if (e > strArr.size) e = strArr.size
        if (s >= e) return ""
        return strArr.copyOfRange(s, e).joinToString(" ")
    }

    /**
     * Recursively processes each ayah in the selected range.
     *
     * For each ayah, this method:
     * 1. Reads the Arabic text from Quran assets
     * 2. Optionally reads the translation text
     * 3. Splits the ayah into chunks via [splitAya]
     * 4. Adds the translation text via [IAddQuran.onAddTranslation]
     * 5. Adds the reciter model for audio resolution
     * 6. On the last ayah, calls [IAddQuran.onDone] with the appropriate
     *    overload (custom upload vs online reciter)
     *
     * @param from Current ayah number (1-based), incremented on each recursive call
     * @param to The last ayah number to process
     * @param surahNumber The surah number (1-based)
     */
    fun addAyaEntityRecursive(from: Int, to: Int, surahNumber: Int) {
        try {
            val ayahText = quranReader?.getAyahText(surahNumber, from) ?: return
            val translationPos = spinnerTranslation?.selectedItemPosition ?: 0
            val translationAyahText = if (translationPos > 0) {
                quranReader?.getTranslationAyahText(
                    translation_name[translationPos - 1], surahNumber, from
                )
            } else null
            splitAya(ayahText, null, from)
            if (translationAyahText != null) {
                iAddQuran?.onAddTranslation(translationAyahText, from, translationPos == 1)
            }
            if (iAddQuran != null) {
                if (spinnerReciters?.isEnabled == true) {
                    val identifier = arrayIdentifier?.get(spinnerReciters?.selectedItemPosition ?: 0) ?: return
                    recitersModels.add(RecitersModel(identifier, surahNumber, from))
                }
                if (from >= to) {
                    val surahName = arraySurah?.get(spinnerSurah?.selectedItemPosition ?: 0) ?: ""
                    val surahIndex = (spinnerSurah?.selectedItemPosition ?: 0) + 1
                    if (uri_recitation != null) {
                        iAddQuran?.onDone(
                            surah_hint + surahName, surahIndex,
                            reader_name, uri_recitation, path_video_copy
                        )
                    } else {
                        val reciterName = arrayReciters?.get(spinnerReciters?.selectedItemPosition ?: 0) ?: ""
                        iAddQuran?.onDone(
                            surah_hint + surahName, surahIndex,
                            reciterName, recitersModels
                        )
                    }
                    return
                }
            }
            addAyaEntityRecursive(from + 1, to, surahNumber)
        } catch (e: Exception) {
            // BEFORE: Silent catch — spinner stays forever when any exception occurs
            // WHY_CHANGED: Exceptions (like NPE from onAdd callback) were swallowed silently
            // FIXED_BY: Log the error and ensure spinner is stopped
            // REF: Original Java had e.printStackTrace() here
            // VISUAL_IMPACT: Spinner no longer runs forever on error
            e.printStackTrace()
            iAddQuran?.onCancel()
        }
    }

    /**
     * Saves the current spinner selections and checkbox states to
     * SharedPreferences so they can be restored when the fragment
     * is recreated.
     */
    private fun savePreference() {
        quranPreference?.savePreferences(
            spinnerSurah?.selectedItemPosition ?: 0,
            spinnerFrom?.selectedItemPosition ?: 0,
            spinnerTo?.selectedItemPosition ?: 0,
            spinnerReciters?.selectedItemPosition ?: 0,
            spinnerTranslation?.selectedItemPosition ?: 0
        )
        try {
            MyPreferences.putIndexLastIcon(requireContext(), iconQuranAdabters?.getSelect() ?: 0)
        } catch (_: Exception) {}
        try {
            MyPreferences.putIncludeBismilah(requireContext(), includeBismilah?.isChecked ?: false)
        } catch (_: Exception) {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        savePreference()
        QuranPreference.saveLastSearch(requireContext(), null)
        iAddQuran?.onCancel()
        onFromAyaSelectedListener = null
        onSurahSelectedListener = null
        fragmentBinding = null
        instance = null
        iconQuranCallback = null
    }
}
