package hazem.nurmontage.videoquran.fragment

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.databinding.FragmentAddAudioBinding
import hazem.nurmontage.videoquran.views.TextCustumFont

/**
 * Bottom-sheet fragment for adding audio to a project.
 *
 * Presents three options to the user:
 * - **Upload**: Pick an audio file from the device
 * - **Extract**: Extract audio from an existing video
 * - **Close**: Dismiss the fragment
 *
 * Each option fires a callback on [IAudioCallback]. The singleton pattern
 * ensures only one instance exists at a time.
 *
 * Converted from AddAudioFragment.java (84 lines).
 */
class AddAudioFragment : Fragment {

    companion object {
        @Volatile
        @JvmStatic var instance: AddAudioFragment? = null

        fun getInstance(callback: IAudioCallback?, resources: Resources?): AddAudioFragment {
            if (instance == null) {
                instance = AddAudioFragment(callback, resources)
            }
            return instance!!
        }
    }

    /**
     * Callback interface for audio source selection events.
     */
    interface IAudioCallback {
        fun cancel()
        fun extract()
        fun upload()
    }

    private var addAudioBinding: FragmentAddAudioBinding? = null
    private var iAudioCallback: IAudioCallback? = null
    private var resourcesRef: Resources? = null

    constructor()

    constructor(iAudioCallback: IAudioCallback?, resources: Resources?) {
        this.iAudioCallback = iAudioCallback
        this.resourcesRef = resources
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAddAudioBinding.inflate(inflater, container, false)
        addAudioBinding = binding
        val root: LinearLayout = binding.root

        if (resourcesRef != null && iAudioCallback != null) {
            (root.findViewById(R.id.tv_extract) as TextCustumFont)
                .text = resourcesRef!!.getString(R.string.extract_audio)
            (root.findViewById(R.id.tv_audio) as TextCustumFont)
                .text = resourcesRef!!.getString(R.string.audio)

            root.findViewById<View>(R.id.btn_upload).setOnClickListener {
                iAudioCallback?.upload()
            }
            root.findViewById<View>(R.id.btn_extract).setOnClickListener {
                iAudioCallback?.extract()
            }
            root.findViewById<View>(R.id.btn_close).setOnClickListener {
                iAudioCallback?.cancel()
            }
        }
        return root
    }

    override fun onDestroyView() {
        addAudioBinding = null
        instance = null
        iAudioCallback = null
        super.onDestroyView()
    }
}
