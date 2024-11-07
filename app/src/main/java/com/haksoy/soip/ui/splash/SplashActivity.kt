package com.haksoy.soip.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.haksoy.soip.R
import com.haksoy.soip.data.FirebaseDao
import com.haksoy.soip.ui.auth.AuthenticationActivity
import com.haksoy.soip.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    @Inject
    lateinit var firebaseDao: FirebaseDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseDao.setLanguageCode("TR")//getCountryIso()
        val intent: Intent = if (firebaseDao.isAuthUserExist()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, AuthenticationActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}