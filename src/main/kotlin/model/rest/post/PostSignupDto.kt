package model.rest.post

data class PostSignupDto(val email: String?, val password: String?, val inviteCode: String?, val license: Boolean)
