import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val first_name: String,
    val last_name: String,
    val email: String,
    val password: String

)