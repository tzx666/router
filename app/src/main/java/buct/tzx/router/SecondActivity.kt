package buct.tzx.router

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import buct.tzx.routerannotation.Router

@Router(Path = "/app/second")
class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
    }
}