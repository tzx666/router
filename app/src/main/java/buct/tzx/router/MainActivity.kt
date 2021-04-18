package buct.tzx.router

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import buct.tzx.routerannotation.Router
import buct.tzx.routerapi.BuctRouter

@Router(Path = "/app/main")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var btn = findViewById(R.id.btn) as Button
        var btn1 = findViewById(R.id.btn1) as Button
        btn.setOnClickListener {
            BuctRouter.getInstance().init(this)
        }
        btn1.setOnClickListener {
            BuctRouter.getInstance().naviagtion(this,"/app/second")
        }
    }
}