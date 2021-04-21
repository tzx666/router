package buct.tzx.javademo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import buct.tzx.routerannotation.Router
import buct.tzx.routerapi.BuctRouter

@Router(Path = "/demo/main2")
class MainActivity4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)
        var button =findViewById<Button>(R.id.button)
        button.setOnClickListener {
            BuctRouter.getInstance().naviagtion(this,"/app/main")
        }
    }
}