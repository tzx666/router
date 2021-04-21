package buct.tzx.router

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import buct.tzx.javademo.MainActivity1
import buct.tzx.javademo.MainActivity3
import buct.tzx.javademo.MainActivity4
import buct.tzx.routerannotation.Router
import buct.tzx.routerapi.BuctRouter

@Router(Path = "/app/main")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var btn = findViewById(R.id.btn) as Button
        var btn1 = findViewById(R.id.btn1) as Button
        var btn2 = findViewById(R.id.btn2) as Button
        var btn3 = findViewById(R.id.btn3) as Button
        btn.setOnClickListener {
            BuctRouter.getInstance().init(this)
        }
        btn1.setOnClickListener {
            BuctRouter.getInstance().naviagtion(this,"/app/second?a=2")
        }
        btn2.setOnClickListener {
            BuctRouter.getInstance().naviagtion(this,"/demo/main2")
        }
        btn3.setOnClickListener {
            val intent = Intent(this@MainActivity, MainActivity4::class.java)
            startActivity(intent)
        }
    }
}