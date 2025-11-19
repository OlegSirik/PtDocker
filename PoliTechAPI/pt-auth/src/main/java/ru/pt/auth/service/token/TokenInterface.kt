package ru.pt.auth.service.token

interface TokenInterface {

    fun checkToken(token: String): Boolean

    fun checkPermissions(token: String, requiredRole: String): Boolean


}