package buct.tzx.javademo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import buct.tzx.routerannotation.Router

@Router(Path = "/demo/main")
class MainActivity1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}