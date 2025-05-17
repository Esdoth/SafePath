package com.example.safepath.auth

import android.content.Context
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.regions.Regions

class CognitoHelper(context: Context) {
    // Configuración de tu User Pool de Cognito
    private val userPoolId = "us-east-1_g5pc8BzKf"
    private val clientId = "2hnkfpve2cdlufvoc97e5r8lu4"
    private val clientSecret: String? = null
    private val region = Regions.US_EAST_1

    val userPool: CognitoUserPool by lazy {
        CognitoUserPool(
            context,
            userPoolId,
            clientId,
            clientSecret,
            region
        )
    }
}