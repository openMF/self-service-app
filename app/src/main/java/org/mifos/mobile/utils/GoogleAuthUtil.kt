package org.mifos.mobile.utils

class GoogleAuthUtil {

    companion object{
        fun init(){
            System.setProperty("com.warrenstrange.googleauth.rng.algorithmProvider",
                    "AndroidOpenSSL")
        }
    }
}