package hazem.nurmontage.videoquran.ui.share

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityShareWithMeBinding

class ShareWithMeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShareWithMeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareWithMeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Handle incoming shared media
        val intent = intent
        val uri = intent?.clipData?.getItemAt(0)?.uri ?: intent?.data
        if (uri != null) {
            // Process the shared file — launch engine with it
        }
    }
}
