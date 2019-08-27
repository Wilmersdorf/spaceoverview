package model.rest

data class PostSignupDto(val email: String?, val password: String?, val inviteCode: String?, val license: Boolean)