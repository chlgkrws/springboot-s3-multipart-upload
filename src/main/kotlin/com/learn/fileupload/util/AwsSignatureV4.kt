package com.learn.fileupload.util

import com.learn.fileupload.config.S3Properties
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class AwsSignatureV4(
    private val s3Properties: S3Properties
) {
	companion object {
		private const val AWS_ALGORITHM = "AWS4-HMAC-SHA256"
		private const val SERVICE = "s3"
		private const val REQUEST_TYPE = "aws4_request"
	}

	fun generateSignature(
		httpMethod: String,
		canonicalUri: String,
		queryParams: Map<String, String>,
		headers: Map<String, String>,
		payload: String,
		dateTime: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
	): SignatureData {
		val amzDate = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))
		val dateStamp = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

		val canonicalHeaders = headers.toSortedMap()
			.map { "${it.key.lowercase()}:${it.value}" }
			.joinToString("\n")

		val signedHeaders = headers.keys
			.map { it.lowercase() }
			.sorted()
			.joinToString(";")

		val canonicalRequest = createCanonicalRequest(
			httpMethod,
			canonicalUri,
			queryParams,
			canonicalHeaders,
			signedHeaders,
		)

		val stringToSign = createStringToSign(
			dateTime,
			canonicalRequest,
			dateStamp
		)

		val signature = calculateSignature(
			dateStamp,
			stringToSign
		)

		val authorizationHeader = createAuthorizationHeader(
			dateStamp,
			signedHeaders,
			signature
		)

		return SignatureData(
			signature = signature,
			authorizationHeader = authorizationHeader,
			amzDate = amzDate
		)
	}

	private fun createCanonicalRequest(
		httpMethod: String,
		canonicalUri: String,
		queryParams: Map<String, String>,
		canonicalHeaders: String,
		signedHeaders: String,
	): String {
		val canonicalQueryString = queryParams.entries
			.sortedBy { it.key }
			.joinToString("&") { (key, value) ->
				"$key=$value"
			}

		return buildString {
			append(httpMethod)
			append("\n")
			append(canonicalUri)
			append("\n")
			append(canonicalQueryString)
			append("\n")
			append(canonicalHeaders)
			append("\n\n")
			append(signedHeaders)
			append("\n")
			append("UNSIGNED-PAYLOAD")
		}
	}

	private fun createStringToSign(
		dateTime: LocalDateTime,
		canonicalRequest: String,
		dateStamp: String
	): String {
		return buildString {
			append("AWS4-HMAC-SHA256")
			append("\n")
			append(dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")))
			append("\n")
			append("$dateStamp/${s3Properties.region}/s3/aws4_request")
			append("\n")
			append(hash(canonicalRequest))
		}
	}

	private fun calculateSignature(dateStamp: String, stringToSign: String): String {
		val kSecret = "AWS4${s3Properties.secretKey}".toByteArray()
		val kDate = hmacSHA256(dateStamp, kSecret)
		val kRegion = hmacSHA256(s3Properties.region, kDate)
		val kService = hmacSHA256(SERVICE, kRegion)
		val kSigning = hmacSHA256(REQUEST_TYPE, kService)
		return hmacSHA256(stringToSign, kSigning).toHex()
	}

	private fun createAuthorizationHeader(
		dateStamp: String,
		signedHeaders: String,
		signature: String
	): String {
		return "$AWS_ALGORITHM " +
				"Credential=${s3Properties.accessKey}/$dateStamp/" +
				"${s3Properties.region}/$SERVICE/$REQUEST_TYPE, " +
				"SignedHeaders=$signedHeaders, " +
				"Signature=$signature"
	}

	private fun hmacSHA256(data: String, key: ByteArray): ByteArray {
		val mac = Mac.getInstance("HmacSHA256")
		mac.init(SecretKeySpec(key, "HmacSHA256"))
		return mac.doFinal(data.toByteArray())
	}

	private fun hash(content: String): String {
		val md = MessageDigest.getInstance("SHA-256")
		return md.digest(content.toByteArray()).toHex()
	}

	private fun ByteArray.toHex(): String {
		return joinToString("") { "%02x".format(it) }
	}
}

data class SignatureData(
	val signature: String,
	val authorizationHeader: String,
	val amzDate: String
) 