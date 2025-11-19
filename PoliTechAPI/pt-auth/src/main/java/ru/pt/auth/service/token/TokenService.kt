package ru.pt.auth.service.token

class TokenService(

): TokenInterface {

    override fun checkToken(token: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun checkPermissions(token: String, requiredRole: String): Boolean {
        TODO("Not yet implemented")
    }
}