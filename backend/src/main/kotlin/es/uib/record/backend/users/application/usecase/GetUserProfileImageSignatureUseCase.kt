package es.uib.record.backend.users.application.usecase

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import es.uib.record.backend.users.application.usecase.dto.UserProfileImageSignatureResponseDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GetUserProfileImageSignatureUseCase(
    @Value($$"${application.cloudinary.api-key}") private val apiKey: String,
    @Value($$"${application.cloudinary.api-secret}") private val apiSecret: String,
    @Value($$"${application.cloudinary.cloud-name}") private val cloudName: String,
    @Value($$"${application.cloudinary.user-avatar-folder}") private val avatarFolder: String,
    @Value($$"${application.cloudinary.user-transformation}") private val avatarTransformation: String
) {
    fun execute(): UserProfileImageSignatureResponseDto {
        val cloudinary = Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        ))

        val timestamp = (System.currentTimeMillis() / 1000L).toString()
        
        val paramsToSign = mutableMapOf<String, Any>(
            "timestamp" to timestamp,
            "folder" to avatarFolder,
            "transformation" to avatarTransformation
        )

        val signature = cloudinary.apiSignRequest(paramsToSign, apiSecret)

        return UserProfileImageSignatureResponseDto(
            signature = signature,
            timestamp = timestamp,
            apiKey = apiKey,
            cloudName = cloudName,
            transformation = avatarTransformation,
            folder = avatarFolder
        )
    }
}