package buct.tzx.router

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import buct.tzx.routerannotation.Router


@Router(Path = "/app/second")
class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        val intent = intent
        var bundle=intent.getBundleExtra("buctrouter")
        // 在这里拿到你想要的值
        var value=bundle?.getString("a")
        Toast.makeText(this,value,Toast.LENGTH_SHORT).show()
    }
}